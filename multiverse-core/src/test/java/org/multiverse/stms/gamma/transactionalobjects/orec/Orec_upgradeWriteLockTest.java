package org.multiverse.stms.gamma.transactionalobjects.orec;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.exceptions.PanicError;
import org.multiverse.stms.gamma.GammaConstants;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactionalobjects.GammaLongRef;

import static org.junit.Assert.*;
import static org.multiverse.TestUtils.assertOrecValue;
import static org.multiverse.stms.gamma.GammaTestUtils.*;

public class Orec_upgradeWriteLockTest implements GammaConstants {

    private GammaStm stm;

    @Before
    public void setUp() {
        stm = new GammaStm();
    }

    // ==================== write biased ================================

    @Test
    public void writeBiased_whenNotLocked_thenPanicError() {
        GammaLongRef orec = new GammaLongRef(stm);
        long orecValue = orec.orec;

        try {
            orec.upgradeWriteLock();
            fail();
        } catch (PanicError expected) {
        }

        assertOrecValue(orec, orecValue);
    }

    @Test
    public void writeBiased_whenReadLocked_thenPanicError() {
        GammaLongRef orec = new GammaLongRef(stm);
        orec.arriveAndLock(1, LOCKMODE_READ);
        long orecValue = orec.orec;

        try {
            orec.upgradeWriteLock();
            fail();
        } catch (PanicError expected) {
        }

        assertOrecValue(orec, orecValue);
    }

    @Test
    public void writeBiased_whenWriteLockedAndNoSurplus() {
        GammaLongRef orec = new GammaLongRef(stm);
        orec.arriveAndLock(1, LOCKMODE_WRITE);

        boolean result = orec.upgradeWriteLock();

        assertFalse(result);
        assertSurplus(orec, 1);
        assertLockMode(orec, LOCKMODE_EXCLUSIVE);
        assertWriteBiased(orec);
        assertReadonlyCount(orec, 0);
    }

    @Test
    public void writeBiased_whenWriteLockedAndSurplusOfReaders() {
        GammaLongRef orec = new GammaLongRef(stm);
        orec.arrive(1);
        orec.arriveAndLock(1, LOCKMODE_WRITE);

        boolean result = orec.upgradeWriteLock();

        assertTrue(result);
        assertSurplus(orec, 2);
        assertLockMode(orec, LOCKMODE_EXCLUSIVE);
        assertWriteBiased(orec);
        assertReadonlyCount(orec, 0);
    }

    @Test
    public void writeBiased_whenExclusiveLocked() {
        GammaLongRef orec = new GammaLongRef(stm);
        orec.arriveAndLock(1, LOCKMODE_EXCLUSIVE);

        boolean result = orec.upgradeWriteLock();

        assertFalse(result);
        assertSurplus(orec, 1);
        assertLockMode(orec, LOCKMODE_EXCLUSIVE);
        assertWriteBiased(orec);
        assertReadonlyCount(orec, 0);
    }

    // ==================== read biased ================================

    @Test
    public void readBiased_whenNotLocked_thenPanicError() {
        GammaLongRef orec = makeReadBiased(new GammaLongRef(stm));
        long orecValue = orec.orec;

        try {
            orec.upgradeWriteLock();
            fail();
        } catch (PanicError expected) {
        }

        assertOrecValue(orec, orecValue);
    }

    @Test
    public void readBiased_whenReadLocked_thenPanicError() {
        GammaLongRef orec = makeReadBiased(new GammaLongRef(stm));
        orec.arriveAndLock(1, LOCKMODE_READ);
        long orecValue = orec.orec;

        try {
            orec.upgradeWriteLock();
            fail();
        } catch (PanicError expected) {
        }

        assertOrecValue(orec, orecValue);
    }

    @Test
    public void readBiased_whenWriteLockedAndNoSurplus() {
        GammaLongRef orec = makeReadBiased(new GammaLongRef(stm));
        orec.arriveAndLock(1, LOCKMODE_WRITE);

        boolean result = orec.upgradeWriteLock();

        assertTrue(result);
        assertSurplus(orec, 1);
        assertLockMode(orec, LOCKMODE_EXCLUSIVE);
        assertReadBiased(orec);
        assertReadonlyCount(orec, 0);
    }

    @Test
    public void readBiased_whenWriteLockedAndSurplusOfReaders() {
        GammaLongRef orec = makeReadBiased(new GammaLongRef(stm));
        orec.arrive(1);
        orec.arriveAndLock(1, LOCKMODE_WRITE);

        boolean result = orec.upgradeWriteLock();

        assertTrue(result);
        assertSurplus(orec, 1);
        assertLockMode(orec, LOCKMODE_EXCLUSIVE);
        assertReadBiased(orec);
        assertReadonlyCount(orec, 0);
    }

    @Test
    public void readBiased_whenExclusiveLocked() {
        GammaLongRef orec = makeReadBiased(new GammaLongRef(stm));
        orec.arriveAndLock(1, LOCKMODE_EXCLUSIVE);

        boolean result = orec.upgradeWriteLock();

        assertFalse(result);
        assertSurplus(orec, 1);
        assertLockMode(orec, LOCKMODE_EXCLUSIVE);
        assertReadBiased(orec);
        assertReadonlyCount(orec, 0);
    }
}
