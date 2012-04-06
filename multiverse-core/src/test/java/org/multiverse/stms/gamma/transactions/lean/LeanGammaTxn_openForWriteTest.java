package org.multiverse.stms.gamma.transactions.lean;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.LockMode;
import org.multiverse.api.TxnStatus;
import org.multiverse.api.exceptions.*;
import org.multiverse.api.exceptions.DeadTxnException;
import org.multiverse.api.exceptions.PreparedTxnException;
import org.multiverse.stms.gamma.GammaConstants;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactionalobjects.*;
import org.multiverse.stms.gamma.transactions.GammaTxn;

import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;
import static org.multiverse.TestUtils.*;
import static org.multiverse.stms.gamma.GammaTestUtils.*;

public abstract class LeanGammaTxn_openForWriteTest<T extends GammaTxn> implements GammaConstants {

    public GammaStm stm;

    @Before
    public void setUp() {
        stm = new GammaStm();
    }

    public abstract T newTransaction();

    public abstract int getMaximumLength();

    @Test
    public void whenMultipleReadsAndInconsistent() {
        assumeTrue(getMaximumLength() > 1);

        GammaTxnRef<String> ref1 = new GammaTxnRef<String>(stm);
        GammaTxnRef<String> ref2 = new GammaTxnRef<String>(stm);

        GammaTxn tx = newTransaction();
        ref1.openForWrite(tx, LOCKMODE_NONE);

        ref1.atomicSet("foo");

        try {
            ref2.openForWrite(tx, LOCKMODE_NONE);
            fail();
        } catch (ReadWriteConflict expected) {
        }

        assertIsAborted(tx);
        assertRefHasNoLocks(ref1);
        assertSurplus(ref1, 0);
        assertRefHasNoLocks(ref2);
        assertSurplus(ref2, 0);
    }

    @Test
    public void whenMultipleReadsAndPreviousReadLockedExclusively() {
        assumeTrue(getMaximumLength() > 1);

        GammaTxnRef<String> ref1 = new GammaTxnRef<String>(stm);
        GammaTxnRef<String> ref2 = new GammaTxnRef<String>(stm);

        GammaTxn tx = newTransaction();
        ref1.openForWrite(tx, LOCKMODE_NONE);

        GammaTxn otherTx = stm.newDefaultTxn();
        ref1.getLock().acquire(otherTx, LockMode.Exclusive);

        try {
            ref2.openForWrite(tx, LOCKMODE_NONE);
            fail();
        } catch (ReadWriteConflict expected) {
        }

        assertIsAborted(tx);
        assertRefHasExclusiveLock(ref1, otherTx);
        assertSurplus(ref1, 1);
        assertRefHasNoLocks(ref2);
        assertSurplus(ref2, 0);
    }

    @Test
    public void whenMultipleWrites() {
        assumeTrue(getMaximumLength() > 1);

        String initialValue1 = "foo1";
        GammaTxnRef<String> ref1 = new GammaTxnRef<String>(stm, initialValue1);
        String initialValue2 = "foo2";
        GammaTxnRef<String> ref2 = new GammaTxnRef<String>(stm, initialValue2);

        GammaTxn tx = newTransaction();
        Tranlocal tranlocal1 = ref1.openForWrite(tx, LOCKMODE_NONE);
        Tranlocal tranlocal2 = ref2.openForWrite(tx, LOCKMODE_NONE);

        assertIsActive(tx);

        assertRefHasNoLocks(ref1);
        assertSurplus(ref1, 0);
        assertSame(ref1, tranlocal1.owner);
        assertEquals(TRANLOCAL_WRITE, tranlocal1.mode);
        assertSame(initialValue1, tranlocal1.ref_value);

        assertRefHasNoLocks(ref2);
        assertSurplus(ref2, 0);
        assertSame(ref2, tranlocal2.owner);
        assertEquals(TRANLOCAL_WRITE, tranlocal2.mode);
        assertSame(initialValue2, tranlocal2.ref_value);
    }

