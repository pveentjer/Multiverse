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
public class GammaLongRef_incrementAndGet1Test {

    private final GammaTxnFactory transactionFactory;
    private final GammaStm stm;

    public GammaLongRef_incrementAndGet1Test(GammaTxnFactory transactionFactory) {
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
    public void whenPreparedTransactionAvailable_thenPreparedTxnException() {
        long initialValue = 10;
        GammaLongRef ref = new GammaLongRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTxn tx = transactionFactory.newTransaction();
        tx.prepare();
        setThreadLocalTxn(tx);

        long amount = 30;
        try {
            ref.incrementAndGet(amount);
            fail();
        } catch (PreparedTxnException expected) {

        }

        assertIsAborted(tx);
        assertVersionAndValue(ref, initialVersion, initialValue);
        assertRefHasNoLocks(ref);
    }

    @Test
    public void whenActiveTransactionAvailable() {
        int initialValue = 10;
        GammaLongRef ref = new GammaLongRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTxn tx = transactionFactory.newTransaction();
        setThreadLocalTxn(tx);
        long amount = 20;
        long value = ref.incrementAndGet(amount);
        tx.commit();

        assertEquals(initialValue + amount, value);
        assertIsCommitted(tx);
        assertVersionAndValue(ref, initialVersion + 1, initialValue + amount);
        assertRefHasNoLocks(ref);
        assertSame(tx, getThreadLocalTxn());
    }

    @Test
    public void whenNoTransactionAvailable_thenNoTransactionFoundException() {
        long initialValue = 10;
        GammaLongRef ref = new GammaLongRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        try {
            ref.incrementAndGet(1);
            fail();
        } catch (TxnMandatoryException expected) {

        }

        assertVersionAndValue(ref, initialVersion, initialValue);
        assertNull(getThreadLocalTxn());
        assertSurplus(ref, 0);
        assertRefHasNoLocks(ref);
    }

    @Test
    public void whenCommittedTransactionAvailable_thenExecutedAtomically() {
        long initialValue = 10;
        GammaLongRef ref = new GammaLongRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTxn tx = transactionFactory.newTransaction();
        setThreadLocalTxn(tx);
        tx.commit();

        try {
            ref.incrementAndGet(20);
            fail();
        } catch (DeadTxnException expected) {

        }

        assertIsCommitted(tx);
        assertVersionAndValue(ref, initialVersion, initialValue);
        assertSame(tx, getThreadLocalTxn());
        assertSurplus(ref, 0);
        assertRefHasNoLocks(ref);
    }

    @Test
    public void whenAbortedTransactionAvailable_thenDeadTxnException() {
        long initialValue = 10;
        GammaLongRef ref = new GammaLongRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTxn tx = transactionFactory.newTransaction();
        setThreadLocalTxn(tx);
        tx.abort();

        try {
            ref.incrementAndGet(20);
            fail();
        } catch (DeadTxnException expected) {
        }

        assertIsAborted(tx);
        assertVersionAndValue(ref, initialVersion, initialValue);
        assertSame(tx, getThreadLocalTxn());
        assertSurplus(ref, 0);
        assertRefHasNoLocks(ref);
    }

    @Test
    public void whenReadLockAlreadyAcquiredBySelf_thenSuccess() {
        long initialValue = 10;
        GammaLongRef ref = new GammaLongRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTxn tx = transactionFactory.newTransaction();
        setThreadLocalTxn(tx);

        ref.getLock().acquire(LockMode.Read);
        int amount = 1;
        long result = ref.incrementAndGet(amount);

        assertEquals(initialValue + amount, result);
        assertIsActive(tx);
        assertSame(tx, getThreadLocalTxn());
        assertSurplus(ref, 1);
        assertRefHasReadLock(ref, tx);
    }

    @Test
    public void whenWriteLockAlreadyAcquiredBySelf_thenSuccess() {
        long initialValue = 10;
        GammaLongRef ref = new GammaLongRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTxn tx = transactionFactory.newTransaction();
        setThreadLocalTxn(tx);

        ref.getLock().acquire(LockMode.Write);
        int amount = 1;
        long result = ref.incrementAndGet(amount);

        assertEquals(initialValue + amount, result);
        assertIsActive(tx);
        assertSame(tx, getThreadLocalTxn());
        assertSurplus(ref, 1);
        assertRefHasWriteLock(ref, tx);
    }

    @Test
    public void whenExclusiveLockAlreadyAcquiredBySelf_thenSuccess() {
        GammaLongRef ref = new GammaLongRef(stm, 10);

        GammaTxn tx = transactionFactory.newTransaction();
        setThreadLocalTxn(tx);

        ref.getLock().acquire(LockMode.Exclusive);
        long result = ref.incrementAndGet(1);

        assertEquals(11, result);
        assertIsActive(tx);
        assertSame(tx, getThreadLocalTxn());
        assertSurplus(ref, 1);
        assertRefHasExclusiveLock(ref, tx);
    }

    @Test
    public void whenExclusiveLockAcquiredByOther_thenReadConflict() {
        GammaLongRef ref = new GammaLongRef(stm, 10);
        long version = ref.getVersion();

        GammaTxn tx = transactionFactory.newTransaction();
        setThreadLocalTxn(tx);

        GammaTxn otherTx = transactionFactory.newTransaction();

        ref.getLock().acquire(otherTx, LockMode.Exclusive);

        try {
            ref.incrementAndGet(1);
            fail();
        } catch (ReadWriteConflict expected) {

        }

        assertIsAborted(tx);
        assertSame(tx, getThreadLocalTxn());
        assertSurplus(ref, 1);
        assertRefHasExclusiveLock(ref, otherTx);
        assertVersionAndValue(ref, version, 10);
    }

    @Test
    public void whenWriteLockAcquiredByOther_thenIncrementSucceedsButCommitFails() {
        GammaLongRef ref = new GammaLongRef(stm, 10);
        long version = ref.getVersion();

        GammaTxn tx = transactionFactory.newTransaction();
        setThreadLocalTxn(tx);

        GammaTxn otherTx = transactionFactory.newTransaction();

        ref.getLock().acquire(otherTx, LockMode.Write);

        long result = ref.incrementAndGet(1);
        assertEquals(11, result);

        assertIsActive(tx);
        assertSame(tx, getThreadLocalTxn());
        assertSurplus(ref, 1);
        assertRefHasWriteLock(ref, otherTx);
        assertSame(version, ref.getVersion());
        assertEquals(10, ref.atomicWeakGet());

        try {
            tx.commit();
            fail();
        } catch (ReadWriteConflict expected) {
        }

        assertIsAborted(tx);
        assertSame(tx, getThreadLocalTxn());
        assertSurplus(ref, 1);
        assertRefHasWriteLock(ref, otherTx);
        assertSame(version, ref.getVersion());
        assertEquals(10, ref.atomicWeakGet());
    }

    @Test
    public void whenListenersAvailable() {
        long initialValue = 10;
        GammaLongRef ref = new GammaLongRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        long amount = 4;
        LongRefAwaitThread thread = new LongRefAwaitThread(ref, initialValue + amount);
        thread.start();

        sleepMs(500);

        GammaTxn tx = transactionFactory.newTransaction();
        setThreadLocalTxn(tx);
        long result = ref.incrementAndGet(amount);
        tx.commit();

        joinAll(thread);

        assertEquals(initialValue + amount, result);
        assertRefHasNoLocks(ref);
        assertVersionAndValue(ref, initialVersion + 1, initialValue + amount);
    }
}
