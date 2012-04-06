package org.multiverse.commitbarriers;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.multiverse.TestThread;
import org.multiverse.api.Txn;
import org.multiverse.api.callables.TxnVoidCallable;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactionalobjects.GammaTxnInteger;

import static org.junit.Assert.assertEquals;
import static org.multiverse.TestUtils.*;
import static org.multiverse.api.TxnThreadLocal.clearThreadLocalTxn;
import static org.multiverse.api.TxnThreadLocal.getThreadLocalTxn;

public class VetoCommitBarrier_integrationTest {

    private VetoCommitBarrier barrier;
    private GammaStm stm;

    @Before
    public void setUp() {
        clearThreadLocalTxn();
        clearCurrentThreadInterruptedStatus();
        stm = new GammaStm();
    }

    @After
    public void tearDown() {
        clearCurrentThreadInterruptedStatus();
    }

    @Test
    public void test() throws InterruptedException {
        barrier = new VetoCommitBarrier();

        GammaTxnInteger ref1 = new GammaTxnInteger(stm);
        GammaTxnInteger ref2 = new GammaTxnInteger(stm);

        CommitThread t1 = new CommitThread(1, ref1);
        CommitThread t2 = new CommitThread(2, ref2);

        startAll(t1, t2);
        sleepMs(1000);

        barrier.atomicVetoCommit();

        joinAll(t1, t2);

        assertEquals(1, ref1.atomicGet());
        assertEquals(1, ref2.atomicGet());
    }

    @Test
    public void testAbort() throws InterruptedException {
        barrier = new VetoCommitBarrier();

        GammaTxnInteger ref1 = new GammaTxnInteger(stm);
        GammaTxnInteger ref2 = new GammaTxnInteger(stm);

        CommitThread t1 = new CommitThread(1, ref1);
        t1.setPrintStackTrace(false);
        CommitThread t2 = new CommitThread(2, ref2);
        t2.setPrintStackTrace(false);

        startAll(t1, t2);
        sleepMs(500);

        barrier.abort();

        t1.join();
        t2.join();

        assertEquals(0, ref1.atomicGet());
        assertEquals(0, ref2.atomicGet());
    }

    public class CommitThread extends TestThread {
        private GammaTxnInteger ref;

        public CommitThread(int id, GammaTxnInteger ref) {
            super("CommitThread-" + id);
            this.ref = ref;
        }

        @Override
        public void doRun() throws Exception {
            stm.getDefaultTxnExecutor().execute(new TxnVoidCallable() {
                @Override
                public void call(Txn tx) throws Exception {
                    ref.getAndIncrement(tx, 1);
                    barrier.joinCommit(getThreadLocalTxn());
                }
            });
        }
    }
}
