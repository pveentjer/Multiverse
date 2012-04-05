package org.multiverse.stms.gamma.integration.isolation;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.TestThread;
import org.multiverse.api.TransactionExecutor;
import org.multiverse.api.Transaction;
import org.multiverse.api.closures.AtomicVoidClosure;
import org.multiverse.api.exceptions.DeadTransactionException;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactionalobjects.GammaLongRef;
import org.multiverse.stms.gamma.transactions.GammaTransaction;

import static org.junit.Assert.fail;
import static org.multiverse.TestUtils.*;
import static org.multiverse.api.GlobalStmInstance.getGlobalStmInstance;
import static org.multiverse.api.ThreadLocalTransaction.clearThreadLocalTransaction;

/**
 * A Stresstest that check if the it is possible that a dirty read is done (this is not allowed).
 *
 * @author Peter Veentjer.
 */
public class ReadCommittedStressTest {
    private GammaLongRef ref;

    private int readThreadCount = 10;
    private int modifyThreadCount = 2;

    private volatile boolean stop;
    private GammaStm stm;

    @Before
    public void setUp() {
        clearThreadLocalTransaction();
        stm = (GammaStm) getGlobalStmInstance();
        ref = new GammaLongRef(stm);
        stop = false;
    }

    @Test
    public void test() {
        FailingModifyThread[] modifyThreads = new FailingModifyThread[modifyThreadCount];
        for (int k = 0; k < modifyThreadCount; k++) {
            modifyThreads[k] = new FailingModifyThread(k);
        }

        ReadThread[] readerThread = new ReadThread[readThreadCount];
        for (int k = 0; k < readThreadCount; k++) {
            readerThread[k] = new ReadThread(k);
        }

        startAll(modifyThreads);
        startAll(readerThread);
        sleepMs(getStressTestDurationMs(30 * 1000));
        stop = true;
        joinAll(modifyThreads);
        joinAll(readerThread);
    }

    class FailingModifyThread extends TestThread {

        public FailingModifyThread(int threadId) {
            super("FailingModifyThread-" + threadId);
        }

        @Override
        public void doRun() {
            TransactionExecutor block = stm.getDefaultTransactionExecutor();
            AtomicVoidClosure closure = new AtomicVoidClosure() {
                @Override
                public void execute(Transaction tx) throws Exception {
                    GammaTransaction btx = (GammaTransaction) tx;
                    ref.getAndSet(btx, ref.get(btx));
                    btx.abort();
                }
            };

            while (!stop) {
                try {
                    block.atomic(closure);
                    fail();
                } catch (DeadTransactionException ignore) {
                }

                sleepRandomMs(10);
            }
        }
    }

    class ReadThread extends TestThread {


        public ReadThread(int threadId) {
            super("ReadThread-" + threadId);
        }

        @Override
        public void doRun() {
            AtomicVoidClosure closure = new AtomicVoidClosure() {
                @Override
                public void execute(Transaction tx) throws Exception {
                    GammaTransaction btx = (GammaTransaction) tx;

                    if (ref.get(btx) % 2 != 0) {
                        fail();
                    }
                }
            };

            TransactionExecutor readonlyReadtrackingBlock = stm.newTransactionFactoryBuilder()
                    .setReadonly(true)
                    .setReadTrackingEnabled(true)
                    .newTransactionExecutor();

            TransactionExecutor updateReadtrackingBlock = stm.newTransactionFactoryBuilder()
                    .setReadonly(false)
                    .setReadTrackingEnabled(true)
                    .newTransactionExecutor();

            int k = 0;
            while (!stop) {
                switch (k % 2) {
                    case 0:
                        readonlyReadtrackingBlock.atomic(closure);
                        break;
                    case 1:
                    case 3:
                        updateReadtrackingBlock.atomic(closure);
                        break;
                    default:
                        throw new IllegalStateException();
                }

                k++;
                sleepRandomMs(5);
            }
        }
    }
}
