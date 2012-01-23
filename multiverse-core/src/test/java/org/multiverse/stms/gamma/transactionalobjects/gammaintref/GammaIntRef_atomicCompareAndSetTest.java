package org.multiverse.stms.gamma.transactionalobjects.gammaintref;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.LockMode;
import org.multiverse.api.exceptions.LockedException;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.GammaStmConfiguration;
import org.multiverse.stms.gamma.transactionalobjects.GammaIntRef;
import org.multiverse.stms.gamma.transactions.GammaTransaction;

import static org.junit.Assert.*;
import static org.multiverse.TestUtils.joinAll;
import static org.multiverse.TestUtils.sleepMs;
import static org.multiverse.api.ThreadLocalTransaction.clearThreadLocalTransaction;
import static org.multiverse.api.ThreadLocalTransaction.setThreadLocalTransaction;
import static org.multiverse.stms.gamma.GammaTestUtils.*;

public class GammaIntRef_atomicCompareAndSetTest {

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
        int initialValue = 1;
        GammaIntRef ref = new GammaIntRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTransaction otherTx = stm.newDefaultTransaction();
        ref.getLock().acquire(otherTx, LockMode.Exclusive);

        int newValue = 2;
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
        int initialValue = 1;
        GammaIntRef ref = new GammaIntRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTransaction otherTx = stm.newDefaultTransaction();
        ref.getLock().acquire(otherTx, LockMode.Write);

        int newValue = 2;
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
        int initialValue = 1;
        GammaIntRef ref = new GammaIntRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTransaction tx = stm.newDefaultTransaction();
        setThreadLocalTransaction(tx);
        ref.set(initialValue + 100);

        int newValue = 2;
        boolean result = ref.atomicCompareAndSet(initialValue, newValue);

        assertTrue(result);
        assertRefHasNoLocks(ref);
        assertSurplus(ref, 0);
        assertVersionAndValue(ref, initialVersion + 1, newValue);
    }

    @Test
    public void whenExpectedValueFoundAndUpdateIsSame() {
        int initialValue = 1;
        GammaIntRef ref = new GammaIntRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        boolean result = ref.atomicCompareAndSet(initialValue, initialValue);

        assertTrue(result);
        assertVersionAndValue(ref, initialVersion, initialValue);
        assertRefHasNoLocks(ref);
        assertSurplus(ref, 0);
    }

    @Test
    public void whenExpectedValueFound() {
        int initialValue = 1;
        GammaIntRef ref = new GammaIntRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        int newValue = initialValue + 1;
        boolean result = ref.atomicCompareAndSet(initialValue, newValue);

        assertTrue(result);
        assertRefHasNoLocks(ref);
        assertSurplus(ref, 0);
        assertVersionAndValue(ref, initialVersion + 1, newValue);
    }

    @Test
    public void whenExpectedValueNotFound() {
        int initialValue = 2;
        GammaIntRef ref = new GammaIntRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        boolean result = ref.atomicCompareAndSet(1, 3);

        assertFalse(result);
        assertVersionAndValue(ref, initialVersion, initialValue);
        assertRefHasNoLocks(ref);
        assertSurplus(ref, 0);
    }

    @Test
    public void whenListenersAvailable() {
        int initialValue = 10;
        GammaIntRef ref = new GammaIntRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        int newValue = initialValue + 1;
        IntRefAwaitThread thread = new IntRefAwaitThread(ref, newValue);
        thread.start();

        sleepMs(500);

        ref.atomicCompareAndSet(initialValue, newValue);

        joinAll(thread);
        assertRefHasNoLocks(ref);
        assertVersionAndValue(ref, initialVersion + 1, newValue);
    }
}
