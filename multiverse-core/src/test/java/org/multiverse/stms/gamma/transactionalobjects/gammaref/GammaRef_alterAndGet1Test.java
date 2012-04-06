package org.multiverse.stms.gamma.transactionalobjects.gammaref;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Matchers;
import org.multiverse.api.TxnFactory;
import org.multiverse.api.exceptions.DeadTxnException;
import org.multiverse.api.exceptions.PreparedTxnException;
import org.multiverse.api.exceptions.TxnMandatoryException;
import org.multiverse.api.functions.Function;
import org.multiverse.api.functions.Functions;
import org.multiverse.api.functions.LongFunction;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactionalobjects.GammaRef;
import org.multiverse.stms.gamma.transactions.GammaTxn;
import org.multiverse.stms.gamma.transactions.GammaTxnFactory;
import org.multiverse.stms.gamma.transactions.fat.FatFixedLengthGammaTxnFactory;
import org.multiverse.stms.gamma.transactions.fat.FatMonoGammaTxnFactory;
import org.multiverse.stms.gamma.transactions.fat.FatVariableLengthGammaTxnFactory;

import java.util.Collection;

import static java.util.Arrays.asList;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.multiverse.TestUtils.*;
import static org.multiverse.api.TxnThreadLocal.*;
import static org.multiverse.api.functions.Functions.identityLongFunction;
import static org.multiverse.api.functions.Functions.incLongFunction;
import static org.multiverse.stms.gamma.GammaTestUtils.*;

@RunWith(Parameterized.class)
public class GammaRef_alterAndGet1Test {

    private final GammaTxnFactory transactionFactory;
    private final GammaStm stm;

    public GammaRef_alterAndGet1Test(GammaTxnFactory transactionFactory) {
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
    public void whenActiveTransactionAvailableAndNullFunction_thenNullPointerException() {
        GammaRef<Long> ref = new GammaRef<Long>(stm);

        GammaTxn tx = transactionFactory.newTransaction();
        setThreadLocalTxn(tx);

        try {
            ref.alterAndGet(null);
            fail();
        } catch (NullPointerException expected) {
        }

        assertIsAborted(tx);
        assertSame(tx, getThreadLocalTxn());
    }

    @Test
    public void whenFunctionCausesException() {
        GammaRef<Long> ref = new GammaRef<Long>(stm);

        Function<Long> function = mock(Function.class);
        RuntimeException ex = new RuntimeException();
        when(function.call(Matchers.<Long>anyObject())).thenThrow(ex);

        GammaTxn tx = transactionFactory.newTransaction();
        setThreadLocalTxn(tx);

        try {
            ref.alterAndGet(function);
            fail();
        } catch (RuntimeException found) {
            assertSame(ex, found);
        }

        assertIsAborted(tx);
        assertSame(tx, getThreadLocalTxn());
    }

    @Test
    public void whenActiveTransactionAvailable() {
        Long initialValue = 10L;
        GammaRef<Long> ref = new GammaRef<Long>(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTxn tx = transactionFactory.newTransaction();
        setThreadLocalTxn(tx);

        Function<Long> function = Functions.incLongFunction();
        ref.alterAndGet(function);
        assertEquals(new Long(initialValue + 1L), ref.get());
        assertVersionAndValue(ref, initialVersion, initialValue);
        tx.commit();

        assertVersionAndValue(ref, initialVersion + 1, initialValue + 1);
    }

    @Test
    public void whenActiveTransactionAvailableButNoChange_thenNoWrite() {
        Long initialValue = 10L;
        GammaRef<Long> ref = new GammaRef<Long>(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTxn tx = transactionFactory.newTransaction();
        setThreadLocalTxn(tx);

        Function<Long> function = identityLongFunction();
        ref.alterAndGet(function);
        assertEquals(initialValue, ref.get());
        assertVersionAndValue(ref, initialVersion, initialValue);
        tx.commit();

        assertVersionAndValue(ref, initialVersion, initialValue);
    }

    @Test
    public void whenPreparedTransactionAvailable_thenPreparedTxnException() {
        Long initialValue = 10L;
        GammaRef<Long> ref = new GammaRef<Long>(stm, initialValue);
        long initialVersion = ref.getVersion();

        Function<Long> function = mock(LongFunction.class);
        GammaTxn tx = transactionFactory.newTransaction();
        tx.prepare();
        setThreadLocalTxn(tx);

        try {
            ref.alterAndGet(function);
            fail();
        } catch (PreparedTxnException expected) {

        }

        assertIsAborted(tx);
        verifyZeroInteractions(function);
        assertVersionAndValue(ref, initialVersion, initialValue);
    }

    @Test
    public void whenNoTransactionAvailable_thenNoTransactionFoundException() {
        Long initialValue = 10L;
        GammaRef<Long> ref = new GammaRef<Long>(stm, initialValue);
        long initialVersion = ref.getVersion();
        Function<Long> function = Functions.incLongFunction(1);

        try {
            ref.alterAndGet(function);
            fail();
        } catch (TxnMandatoryException expected) {

        }

        assertVersionAndValue(ref, initialVersion, initialValue);
        assertNull(getThreadLocalTxn());
        assertSurplus(ref, 0);
        assertRefHasNoLocks(ref);
    }

    @Test
    public void whenCommittedTransactionAvailable_thenDeadTxnException() {
        GammaTxn tx = transactionFactory.newTransaction();
        setThreadLocalTxn(tx);
        tx.commit();

        long initialValue = 10;
        GammaRef<Long> ref = new GammaRef<Long>(stm, initialValue);
        long initialVersion = ref.getVersion();
        Function<Long> function = Functions.incLongFunction(1);

        try {
            ref.alterAndGet(function);
            fail();
        } catch (DeadTxnException expected) {

        }

        assertIsCommitted(tx);
        assertSame(tx, getThreadLocalTxn());
        assertSurplus(ref, 0);
        assertRefHasNoLocks(ref);
        assertVersionAndValue(ref, initialVersion, initialValue);
    }

    @Test
    public void whenAbortedTransactionAvailable_thenDeadTxnException() {
        GammaTxn tx = transactionFactory.newTransaction();
        setThreadLocalTxn(tx);
        tx.abort();

        Long initialValue = 10L;
        GammaRef<Long> ref = new GammaRef<Long>(stm, initialValue);
        long initialVersion = ref.getVersion();
        Function<Long> function = incLongFunction(1);

        try {
            ref.alterAndGet(function);
            fail();
        } catch (DeadTxnException expected) {
        }

        assertIsAborted(tx);
        assertSame(tx, getThreadLocalTxn());
        assertSurplus(ref, 0);
        assertRefHasNoLocks(ref);
        assertVersionAndValue(ref, initialVersion, initialValue);
    }

    @Test
    public void whenListenersAvailable_thenTheyAreNotified() {
        Long initialValue = 10L;
        GammaRef<Long> ref = new GammaRef<Long>(stm, initialValue);
        long initialVersion = ref.getVersion();

        RefAwaitThread thread = new RefAwaitThread<Long>(ref, initialValue + 1L);
        thread.start();

        sleepMs(500);

        GammaTxn tx = stm.newDefaultTxn();
        setThreadLocalTxn(tx);
        ref.alterAndGet(Functions.incLongFunction());
        tx.commit();

        joinAll(thread);

        assertVersionAndValue(ref, initialVersion + 1, initialValue + 1);
    }
}
