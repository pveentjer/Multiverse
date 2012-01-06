package org.multiverse.stms.gamma;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.AtomicBlock;
import org.multiverse.api.Transaction;
import org.multiverse.api.closures.AtomicClosure;
import org.multiverse.api.closures.AtomicIntClosure;
import org.multiverse.api.closures.AtomicLongClosure;
import org.multiverse.api.closures.AtomicVoidClosure;

import static org.junit.Assert.assertEquals;
import static org.multiverse.api.ThreadLocalTransaction.clearThreadLocalTransaction;

public class GammaAtomicBlockTest {

    private GammaStm stm;
    private AtomicBlock block;

    @Before
    public void setUp() {
        clearThreadLocalTransaction();
        stm = new GammaStm();
        block = stm.newTransactionFactoryBuilder()
                .newAtomicBlock();
    }

    @Test
    public void whenAtomicIntClosureUsed() {
        int result = block.execute(new AtomicIntClosure() {
            @Override
            public int execute(Transaction tx) throws Exception {
                return 10;
            }
        });

        assertEquals(10, result);
    }

    @Test
    public void whenAtomicLongClosureUsed() {
        long result = block.execute(new AtomicLongClosure() {
            @Override
            public long execute(Transaction tx) throws Exception {
                return 10;
            }
        });

        assertEquals(10, result);
    }

    @Test
    public void whenAtomicVoidClosureUsed() {
        block.execute(new AtomicVoidClosure() {
            @Override
            public void execute(Transaction tx) throws Exception {
            }
        });
    }

    @Test
    public void whenAtomicClosureUsed() {
        String result = block.execute(new AtomicClosure<String>() {
            @Override
            public String execute(Transaction tx) throws Exception {
                return "foo";
            }
        });

        assertEquals("foo", result);
    }
}
