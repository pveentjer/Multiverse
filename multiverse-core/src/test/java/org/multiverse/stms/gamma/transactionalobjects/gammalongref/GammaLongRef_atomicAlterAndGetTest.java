package org.multiverse.stms.gamma.transactionalobjects.gammalongref;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.LockMode;
import org.multiverse.api.exceptions.LockedException;
import org.multiverse.api.functions.Functions;
import org.multiverse.api.functions.LongFunction;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.GammaStmConfiguration;
import org.multiverse.stms.gamma.transactionalobjects.GammaLongRef;
import org.multiverse.stms.gamma.transactions.GammaTxn;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.*;
import static org.multiverse.TestUtils.*;
import static org.multiverse.api.TxnThreadLocal.*;
import static org.multiverse.api.functions.Functions.identityLongFunction;
import static org.multiverse.api.functions.Functions.incLongFunction;
import static org.multiverse.stms.gamma.GammaTestUtils.*;

public class GammaLongRef_atomicAlterAndGetTest {

    private GammaStm stm;

    @Before
    public void setUp() {
        GammaStmConfiguration config = new GammaStmConfiguration();
        config.maxRetries = 10;
        stm = new GammaStm(config);
        clearThreadLocalTxn();
    }

    @Test
    public void whenFunctionCausesException() {
        long initialValue = 10;
        GammaLongRef ref = new GammaLongRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        LongFunction function = mock(LongFunction.class);
        RuntimeException ex = new RuntimeException();
        when(function.call(anyLong())).thenThrow(ex);

        try {
            ref.atomicAlterAndGet(function);
            fail();
        } catch (RuntimeException found) {
            assertSame(ex, found);
        }

        assertVersionAndValue(ref, initialVersion, initialValue);
        assertRefHasNoLocks(ref);
        assertSurplus(ref, 0);
        assertNull(getThreadLocalTxn());
    }

    @Test
    public void whenNullFunction_thenNullPointerException() {
        int initialValue = 5;
        GammaLongRef ref = new GammaLongRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        try {
            ref.atomicAlterAndGet(null);
            fail();
        } catch (NullPointerException expected) {
        }

        assertRefHasNoLocks(ref);
        assertSurplus(ref, 0);
        assertVersionAndValue(ref, initialVersion, initialValue);
    }

    @Test
    public void whenSuccess() {
        int initialValue = 5;
        GammaLongRef ref = new GammaLongRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        LongFunction function = Functions.incLongFunction(1);

        long result = ref.atomicAlterAndGet(function);

        assertEquals(initialValue + 1, result);
        assertRefHasNoLocks(ref);
        assertSurplus(ref, 0);
        assertVersionAndValue(ref, initialVersion + 1, initialValue + 1);
    }

    @Test
    public void whenNoChange() {
        int initialValue = 5;
        GammaLongRef ref = new GammaLongRef(stm, initialValue);
        long version = ref.getVersion();

        LongFunction function = identityLongFunction();

        long result = ref.atomicAlterAndGet(function);

        assertEquals(initialValue, result);
        assertRefHasNoLocks(ref);
        assertSurplus(ref, 0);
        assertVersionAndValue(ref, version, initialValue);
    }

    @Test
    public void whenActiveTransactionAvailable_thenIgnored() {
        long initialValue = 5;
        GammaLongRef ref = new GammaLongRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTxn tx = stm.newDefaultTransaction();
        setThreadLocalTxn(tx);
        ref.set(tx, 100);

        LongFunction function = incLongFunction(1);

        long result = ref.atomicAlterAndGet(function);

        assertEquals(initialValue + 1, result);
        assertRefHasNoLocks(ref);
        assertSurplus(ref, 0);
        assertVersionAndValue(ref, initialVersion + 1, initialValue + 1);
        assertIsActive(tx);
        assertSame(tx, getThreadLocalTxn());
    }

    @Test
    public void whenEnsuredByOther_thenLockedException() {
        int initialValue = 5;
        GammaLongRef ref = new GammaLongRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTxn otherTx = stm.newDefaultTransaction();
        ref.getLock().acquire(otherTx, LockMode.Write);

        LongFunction function = mock(LongFunction.class);
        try {
            ref.atomicAlterAndGet(function);
            fail();
        } catch (LockedException expected) {
        }

        verifyZeroInteractions(function);
        assertSurplus(ref, 1);
        assertRefHasWriteLock(ref, otherTx);
        assertWriteBiased(ref);
        assertVersionAndValue(ref, initialVersion, initialValue);
    }

    @Test
    public void whenPrivatizedByOther() {
        int initialValue = 5;
        GammaLongRef ref = new GammaLongRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTxn otherTx = stm.newDefaultTransaction();
        ref.getLock().acquire(otherTx, LockMode.Exclusive);

        LongFunction function = mock(LongFunction.class);
        try {
            ref.atomicAlterAndGet(function);
            fail();
        } catch (LockedException expected) {
        }

        verifyZeroInteractions(function);
        assertSurplus(ref, 1);
        assertRefHasExclusiveLock(ref, otherTx);
        assertWriteBiased(ref);
        assertVersionAndValue(ref, initialVersion, initialValue);
    }

    @Test
    public void whenListenersAvailable() {
        long initialValue = 10;
        GammaLongRef ref = new GammaLongRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        LongRefAwaitThread thread = new LongRefAwaitThread(ref, initialValue + 1);
        thread.start();

        sleepMs(500);

        ref.atomicAlterAndGet(Functions.incLongFunction());

        joinAll(thread);
        assertRefHasNoLocks(ref);
        assertVersionAndValue(ref, initialVersion + 1, initialValue + 1);
    }
}
