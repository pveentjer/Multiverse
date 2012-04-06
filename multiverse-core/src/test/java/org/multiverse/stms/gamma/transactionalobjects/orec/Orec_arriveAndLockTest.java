package org.multiverse.stms.gamma.transactionalobjects.orec;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.stms.gamma.GammaConstants;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactionalobjects.AbstractGammaObject;
import org.multiverse.stms.gamma.transactionalobjects.GammaTxnLong;

import static org.junit.Assert.assertEquals;
import static org.multiverse.TestUtils.*;
import static org.multiverse.stms.gamma.GammaTestUtils.*;

public class Orec_arriveAndLockTest implements GammaConstants {

    private GammaStm stm;

    @Before
    public void setUp() {
        stm = new GammaStm();
    }

    // ================ write biased and acquire read lock ========================

    @Test
    public void writeBiased_acquireReadLock_whenUnlocked() {
        AbstractGammaObject orec = new GammaTxnLong(stm);

        int result = orec.arriveAndLock(1, LOCKMODE_READ);

        assertHasMasks(result, MASK_SUCCESS);
        assertNotHasMasks(result, MASK_CONFLICT, MASK_UNREGISTERED);
        assertSurplus(orec, 1);
        assertWriteBiased(orec);
        assertReadonlyCount(orec, 0);
        assertReadLockCount(orec, 1);
    }

    @Test
    public void writeBiased_acquireReadLock_whenUnlockedAndConflictingReaders() {
        AbstractGammaObject orec = new GammaTxnLong(stm);
        orec.arrive(1);

        int result = orec.arriveAndLock(1, LOCKMODE_READ);

        assertHasMasks(result, MASK_SUCCESS);
        assertNotHasMasks(result, MASK_CONFLICT, MASK_UNREGISTERED);
        assertSurplus(orec, 2);
        assertWriteBiased(orec);
        assertReadonlyCount(orec, 0);
        assertReadLockCount(orec, 1);
    }

    @Test
    public void writeBiased_acquireReadLock_whenWriteLocked() {
        AbstractGammaObject orec = new GammaTxnLong(stm);
        orec.arriveAndLock(1, LOCKMODE_WRITE);
        long orecValue = orec.orec;

        int result = orec.arriveAndLock(1, LOCKMODE_READ);

        assertFailure(result);
        assertOrecValue(orec, orecValue);
    }

    @Test
    public void writeBiased_acquireReadLock_whenExclusiveLocked() {
        AbstractGammaObject orec = new GammaTxnLong(stm);
        orec.arriveAndLock(1, LOCKMODE_EXCLUSIVE);
        long orecValue = orec.orec;

        int result = orec.arriveAndLock(1, LOCKMODE_READ);

        assertFailure(result);
        assertOrecValue(orec, orecValue);
    }

    @Test
    public void writeBiased_acquireReadLock_whenReadLocked() {
        AbstractGammaObject orec = new GammaTxnLong(stm);
        orec.arriveAndLock(1, LOCKMODE_READ);

        int result = orec.arriveAndLock(1, LOCKMODE_READ);

        assertHasMasks(result, MASK_SUCCESS);
        assertNotHasMasks(result, MASK_CONFLICT, MASK_UNREGISTERED);
        assertSurplus(orec, 2);
        assertWriteBiased(orec);
        assertReadonlyCount(orec, 0);
        assertReadLockCount(orec, 2);
    }

    // ================ write biased and acquire write lock ========================

    @Test
    public void writeBiased_acquireWriteLock_whenUnlocked() {
        AbstractGammaObject orec = new GammaTxnLong(stm);

        int result = orec.arriveAndLock(1, LOCKMODE_WRITE);

        assertHasMasks(result, MASK_SUCCESS);
        assertNotHasMasks(result, MASK_UNREGISTERED, MASK_CONFLICT);
        assertSurplus(orec, 1);
        assertWriteBiased(orec);
        assertReadonlyCount(orec, 0);
        assertLockMode(orec, LOCKMODE_WRITE);
    }

