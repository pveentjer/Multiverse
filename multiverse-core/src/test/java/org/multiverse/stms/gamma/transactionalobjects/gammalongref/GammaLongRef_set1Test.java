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
import org.multiverse.stms.gamma.transactionalobjects.GammaTxnLong;
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
public class GammaLongRef_set1Test {

    private final GammaTxnFactory transactionFactory;
    private final GammaStm stm;

    public GammaLongRef_set1Test(GammaTxnFactory transactionFactory) {
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
        GammaTxnLong ref = new GammaTxnLong(stm, 10);
        long version = ref.getVersion();

        GammaTxn tx = transactionFactory.newTransaction();
        tx.prepare();
        setThreadLocalTxn(tx);

        try {
            ref.set(30);
            fail();
        } catch (PreparedTxnException expected) {

        }

        assertRefHasNoLocks(ref);
        assertSurplus(ref, 0);
        assertIsAborted(tx);
        assertEquals(10, ref.atomicGet());
        assertVersionAndValue(ref, version, 10);
    }

    @Test
    public void whenActiveTransactionAvailable_thenPreparedTxnException() {
        GammaTxnLong ref = new GammaTxnLong(stm, 10);
        long version = ref.getVersion();

        GammaTxn tx = transactionFactory.newTransaction();
        setThreadLocalTxn(tx);
        long value = ref.set(20);

        assertIsActive(tx);
        assertEquals(20, value);
        assertRefHasNoLocks(ref);
        assertSurplus(ref, 0);
        assertWriteBiased(ref);
        assertVersionAndValue(ref, version, 10);

        tx.commit();

        assertIsCommitted(tx);
        assertEquals(20, ref.atomicGet());
        assertSame(tx, getThreadLocalTxn());
        assertRefHasNoLocks(ref);
        assertSurplus(ref, 0);
        assertWriteBiased(ref);
    }

