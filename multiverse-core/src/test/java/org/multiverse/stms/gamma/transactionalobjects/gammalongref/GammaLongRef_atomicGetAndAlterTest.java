package org.multiverse.stms.gamma.transactionalobjects.gammalongref;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.LockMode;
import org.multiverse.api.exceptions.LockedException;
import org.multiverse.api.functions.Functions;
import org.multiverse.api.functions.LongFunction;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.GammaStmConfig;
import org.multiverse.stms.gamma.transactionalobjects.GammaLongRef;
import org.multiverse.stms.gamma.transactions.GammaTxn;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.multiverse.TestUtils.joinAll;
import static org.multiverse.TestUtils.sleepMs;
import static org.multiverse.api.TxnThreadLocal.clearThreadLocalTxn;
import static org.multiverse.api.TxnThreadLocal.setThreadLocalTxn;
import static org.multiverse.api.functions.Functions.identityLongFunction;
import static org.multiverse.stms.gamma.GammaTestUtils.*;

public class GammaLongRef_atomicGetAndAlterTest {

    private GammaStm stm;

    @Before
    public void setUp() {
        GammaStmConfig config = new GammaStmConfig();
        config.maxRetries = 10;
        stm = new GammaStm(config);
        clearThreadLocalTxn();
    }

    @Test
    public void whenSuccess() {
        long initialValue = 2;
        GammaLongRef ref = new GammaLongRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        LongFunction function = Functions.incLongFunction();
        long result = ref.atomicGetAndAlter(function);

        assertEquals(2, result);
        assertRefHasNoLocks(ref);
        assertWriteBiased(ref);
        assertSurplus(ref, 0);
        assertVersionAndValue(ref, initialVersion + 1, initialValue + 1);
    }

    @Test
    public void whenNullFunction_thenNullPointerException() {
        long initialValue = 10;
        GammaLongRef ref = new GammaLongRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        try {
            ref.atomicGetAndAlter(null);
            fail();
        } catch (NullPointerException expected) {
        }

        assertVersionAndValue(ref, initialVersion, initialValue);
        assertRefHasNoLocks(ref);
        assertSurplus(ref, 0);
        assertWriteBiased(ref);
    }

    @Test
    public void whenActiveTransactionAvailable_thenIgnored() {
        long initialValue = 2;
        GammaLongRef ref = new GammaLongRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTxn tx = stm.newDefaultTxn();
        setThreadLocalTxn(tx);
        ref.set(10);

        LongFunction function = Functions.incLongFunction();
        long result = ref.atomicGetAndAlter(function);

        tx.abort();

        assertEquals(initialValue, result);
        assertRefHasNoLocks(ref);
        assertWriteBiased(ref);
        assertSurplus(ref, 0);
        assertVersionAndValue(ref, initialVersion + 1, initialValue + 1);
    }

    @Test
    public void whenPrivatizedByOther_thenLockedException() {
        long initialValue = 2;
        GammaLongRef ref = new GammaLongRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTxn otherTx = stm.newDefaultTxn();
        ref.getLock().acquire(otherTx, LockMode.Exclusive);

        LongFunction function = mock(LongFunction.class);
        try {
            ref.atomicGetAndAlter(function);
            fail();
        } catch (LockedException expected) {
        }

        verifyZeroInteractions(function);
        assertRefHasExclusiveLock(ref, otherTx);
        assertWriteBiased(ref);
        assertSurplus(ref, 1);
        assertVersionAndValue(ref, initialVersion, initialValue);
    }

    @Test
    public void whenEnsuredByOtherAndNothingDirty_thenLockedException() {
        long initialValue = 2;
        GammaLongRef ref = new GammaLongRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTxn otherTx = stm.newDefaultTxn();
        ref.getLock().acquire(otherTx, LockMode.Write);

        try {
            ref.atomicGetAndAlter(identityLongFunction());
            fail();
        } catch (LockedException expected) {
        }

        assertRefHasWriteLock(ref, otherTx);
        assertSurplus(ref, 1);
        assertWriteBiased(ref);
        assertReadonlyCount(ref, 0);
        assertVersionAndValue(ref, initialVersion, initialValue);
    }

    @Test
    public void whenEnsuredByOther_thenLockedException() {
        int initialValue = 2;
        GammaLongRef ref = new GammaLongRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTxn otherTx = stm.newDefaultTxn();
        ref.getLock().acquire(otherTx, LockMode.Write);

        LongFunction function = mock(LongFunction.class);
        try {
            ref.atomicGetAndAlter(function);
            fail();
        } catch (LockedException expected) {
        }

        verifyZeroInteractions(function);
        assertRefHasWriteLock(ref, otherTx);
        assertWriteBiased(ref);
        assertSurplus(ref, 1);
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

        long result = ref.atomicGetAndAlter(Functions.incLongFunction());

        assertEquals(result, initialValue);
        joinAll(thread);
        assertRefHasNoLocks(ref);
        assertVersionAndValue(ref, initialVersion + 1, initialValue + 1);
    }
}
