package org.multiverse.stms.gamma.integration.failureatomicity;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.TestThread;
import org.multiverse.api.Txn;
import org.multiverse.api.TxnExecutor;
import org.multiverse.api.closures.TxnVoidClosure;
import org.multiverse.api.exceptions.DeadTxnException;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactionalobjects.GammaTxnLong;
import org.multiverse.stms.gamma.transactions.GammaTxn;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.multiverse.TestUtils.*;
import static org.multiverse.api.GlobalStmInstance.getGlobalStmInstance;
import static org.multiverse.api.TxnThreadLocal.clearThreadLocalTxn;

public class FailureAtomicityStressTest {

    private int modifyThreadCount = 10;
    private boolean stop;
    private GammaTxnLong ref;
    private GammaStm stm;

    @Before
    public void setUp() {
        clearThreadLocalTxn();
        stm = (GammaStm) getGlobalStmInstance();
        ref = new GammaTxnLong(stm);
        stop = false;
    }

    @Test
    public void test() {
        ModifyThread[] modifyThreads = new ModifyThread[modifyThreadCount];
        for (int k = 0; k < modifyThreadCount; k++) {
            modifyThreads[k] = new ModifyThread(k);
        }

        startAll(modifyThreads);

        sleepMs(getStressTestDurationMs(30 * 1000));
        stop = true;

        joinAll(modifyThreads);
        //since half of the transactions are going to be aborted we need to divide it by 2

        assertEquals(sum(modifyThreads), ref.atomicGet());
    }

    public long sum(ModifyThread[] threads) {
        long result = 0;
        for (ModifyThread thread : threads) {
            result += thread.writeCount;
        }
        return result;
    }

    public class ModifyThread extends TestThread {

        long writeCount;
        final TxnExecutor txnExecutor = stm.newTxnFactoryBuilder()
                .newTxnExecutor();

        public ModifyThread(int id) {
            super("ModifyThread-" + id);
        }

        @Override
        public void doRun() throws Exception {
            while (!stop) {
                if (writeCount % 500000 == 0) {
                    System.out.printf("%s is at %s\n", getName(), writeCount);
                }

                boolean abort = randomOneOf(10);
                if (abort) {
                    try {
                        modifyButAbort();
                        fail();
                    } catch (DeadTxnException ignore) {
                    }
                } else {
                    writeCount++;
                    modify();
                }
            }
        }

        private void modify() {
            txnExecutor.atomic(new TxnVoidClosure() {
                @Override
                public void call(Txn tx) throws Exception {
                    GammaTxn btx = (GammaTxn) tx;
                    long value = ref.get(btx);
                    ref.getAndSet(btx, value + 1);
                }
            });
        }

        private void modifyButAbort() {
            txnExecutor.atomic(new TxnVoidClosure() {
                @Override
                public void call(Txn tx) throws Exception {
                    GammaTxn btx = (GammaTxn) tx;
                    long value = ref.get(btx);
                    ref.getAndSet(btx, value + 1);
                    btx.abort();
                }
            });
        }
    }

}
