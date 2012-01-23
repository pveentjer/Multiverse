package org.multiverse.commitbarriers;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.multiverse.TestThread;
import org.multiverse.api.Transaction;
import org.multiverse.api.closures.AtomicVoidClosure;
import org.multiverse.api.exceptions.DeadTransactionException;
import org.multiverse.api.exceptions.ReadWriteConflict;
import org.multiverse.stms.gamma.GammaConstants;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactionalobjects.GammaIntRef;
import org.multiverse.stms.gamma.transactions.GammaTransaction;

import static org.junit.Assert.*;
import static org.multiverse.TestUtils.*;
import static org.multiverse.api.ThreadLocalTransaction.clearThreadLocalTransaction;

public class VetoCommitBarrier_vetoCommitWithTransactionTest implements GammaConstants {
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
    public void whenNullTx_thenNullPointerException() {
        VetoCommitBarrier barrier = new VetoCommitBarrier();

        try {
            barrier.vetoCommit(null);
            fail();
        } catch (NullPointerException expected) {
        }

        assertTrue(barrier.isClosed());
    }

    @Test
    public void whenNoPendingTransactions() {
        VetoCommitBarrier barrier = new VetoCommitBarrier();

        Transaction tx = stm.newDefaultTransaction();
        barrier.vetoCommit(tx);

        assertTrue(barrier.isCommitted());
        assertIsCommitted(tx);
    }

    @Test
    @Ignore
    public void whenPendingTransaction() throws InterruptedException {
        final VetoCommitBarrier barrier = new VetoCommitBarrier();

        final GammaIntRef ref = new GammaIntRef(stm);

        TestThread t = new TestThread() {
            @Override
            public void doRun() throws Exception {
                stm.getDefaultAtomicBlock().execute(new AtomicVoidClosure() {
                    @Override
                    public void execute(Transaction tx) throws Exception {
                        ref.incrementAndGet(tx, 1);
                        barrier.joinCommit(tx);
                    }
                });


            }
        };
        t.start();

        sleepMs(500);
        assertAlive(t);
        assertTrue(barrier.isClosed());

        barrier.atomicVetoCommit();
        t.join();
        t.assertNothingThrown();
        assertTrue(barrier.isCommitted());
        assertEquals(1, ref.atomicGet());
        assertEquals(0, barrier.getNumberWaiting());
    }


    @Test
    public void whenTransactionFailedToPrepare_thenBarrierNotAbortedOrCommitted() {
        final GammaIntRef ref = new GammaIntRef(stm);

        GammaTransaction tx = stm.newDefaultTransaction();
        ref.get(tx);

        //conflicting write
        ref.atomicIncrementAndGet(1);

        ref.incrementAndGet(tx, 1);

        VetoCommitBarrier barrier = new VetoCommitBarrier();
        try {
            barrier.vetoCommit(tx);
            fail();
        } catch (ReadWriteConflict expected) {
        }

        assertTrue(barrier.isClosed());
    }

    @Test
    public void whenTransactionAborted_thenDeadTransactionException() {
        VetoCommitBarrier barrier = new VetoCommitBarrier();

        Transaction tx = stm.newDefaultTransaction();
        tx.abort();

        try {
            barrier.vetoCommit(tx);
            fail();
        } catch (DeadTransactionException expected) {
        }

        assertIsAborted(tx);
        assertTrue(barrier.isClosed());
    }

    @Test
    public void whenTransactionCommitted_thenDeadTransactionException() {
        VetoCommitBarrier barrier = new VetoCommitBarrier();

        Transaction tx = stm.newDefaultTransaction();
        tx.commit();

        try {
            barrier.vetoCommit(tx);
            fail();
        } catch (DeadTransactionException expected) {
        }

        assertIsCommitted(tx);
        assertTrue(barrier.isClosed());
    }

    @Test
    public void whenBarrierCommitted_thenCommitBarrierOpenException() {
        VetoCommitBarrier barrier = new VetoCommitBarrier();
        barrier.atomicVetoCommit();

        Transaction tx = stm.newDefaultTransaction();
        try {
            barrier.vetoCommit(tx);
            fail();
        } catch (CommitBarrierOpenException expected) {
        }

        assertTrue(barrier.isCommitted());
        assertIsActive(tx);
    }

    @Test
    public void whenBarrierAborted_thenCommitBarrierOpenException() {
        VetoCommitBarrier barrier = new VetoCommitBarrier();
        barrier.abort();

        Transaction tx = stm.newDefaultTransaction();
        try {
            barrier.vetoCommit(tx);
            fail();
        } catch (CommitBarrierOpenException expected) {
        }

        assertTrue(barrier.isAborted());
        assertIsActive(tx);
    }

}
