package org.multiverse.api;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.closures.TxnVoidClosure;
import org.multiverse.api.exceptions.InvisibleCheckedException;
import org.multiverse.api.references.IntRef;

import static org.junit.Assert.*;
import static org.multiverse.api.StmUtils.*;
import static org.multiverse.api.TxnThreadLocal.clearThreadLocalTxn;

public class StmUtils_executeTest {

    @Before
    public void setUp() {
        clearThreadLocalTxn();
    }

    @Test(expected = NullPointerException.class)
    public void whenNullClosure_thenNullPointerException() {
        StmUtils.atomic((TxnVoidClosure) null);
    }

    @Test
    public void whenExecuteSuccess() {
        final IntRef ref = newIntRef();

        atomic(new TxnVoidClosure() {
            @Override
            public void execute(Txn tx) throws Exception {
                ref.incrementAndGet(10);
            }
        });

        assertEquals(10, ref.atomicGet());
    }

    @Test
    public void whenExecuteThrowsCheckedException() {
        final IntRef ref = newIntRef();
        final Exception ex = new Exception();

        try {
            atomic(new TxnVoidClosure() {
                @Override
                public void execute(Txn tx) throws Exception {
                    ref.incrementAndGet(10);
                    throw ex;
                }
            });
            fail();
        } catch (InvisibleCheckedException expected) {
            assertSame(ex, expected.getCause());
        }


        assertEquals(0, ref.atomicGet());
    }

    @Test
    public void whenExecuteThrowsUncheckedException() {
        final IntRef ref = newIntRef();
        final RuntimeException ex = new RuntimeException();

        try {
            atomic(new TxnVoidClosure() {
                @Override
                public void execute(Txn tx) throws Exception {
                    ref.incrementAndGet(10);
                    throw ex;
                }
            });
            fail();
        } catch (Exception found) {
            assertSame(ex, found);
        }

        assertEquals(0, ref.atomicGet());
    }

    @Test
    public void whenExecuteCheckedSuccess() throws Exception {
        final IntRef ref = newIntRef();

        atomicChecked(new TxnVoidClosure() {
            @Override
            public void execute(Txn tx) throws Exception {
                ref.incrementAndGet(10);
            }
        });

        assertEquals(10, ref.atomicGet());
    }

    @Test
    public void whenExecuteCheckedThrowsCheckedException() {
        final IntRef ref = newIntRef();
        final Exception ex = new Exception();

        try {
            atomicChecked(new TxnVoidClosure() {
                @Override
                public void execute(Txn tx) throws Exception {
                    ref.incrementAndGet(10);
                    throw ex;
                }
            });
            fail();
        } catch (Exception found) {
            assertSame(ex, found);
        }


        assertEquals(0, ref.atomicGet());
    }

    @Test
    public void whenExecuteCheckedThrowsUncheckedException() {
        final IntRef ref = newIntRef();
        final RuntimeException ex = new RuntimeException();

        try {
            atomicChecked(new TxnVoidClosure() {
                @Override
                public void execute(Txn tx) throws Exception {
                    ref.incrementAndGet(10);
                    throw ex;
                }
            });
            fail();
        } catch (Exception found) {
            assertSame(ex, found);
        }

        assertEquals(0, ref.atomicGet());
    }
}
