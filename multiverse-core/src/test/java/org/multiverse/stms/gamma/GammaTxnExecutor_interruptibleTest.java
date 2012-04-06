package org.multiverse.stms.gamma;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.TestThread;
import org.multiverse.api.TxnExecutor;
import org.multiverse.api.Txn;
import org.multiverse.api.closures.TxnVoidClosure;
import org.multiverse.api.exceptions.RetryInterruptedException;
import org.multiverse.stms.gamma.transactionalobjects.GammaLongRef;
import org.multiverse.stms.gamma.transactionalobjects.GammaRefTranlocal;
import org.multiverse.stms.gamma.transactions.GammaTxn;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.multiverse.TestUtils.assertAlive;
import static org.multiverse.TestUtils.sleepMs;
import static org.multiverse.api.StmUtils.retry;
import static org.multiverse.api.TxnThreadLocal.clearThreadLocalTxn;

public class GammaTxnExecutor_interruptibleTest implements GammaConstants {

    private GammaStm stm;

    @Before
    public void setUp() {
        clearThreadLocalTxn();
        stm = new GammaStm();
    }

    @Test
    public void whenNoTimeoutAndInterruptible() throws InterruptedException {
        final GammaLongRef ref = new GammaLongRef(stm);

        TxnExecutor block = stm.newTxnFactoryBuilder()
                .setInterruptible(true)
                .newTxnExecutor();

        WaitWithoutTimeoutThread t = new WaitWithoutTimeoutThread(ref, block);
        t.setPrintStackTrace(false);
        t.start();

        sleepMs(1000);
        assertAlive(t);

        t.interrupt();

        t.join();

        t.assertFailedWithException(RetryInterruptedException.class);
        assertEquals(0, ref.atomicGet());
    }

    @Test
    public void whenTimeoutAndInterruptible() throws InterruptedException {
        final GammaLongRef ref = new GammaLongRef(stm);

        TxnExecutor block = stm.newTxnFactoryBuilder()
                .setTimeoutNs(TimeUnit.SECONDS.toNanos(10))
                .setInterruptible(true)
                .newTxnExecutor();

        WaitWithoutTimeoutThread t = new WaitWithoutTimeoutThread(ref, block);
        t.setPrintStackTrace(false);
        t.start();

        sleepMs(1000);
        assertAlive(t);

        t.interrupt();

        t.join();

        t.assertFailedWithException(RetryInterruptedException.class);
        assertEquals(0, ref.atomicGet());
    }


    class WaitWithoutTimeoutThread extends TestThread {
        final GammaLongRef ref;
        private TxnExecutor block;

        public WaitWithoutTimeoutThread(GammaLongRef ref, TxnExecutor block) {
            this.ref = ref;
            this.block = block;
        }

        @Override
        public void doRun() throws Exception {
            block.atomic(new TxnVoidClosure() {
                @Override
                public void execute(Txn tx) throws Exception {
                    GammaTxn btx = (GammaTxn) tx;
                    GammaRefTranlocal write = ref.openForWrite(btx, LOCKMODE_NONE);
                    if (write.long_value == 0) {
                        retry();
                    }

                    write.long_value = 100;
                }
            });
        }
    }
}
