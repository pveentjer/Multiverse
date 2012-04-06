package org.multiverse.stms.gamma.transactionalobjects.gammalongref;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.multiverse.api.LockMode;
import org.multiverse.api.TxnFactory;
import org.multiverse.api.exceptions.DeadTxnException;
import org.multiverse.api.exceptions.PreparedTxnException;
import org.multiverse.api.exceptions.ReadWriteConflict;
import org.multiverse.api.exceptions.TxnMandatoryException;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactionalobjects.GammaLongRef;
import org.multiverse.stms.gamma.transactions.GammaTxn;
import org.multiverse.stms.gamma.transactions.GammaTxnFactory;
import org.multiverse.stms.gamma.transactions.fat.FatFixedLengthGammaTxnFactory;
import org.multiverse.stms.gamma.transactions.fat.FatMonoGammaTxnFactory;
import org.multiverse.stms.gamma.transactions.fat.FatVariableLengthGammaTxnFactory;

import java.util.Collection;

import static java.util.Arrays.asList;
import static org.junit.Assert.*;
import static org.multiverse.TestUtils.*;
import static org.multiverse.api.TxnThreadLocal.*;
import static org.multiverse.stms.gamma.GammaTestUtils.*;

@RunWith(Parameterized.class)
public class GammaLongRef_getAndSet1Test {
    private final GammaTxnFactory transactionFactory;
    private final GammaStm stm;

    public GammaLongRef_getAndSet1Test(GammaTxnFactory transactionFactory) {
        this.transactionFactory = transactionFactory;
        this.stm = transactionFactory.getConfiguration().getStm();
    }

    @Before
    public void setUp() {
        clearThreadLocalTxn();
    }

    @Parameterized.Parameters
    public static Collection<TxnFactory[]> configs() {
        return asList(
                new TxnFactory[]{new FatVariableLengthGammaTxnFactory(new GammaStm())},
                new TxnFactory[]{new FatFixedLengthGammaTxnFactory(new GammaStm())},
                new TxnFactory[]{new FatMonoGammaTxnFactory(new GammaStm())}
        );
    }

    @Test
    public void whenActiveTransactionAvailable() {
        long initialValue = 10;
        GammaLongRef ref = new GammaLongRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTxn tx = transactionFactory.newTransaction();
        setThreadLocalTxn(tx);
        long value = ref.getAndSet(initialValue + 2);
        tx.commit();

        assertEquals(initialValue, value);
        assertIsCommitted(tx);
        assertSame(tx, getThreadLocalTxn());
        assertVersionAndValue(ref, initialVersion + 1, initialValue + 2);
    }

    @Test
    public void whenNoChange() {
        long initialValue = 10;
        GammaLongRef ref = new GammaLongRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTxn tx = transactionFactory.newTransaction();
        setThreadLocalTxn(tx);
        long value = ref.getAndSet(initialValue);
        tx.commit();

        assertEquals(initialValue, value);
        assertIsCommitted(tx);
        assertSame(tx, getThreadLocalTxn());
        assertSurplus(ref, 0);
        assertVersionAndValue(ref, initialVersion, initialValue);
    }

    @Test
    public void whenEnsuredBySelf() {
        GammaLongRef ref = new GammaLongRef(stm, 10);
        long version = ref.getVersion();

        GammaTxn tx = transactionFactory.newTransaction();
        setThreadLocalTxn(tx);

        ref.getLock().acquire(LockMode.Write);
        long result = ref.getAndSet(20);

        assertEquals(10, result);
        assertIsActive(tx);
        assertRefHasWriteLock(ref, tx);
        assertSurplus(ref, 1);
        assertSame(tx, getThreadLocalTxn());
        assertVersionAndValue(ref, version, 10);
    }

    @Test
    public void whenPrivatizedBySelf() {
        GammaLongRef ref = new GammaLongRef(stm, 10);
        long version = ref.getVersion();

        GammaTxn tx = transactionFactory.newTransaction();
        setThreadLocalTxn(tx);

        ref.getLock().acquire(LockMode.Exclusive);
        long result = ref.getAndSet(20);

        assertEquals(10, result);
        assertIsActive(tx);
        assertRefHasExclusiveLock(ref, tx);
        assertSurplus(ref, 1);
        assertSame(tx, getThreadLocalTxn());
        assertVersionAndValue(ref, version, 10);
    }

