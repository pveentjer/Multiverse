package org.multiverse.stms.gamma.transactionalobjects.orec;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.exceptions.PanicError;
import org.multiverse.stms.gamma.GammaConstants;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactionalobjects.GammaLongRef;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.multiverse.TestUtils.*;
import static org.multiverse.stms.gamma.GammaTestUtils.*;

public class Orec_upgradeReadLockTest implements GammaConstants {

    private GammaStm stm;

    @Before
    public void setUp() {
        stm = new GammaStm();
    }

    // ================================ write biased ===================================

    @Test
    public void writeBiased_whenNoLockAcquiredAndUpgradeToWriteLock_thenPanicError() {
        GammaLongRef orec = new GammaLongRef(stm, 0);
        long orecValue = orec.orec;

        try {
            orec.upgradeReadLock(1, false);
            fail();
        } catch (PanicError expected) {
        }

        assertOrecValue(orec, orecValue);
    }

    @Test
    public void writeBiased_whenNoLockAcquiredAndUpgradeToExclusiveLock_thenPanicError() {
        GammaLongRef orec = new GammaLongRef(stm, 0);
        long orecValue = orec.orec;

        try {
            orec.upgradeReadLock(1, true);
            fail();
        } catch (PanicError expected) {
        }

        assertOrecValue(orec, orecValue);
    }

    @Test
    public void writeBiased_whenWriteLockAcquired_thenPanicError() {
        GammaLongRef orec = new GammaLongRef(stm, 0);

        orec.arriveAndLock(1, LOCKMODE_WRITE);
        long orecValue = orec.orec;

        try {
            orec.upgradeReadLock(1, true);
            fail();
        } catch (PanicError expected) {
        }

        assertOrecValue(orec, orecValue);
    }

    @Test
    public void writeBiased_whenExclusiveLockAcquired_thenPanicError() {
        GammaLongRef orec = new GammaLongRef(stm, 0);

        orec.arriveAndLock(1, LOCKMODE_EXCLUSIVE);

        long orecValue = orec.orec;
        try {
            orec.upgradeReadLock(1, true);
            fail();
        } catch (PanicError expected) {
        }

        assertOrecValue(orec, orecValue);
    }

    @Test
    public void writeBiased_whenSingleReadLockAcquiredAndUpgradeToWriteLockAndNoSurplus_thenSuccess() {
        GammaLongRef orec = new GammaLongRef(stm, 0);

        orec.arriveAndLock(1, LOCKMODE_READ);

        int result = orec.upgradeReadLock(1, false);

        assertHasMasks(result, MASK_SUCCESS);
        assertNotHasMasks(result, MASK_CONFLICT);
        assertTrue(orec.hasWriteLock());
        assertReadLockCount(orec, 0);
        assertSurplus(orec, 1);
        assertWriteBiased(orec);
    }

    @Test
    public void writeBiased_whenSingleReadLockAcquiredAndUpgradeToWriteLockAndSurplus_thenSuccess() {
        GammaLongRef orec = new GammaLongRef(stm, 0);

        orec.arrive(1);
        orec.arriveAndLock(1, LOCKMODE_READ);

        int result = orec.upgradeReadLock(1, false);

        assertHasMasks(result, MASK_SUCCESS);
        assertNotHasMasks(result, MASK_CONFLICT);
        assertTrue(orec.hasWriteLock());
        assertReadLockCount(orec, 0);
        assertSurplus(orec, 2);
        assertWriteBiased(orec);
    }

    @Test
    public void writeBiased_whenSingleReadLockAcquiredAndUpgradeToExclusiveLockAndNoSurplus_thenSuccess() {
        GammaLongRef orec = new GammaLongRef(stm, 0);
        orec.arriveAndLock(1, LOCKMODE_READ);

        int result = orec.upgradeReadLock(1, true);

        assertHasMasks(result, MASK_SUCCESS);
        assertNotHasMasks(result, MASK_CONFLICT);
        assertTrue(orec.hasExclusiveLock());
        assertReadLockCount(orec, 0);
        assertSurplus(orec, 1);
        assertReadonlyCount(orec, 0);
        assertWriteBiased(orec);
    }

    @Test
    public void writeBiased_whenSingleReadLockAcquiredAndUpgradeToExclusiveAndSurplus_thenConflict() {
        GammaLongRef orec = new GammaLongRef(stm, 0);
        orec.arrive(1);
        orec.arriveAndLock(1, LOCKMODE_READ);

        int result = orec.upgradeReadLock(1, true);

        assertHasMasks(result, MASK_SUCCESS, MASK_CONFLICT);
        assertTrue(orec.hasExclusiveLock());
        assertReadLockCount(orec, 0);
        assertSurplus(orec, 2);
        assertReadonlyCount(orec, 0);
        assertWriteBiased(orec);
    }

    @Test
    public void writeeBiased_whenMultipleReadLocksAcquired_thenUpgradeToWriteLockFailure() {
        GammaLongRef orec = new GammaLongRef(stm, 0);
        orec.arriveAndLock(1, LOCKMODE_READ);
        orec.arriveAndLock(1, LOCKMODE_READ);
        long orecValue = orec.orec;

        int result = orec.upgradeReadLock(1, false);

        assertFailure(result);
        assertOrecValue(orec, orecValue);
    }

