package org.multiverse.stms.gamma.transactionalobjects.gammaref;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.exceptions.DeadTransactionException;
import org.multiverse.api.exceptions.PreparedTransactionException;
import org.multiverse.api.exceptions.TransactionMandatoryException;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactionalobjects.GammaRef;
import org.multiverse.stms.gamma.transactions.GammaTransaction;

import static org.junit.Assert.*;
import static org.multiverse.TestUtils.*;
import static org.multiverse.api.ThreadLocalTransaction.*;
import static org.multiverse.stms.gamma.GammaTestUtils.assertVersionAndValue;

public class GammaRef_isNullTest {

    private GammaStm stm;

    @Before
    public void setUp() {
        stm = new GammaStm();
        clearThreadLocalTransaction();
    }

    @Test
    public void whenNoTransactionAvailableAndNullValue_thenNoTransactionFoundException() {
        GammaRef<String> ref = new GammaRef<String>(stm);
        long initialVersion = ref.getVersion();

        try {
            ref.isNull();
            fail();
        } catch (TransactionMandatoryException expected) {
        }

        assertVersionAndValue(ref, initialVersion, null);
    }

    @Test
    public void whenNoTransactionAvailableAndValue_thenNoTransactionFoundException() {
        String initialValue = "foo";
        GammaRef<String> ref = new GammaRef<String>(stm, initialValue);
        long initialVersion = ref.getVersion();

        try {
            ref.isNull();
            fail();
        } catch (TransactionMandatoryException expected) {

        }

        assertVersionAndValue(ref, initialVersion, initialValue);
    }

    @Test
    public void whenActiveTransactionAvailable() {
        String initialValue = "foo";
        GammaRef<String> ref = new GammaRef<String>(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTransaction tx = stm.newDefaultTransaction();
        setThreadLocalTransaction(tx);
        assertFalse(ref.isNull());
        ref.set(tx, null);
        assertTrue(ref.isNull());

        assertIsActive(tx);
        assertVersionAndValue(ref, initialVersion, initialValue);
    }

    @Test
    public void whenPreparedTransactionAvailable_thenPreparedTransactionException() {
        String initialValue = "foo";
        GammaRef<String> ref = new GammaRef<String>(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTransaction tx = stm.newDefaultTransaction();
        setThreadLocalTransaction(tx);
        tx.prepare();

        try {
            ref.isNull();
            fail();
        } catch (PreparedTransactionException expected) {
        }

        assertIsAborted(tx);
        assertSame(tx, getThreadLocalTransaction());
        assertEquals(initialVersion, ref.getVersion());
        assertVersionAndValue(ref, initialVersion, initialValue);
    }

    @Test
    public void whenCommittedTransactionAvailable_thenDeadTransactionException() {
        String initialValue = "foo";
        GammaRef<String> ref = new GammaRef<String>(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTransaction tx = stm.newDefaultTransaction();
        tx.commit();
        setThreadLocalTransaction(tx);

        try {
            ref.isNull();
            fail();
        } catch (DeadTransactionException expected) {

        }

        assertIsCommitted(tx);
        assertSame(tx, getThreadLocalTransaction());
        assertVersionAndValue(ref, initialVersion, initialValue);
    }

    @Test
    public void whenAbortedTransactionAvailable_thenDeadTransactionException() {
        String initialValue = "foo";
        GammaRef<String> ref = new GammaRef<String>(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTransaction tx = stm.newDefaultTransaction();
        tx.abort();
        setThreadLocalTransaction(tx);

        try {
            ref.isNull();
            fail();
        } catch (DeadTransactionException expected) {

        }

        assertIsAborted(tx);
        assertSame(tx, getThreadLocalTransaction());
        assertVersionAndValue(ref, initialVersion, initialValue);
    }
}
