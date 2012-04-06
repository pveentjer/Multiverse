package org.multiverse.stms.gamma.transactions.lean;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.LockMode;
import org.multiverse.api.TxnStatus;
import org.multiverse.api.exceptions.DeadTransactionException;
import org.multiverse.api.exceptions.PreparedTransactionException;
import org.multiverse.api.exceptions.ReadWriteConflict;
import org.multiverse.api.exceptions.SpeculativeConfigurationError;
import org.multiverse.stms.gamma.GammaConstants;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactionalobjects.*;
import org.multiverse.stms.gamma.transactions.GammaTransaction;

import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;
import static org.multiverse.TestUtils.*;
import static org.multiverse.stms.gamma.GammaTestUtils.*;

public abstract class LeanGammaTransaction_openForReadTest<T extends GammaTransaction> implements GammaConstants {

    protected GammaStm stm;

    public abstract T newTransaction();

    public abstract int getMaximumLength();

    @Before
    public void setUp() {
        stm = new GammaStm();
    }

    @Test
    public void whenMultipleWrites() {
        assumeTrue(getMaximumLength() > 1);

        String initialValue1 = "foo1";
        GammaRef<String> ref1 = new GammaRef<String>(stm, initialValue1);
        String initialValue2 = "foo2";
        GammaRef<String> ref2 = new GammaRef<String>(stm, initialValue2);

        GammaTransaction tx = newTransaction();
        GammaRefTranlocal tranlocal1 = ref1.openForRead(tx, LOCKMODE_NONE);
        GammaRefTranlocal tranlocal2 = ref2.openForRead(tx, LOCKMODE_NONE);

        assertIsActive(tx);

        assertRefHasNoLocks(ref1);
        assertSurplus(ref1, 0);
        assertSame(ref1, tranlocal1.owner);
        assertEquals(TRANLOCAL_READ, tranlocal1.mode);
        assertSame(initialValue1, tranlocal1.ref_value);

        assertRefHasNoLocks(ref2);
        assertSurplus(ref2, 0);
        assertSame(ref2, tranlocal2.owner);
        assertEquals(TRANLOCAL_READ, tranlocal2.mode);
        assertSame(initialValue2, tranlocal2.ref_value);
    }

    @Test
    public void whenExplicitLocking_thenSpeculativeConfigurationFailure() {
        whenExplicitLocking(LockMode.Read);
        whenExplicitLocking(LockMode.Write);
        whenExplicitLocking(LockMode.Exclusive);
    }

