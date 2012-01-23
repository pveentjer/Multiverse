package org.multiverse.stms.gamma.transactionalobjects.gammabooleanref;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.multiverse.api.TransactionFactory;
import org.multiverse.api.exceptions.DeadTransactionException;
import org.multiverse.api.exceptions.PreparedTransactionException;
import org.multiverse.api.exceptions.TransactionMandatoryException;
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
import static org.multiverse.api.ThreadLocalTransaction.*;
import static org.multiverse.api.functions.Functions.identityBooleanFunction;
import static org.multiverse.api.functions.Functions.inverseBooleanFunction;
import static org.multiverse.stms.gamma.GammaTestUtils.*;

@RunWith(Parameterized.class)
public class GammaBooleanRef_alterAndGet1Test {
    private final GammaTransactionFactory transactionFactory;
    private final GammaStm stm;

    public GammaBooleanRef_alterAndGet1Test(GammaTransactionFactory transactionFactory) {
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
    public void whenActiveTransactionAvailableAndNullFunction_thenNullPointerException() {
        GammaBooleanRef ref = new GammaBooleanRef(stm);

        GammaTransaction tx = transactionFactory.newTransaction();
        setThreadLocalTransaction(tx);

        try {
            ref.alterAndGet(null);
            fail();
        } catch (NullPointerException expected) {
        }

        assertIsAborted(tx);
        assertSame(tx, getThreadLocalTransaction());
    }

    @Test
    public void whenFunctionCausesException() {
        GammaBooleanRef ref = new GammaBooleanRef(stm);

        BooleanFunction function = mock(BooleanFunction.class);
        RuntimeException ex = new RuntimeException();
        when(function.call(anyBoolean())).thenThrow(ex);

        GammaTransaction tx = transactionFactory.newTransaction();
        setThreadLocalTransaction(tx);

        try {
            ref.alterAndGet(function);
            fail();
        } catch (RuntimeException found) {
            assertSame(ex, found);
        }

        assertIsAborted(tx);
        assertSame(tx, getThreadLocalTransaction());
    }

    @Test
    public void whenActiveTransactionAvailable() {
        boolean initialValue = true;
        GammaBooleanRef ref = new GammaBooleanRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTransaction tx = transactionFactory.newTransaction();
        setThreadLocalTransaction(tx);

        BooleanFunction function = Functions.inverseBooleanFunction();
        ref.alterAndGet(function);
        assertEquals(!initialValue, ref.get());
        assertVersionAndValue(ref, initialVersion, initialValue);
        tx.commit();

        assertVersionAndValue(ref, initialVersion + 1, !initialValue);
    }

    @Test
    public void whenActiveTransactionAvailableButNoChange_thenNoWrite() {
        boolean initialValue = true;
        GammaBooleanRef ref = new GammaBooleanRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTransaction tx = transactionFactory.newTransaction();
        setThreadLocalTransaction(tx);

        BooleanFunction function = identityBooleanFunction();
        ref.alterAndGet(function);
        assertEquals(initialValue, ref.get());
        assertVersionAndValue(ref, initialVersion, initialValue);
        tx.commit();

        assertVersionAndValue(ref, initialVersion, initialValue);
    }

    @Test
    public void whenPreparedTransactionAvailable_thenPreparedTransactionException() {
        boolean initialValue = true;
        GammaBooleanRef ref = new GammaBooleanRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        BooleanFunction function = mock(BooleanFunction.class);
        GammaTransaction tx = transactionFactory.newTransaction();
        tx.prepare();
        setThreadLocalTransaction(tx);

        try {
            ref.alterAndGet(function);
            fail();
        } catch (PreparedTransactionException expected) {

        }

        assertIsAborted(tx);
        verifyZeroInteractions(function);
        assertVersionAndValue(ref, initialVersion, initialValue);
    }

    @Test
    public void whenNoTransactionAvailable_thenNoTransactionFoundException() {
        boolean initialValue = true;
        GammaBooleanRef ref = new GammaBooleanRef(stm, initialValue);
        long initialVersion = ref.getVersion();
        BooleanFunction function = Functions.inverseBooleanFunction();

        try {
            ref.alterAndGet(function);
            fail();
        } catch (TransactionMandatoryException expected) {

        }

        assertVersionAndValue(ref, initialVersion, initialValue);
        assertNull(getThreadLocalTransaction());
        assertSurplus(ref, 0);
        assertRefHasNoLocks(ref);
    }

    @Test
    public void whenCommittedTransactionAvailable_thenDeadTransactionException() {
        GammaTransaction tx = transactionFactory.newTransaction();
        setThreadLocalTransaction(tx);
        tx.commit();

        boolean initialValue = true;
        GammaBooleanRef ref = new GammaBooleanRef(stm, initialValue);
        long initialVersion = ref.getVersion();
        BooleanFunction function = Functions.inverseBooleanFunction();

        try {
            ref.alterAndGet(function);
            fail();
        } catch (DeadTransactionException expected) {

        }

        assertIsCommitted(tx);
        assertSame(tx, getThreadLocalTransaction());
        assertSurplus(ref, 0);
        assertRefHasNoLocks(ref);
        assertVersionAndValue(ref, initialVersion, initialValue);
    }

    @Test
    public void whenAbortedTransactionAvailable_thenDeadTransactionException() {
        GammaTransaction tx = transactionFactory.newTransaction();
        setThreadLocalTransaction(tx);
        tx.abort();

        boolean initialValue = true;
        GammaBooleanRef ref = new GammaBooleanRef(stm, initialValue);
        long initialVersion = ref.getVersion();
        BooleanFunction function = inverseBooleanFunction();

        try {
            ref.alterAndGet(function);
            fail();
        } catch (DeadTransactionException expected) {
        }

        assertIsAborted(tx);
        assertSame(tx, getThreadLocalTransaction());
        assertSurplus(ref, 0);
        assertRefHasNoLocks(ref);
        assertVersionAndValue(ref, initialVersion, initialValue);
    }

    @Test
    public void whenListenersAvailable_thenTheyAreNotified() {
        boolean initialValue = true;
        GammaBooleanRef ref = new GammaBooleanRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        BooleanRefAwaitThread thread = new BooleanRefAwaitThread(ref, !initialValue);
        thread.start();

        sleepMs(500);

        GammaTransaction tx = stm.newDefaultTransaction();
        setThreadLocalTransaction(tx);
        ref.alterAndGet(inverseBooleanFunction());
        tx.commit();

        joinAll(thread);

        assertVersionAndValue(ref, initialVersion + 1, !initialValue);
    }
}
