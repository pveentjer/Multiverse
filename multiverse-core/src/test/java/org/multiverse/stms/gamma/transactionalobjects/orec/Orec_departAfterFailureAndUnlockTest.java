package org.multiverse.stms.gamma.transactionalobjects.orec;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.exceptions.PanicError;
import org.multiverse.stms.gamma.GammaConstants;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactionalobjects.AbstractGammaObject;
import org.multiverse.stms.gamma.transactionalobjects.GammaTxnLong;

import static org.junit.Assert.fail;
import static org.multiverse.TestUtils.assertOrecValue;
import static org.multiverse.stms.gamma.GammaTestUtils.*;

public class Orec_departAfterFailureAndUnlockTest implements GammaConstants {

    private GammaStm stm;

    @Before
    public void setUp() {
        stm = new GammaStm();
    }

    @Test
    public void whenUpdateBiasedNotLocked_thenPanicError() {
        AbstractGammaObject orec = new GammaTxnLong(stm);
        long orecValue = orec.orec;
        try {
            orec.departAfterFailureAndUnlock();
            fail();
        } catch (PanicError expected) {
        }

        assertOrecValue(orec, orecValue);
    }

    @Test
    public void whenReadBiasedAndNotLocked_thenPanicError() {
        AbstractGammaObject orec = makeReadBiased(new GammaTxnLong(stm));
        long orecValue = orec.orec;

        try {
            orec.departAfterFailureAndUnlock();
            fail();
        } catch (PanicError expected) {
        }

        assertOrecValue(orec, orecValue);
    }

    @Test
    public void whenUpdateBiasedAndHasMultipleReadLocks() {
        AbstractGammaObject orec = new GammaTxnLong(stm);
        orec.arriveAndLock(1, LOCKMODE_READ);
        orec.arriveAndLock(1, LOCKMODE_READ);

        orec.departAfterFailureAndUnlock();

        assertLockMode(orec, LOCKMODE_READ);
        assertReadLockCount(orec, 1);
        assertSurplus(orec, 1);
        assertReadonlyCount(orec, 0);
        assertWriteBiased(orec);
    }

    @Test
    public void whenUpdateBiasedAndHasSingleReadLock() {
        AbstractGammaObject orec = new GammaTxnLong(stm);
        orec.arriveAndLock(1, LOCKMODE_READ);

        orec.departAfterFailureAndUnlock();

        assertLockMode(orec, LOCKMODE_NONE);
        assertSurplus(orec, 0);
        assertReadonlyCount(orec, 0);
        assertWriteBiased(orec);
        assertReadLockCount(orec, 0);
    }

    @Test
    public void whenUpdateBiasedAndHasWriteLocked() {
        AbstractGammaObject orec = new GammaTxnLong(stm);
        orec.arriveAndLock(1, LOCKMODE_WRITE);

        orec.departAfterFailureAndUnlock();
        assertLockMode(orec, LOCKMODE_NONE);
        assertSurplus(orec, 0);
        assertReadonlyCount(orec, 0);
        assertWriteBiased(orec);
        assertReadLockCount(orec, 0);
    }

    @Test
    public void whenUpdateBiasedAndHasWriteLockedAndSurplus() {
        AbstractGammaObject orec = new GammaTxnLong(stm);
        orec.arrive(1);
        orec.arrive(1);
        orec.arriveAndLock(1, LOCKMODE_WRITE);

        orec.departAfterFailureAndUnlock();
        assertLockMode(orec, LOCKMODE_NONE);
        assertSurplus(orec, 2);
        assertReadonlyCount(orec, 0);
        assertWriteBiased(orec);
        assertReadLockCount(orec, 0);
    }

    @Test
    public void whenUpdateBiasedAndHasExclusiveLocked() {
        AbstractGammaObject orec = new GammaTxnLong(stm);
        orec.arriveAndLock(1, LOCKMODE_EXCLUSIVE);

        orec.departAfterFailureAndUnlock();
        assertLockMode(orec, LOCKMODE_NONE);
        assertSurplus(orec, 0);
        assertReadonlyCount(orec, 0);
        assertWriteBiased(orec);
        assertReadLockCount(orec, 0);
    }

    @Test
    public void whenUpdateBiasedAndHasExclusiveLockedAndSurplus() {
        AbstractGammaObject orec = new GammaTxnLong(stm);
        orec.arrive(1);
        orec.arrive(2);
        orec.arriveAndLock(1, LOCKMODE_EXCLUSIVE);

        orec.departAfterFailureAndUnlock();
        assertLockMode(orec, LOCKMODE_NONE);
        assertSurplus(orec, 2);
        assertReadonlyCount(orec, 0);
        assertWriteBiased(orec);
        assertReadLockCount(orec, 0);
    }
}