    @Test
    public void writeBiased_acquireWriteLock_whenUnlockedAndConflictingReaders() {
        AbstractGammaObject orec = new GammaTxnLong(stm);
        orec.arrive(1);

        int result = orec.arriveAndLock(1, LOCKMODE_WRITE);

        assertHasMasks(result, MASK_SUCCESS);
        assertNotHasMasks(result, MASK_UNREGISTERED, MASK_CONFLICT);
        assertSurplus(orec, 2);
        assertWriteBiased(orec);
        assertReadonlyCount(orec, 0);
        assertLockMode(orec, LOCKMODE_WRITE);
    }

    @Test
    public void writeBiased_acquireWriteLock_whenWriteLocked() {
        AbstractGammaObject orec = new GammaTxnLong(stm);
        orec.arriveAndLock(1, LOCKMODE_WRITE);
        long orecValue = orec.orec;

        int result = orec.arriveAndLock(1, LOCKMODE_WRITE);

        assertFailure(result);
        assertOrecValue(orec, orecValue);
    }

    @Test
    public void writeBiased_acquireWriteLock_whenExclusiveLocked() {
        AbstractGammaObject orec = new GammaTxnLong(stm);
        orec.arriveAndLock(1, LOCKMODE_EXCLUSIVE);
        long orecValue = orec.orec;

        int result = orec.arriveAndLock(1, LOCKMODE_WRITE);

        assertFailure(result);
        assertOrecValue(orec, orecValue);
    }

    @Test
    public void writeBiased_acquireWriteLock_whenReadLocked() {
        AbstractGammaObject orec = new GammaTxnLong(stm);
        orec.arriveAndLock(1, LOCKMODE_READ);
        long orecValue = orec.orec;

        int result = orec.arriveAndLock(1, LOCKMODE_WRITE);

        assertFailure(result);
        assertOrecValue(orec, orecValue);
    }

    // ================ write biased and acquire exclusive lock ========================

    @Test
    public void writeBiased_acquireExclusiveLock_whenUnlocked() {
        AbstractGammaObject orec = new GammaTxnLong(stm);

        int result = orec.arriveAndLock(1, LOCKMODE_EXCLUSIVE);

        assertHasMasks(result, MASK_SUCCESS);
        assertNotHasMasks(result, MASK_CONFLICT, MASK_UNREGISTERED);
        assertSurplus(orec, 1);
        assertWriteBiased(orec);
        assertReadonlyCount(orec, 0);
        assertLockMode(orec, LOCKMODE_EXCLUSIVE);
    }

    @Test
    public void writeBiased_acquireExclusiveLock_whenUnlockedAndConflictingReaders() {
        AbstractGammaObject orec = new GammaTxnLong(stm);
        orec.arrive(1);

        int result = orec.arriveAndLock(1, LOCKMODE_EXCLUSIVE);

        assertHasMasks(result, MASK_SUCCESS, MASK_CONFLICT);
        assertNotHasMasks(result, MASK_UNREGISTERED);
        assertSurplus(orec, 2);
        assertWriteBiased(orec);
        assertReadonlyCount(orec, 0);
        assertLockMode(orec, LOCKMODE_EXCLUSIVE);
    }

    @Test
    public void writeBiased_acquireExclusiveLock_whenWriteLocked() {
        AbstractGammaObject orec = new GammaTxnLong(stm);
        orec.arriveAndLock(1, LOCKMODE_WRITE);
        long orecValue = orec.orec;

        int result = orec.arriveAndLock(1, LOCKMODE_EXCLUSIVE);

        assertFailure(result);
        assertOrecValue(orec, orecValue);
    }

    @Test
    public void writeBiased_acquireExclusiveLock_whenExclusiveLocked() {
        AbstractGammaObject orec = new GammaTxnLong(stm);
        orec.arriveAndLock(1, LOCKMODE_EXCLUSIVE);
        long orecValue = orec.orec;

        int result = orec.arriveAndLock(1, LOCKMODE_EXCLUSIVE);

        assertFailure(result);
        assertOrecValue(orec, orecValue);
    }

