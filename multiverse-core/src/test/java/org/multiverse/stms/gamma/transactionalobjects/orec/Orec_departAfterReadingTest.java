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

public class Orec_departAfterReadingTest {

    private GammaStm stm;

    @Before
    public void setUp() {
        stm = new GammaStm();
    }

    @Test
    public void whenNoSurplus_thenPanicError() {
        AbstractGammaObject orec = new GammaTxnLong(stm);
        long orecValue = orec.orec;

        try {
            orec.departAfterReading();
            fail();
        } catch (PanicError expected) {
        }

        assertOrecValue(orec, orecValue);
    }

    @Test
    public void whenMuchSurplus() {
        AbstractGammaObject orec = new GammaTxnLong(stm);
        orec.arrive(1);
        orec.arrive(1);

        orec.departAfterReading();

        assertSurplus(orec, 1);
        assertReadonlyCount(orec, 1);
        assertWriteBiased(orec);
        assertLockMode(orec, LOCKMODE_NONE);
    }

    @Test
    public void whenReadLockAndSurplus() {
        AbstractGammaObject orec = new GammaTxnLong(stm);
        orec.arrive(1);
        orec.arrive(1);
        orec.lockAfterArrive(1, LOCKMODE_READ);

        orec.departAfterReading();

        assertSurplus(orec, 1);
        assertWriteBiased(orec);
        assertReadonlyCount(orec, 1);
        assertLockMode(orec, LOCKMODE_READ);
    }

    @Test
    public void whenReadLockAndNoAdditionalSurplus() {
        AbstractGammaObject orec = new GammaTxnLong(stm);
        orec.arriveAndLock(1, LOCKMODE_READ);

        long orecValue = orec.orec;
        try {
            orec.departAfterReading();
            fail();
        } catch (PanicError expected) {
        }

        assertOrecValue(orec, orecValue);
    }

    @Test
    public void whenWriteLockAndSurplus() {
        AbstractGammaObject orec = new GammaTxnLong(stm);
        orec.arrive(1);
        orec.arrive(1);
        orec.arriveAndLock(1, LOCKMODE_WRITE);

        orec.departAfterReading();

        assertSurplus(orec, 2);
        assertWriteBiased(orec);
        assertReadonlyCount(orec, 1);
        assertLockMode(orec, LOCKMODE_WRITE);
    }

    @Test
    public void whenWriteLockAndNoAdditionalSurplus() {
        AbstractGammaObject orec = new GammaTxnLong(stm);
        orec.arriveAndLock(1, LOCKMODE_WRITE);

        long orecValue = orec.orec;
        try {
            orec.departAfterReading();
            fail();
        } catch (PanicError expected) {
        }

        assertOrecValue(orec, orecValue);
    }

    @Test
    public void whenExclusiveLocked() {
        AbstractGammaObject orec = new GammaTxnLong(stm);
        orec.arrive(1);
        orec.arrive(1);
        orec.lockAfterArrive(1, LOCKMODE_EXCLUSIVE);

        orec.departAfterReading();

        assertSurplus(orec, 1);
        assertWriteBiased(orec);
        assertReadonlyCount(orec, 1);
        assertLockMode(orec, LOCKMODE_EXCLUSIVE);
    }

    @Test
    public void whenExclusiveLockAndNoAdditionalSurplus() {
        AbstractGammaObject orec = new GammaTxnLong(stm);
        orec.arriveAndLock(1, LOCKMODE_EXCLUSIVE);

        long orecValue = orec.orec;
        try {
            orec.departAfterReading();
            fail();
        } catch (PanicError expected) {
        }

        assertOrecValue(orec, orecValue);
    }


    @Test
    public void whenReadBiasedAndLockedForCommit_thenPanicError() {
        AbstractGammaObject orec = makeReadBiased(new GammaTxnLong(stm));
        orec.arriveAndLock(1, LOCKMODE_EXCLUSIVE);
        long orecValue = orec.orec;

        try {
            orec.departAfterReading();
            fail();
        } catch (PanicError expected) {
        }

        assertOrecValue(orec, orecValue);
    }

    @Test
    public void whenReadBiasedAndUnlocked_thenPanicError() {
        AbstractGammaObject orec = makeReadBiased(new GammaTxnLong(stm));
        long orecValue = orec.orec;

        try {
            orec.departAfterReading();
            fail();
        } catch (PanicError expected) {
        }

        assertOrecValue(orec, orecValue);
    }
}
