package org.multiverse.stms.gamma.integration.locks;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.LockMode;
import org.multiverse.api.exceptions.ReadWriteConflict;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactionalobjects.GammaTxnLong;
import org.multiverse.stms.gamma.transactions.GammaTxn;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.multiverse.TestUtils.*;
import static org.multiverse.api.GlobalStmInstance.getGlobalStmInstance;
import static org.multiverse.api.TxnThreadLocal.clearThreadLocalTxn;
import static org.multiverse.stms.gamma.GammaTestUtils.*;

public class ReadLockTest {

    private GammaStm stm;

    @Before
    public void setUp() {
        stm = (GammaStm) getGlobalStmInstance();
        clearThreadLocalTxn();
    }

    @Test
    public void whenUnlocked() {
        GammaTxnLong ref = new GammaTxnLong(stm, 10);

        GammaTxn tx = stm.newDefaultTxn();
        ref.getLock().acquire(tx, LockMode.Read);

        assertIsActive(tx);
        assertRefHasReadLock(ref, tx);
        assertReadLockCount(ref, 1);
    }

    @Test
    public void whenReadLockAlreadyAcquiredByOther_thenReadLockSuccess() {
        GammaTxnLong ref = new GammaTxnLong(stm);

        GammaTxn otherTx = stm.newDefaultTxn();
        ref.getLock().acquire(otherTx, LockMode.Read);

        GammaTxn tx = stm.newDefaultTxn();
        ref.getLock().acquire(tx, LockMode.Read);

        assertIsActive(tx);
        assertRefHasReadLock(ref, otherTx);
        assertRefHasReadLock(ref, tx);
        assertReadLockCount(ref, 2);
    }


    @Test
    public void whenWriteLockAlreadyAcquiredByOther_thenReadLockNotPossible() {
        GammaTxnLong ref = new GammaTxnLong(stm);

        GammaTxn otherTx = stm.newDefaultTxn();
        ref.getLock().acquire(otherTx, LockMode.Write);

        GammaTxn tx = stm.newDefaultTxn();
        try {
            ref.getLock().acquire(tx, LockMode.Read);
            fail();
        } catch (ReadWriteConflict expected) {

        }

        assertIsAborted(tx);
        assertRefHasWriteLock(ref, otherTx);
    }

    @Test
    public void whenExclusiveLockAlreadyAcquiredByOther_thenReadLockNotPossible() {
        GammaTxnLong ref = new GammaTxnLong(stm);

        GammaTxn otherTx = stm.newDefaultTxn();
        ref.getLock().acquire(otherTx, LockMode.Exclusive);

        GammaTxn tx = stm.newDefaultTxn();
        try {
            ref.getLock().acquire(tx, LockMode.Read);
            fail();
        } catch (ReadWriteConflict expected) {

        }

        assertIsAborted(tx);
        assertRefHasExclusiveLock(ref, otherTx);
    }

    @Test
    public void whenReadLockAcquiredByOther_thenReadPossible() {
        long initialValue = 10;
        GammaTxnLong ref = new GammaTxnLong(stm, initialValue);

        GammaTxn otherTx = stm.newDefaultTxn();
        ref.getLock().acquire(otherTx, LockMode.Read);

        GammaTxn tx = stm.newDefaultTxn();
        long result = ref.get(tx);

        assertIsActive(tx);
        assertEquals(initialValue, result);
        assertRefHasReadLock(ref, otherTx);
        assertReadLockCount(ref, 1);
    }

    @Test
    public void whenReadLockAcquiredByOther_thenWritePossible() {
        long initialValue = 10;
        GammaTxnLong ref = new GammaTxnLong(stm, initialValue);

        GammaTxn otherTx = stm.newDefaultTxn();
        ref.getLock().acquire(otherTx, LockMode.Read);

        GammaTxn tx = stm.newDefaultTxn();
        ref.set(tx, initialValue + 1);

        assertEquals(initialValue + 1, ref.get(tx));
        assertIsActive(tx);
        assertRefHasReadLock(ref, otherTx);
        assertReadLockCount(ref, 1);
    }

    @Test
    public void whenReadLockAcquiredByOtherAndDirtyTransaction_thenReadFails() {
        long initialValue = 10;
        GammaTxnLong ref = new GammaTxnLong(stm, initialValue);

        GammaTxn otherTx = stm.newDefaultTxn();
        ref.getLock().acquire(otherTx, LockMode.Read);

        GammaTxn tx = stm.newDefaultTxn();
        ref.set(tx, initialValue + 1);

        try {
            tx.commit();
            fail();
        } catch (ReadWriteConflict expected) {
        }

        assertIsAborted(tx);
        assertRefHasReadLock(ref, otherTx);
        assertReadLockCount(ref, 1);
    }

