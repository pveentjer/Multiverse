package org.multiverse.stms.gamma.transactions.fat;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.exceptions.DeadTxnException;
import org.multiverse.api.exceptions.PreparedTxnException;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactions.GammaTxn;

import static org.junit.Assert.*;
import static org.multiverse.TestUtils.*;

public abstract class FatGammaTxn_isAbortOnlyTest<T extends GammaTxn> {

    protected GammaStm stm;

    @Before
    public void setUp() {
        stm = new GammaStm();
    }

    protected abstract T newTransaction();

    @Test
    public void whenActiveAndNotSetAbortOnly() {
        T tx = newTransaction();

        boolean result = tx.isAbortOnly();

        assertFalse(result);
        assertIsActive(tx);
    }

    @Test
    public void whenActiveAndSetAbortOnly() {
        T tx = newTransaction();
        tx.setAbortOnly();

        boolean result = tx.isAbortOnly();

        assertTrue(result);
        assertIsActive(tx);
    }


    @Test
    public void whenPreparedAndNotSetAbortOnly() {
        T tx = newTransaction();
        tx.prepare();

        boolean result = tx.isAbortOnly();

        assertFalse(result);
        assertIsPrepared(tx);
    }

    @Test
    public void whenPreparedAndSetAbortOnly_thenPreparedTxnException() {
        T tx = newTransaction();
        tx.prepare();

        try {
            tx.setAbortOnly();
            fail();
        } catch (PreparedTxnException expected) {
        }

        assertIsAborted(tx);
    }

    @Test
    public void whenAborted_thenDeadTxnException() {
        T tx = newTransaction();
        tx.abort();
        try {
            tx.isAbortOnly();
            fail();
        } catch (DeadTxnException expected) {
        }

        assertIsAborted(tx);
    }

    @Test
    public void whenCommitted_thenDeadTxnException() {
        T tx = newTransaction();
        tx.commit();
        try {
            tx.isAbortOnly();
            fail();
        } catch (DeadTxnException expected) {
        }

        assertIsCommitted(tx);
    }
}
