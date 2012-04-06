package org.multiverse.stms.gamma.transactionalobjects.gammalongref;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.multiverse.api.LockMode;
import org.multiverse.api.Txn;
import org.multiverse.api.TxnFactory;
import org.multiverse.api.exceptions.DeadTransactionException;
import org.multiverse.api.exceptions.PreparedTransactionException;
import org.multiverse.api.exceptions.ReadWriteConflict;
import org.multiverse.api.functions.Functions;
import org.multiverse.api.functions.LongFunction;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactionalobjects.GammaLongRef;
import org.multiverse.stms.gamma.transactionalobjects.GammaRefTranlocal;
import org.multiverse.stms.gamma.transactions.GammaTxn;
import org.multiverse.stms.gamma.transactions.GammaTxnFactory;
import org.multiverse.stms.gamma.transactions.fat.FatFixedLengthGammaTxnFactory;
import org.multiverse.stms.gamma.transactions.fat.FatMonoGammaTxn;
import org.multiverse.stms.gamma.transactions.fat.FatMonoGammaTxnFactory;
import org.multiverse.stms.gamma.transactions.fat.FatVariableLengthGammaTxnFactory;

import java.util.Collection;

import static java.util.Arrays.asList;
import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.*;
import static org.multiverse.TestUtils.LOCKMODE_NONE;
import static org.multiverse.TestUtils.*;
import static org.multiverse.api.TxnThreadLocal.clearThreadLocalTxn;
import static org.multiverse.api.TxnThreadLocal.getThreadLocalTxn;
import static org.multiverse.stms.gamma.GammaTestUtils.*;

@RunWith(Parameterized.class)
public class GammaLongRef_commute2Test {

    private final GammaTxnFactory transactionFactory;
    private final GammaStm stm;

    public GammaLongRef_commute2Test(GammaTxnFactory transactionFactory) {
        this.transactionFactory = transactionFactory;
        this.stm = transactionFactory.getConfiguration().getStm();
    }

    @Before
    public void setUp() {
        clearThreadLocalTxn();
    }

    @Parameterized.Parameters
    public static Collection<TxnFactory[]> configs() {
        return asList(
                new TxnFactory[]{new FatVariableLengthGammaTxnFactory(new GammaStm())},
                new TxnFactory[]{new FatFixedLengthGammaTxnFactory(new GammaStm())},
                new TxnFactory[]{new FatMonoGammaTxnFactory(new GammaStm())}
        );
    }

    @Test
    public void whenCommuteFunctionCausesProblems_thenNoProblemsSinceCommuteFunctionNotEvaluatedImmediately() {
        GammaLongRef ref = new GammaLongRef(stm);

        LongFunction function = mock(LongFunction.class);
        RuntimeException ex = new RuntimeException();
        when(function.call(anyLong())).thenThrow(ex);

        GammaTxn tx = transactionFactory.newTransaction();
        ref.commute(tx, function);

        assertHasCommutingFunctions(tx.getRefTranlocal(ref), function);

        assertIsActive(tx);
        assertEquals(0, ref.atomicGet());
        assertRefHasNoLocks(ref);
        assertSurplus(ref, 0);
        assertNull(getThreadLocalTxn());
    }

    @Test
    public void whenExclusiveLockAcquiredByOther_thenCommuteSucceedsButCommitFails() {
        long initialValue = 10;
        GammaLongRef ref = new GammaLongRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTxn otherTx = transactionFactory.newTransaction();
        ref.getLock().acquire(otherTx, LockMode.Exclusive);

        GammaTxn tx = transactionFactory.newTransaction();
        ref.commute(tx, Functions.incLongFunction());

        try {
            tx.commit();
            fail();
        } catch (ReadWriteConflict expected) {
        }

        assertSurplus(ref, 1);
        assertIsAborted(tx);
        assertRefHasExclusiveLock(ref, otherTx);
        assertVersionAndValue(ref, initialVersion, initialValue);
    }

    @Test
    public void whenWriteLockAcquiredByOther_thenCommuteSucceedsButCommitFails() {
        long initialValue = 10;
        GammaLongRef ref = new GammaLongRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTxn otherTx = transactionFactory.newTransaction();
        ref.getLock().acquire(otherTx, LockMode.Write);

        GammaTxn tx = transactionFactory.newTransaction();
        ref.commute(tx, Functions.incLongFunction());

        try {
            tx.commit();
            fail();
        } catch (ReadWriteConflict expected) {
        }

        assertIsAborted(tx);
        assertSurplus(ref, 1);
        assertRefHasWriteLock(ref, otherTx);
        assertVersionAndValue(ref, initialVersion, initialValue);
    }

