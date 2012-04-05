package org.multiverse.stms.gamma.integration.blocking;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.TransactionExecutor;
import org.multiverse.api.Transaction;
import org.multiverse.api.closures.AtomicVoidClosure;
import org.multiverse.api.exceptions.RetryNotAllowedException;
import org.multiverse.api.exceptions.RetryNotPossibleException;
import org.multiverse.api.functions.Functions;
import org.multiverse.api.references.LongRef;
import org.multiverse.stms.gamma.transactionalobjects.GammaLongRef;
import org.multiverse.stms.gamma.transactions.GammaTransaction;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.multiverse.api.GlobalStmInstance.getGlobalStmInstance;
import static org.multiverse.api.StmUtils.*;
import static org.multiverse.api.ThreadLocalTransaction.clearThreadLocalTransaction;

public class NoBlockingTest {

    @Before
    public void setUp() {
        clearThreadLocalTransaction();
    }

    @Test
    public void whenNothingRead_thenNoRetryPossibleException() {
        try {
            atomic(new AtomicVoidClosure() {
                @Override
                public void execute(Transaction tx) throws Exception {
                    retry();
                }
            });
            fail();
        } catch (RetryNotPossibleException expected) {
        }
    }

    @Test
    public void whenContainsCommute_thenNoRetryPossibleException() {
        final LongRef ref = newLongRef();

        try {
            atomic(new AtomicVoidClosure() {
                @Override
                public void execute(Transaction tx) throws Exception {
                    ref.commute(Functions.incLongFunction());
                    retry();
                }
            });
            fail();
        } catch (RetryNotPossibleException expected) {
        }
    }

    @Test
    public void whenContainsConstructing_thenNoRetryPossibleException() {
        try {
            atomic(new AtomicVoidClosure() {
                @Override
                public void execute(Transaction tx) throws Exception {
                    GammaTransaction btx = (GammaTransaction) tx;
                    GammaLongRef ref = new GammaLongRef(btx);
                    retry();
                }
            });
            fail();
        } catch (RetryNotPossibleException expected) {
        }
    }

    @Test
    public void whenBlockingNotAllowed_thenNoBlockingRetryAllowedException() {
        final LongRef ref = newLongRef();

        TransactionExecutor block = getGlobalStmInstance()
                .newTransactionFactoryBuilder()
                .setBlockingAllowed(false)
                .newTransactionExecutor();

        try {
            block.atomic(new AtomicVoidClosure() {
                @Override
                public void execute(Transaction tx) throws Exception {
                    ref.set(1);
                    retry();
                }
            });
            fail();
        } catch (RetryNotAllowedException expected) {
        }

        assertEquals(0, ref.atomicGet());
    }
}