    public void whenExplicitLocking(LockMode lockMode) {
        String initialValue = "foo";
        GammaRef<String> ref = new GammaRef<String>(stm, initialValue);
        long initialVersion = ref.getVersion();

        T tx = newTransaction();
        try {
            ref.openForRead(tx, lockMode.asInt());
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
        GammaIntRef ref = new GammaIntRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        T tx = newTransaction();
        try {
            ref.openForRead(tx, LOCKMODE_NONE);
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
        GammaBooleanRef ref = new GammaBooleanRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        T tx = newTransaction();
        try {
            ref.openForRead(tx, LOCKMODE_NONE);
            fail();
        } catch (SpeculativeConfigurationError expected) {
        }

        assertIsAborted(tx);
        assertRefHasNoLocks(ref);
        assertVersionAndValue(ref, initialVersion, initialValue);
        assertSpeculativeConfigurationNonRefTypeRequired(tx);
    }

    @Test
    public void whenDoubleRef_thenSpeculativeConfigurationError() {
        double initialValue = 10;
        GammaDoubleRef ref = new GammaDoubleRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        T tx = newTransaction();
        try {
            ref.openForRead(tx, LOCKMODE_NONE);
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
        GammaLongRef ref = new GammaLongRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        T tx = newTransaction();
        try {
            ref.openForRead(tx, LOCKMODE_NONE);
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
        GammaRef<String> ref = new GammaRef<String>(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTransaction otherTx = stm.newDefaultTransaction();
        ref.getLock().acquire(otherTx, LockMode.Exclusive);

        T tx = newTransaction();
        try {
            ref.openForRead(tx, LOCKMODE_NONE);
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
        GammaRef<String> ref = new GammaRef<String>(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTransaction otherTx = stm.newDefaultTransaction();
        ref.getLock().acquire(otherTx, lockMode);

        T tx = newTransaction();
        GammaRefTranlocal tranlocal = ref.openForRead(tx, LOCKMODE_NONE);

        assertIsActive(tx);
        assertNotNull(tranlocal);
        assertSame(ref, tranlocal.owner);
        assertSame(initialValue, tranlocal.ref_value);
        assertNull(tranlocal.ref_oldValue);
        assertEquals(LOCKMODE_NONE, tranlocal.lockMode);
        assertEquals(TRANLOCAL_READ, tranlocal.mode);
        assertFalse(tranlocal.hasDepartObligation);
        assertEquals(initialVersion, tranlocal.version);

        assertRefHasLockMode(ref, otherTx, lockMode.asInt());
        assertSurplus(ref, 1);
        assertWriteBiased(ref);
        assertReadonlyCount(ref, 0);
        assertVersionAndValue(ref, initialVersion, initialValue);
    }

    @Test
    public void whenNotOpenedBefore() {
        String initialValue = "foo";
        GammaRef<String> ref = new GammaRef<String>(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTransaction tx = newTransaction();
        GammaRefTranlocal tranlocal = ref.openForRead(tx, LOCKMODE_NONE);

        assertNotNull(tranlocal);
        assertSame(ref, tranlocal.owner);
        assertEquals(LOCKMODE_NONE, tranlocal.lockMode);
        assertEquals(TRANLOCAL_READ, tranlocal.mode);
        assertSame(initialValue, tranlocal.ref_value);
        assertNull(tranlocal.ref_oldValue);
        assertEquals(initialVersion, tranlocal.version);

        assertRefHasNoLocks(ref);
        assertVersionAndValue(ref, initialVersion, initialValue);
    }

    @Test
    public void whenAlreadyOpenedForRead() {
        String initialValue = "foo";
        GammaRef<String> ref = new GammaRef<String>(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTransaction tx = newTransaction();
        GammaRefTranlocal first = ref.openForRead(tx, LOCKMODE_NONE);
        GammaRefTranlocal tranlocal = ref.openForRead(tx, LOCKMODE_NONE);

        assertSame(first, tranlocal);
        assertNotNull(tranlocal);
        assertSame(ref, tranlocal.owner);
        assertEquals(LOCKMODE_NONE, tranlocal.lockMode);
        assertEquals(TRANLOCAL_READ, tranlocal.mode);
        assertSame(initialValue, tranlocal.ref_value);
        assertNull(tranlocal.ref_oldValue);
        assertEquals(initialVersion, tranlocal.version);

        assertRefHasNoLocks(ref);
        assertVersionAndValue(ref, initialVersion, initialValue);
    }

    @Test
    public void whenMultipleReadsAndConsistent() {
        assumeTrue(getMaximumLength() > 1);

        GammaRef<String> ref1 = new GammaRef<String>(stm);
        GammaRef<String> ref2 = new GammaRef<String>(stm);

        GammaTransaction tx = newTransaction();
        ref1.openForRead(tx, LOCKMODE_NONE);
        ref2.openForRead(tx, LOCKMODE_NONE);
        tx.commit();

        assertIsCommitted(tx);
        assertRefHasNoLocks(ref1);
        assertSurplus(ref1, 0);
        assertRefHasNoLocks(ref2);
        assertSurplus(ref2, 0);
    }

    @Test
    public void whenMultipleReadsAndInconsistent() {
        assumeTrue(getMaximumLength() > 1);

        GammaRef<String> ref1 = new GammaRef<String>(stm);
        GammaRef<String> ref2 = new GammaRef<String>(stm);

        GammaTransaction tx = newTransaction();
        ref1.openForRead(tx, LOCKMODE_NONE);

        ref1.atomicSet("foo");

        try {
            ref2.openForRead(tx, LOCKMODE_NONE);
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

        GammaRef<String> ref1 = new GammaRef<String>(stm);
        GammaRef<String> ref2 = new GammaRef<String>(stm);

        GammaTransaction tx = newTransaction();
        ref1.openForRead(tx, LOCKMODE_NONE);

        GammaTransaction otherTx = stm.newDefaultTransaction();
        ref1.getLock().acquire(otherTx, LockMode.Exclusive);

        try {
            ref2.openForRead(tx, LOCKMODE_NONE);
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
    public void whenAlreadyOpenedForWrite() {
        String initialValue = "foo";
        GammaRef<String> ref = new GammaRef<String>(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTransaction tx = newTransaction();
        GammaRefTranlocal first = ref.openForWrite(tx, LOCKMODE_NONE);
        GammaRefTranlocal tranlocal = ref.openForRead(tx, LOCKMODE_NONE);

        assertSame(first, tranlocal);
        assertNotNull(tranlocal);
        assertSame(ref, tranlocal.owner);
        assertEquals(LOCKMODE_NONE, tranlocal.lockMode);
        assertEquals(TRANLOCAL_WRITE, tranlocal.mode);
        assertSame(initialValue, tranlocal.ref_value);
        assertNull(tranlocal.ref_oldValue);
        assertEquals(initialVersion, tranlocal.version);

        assertRefHasNoLocks(ref);
        assertVersionAndValue(ref, initialVersion, initialValue);
    }

    @Test
    public void whenAlreadyPreparedAndunused() {
        String initialValue = "foo";
        GammaRef<String> ref = new GammaRef<String>(stm, initialValue);
        long initialVersion = ref.getVersion();

        T tx = newTransaction();
        tx.prepare();

        try {
            ref.openForRead(tx, LOCKMODE_NONE);
            fail();
        } catch (PreparedTransactionException expected) {
        }

        assertIsAborted(tx);
        assertRefHasNoLocks(ref);
        assertIsAborted(tx);
        assertVersionAndValue(ref, initialVersion, initialValue);
    }

    @Test
    public void whenAlreadyCommitted() {
        String initialValue = "foo";
        GammaRef<String> ref = new GammaRef<String>(stm, initialValue);
        long initialVersion = ref.getVersion();

        T tx = newTransaction();
        tx.commit();

        try {
            ref.openForRead(tx, LOCKMODE_NONE);
            fail();
        } catch (DeadTransactionException expected) {
        }

        assertIsCommitted(tx);
        assertRefHasNoLocks(ref);
        assertVersionAndValue(ref, initialVersion, initialValue);
    }

    @Test
    public void whenAlreadyAborted() {
        String initialValue = "foo";
        GammaRef<String> ref = new GammaRef<String>(stm, initialValue);
        long initialVersion = ref.getVersion();

        T tx = newTransaction();
        tx.abort();

        try {
            ref.openForRead(tx, LOCKMODE_NONE);
            fail();
        } catch (DeadTransactionException expected) {
        }

        assertIsAborted(tx);
        assertRefHasNoLocks(ref);
        assertIsAborted(tx);
        assertVersionAndValue(ref, initialVersion, initialValue);
    }

    @Test
    public void whenOverflowing() {
        int maxCapacity = getMaximumLength();
        assumeTrue(maxCapacity < Integer.MAX_VALUE);

        GammaTransaction tx = newTransaction();
        for (int k = 0; k < maxCapacity; k++) {
            GammaRef ref = new GammaRef(stm, 0);
            ref.openForRead(tx, LOCKMODE_NONE);
        }

        GammaRef ref = new GammaRef(stm, 0);
        try {
            ref.openForRead(tx, LOCKMODE_NONE);
            fail();
        } catch (SpeculativeConfigurationError expected) {
        }

        assertEquals(TxnStatus.Aborted, tx.getStatus());
        assertEquals(maxCapacity + 1, tx.getConfiguration().getSpeculativeConfiguration().minimalLength);
    }
}