    @Test
    public void whenMultipleReadsAndConsistent() {
        assumeTrue(getMaximumLength() > 1);

        GammaTxnRef<String> ref1 = new GammaTxnRef<String>(stm);
        GammaTxnRef<String> ref2 = new GammaTxnRef<String>(stm);

        GammaTxn tx = newTransaction();
        ref1.openForWrite(tx, LOCKMODE_NONE);
        ref2.openForWrite(tx, LOCKMODE_NONE);

        assertIsActive(tx);
        assertRefHasNoLocks(ref1);
        assertSurplus(ref1, 0);
        assertRefHasNoLocks(ref2);
        assertSurplus(ref2, 0);
    }

    @Test
    public void whenExplicitLocking_thenSpeculativeConfigurationFailure() {
        whenExplicitLocking(LockMode.Read);
        whenExplicitLocking(LockMode.Write);
        whenExplicitLocking(LockMode.Exclusive);
    }

    public void whenExplicitLocking(LockMode lockMode) {
        String initialValue = "foo";
        GammaTxnRef<String> ref = new GammaTxnRef<String>(stm, initialValue);
        long initialVersion = ref.getVersion();

        T tx = newTransaction();
        try {
            ref.openForWrite(tx, lockMode.asInt());
            fail();
        } catch (SpeculativeConfigurationError expected) {
        }

        assertIsAborted(tx);
        assertRefHasNoLocks(ref);
        assertSurplus(ref, 0);
        assertReadonlyCount(ref, 0);
        assertWriteBiased(ref);
        assertVersionAndValue(ref, initialVersion, initialValue);
        assertTrue(tx.config.speculativeConfiguration.get().locksDetected);
    }

    @Test
    public void whenIntRef_thenSpeculativeConfigurationError() {
        int initialValue = 10;
        GammaTxnInteger ref = new GammaTxnInteger(stm, initialValue);
        long initialVersion = ref.getVersion();

        T tx = newTransaction();
        try {
            ref.openForWrite(tx, LOCKMODE_NONE);
            fail();
        } catch (SpeculativeConfigurationError expected) {
        }

        assertIsAborted(tx);
        assertRefHasNoLocks(ref);
        assertVersionAndValue(ref, initialVersion, initialValue);
        assertSpeculativeConfigurationNonRefTypeRequired(tx);
    }

    @Test
    public void whenBooleanRef_thenSpeculativeConfigurationError() {
        boolean initialValue = true;
        GammaTxnBoolean ref = new GammaTxnBoolean(stm, initialValue);
        long initialVersion = ref.getVersion();

        T tx = newTransaction();
        try {
            ref.openForWrite(tx, LOCKMODE_NONE);
            fail();
        } catch (SpeculativeConfigurationError expected) {
        }

        assertIsAborted(tx);
        assertRefHasNoLocks(ref);
        assertVersionAndValue(ref, initialVersion, initialValue);
        assertSpeculativeConfigurationNonRefTypeRequired(tx);
    }

    @Test
    public void whenTxnDouble_thenSpeculativeConfigurationError() {
        double initialValue = 10;
        GammaTxnDouble ref = new GammaTxnDouble(stm, initialValue);
        long initialVersion = ref.getVersion();

        T tx = newTransaction();
        try {
            ref.openForWrite(tx, LOCKMODE_NONE);
            fail();
        } catch (SpeculativeConfigurationError expected) {
        }

        assertIsAborted(tx);
        assertRefHasNoLocks(ref);
        assertVersionAndValue(ref, initialVersion, initialValue);
        assertSpeculativeConfigurationNonRefTypeRequired(tx);
    }

    @Test
    public void whenLongRef_thenSpeculativeConfigurationError() {
        long initialValue = 10;
        GammaTxnLong ref = new GammaTxnLong(stm, initialValue);
        long initialVersion = ref.getVersion();

        T tx = newTransaction();
        try {
            ref.openForWrite(tx, LOCKMODE_NONE);
            fail();
        } catch (SpeculativeConfigurationError expected) {
        }

        assertIsAborted(tx);
        assertRefHasNoLocks(ref);
        assertVersionAndValue(ref, initialVersion, initialValue);
        assertSpeculativeConfigurationNonRefTypeRequired(tx);
    }

