package org.multiverse.stms.gamma.transactionalobjects.txnref;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.exceptions.DeadTxnException;
import org.multiverse.api.exceptions.PreparedTxnException;
import org.multiverse.api.exceptions.TxnMandatoryException;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactionalobjects.GammaTxnRef;
import org.multiverse.stms.gamma.transactions.GammaTxn;

import static org.junit.Assert.*;
import static org.multiverse.TestUtils.*;
import static org.multiverse.api.TxnThreadLocal.*;
import static org.multiverse.stms.gamma.GammaTestUtils.assertVersionAndValue;

public class GammaTxnRef_isNullTest {

    private GammaStm stm;

    @Before
    public void setUp() {
        stm = new GammaStm();
        clearThreadLocalTxn();
    }

    @Test
    public void whenNoTransactionAvailableAndNullValue_thenNoTransactionFoundException() {
        GammaTxnRef<String> ref = new GammaTxnRef<String>(stm);
        long initialVersion = ref.getVersion();

        try {
            ref.isNull();
            fail();
        } catch (TxnMandatoryException expected) {
        }

        assertVersionAndValue(ref, initialVersion, null);
    }

    @Test
    public void whenNoTransactionAvailableAndValue_thenNoTransactionFoundException() {
        String initialValue = "foo";
        GammaTxnRef<String> ref = new GammaTxnRef<String>(stm, initialValue);
        long initialVersion = ref.getVersion();

        try {
            ref.isNull();
            fail();
        } catch (TxnMandatoryException expected) {

        }

        assertVersionAndValue(ref, initialVersion, initialValue);
    }

    @Test
    public void whenActiveTransactionAvailable() {
        String initialValue = "foo";
        GammaTxnRef<String> ref = new GammaTxnRef<String>(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTxn tx = stm.newDefaultTxn();
        setThreadLocalTxn(tx);
        assertFalse(ref.isNull());
        ref.set(tx, null);
        assertTrue(ref.isNull());

        assertIsActive(tx);
        assertVersionAndValue(ref, initialVersion, initialValue);
    }

    @Test
    public void whenPreparedTransactionAvailable_thenPreparedTxnException() {
        String initialValue = "foo";
        GammaTxnRef<String> ref = new GammaTxnRef<String>(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTxn tx = stm.newDefaultTxn();
        setThreadLocalTxn(tx);
        tx.prepare();

        try {
            ref.isNull();
            fail();
        } catch (PreparedTxnException expected) {
        }

        assertIsAborted(tx);
        assertSame(tx, getThreadLocalTxn());
        assertEquals(initialVersion, ref.getVersion());
        assertVersionAndValue(ref, initialVersion, initialValue);
    }

    @Test
    public void whenCommittedTransactionAvailable_thenDeadTxnException() {
        String initialValue = "foo";
        GammaTxnRef<String> ref = new GammaTxnRef<String>(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTxn tx = stm.newDefaultTxn();
        tx.commit();
        setThreadLocalTxn(tx);

        try {
            ref.isNull();
            fail();
        } catch (DeadTxnException expected) {

        }

        assertIsCommitted(tx);
        assertSame(tx, getThreadLocalTxn());
        assertVersionAndValue(ref, initialVersion, initialValue);
    }

    @Test
    public void whenAbortedTransactionAvailable_thenDeadTxnException() {
        String initialValue = "foo";
        GammaTxnRef<String> ref = new GammaTxnRef<String>(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTxn tx = stm.newDefaultTxn();
        tx.abort();
        setThreadLocalTxn(tx);

        try {
            ref.isNull();
            fail();
        } catch (DeadTxnException expected) {

        }

        assertIsAborted(tx);
        assertSame(tx, getThreadLocalTxn());
        assertVersionAndValue(ref, initialVersion, initialValue);
    }
}
