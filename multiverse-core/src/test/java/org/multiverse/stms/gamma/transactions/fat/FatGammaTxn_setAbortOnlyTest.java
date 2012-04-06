package org.multiverse.stms.gamma.transactions.fat;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.exceptions.DeadTxnException;
import org.multiverse.api.exceptions.PreparedTxnException;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactions.GammaTxn;
import org.multiverse.stms.gamma.transactions.GammaTxnConfig;

import static org.junit.Assert.*;
import static org.multiverse.TestUtils.*;

public abstract class FatGammaTxn_setAbortOnlyTest<T extends GammaTxn> {

    protected GammaStm stm;

    @Before
    public void setUp() {
        stm = new GammaStm();
    }


    abstract protected T newTransaction();

    abstract protected T newTransaction(GammaTxnConfig config);

    @Test
    public void whenActive() {
        GammaTxn tx = newTransaction();

        tx.setAbortOnly();

        assertIsActive(tx);
        assertTrue((Boolean) getField(tx, "abortOnly"));
    }

    @Test
    public void whenPrepared_thenPreparedTxnException() {
        GammaTxn tx = newTransaction();
        tx.prepare();

        try {
            tx.setAbortOnly();
            fail();
        } catch (PreparedTxnException expected) {
        }

        assertIsAborted(tx);
        assertFalse((Boolean) getField(tx, "abortOnly"));
    }

    @Test
    public void whenAborted_thenDeadTxnException() {
        GammaTxn tx = newTransaction();
        tx.abort();

        try {
            tx.setAbortOnly();
            fail();
        } catch (DeadTxnException expected) {
        }

        assertIsAborted(tx);
        assertFalse((Boolean) getField(tx, "abortOnly"));
    }

    @Test
    public void whenCommitted_thenDeadTxnException() {
        GammaTxn tx = newTransaction();
        tx.commit();

        try {
            tx.setAbortOnly();
            fail();
        } catch (DeadTxnException expected) {
        }

        assertIsCommitted(tx);
        assertFalse((Boolean) getField(tx, "abortOnly"));
    }
}
