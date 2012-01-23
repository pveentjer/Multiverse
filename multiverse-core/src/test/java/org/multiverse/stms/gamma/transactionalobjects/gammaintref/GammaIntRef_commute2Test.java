package org.multiverse.stms.gamma.transactionalobjects.gammaintref;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.multiverse.api.LockMode;
import org.multiverse.api.Transaction;
import org.multiverse.api.TransactionFactory;
import org.multiverse.api.exceptions.DeadTransactionException;
import org.multiverse.api.exceptions.PreparedTransactionException;
import org.multiverse.api.exceptions.ReadWriteConflict;
import org.multiverse.api.functions.Functions;
import org.multiverse.api.functions.IntFunction;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactionalobjects.GammaIntRef;
import org.multiverse.stms.gamma.transactionalobjects.GammaRefTranlocal;
import org.multiverse.stms.gamma.transactions.GammaTransaction;
import org.multiverse.stms.gamma.transactions.GammaTransactionFactory;
import org.multiverse.stms.gamma.transactions.fat.FatFixedLengthGammaTransactionFactory;
import org.multiverse.stms.gamma.transactions.fat.FatMonoGammaTransaction;
import org.multiverse.stms.gamma.transactions.fat.FatMonoGammaTransactionFactory;
import org.multiverse.stms.gamma.transactions.fat.FatVariableLengthGammaTransactionFactory;

import java.util.Collection;

import static java.util.Arrays.asList;
import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;
import static org.mockito.Mockito.*;
import static org.multiverse.TestUtils.LOCKMODE_NONE;
import static org.multiverse.TestUtils.*;
import static org.multiverse.api.ThreadLocalTransaction.clearThreadLocalTransaction;
import static org.multiverse.api.ThreadLocalTransaction.getThreadLocalTransaction;
import static org.multiverse.stms.gamma.GammaTestUtils.*;

@RunWith(Parameterized.class)
public class GammaIntRef_commute2Test {

    private final GammaTransactionFactory transactionFactory;
    private final GammaStm stm;

    public GammaIntRef_commute2Test(GammaTransactionFactory transactionFactory) {
        this.transactionFactory = transactionFactory;
        this.stm = transactionFactory.getConfiguration().getStm();
    }

    @Before
    public void setUp() {
        clearThreadLocalTransaction();
    }

    @Parameterized.Parameters
    public static Collection<TransactionFactory[]> configs() {
        return asList(
                new TransactionFactory[]{new FatVariableLengthGammaTransactionFactory(new GammaStm())},
                new TransactionFactory[]{new FatFixedLengthGammaTransactionFactory(new GammaStm())},
                new TransactionFactory[]{new FatMonoGammaTransactionFactory(new GammaStm())}
        );
    }

    @Test
    public void whenCommuteFunctionCausesProblems_thenNoProblemsSinceCommuteFunctionNotEvaluatedImmediately() {
        GammaIntRef ref = new GammaIntRef(stm);

        IntFunction function = mock(IntFunction.class);
        RuntimeException ex = new RuntimeException();
        when(function.call(anyInt())).thenThrow(ex);

        GammaTransaction tx = transactionFactory.newTransaction();
        ref.commute(tx, function);

        assertHasCommutingFunctions(tx.getRefTranlocal(ref), function);

        assertIsActive(tx);
        assertEquals(0, ref.atomicGet());
        assertRefHasNoLocks(ref);
        assertSurplus(ref, 0);
        assertNull(getThreadLocalTransaction());
    }

    @Test
    public void whenExclusiveLockAcquiredByOther_thenCommuteSucceedsButCommitFails() {
        int initialValue = 10;
        GammaIntRef ref = new GammaIntRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTransaction otherTx = transactionFactory.newTransaction();
        ref.getLock().acquire(otherTx, LockMode.Exclusive);

        GammaTransaction tx = transactionFactory.newTransaction();
        ref.commute(tx, Functions.incIntFunction());

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
        int initialValue = 10;
        GammaIntRef ref = new GammaIntRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTransaction otherTx = transactionFactory.newTransaction();
        ref.getLock().acquire(otherTx, LockMode.Write);

        GammaTransaction tx = transactionFactory.newTransaction();
        ref.commute(tx, Functions.incIntFunction());

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
        int initialValue = 10;
        GammaIntRef ref = new GammaIntRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTransaction otherTx = transactionFactory.newTransaction();
        ref.getLock().acquire(otherTx, LockMode.Read);

        GammaTransaction tx = transactionFactory.newTransaction();
        ref.commute(tx, Functions.incIntFunction());

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
        int initialValue = 10;
        GammaIntRef ref = new GammaIntRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        IntFunction function = Functions.incIntFunction();
        GammaTransaction tx = transactionFactory.newTransaction();
        ref.commute(tx, function);

        GammaRefTranlocal commute = tx.getRefTranlocal(ref);
        assertTrue(commute.isCommuting());
        assertEquals(0, commute.long_value);
        tx.commit();

        assertVersionAndValue(ref, initialVersion + 1, initialValue + 1);
    }

