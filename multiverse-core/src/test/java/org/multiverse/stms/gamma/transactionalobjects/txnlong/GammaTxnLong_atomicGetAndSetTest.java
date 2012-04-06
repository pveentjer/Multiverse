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
import static org.junit.Assert.assertEquals;
import static org.multiverse.TestUtils.*;
import static org.multiverse.api.TxnThreadLocal.clearThreadLocalTxn;
import static org.multiverse.api.TxnThreadLocal.getThreadLocalTxn;
import static org.multiverse.api.TxnThreadLocal.setThreadLocalTxn;
import static org.multiverse.stms.gamma.GammaTestUtils.*;

public class GammaTxnLong_atomicGetAndSetTest {

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
        GammaTxnLong ref = new GammaTxnLong(stm, initialValue);
        long initialVersion = ref.getVersion();

        int newValue = 10;
        long result = ref.atomicGetAndSet(newValue);

        assertEquals(initialValue, result);
        assertNull(getThreadLocalTxn());
        assertSurplus(ref, 0);
        assertRefHasNoLocks(ref);
        assertWriteBiased(ref);
        assertVersionAndValue(ref, initialVersion + 1, newValue);
    }

    @Test
    public void whenActiveTransactionAvailable_thenIgnored() {
        long initialValue = 2;
        GammaTxnLong ref = new GammaTxnLong(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTxn tx = stm.newDefaultTxn();
        setThreadLocalTxn(tx);

        long newValue = 10;
        long result = ref.atomicGetAndSet(newValue);

        assertIsActive(tx);
        assert (tx.getRefTranlocal(ref) == null);
        assertEquals(initialValue, result);
        assertSame(tx, getThreadLocalTxn());
        assertSurplus(ref, 0);
        assertRefHasNoLocks(ref);
        assertWriteBiased(ref);
        assertVersionAndValue(ref, initialVersion + 1, newValue);
    }

    @Test
    public void whenLockedByOther_thenLockedException() {
        whenLockedByOther_thenLockedException(LockMode.Read);
        whenLockedByOther_thenLockedException(LockMode.Write);
        whenLockedByOther_thenLockedException(LockMode.Exclusive);
    }

    public void whenLockedByOther_thenLockedException(LockMode lockMode) {
        long initialValue = 10;
        GammaTxnLong ref = new GammaTxnLong(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTxn otherTx = stm.newDefaultTxn();
        ref.getLock().acquire(otherTx, lockMode);

        long orecValue = ref.orec;
        try {
            ref.atomicGetAndSet(1);
            fail();
        } catch (LockedException expected) {
        }

        assertOrecValue(ref, orecValue);
        assertVersionAndValue(ref, initialVersion, initialValue);
    }

    @Test
    public void whenNoChange_thenNoCommit() {
        long initialValue = 2;
        GammaTxnLong ref = new GammaTxnLong(stm, initialValue);
        long initialVersion = ref.getVersion();

        long result = ref.atomicGetAndSet(initialValue);

        assertEquals(initialValue, result);
        assertVersionAndValue(ref, initialVersion, initialValue);
        assertNull(getThreadLocalTxn());
        assertSurplus(ref, 0);
        assertRefHasNoLocks(ref);
        assertWriteBiased(ref);
    }

    @Test
    public void whenListenersAvailable() {
        long initialValue = 10;
        GammaTxnLong ref = new GammaTxnLong(stm, initialValue);
        long initialVersion = ref.getVersion();

        long newValue = initialValue + 1;
        LongRefAwaitThread thread = new LongRefAwaitThread(ref, newValue);
        thread.start();

        sleepMs(500);

        long result = ref.atomicGetAndSet(newValue);

        assertEquals(initialValue, result);
        joinAll(thread);
        assertRefHasNoLocks(ref);
        assertVersionAndValue(ref, initialVersion + 1, newValue);
    }
}
