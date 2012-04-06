package org.multiverse.stms.gamma.integration.isolation;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.TestThread;
import org.multiverse.api.Txn;
import org.multiverse.api.TxnExecutor;
import org.multiverse.api.closures.TxnVoidClosure;
import org.multiverse.api.exceptions.DeadTxnException;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactionalobjects.GammaLongRef;
import org.multiverse.stms.gamma.transactions.GammaTxn;

import static org.junit.Assert.fail;
import static org.multiverse.TestUtils.*;
import static org.multiverse.api.GlobalStmInstance.getGlobalStmInstance;
import static org.multiverse.api.TxnThreadLocal.clearThreadLocalTxn;

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
        clearThreadLocalTxn();
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
            TxnExecutor block = stm.getDefaultTxnExecutor();
            TxnVoidClosure closure = new TxnVoidClosure() {
                @Override
                public void execute(Txn tx) throws Exception {
                    GammaTxn btx = (GammaTxn) tx;
                    ref.getAndSet(btx, ref.get(btx));
                    btx.abort();
                }
            };

            while (!stop) {
                try {
                    block.atomic(closure);
                    fail();
                } catch (DeadTxnException ignore) {
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
            TxnVoidClosure closure = new TxnVoidClosure() {
                @Override
                public void execute(Txn tx) throws Exception {
                    GammaTxn btx = (GammaTxn) tx;

                    if (ref.get(btx) % 2 != 0) {
                        fail();
                    }
                }
            };

            TxnExecutor readonlyReadtrackingBlock = stm.newTxnFactoryBuilder()
                    .setReadonly(true)
                    .setReadTrackingEnabled(true)
                    .newTxnExecutor();

            TxnExecutor updateReadtrackingBlock = stm.newTxnFactoryBuilder()
                    .setReadonly(false)
                    .setReadTrackingEnabled(true)
                    .newTxnExecutor();

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
