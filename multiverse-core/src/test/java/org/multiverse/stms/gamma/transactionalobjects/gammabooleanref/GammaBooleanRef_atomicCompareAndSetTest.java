package org.multiverse.stms.gamma.transactionalobjects.gammabooleanref;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.LockMode;
import org.multiverse.api.exceptions.LockedException;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.GammaStmConfiguration;
import org.multiverse.stms.gamma.transactionalobjects.GammaBooleanRef;
import org.multiverse.stms.gamma.transactions.GammaTransaction;

import static org.junit.Assert.*;
import static org.multiverse.TestUtils.joinAll;
import static org.multiverse.TestUtils.sleepMs;
import static org.multiverse.api.ThreadLocalTransaction.clearThreadLocalTransaction;
import static org.multiverse.api.ThreadLocalTransaction.setThreadLocalTransaction;
import static org.multiverse.stms.gamma.GammaTestUtils.*;

public class GammaBooleanRef_atomicCompareAndSetTest {

    private GammaStm stm;

    @Before
    public void setUp() {
        GammaStmConfiguration config = new GammaStmConfiguration();
        config.maxRetries = 10;
        stm = new GammaStm(config);
        clearThreadLocalTransaction();
    }

    @Test
    public void whenPrivatizedByOther_thenLockedException() {
        boolean initialValue = true;
        GammaBooleanRef ref = new GammaBooleanRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTransaction otherTx = stm.newDefaultTransaction();
        ref.getLock().acquire(otherTx, LockMode.Exclusive);

        boolean newValue = false;
        try {
            ref.atomicCompareAndSet(initialValue, newValue);
            fail();
        } catch (LockedException expected) {
        }

        assertSurplus(ref, 1);
        assertRefHasExclusiveLock(ref, otherTx);
        assertVersionAndValue(ref, initialVersion, initialValue);
    }

    @Test
    public void whenEnsuredByOther_thenLockedException() {
        boolean initialValue = true;
        GammaBooleanRef ref = new GammaBooleanRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTransaction otherTx = stm.newDefaultTransaction();
        ref.getLock().acquire(otherTx, LockMode.Write);

        boolean newValue = false;
        try {
            ref.atomicCompareAndSet(initialValue, newValue);
            fail();
        } catch (LockedException expected) {
        }

        assertSurplus(ref, 1);
        assertRefHasWriteLock(ref, otherTx);
        assertVersionAndValue(ref, initialVersion, initialValue);
    }

    @Test
    public void whenActiveTransactionAvailable_thenIgnored() {
        boolean initialValue = true;
        GammaBooleanRef ref = new GammaBooleanRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTransaction tx = stm.newDefaultTransaction();
        setThreadLocalTransaction(tx);
        ref.set(false);

        boolean newValue = false;
        boolean result = ref.atomicCompareAndSet(initialValue, newValue);

        assertTrue(result);
        assertRefHasNoLocks(ref);
        assertSurplus(ref, 0);
        assertVersionAndValue(ref, initialVersion + 1, newValue);
    }

    @Test
    public void whenExpectedValueFoundAndUpdateIsSame() {
        boolean initialValue = true;
        GammaBooleanRef ref = new GammaBooleanRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        boolean result = ref.atomicCompareAndSet(initialValue, initialValue);

        assertTrue(result);
        assertVersionAndValue(ref, initialVersion, initialValue);
        assertRefHasNoLocks(ref);
        assertSurplus(ref, 0);
    }

    @Test
    public void whenExpectedValueFound() {
        boolean initialValue = true;
        GammaBooleanRef ref = new GammaBooleanRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        boolean newValue = false;
        boolean result = ref.atomicCompareAndSet(initialValue, newValue);

        assertTrue(result);
        assertRefHasNoLocks(ref);
        assertSurplus(ref, 0);
        assertVersionAndValue(ref, initialVersion + 1, newValue);
    }

    @Test
    public void whenExpectedValueNotFound() {
        boolean initialValue = true;
        GammaBooleanRef ref = new GammaBooleanRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        boolean result = ref.atomicCompareAndSet(false, true);

        assertFalse(result);
        assertVersionAndValue(ref, initialVersion, initialValue);
        assertRefHasNoLocks(ref);
        assertSurplus(ref, 0);
    }

    @Test
    public void whenListenersAvailable() {
        boolean initialValue = true;
        GammaBooleanRef ref = new GammaBooleanRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        boolean newValue = false;
        BooleanRefAwaitThread thread = new BooleanRefAwaitThread(ref, newValue);
        thread.start();

        sleepMs(500);

        ref.atomicCompareAndSet(initialValue, newValue);

        joinAll(thread);
        assertRefHasNoLocks(ref);
        assertVersionAndValue(ref, initialVersion + 1, newValue);
    }
}
