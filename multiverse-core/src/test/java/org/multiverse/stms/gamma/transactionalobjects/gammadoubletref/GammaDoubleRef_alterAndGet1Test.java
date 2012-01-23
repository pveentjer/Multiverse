package org.multiverse.stms.gamma.transactionalobjects.gammadoubletref;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.multiverse.api.TransactionFactory;
import org.multiverse.api.exceptions.DeadTransactionException;
import org.multiverse.api.exceptions.PreparedTransactionException;
import org.multiverse.api.exceptions.TransactionMandatoryException;
import org.multiverse.api.functions.Functions;
import org.multiverse.api.functions.DoubleFunction;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactionalobjects.GammaDoubleRef;
import org.multiverse.stms.gamma.transactions.GammaTransaction;
import org.multiverse.stms.gamma.transactions.GammaTransactionFactory;
import org.multiverse.stms.gamma.transactions.fat.FatFixedLengthGammaTransactionFactory;
import org.multiverse.stms.gamma.transactions.fat.FatMonoGammaTransactionFactory;
import org.multiverse.stms.gamma.transactions.fat.FatVariableLengthGammaTransactionFactory;

import java.util.Collection;

import static java.util.Arrays.asList;
import static org.junit.Assert.*;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.*;
import static org.multiverse.TestUtils.*;
import static org.multiverse.api.ThreadLocalTransaction.*;
import static org.multiverse.api.functions.Functions.identityDoubleFunction;
import static org.multiverse.api.functions.Functions.incDoubleFunction;
import static org.multiverse.stms.gamma.GammaTestUtils.*;

@RunWith(Parameterized.class)
public class GammaDoubleRef_alterAndGet1Test {
    private final GammaTransactionFactory transactionFactory;
    private final GammaStm stm;

    public GammaDoubleRef_alterAndGet1Test(GammaTransactionFactory transactionFactory) {
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
        GammaDoubleRef ref = new GammaDoubleRef(stm);

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
        GammaDoubleRef ref = new GammaDoubleRef(stm);

        DoubleFunction function = mock(DoubleFunction.class);
        RuntimeException ex = new RuntimeException();
        when(function.call(anyLong())).thenThrow(ex);

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
        double initialValue = 10;
        GammaDoubleRef ref = new GammaDoubleRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTransaction tx = transactionFactory.newTransaction();
        setThreadLocalTransaction(tx);

        DoubleFunction function = incDoubleFunction();
        ref.alterAndGet(function);
        assertEqualsDouble(initialValue + 1, ref.get());
        assertVersionAndValue(ref, initialVersion, initialValue);
        tx.commit();

        assertVersionAndValue(ref, initialVersion + 1, initialValue + 1);
    }

    @Test
    public void whenActiveTransactionAvailableButNoChange_thenNoWrite() {
        double initialValue = 10;
        GammaDoubleRef ref = new GammaDoubleRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTransaction tx = transactionFactory.newTransaction();
        setThreadLocalTransaction(tx);

        DoubleFunction function = identityDoubleFunction();
        ref.alterAndGet(function);
        assertEqualsDouble(initialValue, ref.get());
        assertVersionAndValue(ref, initialVersion, initialValue);
        tx.commit();

        assertVersionAndValue(ref, initialVersion, initialValue);
    }

    @Test
    public void whenPreparedTransactionAvailable_thenPreparedTransactionException() {
        double initialValue = 10;
        GammaDoubleRef ref = new GammaDoubleRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        DoubleFunction function = mock(DoubleFunction.class);
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
        double initialValue = 10;
        GammaDoubleRef ref = new GammaDoubleRef(stm, initialValue);
        long initialVersion = ref.getVersion();
        DoubleFunction function = Functions.incDoubleFunction();

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

        double initialValue = 10;
        GammaDoubleRef ref = new GammaDoubleRef(stm, initialValue);
        long initialVersion = ref.getVersion();
        DoubleFunction function = Functions.incDoubleFunction();

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

        double initialValue = 10;
        GammaDoubleRef ref = new GammaDoubleRef(stm, initialValue);
        long initialVersion = ref.getVersion();
        DoubleFunction function = incDoubleFunction();

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
        double initialValue = 10;
        GammaDoubleRef ref = new GammaDoubleRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaDoubleRefAwaitThread thread = new GammaDoubleRefAwaitThread(ref, initialValue + 1);
        thread.start();

        sleepMs(500);

        GammaTransaction tx = stm.newDefaultTransaction();
        setThreadLocalTransaction(tx);
        ref.alterAndGet(incDoubleFunction());
        tx.commit();

        joinAll(thread);

        assertVersionAndValue(ref, initialVersion + 1, initialValue + 1);
    }
}