    @Test
    public void whenReadLockAcquiredByOther_thenCommuteSucceedsButCommitFails() {
        long initialValue = 10;
        GammaLongRef ref = new GammaLongRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTxn otherTx = transactionFactory.newTransaction();
        ref.getLock().acquire(otherTx, LockMode.Read);

        GammaTxn tx = transactionFactory.newTransaction();
        ref.commute(tx, Functions.incLongFunction());

        try {
            tx.commit();
            fail();
        } catch (ReadWriteConflict expected) {
        }

        assertIsAborted(tx);
        assertSurplus(ref, 1);
        assertRefHasReadLock(ref, otherTx);
        assertVersionAndValue(ref, initialVersion, initialValue);
    }

    @Test
    public void whenSuccess() {
        long initialValue = 10;
        GammaLongRef ref = new GammaLongRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        LongFunction function = Functions.incLongFunction();
        GammaTxn tx = transactionFactory.newTransaction();
        ref.commute(tx, function);

        GammaRefTranlocal commute = tx.getRefTranlocal(ref);
        assertTrue(commute.isCommuting());
        assertEquals(0, commute.long_value);
        tx.commit();

        assertVersionAndValue(ref, initialVersion + 1, initialValue + 1);
    }

    @Test
    public void whenNoChange() {
        long initialValue = 10;
        GammaLongRef ref = new GammaLongRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        LongFunction function = Functions.identityLongFunction();
        GammaTxn tx = transactionFactory.newTransaction();
        ref.commute(tx, function);

        GammaRefTranlocal commute = tx.getRefTranlocal(ref);
        assertTrue(commute.isCommuting());
        assertEquals(0, commute.long_value);
        tx.commit();

        assertVersionAndValue(ref, initialVersion, initialValue);
    }

    @Test
    public void whenNormalTransactionUsed() {
        long initialValue = 10;
        GammaLongRef ref = new GammaLongRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        LongFunction function = Functions.incLongFunction(1);
        Txn tx = transactionFactory.newTransaction();
        ref.commute(tx, function);
        tx.commit();

        assertVersionAndValue(ref, initialVersion + 1, initialValue + 1);
    }

    @Test
    public void whenAlreadyOpenedForRead() {
        long initialValue = 10;
        GammaLongRef ref = new GammaLongRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        LongFunction function = Functions.incLongFunction(1);
        GammaTxn tx = transactionFactory.newTransaction();
        ref.get(tx);
        ref.commute(tx, function);

        GammaRefTranlocal commute = tx.getRefTranlocal(ref);
        assertFalse(commute.isCommuting());
        assertEquals(11, commute.long_value);
        tx.commit();

        assertVersionAndValue(ref, initialVersion + 1, initialValue + 1);
    }

    @Test
    public void whenAlreadyOpenedForConstruction() {
        LongFunction function = Functions.incLongFunction(1);
        GammaTxn tx = transactionFactory.newTransaction();
        GammaLongRef ref = new GammaLongRef(tx);
        ref.openForConstruction(tx);
        ref.commute(tx, function);

        GammaRefTranlocal commute = tx.getRefTranlocal(ref);
        assertFalse(commute.isCommuting());
        assertEquals(1, commute.long_value);
        tx.commit();

        assertEquals(1, ref.atomicGet());
    }

    @Test
    public void whenAlreadyOpenedForWrite() {
        GammaLongRef ref = new GammaLongRef(stm, 10);

        LongFunction function = Functions.incLongFunction();
        GammaTxn tx = transactionFactory.newTransaction();
        ref.set(tx, 11);
        ref.commute(tx, function);

        GammaRefTranlocal commute = tx.getRefTranlocal(ref);
        assertFalse(commute.isCommuting());
        assertEquals(12, commute.long_value);
        tx.commit();

        assertEquals(12, ref.atomicGet());
    }

