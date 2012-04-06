package org.multiverse.stms.gamma;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.TestThread;
import org.multiverse.api.Txn;
import org.multiverse.api.closures.TxnVoidClosure;
import org.multiverse.stms.gamma.transactionalobjects.GammaLongRef;
import org.multiverse.stms.gamma.transactionalobjects.GammaRefTranlocal;
import org.multiverse.stms.gamma.transactions.GammaTxn;

import static org.junit.Assert.assertEquals;
import static org.multiverse.TestUtils.*;
import static org.multiverse.api.StmUtils.retry;
import static org.multiverse.api.TxnThreadLocal.clearThreadLocalTxn;

public class GammaTxnExecutor_blockingTest {

    private GammaStm stm;

    @Before
    public void setUp() {
        stm = new GammaStm();
        clearThreadLocalTxn();
    }

    @Test
    public void test() {
        final GammaLongRef ref = new GammaLongRef(stm);

        WaitThread t = new WaitThread(ref);
        t.start();

        sleepMs(1000);
        assertAlive(t);

        stm.getDefaultTxnExecutor().atomic(new TxnVoidClosure() {
            @Override
            public void execute(Txn tx) throws Exception {
                GammaTxn btx = (GammaTxn) tx;
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
            stm.getDefaultTxnExecutor().atomic(new TxnVoidClosure() {
                @Override
                public void execute(Txn tx) throws Exception {
                    GammaTxn btx = (GammaTxn) tx;
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
