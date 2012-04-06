package org.multiverse.stms.gamma.transactionalobjects.txnref;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.exceptions.DeadTxnException;
import org.multiverse.api.exceptions.PreparedTxnException;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactionalobjects.GammaTxnRef;
import org.multiverse.stms.gamma.transactions.GammaTxn;

import static org.junit.Assert.*;
import static org.multiverse.TestUtils.*;
import static org.multiverse.api.TxnThreadLocal.clearThreadLocalTxn;
import static org.multiverse.stms.gamma.GammaTestUtils.assertRefHasNoLocks;
import static org.multiverse.stms.gamma.GammaTestUtils.assertVersionAndValue;

public class GammaTxnRef_setTest {

    private GammaStm stm;

    @Before
    public void setUp() {
        stm = new GammaStm();
        clearThreadLocalTxn();
    }

    @Test
    public void test() {
        String initialValue = "foo";
        GammaTxnRef<String> ref = new GammaTxnRef<String>(stm, initialValue);
        long initialVersion = ref.getVersion();
        long initialOrec = ref.orec;

        GammaTxn tx = stm.newDefaultTxn();
        String newValue = "bar";
        String result = ref.set(tx, newValue);

        assertSame(newValue, result);
        assertIsActive(tx);
        assertVersionAndValue(ref, initialVersion, initialValue);
        assertRefHasNoLocks(ref);
    }

    @Test
    public void whenTransactionPrepared_thenPreparedTxnException() {
        String initialValue = "foo";
        GammaTxnRef<String> ref = new GammaTxnRef<String>(stm, initialValue);
        long initialVersion = ref.getVersion();
        long initialOrec = ref.orec;

        GammaTxn tx = stm.newDefaultTxn();
        tx.prepare();
        try {
            ref.set(tx, "bar");
            fail();
        } catch (PreparedTxnException expected) {
        }

        assertIsAborted(tx);
        assertVersionAndValue(ref, initialVersion, initialValue);
        assertEquals(initialOrec, ref.orec);
    }

    @Test
    public void whenTransactionAborted_thenDeadTxnException() {
        String initialValue = "foo";
        GammaTxnRef<String> ref = new GammaTxnRef<String>(stm, initialValue);
        long initialVersion = ref.getVersion();
        long initialOrec = ref.orec;

        GammaTxn tx = stm.newDefaultTxn();
        tx.abort();
        try {
            ref.set(tx, "bar");
            fail();
        } catch (DeadTxnException expected) {
        }

        assertIsAborted(tx);
        assertVersionAndValue(ref, initialVersion, initialValue);
        assertEquals(initialOrec, ref.orec);
    }

    @Test
    public void whenTransactionCommitted_thenDeadTxnException() {
        String initialValue = "foo";
        GammaTxnRef<String> ref = new GammaTxnRef<String>(stm, initialValue);
        long initialVersion = ref.getVersion();
        long initialOrec = ref.orec;

        GammaTxn tx = stm.newDefaultTxn();
        tx.commit();
        try {
            ref.set(tx, "bar");
            fail();
        } catch (DeadTxnException expected) {
        }

        assertIsCommitted(tx);
        assertVersionAndValue(ref, initialVersion, initialValue);
        assertEquals(initialOrec, ref.orec);
    }
}