    @Test
    public void whenAlreadyCommuting() {
        long initialValue = 10;
        GammaLongRef ref = new GammaLongRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        LongFunction function1 = Functions.incLongFunction();
        LongFunction function2 = Functions.incLongFunction();
        GammaTxn tx = transactionFactory.newTransaction();
        ref.commute(tx, function1);
        ref.commute(tx, function2);

        GammaRefTranlocal commute = tx.getRefTranlocal(ref);
        assertTrue(commute.isCommuting());
        assertEquals(0, commute.long_value);
        tx.commit();

        assertVersionAndValue(ref, initialVersion + 1, initialValue + 2);
    }

    @Test
    public void whenNullFunction_thenNullPointerException() {
        long initialValue = 10;
        GammaLongRef ref = new GammaLongRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTxn tx = transactionFactory.newTransaction();

        try {
            ref.commute(tx, null);
            fail();
        } catch (NullPointerException expected) {

        }

        assertIsAborted(tx);
        assertVersionAndValue(ref, initialVersion, initialValue);
    }

    @Test
    public void whenNullTransaction_thenNullPointerException() {
        long initialValue = 10;
        GammaLongRef ref = new GammaLongRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        LongFunction function = mock(LongFunction.class);

        try {
            ref.commute((Txn) null, function);
            fail();
        } catch (NullPointerException expected) {
        }

        verifyZeroInteractions(function);
        assertVersionAndValue(ref, initialVersion, initialValue);
    }

    @Test
    public void whenTransactionAborted_thenDeadTransactionException() {
        long initialValue = 10;
        GammaLongRef ref = new GammaLongRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        LongFunction function = mock(LongFunction.class);
        GammaTxn tx = transactionFactory.newTransaction();
        tx.abort();

        try {
            ref.commute(tx, function);
            fail();
        } catch (DeadTransactionException expected) {
        }

        assertIsAborted(tx);
        verifyZeroInteractions(function);
        assertVersionAndValue(ref, initialVersion, initialValue);
    }

    @Test
    public void whenTransactionCommitted_thenDeadTransactionException() {
        long initialValue = 20;
        GammaLongRef ref = new GammaLongRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        LongFunction function = mock(LongFunction.class);
        GammaTxn tx = transactionFactory.newTransaction();
        tx.commit();

        try {
            ref.commute(tx, function);
            fail();
        } catch (DeadTransactionException expected) {
        }

        assertIsCommitted(tx);
        verifyZeroInteractions(function);
        assertVersionAndValue(ref, initialVersion, initialValue);
    }

    @Test
    public void whenTransactionPrepared_thenPreparedTransactionException() {
        long initialValue = 10;
        GammaLongRef ref = new GammaLongRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        LongFunction function = mock(LongFunction.class);
        GammaTxn tx = transactionFactory.newTransaction();
        tx.prepare();

        try {
            ref.commute(tx, function);
            fail();
        } catch (PreparedTransactionException expected) {
        }

        assertIsAborted(tx);
        verifyZeroInteractions(function);
        assertVersionAndValue(ref, initialVersion, initialValue);
    }

    @Test
    public void fullExample() {
        assumeTrue(!(transactionFactory.newTransaction() instanceof FatMonoGammaTxn));

        GammaLongRef ref1 = new GammaLongRef(stm, 10);
        GammaLongRef ref2 = new GammaLongRef(stm, 10);

        GammaTxn tx1 = transactionFactory.newTransaction();
        ref1.openForWrite(tx1, LOCKMODE_NONE).long_value++;
        ref2.commute(tx1, Functions.incLongFunction(1));

        GammaTxn tx2 = transactionFactory.newTransaction();
        ref2.openForWrite(tx2, LOCKMODE_NONE).long_value++;
        tx2.commit();

        tx1.commit();

        assertIsCommitted(tx1);
        assertEquals(11, ref1.atomicGet());
        assertEquals(12, ref2.atomicGet());
    }

    @Test
    public void whenListenersAvailable() {
        long initialValue = 10;
        GammaLongRef ref = new GammaLongRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        LongRefAwaitThread thread = new LongRefAwaitThread(ref, initialValue + 1);
        thread.start();

        sleepMs(500);

        GammaTxn tx = transactionFactory.newTransaction();
        ref.commute(tx, Functions.incLongFunction());
        tx.commit();

        joinAll(thread);

        assertRefHasNoLocks(ref);
        assertVersionAndValue(ref, initialVersion + 1, initialValue + 1);
    }
}
