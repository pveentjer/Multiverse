package org.multiverse.stms.gamma.transactionalobjects.gammaref;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.LockMode;
import org.multiverse.api.exceptions.LockedException;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.GammaStmConfig;
import org.multiverse.stms.gamma.transactionalobjects.GammaTxnRef;
import org.multiverse.stms.gamma.transactions.GammaTxn;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
import static org.multiverse.TestUtils.assertOrecValue;
import static org.multiverse.api.TxnThreadLocal.clearThreadLocalTxn;
import static org.multiverse.stms.gamma.GammaTestUtils.*;
import static org.multiverse.stms.gamma.transactionalobjects.AbstractGammaObject.setReadonlyCount;
import static org.multiverse.stms.gamma.transactionalobjects.AbstractGammaObject.setSurplus;

public class GammaTxnRef_atomicSetTest {

    private GammaStm stm;

    @Before
    public void setUp() {
        GammaStmConfig config = new GammaStmConfig();
        config.maxRetries = 0;
        stm = new GammaStm(config);
        clearThreadLocalTxn();
    }

    // =================== write biased ==============================

    @Test
    public void writeBiased_whenReadLocked_thenLockedException() {
        writeBiased_whenLocked_thenLockedException(LockMode.Read);
    }

    @Test
    public void writeBiased_whenWriteLocked_thenLockedException() {
        writeBiased_whenLocked_thenLockedException(LockMode.Write);
    }

    @Test
    public void writeBiased_whenExclusiveLocked_thenLockedException() {
        writeBiased_whenLocked_thenLockedException(LockMode.Exclusive);
    }

    public void writeBiased_whenLocked_thenLockedException(LockMode lockMode) {
        String initialValue = "foo";
        GammaTxnRef<String> ref = new GammaTxnRef<String>(stm, initialValue);
        long initialVersion = ref.version;
        GammaTxn tx = stm.newDefaultTxn();
        ref.getLock().acquire(tx, lockMode);
        long orecValue = ref.orec;
        long globalConflictCount = stm.globalConflictCounter.count();

        try {
            ref.atomicSet("bar");
            fail();
        } catch (LockedException expected) {
        }

        assertOrecValue(ref, orecValue);
        assertGlobalConflictCount(stm, globalConflictCount);
        assertVersionAndValue(ref, initialVersion, initialValue);
    }

    @Test
    public void writeBiased_whenNoChange() {
        String initialValue = "foo";
        GammaTxnRef<String> ref = new GammaTxnRef<String>(stm, initialValue);
        long initialVersion = ref.version;
        long orecValue = ref.orec;
        long globalConflictCount = stm.globalConflictCounter.count();

        String result = ref.atomicSet(initialValue);

        assertSame(initialValue, result);
        assertOrecValue(ref, setReadonlyCount(orecValue, 1));
        assertGlobalConflictCount(stm, globalConflictCount);
        assertVersionAndValue(ref, initialVersion, initialValue);
    }

    @Test
    public void writeBiased_whenNoSurplusOfReaders() {
        String initialValue = "foo";
        GammaTxnRef<String> ref = new GammaTxnRef<String>(stm, initialValue);
        long initialVersion = ref.version;
        long orecValue = ref.orec;
        long globalConflictCount = stm.globalConflictCounter.count();

        String newValue = "bar";
        String result = ref.atomicSet(newValue);

        assertSame(newValue, result);
        assertOrecValue(ref, orecValue);
        assertGlobalConflictCount(stm, globalConflictCount);
        assertVersionAndValue(ref, initialVersion+1, newValue);
    }

