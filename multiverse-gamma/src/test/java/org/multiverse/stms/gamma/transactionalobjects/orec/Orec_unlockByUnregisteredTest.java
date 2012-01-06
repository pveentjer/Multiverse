package org.multiverse.stms.gamma.transactionalobjects.orec;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.LockMode;
import org.multiverse.api.exceptions.PanicError;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactionalobjects.AbstractGammaObject;
import org.multiverse.stms.gamma.transactionalobjects.GammaLongRef;

import static org.junit.Assert.fail;
import static org.multiverse.TestUtils.assertOrecValue;
import static org.multiverse.stms.gamma.GammaTestUtils.*;

public class Orec_unlockByUnregisteredTest {

    private GammaStm stm;

    @Before
    public void setUp() {
        stm = new GammaStm();
    }

    @Test
    public void whenUpdateBiasedAndNoSurplus_thenPanicError() {
        AbstractGammaObject orec = new GammaLongRef(stm);
        long orecValue = orec.orec;

        try {
            orec.unlockByUnregistered();
            fail();
        } catch (PanicError expected) {
        }

        assertOrecValue(orec, orecValue);
    }

    @Test
    public void whenUpdateBiasedAndNotLocked_thenPanicError() {
        AbstractGammaObject orec = new GammaLongRef(stm);
        orec.arrive(1);
        long orecValue = orec.orec;

        try {
            orec.unlockByUnregistered();
            fail();
        } catch (PanicError expected) {
        }

        assertOrecValue(orec, orecValue);
    }

    @Test
    public void readBiased_whenReadLockedAcquiredOnce() {
        AbstractGammaObject orec = makeReadBiased(new GammaLongRef(stm));
        orec.arriveAndLock(1, LOCKMODE_READ);

        orec.unlockByUnregistered();
        assertLockMode(orec, LockMode.None);
        assertSurplus(orec, 1);
    }

    @Test
    public void readBiased_whenReadLockAcquiredMultipleTimes() {
        AbstractGammaObject orec = makeReadBiased(new GammaLongRef(stm));
        orec.arriveAndLock(1, LOCKMODE_READ);
        orec.arriveAndLock(1, LOCKMODE_READ);
        orec.arriveAndLock(1, LOCKMODE_READ);

        orec.unlockByUnregistered();
        assertLockMode(orec, LockMode.Read);
        assertReadLockCount(orec, 2);
        assertSurplus(orec, 1);
    }

    @Test
    public void readBiased_whenWriteLockAcquired() {
        AbstractGammaObject orec = makeReadBiased(new GammaLongRef(stm));
        orec.arriveAndLock(1, LOCKMODE_WRITE);

        orec.unlockByUnregistered();
        assertLockMode(orec, LockMode.None);
        assertSurplus(orec, 1);
    }

    @Test
    public void readBiased_whenExclusiveLockAcquired() {
        AbstractGammaObject orec = makeReadBiased(new GammaLongRef(stm));
        orec.arriveAndLock(1, LOCKMODE_EXCLUSIVE);

        orec.unlockByUnregistered();

        assertLockMode(orec, LOCKMODE_NONE);
        assertReadBiased(orec);
        assertSurplus(orec, 1);
    }
}
