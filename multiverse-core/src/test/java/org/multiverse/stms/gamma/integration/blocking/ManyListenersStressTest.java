package org.multiverse.stms.gamma.integration.blocking;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.TestThread;
import org.multiverse.TestUtils;
import org.multiverse.api.Txn;
import org.multiverse.api.closures.TxnVoidClosure;
import org.multiverse.api.references.TxnLong;

import static org.multiverse.TestUtils.joinAll;
import static org.multiverse.TestUtils.startAll;
import static org.multiverse.api.StmUtils.*;
import static org.multiverse.api.TxnThreadLocal.clearThreadLocalTxn;

public class ManyListenersStressTest {

    private int refCount = 10;
    private int threadCount = 5;
    private volatile boolean stop;
    private TxnLong[] refs;
    private StressThread[] threads;

    @Before
    public void setUp() {
        clearThreadLocalTxn();
        stop = false;

        threads = new StressThread[threadCount];
        for (int k = 0; k < threadCount; k++) {
            threads[k] = new StressThread(k + 1, k == threadCount - 1 ? 1 : k + 2);
        }

        refs = new TxnLong[refCount];
        for (int k = 0; k < refCount; k++) {
            refs[k] = newTxnLong(0);
        }
    }

    @Test
    public void test() {
        refs[0].atomicSet(1);

        startAll(threads);
        TestUtils.sleepMs(30000);
        stop = true;
        refs[0].atomicSet(-1);
        joinAll(threads);
    }

    class StressThread extends TestThread {
        private int wakeup;
        long count;
        private int signal;

        public StressThread(int wakeup, int signal) {
            super("StressThread-" + wakeup);
            this.wakeup = wakeup;
            this.signal = signal;
        }

        @Override
        public void doRun() throws Exception {
            while (!stop) {
                atomic(new TxnVoidClosure() {
                    @Override
                    public void call(Txn tx) throws Exception {
                        for (TxnLong ref : refs) {
                            final long value = ref.get();

                            if (value == -1) {
                                return;
                            }

                            if (value == wakeup) {
                                ref.set(0);
                                refs[TestUtils.randomInt(refs.length)].set(signal);
                                return;
                            }
                        }

                        retry();
                    }
                });
                count++;
                if (count % 10000 == 0) {
                    System.out.printf("%s is at %s\n", getName(), count);
                }
            }
        }
    }
}
