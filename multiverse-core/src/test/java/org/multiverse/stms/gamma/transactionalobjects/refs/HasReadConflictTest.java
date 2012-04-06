package org.multiverse.stms.gamma.transactionalobjects.refs;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.multiverse.api.LockMode;
import org.multiverse.stms.gamma.GammaConstants;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactionalobjects.GammaTxnLong;
import org.multiverse.stms.gamma.transactionalobjects.Tranlocal;
import org.multiverse.stms.gamma.transactions.GammaTxn;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.multiverse.TestUtils.assertOrecValue;
import static org.multiverse.api.TxnThreadLocal.clearThreadLocalTxn;
import static org.multiverse.stms.gamma.GammaTestUtils.*;

public class HasReadConflictTest implements GammaConstants {
    private GammaStm stm;

    @Before
    public void setUp() {
        stm = new GammaStm();
        clearThreadLocalTxn();
    }

    @Test
    public void whenReadAndNoConflict() {
        GammaTxnLong ref = new GammaTxnLong(stm);

        GammaTxn tx = stm.newDefaultTxn();
        Tranlocal read = ref.openForRead(tx, LOCKMODE_NONE);


        boolean hasReadConflict = ref.hasReadConflict(read);

        assertFalse(hasReadConflict);
        assertSurplus(ref, 0);
        assertRefHasNoLocks(ref);
    }

    @Test
    public void whenWriteAndNoConflict() {
        GammaTxnLong ref = new GammaTxnLong(stm);

        GammaTxn tx = stm.newDefaultTxn();
        Tranlocal write = ref.openForWrite(tx, LOCKMODE_NONE);

        boolean hasReadConflict = ref.hasReadConflict(write);

        assertFalse(hasReadConflict);
        assertSurplus(ref, 0);
        assertRefHasNoLocks(ref);
    }

    @Test
    public void whenPrivatizedBySelf_thenNoConflict() {
        GammaTxnLong ref = new GammaTxnLong(stm);

        GammaTxn tx = stm.newDefaultTxn();
        Tranlocal read = ref.openForRead(tx, LOCKMODE_EXCLUSIVE);

        boolean hasConflict = ref.hasReadConflict(read);

        assertFalse(hasConflict);
        assertSurplus(ref, 1);
        assertRefHasExclusiveLock(ref, tx);
    }

    @Test
    public void whenEnsuredBySelf_thenNoConflict() {
        GammaTxnLong ref = new GammaTxnLong(stm);

        GammaTxn tx = stm.newDefaultTxn();
        Tranlocal read = ref.openForRead(tx, LOCKMODE_WRITE);

        boolean hasConflict = ref.hasReadConflict(read);

        assertFalse(hasConflict);
        assertSurplus(ref, 1);
        assertRefHasWriteLock(ref, tx);
    }

    @Test
    public void whenUpdatedByOther_thenConflict() {
        GammaTxnLong ref = new GammaTxnLong(stm);

        GammaTxn tx = stm.newDefaultTxn();
        Tranlocal read = ref.openForRead(tx, LOCKMODE_NONE);

        //conflicting update
        ref.atomicIncrementAndGet(1);

        boolean hasConflict = ref.hasReadConflict(read);
        assertTrue(hasConflict);
        assertSurplus(ref, 0);
        assertRefHasNoLocks(ref);
    }

    @Test
    public void whenFresh() {
        GammaTxn tx = stm.newDefaultTxn();
        GammaTxnLong ref = new GammaTxnLong(tx);
        Tranlocal tranlocal = tx.locate(ref);

        long orecValue = ref.orec;
        boolean conflict = ref.hasReadConflict(tranlocal);

        assertFalse(conflict);
        assertOrecValue(ref, orecValue);
    }

    @Test
    public void whenValueChangedByOtherAndLockedForCommitByOther() {
        GammaTxnLong ref = new GammaTxnLong(stm);

        GammaTxn tx = stm.newDefaultTxn();
        Tranlocal read = ref.openForRead(tx, LOCKMODE_NONE);

        //do the conflicting update
        ref.atomicIncrementAndGet(1);

        //privatize it
        GammaTxn otherTx = stm.newDefaultTxn();
        ref.getLock().acquire(otherTx, LockMode.Exclusive);

        boolean hasConflict = ref.hasReadConflict(read);

        assertTrue(hasConflict);
        assertSurplus(ref, 1);
        assertRefHasExclusiveLock(ref, otherTx);
    }

    @Test
    public void whenValueChangedByOtherAndEnsuredAgain() {
        GammaTxnLong ref = new GammaTxnLong(stm);

        GammaTxn tx = stm.newDefaultTxn();
        Tranlocal read = ref.openForRead(tx, LOCKMODE_NONE);

        //do the conflicting update
        ref.atomicIncrementAndGet(1);

        //ensure it.
        GammaTxn otherTx = stm.newDefaultTxn();
        ref.getLock().acquire(otherTx, LockMode.Write);

        boolean hasConflict = ref.hasReadConflict(read);

        assertTrue(hasConflict);
        assertSurplus(ref, 1);
        assertRefHasWriteLock(ref, otherTx);
    }

    @Test
    public void whenUpdateInProgressBecauseLockedByOther() {
        GammaTxnLong ref = new GammaTxnLong(stm);

        GammaTxn tx = stm.newDefaultTxn();
        Tranlocal tranlocal = ref.openForRead(tx, LOCKMODE_NONE);

        GammaTxn otherTx = stm.newDefaultTxn();
        ref.openForRead(otherTx, LOCKMODE_EXCLUSIVE);

        boolean hasReadConflict = ref.hasReadConflict(tranlocal);

        assertTrue(hasReadConflict);
    }

    @Test
    public void whenAlsoReadByOther_thenNoConflict() {
        GammaTxnLong ref = new GammaTxnLong(stm);

        GammaTxn tx = stm.newDefaultTxn();
        ref.get(tx);

        GammaTxn otherTx = stm.newDefaultTxn();
        Tranlocal read = ref.openForRead(otherTx, LOCKMODE_NONE);

        boolean hasConflict = ref.hasReadConflict(read);

        assertFalse(hasConflict);
        assertSurplus(ref, 0);
        assertRefHasNoLocks(ref);
    }

    @Test
    public void whenPendingUpdateByOther_thenNoConflict() {
        GammaTxnLong ref = new GammaTxnLong(stm);

        GammaTxn tx = stm.newDefaultTxn();
        ref.set(tx, 200);

        GammaTxn otherTx = stm.newDefaultTxn();
        Tranlocal read = ref.openForRead(otherTx, LOCKMODE_NONE);

        boolean hasConflict = ref.hasReadConflict(read);

        assertFalse(hasConflict);
        assertSurplus(ref, 0);
        assertRefHasNoLocks(ref);
    }

    @Test
    @Ignore
    public void whenReadBiased() {
    }
}
