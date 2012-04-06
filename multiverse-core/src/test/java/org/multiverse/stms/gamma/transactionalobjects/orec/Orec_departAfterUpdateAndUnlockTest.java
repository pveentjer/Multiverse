package org.multiverse.stms.gamma.transactionalobjects.orec;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.exceptions.PanicError;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactionalobjects.AbstractGammaObject;
import org.multiverse.stms.gamma.transactionalobjects.GammaTxnLong;

import static org.junit.Assert.fail;
import static org.multiverse.TestUtils.assertOrecValue;
import static org.multiverse.stms.gamma.GammaTestUtils.*;

public class Orec_departAfterUpdateAndUnlockTest {

    private GammaStm stm;

    @Before
    public void setUp() {
        stm = new GammaStm();
    }

    // ================ write biased ===============================

    @Test
    public void writeBiased_whenNotLockedAndNoSurplus_thenPanicError() {
        AbstractGammaObject orec = new GammaTxnLong(stm);

        long orecValue = orec.orec;
        try {
            orec.departAfterUpdateAndUnlock();
            fail();
        } catch (PanicError expected) {
        }

        assertOrecValue(orec, orecValue);
    }

    @Test
    public void writeBiased_whenNotLockedAndSurplus_thenPanicError() {
        AbstractGammaObject orec = new GammaTxnLong(stm);
        orec.arrive(1);
        orec.arrive(1);

        long orecValue = orec.orec;
        try {
            orec.departAfterUpdateAndUnlock();
            fail();
        } catch (PanicError expected) {
        }

        assertOrecValue(orec, orecValue);
    }

    @Test
    public void writeBiased_whenLockedAndNoAdditionalSurplus() {
        AbstractGammaObject orec = new GammaTxnLong(stm);
        orec.arrive(1);
        orec.lockAfterArrive(1, LOCKMODE_EXCLUSIVE);

        orec.departAfterUpdateAndUnlock();

        assertLockMode(orec, LOCKMODE_NONE);
        assertSurplus(orec, 0);
        assertWriteBiased(orec);
        assertReadonlyCount(orec, 0);
    }

    @Test
    public void writeBiased_whenLockedAndAdditionalSurplus() {
        AbstractGammaObject orec = new GammaTxnLong(stm);
        orec.arrive(1);
        orec.arrive(1);
        orec.arrive(1);
        orec.lockAfterArrive(1, LOCKMODE_EXCLUSIVE);

        orec.departAfterUpdateAndUnlock();

        assertLockMode(orec, LOCKMODE_NONE);
        assertSurplus(orec, 2);
        assertWriteBiased(orec);
        assertReadonlyCount(orec, 0);
    }

    @Test
    public void writeBiased_whenWriteLock_thenPanicError() {
        AbstractGammaObject orec = new GammaTxnLong(stm);
        orec.arriveAndLock(1, LOCKMODE_WRITE);

        long orecValue = orec.orec;
        try {
            orec.departAfterUpdateAndUnlock();
            fail();
        } catch (PanicError expected) {
        }

        assertOrecValue(orec, orecValue);
    }

    @Test
    public void writeBiased_whenReadLockAcquired_thenPanicError() {
        AbstractGammaObject orec = new GammaTxnLong(stm);
        orec.arriveAndLock(1, LOCKMODE_READ);

        long orecValue = orec.orec;
        try {
            orec.departAfterUpdateAndUnlock();
            fail();
        } catch (PanicError expected) {
        }

        assertOrecValue(orec, orecValue);
    }

    // ================ read biased ===============================

    @Test
    public void readBiased_whenNotLockedAndNoSurplus_thenPanicError() {
        AbstractGammaObject orec = makeReadBiased(new GammaTxnLong(stm));

        long orecValue = orec.orec;
        try {
            orec.departAfterUpdateAndUnlock();
            fail();
        } catch (PanicError expected) {
        }

        assertOrecValue(orec, orecValue);
    }

    @Test
    public void readBiased_whenNotLockedAndSurplus_thenPanicError() {
        AbstractGammaObject orec = makeReadBiased(new GammaTxnLong(stm));
        orec.arrive(1);

        long orecValue = orec.orec;
        try {
            orec.departAfterUpdateAndUnlock();
            fail();
        } catch (PanicError expected) {
        }

        assertOrecValue(orec, orecValue);
    }

    @Test
    public void readBiased_whenLockedAndNoAdditionalSurplus() {
        AbstractGammaObject orec = makeReadBiased(new GammaTxnLong(stm));
        orec.arriveAndLock(1, LOCKMODE_EXCLUSIVE);

        orec.departAfterUpdateAndUnlock();

        assertLockMode(orec, LOCKMODE_NONE);
        assertSurplus(orec, 0);
        assertWriteBiased(orec);
        assertReadonlyCount(orec, 0);
    }

    @Test
    public void readBiased_whenLockedAndAdditionalSurplus() {
        AbstractGammaObject orec = makeReadBiased(new GammaTxnLong(stm));
        orec.arriveAndLock(1, LOCKMODE_EXCLUSIVE);

        orec.departAfterUpdateAndUnlock();

        assertLockMode(orec, LOCKMODE_NONE);
        assertSurplus(orec, 0);
        assertWriteBiased(orec);
        assertReadonlyCount(orec, 0);
    }

    @Test
    public void readBiased_whenWriteLock_thenPanicError() {
        AbstractGammaObject orec = makeReadBiased(new GammaTxnLong(stm));
        orec.arriveAndLock(1, LOCKMODE_WRITE);

        long orecValue = orec.orec;
        try {
            orec.departAfterUpdateAndUnlock();
            fail();
        } catch (PanicError expected) {
        }

        assertOrecValue(orec, orecValue);
    }

    @Test
    public void readBiased_whenReadLockAcquired_thenPanicError() {
        AbstractGammaObject orec = makeReadBiased(new GammaTxnLong(stm));
        orec.arriveAndLock(1, LOCKMODE_READ);

        long orecValue = orec.orec;
        try {
            orec.departAfterUpdateAndUnlock();
            fail();
        } catch (PanicError expected) {
        }

        assertOrecValue(orec, orecValue);
    }
}
