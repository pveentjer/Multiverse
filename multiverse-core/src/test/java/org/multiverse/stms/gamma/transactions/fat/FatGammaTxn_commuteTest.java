package org.multiverse.stms.gamma.transactions.fat;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.SomeUncheckedException;
import org.multiverse.api.LockMode;
import org.multiverse.api.TxnStatus;
import org.multiverse.api.exceptions.*;
import org.multiverse.api.functions.Functions;
import org.multiverse.api.functions.LongFunction;
import org.multiverse.stms.gamma.GammaConstants;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.GammaTestUtils;
import org.multiverse.stms.gamma.transactionalobjects.GammaTxnLong;
import org.multiverse.stms.gamma.transactionalobjects.Tranlocal;
import org.multiverse.stms.gamma.transactions.GammaTxn;
import org.multiverse.stms.gamma.transactions.GammaTxnConfig;

import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.*;
import static org.multiverse.TestUtils.LOCKMODE_NONE;
import static org.multiverse.TestUtils.*;
import static org.multiverse.stms.gamma.GammaTestUtils.*;

public abstract class FatGammaTxn_commuteTest<T extends GammaTxn> {

    protected GammaStm stm;

    @Before
    public void setUp() {
        stm = new GammaStm();
    }

    protected abstract T newTransaction();

    protected abstract T newTransaction(GammaTxnConfig config);

    protected abstract int getMaxCapacity();

    @Test
    public void whenTransactionAbortOnly_thenWriteStillPossible() {
        GammaTxnLong ref = new GammaTxnLong(stm, 0);

        GammaTxn tx = stm.newDefaultTxn();
        tx.setAbortOnly();
        ref.commute(tx, Functions.incLongFunction());

        Tranlocal tranlocal = tx.locate(ref);
        assertNotNull(tranlocal);
        assertEquals(TRANLOCAL_COMMUTING, tranlocal.getMode());
        assertTrue(tx.isAbortOnly());
        assertIsActive(tx);
    }


