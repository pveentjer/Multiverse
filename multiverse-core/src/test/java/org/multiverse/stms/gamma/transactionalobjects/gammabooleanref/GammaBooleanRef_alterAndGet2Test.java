package org.multiverse.stms.gamma.transactionalobjects.gammabooleanref;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.multiverse.api.LockMode;
import org.multiverse.api.TransactionFactory;
import org.multiverse.api.exceptions.DeadTransactionException;
import org.multiverse.api.exceptions.PreparedTransactionException;
import org.multiverse.api.exceptions.ReadWriteConflict;
import org.multiverse.api.functions.Functions;
import org.multiverse.api.functions.BooleanFunction;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactionalobjects.GammaBooleanRef;
import org.multiverse.stms.gamma.transactions.GammaTransaction;
import org.multiverse.stms.gamma.transactions.GammaTransactionFactory;
import org.multiverse.stms.gamma.transactions.fat.FatFixedLengthGammaTransactionFactory;
import org.multiverse.stms.gamma.transactions.fat.FatMonoGammaTransactionFactory;
import org.multiverse.stms.gamma.transactions.fat.FatVariableLengthGammaTransactionFactory;

import java.util.Collection;

import static java.util.Arrays.asList;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.multiverse.TestUtils.*;
import static org.multiverse.api.ThreadLocalTransaction.clearThreadLocalTransaction;
import static org.multiverse.api.ThreadLocalTransaction.getThreadLocalTransaction;
import static org.multiverse.api.functions.Functions.inverseBooleanFunction;
import static org.multiverse.stms.gamma.GammaTestUtils.*;


@RunWith(Parameterized.class)
public class GammaBooleanRef_alterAndGet2Test {
    private final GammaTransactionFactory transactionFactory;
    private final GammaStm stm;

