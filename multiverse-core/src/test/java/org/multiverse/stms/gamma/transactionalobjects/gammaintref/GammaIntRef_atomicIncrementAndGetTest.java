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
import static org.multiverse.api.ThreadLocalTransaction.*;
import static org.multiverse.stms.gamma.GammaTestUtils.*;

public class GammaIntRef_atomicIncrementAndGetTest {

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

        int amount = 10;
        long result = ref.atomicIncrementAndGet(amount);

        assertEquals(initialValue + amount, result);
        assertRefHasNoLocks(ref);
        assertSurplus(ref, 0);
        assertVersionAndValue(ref, initialVersion + 1, initialValue + amount);
        assertNull(getThreadLocalTransaction());
    }

    @Test
    public void whenActiveTransactionAvailable_thenIgnored() {
        int initialValue = 10;
        GammaIntRef ref = new GammaIntRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTransaction tx = stm.newDefaultTransaction();
        setThreadLocalTransaction(tx);
        ref.set(tx, 1000);

        int amount = 1;
        long result = ref.atomicIncrementAndGet(amount);

        assertEquals(initialValue + amount, result);
        assertRefHasNoLocks(ref);
        assertSame(tx, getThreadLocalTransaction());
        assertVersionAndValue(ref, initialVersion + 1, initialValue + amount);
    }

    @Test
    public void whenNoChange() {
        int initialValue = 10;
        GammaIntRef ref = new GammaIntRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        long result = ref.atomicIncrementAndGet(0);

        assertEquals(initialValue, result);
        assertRefHasNoLocks(ref);
        assertSurplus(ref, 0);
        assertNull(getThreadLocalTransaction());
        assertVersionAndValue(ref, initialVersion, initialValue);
    }

    @Test
    public void whenPrivatizedByOther_thenLockedException() {
        int initialValue = 10;
        GammaIntRef ref = new GammaIntRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTransaction otherTx = stm.newDefaultTransaction();
        ref.getLock().acquire(otherTx, LockMode.Exclusive);

        try {
            ref.atomicIncrementAndGet(1);
            fail();
        } catch (LockedException expected) {
        }

        assertSurplus(ref, 1);
        assertRefHasExclusiveLock(ref, otherTx);
        assertVersionAndValue(ref, initialVersion, initialValue);
    }

    @Test
    public void whenEnsuredByOtherAndChange_thenLockedException() {
        int initialValue = 10;
        GammaIntRef ref = new GammaIntRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTransaction otherTx = stm.newDefaultTransaction();
        ref.getLock().acquire(otherTx, LockMode.Write);

        try {
            ref.atomicIncrementAndGet(1);
            fail();
        } catch (LockedException expected) {
        }

        assertSurplus(ref, 1);
        assertRefHasWriteLock(ref, otherTx);
        assertVersionAndValue(ref, initialVersion, initialValue);
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

        ref.atomicIncrementAndGet(1);

        joinAll(thread);
        assertRefHasNoLocks(ref);
        assertVersionAndValue(ref, initialVersion + 1, newValue);
    }
}