    @Test
    public void whenReadLockAcquiredByOtherAndDirtyTransaction_thenPrepareFails() {
        long initialValue = 10;
        GammaTxnLong ref = new GammaTxnLong(stm, initialValue);

        GammaTxn otherTx = stm.newDefaultTxn();
        ref.getLock().acquire(otherTx, LockMode.Read);

        GammaTxn tx = stm.newDefaultTxn();
        ref.set(tx, initialValue + 1);

        try {
            tx.prepare();
            fail();
        } catch (ReadWriteConflict expected) {
        }

        assertIsAborted(tx);
        assertRefHasReadLock(ref, otherTx);
        assertReadLockCount(ref, 1);
    }

    @Test
    public void whenPreviouslyReadByOtherThread_thenNoProblems() {
        GammaTxnLong ref = new GammaTxnLong(stm, 10);

        GammaTxn otherTx = stm.newDefaultTxn();
        ref.get(otherTx);

        GammaTxn tx = stm.newDefaultTxn();
        ref.getLock().acquire(tx, LockMode.Read);

        long result = ref.get(otherTx);
        assertEquals(10, result);
    }

    @Test
    public void whenReadLockAcquiredAcquired_thenAcquireReadLockSuccess() {
        GammaTxnLong ref = new GammaTxnLong(stm, 5);

        GammaTxn tx = stm.newDefaultTxn();
        ref.getLock().acquire(tx, LockMode.Read);
        ref.getLock().acquire(tx, LockMode.Read);

        assertIsActive(tx);
        assertRefHasReadLock(ref, tx);
        assertReadLockCount(ref, 1);
    }

    @Test
    public void whenWriteLockAcquired_thenUpgradableToReadLockIgnored() {
        GammaTxnLong ref = new GammaTxnLong(stm, 5);

        GammaTxn tx = stm.newDefaultTxn();
        ref.getLock().acquire(tx, LockMode.Write);
        ref.getLock().acquire(tx, LockMode.Read);

        assertIsActive(tx);
        assertRefHasWriteLock(ref, tx);
    }

    @Test
    public void whenExclusiveLockAcquired_thenUpgradableToReadLockIgnored() {
        GammaTxnLong ref = new GammaTxnLong(stm, 5);

        GammaTxn tx = stm.newDefaultTxn();
        ref.getLock().acquire(tx, LockMode.Exclusive);
        ref.getLock().acquire(tx, LockMode.Read);

        assertIsActive(tx);
        assertRefHasExclusiveLock(ref, tx);
    }

    @Test
    public void whenTransactionCommits_thenReadLockReleased() {
        GammaTxnLong ref = new GammaTxnLong(stm, 5);

        GammaTxn tx = stm.newDefaultTxn();
        ref.getLock().acquire(tx, LockMode.Read);
        tx.commit();

        assertIsCommitted(tx);
        assertRefHasNoLocks(ref);
    }

    @Test
    public void whenTransactionIsPrepared_thenReadLockRemains() {
        GammaTxnLong ref = new GammaTxnLong(stm, 5);

        GammaTxn tx = stm.newDefaultTxn();
        ref.getLock().acquire(tx, LockMode.Read);
        tx.prepare();

        assertIsPrepared(tx);
        assertRefHasReadLock(ref, tx);
    }

    @Test
    public void whenTransactionAborts_thenReadLockIsReleased() {
        GammaTxnLong ref = new GammaTxnLong(stm, 5);

        GammaTxn tx = stm.newDefaultTxn();
        ref.getLock().acquire(tx, LockMode.Read);
        tx.abort();

        assertIsAborted(tx);
        assertRefHasNoLocks(ref);
    }

    @Test
    public void whenReadWriteConflictAfterInitialRead_thenReadWriteLockFails() {
        GammaTxnLong ref = new GammaTxnLong(stm, 5);

        GammaTxn tx = stm.newDefaultTxn();
        ref.get(tx);

        ref.atomicIncrementAndGet(1);

        try {
            ref.getLock().acquire(tx, LockMode.Read);
            fail();
        } catch (ReadWriteConflict expected) {
        }

        assertRefHasNoLocks(ref);
        assertIsAborted(tx);
    }
}
