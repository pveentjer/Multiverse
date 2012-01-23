package org.multiverse.stms.gamma.transactionalobjects.gammaref;

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
import org.multiverse.api.functions.LongFunction;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactionalobjects.GammaRef;
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
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.*;
import static org.multiverse.TestUtils.LOCKMODE_NONE;
import static org.multiverse.TestUtils.*;
import static org.multiverse.api.ThreadLocalTransaction.clearThreadLocalTransaction;
import static org.multiverse.api.ThreadLocalTransaction.getThreadLocalTransaction;
import static org.multiverse.stms.gamma.GammaTestUtils.*;

@RunWith(Parameterized.class)
public class GammaRef_commute2Test {

    private final GammaTransactionFactory transactionFactory;
    private final GammaStm stm;

    public GammaRef_commute2Test(GammaTransactionFactory transactionFactory) {
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
        Long initialValue = 10L;
        GammaRef<Long> ref = new GammaRef<Long>(stm, initialValue);

        LongFunction function = mock(LongFunction.class);
        RuntimeException ex = new RuntimeException();
        when(function.call(anyLong())).thenThrow(ex);

        GammaTransaction tx = transactionFactory.newTransaction();
        ref.commute(tx, function);

        assertHasCommutingFunctions(tx.getRefTranlocal(ref), function);

        assertIsActive(tx);
        assertEquals(initialValue, ref.atomicGet());
        assertRefHasNoLocks(ref);
        assertSurplus(ref, 0);
        assertNull(getThreadLocalTransaction());
    }

    @Test
    public void whenLockedByOther(){
        whenLockedByOther(LockMode.Read);
        whenLockedByOther(LockMode.Write);
        whenLockedByOther(LockMode.Exclusive);
    }