    @Test
    public void whenNoChange() {
        int initialValue = 10;
        GammaIntRef ref = new GammaIntRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        IntFunction function = Functions.identityIntFunction();
        GammaTransaction tx = transactionFactory.newTransaction();
        ref.commute(tx, function);

        GammaRefTranlocal commute = tx.getRefTranlocal(ref);
        assertTrue(commute.isCommuting());
        assertEquals(0, commute.long_value);
        tx.commit();

        assertVersionAndValue(ref, initialVersion, initialValue);
    }

    @Test
    public void whenNormalTransactionUsed() {
        int initialValue = 10;
        GammaIntRef ref = new GammaIntRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        IntFunction function = Functions.incIntFunction(1);
        Transaction tx = transactionFactory.newTransaction();
        ref.commute(tx, function);
        tx.commit();

        assertVersionAndValue(ref, initialVersion + 1, initialValue + 1);
    }

    @Test
    public void whenAlreadyOpenedForRead() {
        int initialValue = 10;
        GammaIntRef ref = new GammaIntRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        IntFunction function = Functions.incIntFunction(1);
        GammaTransaction tx = transactionFactory.newTransaction();
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
        IntFunction function = Functions.incIntFunction(1);
        GammaTransaction tx = transactionFactory.newTransaction();
        GammaIntRef ref = new GammaIntRef(tx);
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
        GammaIntRef ref = new GammaIntRef(stm, 10);

        IntFunction function = Functions.incIntFunction();
        GammaTransaction tx = transactionFactory.newTransaction();
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
        int initialValue = 10;
        GammaIntRef ref = new GammaIntRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        IntFunction function1 = Functions.incIntFunction();
        IntFunction function2 = Functions.incIntFunction();
        GammaTransaction tx = transactionFactory.newTransaction();
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
        int initialValue = 10;
        GammaIntRef ref = new GammaIntRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTransaction tx = transactionFactory.newTransaction();

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
        int initialValue = 10;
        GammaIntRef ref = new GammaIntRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        IntFunction function = mock(IntFunction.class);

        try {
            ref.commute((Transaction) null, function);
            fail();
        } catch (NullPointerException expected) {
        }

        verifyZeroInteractions(function);
        assertVersionAndValue(ref, initialVersion, initialValue);
    }

    @Test
    public void whenTransactionAborted_thenDeadTransactionException() {
        int initialValue = 10;
        GammaIntRef ref = new GammaIntRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        IntFunction function = mock(IntFunction.class);
        GammaTransaction tx = transactionFactory.newTransaction();
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
        int initialValue = 20;
        GammaIntRef ref = new GammaIntRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        IntFunction function = mock(IntFunction.class);
        GammaTransaction tx = transactionFactory.newTransaction();
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
        int initialValue = 10;
        GammaIntRef ref = new GammaIntRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        IntFunction function = mock(IntFunction.class);
        GammaTransaction tx = transactionFactory.newTransaction();
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
        assumeTrue(!(transactionFactory.newTransaction() instanceof FatMonoGammaTransaction));

        GammaIntRef ref1 = new GammaIntRef(stm, 10);
        GammaIntRef ref2 = new GammaIntRef(stm, 10);

        GammaTransaction tx1 = transactionFactory.newTransaction();
        ref1.openForWrite(tx1, LOCKMODE_NONE).long_value++;
        ref2.commute(tx1, Functions.incIntFunction(1));

        GammaTransaction tx2 = transactionFactory.newTransaction();
        ref2.openForWrite(tx2, LOCKMODE_NONE).long_value++;
        tx2.commit();

        tx1.commit();

        assertIsCommitted(tx1);
        assertEquals(11, ref1.atomicGet());
        assertEquals(12, ref2.atomicGet());
    }

    @Test
    public void whenListenersAvailable() {
        int initialValue = 10;
        GammaIntRef ref = new GammaIntRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        IntRefAwaitThread thread = new IntRefAwaitThread(ref, initialValue + 1);
        thread.start();

        sleepMs(500);

        GammaTransaction tx = transactionFactory.newTransaction();
        ref.commute(tx, Functions.incIntFunction());
        tx.commit();

        joinAll(thread);

        assertRefHasNoLocks(ref);
        assertVersionAndValue(ref, initialVersion + 1, initialValue + 1);
    }
}
