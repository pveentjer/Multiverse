package org.multiverse.commitbarriers;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.multiverse.TestThread;
import org.multiverse.api.Transaction;
import org.multiverse.api.closures.AtomicVoidClosure;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactionalobjects.GammaIntRef;

import static org.junit.Assert.*;
import static org.multiverse.TestUtils.*;
import static org.multiverse.api.ThreadLocalTransaction.clearThreadLocalTransaction;

public class VetoCommitBarrier_vetoCommitTest {
    private GammaStm stm;

    @Before
    public void setUp() {
        stm = new GammaStm();
        clearThreadLocalTransaction();
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

        GammaIntRef ref1 = new GammaIntRef(stm);
        GammaIntRef ref2 = new GammaIntRef(stm);
        GammaIntRef ref3 = new GammaIntRef(stm);

        IncThread thread1 = new IncThread(ref1, barrier);
        IncThread thread2 = new IncThread(ref2, barrier);
        IncThread thread3 = new IncThread(ref3, barrier);

        startAll(thread1, thread2, thread3);

        sleepMs(500);
        barrier.atomicVetoCommit();
        joinAll(thread1, thread2, thread3);

        assertIsCommitted(thread1.tx);
        assertIsCommitted(thread2.tx);
        assertIsCommitted(thread3.tx);

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
        private final GammaIntRef ref;
        private final VetoCommitBarrier barrier;
        private Transaction tx;

        public IncThread(GammaIntRef ref, VetoCommitBarrier barrier) {
            super("IncThread");
            this.barrier = barrier;
            this.ref = ref;
        }

        @Override
        public void doRun() throws Exception {
            stm.getDefaultAtomicBlock().execute(new AtomicVoidClosure() {
                @Override
                public void execute(Transaction tx) throws Exception {
                    IncThread.this.tx = tx;
                    ref.getAndIncrement(tx, 1);
                    barrier.joinCommit(tx);
                }
            });
        }
    }
}
