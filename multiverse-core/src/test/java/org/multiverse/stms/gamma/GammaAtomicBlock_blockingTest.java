package org.multiverse.stms.gamma;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.TestThread;
import org.multiverse.api.Transaction;
import org.multiverse.api.closures.AtomicVoidClosure;
import org.multiverse.stms.gamma.transactionalobjects.GammaLongRef;
import org.multiverse.stms.gamma.transactionalobjects.GammaRefTranlocal;
import org.multiverse.stms.gamma.transactions.GammaTransaction;

import static org.junit.Assert.assertEquals;
import static org.multiverse.TestUtils.*;
import static org.multiverse.api.StmUtils.retry;
import static org.multiverse.api.ThreadLocalTransaction.clearThreadLocalTransaction;

public class GammaAtomicBlock_blockingTest {

    private GammaStm stm;

    @Before
    public void setUp() {
        stm = new GammaStm();
        clearThreadLocalTransaction();
    }

    @Test
    public void test() {
        final GammaLongRef ref = new GammaLongRef(stm);

        WaitThread t = new WaitThread(ref);
        t.start();

        sleepMs(1000);
        assertAlive(t);

        stm.getDefaultAtomicBlock().atomic(new AtomicVoidClosure() {
            @Override
            public void execute(Transaction tx) throws Exception {
                GammaTransaction btx = (GammaTransaction) tx;
                GammaRefTranlocal write = ref.openForWrite(btx, LOCKMODE_NONE);
                write.long_value = 1;
            }
        });

        joinAll(t);
        assertEquals(2, ref.atomicGet());
    }

    class WaitThread extends TestThread {
        final GammaLongRef ref;

        public WaitThread(GammaLongRef ref) {
            this.ref = ref;
        }

        @Override
        public void doRun() throws Exception {
            stm.getDefaultAtomicBlock().atomic(new AtomicVoidClosure() {
                @Override
                public void execute(Transaction tx) throws Exception {
                    GammaTransaction btx = (GammaTransaction) tx;
                    GammaRefTranlocal write = ref.openForWrite(btx, LOCKMODE_NONE);
                    if (write.long_value == 0) {
                        retry();
                    }

                    write.long_value++;
                }
            });
        }
    }
}