    @Test
    public void whenLocked_thenReadWriteConflict() {
        long initialValue = 10;
        GammaTxnLong ref = new GammaTxnLong(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTxn otherTx = transactionFactory.newTransaction();
        ref.getLock().acquire(otherTx, LockMode.Exclusive);

        GammaTxn tx = transactionFactory.newTransaction();
        setThreadLocalTxn(tx);
        try {
            ref.set(20);
            fail();
        } catch (ReadWriteConflict expected) {
        }

        assertIsAborted(tx);

        otherTx.abort();
        assertVersionAndValue(ref, initialVersion, initialValue);
        assertSurplus(ref, 0);
        assertWriteBiased(ref);
        assertRefHasNoLocks(ref);
        assertSame(tx, getThreadLocalTxn());
    }

    @Test
    public void whenNoChange() {
        long initialValue = 10;
        GammaTxnLong ref = new GammaTxnLong(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTxn tx = transactionFactory.newTransaction();
        setThreadLocalTxn(tx);

        long result = ref.set(initialValue);

        tx.commit();

        assertIsCommitted(tx);
        assertEquals(initialValue, result);
        assertRefHasNoLocks(ref);
        assertSurplus(ref, 0);
        assertWriteBiased(ref);
        assertVersionAndValue(ref, initialVersion, initialValue);
    }

    @Test
    public void whenNoTransactionFound_thenNoTransactionFoundException() {
        long initialValue = 10;
        GammaTxnLong ref = new GammaTxnLong(stm, initialValue);
        long initialVersion = ref.getVersion();

        try {
            ref.set(20);
            fail();
        } catch (TxnMandatoryException expected) {

        }

        assertRefHasNoLocks(ref);
        assertSurplus(ref, 0);
        assertWriteBiased(ref);
        assertVersionAndValue(ref, initialVersion, initialValue);
    }

    @Test
    public void whenPrivatizedBySelf_thenSuccess() {
        GammaTxnLong ref = new GammaTxnLong(stm, 100);
        long version = ref.getVersion();

        GammaTxn tx = transactionFactory.newTransaction();
        setThreadLocalTxn(tx);

        ref.getLock().acquire(LockMode.Exclusive);
        long value = ref.set(200);

        assertEquals(200, value);
        assertRefHasExclusiveLock(ref, tx);
        assertSurplus(ref, 1);
        assertWriteBiased(ref);
        assertIsActive(tx);
        assertSame(tx, getThreadLocalTxn());
        assertVersionAndValue(ref, version, 100);
    }

    @Test
    public void whenEnsuredBySelf_thenSuccess() {
        GammaTxnLong ref = new GammaTxnLong(stm, 100);
        long version = ref.getVersion();

        GammaTxn tx = transactionFactory.newTransaction();
        setThreadLocalTxn(tx);

        ref.getLock().acquire(LockMode.Write);
        long value = ref.set(200);

        assertEquals(200, value);
        assertRefHasWriteLock(ref, tx);
        assertSurplus(ref, 1);
        assertWriteBiased(ref);
        assertIsActive(tx);
        assertSame(tx, getThreadLocalTxn());
        assertVersionAndValue(ref, version, 100);
    }

    @Test
    public void whenPrivatizedByOtherAndFirstTimeRead_thenReadConflict() {
        GammaTxnLong ref = new GammaTxnLong(stm, 100);
        long version = ref.getVersion();

        GammaTxn tx = transactionFactory.newTransaction();
        setThreadLocalTxn(tx);

        GammaTxn otherTx = transactionFactory.newTransaction();
        ref.getLock().acquire(otherTx, LockMode.Exclusive);

        try {
            ref.set(200);
            fail();
        } catch (ReadWriteConflict expected) {
        }

        assertRefHasExclusiveLock(ref, otherTx);
        assertSurplus(ref, 1);
        assertWriteBiased(ref);
        assertIsActive(otherTx);
        assertIsAborted(tx);
        assertSame(tx, getThreadLocalTxn());
        assertVersionAndValue(ref, version, 100);
    }

    @Test
    public void whenEnsuredByOther_thenSetPossibleButCommitFails() {
        GammaTxnLong ref = new GammaTxnLong(stm, 100);
        long version = ref.getVersion();

        GammaTxn tx = transactionFactory.newTransaction();
        setThreadLocalTxn(tx);

        GammaTxn otherTx = transactionFactory.newTransaction();
        ref.getLock().acquire(otherTx, LockMode.Write);

        long value = ref.set(200);
        assertEquals(200, value);
        assertRefHasWriteLock(ref, otherTx);
        assertSurplus(ref, 1);
        assertWriteBiased(ref);
        assertIsActive(otherTx);
        assertIsActive(tx);
        assertSame(tx, getThreadLocalTxn());
        assertVersionAndValue(ref, version, 100);

        try {
            tx.commit();
            fail();
        } catch (ReadWriteConflict e) {

        }

        assertRefHasWriteLock(ref, otherTx);
        assertSurplus(ref, 1);
        assertWriteBiased(ref);
        assertIsActive(otherTx);
        assertIsAborted(tx);
        assertSame(tx, getThreadLocalTxn());
        assertVersionAndValue(ref, version, 100);
    }

    @Test
    public void whenCommittedTransactionAvailable_thenExecutedAtomically() {
        long initialValue = 10;
        GammaTxnLong ref = new GammaTxnLong(stm, 10);
        long initialVersion = ref.getVersion();

        GammaTxn tx = transactionFactory.newTransaction();
        tx.commit();
        setThreadLocalTxn(tx);

        try {
            ref.set(initialValue + 1);
            fail();
        } catch (DeadTxnException expected) {

        }

        assertIsCommitted(tx);
        assertVersionAndValue(ref, initialVersion, initialValue);
        assertSame(tx, getThreadLocalTxn());
        assertSurplus(ref, 0);
        assertWriteBiased(ref);
        assertRefHasNoLocks(ref);
    }

    @Test
    public void whenAbortedTransactionAvailable_thenDeadTxnException() {
        long initialValue = 10;
        GammaTxnLong ref = new GammaTxnLong(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTxn tx = transactionFactory.newTransaction();
        tx.commit();
        setThreadLocalTxn(tx);

        try {
            ref.set(20);
            fail();
        } catch (DeadTxnException expected) {

        }

        assertIsCommitted(tx);
        assertVersionAndValue(ref, initialVersion, initialValue);
        assertSurplus(ref, 0);
        assertWriteBiased(ref);
        assertRefHasNoLocks(ref);
        assertSame(tx, getThreadLocalTxn());
    }
}
