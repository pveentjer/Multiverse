package org.multiverse.stms.gamma.transactionalobjects.gammaintref;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.LockMode;
import org.multiverse.api.exceptions.LockedException;
import org.multiverse.api.functions.Functions;
import org.multiverse.api.functions.IntFunction;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.GammaStmConfiguration;
import org.multiverse.stms.gamma.transactionalobjects.GammaIntRef;
import org.multiverse.stms.gamma.transactions.GammaTransaction;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.multiverse.TestUtils.joinAll;
import static org.multiverse.TestUtils.sleepMs;
import static org.multiverse.api.ThreadLocalTransaction.clearThreadLocalTransaction;
import static org.multiverse.api.ThreadLocalTransaction.setThreadLocalTransaction;
import static org.multiverse.api.functions.Functions.identityIntFunction;
import static org.multiverse.stms.gamma.GammaTestUtils.*;

public class GammaIntRef_atomicGetAndAlterTest {

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
        int initialValue = 2;
        GammaIntRef ref = new GammaIntRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        IntFunction function = Functions.incIntFunction();
        long result = ref.atomicGetAndAlter(function);

        assertEquals(2, result);
        assertRefHasNoLocks(ref);
        assertWriteBiased(ref);
        assertSurplus(ref, 0);
        assertVersionAndValue(ref, initialVersion + 1, initialValue + 1);
    }

    @Test
    public void whenNullFunction_thenNullPointerException() {
        int initialValue = 10;
        GammaIntRef ref = new GammaIntRef(stm, initialValue);
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
        int initialValue = 2;
        GammaIntRef ref = new GammaIntRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTransaction tx = stm.newDefaultTransaction();
        setThreadLocalTransaction(tx);
        ref.set(10);

        IntFunction function = Functions.incIntFunction();
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
        int initialValue = 2;
        GammaIntRef ref = new GammaIntRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTransaction otherTx = stm.newDefaultTransaction();
        ref.getLock().acquire(otherTx, LockMode.Exclusive);

        IntFunction function = mock(IntFunction.class);
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
        int initialValue = 2;
        GammaIntRef ref = new GammaIntRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTransaction otherTx = stm.newDefaultTransaction();
        ref.getLock().acquire(otherTx, LockMode.Write);

        try {
            ref.atomicGetAndAlter(identityIntFunction());
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
        GammaIntRef ref = new GammaIntRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTransaction otherTx = stm.newDefaultTransaction();
        ref.getLock().acquire(otherTx, LockMode.Write);

        IntFunction function = mock(IntFunction.class);
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
        int initialValue = 10;
        GammaIntRef ref = new GammaIntRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        IntRefAwaitThread thread = new IntRefAwaitThread(ref, initialValue + 1);
        thread.start();

        sleepMs(500);

        long result = ref.atomicGetAndAlter(Functions.incIntFunction());

        assertEquals(result, initialValue);
        joinAll(thread);
        assertRefHasNoLocks(ref);
        assertVersionAndValue(ref, initialVersion + 1, initialValue + 1);
    }
}
