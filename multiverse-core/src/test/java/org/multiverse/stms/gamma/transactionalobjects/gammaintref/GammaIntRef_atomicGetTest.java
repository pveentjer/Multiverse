package org.multiverse.stms.gamma.transactionalobjects.gammaintref;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.LockMode;
import org.multiverse.api.exceptions.LockedException;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactionalobjects.GammaIntRef;
import org.multiverse.stms.gamma.transactions.GammaTransaction;

import static org.junit.Assert.*;
import static org.multiverse.TestUtils.assertOrecValue;
import static org.multiverse.api.ThreadLocalTransaction.*;
import static org.multiverse.stms.gamma.GammaTestUtils.*;

public class GammaIntRef_atomicGetTest {

    private GammaStm stm;

    @Before
    public void setUp() {
        stm = new GammaStm();
        clearThreadLocalTransaction();
    }

    @Test(expected = LockedException.class)
    public void whenUnconstructed() {
        GammaTransaction tx = stm.newDefaultTransaction();
        GammaIntRef ref = new GammaIntRef(tx);
        ref.atomicGet();
    }

    @Test
    public void whenActiveTransactionAvailable_thenIgnored() {
        GammaIntRef ref = new GammaIntRef(stm, 100);

        GammaTransaction tx = stm.newDefaultTransaction();
        setThreadLocalTransaction(tx);
        ref.set(10);

        assertEquals(100, ref.atomicGet());

        assertSame(tx, getThreadLocalTransaction());
    }

    @Test
    public void whenUpdatedBiasedOnUnlocked() {
        GammaIntRef ref = new GammaIntRef(stm, 100);

        long result = ref.atomicGet();
        assertEquals(100, result);
        //assertUpdateBiased(ref);
    }

    @Test
    public void whenUpdateBiasedAndPrivatizedByOther_thenLockedException() {
        GammaIntRef ref = new GammaIntRef(stm, 100);
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
        assertVersionAndValue(ref, version, 100);
    }

    @Test
    public void whenUpdateBiasedAndEnsuredByOther() {
        GammaIntRef ref = new GammaIntRef(stm, 100);
        long version = ref.getVersion();

        GammaTransaction otherTx = stm.newDefaultTransaction();
        ref.getLock().acquire(otherTx, LockMode.Write);

        long result = ref.atomicGet();

        assertEquals(100, result);
        assertSurplus(ref, 1);
        assertRefHasWriteLock(ref, otherTx);
        assertWriteBiased(ref);
        assertVersionAndValue(ref, version, 100);
    }

    @Test
    public void whenReadBiasedAndUnlocked() {
        GammaIntRef ref = makeReadBiased(new GammaIntRef(stm, 100));

        long result = ref.atomicGet();
        assertEquals(100, result);
        assertReadBiased(ref);
    }

    @Test
    public void whenReadBiasedAndPrivatizedByOther_thenLockedException() {
        GammaIntRef ref = makeReadBiased(new GammaIntRef(stm, 100));
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
        assertVersionAndValue(ref, version, 100);
    }

    @Test
    public void whenReadBiasedAndEnsuredByOther_thenLockedException() {
        GammaIntRef ref = makeReadBiased(new GammaIntRef(stm, 100));
        long version = ref.getVersion();

        GammaTransaction otherTx = stm.newDefaultTransaction();
        ref.getLock().acquire(otherTx, LockMode.Exclusive);

        long orecValue = ref.orec;
        try {
            ref.atomicGet();
            fail();
        } catch (LockedException expected) {
        }

        assertOrecValue(ref, orecValue);
        assertVersionAndValue(ref, version, 100);
    }
}
