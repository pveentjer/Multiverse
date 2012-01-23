package org.multiverse.stms.gamma.transactionalobjects.basegammaref;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.multiverse.api.LockMode;
import org.multiverse.stms.gamma.GammaConstants;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactionalobjects.GammaLongRef;
import org.multiverse.stms.gamma.transactionalobjects.GammaRefTranlocal;
import org.multiverse.stms.gamma.transactions.GammaTransaction;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.multiverse.TestUtils.assertOrecValue;
import static org.multiverse.api.ThreadLocalTransaction.clearThreadLocalTransaction;
import static org.multiverse.stms.gamma.GammaTestUtils.*;

public class BaseGammaRef_hasReadConflictTest implements GammaConstants {
    private GammaStm stm;

    @Before
    public void setUp() {
        stm = new GammaStm();
        clearThreadLocalTransaction();
    }

    @Test
    public void whenReadAndNoConflict() {
        GammaLongRef ref = new GammaLongRef(stm);

        GammaTransaction tx = stm.newDefaultTransaction();
        GammaRefTranlocal read = ref.openForRead(tx, LOCKMODE_NONE);


        boolean hasReadConflict = ref.hasReadConflict(read);

        assertFalse(hasReadConflict);
        assertSurplus(ref, 0);
        assertRefHasNoLocks(ref);
    }

    @Test
    public void whenWriteAndNoConflict() {
        GammaLongRef ref = new GammaLongRef(stm);

        GammaTransaction tx = stm.newDefaultTransaction();
        GammaRefTranlocal write = ref.openForWrite(tx, LOCKMODE_NONE);

        boolean hasReadConflict = ref.hasReadConflict(write);

        assertFalse(hasReadConflict);
        assertSurplus(ref, 0);
        assertRefHasNoLocks(ref);
    }

    @Test
    public void whenPrivatizedBySelf_thenNoConflict() {
        GammaLongRef ref = new GammaLongRef(stm);

        GammaTransaction tx = stm.newDefaultTransaction();
        GammaRefTranlocal read = ref.openForRead(tx, LOCKMODE_EXCLUSIVE);

        boolean hasConflict = ref.hasReadConflict(read);

        assertFalse(hasConflict);
        assertSurplus(ref, 1);
        assertRefHasExclusiveLock(ref, tx);
    }

    @Test
    public void whenEnsuredBySelf_thenNoConflict() {
        GammaLongRef ref = new GammaLongRef(stm);

        GammaTransaction tx = stm.newDefaultTransaction();
        GammaRefTranlocal read = ref.openForRead(tx, LOCKMODE_WRITE);

        boolean hasConflict = ref.hasReadConflict(read);

        assertFalse(hasConflict);
        assertSurplus(ref, 1);
        assertRefHasWriteLock(ref, tx);
    }

    @Test
    public void whenUpdatedByOther_thenConflict() {
        GammaLongRef ref = new GammaLongRef(stm);

        GammaTransaction tx = stm.newDefaultTransaction();
        GammaRefTranlocal read = ref.openForRead(tx, LOCKMODE_NONE);

        //conflicting update
        ref.atomicIncrementAndGet(1);

        boolean hasConflict = ref.hasReadConflict(read);
        assertTrue(hasConflict);
        assertSurplus(ref, 0);
        assertRefHasNoLocks(ref);
    }

    @Test
    public void whenFresh() {
        GammaTransaction tx = stm.newDefaultTransaction();
        GammaLongRef ref = new GammaLongRef(tx);
        GammaRefTranlocal tranlocal = tx.locate(ref);

        long orecValue = ref.orec;
        boolean conflict = ref.hasReadConflict(tranlocal);

        assertFalse(conflict);
        assertOrecValue(ref, orecValue);
    }

    @Test
    public void whenValueChangedByOtherAndLockedForCommitByOther() {
        GammaLongRef ref = new GammaLongRef(stm);

        GammaTransaction tx = stm.newDefaultTransaction();
        GammaRefTranlocal read = ref.openForRead(tx, LOCKMODE_NONE);

        //do the conflicting update
        ref.atomicIncrementAndGet(1);

        //privatize it
        GammaTransaction otherTx = stm.newDefaultTransaction();
        ref.getLock().acquire(otherTx, LockMode.Exclusive);

        boolean hasConflict = ref.hasReadConflict(read);

        assertTrue(hasConflict);
        assertSurplus(ref, 1);
        assertRefHasExclusiveLock(ref, otherTx);
    }

    @Test
    public void whenValueChangedByOtherAndEnsuredAgain() {
        GammaLongRef ref = new GammaLongRef(stm);

        GammaTransaction tx = stm.newDefaultTransaction();
        GammaRefTranlocal read = ref.openForRead(tx, LOCKMODE_NONE);

        //do the conflicting update
        ref.atomicIncrementAndGet(1);

        //ensure it.
        GammaTransaction otherTx = stm.newDefaultTransaction();
        ref.getLock().acquire(otherTx, LockMode.Write);

        boolean hasConflict = ref.hasReadConflict(read);

        assertTrue(hasConflict);
        assertSurplus(ref, 1);
        assertRefHasWriteLock(ref, otherTx);
    }

    @Test
    public void whenUpdateInProgressBecauseLockedByOther() {
        GammaLongRef ref = new GammaLongRef(stm);

        GammaTransaction tx = stm.newDefaultTransaction();
        GammaRefTranlocal tranlocal = ref.openForRead(tx, LOCKMODE_NONE);

        GammaTransaction otherTx = stm.newDefaultTransaction();
        ref.openForRead(otherTx, LOCKMODE_EXCLUSIVE);

        boolean hasReadConflict = ref.hasReadConflict(tranlocal);

        assertTrue(hasReadConflict);
    }

    @Test
    public void whenAlsoReadByOther_thenNoConflict() {
        GammaLongRef ref = new GammaLongRef(stm);

        GammaTransaction tx = stm.newDefaultTransaction();
        ref.get(tx);

        GammaTransaction otherTx = stm.newDefaultTransaction();
        GammaRefTranlocal read = ref.openForRead(otherTx, LOCKMODE_NONE);

        boolean hasConflict = ref.hasReadConflict(read);

        assertFalse(hasConflict);
        assertSurplus(ref, 0);
        assertRefHasNoLocks(ref);
    }

    @Test
    public void whenPendingUpdateByOther_thenNoConflict() {
        GammaLongRef ref = new GammaLongRef(stm);

        GammaTransaction tx = stm.newDefaultTransaction();
        ref.set(tx, 200);

        GammaTransaction otherTx = stm.newDefaultTransaction();
        GammaRefTranlocal read = ref.openForRead(otherTx, LOCKMODE_NONE);

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
