package org.multiverse.stms.gamma.transactionalobjects.gammadoubletref;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.LockMode;
import org.multiverse.api.exceptions.LockedException;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.GammaStmConfiguration;
import org.multiverse.stms.gamma.transactionalobjects.GammaDoubleRef;
import org.multiverse.stms.gamma.transactions.GammaTransaction;

import static org.junit.Assert.*;
import static org.multiverse.TestUtils.*;
import static org.multiverse.api.ThreadLocalTransaction.*;
import static org.multiverse.stms.gamma.GammaTestUtils.*;

public class GammaDoubleRef_atomicGetAndIncrementTest {
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
        GammaDoubleRef ref = new GammaDoubleRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        double result = ref.atomicGetAndIncrement(1);

        assertEqualsDouble(initialValue, result);
        assertVersionAndValue(ref, initialVersion + 1, initialValue + 1);
        assertRefHasNoLocks(ref);
        assertSurplus(ref, 0);
        assertNull(getThreadLocalTransaction());
    }

    @Test
    public void whenNoChange() {
        int initialValue = 2;
        GammaDoubleRef ref = new GammaDoubleRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        double result = ref.atomicGetAndIncrement(0);

        assertEqualsDouble(initialValue, result);
        assertRefHasNoLocks(ref);
        assertSurplus(ref, 0);
        assertNull(getThreadLocalTransaction());
        assertVersionAndValue(ref, initialVersion, initialValue);
    }

    @Test
    public void whenActiveTransactionAvailable_thenIgnored() {
        int initialValue = 2;
        GammaDoubleRef ref = new GammaDoubleRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTransaction tx = stm.newDefaultTransaction();
        setThreadLocalTransaction(tx);
        ref.set(10);

        double result = ref.atomicGetAndIncrement(1);

        assertEqualsDouble(initialValue, result);
        assertVersionAndValue(ref, initialVersion + 1, initialValue + 1);
        assertSurplus(ref, 0);
        assertSame(tx, getThreadLocalTransaction());
        assertIsActive(tx);
    }

    @Test
    public void whenPrivatizedByOther_thenLockedException() {
        double initialValue = 10;
        GammaDoubleRef ref = new GammaDoubleRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTransaction otherTx = stm.newDefaultTransaction();
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
        GammaDoubleRef ref = new GammaDoubleRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTransaction otherTx = stm.newDefaultTransaction();
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
        double initialValue = 10;
        GammaDoubleRef ref = new GammaDoubleRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        int amount = 1;
        GammaDoubleRefAwaitThread thread = new GammaDoubleRefAwaitThread(ref, initialValue + amount);
        thread.start();

        sleepMs(500);

        double result = ref.atomicGetAndIncrement(amount);

        assertEqualsDouble(result, initialValue);
        joinAll(thread);
        assertRefHasNoLocks(ref);
        assertVersionAndValue(ref, initialVersion + 1, initialValue + amount);
    }
}