    @Test
    public void writeBiased_whenSurplusOfReaders_thenGlobalConflictCounterIncreased() {
        String initialValue = "foo";
        GammaTxnRef<String> ref = new GammaTxnRef<String>(stm, initialValue);
        long initialVersion = ref.version;

        ref.arrive(1);

        long orecValue = ref.orec;
        long globalConflictCount = stm.globalConflictCounter.count();

        String newValue = "bar";
        String result = ref.atomicSet(newValue);

        assertSame(newValue, result);
        assertOrecValue(ref, orecValue);
        assertGlobalConflictCount(stm, globalConflictCount+1);
        assertVersionAndValue(ref, initialVersion+1, newValue);
    }

    // =================== read biased ==============================

    @Test
    public void readBiased_whenReadLocked_thenLockedException() {
        readBiased_whenLocked_thenLockedException(LockMode.Read);
    }

    @Test
    public void readBiased_whenWriteLocked_thenLockedException() {
        readBiased_whenLocked_thenLockedException(LockMode.Write);
    }

    @Test
    public void readBiased_whenExclusiveLocked_thenLockedException() {
        readBiased_whenLocked_thenLockedException(LockMode.Exclusive);
    }

    public void readBiased_whenLocked_thenLockedException(LockMode lockMode) {
        String initialValue = "foo";
        GammaTxnRef<String> ref = makeReadBiased(new GammaTxnRef<String>(stm, initialValue));
        long initialVersion = ref.version;
        GammaTxn tx = stm.newDefaultTxn();
        ref.getLock().acquire(tx, lockMode);
        long orecValue = ref.orec;
        long globalConflictCount = stm.globalConflictCounter.count();

        try {
            ref.atomicSet("bar");
            fail();
        } catch (LockedException expected) {
        }

        assertOrecValue(ref, orecValue);
        assertGlobalConflictCount(stm, globalConflictCount);
        assertVersionAndValue(ref, initialVersion, initialValue);
    }

    @Test
    public void readBiased_whenNoChange() {
        String initialValue = "foo";
        GammaTxnRef<String> ref = makeReadBiased(new GammaTxnRef<String>(stm, initialValue));
        long initialVersion = ref.version;
        long orecValue = ref.orec;
        long globalConflictCount = stm.globalConflictCounter.count();

        String result = ref.atomicSet(initialValue);

        System.out.println(ref.toDebugString());

        assertSame(initialValue, result);
        assertOrecValue(ref, setSurplus(orecValue, 1));
        assertGlobalConflictCount(stm, globalConflictCount);
        assertVersionAndValue(ref, initialVersion, initialValue);
    }

    @Test
    public void readBiased_whenNoSurplusOfReaders() {
        String initialValue = "foo";
        GammaTxnRef<String> ref = makeReadBiased(new GammaTxnRef<String>(stm, initialValue));

        long initialVersion = ref.version;
        long globalConflictCount = stm.globalConflictCounter.count();

        String newValue = "bar";
        String result = ref.atomicSet(newValue);

        System.out.println(ref.toDebugString());

        assertSame(newValue, result);
        assertWriteBiased(ref);
        assertSurplus(ref, 0);
        assertReadonlyCount(ref, 0);
        assertLockMode(ref, LockMode.None);
        assertGlobalConflictCount(stm, globalConflictCount);
        assertVersionAndValue(ref, initialVersion+1, newValue);
    }

    @Test
    public void readBiased_whenSurplusOfReaders_thenGlobalConflictCounterIncreased() {
        String initialValue = "foo";
        GammaTxnRef<String> ref = makeReadBiased(new GammaTxnRef<String>(stm, initialValue));
        long initialVersion = ref.version;

        ref.arrive(1);
        long globalConflictCount = stm.globalConflictCounter.count();

        String newValue = "bar";
        String result = ref.atomicSet(newValue);

        System.out.println(ref.toDebugString());

        assertSame(newValue, result);
        assertWriteBiased(ref);
        assertSurplus(ref, 0);
        assertReadonlyCount(ref, 0);
        assertLockMode(ref, LockMode.None);
        assertGlobalConflictCount(stm, globalConflictCount+1);
        assertVersionAndValue(ref, initialVersion+1, newValue);
    }
}
