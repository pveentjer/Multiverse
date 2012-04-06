package org.multiverse.stms.gamma.transactions.fat;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.SomeError;
import org.multiverse.SomeUncheckedException;
import org.multiverse.api.LockMode;
import org.multiverse.api.TxnStatus;
import org.multiverse.api.exceptions.AbortOnlyException;
import org.multiverse.api.exceptions.DeadTxnException;
import org.multiverse.api.exceptions.ReadWriteConflict;
import org.multiverse.api.exceptions.RetryError;
import org.multiverse.api.functions.Functions;
import org.multiverse.api.functions.LongFunction;
import org.multiverse.api.lifecycle.TxnEvent;
import org.multiverse.api.lifecycle.TxnListener;
import org.multiverse.stms.gamma.GammaConstants;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactionalobjects.*;
import org.multiverse.stms.gamma.transactions.GammaTxn;
import org.multiverse.stms.gamma.transactions.GammaTxnConfig;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.multiverse.TestUtils.*;
import static org.multiverse.stms.gamma.GammaTestUtils.*;

public abstract class FatGammaTxn_commitTest<T extends GammaTxn> implements GammaConstants {

    protected GammaStm stm;

    @Before
    public void setUp() {
        stm = new GammaStm();
    }

    protected abstract T newTransaction();

    protected abstract T newTransaction(GammaTxnConfig config);

    protected abstract void assertCleaned(T transaction);

    @Test
    public void listener_whenNormalListenerAvailable() {
        T tx = newTransaction();
        TxnListener listener = mock(TxnListener.class);
        tx.register(listener);

        tx.commit();

        assertIsCommitted(tx);
        verify(listener).notify(tx, TxnEvent.PrePrepare);
        verify(listener).notify(tx, TxnEvent.PostCommit);
    }

    @Test
    public void listener_whenPermanentListenerAvailable() {
        TxnListener listener = mock(TxnListener.class);

        GammaTxnConfig config = new GammaTxnConfig(stm)
                .addPermanentListener(listener);

        T tx = newTransaction(config);

        tx.commit();

        assertIsCommitted(tx);
        verify(listener).notify(tx, TxnEvent.PrePrepare);
        verify(listener).notify(tx, TxnEvent.PostCommit);
    }


    @Test
    public void retryListeners_whenDirtyWrite_thenListenersNotified() {
        retryListeners_whenDirtyWrite_thenListenersNotified(LockMode.None, LockMode.None);
        retryListeners_whenDirtyWrite_thenListenersNotified(LockMode.None, LockMode.Read);
        retryListeners_whenDirtyWrite_thenListenersNotified(LockMode.None, LockMode.Write);
        retryListeners_whenDirtyWrite_thenListenersNotified(LockMode.None, LockMode.Exclusive);

        retryListeners_whenDirtyWrite_thenListenersNotified(LockMode.Read, LockMode.Read);
        retryListeners_whenDirtyWrite_thenListenersNotified(LockMode.Read, LockMode.Write);
        retryListeners_whenDirtyWrite_thenListenersNotified(LockMode.Read, LockMode.Exclusive);

        retryListeners_whenDirtyWrite_thenListenersNotified(LockMode.Write, LockMode.Write);
        retryListeners_whenDirtyWrite_thenListenersNotified(LockMode.Write, LockMode.Exclusive);

        retryListeners_whenDirtyWrite_thenListenersNotified(LockMode.Exclusive, LockMode.Exclusive);
    }

