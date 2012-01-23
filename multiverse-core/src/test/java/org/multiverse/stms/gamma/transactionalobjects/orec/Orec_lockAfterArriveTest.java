package org.multiverse.stms.gamma.transactionalobjects.orec;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.exceptions.PanicError;
import org.multiverse.stms.gamma.GammaConstants;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactionalobjects.AbstractGammaObject;
import org.multiverse.stms.gamma.transactionalobjects.GammaLongRef;

import static org.junit.Assert.fail;
import static org.multiverse.TestUtils.*;
import static org.multiverse.stms.gamma.GammaTestUtils.*;

public class Orec_lockAfterArriveTest implements GammaConstants {

    private GammaStm stm;

    @Before
    public void setUp() {
        stm = new GammaStm();
    }

    // ===================== write biased and acquire readlock =========================

    @Test
    public void writeBiased_acquireReadLock_whenNoSurplus_thenPanicError() {
        AbstractGammaObject orec = new GammaLongRef(stm);
        long orecValue = orec.orec;

        try {
            orec.lockAfterArrive(1, LOCKMODE_READ);
            fail();
        } catch (PanicError expected) {
        }

        assertOrecValue(orec, orecValue);
    }

    @Test
    public void writeBiased_acquireReadLock_whenUnlocked() {
        AbstractGammaObject orec = new GammaLongRef(stm);
        orec.arrive(1);

        int result = orec.lockAfterArrive(1, LOCKMODE_READ);

        assertHasMasks(result, MASK_SUCCESS);
        assertNotHasMasks(result, MASK_CONFLICT, MASK_UNREGISTERED);
        assertSurplus(orec, 1);
        assertWriteBiased(orec);
        assertReadonlyCount(orec, 0);
        assertReadLockCount(orec, 1);
    }

    @Test
    public void writeBiased_acquireReadLock_whenUnlockedAndConflictingReaders() {
        AbstractGammaObject orec = new GammaLongRef(stm);
        orec.arrive(1);
        orec.arrive(1);

        int result = orec.lockAfterArrive(1, LOCKMODE_READ);

        assertHasMasks(result, MASK_SUCCESS);
        assertNotHasMasks(result, MASK_CONFLICT, MASK_UNREGISTERED);
        assertSurplus(orec, 2);
        assertWriteBiased(orec);
        assertReadonlyCount(orec, 0);
        assertReadLockCount(orec, 1);
    }

    @Test
    public void writeBiased_acquireReadLock_whenWriteLocked() {
        AbstractGammaObject orec = new GammaLongRef(stm);
        orec.arrive(1);
        orec.arriveAndLock(1, LOCKMODE_WRITE);
        long orecValue = orec.orec;

        int result = orec.lockAfterArrive(1, LOCKMODE_READ);

        assertFailure(result);
        assertOrecValue(orec, orecValue);
    }

    @Test
    public void writeBiased_acquireReadLock_whenExclusiveLocked() {
        AbstractGammaObject orec = new GammaLongRef(stm);
        orec.arrive(1);
        orec.arriveAndLock(1, LOCKMODE_EXCLUSIVE);
        long orecValue = orec.orec;

        int result = orec.lockAfterArrive(1, LOCKMODE_READ);

        assertFailure(result);
        assertOrecValue(orec, orecValue);
    }

    @Test
    public void writeBiased_acquireReadLock_whenReadLockAcquiredOnceByOther() {
        AbstractGammaObject orec = new GammaLongRef(stm);
        orec.arrive(1);
        orec.arriveAndLock(1, LOCKMODE_READ);

        int result = orec.lockAfterArrive(1, LOCKMODE_READ);

        assertHasMasks(result, MASK_SUCCESS);
        assertNotHasMasks(result, MASK_CONFLICT, MASK_UNREGISTERED);
        assertSurplus(orec, 2);
        assertWriteBiased(orec);
        assertReadonlyCount(orec, 0);
        assertReadLockCount(orec, 2);
    }