    public GammaBooleanRef_alterAndGet2Test(GammaTransactionFactory transactionFactory) {
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
    public void whenNullTransaction_thenNullPointerException() {
        boolean initialValue = true;
        GammaBooleanRef ref = new GammaBooleanRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        BooleanFunction function = mock(BooleanFunction.class);

        try {
            ref.alterAndGet(null, function);
            fail();
        } catch (NullPointerException expected) {
        }

        verifyZeroInteractions(function);
        assertVersionAndValue(ref, initialVersion, initialValue);
    }

    @Test
    public void whenNullFunction_thenNullPointerException() {
        boolean initialValue = true;
        GammaBooleanRef ref = new GammaBooleanRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTransaction tx = transactionFactory.newTransaction();

        try {
            ref.alterAndGet(tx, null);
            fail();
        } catch (NullPointerException expected) {
        }

        assertRefHasNoLocks(ref);
        assertIsAborted(tx);
        assertVersionAndValue(ref, initialVersion, initialValue);
    }

    @Test
    public void whenCommittedTransaction_thenDeadTransactionException() {
        boolean initialValue = true;
        GammaBooleanRef ref = new GammaBooleanRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTransaction tx = transactionFactory.newTransaction();
        tx.commit();

        BooleanFunction function = mock(BooleanFunction.class);

        try {
            ref.alterAndGet(tx, function);
            fail();
        } catch (DeadTransactionException expected) {
        }

        assertIsCommitted(tx);
        assertRefHasNoLocks(ref);
        assertVersionAndValue(ref, initialVersion, initialValue);
    }

    @Test
    public void whenPreparedTransaction_thenPreparedTransactionException() {
        boolean initialValue = true;
        GammaBooleanRef ref = new GammaBooleanRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTransaction tx = transactionFactory.newTransaction();
        tx.prepare();

        BooleanFunction function = mock(BooleanFunction.class);

        try {
            ref.alterAndGet(tx, function);
            fail();
        } catch (PreparedTransactionException expected) {
        }

        assertRefHasNoLocks(ref);
        assertIsAborted(tx);
        assertVersionAndValue(ref, initialVersion, initialValue);
    }

    @Test
    public void whenAbortedTransaction() {
        boolean initialValue = true;
        GammaBooleanRef ref = new GammaBooleanRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTransaction tx = transactionFactory.newTransaction();
        tx.abort();

        BooleanFunction function = mock(BooleanFunction.class);

        try {
            ref.alterAndGet(tx, function);
            fail();
        } catch (DeadTransactionException expected) {
        }

        assertRefHasNoLocks(ref);
        assertIsAborted(tx);
        assertVersionAndValue(ref, initialVersion, initialValue);
    }

    @Test
    public void whenFunctionCausesException() {
        boolean initialValue = true;
        GammaBooleanRef ref = new GammaBooleanRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        BooleanFunction function = mock(BooleanFunction.class);
        RuntimeException ex = new RuntimeException();
        when(function.call(anyBoolean())).thenThrow(ex);

        GammaTransaction tx = transactionFactory.newTransaction();

        try {
            ref.alterAndGet(tx, function);
            fail();
        } catch (RuntimeException found) {
            assertSame(ex, found);
        }

        assertRefHasNoLocks(ref);
        assertIsAborted(tx);
        assertNull(getThreadLocalTransaction());
        assertVersionAndValue(ref, initialVersion, initialValue);
    }

    @Test
    public void whenPrivatizedByOther() {
        boolean initialValue = true;
        GammaBooleanRef ref = new GammaBooleanRef(stm, initialValue);
        long version = ref.getVersion();

        GammaTransaction otherTx = transactionFactory.newTransaction();
        ref.getLock().acquire(otherTx, LockMode.Exclusive);

        GammaTransaction tx = transactionFactory.newTransaction();
        BooleanFunction function = mock(BooleanFunction.class);

        try {
            ref.alterAndGet(tx, function);
            fail();
        } catch (ReadWriteConflict expected) {
        }

        assertSurplus(ref, 1);
        assertIsAborted(tx);
        assertRefHasExclusiveLock(ref, otherTx);
        assertVersionAndValue(ref, version, initialValue);
    }


    @Test
    public void whenEnsuredByOther_thenOperationSucceedsButCommitFails() {
        boolean initialValue = true;
        GammaBooleanRef ref = new GammaBooleanRef(stm, initialValue);
        long version = ref.getVersion();

        GammaTransaction otherTx = transactionFactory.newTransaction();
        ref.getLock().acquire(otherTx, LockMode.Write);

        GammaTransaction tx = transactionFactory.newTransaction();
        BooleanFunction function = Functions.inverseBooleanFunction();
        ref.alterAndGet(tx, function);

        try {
            tx.commit();
            fail();
        } catch (ReadWriteConflict expected) {
        }

        assertRefHasWriteLock(ref, otherTx);
        assertSurplus(ref, 1);
        assertIsActive(otherTx);
        assertIsAborted(tx);
        assertVersionAndValue(ref, version, initialValue);
    }

    @Test
    public void whenListenersAvailable_thenTheyAreNotified() {
        boolean initialValue = true;
        GammaBooleanRef ref = new GammaBooleanRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        BooleanRefAwaitThread thread = new BooleanRefAwaitThread(ref, !initialValue);
        thread.start();

        sleepMs(500);

        GammaTransaction tx = transactionFactory.newTransaction();
        ref.alterAndGet(tx, inverseBooleanFunction());
        tx.commit();

        joinAll(thread);

        assertVersionAndValue(ref, initialVersion + 1, !initialValue);
    }

    @Test
    public void whenSuccess() {
        boolean initialValue = true;
        GammaBooleanRef ref = new GammaBooleanRef(stm, initialValue);
        GammaTransaction tx = transactionFactory.newTransaction();
        boolean result = ref.alterAndGet(tx, Functions.inverseBooleanFunction());
        tx.commit();

        assertEquals(!initialValue, ref.atomicGet());
        assertEquals(!initialValue, result);
    }

}
