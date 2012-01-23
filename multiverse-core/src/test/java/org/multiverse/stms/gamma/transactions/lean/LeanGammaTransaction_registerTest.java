package org.multiverse.stms.gamma.transactions.lean;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.exceptions.DeadTransactionException;
import org.multiverse.api.exceptions.PreparedTransactionException;
import org.multiverse.api.exceptions.SpeculativeConfigurationError;
import org.multiverse.api.lifecycle.TransactionListener;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactions.GammaTransaction;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.multiverse.TestUtils.assertIsAborted;
import static org.multiverse.TestUtils.assertIsCommitted;

public abstract class LeanGammaTransaction_registerTest<T extends GammaTransaction> {

    public GammaStm stm;

    @Before
    public void setUp() {
        stm = new GammaStm();
    }

    public abstract T newTransaction();

    @Test
    public void whenNullListener_thenNullPointerException() {
        T tx = newTransaction();
        try {
            tx.register(null);
            fail();
        } catch (NullPointerException expected) {

        }

        assertIsAborted(tx);
        assertFalse(tx.getConfiguration().getSpeculativeConfiguration().listenersDetected);
    }

    @Test
    public void whenSuccess() {
        T tx = newTransaction();
        TransactionListener listener = mock(TransactionListener.class);

        try {
            tx.register(listener);
            fail();
        } catch (SpeculativeConfigurationError expected) {

        }

        assertIsAborted(tx);
        assertTrue(tx.getConfiguration().getSpeculativeConfiguration().listenersDetected);
        assertNull(tx.listeners);
        verifyZeroInteractions(listener);
    }

    @Test
    public void whenAlreadyPrepared() {
        T tx = newTransaction();
        tx.prepare();

        TransactionListener listener = mock(TransactionListener.class);
        try {
            tx.register(listener);
            fail();
        } catch (PreparedTransactionException expected) {
        }

        assertIsAborted(tx);
        verifyZeroInteractions(listener);
        assertFalse(tx.getConfiguration().getSpeculativeConfiguration().listenersDetected);
    }

    @Test
    public void whenAlreadyAborted() {
        T tx = newTransaction();
        tx.abort();

        TransactionListener listener = mock(TransactionListener.class);
        try {
            tx.register(listener);
            fail();
        } catch (DeadTransactionException expected) {
        }

        assertIsAborted(tx);
        verifyZeroInteractions(listener);
        assertFalse(tx.getConfiguration().getSpeculativeConfiguration().listenersDetected);
    }

    @Test
    public void whenAlreadyCommitted() {
        T tx = newTransaction();
        tx.commit();

        TransactionListener listener = mock(TransactionListener.class);
        try {
            tx.register(listener);
            fail();
        } catch (DeadTransactionException expected) {
        }

        assertIsCommitted(tx);
        verifyZeroInteractions(listener);
        assertFalse(tx.getConfiguration().getSpeculativeConfiguration().listenersDetected);
    }
}