   public void whenLockedByOther(LockMode lockMode) {
        Long initialValue = 10L;
        GammaRef<Long> ref = new GammaRef<Long>(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTransaction otherTx = transactionFactory.newTransaction();
        ref.getLock().acquire(otherTx, lockMode);

        GammaTransaction tx = transactionFactory.newTransaction();
        ref.commute(tx, Functions.incLongFunction());

        try {
            tx.commit();
            fail();
        } catch (ReadWriteConflict expected) {
        }

        assertSurplus(ref, 1);
        assertIsAborted(tx);
        assertRefHasLockMode(ref, otherTx, lockMode.asInt());
        assertVersionAndValue(ref, initialVersion, initialValue);
    }

    @Test
    public void whenSuccess() {
        Long initialValue = 10L;
        GammaRef<Long> ref = new GammaRef<Long>(stm, initialValue);
        long initialVersion = ref.getVersion();

        LongFunction function = Functions.incLongFunction();
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
        Long initialValue = 10L;
        GammaRef<Long> ref = new GammaRef<Long>(stm, initialValue);
        long initialVersion = ref.getVersion();

        LongFunction function = Functions.identityLongFunction();
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
        Long initialValue = 10L;
        GammaRef<Long> ref = new GammaRef<Long>(stm, initialValue);
        long initialVersion = ref.getVersion();

        LongFunction function = Functions.incLongFunction(1);
        Transaction tx = transactionFactory.newTransaction();
        ref.commute(tx, function);
        tx.commit();

        assertVersionAndValue(ref, initialVersion + 1, initialValue + 1);
    }

    @Test
    public void whenAlreadyOpenedForRead() {
        Long initialValue = 10L;
        GammaRef<Long> ref = new GammaRef<Long>(stm, initialValue);
        long initialVersion = ref.getVersion();

        LongFunction function = Functions.incLongFunction(1);
        GammaTransaction tx = transactionFactory.newTransaction();
        ref.get(tx);
        ref.commute(tx, function);

        GammaRefTranlocal commute = tx.getRefTranlocal(ref);
        assertFalse(commute.isCommuting());
        assertEquals(new Long(11), commute.ref_value);
        tx.commit();

        assertVersionAndValue(ref, initialVersion + 1, initialValue + 1);
    }

    @Test
    public void whenAlreadyOpenedForConstruction() {
        GammaTransaction tx = transactionFactory.newTransaction();
        Long initialValue = 10L;
        GammaRef<Long> ref = new GammaRef<Long>(tx, initialValue);
        ref.openForConstruction(tx);
        ref.commute(tx, Functions.incLongFunction());

        GammaRefTranlocal commute = tx.getRefTranlocal(ref);
        assertFalse(commute.isCommuting());
        assertEquals(new Long(11), commute.ref_value);
        tx.commit();

        assertEquals(new Long(11), ref.atomicGet());
    }

    @Test
    public void whenAlreadyOpenedForWrite() {
        Long initialValue = 10L;
        GammaRef<Long> ref = new GammaRef<Long>(stm, initialValue);

        LongFunction function = Functions.incLongFunction();
        GammaTransaction tx = transactionFactory.newTransaction();
        ref.set(tx, new Long(11));
        ref.commute(tx, function);

        GammaRefTranlocal commute = tx.getRefTranlocal(ref);
        assertFalse(commute.isCommuting());
        assertEquals(new Long(12), commute.ref_value);
        tx.commit();

        assertEquals(new Long(12), ref.atomicGet());
    }

    @Test
    public void whenAlreadyCommuting() {
        Long initialValue = 10L;
        GammaRef<Long> ref = new GammaRef<Long>(stm, initialValue);
        long initialVersion = ref.getVersion();

        LongFunction function1 = Functions.incLongFunction();
        LongFunction function2 = Functions.incLongFunction();
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
        Long initialValue = 10L;
        GammaRef<Long> ref = new GammaRef<Long>(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTransaction tx = transactionFactory.newTransaction();

        long orecValue = ref.orec;
        try {
            ref.commute(tx, null);
            fail();
        } catch (NullPointerException expected) {

        }

        assertIsAborted(tx);
        assertVersionAndValue(ref, initialVersion, initialValue);
        assertOrecValue(ref, orecValue);
    }

    @Test
    public void whenNullTransaction_thenNullPointerException() {
        Long initialValue = 10L;
        GammaRef<Long> ref = new GammaRef<Long>(stm, initialValue);
        long initialVersion = ref.getVersion();

        LongFunction function = mock(LongFunction.class);

        long orecValue = ref.orec;
        try {
            ref.commute((Transaction) null, function);
            fail();
        } catch (NullPointerException expected) {
        }

        verifyZeroInteractions(function);
        assertVersionAndValue(ref, initialVersion, initialValue);
        assertOrecValue(ref, orecValue);
    }

    @Test
    public void whenTransactionAborted_thenDeadTransactionException() {
        Long initialValue = 10L;
        GammaRef<Long> ref = new GammaRef<Long>(stm, initialValue);
        long initialVersion = ref.getVersion();

        LongFunction function = mock(LongFunction.class);
        GammaTransaction tx = transactionFactory.newTransaction();
        tx.abort();

        long orecValue = ref.orec;
        try {
            ref.commute(tx, function);
            fail();
        } catch (DeadTransactionException expected) {
        }

        assertIsAborted(tx);
        verifyZeroInteractions(function);
        assertVersionAndValue(ref, initialVersion, initialValue);
        assertOrecValue(ref,orecValue);
    }

    @Test
    public void whenTransactionCommitted_thenDeadTransactionException() {
        Long initialValue = 20L;
        GammaRef<Long> ref = new GammaRef<Long>(stm, initialValue);
        long initialVersion = ref.getVersion();

        LongFunction function = mock(LongFunction.class);
        GammaTransaction tx = transactionFactory.newTransaction();
        tx.commit();

        long orecValue = ref.orec;
        try {
            ref.commute(tx, function);
            fail();
        } catch (DeadTransactionException expected) {
        }

        assertIsCommitted(tx);
        verifyZeroInteractions(function);
        assertVersionAndValue(ref, initialVersion, initialValue);
        assertOrecValue(ref, orecValue);
    }

    @Test
    public void whenTransactionPrepared_thenPreparedTransactionException() {
        Long initialValue = 10L;
        GammaRef<Long> ref = new GammaRef<Long>(stm, initialValue);
        long initialVersion = ref.getVersion();

        LongFunction function = mock(LongFunction.class);
        GammaTransaction tx = transactionFactory.newTransaction();
        tx.prepare();

        long orecValue = ref.orec;
        try {
            ref.commute(tx, function);
            fail();
        } catch (PreparedTransactionException expected) {
        }

        assertIsAborted(tx);
        verifyZeroInteractions(function);
        assertVersionAndValue(ref, initialVersion, initialValue);
        assertOrecValue(ref, orecValue);
    }

    @Test
    public void fullExample() {
        assumeTrue(!(transactionFactory.newTransaction() instanceof FatMonoGammaTransaction));

        Long initialValue = 10L;
        GammaRef<Long> ref1 = new GammaRef<Long>(stm, initialValue);
        GammaRef<Long> ref2 = new GammaRef<Long>(stm, initialValue);

        GammaTransaction tx1 = transactionFactory.newTransaction();
        ref1.openForWrite(tx1, LOCKMODE_NONE).ref_value = new Long(11);
        ref2.commute(tx1, Functions.incLongFunction(1));

        GammaTransaction tx2 = transactionFactory.newTransaction();
        ref2.openForWrite(tx2, LOCKMODE_NONE).ref_value = new Long(11);
        tx2.commit();

        tx1.commit();

        assertIsCommitted(tx1);
        assertEquals(new Long(11), ref1.atomicGet());
        assertEquals(new Long(12), ref2.atomicGet());
    }

    @Test
    public void whenListenersAvailable() {
        Long initialValue = 10L;
        GammaRef<Long> ref = new GammaRef<Long>(stm, initialValue);
        long initialVersion = ref.getVersion();

        RefAwaitThread thread = new RefAwaitThread(ref, initialValue + 1);
        thread.start();

        sleepMs(500);

        GammaTransaction tx = transactionFactory.newTransaction();
        ref.commute(tx, Functions.incLongFunction());
        tx.commit();

        joinAll(thread);

        assertRefHasNoLocks(ref);
        assertVersionAndValue(ref, initialVersion + 1, initialValue + 1);
    }
}
