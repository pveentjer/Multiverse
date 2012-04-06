package org.multiverse.stms.gamma.transactionalobjects.orec;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.exceptions.PanicError;
import org.multiverse.stms.gamma.GammaConstants;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactionalobjects.GammaTxnLong;

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
        GammaTxnLong orec = new GammaTxnLong(stm);
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
        GammaTxnLong orec = new GammaTxnLong(stm);
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
        GammaTxnLong orec = new GammaTxnLong(stm);
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
        GammaTxnLong orec = new GammaTxnLong(stm);
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
        GammaTxnLong orec = new GammaTxnLong(stm);
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
        GammaTxnLong orec = makeReadBiased(new GammaTxnLong(stm));
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
        GammaTxnLong orec = makeReadBiased(new GammaTxnLong(stm));
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
        GammaTxnLong orec = makeReadBiased(new GammaTxnLong(stm));
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
        GammaTxnLong orec = makeReadBiased(new GammaTxnLong(stm));
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
        GammaTxnLong orec = makeReadBiased(new GammaTxnLong(stm));
        orec.arriveAndLock(1, LOCKMODE_EXCLUSIVE);

        boolean result = orec.upgradeWriteLock();

        assertFalse(result);
        assertSurplus(orec, 1);
        assertLockMode(orec, LOCKMODE_EXCLUSIVE);
        assertReadBiased(orec);
        assertReadonlyCount(orec, 0);
    }
}
