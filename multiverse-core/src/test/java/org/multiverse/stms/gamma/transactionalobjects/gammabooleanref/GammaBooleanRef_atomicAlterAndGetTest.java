package org.multiverse.stms.gamma.transactionalobjects.gammabooleanref;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.LockMode;
import org.multiverse.api.exceptions.LockedException;
import org.multiverse.api.functions.BooleanFunction;
import org.multiverse.api.functions.Functions;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.GammaStmConfiguration;
import org.multiverse.stms.gamma.transactionalobjects.GammaBooleanRef;
import org.multiverse.stms.gamma.transactions.GammaTransaction;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.multiverse.TestUtils.*;
import static org.multiverse.api.ThreadLocalTransaction.*;
import static org.multiverse.api.functions.Functions.identityBooleanFunction;
import static org.multiverse.api.functions.Functions.inverseBooleanFunction;
import static org.multiverse.stms.gamma.GammaTestUtils.*;

public class GammaBooleanRef_atomicAlterAndGetTest {

    private GammaStm stm;

    @Before
    public void setUp() {
        GammaStmConfiguration config = new GammaStmConfiguration();
        config.maxRetries = 10;
        stm = new GammaStm(config);
        clearThreadLocalTransaction();
    }

    @Test
    public void whenFunctionCausesException() {
        boolean initialValue = true;
        GammaBooleanRef ref = new GammaBooleanRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        BooleanFunction function = mock(BooleanFunction.class);
        RuntimeException ex = new RuntimeException();
        when(function.call(anyBoolean())).thenThrow(ex);

        try {
            ref.atomicAlterAndGet(function);
            fail();
        } catch (RuntimeException found) {
            assertSame(ex, found);
        }

        assertVersionAndValue(ref, initialVersion, initialValue);
        assertRefHasNoLocks(ref);
        assertSurplus(ref, 0);
        assertNull(getThreadLocalTransaction());
    }

    @Test
    public void whenNullFunction_thenNullPointerException() {
        boolean initialValue = true;
        GammaBooleanRef ref = new GammaBooleanRef(stm, initialValue);
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
        boolean initialValue = true;
        GammaBooleanRef ref = new GammaBooleanRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        BooleanFunction function = Functions.inverseBooleanFunction();

        boolean result = ref.atomicAlterAndGet(function);

        assertEquals(!initialValue, result);
        assertRefHasNoLocks(ref);
        assertSurplus(ref, 0);
        assertVersionAndValue(ref, initialVersion + 1, !initialValue);
    }

    @Test
    public void whenNoChange() {
        boolean initialValue = true;
        GammaBooleanRef ref = new GammaBooleanRef(stm, initialValue);
        long version = ref.getVersion();

        BooleanFunction function = identityBooleanFunction();

        boolean result = ref.atomicAlterAndGet(function);

        assertEquals(initialValue, result);
        assertRefHasNoLocks(ref);
        assertSurplus(ref, 0);
        assertVersionAndValue(ref, version, initialValue);
    }

    @Test
    public void whenActiveTransactionAvailable_thenIgnored() {
        boolean initialValue = true;
        GammaBooleanRef ref = new GammaBooleanRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTransaction tx = stm.newDefaultTransaction();
        setThreadLocalTransaction(tx);
        ref.set(tx, !initialValue);

        BooleanFunction function = inverseBooleanFunction();

        boolean result = ref.atomicAlterAndGet(function);

        assertEquals(!initialValue, result);
        assertRefHasNoLocks(ref);
        assertSurplus(ref, 0);
        assertVersionAndValue(ref, initialVersion + 1, !initialValue);
        assertIsActive(tx);
        assertSame(tx, getThreadLocalTransaction());
    }

    @Test
    public void whenEnsuredByOther_thenLockedException() {
        boolean initialValue = true;
        GammaBooleanRef ref = new GammaBooleanRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTransaction otherTx = stm.newDefaultTransaction();
        ref.getLock().acquire(otherTx, LockMode.Write);

        BooleanFunction function = mock(BooleanFunction.class);
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
        boolean initialValue = true;
        GammaBooleanRef ref = new GammaBooleanRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTransaction otherTx = stm.newDefaultTransaction();
        ref.getLock().acquire(otherTx, LockMode.Exclusive);

        BooleanFunction function = mock(BooleanFunction.class);
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
        boolean initialValue = true;
        GammaBooleanRef ref = new GammaBooleanRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        BooleanRefAwaitThread thread = new BooleanRefAwaitThread(ref, !initialValue);
        thread.start();

        sleepMs(500);

        ref.atomicAlterAndGet(inverseBooleanFunction());

        joinAll(thread);
        assertRefHasNoLocks(ref);
        assertVersionAndValue(ref, initialVersion + 1, !initialValue);
    }
}
