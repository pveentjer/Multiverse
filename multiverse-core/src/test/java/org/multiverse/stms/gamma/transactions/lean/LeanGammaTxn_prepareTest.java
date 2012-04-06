package org.multiverse.stms.gamma.transactions.lean;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.LockMode;
import org.multiverse.api.exceptions.DeadTransactionException;
import org.multiverse.api.exceptions.ReadWriteConflict;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactionalobjects.GammaRef;
import org.multiverse.stms.gamma.transactionalobjects.GammaRefTranlocal;
import org.multiverse.stms.gamma.transactions.GammaTxn;
import org.multiverse.stms.gamma.transactions.GammaTxnConfiguration;
import org.multiverse.stms.gamma.transactions.fat.FatMonoGammaTxn;
import org.multiverse.stms.gamma.transactions.fat.FatVariableLengthGammaTxn;

import static org.junit.Assert.*;
import static org.multiverse.TestUtils.*;
import static org.multiverse.stms.gamma.GammaTestUtils.LOCKMODE_EXCLUSIVE;
import static org.multiverse.stms.gamma.GammaTestUtils.LOCKMODE_NONE;
import static org.multiverse.stms.gamma.GammaTestUtils.LOCKMODE_READ;
import static org.multiverse.stms.gamma.GammaTestUtils.LOCKMODE_WRITE;
import static org.multiverse.stms.gamma.GammaTestUtils.*;

public abstract class LeanGammaTxn_prepareTest<T extends GammaTxn> {

    protected GammaStm stm;

    @Before
    public void setUp() {
        stm = new GammaStm();
    }

    public abstract T newTransaction();

    @Test
    public void conflict_whenArriveByOther() {
        String initialValue = "foo";
        GammaRef<String> ref = new GammaRef<String>(stm, initialValue);
        long initialVersion = ref.getVersion();

        T tx = newTransaction();
        String newValue = "bar";
        ref.set(tx, newValue);

        GammaTxnConfiguration config = new GammaTxnConfiguration(stm)
                .setMaximumPoorMansConflictScanLength(0);

        FatVariableLengthGammaTxn otherTx = new FatVariableLengthGammaTxn(config);
        ref.get(otherTx);

        long globalConflictCount = stm.globalConflictCounter.count();
        tx.prepare();

        assertIsPrepared(tx);
        assertTrue(tx.commitConflict);
        assertGlobalConflictCount(stm, globalConflictCount);
        assertVersionAndValue(ref, initialVersion, initialValue);
        assertLockMode(ref, LOCKMODE_EXCLUSIVE);
        assertSurplus(ref, 2);
    }

