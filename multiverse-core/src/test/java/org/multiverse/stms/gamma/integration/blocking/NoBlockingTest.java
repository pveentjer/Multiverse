package org.multiverse.stms.gamma.integration.blocking;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.TxnExecutor;
import org.multiverse.api.Txn;
import org.multiverse.api.closures.TxnVoidClosure;
import org.multiverse.api.exceptions.RetryNotAllowedException;
import org.multiverse.api.exceptions.RetryNotPossibleException;
import org.multiverse.api.functions.Functions;
import org.multiverse.api.references.TxnLong;
import org.multiverse.stms.gamma.transactionalobjects.GammaTxnLong;
import org.multiverse.stms.gamma.transactions.GammaTxn;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.multiverse.api.GlobalStmInstance.getGlobalStmInstance;
import static org.multiverse.api.StmUtils.*;
import static org.multiverse.api.TxnThreadLocal.clearThreadLocalTxn;

public class NoBlockingTest {

    @Before
    public void setUp() {
        clearThreadLocalTxn();
    }

    @Test
    public void whenNothingRead_thenNoRetryPossibleException() {
        try {
            atomic(new TxnVoidClosure() {
                @Override
                public void execute(Txn tx) throws Exception {
                    retry();
                }
            });
            fail();
        } catch (RetryNotPossibleException expected) {
        }
    }

    @Test
    public void whenContainsCommute_thenNoRetryPossibleException() {
        final TxnLong ref = newTxnLong();

        try {
            atomic(new TxnVoidClosure() {
                @Override
                public void execute(Txn tx) throws Exception {
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
            atomic(new TxnVoidClosure() {
                @Override
                public void execute(Txn tx) throws Exception {
                    GammaTxn btx = (GammaTxn) tx;
                    GammaTxnLong ref = new GammaTxnLong(btx);
                    retry();
                }
            });
            fail();
        } catch (RetryNotPossibleException expected) {
        }
    }

    @Test
    public void whenBlockingNotAllowed_thenNoBlockingRetryAllowedException() {
        final TxnLong ref = newTxnLong();

        TxnExecutor block = getGlobalStmInstance()
                .newTxnFactoryBuilder()
                .setBlockingAllowed(false)
                .newTxnExecutor();

        try {
            block.atomic(new TxnVoidClosure() {
                @Override
                public void execute(Txn tx) throws Exception {
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
