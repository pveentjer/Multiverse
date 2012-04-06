package org.multiverse.commitbarriers;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.stms.gamma.GammaStm;

import static org.junit.Assert.assertTrue;
import static org.multiverse.TestUtils.*;
import static org.multiverse.api.TxnThreadLocal.clearThreadLocalTxn;

public class CountDownCommitBarrier_countDownTest {
    private GammaStm stm;

    @Before
    public void setUp() {
        stm = new GammaStm();
        clearThreadLocalTxn();
    }

    @Test
    public void whenLastOne_thenBarrierOpened() {
        CountDownCommitBarrier barrier = new CountDownCommitBarrier(3);

        JoinCommitThread t1 = new JoinCommitThread(stm, barrier);
        JoinCommitThread t2 = new JoinCommitThread(stm, barrier);

        startAll(t1, t2);
        sleepMs(500);
        assertAlive(t1, t2);
        assertTrue(barrier.isClosed());

        barrier.countDown();

        assertTrue(barrier.isCommitted());
        joinAll(t1, t2);
        assertNothingThrown(t1,t2);
    }

    @Test
    public void whenNotLastOne() {
        CountDownCommitBarrier barrier = new CountDownCommitBarrier(4);

        JoinCommitThread t1 = new JoinCommitThread(stm, barrier);
        JoinCommitThread t2 = new JoinCommitThread(stm, barrier);

        startAll(t1, t2);
        sleepMs(500);
        assertAlive(t1, t2);
        assertTrue(barrier.isClosed());

        barrier.countDown();

        assertTrue(barrier.isClosed());
        sleepMs(500);
        assertAlive(t1, t2);
    }

    @Test
    public void whenAborted_thenIgnored() {
        CountDownCommitBarrier barrier = new CountDownCommitBarrier(1);
        barrier.abort();

        barrier.countDown();
        assertTrue(barrier.isAborted());
    }

    @Test
    public void whenCommitted_thenIgnored() {
        CountDownCommitBarrier barrier = new CountDownCommitBarrier(0);
        barrier.countDown();
        assertTrue(barrier.isCommitted());
    }
}
