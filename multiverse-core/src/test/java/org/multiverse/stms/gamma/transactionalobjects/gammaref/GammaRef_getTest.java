package org.multiverse.stms.gamma.transactionalobjects.gammaref;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.multiverse.api.exceptions.DeadTxnException;
import org.multiverse.api.exceptions.PreparedTxnException;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactionalobjects.GammaRef;
import org.multiverse.stms.gamma.transactions.GammaTxn;

import static org.junit.Assert.fail;
import static org.multiverse.TestUtils.assertIsAborted;
import static org.multiverse.TestUtils.assertIsCommitted;
import static org.multiverse.api.TxnThreadLocal.clearThreadLocalTxn;
import static org.multiverse.stms.gamma.GammaTestUtils.assertRefHasNoLocks;
import static org.multiverse.stms.gamma.GammaTestUtils.assertVersionAndValue;

public class GammaRef_getTest {

    private GammaStm stm;

    @Before
    public void setUp() {
        stm = new GammaStm();
        clearThreadLocalTxn();
    }

    @Test
    @Ignore
    public void locked() {

    }

    @Test
    @Ignore
    public void locked_writeLockAcquired() {

    }

    @Test
    @Ignore
    public void locked_readLockAcquired() {

    }

    @Test
    @Ignore
    public void locked_exclusiveLockAcquired() {

    }

    @Test
    public void whenTransactionPrepared_thenPreparedTxnException() {
        String initialValue = "foo";
        GammaRef<String> ref = new GammaRef<String>(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTxn tx = stm.newDefaultTxn();
        tx.prepare();
        try {
            ref.get(tx);
            fail();
        } catch (PreparedTxnException expected) {
        }

        assertRefHasNoLocks(ref);
        assertVersionAndValue(ref, initialVersion, initialValue);
        assertIsAborted(tx);
    }

    @Test
    public void whenTransactionCommitted_thenDeadTxnException() {
        String initialValue = "foo";
        GammaRef<String> ref = new GammaRef<String>(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTxn tx = stm.newDefaultTxn();
        tx.commit();
        try {
            ref.get(tx);
            fail();
        } catch (DeadTxnException expected) {
        }

        assertRefHasNoLocks(ref);
        assertVersionAndValue(ref, initialVersion, initialValue);
        assertIsCommitted(tx);
    }

    @Test
    public void whenTransactionAborted_thenDeadTxnException() {
        String initialValue = "foo";
        GammaRef<String> ref = new GammaRef<String>(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTxn tx = stm.newDefaultTxn();
        tx.abort();
        try {
            ref.get(tx);
            fail();
        } catch (DeadTxnException expected) {
        }

        assertRefHasNoLocks(ref);
        assertVersionAndValue(ref, initialVersion, initialValue);
        assertIsAborted(tx);
    }
}
