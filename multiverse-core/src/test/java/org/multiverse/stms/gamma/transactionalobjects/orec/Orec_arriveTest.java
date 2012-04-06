package org.multiverse.stms.gamma.transactionalobjects.orec;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.stms.gamma.GammaConstants;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactionalobjects.AbstractGammaObject;
import org.multiverse.stms.gamma.transactionalobjects.GammaTxnLong;

import static junit.framework.Assert.assertEquals;
import static org.multiverse.TestUtils.*;
import static org.multiverse.stms.gamma.GammaTestUtils.*;

public class Orec_arriveTest implements GammaConstants {

    private GammaStm stm;

    @Before
    public void setUp() {
        stm = new GammaStm();
    }

    // ======================= write biased ==============================

    @Test
    public void writeBiased_whenNotLockedAndNoSurplus_thenNormalArrive() {
        AbstractGammaObject orec = new GammaTxnLong(stm, 0);

        int result = orec.arrive(1);

        assertHasMasks(result, MASK_SUCCESS);
        assertNotHasMasks(result, MASK_CONFLICT, MASK_UNREGISTERED);
        assertSurplus(orec, 1);
        assertReadonlyCount(orec, 0);
        assertWriteBiased(orec);
        assertLockMode(orec, LOCKMODE_NONE);
    }

    @Test
    public void writeBiased_whenNotLockedAndSurplus_thenNormalArrive() {
        AbstractGammaObject orec = new GammaTxnLong(stm);
        orec.arrive(1);
        orec.arrive(1);

        int result = orec.arrive(1);

        assertHasMasks(result, MASK_SUCCESS);
        assertNotHasMasks(result, MASK_CONFLICT, MASK_UNREGISTERED);
        assertWriteBiased(orec);
        assertSurplus(orec, 3);
        assertReadonlyCount(orec, 0);
        assertLockMode(orec, LOCKMODE_NONE);
    }

    @Test
    public void writeBiased_whenExclusiveLock_thenLockNotFree() {
        AbstractGammaObject orec = new GammaTxnLong(stm);
        orec.arriveAndLock(1, LOCKMODE_EXCLUSIVE);
        long orecValue = orec.orec;

        int result = orec.arrive(1);

        assertEquals(FAILURE, result);
        assertOrecValue(orec, orecValue);
    }

    @Test
    public void writeBiased_whenWriteLock_thenArriveSuccess() {
        AbstractGammaObject orec = new GammaTxnLong(stm);
        orec.arriveAndLock(1, LOCKMODE_WRITE);

        int result = orec.arrive(1);

        assertHasMasks(result, MASK_SUCCESS);
        assertNotHasMasks(result, MASK_CONFLICT, MASK_CONFLICT);
        assertSurplus(orec, 2);
        assertReadonlyCount(orec, 0);
        assertWriteBiased(orec);
        assertLockMode(orec, LOCKMODE_WRITE);
    }

    // ======================= read biased ==============================

    @Test
    public void readBiased_whenNoSurplus() {
        AbstractGammaObject orec = makeReadBiased(new GammaTxnLong(stm));

        int result = orec.arrive(1);

        assertHasMasks(result, MASK_SUCCESS, MASK_UNREGISTERED);
        assertNotHasMasks(result, MASK_CONFLICT);
        assertLockMode(orec, LOCKMODE_NONE);
        assertSurplus(orec, 1);
        assertReadBiased(orec);
        assertReadonlyCount(orec, 0);
    }

    @Test
    public void readBiased_whenSurplus_thenCallIgnored() {
        AbstractGammaObject orec = makeReadBiased(new GammaTxnLong(stm));
        orec.arrive(1);
        long orecValue = orec.orec;
        int result = orec.arrive(1);

        assertHasMasks(result, MASK_SUCCESS, MASK_UNREGISTERED);
        assertNotHasMasks(result, MASK_CONFLICT);
        assertOrecValue(orec, orecValue);
    }

    @Test
    public void readBiased_whenReadLockAcquired() {
        AbstractGammaObject orec = makeReadBiased(new GammaTxnLong(stm));
        orec.arriveAndLock(1, LOCKMODE_READ);
        long orecValue = orec.orec;

        int result = orec.arrive(1);

        assertHasMasks(result, MASK_SUCCESS, MASK_UNREGISTERED);
        assertNotHasMasks(result, MASK_CONFLICT);
        assertOrecValue(orec, orecValue);
    }

    @Test
    public void readBiased_whenWriteLockAcquired() {
        AbstractGammaObject orec = makeReadBiased(new GammaTxnLong(stm));
        orec.arriveAndLock(1, LOCKMODE_WRITE);
        long orecValue = orec.orec;

        int result = orec.arrive(1);

        assertHasMasks(result, MASK_SUCCESS, MASK_UNREGISTERED);
        assertNotHasMasks(result, MASK_CONFLICT);
        assertOrecValue(orec, orecValue);
    }

    @Test
    public void readBiased_whenExclusiveLockAquired() {
        AbstractGammaObject orec = makeReadBiased(new GammaTxnLong(stm));
        orec.arriveAndLock(1, LOCKMODE_EXCLUSIVE);
        long orecValue = orec.orec;

        int result = orec.arrive(1);

        assertFailure(result);
        assertOrecValue(orec, orecValue);
    }
}
