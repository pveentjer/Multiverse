package org.multiverse.commitbarriers;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;
import static org.multiverse.api.ThreadLocalTransaction.clearThreadLocalTransaction;

public class VetoCommitBarrier_registerOnCommitTaskTest {

    @Before
    public void setUp() {
        clearThreadLocalTransaction();
    }

    @Test
    public void whenNullTask_thenNullPointerException() {
        VetoCommitBarrier barrier = new VetoCommitBarrier();

        try {
            barrier.registerOnCommitTask(null);
            fail();
        } catch (NullPointerException expected) {
        }

        assertTrue(barrier.isClosed());
    }

    @Test
    public void whenAborted_thenTaskNotExecuted() {
        VetoCommitBarrier barrier = new VetoCommitBarrier();
        Runnable task = mock(Runnable.class);

        barrier.registerOnCommitTask(task);
        barrier.abort();

        verify(task, never()).run();
    }

    @Test
    public void whenCommitted_thenTaskExecuted() throws InterruptedException {
        VetoCommitBarrier barrier = new VetoCommitBarrier();
        Runnable task = mock(Runnable.class);

        barrier.registerOnCommitTask(task);
        barrier.atomicVetoCommit();

        verify(task, times(1)).run();
    }

    @Test
    public void whenTaskThrowsRuntimeException_thenOtherTasksNotExecuted() {
        VetoCommitBarrier barrier = new VetoCommitBarrier();
        Runnable task1 = mock(Runnable.class);
        doThrow(new FakeException()).when(task1).run();
        Runnable task2 = mock(Runnable.class);

        barrier.registerOnCommitTask(task1);
        barrier.registerOnCommitTask(task2);

        try {
            barrier.atomicVetoCommit();
            fail();
        } catch (FakeException expected) {
        }

        verify(task2, never()).run();
    }

    private static class FakeException extends RuntimeException {
    }

    @Test
    public void whenCommitted_thenCommitBarrierOpenException() {
        VetoCommitBarrier barrier = new VetoCommitBarrier();
        barrier.atomicVetoCommit();

        Runnable task = mock(Runnable.class);
        try {
            barrier.registerOnCommitTask(task);
            fail();
        } catch (CommitBarrierOpenException expected) {
        }

        assertTrue(barrier.isCommitted());
        verify(task, never()).run();
    }

    @Test
    public void whenAborted_thenCommitBarrierOpenException() {
        VetoCommitBarrier barrier = new VetoCommitBarrier();
        barrier.abort();

        Runnable task = mock(Runnable.class);
        try {
            barrier.registerOnCommitTask(task);
            fail();
        } catch (CommitBarrierOpenException expected) {
        }

        assertTrue(barrier.isAborted());
        verify(task, never()).run();
    }
}
