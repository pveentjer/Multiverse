package org.multiverse.stms.gamma.transactions.fat;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.exceptions.DeadTransactionException;
import org.multiverse.api.exceptions.PreparedTransactionException;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactions.GammaTransaction;
import org.multiverse.stms.gamma.transactions.GammaTxnConfiguration;

import static org.junit.Assert.*;
import static org.multiverse.TestUtils.*;

public abstract class FatGammaTransaction_setAbortOnlyTest<T extends GammaTransaction> {

    protected GammaStm stm;

    @Before
    public void setUp() {
        stm = new GammaStm();
    }


    abstract protected T newTransaction();

    abstract protected T newTransaction(GammaTxnConfiguration config);

    @Test
    public void whenActive() {
        GammaTransaction tx = newTransaction();

        tx.setAbortOnly();

        assertIsActive(tx);
        assertTrue((Boolean) getField(tx, "abortOnly"));
    }

    @Test
    public void whenPrepared_thenPreparedTransactionException() {
        GammaTransaction tx = newTransaction();
        tx.prepare();

        try {
            tx.setAbortOnly();
            fail();
        } catch (PreparedTransactionException expected) {
        }

        assertIsAborted(tx);
        assertFalse((Boolean) getField(tx, "abortOnly"));
    }

    @Test
    public void whenAborted_thenDeadTransactionException() {
        GammaTransaction tx = newTransaction();
        tx.abort();

        try {
            tx.setAbortOnly();
            fail();
        } catch (DeadTransactionException expected) {
        }

        assertIsAborted(tx);
        assertFalse((Boolean) getField(tx, "abortOnly"));
    }

    @Test
    public void whenCommitted_thenDeadTransactionException() {
        GammaTransaction tx = newTransaction();
        tx.commit();

        try {
            tx.setAbortOnly();
            fail();
        } catch (DeadTransactionException expected) {
        }

        assertIsCommitted(tx);
        assertFalse((Boolean) getField(tx, "abortOnly"));
    }
}
