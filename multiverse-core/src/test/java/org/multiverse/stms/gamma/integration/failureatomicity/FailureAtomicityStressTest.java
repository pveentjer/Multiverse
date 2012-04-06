package org.multiverse.stms.gamma.integration.failureatomicity;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.TestThread;
import org.multiverse.api.TxnExecutor;
import org.multiverse.api.Transaction;
import org.multiverse.api.closures.AtomicVoidClosure;
import org.multiverse.api.exceptions.DeadTransactionException;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactionalobjects.GammaLongRef;
import org.multiverse.stms.gamma.transactions.GammaTransaction;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.multiverse.TestUtils.*;
import static org.multiverse.api.GlobalStmInstance.getGlobalStmInstance;
import static org.multiverse.api.ThreadLocalTransaction.clearThreadLocalTransaction;

public class FailureAtomicityStressTest {

    private int modifyThreadCount = 10;
    private boolean stop;
    private GammaLongRef ref;
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
        final TxnExecutor txnExecutor = stm.newTransactionFactoryBuilder()
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
                    } catch (DeadTransactionException ignore) {
                    }
                } else {
                    writeCount++;
                    modify();
                }
            }
        }

        private void modify() {
            txnExecutor.atomic(new AtomicVoidClosure() {
                @Override
                public void execute(Transaction tx) throws Exception {
                    GammaTransaction btx = (GammaTransaction) tx;
                    long value = ref.get(btx);
                    ref.getAndSet(btx, value + 1);
                }
            });
        }

        private void modifyButAbort() {
            txnExecutor.atomic(new AtomicVoidClosure() {
                @Override
                public void execute(Transaction tx) throws Exception {
                    GammaTransaction btx = (GammaTransaction) tx;
                    long value = ref.get(btx);
                    ref.getAndSet(btx, value + 1);
                    btx.abort();
                }
            });
        }
    }

}
