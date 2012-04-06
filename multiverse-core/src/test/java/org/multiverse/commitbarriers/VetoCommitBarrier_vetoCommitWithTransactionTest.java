package org.multiverse.commitbarriers;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.multiverse.TestThread;
import org.multiverse.api.Txn;
import org.multiverse.api.closures.TxnVoidClosure;
import org.multiverse.api.exceptions.DeadTxnException;
import org.multiverse.api.exceptions.ReadWriteConflict;
import org.multiverse.stms.gamma.GammaConstants;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactionalobjects.GammaTxnInteger;
import org.multiverse.stms.gamma.transactions.GammaTxn;

import static org.junit.Assert.*;
import static org.multiverse.TestUtils.*;
import static org.multiverse.api.TxnThreadLocal.clearThreadLocalTxn;

public class VetoCommitBarrier_vetoCommitWithTransactionTest implements GammaConstants {
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

        Txn tx = stm.newDefaultTxn();
        barrier.vetoCommit(tx);

        assertTrue(barrier.isCommitted());
        assertIsCommitted(tx);
    }

    @Test
    @Ignore
    public void whenPendingTransaction() throws InterruptedException {
        final VetoCommitBarrier barrier = new VetoCommitBarrier();

        final GammaTxnInteger ref = new GammaTxnInteger(stm);

        TestThread t = new TestThread() {
            @Override
            public void doRun() throws Exception {
                stm.getDefaultTxnExecutor().atomic(new TxnVoidClosure() {
                    @Override
                    public void call(Txn tx) throws Exception {
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
        assertNothingThrown(t);
        assertTrue(barrier.isCommitted());
        assertEquals(1, ref.atomicGet());
        assertEquals(0, barrier.getNumberWaiting());
    }


    @Test
    public void whenTransactionFailedToPrepare_thenBarrierNotAbortedOrCommitted() {
        final GammaTxnInteger ref = new GammaTxnInteger(stm);

        GammaTxn tx = stm.newDefaultTxn();
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
    public void whenTransactionAborted_thenDeadTxnException() {
        VetoCommitBarrier barrier = new VetoCommitBarrier();

        Txn tx = stm.newDefaultTxn();
        tx.abort();

        try {
            barrier.vetoCommit(tx);
            fail();
        } catch (DeadTxnException expected) {
        }

        assertIsAborted(tx);
        assertTrue(barrier.isClosed());
    }

    @Test
    public void whenTransactionCommitted_thenDeadTxnException() {
        VetoCommitBarrier barrier = new VetoCommitBarrier();

        Txn tx = stm.newDefaultTxn();
        tx.commit();

        try {
            barrier.vetoCommit(tx);
            fail();
        } catch (DeadTxnException expected) {
        }

        assertIsCommitted(tx);
        assertTrue(barrier.isClosed());
    }

    @Test
    public void whenBarrierCommitted_thenCommitBarrierOpenException() {
        VetoCommitBarrier barrier = new VetoCommitBarrier();
        barrier.atomicVetoCommit();

        Txn tx = stm.newDefaultTxn();
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

        Txn tx = stm.newDefaultTxn();
        try {
            barrier.vetoCommit(tx);
            fail();
        } catch (CommitBarrierOpenException expected) {
        }

        assertTrue(barrier.isAborted());
        assertIsActive(tx);
    }

}
