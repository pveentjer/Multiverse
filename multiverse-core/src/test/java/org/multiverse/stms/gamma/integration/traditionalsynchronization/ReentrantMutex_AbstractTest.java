package org.multiverse.stms.gamma.integration.traditionalsynchronization;

import org.junit.Before;
import org.multiverse.TestThread;
import org.multiverse.api.Txn;
import org.multiverse.api.closures.AtomicVoidClosure;
import org.multiverse.api.references.IntRef;
import org.multiverse.api.references.Ref;
import org.multiverse.stms.gamma.GammaTxnExecutor;
import org.multiverse.stms.gamma.GammaStm;

import static org.multiverse.TestUtils.*;
import static org.multiverse.api.GlobalStmInstance.getGlobalStmInstance;
import static org.multiverse.api.StmUtils.*;
import static org.multiverse.api.TxnThreadLocal.clearThreadLocalTxn;

public abstract class ReentrantMutex_AbstractTest {
    protected GammaStm stm;
    private int threadCount = 10;
    private ReentrantMutex mutex;
    private volatile boolean stop;

    @Before
    public void setUp() {
        stm = (GammaStm) getGlobalStmInstance();
        clearThreadLocalTxn();
        stop = false;
    }

    protected abstract GammaTxnExecutor newUnlockBlock();

    protected abstract GammaTxnExecutor newLockBlock();

    public void run() {
        mutex = new ReentrantMutex();
        StressThread[] threads = new StressThread[threadCount];
        for (int k = 0; k < threads.length; k++) {
            threads[k] = new StressThread(k);
        }

        startAll(threads);
        sleepMs(30000);
        stop = true;
        joinAll(threads);
    }

    class StressThread extends TestThread {
        public StressThread(int id) {
            super("StressThread-" + id);
        }

        @Override
        public void doRun() throws Exception {
            long count = 0;
            while (!stop) {
                mutex.lock(this);

                boolean nested = randomOneOf(3);
                if (nested) {
                    mutex.lock(this);
                }

                sleepRandomMs(100);
                mutex.unlock(this);

                if (nested) {
                    mutex.unlock(this);
                }

                count++;
                if (count % 10 == 0) {
                    System.out.printf("%s is at %s\n", getName(), count);
                }
            }
        }
    }

    class ReentrantMutex {
        private final Ref<Thread> owner = newRef();
        private final IntRef count = newIntRef();
        private final GammaTxnExecutor lockBlock = newLockBlock();
        private final GammaTxnExecutor unlockBlock = newUnlockBlock();

        public void lock(final Thread thread) {
            lockBlock.atomic(new AtomicVoidClosure() {
                @Override
                public void execute(Txn tx) throws Exception {
                    if (owner.get() == null) {
                        owner.set(thread);
                        count.increment();
                        return;
                    }

                    if (owner.get() == thread) {
                        count.increment();
                        return;
                    }

                    retry();
                }
            });
        }

        public void unlock(final Thread thread) {
            unlockBlock.atomic(new AtomicVoidClosure() {
                @Override
                public void execute(Txn tx) throws Exception {
                    if (owner.get() != thread) {
                        throw new IllegalMonitorStateException();
                    }

                    count.decrement();
                    if (count.get() == 0) {
                        owner.set(null);
                    }
                }
            });
        }
    }


}
