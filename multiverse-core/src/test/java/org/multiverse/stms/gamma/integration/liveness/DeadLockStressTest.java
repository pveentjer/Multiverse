package org.multiverse.stms.gamma.integration.liveness;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.TestThread;
import org.multiverse.api.Txn;
import org.multiverse.api.TxnExecutor;
import org.multiverse.api.LockMode;
import org.multiverse.api.closures.TxnVoidClosure;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactionalobjects.GammaLongRef;
import org.multiverse.stms.gamma.transactions.GammaTxn;

import static org.multiverse.TestUtils.*;
import static org.multiverse.api.GlobalStmInstance.getGlobalStmInstance;
import static org.multiverse.api.TxnThreadLocal.clearThreadLocalTxn;

public class DeadLockStressTest {

    enum Mode {
        Normal, Mix, PrivatizeReadLevelMode, PrivatizeWriteLevelMode
    }

    private volatile boolean stop;
    private int refCount = 100;
    private int threadCount = 10;
    private GammaLongRef[] refs;
    private ChangeThread[] threads;
    private GammaStm stm;
    private Mode mode;

    @Before
    public void setUp() {
        clearThreadLocalTxn();
        stop = false;
        stm = (GammaStm) getGlobalStmInstance();
    }

    @Test
    public void whenNormal() {
        test(Mode.Normal);
    }

    @Test
    public void whenMix() {
        test(Mode.Mix);
    }

    @Test
    public void whenPessimisticReadLevel() {
        test(Mode.PrivatizeReadLevelMode);
    }

    @Test
    public void whenPessimisticWriteLevel() {
        test(Mode.PrivatizeWriteLevelMode);
    }

    public void test(Mode mode) {
        this.mode = mode;

        refs = new GammaLongRef[refCount];
        for (int k = 0; k < refCount; k++) {
            refs[k] = new GammaLongRef(stm);
        }

        threads = new ChangeThread[threadCount];
        for (int k = 0; k < threadCount; k++) {
            threads[k] = new ChangeThread(k);
        }

        startAll(threads);
        sleepMs(getStressTestDurationMs(60 * 1000));
        stop = true;
        joinAll(threads);
    }

    public class ChangeThread extends TestThread {

        private final TxnExecutor normalBlock = stm.newTxnFactoryBuilder()
                .newTxnExecutor();

        private final TxnExecutor pessimisticReadLevelBlock = stm.newTxnFactoryBuilder()
                .setReadLockMode(LockMode.Exclusive)
                .newTxnExecutor();

        private final TxnExecutor pessimisticWriteLevelBlock = stm.newTxnFactoryBuilder()
                .setWriteLockMode(LockMode.Exclusive)
                .newTxnExecutor();

        public ChangeThread(int id) {
            super("ChangeThread-" + id);
        }

        @Override
        public void doRun() throws Exception {
            int k = 0;
            while (!stop) {
                if (k % 100000 == 0) {
                    System.out.printf("%s is at %s\n", getName(), k);
                }
                switch (mode) {
                    case PrivatizeReadLevelMode:
                        privatizeReadLevel();
                        break;
                    case PrivatizeWriteLevelMode:
                        privatizeWriteLevel();
                        break;
                    case Normal:
                        normal();
                        break;
                    case Mix:
                        switch (randomInt(3)) {
                            case 0:
                                privatizeReadLevel();
                                break;
                            case 1:
                                privatizeWriteLevel();
                                break;
                            case 2:
                                normal();
                                break;
                            default:
                                throw new IllegalStateException();
                        }
                        break;
                    default:
                        throw new IllegalStateException();

                }
                k++;
            }
        }

        public void normal() {
            normalBlock.atomic(new TxnVoidClosure() {
                @Override
                public void execute(Txn tx) throws Exception {
                    doIt((GammaTxn) tx);
                }
            });
        }

        public void privatizeReadLevel() {
            pessimisticReadLevelBlock.atomic(new TxnVoidClosure() {
                @Override
                public void execute(Txn tx) throws Exception {
                    doIt((GammaTxn) tx);
                }
            });
        }

        public void privatizeWriteLevel() {
            pessimisticWriteLevelBlock.atomic(new TxnVoidClosure() {
                @Override
                public void execute(Txn tx) throws Exception {
                    doIt((GammaTxn) tx);
                }
            });
        }

        public void doIt(GammaTxn tx) {
            for (int k = 0; k < refs.length; k++) {
                if (!randomOneOf(10)) {
                    continue;
                }

                int index = randomInt(refs.length);
                GammaLongRef ref = refs[index];
                ref.getAndSet(tx, ref.get(tx) + 1);
            }
        }
    }
}