    @Test
    public void whenMultipleCommutesOnSingleRef() {
        long initialValue = 10;
        GammaTxnLong ref = new GammaTxnLong(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTxn tx = stm.newDefaultTxn();
        ref.commute(tx, Functions.incLongFunction());
        ref.commute(tx, Functions.incLongFunction());
        ref.commute(tx, Functions.incLongFunction());
        tx.commit();

        assertRefHasNoLocks(ref);
        assertVersionAndValue(ref, initialVersion + 1, initialValue + 3);
    }

    @Test
    public void whenAlreadyOpenedForRead() {
        whenAlreadyOpenedForRead(LockMode.None);
        whenAlreadyOpenedForRead(LockMode.Read);
        whenAlreadyOpenedForRead(LockMode.Write);
        whenAlreadyOpenedForRead(LockMode.Exclusive);
    }

    public void whenAlreadyOpenedForRead(LockMode lockMode) {
        long initialValue = 10;
        GammaTxnLong ref = new GammaTxnLong(stm, initialValue);
        long initialVersion = ref.version;

        GammaTxn tx = newTransaction();
        Tranlocal tranlocal = ref.openForRead(tx, lockMode.asInt());
        LongFunction incFunction = Functions.incLongFunction();
        ref.commute(tx, incFunction);

        assertEquals(initialValue + 1, tranlocal.long_value);
        assertTrue(tx.hasWrites);
        assertIsActive(tx);
        assertVersionAndValue(ref, initialVersion, initialValue);
        assertLockMode(ref, lockMode);
        assertTrue(tranlocal.isWrite());
        assertNull(tranlocal.headCallable);
    }

    @Test
    public void whenAlreadyOpenedForReadAndFunctionCausesProblem() {
        long initialValue = 10;
        GammaTxnLong ref = new GammaTxnLong(stm, initialValue);
        long initalVersion = ref.getVersion();

        GammaTxn tx = newTransaction();
        Tranlocal tranlocal = ref.openForRead(tx, LOCKMODE_NONE);
        LongFunction function = mock(LongFunction.class);
        when(function.call(anyLong())).thenThrow(new SomeUncheckedException());

        try {
            ref.commute(tx, function);
            fail();
        } catch (SomeUncheckedException expected) {
        }

        assertIsAborted(tx);
        assertNull(tranlocal.owner);
        assertRefHasNoLocks(ref);
        assertVersionAndValue(ref, initalVersion, initialValue);
    }

    @Test
    public void whenAlreadyOpenedForWrite() {
        whenAlreadyOpenedForWrite(LockMode.None);
        whenAlreadyOpenedForWrite(LockMode.Read);
        whenAlreadyOpenedForWrite(LockMode.Write);
        whenAlreadyOpenedForWrite(LockMode.Exclusive);
    }

    public void whenAlreadyOpenedForWrite(LockMode lockMode) {
        long initialValue = 10;
        GammaTxnLong ref = new GammaTxnLong(stm, initialValue);
        long initialVersion = ref.version;

        GammaTxn tx = newTransaction();
        Tranlocal tranlocal = ref.openForWrite(tx, lockMode.asInt());
        LongFunction incFunction = Functions.incLongFunction();
        ref.commute(tx, incFunction);

        assertEquals(initialValue + 1, tranlocal.long_value);
        assertTrue(tx.hasWrites);
        assertIsActive(tx);
        assertVersionAndValue(ref, initialVersion, initialValue);
        assertLockMode(ref, lockMode);
        assertTrue(tranlocal.isWrite());
        assertNull(tranlocal.headCallable);
    }

    @Test
    public void whenAlreadyOpenedForWriteAndFunctionCausesProblem() {
        long initialValue = 10;
        GammaTxnLong ref = new GammaTxnLong(stm, initialValue);
        long initalVersion = ref.getVersion();

        GammaTxn tx = newTransaction();
        Tranlocal tranlocal = ref.openForWrite(tx, LOCKMODE_NONE);
        LongFunction function = mock(LongFunction.class);
        when(function.call(anyLong())).thenThrow(new SomeUncheckedException());

        try {
            ref.commute(tx, function);
            fail();
        } catch (SomeUncheckedException expected) {
        }

        assertIsAborted(tx);
        assertNull(tranlocal.owner);
        assertRefHasNoLocks(ref);
        assertVersionAndValue(ref, initalVersion, initialValue);
    }

    @Test
    public void whenNotOpenedBefore() {
        long initialValue = 10;
        GammaTxnLong ref = new GammaTxnLong(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTxn tx = stm.newDefaultTxn();
        LongFunction function = mock(LongFunction.class);
        ref.commute(tx, function);
        Tranlocal tranlocal = tx.getRefTranlocal(ref);

        assertNotNull(tranlocal);
        assertTrue(tranlocal.isCommuting());
        assertSame(ref, tranlocal.owner);
        assertEquals(LOCKMODE_NONE, tranlocal.getLockMode());
        assertIsActive(tx);
        GammaTestUtils.assertHasCommutingFunctions(tranlocal, function);
        assertRefHasNoLocks(ref);
        assertVersionAndValue(ref, initialVersion, initialValue);
    }

    @Test
    public void whenAlreadyOpenedForCommute() {
        long initialValue = 10;
        GammaTxnLong ref = new GammaTxnLong(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTxn tx = stm.newDefaultTxn();
        LongFunction function1 = mock(LongFunction.class);
        LongFunction function2 = mock(LongFunction.class);
        ref.commute(tx, function1);
        ref.commute(tx, function2);
        Tranlocal tranlocal = tx.getRefTranlocal(ref);

        assertNotNull(tranlocal);
        assertTrue(tranlocal.isCommuting());
        assertSame(ref, tranlocal.owner);
        assertEquals(LOCKMODE_NONE, tranlocal.getLockMode());
        assertIsActive(tx);
        GammaTestUtils.assertHasCommutingFunctions(tranlocal, function2, function1);
        assertRefHasNoLocks(ref);
        assertVersionAndValue(ref, initialVersion, initialValue);
    }

    @Test
    public void lockedByOther() {
        lockedByOther(LockMode.None);
        lockedByOther(LockMode.Read);
        lockedByOther(LockMode.Write);
        lockedByOther(LockMode.Exclusive);
    }

    public void lockedByOther(LockMode otherLockMode) {
        long initialValue = 10;
        GammaTxnLong ref = new GammaTxnLong(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTxn otherTx = stm.newDefaultTxn();
        ref.getLock().acquire(otherTx, otherLockMode);

        GammaTxn tx = stm.newDefaultTxn();
        LongFunction function1 = mock(LongFunction.class);
        LongFunction function2 = mock(LongFunction.class);
        ref.commute(tx, function1);
        ref.commute(tx, function2);
        Tranlocal tranlocal = tx.getRefTranlocal(ref);

        assertNotNull(tranlocal);
        assertTrue(tranlocal.isCommuting());
        assertSame(ref, tranlocal.owner);
        assertEquals(LOCKMODE_NONE, tranlocal.getLockMode());
        assertIsActive(tx);
        GammaTestUtils.assertHasCommutingFunctions(tranlocal, function2, function1);
        assertRefHasLockMode(ref, otherTx, otherLockMode.asInt());
        assertVersionAndValue(ref, initialVersion, initialValue);
    }

    @Test
    public void whenAlreadyOpenedForConstruction() {
        GammaTxn tx = newTransaction();

        long initialValue = 10;
        GammaTxnLong ref = new GammaTxnLong(tx, initialValue);
        Tranlocal tranlocal = tx.locate(ref);
        LongFunction incFunction = Functions.incLongFunction();
        ref.commute(tx, incFunction);

        assertEquals(initialValue + 1, tranlocal.long_value);
        assertTrue(tx.hasWrites);
        assertIsActive(tx);
        assertVersionAndValue(ref, GammaConstants.VERSION_UNCOMMITTED, 0);
        assertLockMode(ref, LockMode.Exclusive);
        assertTrue(tranlocal.isConstructing());
        assertNull(tranlocal.headCallable);
    }

    @Test
    public void whenAlreadyOpenedForConstructionAndFunctionCausesProblem() {
        GammaTxn tx = newTransaction();
        LongFunction function = mock(LongFunction.class);
        when(function.call(anyLong())).thenThrow(new SomeUncheckedException());

        long initialValue = 10;
        GammaTxnLong ref = new GammaTxnLong(tx, initialValue);
        Tranlocal tranlocal = ref.openForWrite(tx, LOCKMODE_NONE);

        try {
            ref.commute(tx, function);
            fail();
        } catch (SomeUncheckedException expected) {
        }

        assertIsAborted(tx);
        assertNull(tranlocal.owner);
        assertLockMode(ref, LockMode.Exclusive);
        assertVersionAndValue(ref, GammaConstants.VERSION_UNCOMMITTED, 0);
    }

    @Test
    public void whenOverflowing() {
        int maxCapacity = getMaxCapacity();
        assumeTrue(maxCapacity < Integer.MAX_VALUE);

        GammaTxn tx = newTransaction();
        for (int k = 0; k < maxCapacity; k++) {
            GammaTxnLong ref = new GammaTxnLong(stm, 0);
            ref.openForRead(tx, LOCKMODE_NONE);
        }

        GammaTxnLong ref = new GammaTxnLong(stm, 0);
        try {
            ref.commute(tx, Functions.incLongFunction());
            fail();
        } catch (SpeculativeConfigurationError expected) {
        }

        assertEquals(TxnStatus.Aborted, tx.getStatus());
    }

    @Test
    public void whenNullTransaction() {
        long initialValue = 10;
        GammaTxnLong ref = new GammaTxnLong(stm, initialValue);
        long initialVersion = ref.getVersion();

        LongFunction function = mock(LongFunction.class);

        try {
            ref.commute((FatFixedLengthGammaTxn) null, function);
            fail();
        } catch (NullPointerException expected) {

        }

        assertVersionAndValue(ref, initialVersion, initialValue);
        assertLockMode(ref, LOCKMODE_NONE);
        verifyZeroInteractions(function);
    }

    @Test
    public void whenNullFunction() {
        long initialValue = 10;
        GammaTxnLong ref = new GammaTxnLong(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTxnConfig config = new GammaTxnConfig(stm)
                .setReadonly(true);

        GammaTxn tx = newTransaction(config);

        try {
            ref.commute(tx, null);
            fail();
        } catch (NullPointerException expected) {

        }

        assertIsAborted(tx);
        assertVersionAndValue(ref, initialVersion, initialValue);
        assertLockMode(ref, LOCKMODE_NONE);
    }

    @Test
    public void whenReadonlyTransaction_thenReadonlyException() {
        long initialValue = 10;
        GammaTxnLong ref = new GammaTxnLong(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTxnConfig config = new GammaTxnConfig(stm)
                .setReadonly(true);

        GammaTxn tx = newTransaction(config);
        LongFunction function = mock(LongFunction.class);

        try {
            ref.commute(tx, function);
            fail();
        } catch (ReadonlyException expected) {

        }

        assertIsAborted(tx);
        assertVersionAndValue(ref, initialVersion, initialValue);
        assertLockMode(ref, LOCKMODE_NONE);
        verifyZeroInteractions(function);
    }

    @Test
    public void whenStmMismatch() {
        GammaStm otherStm = new GammaStm();
        long initialValue = 10;
        GammaTxnLong ref = new GammaTxnLong(otherStm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTxn tx = stm.newDefaultTxn();
        LongFunction function = mock(LongFunction.class);
        try {
            ref.commute(tx, function);
            fail();
        } catch (StmMismatchException expected) {
        }

        assertIsAborted(tx);
        assertRefHasNoLocks(ref);
        assertVersionAndValue(ref, initialVersion, initialValue);
        verifyZeroInteractions(function);
    }

    // =========================== commuting =========================

    @Test
    public void commuting_whenCommuting_thenFailure() {
        long initialValue = 10;
        GammaTxnLong ref = new GammaTxnLong(stm, initialValue);
        long initialVersion = ref.getVersion();

        T tx = newTransaction();
        tx.evaluatingCommute = true;

        LongFunction function = mock(LongFunction.class);
        try {
            ref.commute(tx, function);
            fail();
        } catch (IllegalCommuteException expected) {
        }

        assertIsAborted(tx);
        assertVersionAndValue(ref, initialVersion, initialValue);
        assertRefHasNoLocks(ref);
        verifyZeroInteractions(function);
    }

    // ========================== state ==============================

    @Test
    public void whenTransactionPrepared_thenPreparedTxnException() {
        long initialValue = 10;
        GammaTxnLong ref = new GammaTxnLong(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTxn tx = newTransaction();
        LongFunction function = mock(LongFunction.class);

        tx.prepare();
        try {
            ref.commute(tx, function);
            fail();
        } catch (PreparedTxnException expected) {

        }

        assertIsAborted(tx);
        assertVersionAndValue(ref, initialVersion, initialValue);
        assertLockMode(ref, LOCKMODE_NONE);
        verifyZeroInteractions(function);
    }

    @Test
    public void whenTransactionAborted_thenDeadTxnException() {
        long initialValue = 10;
        GammaTxnLong ref = new GammaTxnLong(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTxn tx = newTransaction();
        LongFunction function = mock(LongFunction.class);

        tx.abort();
        try {
            ref.commute(tx, function);
            fail();
        } catch (DeadTxnException expected) {
        }

        assertIsAborted(tx);
        assertVersionAndValue(ref, initialVersion, initialValue);
        assertLockMode(ref, LOCKMODE_NONE);
        verifyZeroInteractions(function);
    }

    @Test
    public void whenTransactionCommitted_thenDeadTxnException() {
        long initialValue = 10;
        GammaTxnLong ref = new GammaTxnLong(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTxn tx = newTransaction();
        LongFunction function = mock(LongFunction.class);

        tx.commit();
        try {
            ref.commute(tx, function);
            fail();
        } catch (DeadTxnException expected) {
        }

        assertIsCommitted(tx);
        assertVersionAndValue(ref, initialVersion, initialValue);
        assertLockMode(ref, LOCKMODE_NONE);
        verifyZeroInteractions(function);
    }
}
