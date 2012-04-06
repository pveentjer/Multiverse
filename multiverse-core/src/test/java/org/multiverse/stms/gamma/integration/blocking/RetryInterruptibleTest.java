package org.multiverse.stms.gamma.integration.blocking;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.TestThread;
import org.multiverse.api.TxnExecutor;
import org.multiverse.api.Txn;
import org.multiverse.api.closures.TxnVoidClosure;
import org.multiverse.api.exceptions.RetryInterruptedException;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactionalobjects.GammaTxnLong;
import org.multiverse.stms.gamma.transactions.GammaTxn;

import static org.junit.Assert.assertTrue;
import static org.multiverse.TestUtils.assertAlive;
import static org.multiverse.TestUtils.sleepMs;
import static org.multiverse.api.GlobalStmInstance.getGlobalStmInstance;
import static org.multiverse.api.StmUtils.retry;
import static org.multiverse.api.TxnThreadLocal.clearThreadLocalTxn;

public class RetryInterruptibleTest {

    private GammaTxnLong ref;
    private GammaStm stm;

    @Before
    public void setUp() {
        clearThreadLocalTxn();
        stm = (GammaStm) getGlobalStmInstance();
        ref = new GammaTxnLong(stm);
    }

    @Test
    public void test() throws InterruptedException {
        ref = new GammaTxnLong(stm, 0);

        AwaitThread t = new AwaitThread();
        t.start();

        sleepMs(200);
        assertAlive(t);
        t.interrupt();

        t.join();
        assertTrue(t.wasInterrupted);
    }

    class AwaitThread extends TestThread {
        private boolean wasInterrupted;

        public void doRun() throws Exception {
            try {
                await();
            } catch (RetryInterruptedException e) {
                wasInterrupted = true;
            }
        }

        public void await() throws Exception {
            TxnExecutor block = stm.newTxnFactoryBuilder()
                    .setInterruptible(true)
                    .newTxnExecutor();

            block.atomicChecked(new TxnVoidClosure() {
                @Override
                public void execute(Txn tx) throws Exception {
                    GammaTxn btx = (GammaTxn) tx;
                    if (ref.get(btx) != 1) {
                        retry();
                    }
                }
            });
        }
    }
}
