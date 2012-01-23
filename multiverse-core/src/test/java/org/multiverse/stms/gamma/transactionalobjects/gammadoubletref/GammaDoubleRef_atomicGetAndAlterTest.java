package org.multiverse.stms.gamma.transactionalobjects.gammadoubletref;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.LockMode;
import org.multiverse.api.exceptions.LockedException;
import org.multiverse.api.functions.DoubleFunction;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.GammaStmConfiguration;
import org.multiverse.stms.gamma.transactionalobjects.GammaDoubleRef;
import org.multiverse.stms.gamma.transactions.GammaTransaction;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.multiverse.TestUtils.*;
import static org.multiverse.api.ThreadLocalTransaction.clearThreadLocalTransaction;
import static org.multiverse.api.ThreadLocalTransaction.setThreadLocalTransaction;
import static org.multiverse.api.functions.Functions.identityDoubleFunction;
import static org.multiverse.api.functions.Functions.incDoubleFunction;
import static org.multiverse.stms.gamma.GammaTestUtils.*;

public class GammaDoubleRef_atomicGetAndAlterTest {

    private GammaStm stm;

    @Before
    public void setUp() {
        GammaStmConfiguration config = new GammaStmConfiguration();
        config.maxRetries = 10;
        stm = new GammaStm(config);
        clearThreadLocalTransaction();
    }

    @Test
    public void whenSuccess() {
        double initialValue = 2;
        GammaDoubleRef ref = new GammaDoubleRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        DoubleFunction function = incDoubleFunction();
        double result = ref.atomicGetAndAlter(function);

        assertEqualsDouble(2, result);
        assertRefHasNoLocks(ref);
        assertWriteBiased(ref);
        assertSurplus(ref, 0);
        assertVersionAndValue(ref, initialVersion + 1, initialValue + 1);
    }

    @Test
    public void whenNullFunction_thenNullPointerException() {
        double initialValue = 10;
        GammaDoubleRef ref = new GammaDoubleRef(stm, initialValue);
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
        double initialValue = 2;
        GammaDoubleRef ref = new GammaDoubleRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTransaction tx = stm.newDefaultTransaction();
        setThreadLocalTransaction(tx);
        ref.set(10);

        DoubleFunction function = incDoubleFunction();
        double result = ref.atomicGetAndAlter(function);

        tx.abort();

        assertEqualsDouble(initialValue, result);
        assertRefHasNoLocks(ref);
        assertWriteBiased(ref);
        assertSurplus(ref, 0);
        assertVersionAndValue(ref, initialVersion + 1, initialValue + 1);
    }

    @Test
    public void whenPrivatizedByOther_thenLockedException() {
        double initialValue = 2;
        GammaDoubleRef ref = new GammaDoubleRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTransaction otherTx = stm.newDefaultTransaction();
        ref.getLock().acquire(otherTx, LockMode.Exclusive);

        DoubleFunction function = mock(DoubleFunction.class);
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
        double initialValue = 2;
        GammaDoubleRef ref = new GammaDoubleRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTransaction otherTx = stm.newDefaultTransaction();
        ref.getLock().acquire(otherTx, LockMode.Write);

        try {
            ref.atomicGetAndAlter(identityDoubleFunction());
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
        GammaDoubleRef ref = new GammaDoubleRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTransaction otherTx = stm.newDefaultTransaction();
        ref.getLock().acquire(otherTx, LockMode.Write);

        DoubleFunction function = mock(DoubleFunction.class);
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
        double initialValue = 10;
        GammaDoubleRef ref = new GammaDoubleRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaDoubleRefAwaitThread thread = new GammaDoubleRefAwaitThread(ref, initialValue + 1);
        thread.start();

        sleepMs(500);

        double result = ref.atomicGetAndAlter(incDoubleFunction());

        assertEqualsDouble(result, initialValue);
        joinAll(thread);
        assertRefHasNoLocks(ref);
        assertVersionAndValue(ref, initialVersion + 1, initialValue + 1);
    }
}
