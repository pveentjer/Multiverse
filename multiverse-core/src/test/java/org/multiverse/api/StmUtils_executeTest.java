package org.multiverse.api;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.callables.TxnVoidCallable;
import org.multiverse.api.exceptions.InvisibleCheckedException;
import org.multiverse.api.references.TxnInteger;

import static org.junit.Assert.*;
import static org.multiverse.api.StmUtils.*;
import static org.multiverse.api.TxnThreadLocal.clearThreadLocalTxn;

public class StmUtils_executeTest {

    @Before
    public void setUp() {
        clearThreadLocalTxn();
    }

    @Test(expected = NullPointerException.class)
    public void whenNullTxnCallable_thenNullPointerException() {
        StmUtils.atomic((TxnVoidCallable) null);
    }

    @Test
    public void whenExecuteSuccess() {
        final TxnInteger ref = newTxnInteger();

        atomic(new TxnVoidCallable() {
            @Override
            public void call(Txn tx) throws Exception {
                ref.incrementAndGet(10);
            }
        });

        assertEquals(10, ref.atomicGet());
    }

    @Test
    public void whenExecuteThrowsCheckedException() {
        final TxnInteger ref = newTxnInteger();
        final Exception ex = new Exception();

        try {
            atomic(new TxnVoidCallable() {
                @Override
                public void call(Txn tx) throws Exception {
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
        final TxnInteger ref = newTxnInteger();
        final RuntimeException ex = new RuntimeException();

        try {
            atomic(new TxnVoidCallable() {
                @Override
                public void call(Txn tx) throws Exception {
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
        final TxnInteger ref = newTxnInteger();

        atomicChecked(new TxnVoidCallable() {
            @Override
            public void call(Txn tx) throws Exception {
                ref.incrementAndGet(10);
            }
        });

        assertEquals(10, ref.atomicGet());
    }

    @Test
    public void whenExecuteCheckedThrowsCheckedException() {
        final TxnInteger ref = newTxnInteger();
        final Exception ex = new Exception();

        try {
            atomicChecked(new TxnVoidCallable() {
                @Override
                public void call(Txn tx) throws Exception {
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
        final TxnInteger ref = newTxnInteger();
        final RuntimeException ex = new RuntimeException();

        try {
            atomicChecked(new TxnVoidCallable() {
                @Override
                public void call(Txn tx) throws Exception {
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
