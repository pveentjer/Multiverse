package org.multiverse.stms.gamma.transactions.fat;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.multiverse.SomeUncheckedException;
import org.multiverse.api.LockMode;
import org.multiverse.api.exceptions.AbortOnlyException;
import org.multiverse.api.exceptions.DeadTxnException;
import org.multiverse.api.exceptions.ReadWriteConflict;
import org.multiverse.api.functions.Functions;
import org.multiverse.api.functions.LongFunction;
import org.multiverse.api.lifecycle.TxnEvent;
import org.multiverse.api.lifecycle.TxnListener;
import org.multiverse.stms.gamma.GammaConstants;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactionalobjects.GammaTxnLong;
import org.multiverse.stms.gamma.transactionalobjects.Tranlocal;
import org.multiverse.stms.gamma.transactions.GammaTxn;
import org.multiverse.stms.gamma.transactions.GammaTxnConfig;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.*;
import static org.multiverse.TestUtils.*;
import static org.multiverse.stms.gamma.GammaTestUtils.*;

public abstract class FatGammaTxn_prepareTest<T extends GammaTxn> implements GammaConstants {

    public GammaStm stm;

    @Before
    public void setUp() {
        stm = new GammaStm();
    }

    protected abstract T newTransaction();

    protected abstract T newTransaction(GammaTxnConfig config);

    @Test
    public void listener_whenNormalListenerAvailable() {
        T tx = newTransaction();
        TxnListener listener = mock(TxnListener.class);
        tx.register(listener);

        tx.prepare();

        assertIsPrepared(tx);
        //verify(listener).notify(tx, TxnEvent.PrePrepare);
        verify(listener).notify(tx, TxnEvent.PrePrepare);
    }

    @Test
    public void listener_whenPermanentListenerAvailable() {
        TxnListener listener = mock(TxnListener.class);

        GammaTxnConfig config = new GammaTxnConfig(stm)
                .addPermanentListener(listener);

        T tx = newTransaction(config);

        tx.prepare();

        assertIsPrepared(tx);
        //verify(listener).notify(tx, TxnEvent.PrePrepare);
        verify(listener).notify(tx, TxnEvent.PrePrepare);
    }

