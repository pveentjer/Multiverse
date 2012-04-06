package org.multiverse.stms.gamma.integration.traditionalsynchronization;

import org.junit.Before;
import org.multiverse.TestThread;
import org.multiverse.TestUtils;
import org.multiverse.api.TxnExecutor;
import org.multiverse.api.Transaction;
import org.multiverse.api.closures.AtomicVoidClosure;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactionalobjects.GammaRef;

import java.util.concurrent.atomic.AtomicLong;

import static org.junit.Assert.fail;
import static org.multiverse.TestUtils.*;
import static org.multiverse.api.GlobalStmInstance.getGlobalStmInstance;
import static org.multiverse.api.StmUtils.retry;
import static org.multiverse.api.ThreadLocalTransaction.clearThreadLocalTransaction;

/**
 * A StressTest that checks if a the Semaphore; a traditional synchronization structure can be build
 * using an STM.
 */
public abstract class Semaphore_AbstractTest {
    protected GammaStm stm;
    private volatile boolean stop;
    private int threadCount = 10;
    private int resourceCount = 5;
    private Semaphore semaphore;

    @Before
    public void setUp() {
        clearThreadLocalTransaction();
        stm = (GammaStm) getGlobalStmInstance();
        stop = false;
    }

    protected abstract TxnExecutor newDownBlock();

    protected abstract TxnExecutor newUpBlock();

    public void run() {
        semaphore = new Semaphore(resourceCount);

        WorkerThread[] workers = new WorkerThread[threadCount];
        for (int k = 0; k < threadCount; k++) {
            workers[k] = new WorkerThread(k);
        }

        startAll(workers);
        sleepMs(TestUtils.getStressTestDurationMs(30 * 1000));
        System.out.println("Terminating");
        stop = true;
        System.out.println(semaphore.ref.toDebugString());
        joinAll(workers);
    }

    class WorkerThread extends TestThread {
        long count;

        public WorkerThread(int id) {
            super("Producer-" + id);
        }

        @Override
        public void doRun() throws Exception {
            while (!stop) {
                semaphore.down();
                semaphore.up();
                count++;

                if (count % 1000000 == 0) {
                    System.out.printf("%s is at %s\n", getName(), count);
                }
            }
        }
    }

    class Semaphore {

        private GammaRef<Long> ref;
        private AtomicLong users = new AtomicLong();
        private TxnExecutor upBlock = newUpBlock();
        private TxnExecutor downBlock = newDownBlock();

        public Semaphore(int initial) {
            ref = new GammaRef<Long>(stm, new Long(initial));
        }

        public void up() {
            upBlock.atomic(new AtomicVoidClosure() {
                @Override
                public void execute(Transaction tx) throws Exception {
                    ref.set(ref.get() + 1);
                }
            });

            users.incrementAndGet();
            if (users.get() > resourceCount) {
                fail();
            }
        }

        public void down() {
            users.decrementAndGet();

            downBlock.atomic(new AtomicVoidClosure() {
                @Override
                public void execute(Transaction tx) throws Exception {
                    if (ref.get() == 0) {
                        retry();
                    }

                    ref.set(ref.get() - 1);
                }
            });
        }
    }

}
