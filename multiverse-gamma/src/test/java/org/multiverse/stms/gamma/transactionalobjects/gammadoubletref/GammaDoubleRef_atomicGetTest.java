package org.multiverse.stms.gamma.transactionalobjects.gammadoubletref;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.LockMode;
import org.multiverse.api.exceptions.LockedException;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactionalobjects.GammaDoubleRef;
import org.multiverse.stms.gamma.transactions.GammaTransaction;

import static org.junit.Assert.*;
import static org.multiverse.TestUtils.assertEqualsDouble;
import static org.multiverse.TestUtils.assertOrecValue;
import static org.multiverse.api.ThreadLocalTransaction.*;
import static org.multiverse.stms.gamma.GammaTestUtils.*;

public class GammaDoubleRef_atomicGetTest {

    private GammaStm stm;

    @Before
    public void setUp() {
        stm = new GammaStm();
        clearThreadLocalTransaction();
    }

    @Test(expected = LockedException.class)
    public void whenUnconstructed() {
        GammaTransaction tx = stm.newDefaultTransaction();
        GammaDoubleRef ref = new GammaDoubleRef(tx);
        ref.atomicGet();
    }

    @Test
    public void whenActiveTransactionAvailable_thenIgnored() {
        GammaDoubleRef ref = new GammaDoubleRef(stm, 100);

        GammaTransaction tx = stm.newDefaultTransaction();
        setThreadLocalTransaction(tx);
        ref.set(10);

        assertEqualsDouble(100, ref.atomicGet());

        assertSame(tx, getThreadLocalTransaction());
    }

    @Test
    public void whenUpdatedBiasedOnUnlocked() {
        GammaDoubleRef ref = new GammaDoubleRef(stm, 100);

        double result = ref.atomicGet();
        assertEqualsDouble(100, result);
        //assertUpdateBiased(ref);
    }

    @Test
    public void whenUpdateBiasedAndPrivatizedByOther_thenLockedException() {
        GammaDoubleRef ref = new GammaDoubleRef(stm, 100);
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
        GammaDoubleRef ref = new GammaDoubleRef(stm, 100);
        long version = ref.getVersion();

        GammaTransaction otherTx = stm.newDefaultTransaction();
        ref.getLock().acquire(otherTx, LockMode.Write);

        double result = ref.atomicGet();

        assertEqualsDouble(100, result);
        assertSurplus(ref, 1);
        assertRefHasWriteLock(ref, otherTx);
        assertWriteBiased(ref);
        assertVersionAndValue(ref, version, 100);
    }

    @Test
    public void whenReadBiasedAndUnlocked() {
        GammaDoubleRef ref = makeReadBiased(new GammaDoubleRef(stm, 100));

        double result = ref.atomicGet();
        assertEqualsDouble(100, result);
        assertReadBiased(ref);
    }

    @Test
    public void whenReadBiasedAndPrivatizedByOther_thenLockedException() {
        GammaDoubleRef ref = makeReadBiased(new GammaDoubleRef(stm, 100));
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
        GammaDoubleRef ref = makeReadBiased(new GammaDoubleRef(stm, 100));
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
        assertVersionAndValue(ref, version, 100);
    }
}
