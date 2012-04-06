package org.multiverse.stms.gamma.integration.lifecycle;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.Txn;
import org.multiverse.api.closures.AtomicVoidClosure;
import org.multiverse.api.exceptions.DeadTransactionException;
import org.multiverse.stms.gamma.GammaStm;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;
import static org.multiverse.api.GlobalStmInstance.getGlobalStmInstance;
import static org.multiverse.api.StmUtils.*;
import static org.multiverse.api.TxnThreadLocal.clearThreadLocalTxn;

public class LifecycleTest {
    private GammaStm stm;

    @Before
    public void setUp() {
        stm = (GammaStm) getGlobalStmInstance();
        clearThreadLocalTxn();
    }

    @Test
    public void whenTransactionCommit_thenCompensatingOrDeferredTaskExecuted() {
        final Runnable task = mock(Runnable.class);

        atomic(new AtomicVoidClosure() {
            @Override
            public void execute(Txn tx) throws Exception {
                scheduleCompensatingOrDeferredTask(task);
                tx.commit();
            }
        });

        verify(task).run();
    }

    @Test
    public void whenTransactionAborts_thenCompensatingOrDeferredTaskExecuted() {
        final Runnable task = mock(Runnable.class);

        try {
            atomic(new AtomicVoidClosure() {
                @Override
                public void execute(Txn tx) throws Exception {
                    scheduleCompensatingOrDeferredTask(task);
                    tx.abort();
                }
            });
            fail();
        } catch (DeadTransactionException expected) {

        }

        verify(task).run();
    }

    @Test
    public void whenTransactionCommit_thenDeferredOperationCalled() {
        final Runnable task = mock(Runnable.class);

        atomic(new AtomicVoidClosure() {
            @Override
            public void execute(Txn tx) throws Exception {
                scheduleDeferredTask(task);
                tx.commit();
            }
        });

        verify(task).run();
    }

    @Test
    public void whenTransactionCommit_thenCompensatingOperationNotCalled() {
        final Runnable task = mock(Runnable.class);

        atomic(new AtomicVoidClosure() {
            @Override
            public void execute(Txn tx) throws Exception {
                scheduleCompensatingTask(task);
                tx.commit();
            }
        });

        verifyZeroInteractions(task);
    }

    @Test
    public void whenTransactionAborts_thenDeferredOperationNotCalled() {
        final Runnable task = mock(Runnable.class);

        try {
            atomic(new AtomicVoidClosure() {
                @Override
                public void execute(Txn tx) throws Exception {
                    scheduleDeferredTask(task);
                    tx.abort();
                }
            });
            fail();
        } catch (DeadTransactionException e) {

        }

        verifyZeroInteractions(task);
    }

    @Test
    public void whenTransactionAborts_thenCompensatingOperationCalled() {
        final Runnable task = mock(Runnable.class);

        try {
            atomic(new AtomicVoidClosure() {
                @Override
                public void execute(Txn tx) throws Exception {
                    scheduleCompensatingTask(task);
                    tx.abort();
                }
            });
            fail();
        } catch (DeadTransactionException e) {

        }

        verify(task).run();
    }
}