    public void retryListeners_whenDirtyWrite_thenListenersNotified(LockMode readLockMode, LockMode writeLockMode) {
        String oldValue = "oldvalue";
        GammaTxnRef<String> ref = new GammaTxnRef<String>(stm, oldValue);
        long initialVersion = ref.getVersion();

        GammaTxn waitingTx = newTransaction();
        ref.get(waitingTx);
        try {
            waitingTx.retry();
            fail();
        } catch (RetryError expected) {
        }

        GammaTxnConfig config = new GammaTxnConfig(stm)
                .setReadLockMode(readLockMode)
                .setWriteLockMode(writeLockMode);

        GammaTxn tx = newTransaction(config);
        String newValue = "newvalue";
        ref.set(tx, newValue);
        tx.commit();

        assertTrue(waitingTx.retryListener.isOpen());
        assertVersionAndValue(ref, initialVersion + 1, newValue);
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
        tx.commit();

        assertGlobalConflictCount(stm, globalConflictCount + 1);
        assertVersionAndValue(ref, initialVersion + 1, newValue);
    }

    @Test
    public void whenContainsConstructedIntRef() {
        long globalConflictCount = stm.globalConflictCounter.count();

        T tx = newTransaction();
        int initialValue = 10;
        GammaTxnInteger ref = new GammaTxnInteger(tx, initialValue);
        tx.commit();

        assertIsCommitted(tx);
        assertRefHasNoLocks(ref);
        assertVersionAndValue(ref, VERSION_UNCOMMITTED + 1, initialValue);
        assertSurplus(ref, 0);
        assertReadonlyCount(ref, 0);
        assertWriteBiased(ref);

        assertGlobalConflictCount(stm, globalConflictCount);
    }

    @Test
    public void whenContainsConstructedBooleanRef() {
        long globalConflictCount = stm.globalConflictCounter.count();

        T tx = newTransaction();
        boolean initialValue = true;
        GammaTxnBoolean ref = new GammaTxnBoolean(tx, initialValue);
        tx.commit();

        assertIsCommitted(tx);
        assertRefHasNoLocks(ref);
        assertVersionAndValue(ref, VERSION_UNCOMMITTED + 1, initialValue);
        assertSurplus(ref, 0);
        assertReadonlyCount(ref, 0);
        assertWriteBiased(ref);

        assertGlobalConflictCount(stm, globalConflictCount);
    }

    @Test
    public void whenContainsConstructedTxnDouble() {
        long globalConflictCount = stm.globalConflictCounter.count();

        T tx = newTransaction();
        double initialValue = 10;
        GammaTxnDouble ref = new GammaTxnDouble(tx, initialValue);
        tx.commit();

        assertIsCommitted(tx);
        assertRefHasNoLocks(ref);
        assertVersionAndValue(ref, VERSION_UNCOMMITTED + 1, initialValue);
        assertSurplus(ref, 0);
        assertReadonlyCount(ref, 0);
        assertWriteBiased(ref);

        assertGlobalConflictCount(stm, globalConflictCount);
    }

    @Test
    public void whenContainsConstructedRef() {
        long globalConflictCount = stm.globalConflictCounter.count();

        T tx = newTransaction();
        String initialValue = "foo";
        GammaTxnRef<String> ref = new GammaTxnRef<String>(tx, initialValue);
        tx.commit();

        assertIsCommitted(tx);
        assertRefHasNoLocks(ref);
        assertVersionAndValue(ref, VERSION_UNCOMMITTED + 1, initialValue);
        assertSurplus(ref, 0);
        assertReadonlyCount(ref, 0);
        assertWriteBiased(ref);

        assertGlobalConflictCount(stm, globalConflictCount);
    }

    @Test
    public void whenContainsConstructedLongRef() {
        long globalConflictCount = stm.globalConflictCounter.count();

        T tx = newTransaction();
        long initialValue = 10;
        GammaTxnLong ref = new GammaTxnLong(tx, initialValue);
        tx.commit();

        assertIsCommitted(tx);
        assertRefHasNoLocks(ref);
        assertVersionAndValue(ref, VERSION_UNCOMMITTED + 1, initialValue);
        assertSurplus(ref, 0);
        assertReadonlyCount(ref, 0);
        assertWriteBiased(ref);

        assertGlobalConflictCount(stm, globalConflictCount);
    }