    @Test
    public void whenExclusiveLockObtainedByOthers() {
        String initialValue = "foo";
        GammaTxnRef<String> ref = new GammaTxnRef<String>(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTxn otherTx = stm.newDefaultTxn();
        ref.getLock().acquire(otherTx, LockMode.Exclusive);

        T tx = newTransaction();
        try {
            ref.openForWrite(tx, LOCKMODE_NONE);
            fail();
        } catch (ReadWriteConflict expected) {

        }

        assertIsAborted(tx);
        assertRefHasExclusiveLock(ref, otherTx);
        assertSurplus(ref, 1);
        assertWriteBiased(ref);
        assertReadonlyCount(ref, 0);
        assertVersionAndValue(ref, initialVersion, initialValue);
    }

    @Test
    public void whenNonExclusiveLockAcquiredByOthers() {
        whenNonExclusiveLockAcquiredByOthers(LockMode.Read);
        whenNonExclusiveLockAcquiredByOthers(LockMode.Write);
    }

    public void whenNonExclusiveLockAcquiredByOthers(LockMode lockMode) {
        String initialValue = "foo";
        GammaTxnRef<String> ref = new GammaTxnRef<String>(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTxn otherTx = stm.newDefaultTxn();
        ref.getLock().acquire(otherTx, lockMode);

        T tx = newTransaction();
        Tranlocal tranlocal = ref.openForWrite(tx, LOCKMODE_NONE);

        assertIsActive(tx);
        assertNotNull(tranlocal);
        assertSame(ref, tranlocal.owner);
        assertSame(initialValue, tranlocal.ref_value);
        assertNull(tranlocal.ref_oldValue);
        assertEquals(LOCKMODE_NONE, tranlocal.lockMode);
        assertEquals(TRANLOCAL_WRITE, tranlocal.mode);
        assertFalse(tranlocal.hasDepartObligation);
        assertEquals(initialVersion, tranlocal.version);

        assertRefHasLockMode(ref, otherTx, lockMode.asInt());
        assertSurplus(ref, 1);
        assertWriteBiased(ref);
        assertReadonlyCount(ref, 0);
        assertVersionAndValue(ref, initialVersion, initialValue);
    }

    @Test
    public void whenOverflowing() {
         int maxCapacity = getMaximumLength();
        assumeTrue(maxCapacity < Integer.MAX_VALUE);

        T tx = newTransaction();

        for (int k = 0; k < maxCapacity; k++) {
            GammaTxnRef ref = new GammaTxnRef(stm, 0);
            ref.openForWrite(tx, LOCKMODE_NONE);
        }

        GammaTxnRef ref = new GammaTxnRef(stm, 0);
        try {
            ref.openForWrite(tx, LOCKMODE_NONE);
            fail();
        } catch (SpeculativeConfigurationError expected) {
        }

        assertEquals(TxnStatus.Aborted, tx.getStatus());
        assertEquals(maxCapacity + 1, tx.getConfig().getSpeculativeConfiguration().minimalLength);
    }

    @Test
    public void whenNotOpenedBefore() {
        String initialValue = "foo";
        GammaTxnRef<String> ref = new GammaTxnRef<String>(stm, initialValue);
        long initialVersion = ref.getVersion();

        T tx = newTransaction();
        Tranlocal tranlocal = ref.openForWrite(tx, LOCKMODE_NONE);

        assertNotNull(tranlocal);
        assertSame(ref, tranlocal.owner);
        assertSame(initialValue, tranlocal.ref_value);
        assertNull(tranlocal.ref_oldValue);
        assertEquals(TRANLOCAL_WRITE, tranlocal.getMode());
        assertEquals(LOCKMODE_NONE, tranlocal.getLockMode());
        assertEquals(initialVersion, tranlocal.version);

        assertRefHasNoLocks(ref);
        assertVersionAndValue(ref, initialVersion, initialValue);
    }

    @Test
    public void whenAlreadyOpenedForRead() {
        String initialValue = "foo";
        GammaTxnRef<String> ref = new GammaTxnRef<String>(stm, initialValue);
        long initialVersion = ref.getVersion();

        T tx = newTransaction();
        Tranlocal read = ref.openForRead(tx, LOCKMODE_NONE);
        Tranlocal tranlocal = ref.openForWrite(tx, LOCKMODE_NONE);

        assertNotNull(tranlocal);
        assertSame(read, tranlocal);
        assertSame(ref, tranlocal.owner);
        assertSame(initialValue, tranlocal.ref_value);
        assertNull(tranlocal.ref_oldValue);
        assertEquals(TRANLOCAL_WRITE, tranlocal.getMode());
        assertEquals(LOCKMODE_NONE, tranlocal.getLockMode());
        assertEquals(initialVersion, tranlocal.version);

        assertRefHasNoLocks(ref);
        assertVersionAndValue(ref, initialVersion, initialValue);
    }

    @Test
    public void whenAlreadyOpenedForWrite() {
        String initialValue = "foo";
        GammaTxnRef<String> ref = new GammaTxnRef<String>(stm, initialValue);
        long initialVersion = ref.getVersion();

        T tx = newTransaction();
        Tranlocal first = ref.openForWrite(tx, LOCKMODE_NONE);
        Tranlocal tranlocal = ref.openForWrite(tx, LOCKMODE_NONE);

        assertNotNull(tranlocal);
        assertSame(first, tranlocal);
        assertSame(ref, tranlocal.owner);
        assertSame(initialValue, tranlocal.ref_value);
        assertNull(tranlocal.ref_oldValue);
        assertEquals(TRANLOCAL_WRITE, tranlocal.getMode());
        assertEquals(LOCKMODE_NONE, tranlocal.getLockMode());
        assertEquals(initialVersion, tranlocal.version);

        assertRefHasNoLocks(ref);
        assertVersionAndValue(ref, initialVersion, initialValue);
    }

    @Test
    public void whenAlreadyPreparedAndunused() {
        String initialValue = "foo";
        GammaTxnRef<String> ref = new GammaTxnRef<String>(stm, initialValue);
        long initialVersion = ref.getVersion();

        T tx = newTransaction();
        tx.prepare();

        try {
            ref.openForWrite(tx, LOCKMODE_NONE);
            fail();
        } catch (PreparedTxnException expected) {
        }

        assertIsAborted(tx);
        assertRefHasNoLocks(ref);
        assertIsAborted(tx);
        assertVersionAndValue(ref, initialVersion, initialValue);
    }

    @Test
    public void whenAlreadyCommitted() {
        String initialValue = "foo";
        GammaTxnRef<String> ref = new GammaTxnRef<String>(stm, initialValue);
        long initialVersion = ref.getVersion();

        T tx = newTransaction();
        tx.commit();

        try {
            ref.openForWrite(tx, LOCKMODE_NONE);
            fail();
        } catch (DeadTxnException expected) {
        }

        assertIsCommitted(tx);
        assertRefHasNoLocks(ref);
        assertVersionAndValue(ref, initialVersion, initialValue);
    }

    @Test
    public void whenAlreadyAborted() {
        String initialValue = "foo";
        GammaTxnRef<String> ref = new GammaTxnRef<String>(stm, initialValue);
        long initialVersion = ref.getVersion();

        T tx = newTransaction();
        tx.abort();

        try {
            ref.openForWrite(tx, LOCKMODE_NONE);
            fail();
        } catch (DeadTxnException expected) {
        }

        assertIsAborted(tx);
        assertRefHasNoLocks(ref);
        assertIsAborted(tx);
        assertVersionAndValue(ref, initialVersion, initialValue);
    }
}