    @Test
    public void writeBiased_acquireReadLock_whenReadLockAcquiredMoreThanOnceByOthers() {
        AbstractGammaObject orec = new GammaLongRef(stm);
        orec.arrive(1);
        orec.arriveAndLock(1, LOCKMODE_READ);
        orec.arriveAndLock(1, LOCKMODE_READ);

        int result = orec.lockAfterArrive(1, LOCKMODE_READ);

        assertHasMasks(result, MASK_SUCCESS);
        assertNotHasMasks(result, MASK_CONFLICT, MASK_UNREGISTERED);
        assertSurplus(orec, 3);
        assertWriteBiased(orec);
        assertReadonlyCount(orec, 0);
        assertReadLockCount(orec, 3);
    }

    // ===================== write biased and acquire writelock =========================

    @Test
    public void writeBiased_acquireWriteLock_whenUnlocked() {
        AbstractGammaObject orec = new GammaLongRef(stm);
        orec.arrive(1);

        int result = orec.lockAfterArrive(1, LOCKMODE_WRITE);

        assertHasMasks(result, MASK_SUCCESS);
        assertNotHasMasks(result, MASK_CONFLICT, MASK_UNREGISTERED);
        assertSurplus(orec, 1);
        assertWriteBiased(orec);
        assertReadonlyCount(orec, 0);
        assertLockMode(orec, LOCKMODE_WRITE);
    }

    @Test
    public void writeBiased_acquireWriteLock_whenUnlockedButConflictingReaders() {
        AbstractGammaObject orec = new GammaLongRef(stm);
        orec.arrive(1);
        orec.arrive(1);

        int result = orec.lockAfterArrive(1, LOCKMODE_WRITE);

        assertHasMasks(result, MASK_SUCCESS);
        assertNotHasMasks(result, MASK_CONFLICT, MASK_UNREGISTERED);
        assertSurplus(orec, 2);
        assertWriteBiased(orec);
        assertReadonlyCount(orec, 0);
        assertLockMode(orec, LOCKMODE_WRITE);
    }

    @Test
    public void writeBiased_acquireWriteLock_whenNoSurplus_thenPanicError() {
        AbstractGammaObject orec = new GammaLongRef(stm);
        long orecValue = orec.orec;

        try {
            orec.lockAfterArrive(1, LOCKMODE_WRITE);
            fail();
        } catch (PanicError expected) {
        }

        assertOrecValue(orec, orecValue);
    }

    @Test
    public void writeBiased_acquireWriteLock_whenWriteLocked_thenFailure() {
        AbstractGammaObject orec = new GammaLongRef(stm);
        orec.arrive(1);
        orec.arriveAndLock(1, LOCKMODE_WRITE);
        long orecValue = orec.orec;

        int result = orec.lockAfterArrive(1, LOCKMODE_WRITE);

        assertFailure(result);
        assertOrecValue(orec, orecValue);
    }

    @Test
    public void writeBiased_acquireWriteLock_whenExclusiveLocked_thenFailure() {
        AbstractGammaObject orec = new GammaLongRef(stm);
        orec.arrive(1);
        orec.arriveAndLock(1, LOCKMODE_EXCLUSIVE);
        long orecValue = orec.orec;

        int result = orec.lockAfterArrive(1, LOCKMODE_WRITE);

        assertFailure(result);
        assertOrecValue(orec, orecValue);
    }

    @Test
    public void writeBiased_acquireWriteLock_whenReadLocken_thenFailure() {
        AbstractGammaObject orec = new GammaLongRef(stm);
        orec.arrive(1);
        orec.arriveAndLock(1, LOCKMODE_READ);
        long orecValue = orec.orec;

        int result = orec.lockAfterArrive(1, LOCKMODE_WRITE);

        assertFailure(result);
        assertOrecValue(orec, orecValue);
    }

    // ===================== write biased and acquire exclusivelock =========================

