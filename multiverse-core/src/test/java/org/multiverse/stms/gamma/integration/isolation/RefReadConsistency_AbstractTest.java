package org.multiverse.stms.gamma.integration.isolation;

import org.junit.After;
import org.junit.Before;
import org.multiverse.TestThread;
import org.multiverse.api.Txn;
import org.multiverse.api.TxnExecutor;
import org.multiverse.api.closures.AtomicVoidClosure;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactionalobjects.GammaRef;
import org.multiverse.stms.gamma.transactions.GammaTxn;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertFalse;
import static org.multiverse.TestUtils.*;
import static org.multiverse.api.GlobalStmInstance.getGlobalStmInstance;
import static org.multiverse.api.TxnThreadLocal.clearThreadLocalTxn;

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
public abstract class RefReadConsistency_AbstractTest {

    private GammaRef<String>[] refs;
    private final AtomicBoolean inconsistencyDetected = new AtomicBoolean();

    private int readerCount = 10;
    private int writerCount = 2;
    private int durationMs = 1 * 60 * 1000;
    private volatile boolean stop;
    protected GammaStm stm;

    @Before
    public void setUp() {
        clearThreadLocalTxn();
        stop = false;
        stm = (GammaStm) getGlobalStmInstance();
        inconsistencyDetected.set(false);
    }

    @After
    public void tearDown() {
        System.out.println("Stm.GlobalConflictCount: " + stm.getGlobalConflictCounter().count());
        for (GammaRef ref : refs) {
            System.out.println(ref.toDebugString());
        }
    }

    protected abstract TxnExecutor createReadBlock();

    protected abstract TxnExecutor createWriteBlock();

    public void run(int refCount) {
        refs = new GammaRef[refCount];
        for (int k = 0; k < refs.length; k++) {
            refs[k] = new GammaRef<String>(stm);
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
        assertFalse("Inconsistency detected", inconsistencyDetected.get());
    }

    public class WriterThread extends TestThread {

        public WriterThread(int id) {
            super("WriterThread-" + id);
        }

        @Override
        public void doRun() throws Exception {
            final String value = getName();

            TxnExecutor block = createWriteBlock();
            AtomicVoidClosure closure = new AtomicVoidClosure() {
                @Override
                public void execute(Txn tx) throws Exception {
                    GammaTxn btx = (GammaTxn) tx;
                    //String initial = refs[0].get(btx);

                    for (int k = 0; k < refs.length; k++) {
                        refs[k].openForWrite(btx, LOCKMODE_NONE).ref_value = value;
                        //String s = refs[k].getAndSet(tx, value);
                        //assertSame("failed at " + k, initial, s);
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
                public void execute(Txn tx) throws Exception {
                    GammaTxn btx = (GammaTxn) tx;

                    String initial = (String) refs[0].openForRead(btx, LOCKMODE_NONE).ref_value;

                    for (int k = 1; k < refs.length; k++) {
                        String s = (String) refs[k].openForRead(btx, LOCKMODE_NONE).ref_value;
                        if (s != initial) {
                            System.out.printf("Inconsistency detected at index %s!!!\n",k);
                            inconsistencyDetected.set(true);
                            stop = true;
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

