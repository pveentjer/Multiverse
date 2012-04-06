package org.multiverse.stms.gamma;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.Txn;
import org.multiverse.api.TxnExecutor;
import org.multiverse.api.closures.TxnClosure;
import org.multiverse.api.closures.TxnIntClosure;
import org.multiverse.api.closures.TxnLongClosure;
import org.multiverse.api.closures.TxnVoidClosure;

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
    public void whenAtomicIntClosureUsed() {
        int result = executor.atomic(new TxnIntClosure() {
            @Override
            public int execute(Txn tx) throws Exception {
                return 10;
            }
        });

        assertEquals(10, result);
    }

    @Test
    public void whenAtomicLongClosureUsed() {
        long result = executor.atomic(new TxnLongClosure() {
            @Override
            public long execute(Txn tx) throws Exception {
                return 10;
            }
        });

        assertEquals(10, result);
    }

    @Test
    public void whenAtomicVoidClosureUsed() {
        executor.atomic(new TxnVoidClosure() {
            @Override
            public void execute(Txn tx) throws Exception {
            }
        });
    }

    @Test
    public void whenAtomicClosureUsed() {
        String result = executor.atomic(new TxnClosure<String>() {
            @Override
            public String execute(Txn tx) throws Exception {
                return "foo";
            }
        });

        assertEquals("foo", result);
    }
}
