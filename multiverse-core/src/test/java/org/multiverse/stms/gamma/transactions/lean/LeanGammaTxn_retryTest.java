package org.multiverse.stms.gamma.transactions.lean;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.exceptions.DeadTxnException;
import org.multiverse.api.exceptions.PreparedTxnException;
import org.multiverse.api.exceptions.RetryNotAllowedException;
import org.multiverse.api.exceptions.RetryNotPossibleException;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactions.GammaTxn;
import org.multiverse.stms.gamma.transactions.GammaTxnConfig;

import static org.junit.Assert.fail;
import static org.multiverse.TestUtils.assertIsAborted;
import static org.multiverse.TestUtils.assertIsCommitted;

public abstract class LeanGammaTxn_retryTest<T extends GammaTxn> {

    protected GammaStm stm;

    @Before
    public void setUp() {
        stm = new GammaStm();
    }

    public abstract T newTransaction();

    public abstract T newTransaction(GammaTxnConfig config);

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