    @Test
    public void conflict_whenArriveByOther() {
        long initialValue = 10;
        GammaTxnLong ref = new GammaTxnLong(stm, initialValue);
        long initialVersion = ref.getVersion();

        T tx = newTransaction();
        long newValue = 1;
        ref.set(tx, newValue);

        GammaTxnConfig config = new GammaTxnConfig(stm)
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
    public void whenAbortOnly() {
        long globalConflictCount = stm.globalConflictCounter.count();

        T tx = newTransaction();
        tx.setAbortOnly();

        try {
            tx.prepare();
            fail();
        } catch (AbortOnlyException expected) {
        }

        assertIsAborted(tx);
        assertGlobalConflictCount(stm, globalConflictCount);
    }

    @Test
    public void whenContainsRead() {
        whenContainsRead(LockMode.None);
        whenContainsRead(LockMode.Read);
        whenContainsRead(LockMode.Write);
        whenContainsRead(LockMode.Exclusive);
    }

    public void whenContainsRead(LockMode readLockMode) {
        long globalConflictCount = stm.globalConflictCounter.count();

        long initialValue = 10;
        GammaTxnLong ref = new GammaTxnLong(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTxn tx = stm.newDefaultTxn();
        ref.openForRead(tx, readLockMode.asInt());
        tx.prepare();

        assertIsPrepared(tx);
        assertFalse(tx.commitConflict);
        assertLockMode(ref, readLockMode);
        assertVersionAndValue(ref, initialVersion, initialValue);
        assertGlobalConflictCount(stm, globalConflictCount);
    }

    @Test
    @Ignore
    public void writeSkew() {

    }

    @Test
    public void whenContainsCommute() {
        int initialValue = 10;
        GammaTxnLong ref = new GammaTxnLong(stm, initialValue);
        long initialVersion = ref.getVersion();

        long globalConflictCount = stm.globalConflictCounter.count();
        LongFunction function = Functions.incLongFunction();
        GammaTxn tx = newTransaction();
        ref.commute(tx, function);
        Tranlocal tranlocal = tx.locate(ref);

        tx.prepare();

        assertIsPrepared(tx);
        assertRefHasExclusiveLock(ref, tx);
        assertTrue(tranlocal.isDirty);
        assertEquals(LOCKMODE_EXCLUSIVE, tranlocal.lockMode);
        assertEquals(initialValue + 1, tranlocal.long_value);
        assertTrue(tranlocal.hasDepartObligation);
        assertGlobalConflictCount(stm, globalConflictCount);
        assertVersionAndValue(ref, initialVersion, initialValue);
    }

    @Test
    public void whenContainsMultipleCommutes() {
        int initialValue = 10;
        GammaTxnLong ref = new GammaTxnLong(stm, initialValue);
        long initialVersion = ref.getVersion();

        long globalConflictCount = stm.globalConflictCounter.count();
        LongFunction function1 = Functions.incLongFunction();
        LongFunction function2 = Functions.incLongFunction();
        LongFunction function3 = Functions.incLongFunction();
        GammaTxn tx = newTransaction();
        ref.commute(tx, function1);
        ref.commute(tx, function2);
        ref.commute(tx, function3);
        Tranlocal tranlocal = tx.locate(ref);

        tx.prepare();

        assertIsPrepared(tx);
        assertRefHasExclusiveLock(ref, tx);
        assertTrue(tranlocal.isDirty);
        assertEquals(LOCKMODE_EXCLUSIVE, tranlocal.lockMode);
        assertEquals(initialValue + 3, tranlocal.long_value);
        assertTrue(tranlocal.hasDepartObligation);
        assertGlobalConflictCount(stm, globalConflictCount);
        assertVersionAndValue(ref, initialVersion, initialValue);
    }

    @Test
    public void whenContainsCommuteThatCausesProblems() {
        int initialValue = 10;
        GammaTxnLong ref = new GammaTxnLong(stm, initialValue);
        long initialVersion = ref.getVersion();

        long globalConflictCount = stm.globalConflictCounter.count();
        LongFunction function = mock(LongFunction.class);
        when(function.call(anyLong())).thenThrow(new SomeUncheckedException());
        GammaTxn tx = newTransaction();
        ref.commute(tx, function);
        Tranlocal tranlocal = tx.locate(ref);

        try {
            tx.prepare();
            fail();
        } catch (SomeUncheckedException expected) {

        }

        assertIsAborted(tx);
        assertRefHasNoLocks(ref);
        assertEquals(LOCKMODE_NONE, tranlocal.lockMode);
        assertGlobalConflictCount(stm, globalConflictCount);
        assertVersionAndValue(ref, initialVersion, initialValue);
    }

    @Test
    public void whenContainsCommuteThatIsLocked() {
        whenContainsCommuteThatIsLocked(LockMode.Read);
        whenContainsCommuteThatIsLocked(LockMode.Write);
        whenContainsCommuteThatIsLocked(LockMode.Exclusive);
    }

    public void whenContainsCommuteThatIsLocked(LockMode lockMode) {
        int initialValue = 10;
        GammaTxnLong ref = new GammaTxnLong(stm, initialValue);
        long initialVersion = ref.getVersion();

        long globalConflictCount = stm.globalConflictCounter.count();
        LongFunction function = mock(LongFunction.class);
        when(function.call(anyLong())).thenThrow(new SomeUncheckedException());
        GammaTxn tx = newTransaction();
        ref.commute(tx, function);
        Tranlocal tranlocal = tx.locate(ref);

        GammaTxn otherTx = newTransaction();
        ref.getLock().acquire(otherTx, lockMode);

        try {
            tx.prepare();
            fail();
        } catch (ReadWriteConflict expected) {

        }

        assertIsAborted(tx);
        assertRefHasLockMode(ref, otherTx, lockMode.asInt());
        assertEquals(LOCKMODE_NONE, tranlocal.lockMode);
        assertGlobalConflictCount(stm, globalConflictCount);
        assertVersionAndValue(ref, initialVersion, initialValue);
    }


    @Test
    public void whenContainsConstructed() {
        long globalConflictCount = stm.globalConflictCounter.count();

        GammaTxn tx = newTransaction();
        int initialValue = 10;
        GammaTxnLong ref = new GammaTxnLong(tx, initialValue);
        Tranlocal tranlocal = tx.locate(ref);

        tx.prepare();

        assertIsPrepared(tx);
        assertRefHasExclusiveLock(ref, tx);
        assertTrue(tranlocal.isDirty);
        assertEquals(LOCKMODE_EXCLUSIVE, tranlocal.lockMode);
        assertTrue(tranlocal.hasDepartObligation);
        assertGlobalConflictCount(stm, globalConflictCount);
        assertVersionAndValue(ref, GammaConstants.VERSION_UNCOMMITTED, 0);
    }

    // =============================== dirty check =================================

    @Test
    public void dirtyCheckDisabled_whenNotDirty_thenLockedForCommit() {
        dirtyCheckDisabled_whenNotDirty_thenLockedForCommit(LockMode.None);
        dirtyCheckDisabled_whenNotDirty_thenLockedForCommit(LockMode.Read);
        dirtyCheckDisabled_whenNotDirty_thenLockedForCommit(LockMode.Write);
        dirtyCheckDisabled_whenNotDirty_thenLockedForCommit(LockMode.None);
    }

    public void dirtyCheckDisabled_whenNotDirty_thenLockedForCommit(LockMode writeLockMode) {
        long globalConflictCount = stm.globalConflictCounter.count();

        long initialValue = 10;
        GammaTxnLong ref = new GammaTxnLong(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTxnConfig config = new GammaTxnConfig(stm)
                .setDirtyCheckEnabled(false);

        GammaTxn tx = newTransaction(config);
        Tranlocal tranlocal = ref.openForWrite(tx, writeLockMode.asInt());
        tx.prepare();

        assertIsPrepared(tx);
        assertFalse(tx.commitConflict);
        assertTrue(tranlocal.isDirty());
        assertEquals(LockMode.Exclusive.asInt(), tranlocal.getLockMode());
        assertLockMode(ref, LockMode.Exclusive);
        assertVersionAndValue(ref, initialVersion, initialValue);
        assertGlobalConflictCount(stm, globalConflictCount);
    }

    @Test
    public void dirtyCheckDisabled_whenDirty_thenLockedForCommit() {
        dirtyCheckDisabled_whenDirty_thenLockedForCommit(LockMode.None);
        dirtyCheckDisabled_whenDirty_thenLockedForCommit(LockMode.Read);
        dirtyCheckDisabled_whenDirty_thenLockedForCommit(LockMode.Write);
        dirtyCheckDisabled_whenDirty_thenLockedForCommit(LockMode.None);
    }

    public void dirtyCheckDisabled_whenDirty_thenLockedForCommit(LockMode writeLockMode) {
        long globalConflictCount = stm.globalConflictCounter.count();

        long initialValue = 10;
        GammaTxnLong ref = new GammaTxnLong(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTxnConfig config = new GammaTxnConfig(stm)
                .setDirtyCheckEnabled(false);

        GammaTxn tx = newTransaction(config);
        Tranlocal tranlocal = ref.openForWrite(tx, writeLockMode.asInt());
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

    @Test
    public void dirtyCheckEnabled_whenNotDirty_thenNothingHappens() {
        dirtyCheckDisabled_whenNotDirty_thenLockedForCommit(LockMode.None);
        dirtyCheckDisabled_whenNotDirty_thenLockedForCommit(LockMode.Read);
        dirtyCheckDisabled_whenNotDirty_thenLockedForCommit(LockMode.Write);
        dirtyCheckDisabled_whenNotDirty_thenLockedForCommit(LockMode.Exclusive);
    }

    public void dirtyCheckEnabled_whenNotDirty_nothingHappens(LockMode writeLockMode) {
        long globalConflictCount = stm.globalConflictCounter.count();

        long initialValue = 10;
        GammaTxnLong ref = new GammaTxnLong(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTxnConfig config = new GammaTxnConfig(stm)
                .setDirtyCheckEnabled(true);

        GammaTxn tx = newTransaction(config);
        Tranlocal tranlocal = ref.openForWrite(tx, writeLockMode.asInt());
        tx.prepare();

        assertIsPrepared(tx);
        assertFalse(tx.commitConflict);
        assertFalse(tranlocal.isDirty());
        assertEquals(writeLockMode.asInt(), tranlocal.getLockMode());
        assertLockMode(ref, writeLockMode);
        assertVersionAndValue(ref, initialVersion, initialValue);
        assertGlobalConflictCount(stm, globalConflictCount);
    }

    @Test
    public void dirtyCheckEnabled_whenDirty_thenLockedForCommit() {
        dirtyCheckEnabled_whenDirty_thenLockedForCommit(LockMode.None);
        dirtyCheckEnabled_whenDirty_thenLockedForCommit(LockMode.Read);
        dirtyCheckEnabled_whenDirty_thenLockedForCommit(LockMode.Write);
        dirtyCheckEnabled_whenDirty_thenLockedForCommit(LockMode.Exclusive);
    }

    public void dirtyCheckEnabled_whenDirty_thenLockedForCommit(LockMode writeLockMode) {
        long globalConflictCount = stm.globalConflictCounter.count();

        long initialValue = 10;
        GammaTxnLong ref = new GammaTxnLong(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTxnConfig config = new GammaTxnConfig(stm)
                .setDirtyCheckEnabled(true);

        GammaTxn tx = newTransaction(config);
        Tranlocal tranlocal = ref.openForWrite(tx, writeLockMode.asInt());
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
        long globalConflictCount = stm.globalConflictCounter.count();

        long initialValue = 10;
        GammaTxnLong ref = new GammaTxnLong(stm, initialValue);
        long initialVersion = ref.getVersion();

        T tx = newTransaction();
        Tranlocal tranlocal = ref.openForWrite(tx, LOCKMODE_NONE);
        tranlocal.long_value++;

        T otherTx = newTransaction();
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
        long globalConflictCount = stm.globalConflictCounter.count();

        long initialValue = 10;
        GammaTxnLong ref = new GammaTxnLong(stm, initialValue);
        long initialVersion = ref.getVersion();

        T tx = newTransaction();
        Tranlocal tranlocal = ref.openForWrite(tx, LOCKMODE_NONE);
        tranlocal.long_value++;

        T otherTx = newTransaction();
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
        long globalConflictCount = stm.globalConflictCounter.count();

        long initialValue = 10;
        GammaTxnLong ref = new GammaTxnLong(stm, initialValue);
        long initialVersion = ref.getVersion();

        T tx = newTransaction();
        Tranlocal tranlocal = ref.openForWrite(tx, LOCKMODE_NONE);
        tranlocal.long_value++;

        T otherTx = newTransaction();
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
        long globalConflictCount = stm.globalConflictCounter.count();

        T tx = newTransaction();
        tx.prepare();

        tx.prepare();

        assertIsPrepared(tx);
        assertFalse(tx.commitConflict);
        assertGlobalConflictCount(stm, globalConflictCount);
    }

    @Test
    public void whenAlreadyAborted_thenDeadTxnException() {
        long globalConflictCount = stm.globalConflictCounter.count();

        T tx = newTransaction();
        tx.abort();

        try {
            tx.prepare();
            fail();
        } catch (DeadTxnException expected) {
        }

        assertIsAborted(tx);
        assertGlobalConflictCount(stm, globalConflictCount);
    }

    @Test
    public void whenAlreadyCommitted_thenDeadTxnException() {
        long globalConflictCount = stm.globalConflictCounter.count();

        T tx = newTransaction();
        tx.commit();

        try {
            tx.prepare();
            fail();
        } catch (DeadTxnException expected) {
        }

        assertIsCommitted(tx);
        assertGlobalConflictCount(stm, globalConflictCount);
    }
}
