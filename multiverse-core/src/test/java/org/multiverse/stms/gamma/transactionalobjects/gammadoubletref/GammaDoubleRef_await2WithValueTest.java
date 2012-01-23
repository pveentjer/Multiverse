package org.multiverse.stms.gamma.transactionalobjects.gammadoubletref;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.multiverse.api.exceptions.DeadTransactionException;
import org.multiverse.api.exceptions.PreparedTransactionException;
import org.multiverse.api.exceptions.RetryNotAllowedException;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactionalobjects.GammaDoubleRef;
import org.multiverse.stms.gamma.transactions.GammaTransaction;

import static org.junit.Assert.fail;
import static org.multiverse.TestUtils.assertIsAborted;
import static org.multiverse.api.ThreadLocalTransaction.clearThreadLocalTransaction;

public class GammaDoubleRef_await2WithValueTest {
    private GammaStm stm;

    @Before
    public void setUp() {
        stm = new GammaStm();
        clearThreadLocalTransaction();
    }

    @Test
    @Ignore
    public void whenSomeWaitingNeeded() {

    }

    @Test
    public void whenNullTransaction_thenNullPointerException() {
        GammaDoubleRef ref = new GammaDoubleRef(stm);

        try {
            ref.await(null, 10);
            fail();
        } catch (NullPointerException expected) {
        }
    }

    @Test
    public void whenPreparedTransaction_thenPreparedTransactionException() {
        GammaDoubleRef ref = new GammaDoubleRef(stm);
        GammaTransaction tx = stm.newDefaultTransaction();
        tx.prepare();

        try {
            ref.await(tx, 10);
            fail();
        } catch (PreparedTransactionException expected) {
        }

        assertIsAborted(tx);
    }

    @Test
    public void whenAbortedTransaction_thenDeadTransactionException() {
        GammaDoubleRef ref = new GammaDoubleRef(stm);
        GammaTransaction tx = stm.newDefaultTransaction();
        tx.abort();

        try {
            ref.await(tx, 10);
            fail();
        } catch (DeadTransactionException expected) {
        }

        assertIsAborted(tx);
    }

    @Test
    public void whenCommittedTransaction_thenDeadTransactionException() {
        GammaDoubleRef ref = new GammaDoubleRef(stm);
        GammaTransaction tx = stm.newDefaultTransaction();
        tx.abort();

        try {
            ref.await(tx, 10);
            fail();
        } catch (DeadTransactionException expected) {
        }

        assertIsAborted(tx);
    }

    @Test
    public void whenBlockingNotAllowed_thenRetryNotAllowedException() {
        GammaDoubleRef ref = new GammaDoubleRef(stm);
        GammaTransaction tx = stm.newTransactionFactoryBuilder()
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
