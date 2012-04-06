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

public class ExclusiveLockTest {
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
        ref.getLock().acquire(tx, LockMode.Exclusive);

        assertIsActive(tx);
        assertRefHasExclusiveLock(ref, tx);
    }

    @Test
    public void whenReadLockAlreadyAcquiredByOther_thenExclusiveLockNotPossible() {
        GammaTxnLong ref = new GammaTxnLong(stm);

        GammaTxn otherTx = stm.newDefaultTxn();
        ref.getLock().acquire(otherTx, LockMode.Read);

        GammaTxn tx = stm.newDefaultTxn();
        try {
            ref.getLock().acquire(tx, LockMode.Exclusive);
            fail();
        } catch (ReadWriteConflict expected) {

        }

        assertIsAborted(tx);
        assertRefHasReadLock(ref, otherTx);
    }

    @Test
    public void whenExclusiveLockAlreadyAcquiredByOther_thenExclusiveLockNotPossible() {
        GammaTxnLong ref = new GammaTxnLong(stm);

        GammaTxn otherTx = stm.newDefaultTxn();
        ref.getLock().acquire(otherTx, LockMode.Exclusive);

        GammaTxn tx = stm.newDefaultTxn();
        try {
            ref.getLock().acquire(tx, LockMode.Exclusive);
            fail();
        } catch (ReadWriteConflict expected) {

        }

        assertIsAborted(tx);
        assertRefHasExclusiveLock(ref, otherTx);
    }

    @Test
    public void whenWriteLockAlreadyAcquiredByOther_thenExclusiveLockNotPossible() {
        GammaTxnLong ref = new GammaTxnLong(stm);

        GammaTxn otherTx = stm.newDefaultTxn();
        ref.getLock().acquire(otherTx, LockMode.Write);

        GammaTxn tx = stm.newDefaultTxn();
        try {
            ref.getLock().acquire(tx, LockMode.Exclusive);
            fail();
        } catch (ReadWriteConflict expected) {

        }

        assertIsAborted(tx);
        assertRefHasWriteLock(ref, otherTx);
    }

    @Test
    public void whenExclusiveLockAcquiredByOther_thenReadNotPossible() {
        GammaTxnLong ref = new GammaTxnLong(stm);

        GammaTxn tx = stm.newDefaultTxn();
        ref.getLock().acquire(tx, LockMode.Exclusive);

        GammaTxn otherTx = stm.newDefaultTxn();
        try {
            ref.get(otherTx);
            fail();
        } catch (ReadWriteConflict expected) {
        }
    }

    @Test
    public void whenPreviouslyReadByOtherThread_thenNoProblems() {
        GammaTxnLong ref = new GammaTxnLong(stm, 10);

        GammaTxn otherTx = stm.newDefaultTxn();
        ref.get(otherTx);

        GammaTxn tx = stm.newDefaultTxn();
        ref.getLock().acquire(tx, LockMode.Exclusive);

        long result = ref.get(otherTx);
        assertEquals(10, result);
    }

    @Test
    public void whenPreviouslyReadByOtherThread_thenWriteSuccessButExclusiveFails() {
        GammaTxnLong ref = new GammaTxnLong(stm, 10);

        GammaTxn tx = stm.newDefaultTxn();
        ref.get(tx);

        GammaTxn otherTx = stm.newDefaultTxn();
        ref.getLock().acquire(otherTx, LockMode.Exclusive);

        ref.set(tx, 100);

        try {
            tx.commit();
            fail();
        } catch (ReadWriteConflict expected) {
        }

        assertIsAborted(tx);
        assertRefHasExclusiveLock(ref, otherTx);
    }

    @Test
    public void whenExclusiveLockAcquiredByOther_thenWriteNotAllowed() {
        GammaTxnLong ref = new GammaTxnLong(stm, 5);

        GammaTxn otherTx = stm.newDefaultTxn();
        ref.getLock().acquire(otherTx, LockMode.Exclusive);

        GammaTxn tx = stm.newDefaultTxn();
        try {
            ref.set(tx, 100);
            fail();
        } catch (ReadWriteConflict expected) {
        }

        assertIsAborted(tx);
        assertRefHasExclusiveLock(ref, otherTx);
    }

    @Test
    public void writeLockIsUpgradableToExclusiveLock() {
        GammaTxnLong ref = new GammaTxnLong(stm, 5);

        GammaTxn tx = stm.newDefaultTxn();
        ref.getLock().acquire(tx, LockMode.Write);
        ref.getLock().acquire(tx, LockMode.Exclusive);

        assertIsActive(tx);
        assertRefHasExclusiveLock(ref, tx);
    }

    @Test
    public void whenReadLockAcquired_thenUpgradableToExclusiveLock() {
        GammaTxnLong ref = new GammaTxnLong(stm, 5);

        GammaTxn tx = stm.newDefaultTxn();
        ref.getLock().acquire(tx, LockMode.Read);
        ref.getLock().acquire(tx, LockMode.Exclusive);

        assertIsActive(tx);
        assertRefHasExclusiveLock(ref, tx);
    }

    @Test
    public void whenReadLockAlsoAcquiredByOther_thenNotUpgradableToExclusiveLock() {
        GammaTxnLong ref = new GammaTxnLong(stm, 5);

        GammaTxn otherTx = stm.newDefaultTxn();
        ref.getLock().acquire(otherTx, LockMode.Read);

        GammaTxn tx = stm.newDefaultTxn();
        ref.getLock().acquire(tx, LockMode.Read);

        try {
            ref.getLock().acquire(tx, LockMode.Exclusive);
            fail();
        } catch (ReadWriteConflict expected) {

        }

        assertIsAborted(tx);
        assertRefHasReadLock(ref, otherTx);
        assertReadLockCount(ref, 1);
    }


    @Test
    public void whenTransactionCommits_thenExclusiveLockReleased() {
        GammaTxnLong ref = new GammaTxnLong(stm, 5);

        GammaTxn tx = stm.newDefaultTxn();
        ref.getLock().acquire(tx, LockMode.Exclusive);
        tx.commit();

        assertIsCommitted(tx);
        assertRefHasNoLocks(ref);
    }

    @Test
    public void whenTransactionIsPrepared_thenExclusiveLockRemains() {
        GammaTxnLong ref = new GammaTxnLong(stm, 5);

        GammaTxn tx = stm.newDefaultTxn();
        ref.getLock().acquire(tx, LockMode.Exclusive);
        tx.prepare();

        assertIsPrepared(tx);
        assertRefHasExclusiveLock(ref, tx);
    }

    @Test
    public void whenTransactionAborts_thenExclusiveLockIsReleased() {
        GammaTxnLong ref = new GammaTxnLong(stm, 5);

        GammaTxn tx = stm.newDefaultTxn();
        ref.getLock().acquire(tx, LockMode.Exclusive);
        tx.abort();

        assertIsAborted(tx);
        assertRefHasNoLocks(ref);
    }

    @Test
    public void whenExclusiveLockAlreadyIsAcquired_thenReentrantExclusiveLockIsSuccess() {
        GammaTxnLong ref = new GammaTxnLong(stm, 5);

        GammaTxn tx = stm.newDefaultTxn();
        ref.getLock().acquire(tx, LockMode.Exclusive);
        ref.getLock().acquire(tx, LockMode.Exclusive);

        assertIsActive(tx);
        assertRefHasExclusiveLock(ref, tx);
    }

    @Test
    public void whenReadConflict_thenExclusiveLockFails() {
        GammaTxnLong ref = new GammaTxnLong(stm, 5);

        GammaTxn tx = stm.newDefaultTxn();
        ref.get(tx);

        ref.atomicIncrementAndGet(1);

        try {
            ref.getLock().acquire(tx, LockMode.Exclusive);
            fail();
        } catch (ReadWriteConflict expected) {
        }

        assertRefHasNoLocks(ref);
        assertIsAborted(tx);
    }

}
