package org.multiverse.stms.gamma.integration.commute;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.TestThread;
import org.multiverse.api.TxnExecutor;
import org.multiverse.api.Transaction;
import org.multiverse.api.closures.AtomicLongClosure;
import org.multiverse.api.functions.Functions;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactionalobjects.GammaLongRef;
import org.multiverse.stms.gamma.transactions.GammaTransaction;

import static org.junit.Assert.assertEquals;
import static org.multiverse.TestUtils.*;
import static org.multiverse.api.GlobalStmInstance.getGlobalStmInstance;
import static org.multiverse.api.ThreadLocalTransaction.clearThreadLocalTransaction;

public abstract class Commute_AbstractTest {
    protected GammaStm stm;
    private volatile boolean stop;
    private GammaLongRef[] refs;
    private int refCount = 10;
    private int workerCount = 2;

    @Before
    public void setUp() {
        clearThreadLocalTransaction();
        stm = (GammaStm) getGlobalStmInstance();
        stop = false;
    }

    protected abstract TxnExecutor newBlock();

    @Test
    public void test() {
        refs = new GammaLongRef[refCount];
        for (int k = 0; k < refCount; k++) {
            refs[k] = new GammaLongRef(stm);
        }

        WorkerThread[] workers = new WorkerThread[workerCount];
        for (int k = 0; k < workers.length; k++) {
            workers[k] = new WorkerThread(k);
        }

        startAll(workers);
        sleepMs(getStressTestDurationMs(30 * 1000));
        stop = true;
        joinAll(workers);

        assertEquals(count(workers), count(refs));
    }

    public long count(GammaLongRef[] refs) {
        long result = 0;
        for (GammaLongRef ref : refs) {
            result += ref.atomicGet();
        }
        return result;
    }

    public long count(WorkerThread[] threads) {
        long result = 0;
        for (WorkerThread thread : threads) {
            result += thread.count;
        }
        return result;
    }

    public class WorkerThread extends TestThread {

        private long count;

        public WorkerThread(int id) {
            super("CommuteThread-" + id);
        }

        @Override
        public void doRun() throws Exception {
            TxnExecutor block = newBlock();

            AtomicLongClosure commutingClosure = new AtomicLongClosure() {
                @Override
                public long execute(Transaction tx) throws Exception {
                    GammaTransaction btx = (GammaTransaction) tx;
                    for (int k = 0; k < refs.length; k++) {
                        refs[k].commute(btx, Functions.incLongFunction(1));
                    }
                    return refs.length;
                }
            };

            AtomicLongClosure nonCommutingClosure = new AtomicLongClosure() {
                @Override
                public long execute(Transaction tx) throws Exception {
                    GammaTransaction btx = (GammaTransaction) tx;
                    for (int k = 0; k < refs.length; k++) {
                        refs[k].openForWrite(btx, LOCKMODE_NONE).long_value++;
                    }
                    return refs.length;
                }
            };

            int k = 0;
            while (!stop) {
                AtomicLongClosure closure = randomOneOf(10) ? nonCommutingClosure : commutingClosure;
                count += block.atomic(closure);
                k++;

                if (k % 100000 == 0) {
                    System.out.printf("%s is at %s\n", getName(), k);
                }
            }

            System.out.printf("%s completed %s\n", getName(), k);
        }
    }
}