    @Test
    public void writeBiased_whenMultipleReadLocksAcquired_thenUpgradeToExclusiveLockFailure() {
        GammaLongRef orec = new GammaLongRef(stm, 0);
        orec.arriveAndLock(1, LOCKMODE_READ);
        orec.arriveAndLock(1, LOCKMODE_READ);
        long orecValue = orec.orec;

        int result = orec.upgradeReadLock(1, true);

        assertFailure(result);
        assertOrecValue(orec, orecValue);
    }

    // ================================ read biased ===================================

    @Test
    public void readBiased_whenNoLockAcquiredAndUpgradeToWriteLock_thenPanicError() {
        GammaLongRef orec = makeReadBiased(new GammaLongRef(stm, 0));
        long orecValue = orec.orec;

        try {
            orec.upgradeReadLock(1, false);
            fail();
        } catch (PanicError expected) {
        }

        assertOrecValue(orec, orecValue);
    }

    @Test
    public void readBiased_whenNoLockAcquiredAndUpgradeToExclusiveLock_thenPanicError() {
        GammaLongRef orec = makeReadBiased(new GammaLongRef(stm, 0));
        long orecValue = orec.orec;

        try {
            orec.upgradeReadLock(1, true);
            fail();
        } catch (PanicError expected) {
        }

        assertOrecValue(orec, orecValue);
    }

    @Test
    public void readBiased_whenWriteLockAcquired_thenPanicError() {
        GammaLongRef orec = makeReadBiased(new GammaLongRef(stm, 0));
        orec.arriveAndLock(1, LOCKMODE_WRITE);
        long orecValue = orec.orec;

        try {
            orec.upgradeReadLock(1, true);
            fail();
        } catch (PanicError expected) {
        }

        assertOrecValue(orec, orecValue);
    }

    @Test
    public void readBiased_whenExclusiveLockAcquired_thenPanicError() {
        GammaLongRef orec = makeReadBiased(new GammaLongRef(stm, 0));
        orec.arriveAndLock(1, LOCKMODE_EXCLUSIVE);
        long orecValue = orec.orec;

        try {
            orec.upgradeReadLock(1, true);
            fail();
        } catch (PanicError expected) {
        }

        assertOrecValue(orec, orecValue);
    }

    @Test
    public void readBiased_whenSingleReadLockAcquiredAndUpgradeToWriteLockAndNoSurplus_thenSuccess() {
        GammaLongRef orec = makeReadBiased(new GammaLongRef(stm, 0));
        orec.arriveAndLock(1, LOCKMODE_READ);

        int result = orec.upgradeReadLock(1, false);

        assertHasMasks(result, MASK_SUCCESS);
        assertNotHasMasks(result, MASK_CONFLICT);
        assertTrue(orec.hasWriteLock());
        assertReadLockCount(orec, 0);
        assertSurplus(orec, 1);
        assertReadBiased(orec);
    }

    @Test
    public void readBiased_whenSingleReadLockAcquiredAndUpgradeToWriteLockAndSurplus_thenSuccess() {
        GammaLongRef orec = makeReadBiased(new GammaLongRef(stm, 0));
        orec.arrive(1);
        orec.arriveAndLock(1, LOCKMODE_READ);

        int result = orec.upgradeReadLock(1, false);

        assertHasMasks(result, MASK_SUCCESS);
        assertNotHasMasks(result, MASK_CONFLICT);
        assertTrue(orec.hasWriteLock());
        assertReadLockCount(orec, 0);
        assertSurplus(orec, 1);
        assertReadBiased(orec);
    }

    @Test
    public void readBiased_whenSingleReadLockAcquiredAndUpgradeToExclusiveLockAndNoSurplus_thenSuccess() {
        GammaLongRef orec = makeReadBiased(new GammaLongRef(stm, 0));
        orec.arriveAndLock(1, LOCKMODE_READ);

        int result = orec.upgradeReadLock(1, true);

        assertHasMasks(result, MASK_SUCCESS, MASK_CONFLICT);
        assertTrue(orec.hasExclusiveLock());
        assertReadLockCount(orec, 0);
        assertSurplus(orec, 1);
        assertReadonlyCount(orec, 0);
        assertReadBiased(orec);
    }

    @Test
    public void readBiased_whenMultipleReadLocksAcquired_thenUpgradeToWriteLockFailure() {
        GammaLongRef orec = makeReadBiased(new GammaLongRef(stm, 0));
        orec.arriveAndLock(1, LOCKMODE_READ);
        orec.arriveAndLock(1, LOCKMODE_READ);
        long orecValue = orec.orec;

        int result = orec.upgradeReadLock(1, false);

        assertFailure(result);
        assertOrecValue(orec, orecValue);
    }

    @Test
    public void readBiased_whenMultipleReadLocksAcquired_thenUpgradeToExclusiveLockFailure() {
        GammaLongRef orec = makeReadBiased(new GammaLongRef(stm, 0));
        orec.arriveAndLock(1, LOCKMODE_READ);
        orec.arriveAndLock(1, LOCKMODE_READ);
        long orecValue = orec.orec;

        int result = orec.upgradeReadLock(1, true);

        assertFailure(result);
        assertOrecValue(orec, orecValue);
    }
}