    @Test
    public void whenEnsuredByOther_thenGetAndSetSucceedsButCommitFails() {
        GammaLongRef ref = new GammaLongRef(stm, 10);
        long version = ref.getVersion();

        GammaTxn tx = transactionFactory.newTransaction();
        setThreadLocalTxn(tx);

        GammaTxn otherTx = transactionFactory.newTransaction();

        ref.getLock().acquire(otherTx, LockMode.Write);

        long result = ref.getAndSet(20);
        assertEquals(10, result);
        assertIsActive(tx);
        assertRefHasWriteLock(ref, otherTx);
        assertSurplus(ref, 1);
        assertIsActive(otherTx);
        assertSame(tx, getThreadLocalTxn());
        assertVersionAndValue(ref, version, 10);

        try {
            tx.commit();
            fail();
        } catch (ReadWriteConflict expected) {
        }

        assertIsAborted(tx);
        assertIsActive(otherTx);
        assertRefHasWriteLock(ref, otherTx);
        assertSurplus(ref, 1);
        assertSame(tx, getThreadLocalTxn());
        assertVersionAndValue(ref, version, 10);
    }

    @Test
    public void whenPrivatizedByOther_thenReadConflict() {
        GammaLongRef ref = new GammaLongRef(stm, 10);
        long version = ref.getVersion();

        GammaTxn tx = transactionFactory.newTransaction();
        setThreadLocalTxn(tx);

        GammaTxn otherTx = transactionFactory.newTransaction();
        ref.getLock().acquire(otherTx, LockMode.Exclusive);

        try {
            ref.getAndSet(20);
            fail();
        } catch (ReadWriteConflict expected) {
        }

        assertIsAborted(tx);
        assertRefHasExclusiveLock(ref, otherTx);
        assertSurplus(ref, 1);
        assertIsActive(otherTx);
        assertSame(tx, getThreadLocalTxn());
        assertVersionAndValue(ref, version, 10);
    }

    @Test
    public void whenListenersAvailable() {
        long initialValue = 10;
        GammaLongRef ref = new GammaLongRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        long newValue = 20;
        LongRefAwaitThread thread = new LongRefAwaitThread(ref, newValue);
        thread.start();

        sleepMs(500);

        GammaTxn tx = transactionFactory.newTransaction();
        setThreadLocalTxn(tx);
        long result = ref.getAndSet(newValue);
        tx.commit();

        joinAll(thread);

        assertEquals(initialValue, result);
        assertRefHasNoLocks(ref);
        assertVersionAndValue(ref, initialVersion + 1, newValue);
    }

    @Test
    public void whenNoTransactionAvailable_thenNoTransactionFoundException() {
        long initialValue = 10;
        GammaLongRef ref = new GammaLongRef(stm, 10);
        long initialVersion = ref.getVersion();

        long newValue = 20;
        try {
            ref.getAndSet(newValue);
            fail();
        } catch (TxnMandatoryException expected) {
        }

        assertSurplus(ref, 0);
        assertRefHasNoLocks(ref);
        assertVersionAndValue(ref, initialVersion, initialValue);
    }

    @Test
    public void whenPreparedTransactionAvailable_thenPreparedTxnException() {
        long initialValue = 10;
        GammaLongRef ref = new GammaLongRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTxn tx = transactionFactory.newTransaction();
        tx.prepare();
        setThreadLocalTxn(tx);

        try {
            ref.getAndSet(30);
            fail();
        } catch (PreparedTxnException expected) {

        }

        assertIsAborted(tx);
        assertVersionAndValue(ref, initialVersion, initialValue);
    }

    @Test
    public void whenCommittedTransactionAvailable_thenDeadTxnException() {
        long initialValue = 10;
        GammaLongRef ref = new GammaLongRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTxn tx = transactionFactory.newTransaction();
        tx.commit();
        setThreadLocalTxn(tx);

        try {
            ref.getAndSet(initialValue + 1);
            fail();
        } catch (DeadTxnException expected) {

        }

        assertIsCommitted(tx);
        assertVersionAndValue(ref, initialVersion, initialValue);
    }

    @Test
    public void whenAbortedTransactionAvailable_thenDeadTxnException() {
        long initialValue = 10;
        GammaLongRef ref = new GammaLongRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTxn tx = transactionFactory.newTransaction();
        tx.abort();
        setThreadLocalTxn(tx);

        try {
            ref.getAndSet(initialValue + 1);
            fail();
        } catch (DeadTxnException expected) {

        }

        assertIsAborted(tx);
        assertVersionAndValue(ref, initialVersion, initialValue);
    }
}
