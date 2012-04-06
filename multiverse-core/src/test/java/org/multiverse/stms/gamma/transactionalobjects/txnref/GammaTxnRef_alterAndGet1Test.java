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
import org.multiverse.stms.gamma.transactionalobjects.GammaTxnRef;
import org.multiverse.stms.gamma.transactionalobjects.txnref.TxnRefAwaitThread;
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
public class GammaTxnRef_alterAndGet1Test {

    private final GammaTxnFactory transactionFactory;
    private final GammaStm stm;

    public GammaTxnRef_alterAndGet1Test(GammaTxnFactory transactionFactory) {
        this.transactionFactory = transactionFactory;
        this.stm = transactionFactory.getConfig().getStm();
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
        GammaTxnRef<Long> ref = new GammaTxnRef<Long>(stm);

        GammaTxn tx = transactionFactory.newTxn();
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
        GammaTxnRef<Long> ref = new GammaTxnRef<Long>(stm);

        Function<Long> function = mock(Function.class);
        RuntimeException ex = new RuntimeException();
        when(function.call(Matchers.<Long>anyObject())).thenThrow(ex);

        GammaTxn tx = transactionFactory.newTxn();
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
        GammaTxnRef<Long> ref = new GammaTxnRef<Long>(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTxn tx = transactionFactory.newTxn();
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
        GammaTxnRef<Long> ref = new GammaTxnRef<Long>(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTxn tx = transactionFactory.newTxn();
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
        GammaTxnRef<Long> ref = new GammaTxnRef<Long>(stm, initialValue);
        long initialVersion = ref.getVersion();

        Function<Long> function = mock(LongFunction.class);
        GammaTxn tx = transactionFactory.newTxn();
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
        GammaTxnRef<Long> ref = new GammaTxnRef<Long>(stm, initialValue);
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
        GammaTxn tx = transactionFactory.newTxn();
        setThreadLocalTxn(tx);
        tx.commit();

        long initialValue = 10;
        GammaTxnRef<Long> ref = new GammaTxnRef<Long>(stm, initialValue);
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
        GammaTxn tx = transactionFactory.newTxn();
        setThreadLocalTxn(tx);
        tx.abort();

        Long initialValue = 10L;
        GammaTxnRef<Long> ref = new GammaTxnRef<Long>(stm, initialValue);
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
        GammaTxnRef<Long> ref = new GammaTxnRef<Long>(stm, initialValue);
        long initialVersion = ref.getVersion();

        TxnRefAwaitThread thread = new TxnRefAwaitThread<Long>(ref, initialValue + 1L);
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
