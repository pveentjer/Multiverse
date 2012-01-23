package org.multiverse.stms.gamma.transactionalobjects.gammabooleanref;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.LockMode;
import org.multiverse.api.exceptions.LockedException;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactionalobjects.GammaBooleanRef;
import org.multiverse.stms.gamma.transactions.GammaTransaction;

import static org.junit.Assert.*;
import static org.multiverse.TestUtils.assertOrecValue;
import static org.multiverse.api.ThreadLocalTransaction.*;
import static org.multiverse.stms.gamma.GammaTestUtils.*;

public class GammaBooleanRef_atomicGetTest {

    private GammaStm stm;

    @Before
    public void setUp() {
        stm = new GammaStm();
        clearThreadLocalTransaction();
    }

    @Test(expected = LockedException.class)
    public void whenUnconstructed() {
        GammaTransaction tx = stm.newDefaultTransaction();
        GammaBooleanRef ref = new GammaBooleanRef(tx);
        ref.atomicGet();
    }

    @Test
    public void whenActiveTransactionAvailable_thenIgnored() {
        boolean initialValue = true;
        GammaBooleanRef ref = new GammaBooleanRef(stm, initialValue);

        GammaTransaction tx = stm.newDefaultTransaction();
        setThreadLocalTransaction(tx);
        ref.set(false);

        assertEquals(true, ref.atomicGet());

        assertSame(tx, getThreadLocalTransaction());
    }

    @Test
    public void whenUpdatedBiasedOnUnlocked() {
        boolean initialValue = true;
        GammaBooleanRef ref = new GammaBooleanRef(stm, initialValue);

        boolean result = ref.atomicGet();
        assertTrue(result);
        //assertUpdateBiased(ref);
    }

    @Test
    public void whenUpdateBiasedAndPrivatizedByOther_thenLockedException() {
        boolean initialValue = true;
        GammaBooleanRef ref = new GammaBooleanRef(stm, initialValue);
        long version = ref.getVersion();

        GammaTransaction otherTx = stm.newDefaultTransaction();
        ref.getLock().acquire(otherTx, LockMode.Exclusive);

        try {
            ref.atomicGet();
            fail();
        } catch (LockedException ex) {
        }

        assertSurplus(ref, 1);
        assertWriteBiased(ref);
        assertRefHasExclusiveLock(ref, otherTx);
        assertVersionAndValue(ref, version, true);
    }

    @Test
    public void whenUpdateBiasedAndEnsuredByOther() {
        boolean initialValue = true;
        GammaBooleanRef ref = new GammaBooleanRef(stm, initialValue);
        long version = ref.getVersion();

        GammaTransaction otherTx = stm.newDefaultTransaction();
        ref.getLock().acquire(otherTx, LockMode.Write);

        boolean result = ref.atomicGet();

        assertEquals(initialValue, result);
        assertSurplus(ref, 1);
        assertRefHasWriteLock(ref, otherTx);
        assertWriteBiased(ref);
        assertVersionAndValue(ref, version, true);
    }

    @Test
    public void whenReadBiasedAndUnlocked() {
        boolean initialValue = true;
        GammaBooleanRef ref = makeReadBiased(new GammaBooleanRef(stm, initialValue));

        boolean result = ref.atomicGet();
        assertTrue(result);
        assertReadBiased(ref);
    }

    @Test
    public void whenReadBiasedAndPrivatizedByOther_thenLockedException() {
        boolean initialValue =true;
        GammaBooleanRef ref = makeReadBiased(new GammaBooleanRef(stm, initialValue));
        long version = ref.getVersion();

        GammaTransaction otherTx = stm.newDefaultTransaction();
        ref.getLock().acquire(otherTx, LockMode.Exclusive);

        long orecValue = ref.orec;
        try {
            ref.atomicGet();
            fail();
        } catch (LockedException ex) {
        }

        assertOrecValue(ref, orecValue);
        assertRefHasExclusiveLock(ref, otherTx);
        assertVersionAndValue(ref, version, initialValue);
    }

    @Test
    public void whenReadBiasedAndEnsuredByOther_thenLockedException() {
        boolean initialValue = true;
        GammaBooleanRef ref = makeReadBiased(new GammaBooleanRef(stm, initialValue));
        long version = ref.getVersion();

        GammaTransaction otherTx = stm.newDefaultTransaction();
        ref.getLock().acquire(otherTx, LockMode.Exclusive);

        long orecValue = ref.orec;
        try {
            ref.atomicGet();
            fail();
        } catch (LockedException expected) {
        }

        assertOrecValue(ref,orecValue);
        assertVersionAndValue(ref, version, initialValue);
    }
}
