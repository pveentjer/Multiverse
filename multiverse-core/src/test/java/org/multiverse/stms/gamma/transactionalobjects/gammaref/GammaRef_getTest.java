package org.multiverse.stms.gamma.transactionalobjects.gammaref;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.multiverse.api.exceptions.DeadTransactionException;
import org.multiverse.api.exceptions.PreparedTransactionException;
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
    public void whenTransactionPrepared_thenPreparedTransactionException() {
        String initialValue = "foo";
        GammaRef<String> ref = new GammaRef<String>(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTxn tx = stm.newDefaultTransaction();
        tx.prepare();
        try {
            ref.get(tx);
            fail();
        } catch (PreparedTransactionException expected) {
        }

        assertRefHasNoLocks(ref);
        assertVersionAndValue(ref, initialVersion, initialValue);
        assertIsAborted(tx);
    }

    @Test
    public void whenTransactionCommitted_thenDeadTransactionException() {
        String initialValue = "foo";
        GammaRef<String> ref = new GammaRef<String>(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTxn tx = stm.newDefaultTransaction();
        tx.commit();
        try {
            ref.get(tx);
            fail();
        } catch (DeadTransactionException expected) {
        }

        assertRefHasNoLocks(ref);
        assertVersionAndValue(ref, initialVersion, initialValue);
        assertIsCommitted(tx);
    }

    @Test
    public void whenTransactionAborted_thenDeadTransactionException() {
        String initialValue = "foo";
        GammaRef<String> ref = new GammaRef<String>(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTxn tx = stm.newDefaultTransaction();
        tx.abort();
        try {
            ref.get(tx);
            fail();
        } catch (DeadTransactionException expected) {
        }

        assertRefHasNoLocks(ref);
        assertVersionAndValue(ref, initialVersion, initialValue);
        assertIsAborted(tx);
    }
}
