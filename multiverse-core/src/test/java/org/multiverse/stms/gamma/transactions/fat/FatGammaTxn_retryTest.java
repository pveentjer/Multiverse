package org.multiverse.stms.gamma.transactions.fat;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.multiverse.api.LockMode;
import org.multiverse.api.exceptions.*;
import org.multiverse.api.exceptions.DeadTxnException;
import org.multiverse.api.exceptions.PreparedTxnException;
import org.multiverse.api.functions.Function;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactionalobjects.GammaRef;
import org.multiverse.stms.gamma.transactions.GammaTxn;
import org.multiverse.stms.gamma.transactions.GammaTxnConfig;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.multiverse.TestUtils.assertIsAborted;
import static org.multiverse.TestUtils.assertIsCommitted;
import static org.multiverse.stms.gamma.GammaTestUtils.assertLockMode;
import static org.multiverse.stms.gamma.GammaTestUtils.assertRefHasNoLocks;

public abstract class FatGammaTxn_retryTest<T extends GammaTxn> {

    protected GammaStm stm;

    @Before
    public void setUp() {
        stm = new GammaStm();
    }

    protected abstract T newTransaction(GammaTxnConfig config);

    protected abstract T newTransaction();

    @Test
    @Ignore
    public void whenContainsRead() {

    }

    @Test
    @Ignore
    public void whenContainsWrite() {

    }

    @Test
    public void whenOnlyContainsConstructed() {
        GammaTxn tx = newTransaction();
        GammaRef<String> ref = new GammaRef<String>(tx, "foo");

        try {
            tx.retry();
            fail();
        } catch (RetryNotPossibleException expected) {
        }

        assertIsAborted(tx);
        assertLockMode(ref, LockMode.Exclusive);
    }

    @Test
    public void whenOnlyContainsCommute() {
        String intialValue = "initialValue";
        GammaRef<String> ref = new GammaRef<String>(stm, intialValue);

        GammaTxn tx = newTransaction();
        Function<String> function = mock(Function.class);
        ref.commute(tx, function);

        try {
            tx.retry();
            fail();
        } catch (RetryNotPossibleException expected) {
        }

        assertIsAborted(tx);
        assertRefHasNoLocks(ref);
        verifyZeroInteractions(function);
    }

    @Test
    public void whenUnused() {
        GammaTxn tx = newTransaction();

        try {
            tx.retry();
            fail();
        } catch (RetryNotPossibleException expected) {
        }

        assertIsAborted(tx);
    }

    @Test
    public void whenNoRetryAllowed() {
        GammaTxnConfig config = new GammaTxnConfig(stm);
        config.blockingAllowed = false;

        T tx = newTransaction(config);
        try {
            tx.retry();
            fail();
        } catch (RetryNotAllowedException expected) {
        }

        assertIsAborted(tx);
    }

    @Test
    public void whenAlreadyPrepared() {
        T tx = newTransaction();
        tx.prepare();

        try {
            tx.retry();
            fail();
        } catch (PreparedTxnException expected) {
        }

        assertIsAborted(tx);
    }

    @Test
    public void whenAlreadyAborted() {
        T tx = newTransaction();
        tx.abort();

        try {
            tx.retry();
            fail();
        } catch (DeadTxnException expected) {
        }

        assertIsAborted(tx);
    }

    @Test
    public void whenAlreadyCommitted() {
        T tx = newTransaction();
        tx.commit();

        try {
            tx.retry();
            fail();
        } catch (DeadTxnException expected) {
        }

        assertIsCommitted(tx);
    }
}
