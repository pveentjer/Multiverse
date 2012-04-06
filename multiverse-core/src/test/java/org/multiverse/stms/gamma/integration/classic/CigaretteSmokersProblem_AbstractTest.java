package org.multiverse.stms.gamma.integration.classic;

import org.junit.Before;
import org.multiverse.TestThread;
import org.multiverse.TestUtils;
import org.multiverse.api.Txn;
import org.multiverse.api.TxnExecutor;
import org.multiverse.api.closures.TxnBooleanClosure;
import org.multiverse.api.closures.TxnVoidClosure;
import org.multiverse.api.references.TxnBoolean;
import org.multiverse.api.references.TxnRef;
import org.multiverse.stms.gamma.GammaStm;

import static org.junit.Assert.assertEquals;
import static org.multiverse.TestUtils.*;
import static org.multiverse.api.GlobalStmInstance.getGlobalStmInstance;
import static org.multiverse.api.StmUtils.*;
import static org.multiverse.api.TxnThreadLocal.clearThreadLocalTxn;

/**
 * http://en.wikipedia.org/wiki/Cigarette_smokers_problem
 */
public abstract class CigaretteSmokersProblem_AbstractTest {
    private static final int SMOKE_TIME_SECONDS = 10;

    private TxnBoolean tobaccoAvailable;
    private TxnBoolean paperAvailable;
    private TxnBoolean matchesAvailable;
    private TxnRef<Thread> notifier;
    private ArbiterThread arbiterThread;
    private SmokerThread paperProvider;
    private SmokerThread matchProvider;
    private SmokerThread tobaccoProvider;
    private volatile boolean stop;
    private TxnExecutor executor;
    protected GammaStm stm;

    protected abstract TxnExecutor newBlock();

    @Before
    public void setUp() {
        clearThreadLocalTxn();
        stm = (GammaStm) getGlobalStmInstance();
        tobaccoAvailable = newTxnBoolean();
        paperAvailable = newTxnBoolean();
        matchesAvailable = newTxnBoolean();
        notifier = newTxnRef();
        arbiterThread = new ArbiterThread();
        paperProvider = new SmokerThread("PaperProviderThread", tobaccoAvailable, matchesAvailable);
        matchProvider = new SmokerThread("MatchProvidedThread", tobaccoAvailable, paperAvailable);
        tobaccoProvider = new SmokerThread("TobaccoProviderThread", paperAvailable, matchesAvailable);
        stop = false;
    }

    public void run() {
        executor = newBlock();

        startAll(arbiterThread, paperProvider, matchProvider, tobaccoProvider);
        sleepMs(60000);
        System.out.println("Stopping threads");
        stop = true;
        joinAll(arbiterThread, paperProvider, matchProvider, tobaccoProvider);

        System.out.println("MatchesAvailable: " + matchesAvailable.atomicGet());
        System.out.println("PaperAvailable: " + paperAvailable.atomicGet());
        System.out.println("TobaccoAvailable: " + tobaccoAvailable.atomicGet());

        assertEquals(arbiterThread.count,
                paperProvider.count + matchProvider.count + tobaccoProvider.count);
    }

    class ArbiterThread extends TestThread {
        private int count;

        public ArbiterThread() {
            super("Arbiter");
        }

        @Override
        public void doRun() throws Exception {
            while (!stop) {
                count++;
                switch (TestUtils.randomInt(3)) {
                    case 0:
                        executor.atomic(new TxnVoidClosure() {
                            @Override
                            public void call(Txn tx) {
                                if (notifier.get() != null) {
                                    retry();
                                }

                                tobaccoAvailable.set(true);
                                paperAvailable.set(true);
                                notifier.set(matchProvider);
                            }
                        });
                        break;
                    case 1:
                        executor.atomic(new TxnVoidClosure() {
                            @Override
                            public void call(Txn tx) {
                                if (notifier.get() != null) {
                                    retry();
                                }

                                tobaccoAvailable.set(true);
                                matchesAvailable.set(true);
                                notifier.set(paperProvider);
                            }
                        });
                        break;
                    case 2:
                        executor.atomic(new TxnVoidClosure() {
                            @Override
                            public void call(Txn tx) {
                                if (notifier.get() != null) {
                                    retry();
                                }

                                matchesAvailable.set(true);
                                paperAvailable.set(true);
                                notifier.set(tobaccoProvider);
                            }
                        });
                        break;
                    default:
                        throw new RuntimeException();
                }
            }
            executor.atomic(new TxnVoidClosure() {
                @Override
                public void call(Txn tx) throws Exception {
                    notifier.awaitNull();
                    notifier.set(arbiterThread);
                }
            });
        }
    }


    class SmokerThread extends TestThread {
        private int count;
        private TxnBoolean item1;
        private TxnBoolean item2;

        public SmokerThread(String name, TxnBoolean item1, TxnBoolean item2) {
            super(name);
            this.item1 = item1;
            this.item2 = item2;
        }

        @Override
        public void doRun() throws Exception {

            while (makeCigarette()) {
                sleepRandomMs(SMOKE_TIME_SECONDS);

                count++;
                if (count % 100 == 0) {
                    System.out.printf("%s is at %s\n", getName(), count);
                }
            }
        }

        private boolean makeCigarette() {
            return executor.atomic(new TxnBooleanClosure() {
                @Override
                public boolean call(Txn tx) throws Exception {
                    if (notifier.get() != SmokerThread.this) {
                        if (notifier.get() == arbiterThread) {
                            return false;
                        }

                        retry();
                    }

                    item1.set(false);
                    item2.set(false);
                    notifier.set(null);
                    return true;
                }
            });
        }
    }
}



