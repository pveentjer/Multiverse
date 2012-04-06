package org.multiverse.stms.gamma;

import org.junit.Assert;
import org.multiverse.TestUtils;
import org.multiverse.api.LockMode;
import org.multiverse.api.blocking.RetryLatch;
import org.multiverse.api.functions.Function;
import org.multiverse.stms.gamma.transactionalobjects.*;
import org.multiverse.stms.gamma.transactions.GammaTxn;
import org.multiverse.stms.gamma.transactions.GammaTxnConfig;
import org.multiverse.stms.gamma.transactions.fat.FatVariableLengthGammaTxn;

import java.util.*;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.*;
import static org.multiverse.TestUtils.getField;

public class GammaTestUtils implements GammaConstants {

    public static GammaTxn newArrivingTransaction(GammaStm stm){
        GammaTxnConfig config = new GammaTxnConfig(stm)
                .setMaximumPoorMansConflictScanLength(0)
                .setSpeculative(false);
        return new FatVariableLengthGammaTxn(config);
    }

    public static void assertGlobalConflictCount(GammaStm stm, long expected){
        assertEquals(expected, stm.globalConflictCounter.count());
    }

    public static void causeLotsOfConflicts(GammaStm stm) {
        for (int k = 0; k < 100; k++) {
            stm.getGlobalConflictCounter().signalConflict();
        }
    }

    public static void assertHasCommutingFunctions(Tranlocal tranlocal, Function... expected) {
        CallableNode current = tranlocal.headCallable;
        List<Function> functions = new LinkedList<Function>();
        while (current != null) {
            functions.add(current.function);
            current = current.next;
        }

        Assert.assertEquals(asList(expected), functions);
    }

    public static void assertSpeculativeConfigurationNonRefTypeRequired(GammaTxn tx) {
        assertTrue(tx.config.speculativeConfiguration.get().nonRefTypeDetected);
    }

    public static void assertHasListeners(AbstractGammaObject ref, RetryLatch... listeners) {
        Set<RetryLatch> expected = new HashSet<RetryLatch>(Arrays.asList(listeners));

        Set<RetryLatch> found = new HashSet<RetryLatch>();
        Listeners l = (Listeners) getField(ref, "listeners");
        while (l != null) {
            found.add(l.listener);
            l = l.next;
        }
        Assert.assertEquals(expected, found);
    }

    public static void assertHasNoListeners(AbstractGammaObject ref) {
        assertHasListeners(ref);
    }

    public static void assertRefHasNoLocks(AbstractGammaObject ref) {
        assertLockMode(ref, LOCKMODE_NONE);
        assertReadLockCount(ref, 0);
    }

    public static void assertRefHasReadLock(BaseGammaTxnRef ref, GammaTxn tx) {
        Tranlocal tranlocal = tx.getRefTranlocal(ref);
        if (tranlocal == null) {
            fail("A Tranlocal should have been available for a ref that has the read lock");
        }
        Assert.assertEquals(LOCKMODE_READ, tranlocal.getLockMode());
        assertLockMode(ref, LOCKMODE_READ);
    }

    public static void assertRefHasNoLocks(BaseGammaTxnRef ref, GammaTxn tx) {
        Tranlocal tranlocal = tx.getRefTranlocal(ref);
        if (tranlocal != null) {
            Assert.assertEquals(LOCKMODE_NONE, tranlocal.getLockMode());
        }
        assertLockMode(ref, LOCKMODE_NONE);
        assertReadLockCount(ref, 0);
    }

    public static void assertRefHasWriteLock(BaseGammaTxnRef ref, GammaTxn lockOwner) {
        Tranlocal tranlocal = lockOwner.getRefTranlocal(ref);
        if (tranlocal == null) {
            fail("A Tranlocal should have been available for a ref that has the write lock");
        }
        Assert.assertEquals(LOCKMODE_WRITE, tranlocal.getLockMode());
        assertLockMode(ref, LOCKMODE_WRITE);
        assertReadLockCount(ref, 0);
    }

