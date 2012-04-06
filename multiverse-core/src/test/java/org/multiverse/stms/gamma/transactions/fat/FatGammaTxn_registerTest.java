package org.multiverse.stms.gamma.transactions.fat;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.exceptions.DeadTxnException;
import org.multiverse.api.exceptions.PreparedTxnException;
import org.multiverse.api.lifecycle.TxnListener;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactions.GammaTxn;

import java.util.Arrays;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.multiverse.TestUtils.*;

public abstract class FatGammaTxn_registerTest<T extends GammaTxn> {

    protected GammaStm stm;

    @Before
    public void setUp() {
        stm = new GammaStm();
    }

    protected abstract T newTransaction();

    @Test
    public void whenNullListener_thenNullPointerException() {
        T tx = newTransaction();
        try {
            tx.register(null);
            fail();
        } catch (NullPointerException expected) {

        }

        assertIsAborted(tx);
    }

    @Test
    public void whenSuccess() {
        T tx = newTransaction();
        TxnListener listener = mock(TxnListener.class);
        tx.register(listener);

        assertIsActive(tx);
        assertNotNull(tx.listeners);
        assertEquals(Arrays.asList(listener), tx.listeners);
        verifyZeroInteractions(listener);
    }

    @Test
    public void whenMultpleListeners() {
        T tx = newTransaction();
        TxnListener listener1 = mock(TxnListener.class);
        TxnListener listener2 = mock(TxnListener.class);
        TxnListener listener3 = mock(TxnListener.class);
        tx.register(listener1);
        tx.register(listener2);
        tx.register(listener3);

        assertIsActive(tx);
        assertNotNull(tx.listeners);
        assertEquals(Arrays.asList(listener1, listener2, listener3), tx.listeners);
        verifyZeroInteractions(listener1);
        verifyZeroInteractions(listener2);
        verifyZeroInteractions(listener3);
    }

    @Test
    public void whenSameListenerAddedMultipleTimes() {
        T tx = newTransaction();
        TxnListener listener = mock(TxnListener.class);

        tx.register(listener);
        tx.register(listener);

        assertIsActive(tx);
        assertNotNull(tx.listeners);
        assertEquals(Arrays.asList(listener, listener), tx.listeners);
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
    }
}
