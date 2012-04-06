package org.multiverse.stms.gamma.transactionalobjects.txnlong;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.LockMode;
import org.multiverse.api.exceptions.LockedException;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.GammaStmConfig;
import org.multiverse.stms.gamma.transactionalobjects.GammaTxnLong;
import org.multiverse.stms.gamma.transactions.GammaTxn;

import static org.junit.Assert.*;
import static org.multiverse.TestUtils.*;
import static org.multiverse.api.TxnThreadLocal.*;
import static org.multiverse.stms.gamma.GammaTestUtils.*;

public class GammaTxnLong_atomicGetAndIncrementTest {
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
        int initialValue = 2;
        GammaTxnLong ref = new GammaTxnLong(stm, initialValue);
        long initialVersion = ref.getVersion();

        long result = ref.atomicGetAndIncrement(1);

        assertEquals(initialValue, result);
        assertVersionAndValue(ref, initialVersion + 1, initialValue + 1);
        assertRefHasNoLocks(ref);
        assertSurplus(ref, 0);
        assertNull(getThreadLocalTxn());
    }

    @Test
    public void whenNoChange() {
        int initialValue = 2;
        GammaTxnLong ref = new GammaTxnLong(stm, initialValue);
        long initialVersion = ref.getVersion();

        long result = ref.atomicGetAndIncrement(0);

        assertEquals(initialValue, result);
        assertRefHasNoLocks(ref);
        assertSurplus(ref, 0);
        assertNull(getThreadLocalTxn());
        assertVersionAndValue(ref, initialVersion, initialValue);
    }

    @Test
    public void whenActiveTransactionAvailable_thenIgnored() {
        int initialValue = 2;
        GammaTxnLong ref = new GammaTxnLong(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTxn tx = stm.newDefaultTxn();
        setThreadLocalTxn(tx);
        ref.set(10);

        long result = ref.atomicGetAndIncrement(1);

        assertEquals(initialValue, result);
        assertVersionAndValue(ref, initialVersion + 1, initialValue + 1);
        assertSurplus(ref, 0);
        assertSame(tx, getThreadLocalTxn());
        assertIsActive(tx);
    }

    @Test
    public void whenPrivatizedByOther_thenLockedException() {
        long initialValue = 10;
        GammaTxnLong ref = new GammaTxnLong(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTxn otherTx = stm.newDefaultTxn();
        ref.getLock().acquire(otherTx, LockMode.Exclusive);

        try {
            ref.atomicCompareAndSet(0, 1);
            fail();
        } catch (LockedException expected) {
        }

        assertSurplus(ref, 1);
        assertRefHasExclusiveLock(ref, otherTx);
        assertVersionAndValue(ref, initialVersion, initialValue);
    }

    @Test
    public void whenEnsuredByOther_thenLockedException() {
        int initialValue = 10;
        GammaTxnLong ref = new GammaTxnLong(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTxn otherTx = stm.newDefaultTxn();
        ref.getLock().acquire(otherTx, LockMode.Write);

        try {
            ref.atomicCompareAndSet(initialValue, 20);
            fail();
        } catch (LockedException expected) {
        }

        assertSurplus(ref, 1);
        assertRefHasWriteLock(ref, otherTx);
        assertVersionAndValue(ref, initialVersion, 10);
    }

    @Test
    public void whenListenersAvailable() {
        long initialValue = 10;
        GammaTxnLong ref = new GammaTxnLong(stm, initialValue);
        long initialVersion = ref.getVersion();

        int amount = 1;
        LongRefAwaitThread thread = new LongRefAwaitThread(ref, initialValue + amount);
        thread.start();

        sleepMs(500);

        long result = ref.atomicGetAndIncrement(amount);

        assertEquals(result, initialValue);
        joinAll(thread);
        assertRefHasNoLocks(ref);
        assertVersionAndValue(ref, initialVersion + 1, initialValue + amount);
    }
}