    public static void assertRefHasExclusiveLock(BaseGammaTxnRef ref, GammaTxn lockOwner) {
        Tranlocal tranlocal = lockOwner.getRefTranlocal(ref);
        if (tranlocal == null) {
            fail("A tranlocal should have been stored in the transaction for the ref");
        }
        Assert.assertEquals(LOCKMODE_EXCLUSIVE, tranlocal.getLockMode());
        assertLockMode(ref, LOCKMODE_EXCLUSIVE);
        assertReadLockCount(ref, 0);
    }

    public static void assertRefHasLockMode(BaseGammaTxnRef ref, GammaTxn lockOwner, int lockMode) {
        switch (lockMode) {
            case LOCKMODE_NONE:
                assertRefHasNoLocks(ref, lockOwner);
                break;
            case LOCKMODE_READ:
                assertRefHasReadLock(ref, lockOwner);
                break;
            case LOCKMODE_WRITE:
                assertRefHasWriteLock(ref, lockOwner);
                break;
            case LOCKMODE_EXCLUSIVE:
                assertRefHasExclusiveLock(ref, lockOwner);
                break;
            default:
                throw new IllegalArgumentException();
        }
    }

    public static void assertVersionAndValue(GammaTxnLong ref, long version, long value) {
        Assert.assertEquals("version doesn't match", version, ref.getVersion());
        Assert.assertEquals("value doesn't match", value, ref.atomicWeakGet());
    }

    public static void assertVersionAndValue(GammaTxnBoolean ref, long version, boolean value) {
        Assert.assertEquals("version doesn't match", version, ref.getVersion());
        Assert.assertEquals("value doesn't match", value, ref.atomicWeakGet());
    }

    public static void assertVersionAndValue(GammaTxnInteger ref, long version, int value) {
        Assert.assertEquals("version doesn't match", version, ref.getVersion());
        Assert.assertEquals("value doesn't match", value, ref.atomicWeakGet());
    }

    public static void assertVersionAndValue(GammaTxnDouble ref, long version, double value) {
        Assert.assertEquals("version doesn't match", version, ref.getVersion());
        TestUtils.assertEqualsDouble(format("value doesn't match, expected %s found %s", value, ref.atomicWeakGet()),value, ref.atomicWeakGet());
    }

    public static <E> void assertVersionAndValue(GammaTxnRef<E> ref, long version, E value) {
        Assert.assertEquals("version doesn't match", version, ref.getVersion());
        Assert.assertSame("value doesn't match", value, ref.atomicWeakGet());
    }

    public static void assertReadLockCount(AbstractGammaObject orec, int readLockCount) {
        if (readLockCount > 0) {
            assertEquals(LOCKMODE_READ, orec.atomicGetLockModeAsInt());
        }
        assertEquals(readLockCount, orec.getReadLockCount());
    }

    public static void assertLockMode(GammaObject orec, LockMode lockMode) {
        assertEquals(lockMode, orec.getLock().atomicGetLockMode());
    }

    public static void assertLockMode(AbstractGammaObject orec, int lockMode) {
        assertEquals(lockMode, orec.atomicGetLockModeAsInt());
        if (lockMode != LOCKMODE_READ) {
            assertEquals(0, orec.getReadLockCount());
        }
    }

    public static void assertSurplus(AbstractGammaObject orec, int expectedSurplus) {
        assertEquals(expectedSurplus, orec.getSurplus());
    }

    public static void assertReadBiased(AbstractGammaObject orec, boolean readBiased) {
        if (readBiased) {
            assertReadBiased(orec);
        } else {
            assertWriteBiased(orec);
        }
    }

    public static void assertReadBiased(AbstractGammaObject orec) {
        assertTrue(orec.isReadBiased());
    }

    public static void assertWriteBiased(AbstractGammaObject orec) {
        assertFalse(orec.isReadBiased());
    }

    public static void assertReadonlyCount(AbstractGammaObject orec, int expectedReadonlyCount) {
        assertEquals(expectedReadonlyCount, orec.getReadonlyCount());
    }

    public static <O extends AbstractGammaObject> O makeReadBiased(O orec) {
        if (orec.isReadBiased()) {
            return orec;
        }

        int x = orec.getReadonlyCount();
        for (int k = x; k < orec.getReadBiasedThreshold(); k++) {
            orec.arrive(1);
            orec.departAfterReading();
        }

        assertReadBiased(orec);
        assertLockMode(orec, LOCKMODE_NONE);

        return orec;
    }
}
