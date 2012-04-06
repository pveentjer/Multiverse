package org.multiverse.stms.gamma.integration.locks;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.LockMode;
import org.multiverse.api.exceptions.ReadWriteConflict;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactionalobjects.GammaLongRef;
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
        GammaLongRef ref = new GammaLongRef(stm, 10);

        GammaTxn tx = stm.newDefaultTransaction();
        ref.getLock().acquire(tx, LockMode.Exclusive);

        assertIsActive(tx);
        assertRefHasExclusiveLock(ref, tx);
    }

    @Test
    public void whenReadLockAlreadyAcquiredByOther_thenExclusiveLockNotPossible() {
        GammaLongRef ref = new GammaLongRef(stm);

        GammaTxn otherTx = stm.newDefaultTransaction();
        ref.getLock().acquire(otherTx, LockMode.Read);

        GammaTxn tx = stm.newDefaultTransaction();
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
        GammaLongRef ref = new GammaLongRef(stm);

        GammaTxn otherTx = stm.newDefaultTransaction();
        ref.getLock().acquire(otherTx, LockMode.Exclusive);

        GammaTxn tx = stm.newDefaultTransaction();
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
        GammaLongRef ref = new GammaLongRef(stm);

        GammaTxn otherTx = stm.newDefaultTransaction();
        ref.getLock().acquire(otherTx, LockMode.Write);

        GammaTxn tx = stm.newDefaultTransaction();
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
        GammaLongRef ref = new GammaLongRef(stm);

        GammaTxn tx = stm.newDefaultTransaction();
        ref.getLock().acquire(tx, LockMode.Exclusive);

        GammaTxn otherTx = stm.newDefaultTransaction();
        try {
            ref.get(otherTx);
            fail();
        } catch (ReadWriteConflict expected) {
        }
    }

    @Test
    public void whenPreviouslyReadByOtherThread_thenNoProblems() {
        GammaLongRef ref = new GammaLongRef(stm, 10);

        GammaTxn otherTx = stm.newDefaultTransaction();
        ref.get(otherTx);

        GammaTxn tx = stm.newDefaultTransaction();
        ref.getLock().acquire(tx, LockMode.Exclusive);

        long result = ref.get(otherTx);
        assertEquals(10, result);
    }

    @Test
    public void whenPreviouslyReadByOtherThread_thenWriteSuccessButExclusiveFails() {
        GammaLongRef ref = new GammaLongRef(stm, 10);

        GammaTxn tx = stm.newDefaultTransaction();
        ref.get(tx);

        GammaTxn otherTx = stm.newDefaultTransaction();
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
        GammaLongRef ref = new GammaLongRef(stm, 5);

        GammaTxn otherTx = stm.newDefaultTransaction();
        ref.getLock().acquire(otherTx, LockMode.Exclusive);

        GammaTxn tx = stm.newDefaultTransaction();
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
        GammaLongRef ref = new GammaLongRef(stm, 5);

        GammaTxn tx = stm.newDefaultTransaction();
        ref.getLock().acquire(tx, LockMode.Write);
        ref.getLock().acquire(tx, LockMode.Exclusive);

        assertIsActive(tx);
        assertRefHasExclusiveLock(ref, tx);
    }

    @Test
    public void whenReadLockAcquired_thenUpgradableToExclusiveLock() {
        GammaLongRef ref = new GammaLongRef(stm, 5);

        GammaTxn tx = stm.newDefaultTransaction();
        ref.getLock().acquire(tx, LockMode.Read);
        ref.getLock().acquire(tx, LockMode.Exclusive);

        assertIsActive(tx);
        assertRefHasExclusiveLock(ref, tx);
    }

    @Test
    public void whenReadLockAlsoAcquiredByOther_thenNotUpgradableToExclusiveLock() {
        GammaLongRef ref = new GammaLongRef(stm, 5);

        GammaTxn otherTx = stm.newDefaultTransaction();
        ref.getLock().acquire(otherTx, LockMode.Read);

        GammaTxn tx = stm.newDefaultTransaction();
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
        GammaLongRef ref = new GammaLongRef(stm, 5);

        GammaTxn tx = stm.newDefaultTransaction();
        ref.getLock().acquire(tx, LockMode.Exclusive);
        tx.commit();

        assertIsCommitted(tx);
        assertRefHasNoLocks(ref);
    }

    @Test
    public void whenTransactionIsPrepared_thenExclusiveLockRemains() {
        GammaLongRef ref = new GammaLongRef(stm, 5);

        GammaTxn tx = stm.newDefaultTransaction();
        ref.getLock().acquire(tx, LockMode.Exclusive);
        tx.prepare();

        assertIsPrepared(tx);
        assertRefHasExclusiveLock(ref, tx);
    }

    @Test
    public void whenTransactionAborts_thenExclusiveLockIsReleased() {
        GammaLongRef ref = new GammaLongRef(stm, 5);

        GammaTxn tx = stm.newDefaultTransaction();
        ref.getLock().acquire(tx, LockMode.Exclusive);
        tx.abort();

        assertIsAborted(tx);
        assertRefHasNoLocks(ref);
    }

    @Test
    public void whenExclusiveLockAlreadyIsAcquired_thenReentrantExclusiveLockIsSuccess() {
        GammaLongRef ref = new GammaLongRef(stm, 5);

        GammaTxn tx = stm.newDefaultTransaction();
        ref.getLock().acquire(tx, LockMode.Exclusive);
        ref.getLock().acquire(tx, LockMode.Exclusive);

        assertIsActive(tx);
        assertRefHasExclusiveLock(ref, tx);
    }

    @Test
    public void whenReadConflict_thenExclusiveLockFails() {
        GammaLongRef ref = new GammaLongRef(stm, 5);

        GammaTxn tx = stm.newDefaultTransaction();
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
