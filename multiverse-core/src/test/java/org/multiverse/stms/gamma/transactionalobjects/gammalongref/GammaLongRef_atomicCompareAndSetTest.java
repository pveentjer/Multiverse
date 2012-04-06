package org.multiverse.stms.gamma.transactionalobjects.gammalongref;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.LockMode;
import org.multiverse.api.exceptions.LockedException;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.GammaStmConfiguration;
import org.multiverse.stms.gamma.transactionalobjects.GammaLongRef;
import org.multiverse.stms.gamma.transactions.GammaTxn;

import static org.junit.Assert.*;
import static org.multiverse.TestUtils.joinAll;
import static org.multiverse.TestUtils.sleepMs;
import static org.multiverse.api.TxnThreadLocal.clearThreadLocalTxn;
import static org.multiverse.api.TxnThreadLocal.setThreadLocalTxn;
import static org.multiverse.stms.gamma.GammaTestUtils.*;

public class GammaLongRef_atomicCompareAndSetTest {

    private GammaStm stm;

    @Before
    public void setUp() {
        GammaStmConfiguration config = new GammaStmConfiguration();
        config.maxRetries = 10;
        stm = new GammaStm(config);
        clearThreadLocalTxn();
    }

    @Test
    public void whenPrivatizedByOther_thenLockedException() {
        long initialValue = 1;
        GammaLongRef ref = new GammaLongRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTxn otherTx = stm.newDefaultTransaction();
        ref.getLock().acquire(otherTx, LockMode.Exclusive);

        long newValue = 2;
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
        long initialValue = 1;
        GammaLongRef ref = new GammaLongRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTxn otherTx = stm.newDefaultTransaction();
        ref.getLock().acquire(otherTx, LockMode.Write);

        long newValue = 2;
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
        GammaLongRef ref = new GammaLongRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTxn tx = stm.newDefaultTransaction();
        setThreadLocalTxn(tx);
        ref.set(initialValue + 100);

        long newValue = 2;
        boolean result = ref.atomicCompareAndSet(initialValue, newValue);

        assertTrue(result);
        assertRefHasNoLocks(ref);
        assertSurplus(ref, 0);
        assertVersionAndValue(ref, initialVersion + 1, newValue);
    }

    @Test
    public void whenExpectedValueFoundAndUpdateIsSame() {
        long initialValue = 1;
        GammaLongRef ref = new GammaLongRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        boolean result = ref.atomicCompareAndSet(initialValue, initialValue);

        assertTrue(result);
        assertVersionAndValue(ref, initialVersion, initialValue);
        assertRefHasNoLocks(ref);
        assertSurplus(ref, 0);
    }

    @Test
    public void whenExpectedValueFound() {
        long initialValue = 1;
        GammaLongRef ref = new GammaLongRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        long newValue = initialValue + 1;
        boolean result = ref.atomicCompareAndSet(initialValue, newValue);

        assertTrue(result);
        assertRefHasNoLocks(ref);
        assertSurplus(ref, 0);
        assertVersionAndValue(ref, initialVersion + 1, newValue);
    }

    @Test
    public void whenExpectedValueNotFound() {
        int initialValue = 2;
        GammaLongRef ref = new GammaLongRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        boolean result = ref.atomicCompareAndSet(1, 3);

        assertFalse(result);
        assertVersionAndValue(ref, initialVersion, initialValue);
        assertRefHasNoLocks(ref);
        assertSurplus(ref, 0);
    }

    @Test
    public void whenListenersAvailable() {
        long initialValue = 10;
        GammaLongRef ref = new GammaLongRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        long newValue = initialValue + 1;
        LongRefAwaitThread thread = new LongRefAwaitThread(ref, newValue);
        thread.start();

        sleepMs(500);

        ref.atomicCompareAndSet(initialValue, newValue);

        joinAll(thread);
        assertRefHasNoLocks(ref);
        assertVersionAndValue(ref, initialVersion + 1, newValue);
    }
}
