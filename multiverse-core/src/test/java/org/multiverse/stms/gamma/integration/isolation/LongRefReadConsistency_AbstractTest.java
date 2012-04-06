package org.multiverse.stms.gamma.integration.isolation;

import org.junit.After;
import org.junit.Before;
import org.multiverse.TestThread;
import org.multiverse.api.TxnExecutor;
import org.multiverse.api.Transaction;
import org.multiverse.api.closures.AtomicVoidClosure;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactionalobjects.GammaLongRef;
import org.multiverse.stms.gamma.transactions.GammaTransaction;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertFalse;
import static org.multiverse.TestUtils.*;
import static org.multiverse.api.ThreadLocalTransaction.clearThreadLocalTransaction;

/**
 * Question: could the problem be in the quick release mechanism?
 * <p/>
 * Problem?
 * if a writing transaction has done n updates (and has released the updates) and has m to go.
 * If a reading transaction reads the n updates, there is no reason for the updating transaction to cause
 * a conflict since they are no conflicting arrives on the part if has already completes. If the reading transactions
 * hits the n+1 update, it is allowed to see a different value than it already has read...
 * problem.. the n updates it has read, already contains the new values, so reading another new value is no problem.
 */
public abstract class LongRefReadConsistency_AbstractTest {

    private GammaLongRef[] refs;

    private int readerCount = 10;
    private int writerCount = 2;
    private long durationMs = 1 * 60 * 1000;
    private volatile boolean stop;
    protected GammaStm stm;
    protected final AtomicBoolean inconsistencyDetected = new AtomicBoolean();

    @Before
    public void setUp() {
        clearThreadLocalTransaction();
        stop = false;
        stm = new GammaStm();
        inconsistencyDetected.set(false);
    }

    @After
    public void tearDown() {
        System.out.println("Stm.GlobalConflictCount: " + stm.getGlobalConflictCounter().count());
        for (GammaLongRef ref : refs) {
            System.out.println(ref.toDebugString());
        }
    }

    protected abstract TxnExecutor createReadBlock();

    protected abstract TxnExecutor createWriteBlock();

    public void run(int refCount) {
        refs = new GammaLongRef[refCount];
        for (int k = 0; k < refs.length; k++) {
            refs[k] = new GammaLongRef(stm);
        }

        ReadThread[] readerThreads = new ReadThread[readerCount];
        for (int k = 0; k < readerThreads.length; k++) {
            readerThreads[k] = new ReadThread(k);
        }

        WriterThread[] writerThreads = new WriterThread[writerCount];
        for (int k = 0; k < writerThreads.length; k++) {
            writerThreads[k] = new WriterThread(k);
        }

        startAll(readerThreads);
        startAll(writerThreads);
        System.out.printf("Running for %s milliseconds\n", durationMs);
        sleepMs(getStressTestDurationMs(durationMs));
        stop = true;
        joinAll(readerThreads);
        joinAll(writerThreads);
        assertFalse(inconsistencyDetected.get());
    }

    public class WriterThread extends TestThread {

        private int id;

        public WriterThread(int id) {
            super("WriterThread-" + id);
            this.id = id;
        }

        @Override
        public void doRun() throws Exception {
            TxnExecutor block = createWriteBlock();
            AtomicVoidClosure closure = new AtomicVoidClosure() {
                @Override
                public void execute(Transaction tx) throws Exception {
                    GammaTransaction btx = (GammaTransaction) tx;
                    for (int k = 0; k < refs.length; k++) {
                        refs[k].set(btx, id);
                    }
                }
            };

            int mod = 1;
            int k = 0;
            while (!stop) {
                block.atomic(closure);
                sleepRandomUs(100);

                k++;

                if (k % mod == 0) {
                    mod = mod * 2;
                    System.out.printf("%s is at %s\n", getName(), k);
                }
            }
        }
    }

    public class ReadThread extends TestThread {

        public ReadThread(int id) {
            super("ReadThread-" + id);
        }

        @Override
        public void doRun() throws Exception {
            TxnExecutor block = createReadBlock();

            AtomicVoidClosure closure = new AtomicVoidClosure() {
                @Override
                public void execute(Transaction tx) throws Exception {
                    GammaTransaction btx = (GammaTransaction) tx;

                    long initial = refs[0].get(btx);

                    for (int k = 1; k < refs.length; k++) {
                        long s = refs[k].get(btx);
                        if(initial!=s){
                            inconsistencyDetected.set(true);
                            stop = true;
                            System.out.printf("Inconsistency detected at index %s!!\n",k);
                        }
                    }
                }
            };

            int mod = 1;
            int k = 0;
            while (!stop) {
                block.atomic(closure);
                k++;

                if (k % mod == 0) {
                    mod = mod * 2;
                    System.out.printf("%s is at %s\n", getName(), k);
                }
            }
        }
    }
}
