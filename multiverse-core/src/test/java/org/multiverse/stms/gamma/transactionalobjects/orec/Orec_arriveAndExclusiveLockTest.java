package org.multiverse.stms.gamma.transactionalobjects.orec;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.stms.gamma.GammaConstants;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactionalobjects.GammaLongRef;

import static org.multiverse.TestUtils.*;
import static org.multiverse.stms.gamma.GammaTestUtils.*;

public class Orec_arriveAndExclusiveLockTest implements GammaConstants {

    private GammaStm stm;

    @Before
    public void setUp() {
        stm = new GammaStm();
    }

    // ==================== write biased ===============================

    @Test
    public void writeBiased_whenNoLockAcquired() {
        GammaLongRef orec = new GammaLongRef(stm);

        int result = orec.arriveAndExclusiveLock(1);

        assertHasMasks(result, MASK_SUCCESS);
        assertNotHasMasks(result, MASK_CONFLICT, MASK_UNREGISTERED);
        assertWriteBiased(orec);
        assertSurplus(orec, 1);
        assertReadonlyCount(orec, 0);
        assertLockMode(orec, LOCKMODE_EXCLUSIVE);
    }

    @Test
    public void writeBiased_whenSurplusOfReaders() {
        GammaLongRef orec = new GammaLongRef(stm);
        orec.arrive(1);

        int result = orec.arriveAndExclusiveLock(1);

        assertHasMasks(result, MASK_SUCCESS, MASK_CONFLICT);
        assertNotHasMasks(result, MASK_UNREGISTERED);
        assertWriteBiased(orec);
        assertSurplus(orec, 2);
        assertReadonlyCount(orec, 0);
        assertLockMode(orec, LOCKMODE_EXCLUSIVE);
    }

    @Test
    public void writeBiased_whenReadLockAcquiredByOther() {
        GammaLongRef orec = new GammaLongRef(stm);
        orec.arriveAndLock(1, LOCKMODE_READ);
        long orecValue = orec.orec;

        int result = orec.arriveAndExclusiveLock(1);

        assertFailure(result);
        assertOrecValue(orec, orecValue);
    }

    @Test
    public void writeBiased_whenWriteLockAcquiredByOther() {
        GammaLongRef orec = new GammaLongRef(stm);
        orec.arriveAndLock(1, LOCKMODE_WRITE);
        long orecValue = orec.orec;

        int result = orec.arriveAndExclusiveLock(1);

        assertFailure(result);
        assertOrecValue(orec, orecValue);
    }

    @Test
    public void writeBiased_whenExclusiveLockAcquiredByOther() {
        GammaLongRef orec = new GammaLongRef(stm);
        orec.arriveAndLock(1, LOCKMODE_EXCLUSIVE);
        long orecValue = orec.orec;

        int result = orec.arriveAndExclusiveLock(1);

        assertFailure(result);
        assertOrecValue(orec, orecValue);
    }

    // ==================== read biased ===============================

    @Test
    public void readBiased_whenNoLockAcquired() {
        GammaLongRef orec = makeReadBiased(new GammaLongRef(stm));

        int result = orec.arriveAndExclusiveLock(1);

        assertHasMasks(result, MASK_SUCCESS, MASK_UNREGISTERED);
        assertNotHasMasks(result, MASK_CONFLICT);
        assertReadBiased(orec);
        assertSurplus(orec, 1);
        assertReadonlyCount(orec, 0);
        assertLockMode(orec, LOCKMODE_EXCLUSIVE);
    }

    @Test
    public void readBiased_whenSurplusOfReaders() {
        GammaLongRef orec = makeReadBiased(new GammaLongRef(stm));
        orec.arrive(1);

        int result = orec.arriveAndExclusiveLock(1);

        assertHasMasks(result, MASK_SUCCESS, MASK_CONFLICT, MASK_UNREGISTERED);
        assertReadBiased(orec);
        assertSurplus(orec, 1);
        assertReadonlyCount(orec, 0);
        assertLockMode(orec, LOCKMODE_EXCLUSIVE);
    }

    @Test
    public void readBiased_whenReadLockAcquiredByOther() {
        GammaLongRef orec = makeReadBiased(new GammaLongRef(stm));
        orec.arriveAndLock(1, LOCKMODE_READ);
        long orecValue = orec.orec;

        int result = orec.arriveAndExclusiveLock(1);

        assertFailure(result);
        assertOrecValue(orec, orecValue);
    }

    @Test
    public void readBiased_whenWriteLockAcquiredByOther() {
        GammaLongRef orec = makeReadBiased(new GammaLongRef(stm));
        orec.arriveAndLock(1, LOCKMODE_WRITE);
        long orecValue = orec.orec;

        int result = orec.arriveAndExclusiveLock(1);

        assertFailure(result);
        assertOrecValue(orec, orecValue);
    }

    @Test
    public void readBiased_whenExclusiveLockAcquiredByOther() {
        GammaLongRef orec = makeReadBiased(new GammaLongRef(stm));
        orec.arriveAndLock(1, LOCKMODE_EXCLUSIVE);
        long orecValue = orec.orec;

        int result = orec.arriveAndExclusiveLock(1);

        assertFailure(result);
        assertOrecValue(orec, orecValue);
    }
}
