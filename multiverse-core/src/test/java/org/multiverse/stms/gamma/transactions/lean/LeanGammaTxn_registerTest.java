package org.multiverse.stms.gamma.transactions.lean;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.exceptions.DeadTxnException;
import org.multiverse.api.exceptions.PreparedTxnException;
import org.multiverse.api.exceptions.SpeculativeConfigurationError;
import org.multiverse.api.lifecycle.TxnListener;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactions.GammaTxn;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.multiverse.TestUtils.assertIsAborted;
import static org.multiverse.TestUtils.assertIsCommitted;

public abstract class LeanGammaTxn_registerTest<T extends GammaTxn> {

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
        TxnListener listener = mock(TxnListener.class);

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

        TxnListener listener = mock(TxnListener.class);
        try {
            tx.register(listener);
            fail();
        } catch (PreparedTxnException expected) {
        }

        assertIsAborted(tx);
        verifyZeroInteractions(listener);
        assertFalse(tx.getConfiguration().getSpeculativeConfiguration().listenersDetected);
    }

    @Test
    public void whenAlreadyAborted() {
        T tx = newTransaction();
        tx.abort();

        TxnListener listener = mock(TxnListener.class);
        try {
            tx.register(listener);
            fail();
        } catch (DeadTxnException expected) {
        }

        assertIsAborted(tx);
        verifyZeroInteractions(listener);
        assertFalse(tx.getConfiguration().getSpeculativeConfiguration().listenersDetected);
    }

    @Test
    public void whenAlreadyCommitted() {
        T tx = newTransaction();
        tx.commit();

        TxnListener listener = mock(TxnListener.class);
        try {
            tx.register(listener);
            fail();
        } catch (DeadTxnException expected) {
        }

        assertIsCommitted(tx);
        verifyZeroInteractions(listener);
        assertFalse(tx.getConfiguration().getSpeculativeConfiguration().listenersDetected);
    }
}
