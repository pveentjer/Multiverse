package org.multiverse.stms.gamma.transactionalobjects.gammaref;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.LockMode;
import org.multiverse.api.exceptions.LockedException;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.GammaStmConfig;
import org.multiverse.stms.gamma.transactionalobjects.GammaTxnRef;
import org.multiverse.stms.gamma.transactions.GammaTxn;

import static org.junit.Assert.*;
import static org.multiverse.TestUtils.*;
import static org.multiverse.api.TxnThreadLocal.*;
import static org.multiverse.stms.gamma.GammaTestUtils.*;

public class GammaTxnRef_atomicGetAndSetTest {

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
        String initialValue = "initialValue";
        GammaTxnRef<String> ref = new GammaTxnRef<String>(stm, initialValue);
        long initialVersion = ref.getVersion();

        String newValue = "newValue";
        String result = ref.atomicGetAndSet(newValue);

        assertEquals(initialValue, result);
        assertNull(getThreadLocalTxn());
        assertSurplus(ref, 0);
        assertRefHasNoLocks(ref);
        assertWriteBiased(ref);
        assertVersionAndValue(ref, initialVersion + 1, newValue);
    }

    @Test
    public void whenActiveTransactionAvailable_thenIgnored() {
        String initialValue = "initialValue";
        GammaTxnRef<String> ref = new GammaTxnRef<String>(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTxn tx = stm.newDefaultTxn();
        setThreadLocalTxn(tx);

        String newValue = "newValue";
        String result = ref.atomicGetAndSet(newValue);

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
        String initialValue = "initialValue";
        GammaTxnRef<String> ref = new GammaTxnRef<String>(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTxn otherTx = stm.newDefaultTxn();
        ref.getLock().acquire(otherTx, lockMode);

        long orecValue = ref.orec;
        try {
            ref.atomicGetAndSet("newValue");
            fail();
        } catch (LockedException expected) {
        }

        assertOrecValue(ref, orecValue);
        assertVersionAndValue(ref, initialVersion, initialValue);
    }

    @Test
    public void whenNoChange_thenNoCommit() {
        String initialValue = "initialValue";
        GammaTxnRef<String> ref = new GammaTxnRef<String>(stm, initialValue);
        long initialVersion = ref.getVersion();

        String result = ref.atomicGetAndSet(initialValue);

        assertEquals(initialValue, result);
        assertVersionAndValue(ref, initialVersion, initialValue);
        assertNull(getThreadLocalTxn());
        assertSurplus(ref, 0);
        assertRefHasNoLocks(ref);
        assertWriteBiased(ref);
    }

    @Test
    public void whenListenersAvailable() {
        String initialValue = "initialValue";
        GammaTxnRef<String> ref = new GammaTxnRef<String>(stm, initialValue);
        long initialVersion = ref.getVersion();

        String newValue = "newValue";
        RefAwaitThread thread = new RefAwaitThread(ref, newValue);
        thread.start();

        sleepMs(500);

        String result = ref.atomicGetAndSet(newValue);

        assertEquals(initialValue, result);
        joinAll(thread);
        assertRefHasNoLocks(ref);
        assertVersionAndValue(ref, initialVersion + 1, newValue);
    }

}
