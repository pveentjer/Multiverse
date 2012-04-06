package org.multiverse.stms.gamma;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.Txn;
import org.multiverse.api.TxnExecutor;
import org.multiverse.api.closures.AtomicClosure;
import org.multiverse.api.closures.AtomicIntClosure;
import org.multiverse.api.closures.AtomicLongClosure;
import org.multiverse.api.closures.AtomicVoidClosure;

import static org.junit.Assert.assertEquals;
import static org.multiverse.api.TxnThreadLocal.clearThreadLocalTxn;

public class GammaTxnExecutorTest {

    private GammaStm stm;
    private TxnExecutor block;

    @Before
    public void setUp() {
        clearThreadLocalTxn();
        stm = new GammaStm();
        block = stm.newTransactionFactoryBuilder()
                .newTxnExecutor();
    }

    @Test
    public void whenAtomicIntClosureUsed() {
        int result = block.atomic(new AtomicIntClosure() {
            @Override
            public int execute(Txn tx) throws Exception {
                return 10;
            }
        });

        assertEquals(10, result);
    }

    @Test
    public void whenAtomicLongClosureUsed() {
        long result = block.atomic(new AtomicLongClosure() {
            @Override
            public long execute(Txn tx) throws Exception {
                return 10;
            }
        });

        assertEquals(10, result);
    }

    @Test
    public void whenAtomicVoidClosureUsed() {
        block.atomic(new AtomicVoidClosure() {
            @Override
            public void execute(Txn tx) throws Exception {
            }
        });
    }

    @Test
    public void whenAtomicClosureUsed() {
        String result = block.atomic(new AtomicClosure<String>() {
            @Override
            public String execute(Txn tx) throws Exception {
                return "foo";
            }
        });

        assertEquals("foo", result);
    }
}
