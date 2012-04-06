package org.multiverse.stms.gamma;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.Txn;
import org.multiverse.api.TxnExecutor;
import org.multiverse.api.callables.TxnVoidCallable;
import org.multiverse.api.exceptions.InvisibleCheckedException;
import org.multiverse.stms.gamma.transactionalobjects.GammaTxnLong;
import org.multiverse.stms.gamma.transactions.GammaTxn;

import static org.junit.Assert.*;
import static org.multiverse.api.TxnThreadLocal.clearThreadLocalTxn;

public class GammaTxnExecutor_exceptionsTest implements GammaConstants {
    private GammaStm stm;

    @Before
    public void setUp() {
        stm = new GammaStm();
        clearThreadLocalTxn();
    }

    @Test
    public void executeChecked_whenCheckedExceptionThrown() {
        TxnExecutor executor = stm.newTxnFactoryBuilder().newTxnExecutor();
        final GammaTxnLong ref = new GammaTxnLong(stm, 10);

        final Exception ex = new Exception();

        try {
            executor.executeChecked(new TxnVoidCallable() {
                @Override
                public void call(Txn tx) throws Exception {
                    GammaTxn btx = (GammaTxn) tx;
                    ref.openForWrite(btx, LOCKMODE_NONE).long_value++;
                    throw ex;
                }
            });
            fail();
        } catch (Exception expected) {
            assertSame(ex, expected);
        }

        assertEquals(10, ref.atomicGet());
    }

    @Test
    public void executeChecked_whenRuntimeExceptionThrown() throws Exception {
        TxnExecutor executor = stm.newTxnFactoryBuilder().newTxnExecutor();
        final GammaTxnLong ref = new GammaTxnLong(stm, 10);

        final RuntimeException ex = new RuntimeException();

        try {
            executor.executeChecked(new TxnVoidCallable() {
                @Override
                public void call(Txn tx) throws Exception {
                    GammaTxn btx = (GammaTxn) tx;
                    ref.openForWrite(btx, LOCKMODE_NONE).long_value++;
                    throw ex;
                }
            });
            fail();
        } catch (RuntimeException expected) {
            assertSame(ex, expected);
        }

        assertEquals(10, ref.atomicGet());
    }


    @Test
    public void executeChecked_whenErrorThrown() throws Exception {
        TxnExecutor executor = stm.newTxnFactoryBuilder().newTxnExecutor();
        final GammaTxnLong ref = new GammaTxnLong(stm, 10);

        final Error ex = new Error();

        try {
            executor.executeChecked(new TxnVoidCallable() {
                @Override
                public void call(Txn tx) throws Exception {
                    GammaTxn btx = (GammaTxn) tx;
                    ref.openForWrite(btx, LOCKMODE_NONE).long_value++;
                    throw ex;
                }
            });
            fail();
        } catch (Error expected) {
            assertSame(ex, expected);
        }

        assertEquals(10, ref.atomicGet());
    }

    @Test
    public void execute_whenCheckedExceptionThrown() {
        TxnExecutor executor = stm.newTxnFactoryBuilder().newTxnExecutor();
        final GammaTxnLong ref = new GammaTxnLong(stm, 10);

        final Exception ex = new Exception();

        try {
            executor.execute(new TxnVoidCallable() {
                @Override
                public void call(Txn tx) throws Exception {
                    GammaTxn btx = (GammaTxn) tx;
                    ref.openForWrite(btx, LOCKMODE_NONE).long_value++;
                    throw ex;
                }
            });
            fail();
        } catch (InvisibleCheckedException expected) {
            assertSame(ex, expected.getCause());
        }

        assertEquals(10, ref.atomicGet());
    }

    @Test
    public void execute_whenRuntimeExceptionThrown() {
        TxnExecutor executor = stm.newTxnFactoryBuilder().newTxnExecutor();
        final GammaTxnLong ref = new GammaTxnLong(stm, 10);

        final RuntimeException ex = new RuntimeException();

        try {
            executor.execute(new TxnVoidCallable() {
                @Override
                public void call(Txn tx) throws Exception {
                    GammaTxn btx = (GammaTxn) tx;
                    ref.openForWrite(btx, LOCKMODE_NONE).long_value++;
                    throw ex;
                }
            });
            fail();
        } catch (RuntimeException expected) {
            assertSame(ex, expected);
        }

        assertEquals(10, ref.atomicGet());
    }


    @Test
    public void execute_whenErrorThrown() {
        TxnExecutor executor = stm.newTxnFactoryBuilder().newTxnExecutor();
        final GammaTxnLong ref = new GammaTxnLong(stm, 10);

        final Error ex = new Error();

        try {
            executor.execute(new TxnVoidCallable() {
                @Override
                public void call(Txn tx) throws Exception {
                    GammaTxn btx = (GammaTxn) tx;
                    ref.openForWrite(btx, LOCKMODE_NONE).long_value++;
                    throw ex;
                }
            });
            fail();
        } catch (Error expected) {
            assertSame(ex, expected);
        }

        assertEquals(10, ref.atomicGet());
    }


}