    @Test
    public void whenContainsRead() {
        long globalConflictCount = stm.getGlobalConflictCounter().count();

        String initialValue = "foo";
        GammaRef<String> ref = new GammaRef<String>(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTxn tx = stm.newDefaultTransaction();
        ref.openForRead(tx, LOCKMODE_NONE);
        tx.prepare();

        assertIsPrepared(tx);
        assertFalse(tx.commitConflict);
        assertLockMode(ref, LOCKMODE_NONE);
        assertVersionAndValue(ref, initialVersion, initialValue);
        assertGlobalConflictCount(stm, globalConflictCount);
    }

     public void whenNonDirtyDirty_thenLockedForCommit() {
        long globalConflictCount = stm.getGlobalConflictCounter().count();

        Long initialValue = 10L;
        GammaRef<Long> ref = new GammaRef<Long>(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTxn tx = newTransaction();
        GammaRefTranlocal tranlocal = ref.openForWrite(tx, LOCKMODE_NONE);
        tx.prepare();

         assertIsPrepared(tx);
        assertTrue(tranlocal.isDirty());
         assertFalse(tx.commitConflict);
        assertEquals(LockMode.Exclusive.asInt(), tranlocal.getLockMode());
        assertLockMode(ref, LockMode.Exclusive);
        assertVersionAndValue(ref, initialVersion, initialValue);
         assertGlobalConflictCount(stm, globalConflictCount);
    }

    public void whenDirtyDirty_thenLockedForCommit() {
        long globalConflictCount = stm.getGlobalConflictCounter().count();

        Long initialValue = 10L;
        GammaRef<Long> ref = new GammaRef<Long>(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTxn tx = newTransaction();
        GammaRefTranlocal tranlocal = ref.openForWrite(tx, LOCKMODE_NONE);
        tranlocal.long_value++;
        tx.prepare();

        assertIsPrepared(tx);
        assertFalse(tx.commitConflict);
        assertTrue(tranlocal.isDirty());
        assertEquals(LockMode.Exclusive.asInt(), tranlocal.getLockMode());
        assertLockMode(ref, LockMode.Exclusive);
        assertVersionAndValue(ref, initialVersion, initialValue);
        assertGlobalConflictCount(stm, globalConflictCount);
    }

    // =============================== locked by other =============================

    @Test
    public void conflict_dirty_whenReadLockedByOther() {
        long globalConflictCount = stm.getGlobalConflictCounter().count();

        Long initialValue = 10L;
        GammaRef<Long> ref = new GammaRef<Long>(stm, initialValue);
        long initialVersion = ref.getVersion();

        T tx = newTransaction();
        GammaRefTranlocal tranlocal = ref.openForWrite(tx, LOCKMODE_NONE);
        tranlocal.long_value++;

        FatMonoGammaTxn otherTx = new FatMonoGammaTxn(stm);
        ref.openForRead(otherTx, LOCKMODE_READ);

        try {
            tx.prepare();
            fail();
        } catch (ReadWriteConflict expected) {
        }

        assertIsAborted(tx);
        assertVersionAndValue(ref, initialVersion, initialValue);
        assertRefHasReadLock(ref, otherTx);
        assertReadLockCount(ref, 1);
        assertGlobalConflictCount(stm, globalConflictCount);
    }

    @Test
    public void conflict_dirty_whenWriteLockedByOther() {
        long globalConflictCount = stm.getGlobalConflictCounter().count();

        Long initialValue = 10L;
        GammaRef<Long> ref = new GammaRef<Long>(stm, initialValue);
        long initialVersion = ref.getVersion();

        T tx = newTransaction();
        GammaRefTranlocal tranlocal = ref.openForWrite(tx, LOCKMODE_NONE);
        tranlocal.long_value++;

        FatMonoGammaTxn otherTx = new FatMonoGammaTxn(stm);
        ref.openForRead(otherTx, LOCKMODE_WRITE);

        try {
            tx.prepare();
            fail();
        } catch (ReadWriteConflict expected) {
        }

        assertIsAborted(tx);
        assertVersionAndValue(ref, initialVersion, initialValue);
        assertRefHasWriteLock(ref, otherTx);
        assertGlobalConflictCount(stm, globalConflictCount);
    }

    @Test
    public void conflict_dirty_whenExclusiveLockedByOther() {
        long globalConflictCount = stm.getGlobalConflictCounter().count();

        Long initialValue = 10L;
        GammaRef<Long> ref = new GammaRef<Long>(stm, initialValue);
        long initialVersion = ref.getVersion();

        T tx = newTransaction();
        GammaRefTranlocal tranlocal = ref.openForWrite(tx, LOCKMODE_NONE);
        tranlocal.long_value++;

        FatMonoGammaTxn otherTx = new FatMonoGammaTxn(stm);
        ref.openForRead(otherTx, LOCKMODE_EXCLUSIVE);

        try {
            tx.prepare();
            fail();
        } catch (ReadWriteConflict expected) {
        }

        assertIsAborted(tx);
        assertVersionAndValue(ref, initialVersion, initialValue);
        assertRefHasExclusiveLock(ref, otherTx);
        assertGlobalConflictCount(stm, globalConflictCount);
    }

    // ================================ states =====================================

    @Test
    public void whenPreparedAndUnused() {
        long globalConflictCount = stm.getGlobalConflictCounter().count();

        T tx = newTransaction();
        tx.prepare();

        tx.prepare();

        assertIsPrepared(tx);
        assertFalse(tx.commitConflict);
        assertGlobalConflictCount(stm, globalConflictCount);
    }

    @Test
    public void whenAlreadyAborted_thenDeadTransactionException() {
        long globalConflictCount = stm.getGlobalConflictCounter().count();

        T tx = newTransaction();
        tx.abort();

        try {
            tx.prepare();
            fail();
        } catch (DeadTransactionException expected) {
        }

        assertIsAborted(tx);
        assertGlobalConflictCount(stm, globalConflictCount);
    }

    @Test
    public void whenAlreadyCommitted_thenDeadTransactionException() {
        long globalConflictCount = stm.getGlobalConflictCounter().count();

        T tx = newTransaction();
        tx.commit();

        try {
            tx.prepare();
            fail();
        } catch (DeadTransactionException expected) {
        }

        assertIsCommitted(tx);
        assertGlobalConflictCount(stm, globalConflictCount);
    }
}
