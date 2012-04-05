package org.multiverse.stms.gamma.integration.blocking;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.TestThread;
import org.multiverse.api.AtomicBlock;
import org.multiverse.api.Transaction;
import org.multiverse.api.closures.AtomicVoidClosure;
import org.multiverse.api.exceptions.RetryInterruptedException;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactionalobjects.GammaLongRef;
import org.multiverse.stms.gamma.transactions.GammaTransaction;

import static org.junit.Assert.assertTrue;
import static org.multiverse.TestUtils.assertAlive;
import static org.multiverse.TestUtils.sleepMs;
import static org.multiverse.api.GlobalStmInstance.getGlobalStmInstance;
import static org.multiverse.api.StmUtils.retry;
import static org.multiverse.api.ThreadLocalTransaction.clearThreadLocalTransaction;

public class RetryInterruptibleTest {

    private GammaLongRef ref;
    private GammaStm stm;

    @Before
    public void setUp() {
        clearThreadLocalTransaction();
        stm = (GammaStm) getGlobalStmInstance();
        ref = new GammaLongRef(stm);
    }

    @Test
    public void test() throws InterruptedException {
        ref = new GammaLongRef(stm, 0);

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
            AtomicBlock block = stm.newTransactionFactoryBuilder()
                    .setInterruptible(true)
                    .newAtomicBlock();

            block.atomicChecked(new AtomicVoidClosure() {
                @Override
                public void execute(Transaction tx) throws Exception {
                    GammaTransaction btx = (GammaTransaction) tx;
                    if (ref.get(btx) != 1) {
                        retry();
                    }
                }
            });
        }
    }
}
