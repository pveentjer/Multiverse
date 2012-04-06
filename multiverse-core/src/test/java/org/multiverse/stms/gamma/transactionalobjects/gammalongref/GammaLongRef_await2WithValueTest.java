package org.multiverse.stms.gamma.transactionalobjects.gammalongref;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.multiverse.api.exceptions.DeadTxnException;
import org.multiverse.api.exceptions.PreparedTxnException;
import org.multiverse.api.exceptions.RetryNotAllowedException;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactionalobjects.GammaLongRef;
import org.multiverse.stms.gamma.transactions.GammaTxn;

import static org.junit.Assert.fail;
import static org.multiverse.TestUtils.assertIsAborted;
import static org.multiverse.api.TxnThreadLocal.clearThreadLocalTxn;

public class GammaLongRef_await2WithValueTest {
    private GammaStm stm;

    @Before
    public void setUp() {
        stm = new GammaStm();
        clearThreadLocalTxn();
    }

    @Test
    @Ignore
    public void whenSomeWaitingNeeded() {

    }

    @Test
    public void whenNullTransaction_thenNullPointerException() {
        GammaLongRef ref = new GammaLongRef(stm);

        try {
            ref.await(null, 10);
            fail();
        } catch (NullPointerException expected) {
        }
    }

    @Test
    public void whenPreparedTransaction_thenPreparedTxnException() {
        GammaLongRef ref = new GammaLongRef(stm);
        GammaTxn tx = stm.newDefaultTxn();
        tx.prepare();

        try {
            ref.await(tx, 10);
            fail();
        } catch (PreparedTxnException expected) {
        }

        assertIsAborted(tx);
    }

    @Test
    public void whenAbortedTransaction_thenDeadTxnException() {
        GammaLongRef ref = new GammaLongRef(stm);
        GammaTxn tx = stm.newDefaultTxn();
        tx.abort();

        try {
            ref.await(tx, 10);
            fail();
        } catch (DeadTxnException expected) {
        }

        assertIsAborted(tx);
    }

    @Test
    public void whenCommittedTransaction_thenDeadTxnException() {
        GammaLongRef ref = new GammaLongRef(stm);
        GammaTxn tx = stm.newDefaultTxn();
        tx.abort();

        try {
            ref.await(tx, 10);
            fail();
        } catch (DeadTxnException expected) {
        }

        assertIsAborted(tx);
    }

    @Test
    public void whenBlockingNotAllowed_thenRetryNotAllowedException() {
        GammaLongRef ref = new GammaLongRef(stm);
        GammaTxn tx = stm.newTxnFactoryBuilder()
                .setBlockingAllowed(false)
                .setSpeculative(false)
                .newTransactionFactory()
                .newTransaction();

        try {
            ref.await(tx, 10);
            fail();
        } catch (RetryNotAllowedException expected) {
        }

        assertIsAborted(tx);
    }
}
