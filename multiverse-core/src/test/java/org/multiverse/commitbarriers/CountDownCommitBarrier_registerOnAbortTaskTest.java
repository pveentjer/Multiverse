package org.multiverse.commitbarriers;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;
import static org.multiverse.api.TxnThreadLocal.clearThreadLocalTxn;

public class CountDownCommitBarrier_registerOnAbortTaskTest {

    @Before
    public void setUp() {
        clearThreadLocalTxn();
    }

    @Test
    public void whenNullTask_thenNullPointerException() {
        CountDownCommitBarrier barrier = new CountDownCommitBarrier(1);

        try {
            barrier.registerOnAbortTask(null);
            fail();
        } catch (NullPointerException expected) {
        }

        assertTrue(barrier.isClosed());
    }

    @Test
    public void whenAborted_thenTaskExecuted() {
        CountDownCommitBarrier barrier = new CountDownCommitBarrier(1);
        Runnable task = mock(Runnable.class);

        barrier.registerOnAbortTask(task);
        barrier.abort();

        verify(task, times(1)).run();
    }

    @Test
    public void whenCommitted_thenTaskNotExecuted() throws InterruptedException {
        CountDownCommitBarrier barrier = new CountDownCommitBarrier(1);
        Runnable task = mock(Runnable.class);

        barrier.registerOnAbortTask(task);
        barrier.countDown();

        verify(task, never()).run();
    }

    @Test
    public void whenTaskThrowsRuntimeException_thenOtherTasksNotExecuted() {
        CountDownCommitBarrier barrier = new CountDownCommitBarrier(1);
        Runnable task1 = mock(Runnable.class);
        doThrow(new FakeException()).when(task1).run();
        Runnable task2 = mock(Runnable.class);

        barrier.registerOnAbortTask(task1);
        barrier.registerOnAbortTask(task2);

        try {
            barrier.abort();
            fail();
        } catch (FakeException expected) {
        }

        verify(task2, never()).run();
    }

    static class FakeException extends RuntimeException {
    }

    @Test
    public void whenCommitted_thenCommitBarrierOpenException() {
        CountDownCommitBarrier barrier = new CountDownCommitBarrier(0);

        Runnable task = mock(Runnable.class);
        try {
            barrier.registerOnAbortTask(task);
            fail();
        } catch (CommitBarrierOpenException expected) {
        }

        assertTrue(barrier.isCommitted());
        verify(task, never()).run();
    }

    @Test
    public void whenAborted_thenCommitBarrierOpenException() {
        CountDownCommitBarrier barrier = new CountDownCommitBarrier(1);
        barrier.abort();

        Runnable task = mock(Runnable.class);
        try {
            barrier.registerOnAbortTask(task);
            fail();
        } catch (CommitBarrierOpenException expected) {
        }

        assertTrue(barrier.isAborted());
        verify(task, never()).run();
    }
}
