package org.multiverse.stms.gamma;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.Txn;
import org.multiverse.api.TxnExecutor;
import org.multiverse.api.callables.TxnCallable;
import org.multiverse.api.callables.TxnIntCallable;
import org.multiverse.api.callables.TxnLongCallable;
import org.multiverse.api.callables.TxnVoidCallable;

import static org.junit.Assert.assertEquals;
import static org.multiverse.api.TxnThreadLocal.clearThreadLocalTxn;

public class GammaTxnExecutorTest {

    private GammaStm stm;
    private TxnExecutor executor;

    @Before
    public void setUp() {
        clearThreadLocalTxn();
        stm = new GammaStm();
        executor = stm.newTxnFactoryBuilder()
                .newTxnExecutor();
    }

    @Test
    public void whenTxnIntCallableUsed() {
        int result = executor.execute(new TxnIntCallable() {
            @Override
            public int call(Txn tx) throws Exception {
                return 10;
            }
        });

        assertEquals(10, result);
    }

    @Test
    public void whenTxnLongCallableUsed() {
        long result = executor.execute(new TxnLongCallable() {
            @Override
            public long call(Txn tx) throws Exception {
                return 10;
            }
        });

        assertEquals(10, result);
    }

    @Test
    public void whenTxnVoidCallableUsed() {
        executor.execute(new TxnVoidCallable() {
            @Override
            public void call(Txn tx) throws Exception {
            }
        });
    }

    @Test
    public void whenTxnCallableUsed() {
        String result = executor.execute(new TxnCallable<String>() {
            @Override
            public String call(Txn tx) throws Exception {
                return "foo";
            }
        });

        assertEquals("foo", result);
    }
}
