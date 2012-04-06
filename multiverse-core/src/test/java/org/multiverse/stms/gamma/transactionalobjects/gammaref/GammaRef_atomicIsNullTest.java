package org.multiverse.stms.gamma.transactionalobjects.gammaref;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.LockMode;
import org.multiverse.api.exceptions.LockedException;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactionalobjects.GammaRef;
import org.multiverse.stms.gamma.transactions.GammaTxn;

import static org.junit.Assert.*;
import static org.multiverse.TestUtils.assertOrecValue;
import static org.multiverse.api.TxnThreadLocal.clearThreadLocalTxn;
import static org.multiverse.api.TxnThreadLocal.setThreadLocalTxn;
import static org.multiverse.stms.gamma.GammaTestUtils.assertGlobalConflictCount;
import static org.multiverse.stms.gamma.GammaTestUtils.assertVersionAndValue;
import static org.multiverse.stms.gamma.GammaTestUtils.makeReadBiased;

public class GammaRef_atomicIsNullTest {

    private GammaStm stm;

    @Before
    public void setUp() {
        stm = new GammaStm();
        clearThreadLocalTxn();
    }

    // ==================== write biased ========================================

    @Test
    public void writeBiased_whenReadLocked_thenSuccess() {
        String initialValue = null;
        GammaRef<String> ref = new GammaRef<String>(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTxn tx = stm.newDefaultTxn();
        ref.getLock().acquire(tx, LockMode.Read);
        long orecValue = ref.orec;
        long conflictCount = stm.globalConflictCounter.count();

        boolean result = ref.atomicIsNull();

        assertTrue(result);
        assertOrecValue(ref, orecValue);
        assertVersionAndValue(ref, initialVersion, initialValue);
        assertGlobalConflictCount(stm, conflictCount);
    }

    @Test
    public void writeBiased_whenWriteLocked_thenSuccess() {
        String initialValue = null;
        GammaRef<String> ref = new GammaRef<String>(stm, initialValue);
        long initialVersion = ref.getVersion();
        GammaTxn tx = stm.newDefaultTxn();
        ref.getLock().acquire(tx, LockMode.Write);
        long orecValue = ref.orec;
        long conflictCount = stm.globalConflictCounter.count();

        boolean result = ref.atomicIsNull();

        assertTrue(result);
        assertOrecValue(ref, orecValue);
        assertVersionAndValue(ref, initialVersion, initialValue);
        assertGlobalConflictCount(stm, conflictCount);
    }

    @Test
    public void writeBiased_whenExclusiveLocked_thenLockedException() {
        GammaRef<String> ref = new GammaRef<String>(stm, null);
        long initialVersion = ref.version;
        GammaTxn tx = stm.newDefaultTxn();
        ref.getLock().acquire(tx, LockMode.Exclusive);
        long orecValue = ref.orec;
        long conflictCount = stm.globalConflictCounter.count();

        try {
            ref.atomicIsNull();
            fail();
        } catch (LockedException expected) {
        }

        assertOrecValue(ref, orecValue);
        assertVersionAndValue(ref, initialVersion, null);
        assertGlobalConflictCount(stm, conflictCount);
    }

    @Test
    public void writeBiased_whenNull() {
        GammaRef<String> ref = new GammaRef<String>(stm, null);
        long initialVersion = ref.getVersion();
        long orecValue = ref.orec;
        long conflictCount = stm.globalConflictCounter.count();

        boolean result = ref.atomicIsNull();

        assertTrue(result);
        assertOrecValue(ref, orecValue);
        assertVersionAndValue(ref, initialVersion, null);
        assertGlobalConflictCount(stm, conflictCount);
    }

    @Test
    public void writeBiased_whenActiveTransactionAvailable_thenIgnored() {
        String initialValue = "foo";
        GammaRef<String> ref = new GammaRef<String>(stm, initialValue);
        long initialVersion = ref.getVersion();
        GammaTxn tx = stm.newDefaultTxn();
        setThreadLocalTxn(tx);
        ref.set(tx, null);
        long orecValue = ref.orec;
        long conflictCount = stm.globalConflictCounter.count();

        boolean result = ref.atomicIsNull();

        assertFalse(result);
        assertOrecValue(ref, orecValue);
        assertVersionAndValue(ref, initialVersion, initialValue);
        assertGlobalConflictCount(stm, conflictCount);
    }

    @Test
    public void writeBiased_whenNotNull() {
        String initialValue = "foo";
        GammaRef<String> ref = new GammaRef<String>(stm, initialValue);
        long initialVersion = ref.getVersion();
        long orecValue = ref.orec;
        long conflictCount = stm.globalConflictCounter.count();

        boolean result = ref.atomicIsNull();

        assertFalse(result);
        assertOrecValue(ref, orecValue);
        assertVersionAndValue(ref, initialVersion, initialValue);
        assertGlobalConflictCount(stm, conflictCount);
    }

    // ==================== read biased ========================================

    @Test
    public void readBiased_whenReadLocked_thenSuccess() {
        String initialValue = null;
        GammaRef<String> ref = makeReadBiased(new GammaRef<String>(stm, initialValue));
        long initialVersion = ref.getVersion();

        GammaTxn tx = stm.newDefaultTxn();
        ref.getLock().acquire(tx, LockMode.Read);
        long orecValue = ref.orec;
        long conflictCount = stm.globalConflictCounter.count();

        boolean result = ref.atomicIsNull();

        assertTrue(result);
        assertOrecValue(ref, orecValue);
        assertVersionAndValue(ref, initialVersion, initialValue);
        assertGlobalConflictCount(stm, conflictCount);
    }

    @Test
    public void readBiased_whenWriteLocked_thenSuccess() {
        String initialValue = null;
        GammaRef<String> ref = makeReadBiased(new GammaRef<String>(stm, initialValue));
        long initialVersion = ref.getVersion();
        GammaTxn tx = stm.newDefaultTxn();
        ref.getLock().acquire(tx, LockMode.Write);
        long orecValue = ref.orec;
        long conflictCount = stm.globalConflictCounter.count();

        boolean result = ref.atomicIsNull();

        assertTrue(result);
        assertOrecValue(ref, orecValue);
        assertVersionAndValue(ref, initialVersion, initialValue);
        assertGlobalConflictCount(stm, conflictCount);
    }

    @Test
    public void readBiased_whenExclusiveLocked_thenLockedException() {
        String initialValue = null;
        GammaRef<String> ref = makeReadBiased(new GammaRef<String>(stm, initialValue));
        long initialVersion = ref.version;
        GammaTxn tx = stm.newDefaultTxn();
        ref.getLock().acquire(tx, LockMode.Exclusive);
        long orecValue = ref.orec;
        long conflictCount = stm.globalConflictCounter.count();

        try {
            ref.atomicIsNull();
            fail();
        } catch (LockedException expected) {
        }

        assertOrecValue(ref, orecValue);
        assertVersionAndValue(ref, initialVersion, null);
        assertGlobalConflictCount(stm, conflictCount);
    }

    @Test
    public void readBiased_whenNull() {
        String initialValue = null;
        GammaRef<String> ref = makeReadBiased(new GammaRef<String>(stm, initialValue));
        long initialVersion = ref.getVersion();
        long orecValue = ref.orec;
        long conflictCount = stm.globalConflictCounter.count();

        boolean result = ref.atomicIsNull();

        assertTrue(result);
        assertOrecValue(ref, orecValue);
        assertVersionAndValue(ref, initialVersion, null);
        assertGlobalConflictCount(stm, conflictCount);
    }

    @Test
    public void readBiased_whenActiveTransactionAvailable_thenIgnored() {
        String initialValue = "foo";
        GammaRef<String> ref = makeReadBiased(new GammaRef<String>(stm, initialValue));
        long initialVersion = ref.getVersion();
        GammaTxn tx = stm.newDefaultTxn();
        setThreadLocalTxn(tx);
        ref.set(tx, null);
        long orecValue = ref.orec;
        long conflictCount = stm.globalConflictCounter.count();

        boolean result = ref.atomicIsNull();

        assertFalse(result);
        assertOrecValue(ref, orecValue);
        assertVersionAndValue(ref, initialVersion, initialValue);
        assertGlobalConflictCount(stm, conflictCount);
    }

    @Test
    public void readBiased_whenNotNull() {
        String initialValue = "foo";
        GammaRef<String> ref = makeReadBiased(new GammaRef<String>(stm, initialValue));
        long initialVersion = ref.getVersion();
        long orecValue = ref.orec;
        long conflictCount = stm.globalConflictCounter.count();

        boolean result = ref.atomicIsNull();

        assertFalse(result);
        assertOrecValue(ref, orecValue);
        assertVersionAndValue(ref, initialVersion, initialValue);
        assertGlobalConflictCount(stm, conflictCount);
    }
}