    @Test
    public void writeBiased_acquireExclusiveLock_whenReadLocked() {
        AbstractGammaObject orec = new GammaTxnLong(stm);
        orec.arriveAndLock(1, LOCKMODE_READ);
        long orecValue = orec.orec;

        int result = orec.arriveAndLock(1, LOCKMODE_EXCLUSIVE);

        assertFailure(result);
        assertOrecValue(orec, orecValue);
    }

    // ====================================================================

    @Test
    public void readBiased_acquireExclusiveLock_whenUnlockedAndNoSurplus() {
        AbstractGammaObject orec = makeReadBiased(new GammaTxnLong(stm));

        int result = orec.arriveAndLock(1, LOCKMODE_EXCLUSIVE);

        assertHasMasks(result, MASK_SUCCESS, MASK_UNREGISTERED);
        assertNotHasMasks(result, MASK_CONFLICT);
        assertSurplus(orec, 1);
        assertReadBiased(orec);
        assertReadonlyCount(orec, 0);
        assertLockMode(orec, LOCKMODE_EXCLUSIVE);
    }

    @Test
    public void readBiased_acquireExclusiveLock_whenUnlockedAndSurplus() {
        AbstractGammaObject orec = makeReadBiased(new GammaTxnLong(stm));
        orec.arrive(1);

        int result = orec.arriveAndLock(1, LOCKMODE_EXCLUSIVE);

        assertHasMasks(result, MASK_SUCCESS, MASK_UNREGISTERED, MASK_CONFLICT);
        assertSurplus(orec, 1);
        assertReadBiased(orec);
        assertReadonlyCount(orec, 0);
        assertLockMode(orec, LOCKMODE_EXCLUSIVE);
    }

    @Test
    public void readBiased_acquireExclusiveLock_whenReadLocked() {
        AbstractGammaObject orec = makeReadBiased(new GammaTxnLong(stm));
        orec.arriveAndLock(1, LOCKMODE_READ);
        long orecValue = orec.orec;

        int result = orec.arriveAndLock(1, LOCKMODE_EXCLUSIVE);

        assertEquals(FAILURE, result);
        assertOrecValue(orec, orecValue);
    }

    @Test
    public void readBiased_acquireExclusiveLock_whenWriteLocked() {
        AbstractGammaObject orec = makeReadBiased(new GammaTxnLong(stm));
        orec.arriveAndLock(1, LOCKMODE_WRITE);
        long orecValue = orec.orec;

        int result = orec.arriveAndLock(1, LOCKMODE_EXCLUSIVE);

        assertFailure(result);
        assertOrecValue(orec, orecValue);
    }

    @Test
    public void readBiased_acquireExclusiveLock_whenExclusiveLocked() {
        AbstractGammaObject orec = makeReadBiased(new GammaTxnLong(stm));
        orec.arriveAndLock(1, LOCKMODE_EXCLUSIVE);
        long orecValue = orec.orec;

        int result = orec.arriveAndLock(1, LOCKMODE_EXCLUSIVE);

        assertFailure(result);
        assertOrecValue(orec, orecValue);
    }


    // =======================================================================

    @Test
    public void readBiased_acquireWriteLock_whenUnlocked() {
        AbstractGammaObject orec = makeReadBiased(new GammaTxnLong(stm));

        int result = orec.arriveAndLock(1, LOCKMODE_WRITE);

        assertHasMasks(result, MASK_SUCCESS, MASK_UNREGISTERED);
        assertNotHasMasks(result, MASK_CONFLICT);
        assertSurplus(orec, 1);
        assertReadBiased(orec);
        assertReadonlyCount(orec, 0);
        assertLockMode(orec, LOCKMODE_WRITE);
    }

    @Test
    public void readBiased_acquireWriteLock_whenUnlockedAndSurplus() {
        AbstractGammaObject orec = makeReadBiased(new GammaTxnLong(stm));

        int result = orec.arriveAndLock(1, LOCKMODE_WRITE);

        assertHasMasks(result, MASK_SUCCESS, MASK_UNREGISTERED);
        assertNotHasMasks(result, MASK_CONFLICT);
        assertSurplus(orec, 1);
        assertReadBiased(orec);
        assertReadonlyCount(orec, 0);
        assertLockMode(orec, LOCKMODE_WRITE);
    }