    @Test
    public void whenCommuteThrowsRuntimeException() {
        long globalConflictCount = stm.globalConflictCounter.count();

        long initialValue = 10;
        GammaTxnLong ref = new GammaTxnLong(stm, initialValue);
        long initialVersion = ref.getVersion();

        T tx = newTransaction();
        LongFunction function = mock(LongFunction.class);
        when(function.call(initialValue)).thenThrow(new SomeUncheckedException());
        ref.commute(tx, function);

        try {
            tx.commit();
            fail();
        } catch (SomeUncheckedException expected) {
        }

        assertIsAborted(tx);
        assertVersionAndValue(ref, initialVersion, initialValue);
        assertRefHasNoLocks(ref);

        assertGlobalConflictCount(stm, globalConflictCount);
    }

    @Test
    public void whenCommuteThrowsError() {
        long globalConflictCount = stm.globalConflictCounter.count();

        long initialValue = 10;
        GammaTxnLong ref = new GammaTxnLong(stm, initialValue);
        long initialVersion = ref.getVersion();

        T tx = newTransaction();
        LongFunction function = mock(LongFunction.class);
        when(function.call(initialValue)).thenThrow(new SomeError());
        ref.commute(tx, function);

        try {
            tx.commit();
            fail();
        } catch (SomeError expected) {
        }

        assertIsAborted(tx);
        assertVersionAndValue(ref, initialVersion, initialValue);
        assertRefHasNoLocks(ref);

        assertGlobalConflictCount(stm, globalConflictCount);
    }

    @Test
    public void whenContainsCommute() {
        long globalConflictCount = stm.globalConflictCounter.count();

        long initialValue = 10;

        GammaTxnLong ref = new GammaTxnLong(stm, initialValue);
        long initialVersion = ref.getVersion();

        T tx = newTransaction();
        ref.commute(tx, Functions.incLongFunction());
        tx.commit();

        assertIsCommitted(tx);
        assertVersionAndValue(ref, initialVersion + 1, initialValue + 1);
        assertRefHasNoLocks(ref);

        assertGlobalConflictCount(stm, globalConflictCount);
    }

    @Test
    public void abortOnly_whenUnused() {
        long globalConflictCount = stm.globalConflictCounter.count();

        T tx = newTransaction();
        tx.setAbortOnly();

        try {
            tx.commit();
            fail();
        } catch (AbortOnlyException expected) {
        }

        assertIsAborted(tx);

        assertGlobalConflictCount(stm, globalConflictCount);
    }

    @Test
    public void abortOnly_whenDirty() {
        long globalConflictCount = stm.globalConflictCounter.count();

        long initialValue = 10;
        GammaTxnLong ref = new GammaTxnLong(stm, initialValue);
        long initialVersion = ref.getVersion();

        T tx = newTransaction();
        tx.setAbortOnly();

        try {
            ref.set(tx, initialValue + 1);
            tx.commit();
            fail();
        } catch (AbortOnlyException expected) {
        }

        assertIsAborted(tx);
        assertVersionAndValue(ref, initialVersion, initialValue);

        assertGlobalConflictCount(stm, globalConflictCount);
    }