    @Test
    public void writeBiased_acquireExclusiveLock_whenUnlocked() {
        AbstractGammaObject orec = new GammaLongRef(stm);
        orec.arrive(1);

        int result = orec.lockAfterArrive(1, LOCKMODE_EXCLUSIVE);

        assertHasMasks(result, MASK_SUCCESS);
        assertNotHasMasks(result, MASK_CONFLICT, MASK_UNREGISTERED);
        assertSurplus(orec, 1);
        assertWriteBiased(orec);
        assertReadonlyCount(orec, 0);
        assertLockMode(orec, LOCKMODE_EXCLUSIVE);
    }

    @Test
    public void writeBiased_acquireExclusiveLock_whenUnlockedAndConflictingReaders() {
        AbstractGammaObject orec = new GammaLongRef(stm);
        orec.arrive(1);
        orec.arrive(1);

        int result = orec.lockAfterArrive(1, LOCKMODE_EXCLUSIVE);

        assertHasMasks(result, MASK_SUCCESS, MASK_CONFLICT);
        assertNotHasMasks(result, MASK_UNREGISTERED);
        assertSurplus(orec, 2);
        assertWriteBiased(orec);
        assertReadonlyCount(orec, 0);
        assertLockMode(orec, LOCKMODE_EXCLUSIVE);
    }

    @Test
    public void writeeBiased_acquireExclusiveLock_whenNoSurplus_thenPanicError() {
        AbstractGammaObject orec = new GammaLongRef(stm);
        long orecValue = orec.orec;

        try {
            orec.lockAfterArrive(1, LOCKMODE_EXCLUSIVE);
            fail();
        } catch (PanicError expected) {
        }

        assertOrecValue(orec, orecValue);
    }


    @Test
    public void writeBiased_acquireExclusiveLock_whenReadLocked() {
        AbstractGammaObject orec = new GammaLongRef(stm);
        orec.arrive(1);
        orec.arriveAndLock(1, LOCKMODE_READ);
        long orecValue = orec.orec;

        int result = orec.lockAfterArrive(1, LOCKMODE_EXCLUSIVE);

        assertFailure(result);
        assertOrecValue(orec, orecValue);
    }

    @Test
    public void writeBiased_acquireExclusiveLock_whenWriteLocked_thenFailure() {
        AbstractGammaObject orec = new GammaLongRef(stm);
        orec.arriveAndLock(1, LOCKMODE_WRITE);
        orec.arrive(1);
        long orecValue = orec.orec;

        int result = orec.lockAfterArrive(1, LOCKMODE_EXCLUSIVE);

        assertFailure(result);
        assertOrecValue(orec, orecValue);
    }

    @Test
    public void writeBiased_acquireExclusiveLock_whenExclusiveLocked() {
        AbstractGammaObject orec = new GammaLongRef(stm);
        orec.arrive(1);
        orec.arriveAndLock(1, LOCKMODE_EXCLUSIVE);
        long orecValue = orec.orec;

        int result = orec.lockAfterArrive(1, LOCKMODE_EXCLUSIVE);

        assertFailure(result);
        assertOrecValue(orec, orecValue);
    }

    // ====================================================================

    @Test
    public void readBiased_acquireExclusiveLock_thenPanicError() {
        AbstractGammaObject orec = makeReadBiased(new GammaLongRef(stm));
        long orecValue = orec.orec;

        try {
            orec.lockAfterArrive(1, LOCKMODE_EXCLUSIVE);
            fail();
        } catch (PanicError expected) {
        }

        assertOrecValue(orec, orecValue);
    }

    @Test
    public void readBiased_acquireWriteLock_thenPanicError() {
        AbstractGammaObject orec = makeReadBiased(new GammaLongRef(stm));
        long orecValue = orec.orec;

        try {
            orec.lockAfterArrive(1, LOCKMODE_WRITE);
            fail();
        } catch (PanicError expected) {
        }

        assertOrecValue(orec, orecValue);
    }

    @Test
    public void readBiased_acquireReadLock_thenPanicError() {
        AbstractGammaObject orec = makeReadBiased(new GammaLongRef(stm));
        long orecValue = orec.orec;

        try {
            orec.lockAfterArrive(1, LOCKMODE_READ);
            fail();
        } catch (PanicError expected) {
        }

        assertOrecValue(orec, orecValue);
    }
}
