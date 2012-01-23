package org.multiverse.stms.gamma.integration.locks;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.LockMode;
import org.multiverse.api.exceptions.ReadWriteConflict;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactionalobjects.GammaLongRef;
import org.multiverse.stms.gamma.transactions.GammaTransaction;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.multiverse.TestUtils.*;
import static org.multiverse.api.GlobalStmInstance.getGlobalStmInstance;
import static org.multiverse.api.ThreadLocalTransaction.clearThreadLocalTransaction;
import static org.multiverse.stms.gamma.GammaTestUtils.*;

public class WriteLockTest {

    private GammaStm stm;

    @Before
    public void setUp() {
        stm = (GammaStm) getGlobalStmInstance();
        clearThreadLocalTransaction();
    }

    @Test
    public void whenUnlocked() {
        GammaLongRef ref = new GammaLongRef(stm, 10);

        GammaTransaction tx = stm.newDefaultTransaction();
        ref.getLock().acquire(tx, LockMode.Write);

        assertIsActive(tx);
        assertRefHasWriteLock(ref, tx);
    }

    @Test
    public void whenReadLockAlreadyAcquiredByOther_thenWriteLockFails() {
        GammaLongRef ref = new GammaLongRef(stm);

        GammaTransaction otherTx = stm.newDefaultTransaction();
        ref.getLock().acquire(otherTx, LockMode.Read);

        GammaTransaction tx = stm.newDefaultTransaction();
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
    public void whenWriteLockAlreadyAcquiredOther_thenWriteLockFails() {
        GammaLongRef ref = new GammaLongRef(stm);

        GammaTransaction otherTx = stm.newDefaultTransaction();
        ref.getLock().acquire(otherTx, LockMode.Write);

        GammaTransaction tx = stm.newDefaultTransaction();
        try {
            ref.getLock().acquire(tx, LockMode.Write);
            fail();
        } catch (ReadWriteConflict expected) {

        }

        assertIsAborted(tx);
        assertRefHasWriteLock(ref, otherTx);
    }

    @Test
    public void whenExclusiveLockAlreadyAcquiredByOther_thenWriteLockFails() {
        GammaLongRef ref = new GammaLongRef(stm);

        GammaTransaction otherTx = stm.newDefaultTransaction();
        ref.getLock().acquire(otherTx, LockMode.Exclusive);

        GammaTransaction tx = stm.newDefaultTransaction();
        try {
            ref.getLock().acquire(tx, LockMode.Write);
            fail();
        } catch (ReadWriteConflict expected) {

        }

        assertIsAborted(tx);
        assertRefHasExclusiveLock(ref, otherTx);
    }


    @Test
    public void whenWriteLockAcquiredByOther_thenReadStillAllowed() {
        GammaLongRef ref = new GammaLongRef(stm, 5);

        GammaTransaction otherTx = stm.newDefaultTransaction();
        ref.getLock().acquire(otherTx, LockMode.Write);

        GammaTransaction tx = stm.newDefaultTransaction();

        long result = ref.get(tx);

        assertEquals(5, result);
        assertIsActive(tx);
        assertRefHasWriteLock(ref, otherTx);
    }

    @Test
    public void whenPreviouslyReadByOtherThread_thenNoProblems() {
        GammaLongRef ref = new GammaLongRef(stm, 10);

        GammaTransaction tx = stm.newDefaultTransaction();
        ref.get(tx);

        GammaTransaction otherTx = stm.newDefaultTransaction();
        ref.getLock().acquire(otherTx, LockMode.Write);

        long result = ref.get(tx);

        assertEquals(10, result);
        assertIsActive(tx);
        assertRefHasWriteLock(ref, otherTx);
    }

    @Test
    public void whenPreviouslyReadByOtherTransaction_thenWriteSuccessButCommitFails() {
        GammaLongRef ref = new GammaLongRef(stm, 10);

        GammaTransaction tx = stm.newDefaultTransaction();
        ref.get(tx);

        GammaTransaction otherTx = stm.newDefaultTransaction();
        ref.getLock().acquire(otherTx, LockMode.Write);

        ref.set(tx, 100);

        try {
            tx.commit();
            fail();
        } catch (ReadWriteConflict expected) {
        }

        assertIsAborted(tx);
        assertRefHasWriteLock(ref, otherTx);
    }

    @Test
    public void whenWriteLockAcquired_thenWriteAllowedButCommitFails() {
        GammaLongRef ref = new GammaLongRef(stm, 5);

        GammaTransaction otherTx = stm.newDefaultTransaction();
        ref.getLock().acquire(otherTx, LockMode.Write);

        GammaTransaction tx = stm.newDefaultTransaction();
        ref.set(tx, 100);

        try {
            tx.commit();
            fail();
        } catch (ReadWriteConflict expected) {
        }

        assertIsAborted(tx);
        assertRefHasWriteLock(ref, otherTx);
    }

    @Test
    public void whenReadLockAlreadyAcquiredBySelf_thenWriteLockAcquired() {
        GammaLongRef ref = new GammaLongRef(stm, 5);

        GammaTransaction tx = stm.newDefaultTransaction();
        ref.getLock().acquire(tx, LockMode.Read);
        ref.getLock().acquire(tx, LockMode.Write);

        assertIsActive(tx);
        assertRefHasWriteLock(ref, tx);
    }

    @Test
    public void whenReadLockAlsoAcquiredByOther_thenWriteLockFails() {
        GammaLongRef ref = new GammaLongRef(stm, 5);

        GammaTransaction otherTx = stm.newDefaultTransaction();
        ref.getLock().acquire(otherTx, LockMode.Read);

        GammaTransaction tx = stm.newDefaultTransaction();
        ref.getLock().acquire(tx, LockMode.Read);

        try {
            ref.getLock().acquire(tx, LockMode.Write);
            fail();
        } catch (ReadWriteConflict expected) {

        }

        assertIsAborted(tx);
        assertRefHasReadLock(ref, otherTx);
        assertReadLockCount(ref, 1);
    }

    @Test
    public void whenWriteLockAlreadyAcquiredBySelf_thenSuccess() {
        GammaLongRef ref = new GammaLongRef(stm, 5);

        GammaTransaction tx = stm.newDefaultTransaction();
        ref.getLock().acquire(tx, LockMode.Write);
        ref.getLock().acquire(tx, LockMode.Write);

        assertIsActive(tx);
        assertRefHasWriteLock(ref, tx);
    }

    @Test
    public void whenExclusiveLockAlreadyAcquiredBySelf_thenExclusiveLockRemains() {
        GammaLongRef ref = new GammaLongRef(stm, 5);

        GammaTransaction tx = stm.newDefaultTransaction();
        ref.getLock().acquire(tx, LockMode.Exclusive);
        ref.getLock().acquire(tx, LockMode.Write);

        assertIsActive(tx);
        assertRefHasExclusiveLock(ref, tx);
    }

    @Test
    public void whenTransactionCommits_thenWriteLockIsReleased() {
        GammaLongRef ref = new GammaLongRef(stm, 5);

        GammaTransaction tx = stm.newDefaultTransaction();
        ref.getLock().acquire(tx, LockMode.Write);
        tx.commit();

        assertIsCommitted(tx);
        assertRefHasNoLocks(ref);
    }

    @Test
    public void whenTransactionIsPrepared_thenWriteLockRemains() {
        GammaLongRef ref = new GammaLongRef(stm, 5);

        GammaTransaction tx = stm.newDefaultTransaction();
        ref.getLock().acquire(tx, LockMode.Write);
        tx.prepare();

        assertIsPrepared(tx);
        assertRefHasWriteLock(ref, tx);
    }

    @Test
    public void whenTransactionAborts_thenWriteLockReleased() {
        GammaLongRef ref = new GammaLongRef(stm, 5);

        GammaTransaction tx = stm.newDefaultTransaction();
        ref.getLock().acquire(tx, LockMode.Write);
        tx.abort();

        assertIsAborted(tx);
        assertRefHasNoLocks(ref);
    }

    @Test
    public void whenReadConflict_thenAcquireWriteLockFails() {
        GammaLongRef ref = new GammaLongRef(stm, 5);

        GammaTransaction tx = stm.newDefaultTransaction();
        ref.get(tx);

        ref.atomicIncrementAndGet(1);

        try {
            ref.getLock().acquire(tx, LockMode.Write);
            fail();
        } catch (ReadWriteConflict expected) {
        }

        assertRefHasNoLocks(ref);
        assertIsAborted(tx);
    }
}
