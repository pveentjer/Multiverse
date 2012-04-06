package org.multiverse.commitbarriers;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.multiverse.TestThread;
import org.multiverse.api.Txn;
import org.multiverse.api.closures.TxnVoidClosure;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactionalobjects.GammaTxnInteger;

import static org.junit.Assert.*;
import static org.multiverse.TestUtils.*;
import static org.multiverse.api.TxnThreadLocal.clearThreadLocalTxn;

public class VetoCommitBarrier_vetoCommitTest {
    private GammaStm stm;

    @Before
    public void setUp() {
        stm = new GammaStm();
        clearThreadLocalTxn();
        clearCurrentThreadInterruptedStatus();
    }

    @After
    public void tearDown() {
        clearCurrentThreadInterruptedStatus();
    }

    @Test
    public void whenNoPendingTransactions() {
        VetoCommitBarrier barrier = new VetoCommitBarrier();
        barrier.atomicVetoCommit();

        assertTrue(barrier.isCommitted());
    }

    @Test
    public void whenPendingTransactions() {
        VetoCommitBarrier barrier = new VetoCommitBarrier();

        GammaTxnInteger ref1 = new GammaTxnInteger(stm);
        GammaTxnInteger ref2 = new GammaTxnInteger(stm);
        GammaTxnInteger ref3 = new GammaTxnInteger(stm);

        IncThread thread1 = new IncThread(ref1, barrier);
        IncThread thread2 = new IncThread(ref2, barrier);
        IncThread thread3 = new IncThread(ref3, barrier);

        startAll(thread1, thread2, thread3);

        sleepMs(500);
        barrier.atomicVetoCommit();
        joinAll(thread1, thread2, thread3);

        assertIsCommitted(thread1.txn);
        assertIsCommitted(thread2.txn);
        assertIsCommitted(thread3.txn);

        assertEquals(1, ref1.atomicGet());
        assertEquals(1, ref2.atomicGet());
        assertEquals(1, ref3.atomicGet());
    }

    @Test
    public void whenBarrierCommitted_thenIgnored() {
        VetoCommitBarrier barrier = new VetoCommitBarrier();
        barrier.atomicVetoCommit();

        barrier.atomicVetoCommit();
        assertTrue(barrier.isCommitted());
    }

    @Test
    public void whenBarrierAborted_thenCommitBarrierOpenException() {
        VetoCommitBarrier barrier = new VetoCommitBarrier();
        barrier.abort();

        try {
            barrier.atomicVetoCommit();
            fail();
        } catch (CommitBarrierOpenException expected) {
        }
        assertTrue(barrier.isAborted());
    }

    public class IncThread extends TestThread {
        private final GammaTxnInteger ref;
        private final VetoCommitBarrier barrier;
        private Txn txn;

        public IncThread(GammaTxnInteger ref, VetoCommitBarrier barrier) {
            super("IncThread");
            this.barrier = barrier;
            this.ref = ref;
        }

        @Override
        public void doRun() throws Exception {
            stm.getDefaultTxnExecutor().atomic(new TxnVoidClosure() {
                @Override
                public void execute(Txn txn) throws Exception {
                    IncThread.this.txn = txn;
                    ref.getAndIncrement(txn, 1);
                    barrier.joinCommit(txn);
                }
            });
        }
    }
}