    @Test
    public void whenBooleanRef() {
        long globalConflictCount = stm.globalConflictCounter.count();

        boolean initialValue = false;
        GammaTxnBoolean ref = new GammaTxnBoolean(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTxn tx = newTransaction();
        ref.set(tx, true);
        tx.commit();

        assertVersionAndValue(ref, initialVersion + 1, true);
        assertRefHasNoLocks(ref);

        assertGlobalConflictCount(stm, globalConflictCount);
    }

    @Test
    public void whenIntRef() {
        long globalConflictCount = stm.globalConflictCounter.count();

        int initialValue = 10;
        GammaTxnInteger ref = new GammaTxnInteger(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTxn tx = newTransaction();
        ref.set(tx, initialValue + 1);
        tx.commit();

        assertVersionAndValue(ref, initialVersion + 1, initialValue + 1);
        assertRefHasNoLocks(ref);

        assertGlobalConflictCount(stm, globalConflictCount);
    }

    @Test
    public void whenLongRef() {
        long globalConflictCount = stm.globalConflictCounter.count();

        long initialValue = 10;
        GammaTxnLong ref = new GammaTxnLong(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTxn tx = newTransaction();
        ref.set(tx, initialValue + 1);
        tx.commit();

        assertVersionAndValue(ref, initialVersion + 1, initialValue + 1);
        assertRefHasNoLocks(ref);

        assertGlobalConflictCount(stm, globalConflictCount);
    }

    @Test
    public void whenTxnDouble() {
        long globalConflictCount = stm.globalConflictCounter.count();

        double initialValue = 10;
        GammaTxnDouble ref = new GammaTxnDouble(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTxn tx = newTransaction();
        ref.set(tx, initialValue + 1);
        tx.commit();

        assertVersionAndValue(ref, initialVersion + 1, initialValue + 1);
        assertRefHasNoLocks(ref);

        assertGlobalConflictCount(stm, globalConflictCount);
    }

    @Test
    public void whenRef() {
        long globalConflictCount = stm.globalConflictCounter.count();

        String initialValue = "foo";
        GammaTxnRef<String> ref = new GammaTxnRef<String>(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTxn tx = newTransaction();
        String newValue = "bar";
        ref.set(tx, newValue);
        tx.commit();

        assertVersionAndValue(ref, initialVersion + 1, "bar");
        assertRefHasNoLocks(ref);

        assertGlobalConflictCount(stm, globalConflictCount);
    }

    @Test
    public void whenContainsListener() {
        long globalConflictCount = stm.globalConflictCounter.count();

        long initialValue = 10;
        GammaTxnLong ref = new GammaTxnLong(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTxn listeningTx = newTransaction();
        ref.openForRead(listeningTx, LOCKMODE_NONE);

        try {
            listeningTx.retry();
            fail();
        } catch (RetryError retry) {
        }

        GammaTxn tx = newTransaction();
        ref.openForWrite(tx, LOCKMODE_NONE).long_value++;
        tx.commit();

        assertTrue(listeningTx.retryListener.isOpen());
        assertNull(getField(ref, "listeners"));
        assertRefHasNoLocks(ref);
        assertVersionAndValue(ref, initialVersion + 1, initialValue + 1);

        assertGlobalConflictCount(stm, globalConflictCount);
    }

    @Test
    public void whenUnused() {
        long globalConflictCount = stm.globalConflictCounter.count();

        GammaTxn tx = newTransaction();

        tx.commit();

        assertEquals(TxnStatus.Committed, tx.getStatus());
        assertGlobalConflictCount(stm, globalConflictCount);
    }

    @Test
    public void whenConflict() {
        long globalConflictCount = stm.globalConflictCounter.count();

        long initialValue = 10;
        GammaTxnLong ref = new GammaTxnLong(stm, initialValue);
        long initialVersion = ref.getVersion();

        T tx = newTransaction();
        Tranlocal tranlocal = ref.openForWrite(tx, LOCKMODE_NONE);
        tranlocal.long_value++;

        //a conflicting write.
        T otherTx = newTransaction();
        ref.openForWrite(otherTx, LOCKMODE_NONE).long_value++;
        otherTx.commit();

        try {
            tx.commit();
            fail();
        } catch (ReadWriteConflict expected) {

        }

        assertEquals(TxnStatus.Aborted, tx.getStatus());
        assertEquals(initialValue + 1, ref.long_value);
        assertEquals(initialVersion + 1, ref.version);
        assertCleaned(tx);
        assertGlobalConflictCount(stm, globalConflictCount);
    }

    //todo: dirty checking
    //todo: lock releasing

    @Test
    public void whenContainsDirtyWrite() {
        long globalConflictCount = stm.globalConflictCounter.count();

        long initialValue = 10;
        GammaTxnLong ref = new GammaTxnLong(stm, initialValue);
        long initialVersion = ref.getVersion();

        T tx = newTransaction();
        Tranlocal tranlocal = ref.openForWrite(tx, LOCKMODE_NONE);
        tranlocal.long_value++;
        tx.commit();

        assertNull(tranlocal.owner);
        assertEquals(LOCKMODE_NONE, tranlocal.lockMode);
        assertEquals(TxnStatus.Committed, tx.getStatus());
        assertEquals(initialValue + 1, ref.long_value);
        assertEquals(initialVersion + 1, ref.version);
        assertCleaned(tx);
        assertGlobalConflictCount(stm, globalConflictCount);
    }

    @Test
    public void whenMultipleCommitsUsingNewTransaction() {
        long globalConflictCount = stm.globalConflictCounter.count();

        long initialValue = 10;
        GammaTxnLong ref = new GammaTxnLong(stm, initialValue);
        long initialVersion = ref.getVersion();

        int txCount = 10;
        for (int k = 0; k < txCount; k++) {
            T tx = newTransaction();
            ref.openForWrite(tx, LOCKMODE_NONE).long_value++;
            tx.commit();
        }

        assertEquals(initialValue + txCount, ref.long_value);
        assertEquals(initialVersion + txCount, ref.version);

        assertGlobalConflictCount(stm, globalConflictCount);
    }

    @Test
    public void whenMultipleCommitsUsingSame() {
        long globalConflictCount = stm.globalConflictCounter.count();

        long initialValue = 10;
        GammaTxnLong ref = new GammaTxnLong(stm, initialValue);
        long initialVersion = ref.getVersion();

        int txCount = 10;
        T tx = newTransaction();
        for (int k = 0; k < txCount; k++) {
            ref.openForWrite(tx, LOCKMODE_NONE).long_value++;
            tx.commit();
            tx.hardReset();
        }

        assertEquals(initialValue + txCount, ref.long_value);
        assertEquals(initialVersion + txCount, ref.version);

        assertGlobalConflictCount(stm, globalConflictCount);
    }

    @Test
    public void whenPreparedAndContainsRead() {
        long globalConflictCount = stm.globalConflictCounter.count();

        long initialValue = 10;
        GammaTxnLong ref = new GammaTxnLong(stm, initialValue);
        long initialVersion = ref.getVersion();

        T tx = newTransaction();
        Tranlocal tranlocal = ref.openForRead(tx, LOCKMODE_NONE);
        tx.prepare();

        tx.commit();

        assertNull(tranlocal.owner);
        assertEquals(TxnStatus.Committed, tx.getStatus());
        assertEquals(initialValue, ref.long_value);
        assertEquals(initialVersion, ref.version);
        assertCleaned(tx);
        assertGlobalConflictCount(stm, globalConflictCount);
    }

    @Test
    public void whenPreparedAndContainsWrite() {
        long globalConflictCount = stm.globalConflictCounter.count();

        long initialValue = 10;
        GammaTxnLong ref = new GammaTxnLong(stm, initialValue);
        long initialVersion = ref.getVersion();

        T tx = newTransaction();
        Tranlocal tranlocal = ref.openForWrite(tx, LOCKMODE_NONE);
        tranlocal.long_value++;
        tx.prepare();

        tx.commit();

        assertNull(tranlocal.owner);
        assertEquals(TxnStatus.Committed, tx.getStatus());
        assertEquals(LOCKMODE_NONE, tranlocal.lockMode);
        assertEquals(initialValue + 1, ref.long_value);
        assertEquals(initialVersion + 1, ref.version);
        assertCleaned(tx);
        assertGlobalConflictCount(stm, globalConflictCount);
    }

    // ================================ dirty check ================================

    @Test
    public void whenContainsRead() {
        whenContainsRead(true, LockMode.None);
        whenContainsRead(true, LockMode.Read);
        whenContainsRead(true, LockMode.Write);
        whenContainsRead(true, LockMode.Exclusive);

        whenContainsRead(false, LockMode.None);
        whenContainsRead(false, LockMode.Read);
        whenContainsRead(false, LockMode.Write);
        whenContainsRead(false, LockMode.Exclusive);
    }

    public void whenContainsRead(boolean prepareFirst, LockMode readLockMode) {
        long globalConflictCount = stm.globalConflictCounter.count();

        long initialValue = 10;
        GammaTxnLong ref = new GammaTxnLong(stm, initialValue);
        long initialVersion = ref.getVersion();

        T tx = newTransaction();
        Tranlocal tranlocal = ref.openForRead(tx, readLockMode.asInt());
        if (prepareFirst) {
            tx.prepare();
        }
        tx.commit();

        assertIsCommitted(tx);
        assertLockMode(ref, LockMode.None);
        assertEquals(LOCKMODE_NONE, tranlocal.lockMode);
        assertVersionAndValue(ref, initialVersion, initialValue);
        assertCleaned(tx);
        assertGlobalConflictCount(stm, globalConflictCount);
    }

    // ================================ dirty check ================================

    @Test
    public void dirty_whenNoDirtyCheckAndNoDirtyWrite() {
        dirty_whenNoDirtyCheckAndNoDirtyWrite(true);
        dirty_whenNoDirtyCheckAndNoDirtyWrite(false);
    }

    public void dirty_whenNoDirtyCheckAndNoDirtyWrite(boolean prepareFirst) {
        long globalConflictCount = stm.globalConflictCounter.count();

        long initialValue = 10;
        GammaTxnLong ref = new GammaTxnLong(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTxnConfig config = new GammaTxnConfig(stm);
        config.dirtyCheck = false;
        T tx = newTransaction(config);
        Tranlocal tranlocal = ref.openForWrite(tx, LOCKMODE_NONE);

        if (prepareFirst) {
            tx.prepare();
        }
        tx.commit();

        assertNull(tranlocal.owner);
        assertEquals(TxnStatus.Committed, tx.getStatus());
        assertEquals(initialValue, ref.long_value);
        assertEquals(initialVersion + 1, ref.version);
        assertCleaned(tx);
        assertRefHasNoLocks(ref);
        assertGlobalConflictCount(stm, globalConflictCount);
    }

    @Test
    public void dirty_whenNoDirtyCheckAndDirtyWrite() {
        dirty_whenNoDirtyCheckAndDirtyWrite(true);
        dirty_whenNoDirtyCheckAndDirtyWrite(false);
    }

    public void dirty_whenNoDirtyCheckAndDirtyWrite(boolean prepareFirst) {
        long globalConflictCount = stm.globalConflictCounter.count();

        long initialValue = 10;
        GammaTxnLong ref = new GammaTxnLong(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTxnConfig config = new GammaTxnConfig(stm);
        config.dirtyCheck = false;
        T tx = newTransaction(config);
        Tranlocal tranlocal = ref.openForWrite(tx, LOCKMODE_NONE);
        tranlocal.long_value++;

        if (prepareFirst) {
            tx.prepare();
        }
        tx.commit();

        assertNull(tranlocal.owner);
        assertEquals(TxnStatus.Committed, tx.getStatus());
        assertEquals(initialValue + 1, ref.long_value);
        assertEquals(initialVersion + 1, ref.version);
        assertEquals(LOCKMODE_NONE, tranlocal.lockMode);
        assertCleaned(tx);
        assertRefHasNoLocks(ref);
        assertGlobalConflictCount(stm, globalConflictCount);
    }

    @Test
    public void dirty_whenDirtyCheckAndNoDirtyWrite() {
        dirty_whenDirtyCheckAndNoDirtyWrite(true);
        dirty_whenDirtyCheckAndNoDirtyWrite(false);
    }

    public void dirty_whenDirtyCheckAndNoDirtyWrite(boolean prepareFirst) {
        long globalConflictCount = stm.globalConflictCounter.count();

        long initialValue = 10;
        GammaTxnLong ref = new GammaTxnLong(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTxnConfig config = new GammaTxnConfig(stm);
        config.dirtyCheck = true;
        T tx = newTransaction(config);
        Tranlocal tranlocal = ref.openForWrite(tx, LOCKMODE_NONE);
        if (prepareFirst) {
            tx.prepare();
        }
        tx.commit();

        assertNull(tranlocal.owner);
        assertEquals(LOCKMODE_NONE, tranlocal.lockMode);
        assertEquals(TxnStatus.Committed, tx.getStatus());
        assertEquals(initialValue, ref.long_value);
        assertEquals(initialVersion, ref.version);
        assertCleaned(tx);
        assertRefHasNoLocks(ref);
        assertGlobalConflictCount(stm, globalConflictCount);
    }

    @Test
    public void dirty_whenDirtyCheckAndDirtyWrite() {
        dirty_whenDirtyCheckAndDirtyWrite(true);
        dirty_whenDirtyCheckAndDirtyWrite(false);
    }

    public void dirty_whenDirtyCheckAndDirtyWrite(boolean prepareFirst) {
        long globalConflictCount = stm.globalConflictCounter.count();

        long initialValue = 10;
        GammaTxnLong ref = new GammaTxnLong(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTxnConfig config = new GammaTxnConfig(stm);
        config.dirtyCheck = true;
        T tx = newTransaction(config);
        Tranlocal tranlocal = ref.openForWrite(tx, LOCKMODE_NONE);
        tranlocal.long_value++;
        if (prepareFirst) {
            tx.prepare();
        }
        tx.commit();

        assertNull(tranlocal.owner);
        assertEquals(LOCKMODE_NONE, tranlocal.lockMode);
        assertEquals(TxnStatus.Committed, tx.getStatus());
        assertEquals(initialValue + 1, ref.long_value);
        assertEquals(initialVersion + 1, ref.version);
        assertCleaned(tx);
        assertRefHasNoLocks(ref);
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
            tx.commit();
            fail();
        } catch (ReadWriteConflict expected) {
        }

        assertIsAborted(tx);
        assertVersionAndValue(ref, initialVersion, initialValue);
        assertRefHasReadLock(ref, otherTx);
        assertReadLockCount(ref, 1);
        assertEquals(LOCKMODE_NONE, tranlocal.lockMode);
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
            tx.commit();
            fail();
        } catch (ReadWriteConflict expected) {
        }

        assertIsAborted(tx);
        assertVersionAndValue(ref, initialVersion, initialValue);
        assertRefHasWriteLock(ref, otherTx);
        assertEquals(LOCKMODE_NONE, tranlocal.lockMode);
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
            tx.commit();
            fail();
        } catch (ReadWriteConflict expected) {
        }

        assertEquals(LOCKMODE_NONE, tranlocal.lockMode);
        assertIsAborted(tx);
        assertVersionAndValue(ref, initialVersion, initialValue);
        assertRefHasExclusiveLock(ref, otherTx);

        assertGlobalConflictCount(stm, globalConflictCount);
    }

    // ========================= states ==================================

    @Test
    public void whenPreparedAndUnused() {
        long globalConflictCount = stm.globalConflictCounter.count();

        T tx = newTransaction();
        tx.prepare();

        tx.commit();
        assertIsCommitted(tx);
        assertCleaned(tx);

        assertGlobalConflictCount(stm, globalConflictCount);
    }

    @Test
    public void whenAborted_thenDeadTxnException() {
        long globalConflictCount = stm.globalConflictCounter.count();

        T tx = newTransaction();
        tx.abort();

        try {
            tx.commit();
            fail();
        } catch (DeadTxnException expected) {
        }

        assertEquals(TxnStatus.Aborted, tx.getStatus());
        assertCleaned(tx);

        assertGlobalConflictCount(stm, globalConflictCount);
    }

    @Test
    public void whenCommitted_thenIgnored() {
        long globalConflictCount = stm.globalConflictCounter.count();

        T tx = newTransaction();
        tx.commit();

        tx.commit();

        assertEquals(TxnStatus.Committed, tx.getStatus());
        assertCleaned(tx);

        assertGlobalConflictCount(stm, globalConflictCount);
    }
}
