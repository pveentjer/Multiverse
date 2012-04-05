package org.multiverse.stms.gamma;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.Transaction;
import org.multiverse.api.closures.AtomicVoidClosure;
import org.multiverse.api.exceptions.TransactionMandatoryException;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;
import static org.multiverse.api.StmUtils.*;
import static org.multiverse.api.ThreadLocalTransaction.clearThreadLocalTransaction;

public class StmUtilsTest {
    private GammaStm stm;

    @Before
    public void setUp() {
        clearThreadLocalTransaction();
        stm = new GammaStm();
    }

    @Test
    public void whenScheduleDeferredTaskAndNoTransactionAvailable() {
        Runnable task = mock(Runnable.class);

        try {
            scheduleDeferredTask(task);
            fail();
        } catch (TransactionMandatoryException expected) {
        }

        verifyZeroInteractions(task);
    }

    @Test(expected = NullPointerException.class)
    public void whenScheduleDeferredNullTask_thenNullPointerException() {
        scheduleDeferredTask(null);
    }

    @Test
    public void whenScheduleDeferredTaskAndCommit_thenCalled() {
        final Runnable task = mock(Runnable.class);

        stm.getDefaultTransactionExecutor().atomic(new AtomicVoidClosure() {
            @Override
            public void execute(Transaction tx) throws Exception {
                scheduleDeferredTask(task);
            }
        });

        verify(task).run();
    }

    @Test
    public void whenScheduleDeferredTaskAndAborted_thenNotCalled() {
        final Runnable task = mock(Runnable.class);

        try {
            stm.getDefaultTransactionExecutor().atomic(new AtomicVoidClosure() {
                @Override
                public void execute(Transaction tx) throws Exception {
                    scheduleDeferredTask(task);
                    throw new NonsenseException();
                }
            });
            fail();
        } catch (NonsenseException expected) {
        }

        verifyZeroInteractions(task);
    }


    class NonsenseException extends RuntimeException {
    }

    @Test
    public void whenScheduleCompensatingTaskAndNoTransactionAvailable_thenNoTransactionFoundException() {
        Runnable task = mock(Runnable.class);

        try {
            scheduleCompensatingTask(task);
            fail();
        } catch (TransactionMandatoryException expected) {
        }

        verifyZeroInteractions(task);
    }

    @Test(expected = NullPointerException.class)
    public void whenScheduleCompensatingNullTask_thenNullPointerException() {
        scheduleCompensatingTask(null);
    }

    @Test
    public void whenScheduleCompensatingTaskAndCommit_thenCalled() {
        final Runnable task = mock(Runnable.class);

        try {
            stm.getDefaultTransactionExecutor().atomic(new AtomicVoidClosure() {
                @Override
                public void execute(Transaction tx) throws Exception {
                    scheduleCompensatingTask(task);
                    throw new NonsenseException();
                }
            });
            fail();
        } catch (NonsenseException expected) {
        }

        verify(task).run();
    }

    @Test
    public void whenScheduleCompensatingTaskAndAborted_thenNotCalled() {

        final Runnable task = mock(Runnable.class);

        stm.getDefaultTransactionExecutor().atomic(new AtomicVoidClosure() {
            @Override
            public void execute(Transaction tx) throws Exception {
                scheduleCompensatingTask(task);
            }
        });

        verifyZeroInteractions(task);
    }

    @Test
    public void whenScheduleCompensatingOrDeferredTaskAndNoTransactionAvailable_thenNoTransactionFoundException() {
        Runnable task = mock(Runnable.class);

        try {
            scheduleCompensatingOrDeferredTask(task);
            fail();
        } catch (TransactionMandatoryException expected) {
        }

        verifyZeroInteractions(task);
    }

    @Test(expected = NullPointerException.class)
    public void whenScheduleCompensatingOrDeferredNullTask_thenNullPointerException() {
        scheduleCompensatingOrDeferredTask(null);
    }

    @Test
    public void whenScheduleCompensatingOrDeferredTaskAndCommit_thenCalled() {
        final Runnable task = mock(Runnable.class);

        try {
            stm.getDefaultTransactionExecutor().atomic(new AtomicVoidClosure() {
                @Override
                public void execute(Transaction tx) throws Exception {
                    scheduleCompensatingOrDeferredTask(task);
                    throw new NonsenseException();
                }
            });
            fail();
        } catch (NonsenseException expected) {
        }

        verify(task).run();
    }

    @Test
    public void whenScheduleCompensatingOrDeferredTaskAndAborted_thenCalled() {

        final Runnable task = mock(Runnable.class);

        stm.getDefaultTransactionExecutor().atomic(new AtomicVoidClosure() {
            @Override
            public void execute(Transaction tx) throws Exception {
                scheduleCompensatingOrDeferredTask(task);
            }
        });

        verify(task).run();
    }
}
