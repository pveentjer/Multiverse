package org.multiverse.stms.gamma.transactionalobjects.gammaintref;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.multiverse.SomeUncheckedException;
import org.multiverse.api.TransactionFactory;
import org.multiverse.api.exceptions.DeadTransactionException;
import org.multiverse.api.exceptions.PreparedTransactionException;
import org.multiverse.api.functions.Functions;
import org.multiverse.api.functions.IntFunction;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactionalobjects.GammaIntRef;
import org.multiverse.stms.gamma.transactions.GammaTransaction;
import org.multiverse.stms.gamma.transactions.GammaTransactionFactory;
import org.multiverse.stms.gamma.transactions.fat.FatFixedLengthGammaTransactionFactory;
import org.multiverse.stms.gamma.transactions.fat.FatMonoGammaTransactionFactory;
import org.multiverse.stms.gamma.transactions.fat.FatVariableLengthGammaTransactionFactory;

import java.util.Collection;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;
import static org.multiverse.TestUtils.*;
import static org.multiverse.api.ThreadLocalTransaction.clearThreadLocalTransaction;
import static org.multiverse.stms.gamma.GammaTestUtils.assertRefHasNoLocks;
import static org.multiverse.stms.gamma.GammaTestUtils.assertVersionAndValue;

@RunWith(Parameterized.class)
public class GammaIntRef_getAndAlter2Test {

    private final GammaTransactionFactory transactionFactory;
    private final GammaStm stm;

    public GammaIntRef_getAndAlter2Test(GammaTransactionFactory transactionFactory) {
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
        GammaIntRef ref = new GammaIntRef(stm, 10);
        long version = ref.getVersion();
        IntFunction function = mock(IntFunction.class);

        try {
            ref.getAndAlter(null, function);
            fail();
        } catch (NullPointerException expected) {
        }

        verifyZeroInteractions(function);
        assertVersionAndValue(ref, version, 10);
    }

    @Test
    public void whenNullFunction_thenNullPointerException() {
        GammaIntRef ref = new GammaIntRef(stm, 10);
        long version = ref.getVersion();

        GammaTransaction tx = transactionFactory.newTransaction();

        try {
            ref.getAndAlter(tx, null);
            fail();
        } catch (NullPointerException expected) {
        }

        assertIsAborted(tx);
        assertVersionAndValue(ref, version, 10);
    }

    @Test
    public void whenCommittedTransaction_thenDeadTransactionException() {
        GammaIntRef ref = new GammaIntRef(stm, 10);
        long version = ref.getVersion();

        GammaTransaction tx = transactionFactory.newTransaction();
        tx.commit();

        IntFunction function = mock(IntFunction.class);

        try {
            ref.getAndAlter(tx, function);
            fail();
        } catch (DeadTransactionException expected) {
        }

        assertIsCommitted(tx);
        assertVersionAndValue(ref, version, 10);
    }

    @Test
    public void whenPreparedTransaction_thenPreparedTransactionException() {
        GammaIntRef ref = new GammaIntRef(stm, 10);
        long version = ref.getVersion();

        GammaTransaction tx = transactionFactory.newTransaction();
        tx.prepare();

        IntFunction function = mock(IntFunction.class);

        try {
            ref.getAndAlter(tx, function);
            fail();
        } catch (PreparedTransactionException expected) {
        }

        assertIsAborted(tx);
        assertVersionAndValue(ref, version, 10);
    }

    @Test
    public void whenAbortedTransaction() {
        GammaIntRef ref = new GammaIntRef(stm, 10);
        long version = ref.getVersion();

        GammaTransaction tx = transactionFactory.newTransaction();
        tx.abort();

        IntFunction function = mock(IntFunction.class);

        try {
            ref.getAndAlter(tx, function);
            fail();
        } catch (DeadTransactionException expected) {
        }

        assertIsAborted(tx);
        assertVersionAndValue(ref, version, 10);
    }

    @Test
    public void whenAlterFunctionCausesProblems_thenTransactionAborted() {
        int initialValue = 10;
        GammaIntRef ref = new GammaIntRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        IntFunction function = mock(IntFunction.class);
        when(function.call(initialValue)).thenThrow(new SomeUncheckedException());

        GammaTransaction tx = transactionFactory.newTransaction();
        try {
            ref.getAndAlter(tx, function);
            fail();
        } catch (SomeUncheckedException expected) {

        }

        assertIsAborted(tx);
        assertRefHasNoLocks(ref);
        assertVersionAndValue(ref, initialVersion, initialValue);
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
        long result = ref.getAndAlter(tx, Functions.incIntFunction());
        tx.commit();

        joinAll(thread);

        assertEquals(result, initialValue);
        assertRefHasNoLocks(ref);
        assertVersionAndValue(ref, initialVersion + 1, initialValue + 1);
    }

    @Test
    public void whenSuccess() {
        IntFunction function = new IntFunction() {
            @Override
            public int call(int current) {
                return current + 1;
            }
        };

        GammaIntRef ref = new GammaIntRef(stm, 100);
        GammaTransaction tx = transactionFactory.newTransaction();
        long result = ref.getAndAlter(tx, function);
        tx.commit();

        assertEquals(101, ref.atomicGet());
        assertEquals(100, result);
    }
}
