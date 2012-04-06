package org.multiverse.stms.gamma.integration.classic;

import org.junit.Before;
import org.multiverse.TestThread;
import org.multiverse.TestUtils;
import org.multiverse.api.TxnExecutor;
import org.multiverse.api.Transaction;
import org.multiverse.api.closures.AtomicBooleanClosure;
import org.multiverse.api.closures.AtomicVoidClosure;
import org.multiverse.api.references.BooleanRef;
import org.multiverse.api.references.Ref;
import org.multiverse.stms.gamma.GammaStm;

import static org.junit.Assert.assertEquals;
import static org.multiverse.TestUtils.*;
import static org.multiverse.api.GlobalStmInstance.getGlobalStmInstance;
import static org.multiverse.api.StmUtils.*;
import static org.multiverse.api.ThreadLocalTransaction.clearThreadLocalTransaction;

/**
 * http://en.wikipedia.org/wiki/Cigarette_smokers_problem
 */
public abstract class CigaretteSmokersProblem_AbstractTest {
    private static final int SMOKE_TIME_SECONDS = 10;

    private BooleanRef tobaccoAvailable;
    private BooleanRef paperAvailable;
    private BooleanRef matchesAvailable;
    private Ref<Thread> notifier;
    private ArbiterThread arbiterThread;
    private SmokerThread paperProvider;
    private SmokerThread matchProvider;
    private SmokerThread tobaccoProvider;
    private volatile boolean stop;
    private TxnExecutor block;
    protected GammaStm stm;

    protected abstract TxnExecutor newBlock();

    @Before
    public void setUp() {
        clearThreadLocalTransaction();
        stm = (GammaStm) getGlobalStmInstance();
        tobaccoAvailable = newBooleanRef();
        paperAvailable = newBooleanRef();
        matchesAvailable = newBooleanRef();
        notifier = newRef();
        arbiterThread = new ArbiterThread();
        paperProvider = new SmokerThread("PaperProviderThread", tobaccoAvailable, matchesAvailable);
        matchProvider = new SmokerThread("MatchProvidedThread", tobaccoAvailable, paperAvailable);
        tobaccoProvider = new SmokerThread("TobaccoProviderThread", paperAvailable, matchesAvailable);
        stop = false;
    }

    public void run() {
        block = newBlock();

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
                        block.atomic(new AtomicVoidClosure() {
                            @Override
                            public void execute(Transaction tx) {
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
                        block.atomic(new AtomicVoidClosure() {
                            @Override
                            public void execute(Transaction tx) {
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
                        block.atomic(new AtomicVoidClosure() {
                            @Override
                            public void execute(Transaction tx) {
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
            block.atomic(new AtomicVoidClosure() {
                @Override
                public void execute(Transaction tx) throws Exception {
                    notifier.awaitNull();
                    notifier.set(arbiterThread);
                }
            });
        }
    }


    class SmokerThread extends TestThread {
        private int count;
        private BooleanRef item1;
        private BooleanRef item2;

        public SmokerThread(String name, BooleanRef item1, BooleanRef item2) {
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
            return block.atomic(new AtomicBooleanClosure() {
                @Override
                public boolean execute(Transaction tx) throws Exception {
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



