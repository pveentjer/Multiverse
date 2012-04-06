package org.multiverse.stms.gamma.transactions.fat;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.exceptions.DeadTransactionException;
import org.multiverse.api.exceptions.PreparedTransactionException;
import org.multiverse.api.lifecycle.TransactionListener;
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
        TransactionListener listener = mock(TransactionListener.class);
        tx.register(listener);

        assertIsActive(tx);
        assertNotNull(tx.listeners);
        assertEquals(Arrays.asList(listener), tx.listeners);
        verifyZeroInteractions(listener);
    }

    @Test
    public void whenMultpleListeners() {
        T tx = newTransaction();
        TransactionListener listener1 = mock(TransactionListener.class);
        TransactionListener listener2 = mock(TransactionListener.class);
        TransactionListener listener3 = mock(TransactionListener.class);
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
        TransactionListener listener = mock(TransactionListener.class);

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

        TransactionListener listener = mock(TransactionListener.class);
        try {
            tx.register(listener);
            fail();
        } catch (PreparedTransactionException expected) {
        }

        assertIsAborted(tx);
        verifyZeroInteractions(listener);
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
    }
}