    @Test
    public void readBiased_acquireWriteLock_whenWriteLocked() {
        AbstractGammaObject orec = makeReadBiased(new GammaTxnLong(stm));
        orec.arriveAndLock(1, LOCKMODE_WRITE);
        long orecValue = orec.orec;

        int result = orec.arriveAndLock(1, LOCKMODE_WRITE);

        assertFailure(result);
        assertOrecValue(orec, orecValue);
    }

    @Test
    public void readBiased_acquireWriteLock_whenExclusiveLocked() {
        AbstractGammaObject orec = makeReadBiased(new GammaTxnLong(stm));
        orec.arriveAndLock(1, LOCKMODE_EXCLUSIVE);
        long orecValue = orec.orec;

        int result = orec.arriveAndLock(1, LOCKMODE_WRITE);

        assertFailure(result);
        assertOrecValue(orec, orecValue);
    }

    @Test
    public void readBiased_acquireWriteLock_whenReadLocked() {
        AbstractGammaObject orec = makeReadBiased(new GammaTxnLong(stm));
        orec.arriveAndLock(1, LOCKMODE_READ);
        long orecValue = orec.orec;

        int result = orec.arriveAndLock(1, LOCKMODE_WRITE);

        assertFailure(result);
        assertOrecValue(orec, orecValue);
    }

    // =======================================================================

    @Test
    public void readBiased_acquireReadLock_whenUnlocked() {
        AbstractGammaObject orec = makeReadBiased(new GammaTxnLong(stm));

        int result = orec.arriveAndLock(1, LOCKMODE_READ);

        assertHasMasks(result, MASK_SUCCESS, MASK_UNREGISTERED);
        assertNotHasMasks(result, MASK_CONFLICT);
        assertSurplus(orec, 1);
        assertReadBiased(orec);
        assertReadonlyCount(orec, 0);
        assertReadLockCount(orec, 1);
    }

    @Test
    public void readBiased_acquireReadLock_whenSurplusAndUnlocked() {
        AbstractGammaObject orec = makeReadBiased(new GammaTxnLong(stm));
        orec.arrive(1);

        int result = orec.arriveAndLock(1, LOCKMODE_READ);

        assertHasMasks(result, MASK_SUCCESS, MASK_UNREGISTERED);
        assertNotHasMasks(result, MASK_CONFLICT);
        assertSurplus(orec, 1);
        assertReadBiased(orec);
        assertReadonlyCount(orec, 0);
        assertReadLockCount(orec, 1);
    }

    @Test
    public void readBiased_acquireReadLock_whenWriteLocked() {
        AbstractGammaObject orec = makeReadBiased(new GammaTxnLong(stm));
        orec.arriveAndLock(1, LOCKMODE_WRITE);
        long orecValue = orec.orec;

        int result = orec.arriveAndLock(1, LOCKMODE_READ);

        assertEquals(FAILURE, result);
        assertOrecValue(orec, orecValue);
    }

    @Test
    public void readBiased_acquireReadLock_whenExclusiveLocked() {
        AbstractGammaObject orec = makeReadBiased(new GammaTxnLong(stm));
        orec.arriveAndLock(1, LOCKMODE_EXCLUSIVE);
        long orecValue = orec.orec;

        int result = orec.arriveAndLock(1, LOCKMODE_READ);

        assertEquals(FAILURE, result);
        assertOrecValue(orec, orecValue);
    }

    @Test
    public void readBiased_acquireReadLock_whenReadLocked() {
        AbstractGammaObject orec = makeReadBiased(new GammaTxnLong(stm));
        orec.arriveAndLock(1, LOCKMODE_READ);

        int result = orec.arriveAndLock(1, LOCKMODE_READ);

        assertHasMasks(result, MASK_SUCCESS, MASK_UNREGISTERED);
        assertNotHasMasks(result, MASK_CONFLICT);
        assertSurplus(orec, 1);
        assertReadBiased(orec);
        assertReadonlyCount(orec, 0);
        assertReadLockCount(orec, 2);
    }
}
