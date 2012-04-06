package org.multiverse.stms.gamma;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.TestThread;
import org.multiverse.api.TxnExecutor;
import org.multiverse.api.Txn;
import org.multiverse.api.callables.TxnVoidCallable;
import org.multiverse.api.exceptions.RetryInterruptedException;
import org.multiverse.stms.gamma.transactionalobjects.GammaTxnLong;
import org.multiverse.stms.gamma.transactionalobjects.Tranlocal;
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
        final GammaTxnLong ref = new GammaTxnLong(stm);

        TxnExecutor executor = stm.newTxnFactoryBuilder()
                .setInterruptible(true)
                .newTxnExecutor();

        WaitWithoutTimeoutThread t = new WaitWithoutTimeoutThread(ref, executor);
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
        final GammaTxnLong ref = new GammaTxnLong(stm);

        TxnExecutor executor = stm.newTxnFactoryBuilder()
                .setTimeoutNs(TimeUnit.SECONDS.toNanos(10))
                .setInterruptible(true)
                .newTxnExecutor();

        WaitWithoutTimeoutThread t = new WaitWithoutTimeoutThread(ref, executor);
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
        final GammaTxnLong ref;
        private final TxnExecutor executor;

        public WaitWithoutTimeoutThread(GammaTxnLong ref, TxnExecutor executor) {
            this.ref = ref;
            this.executor = executor;
        }

        @Override
        public void doRun() throws Exception {
            executor.atomic(new TxnVoidCallable() {
                @Override
                public void call(Txn tx) throws Exception {
                    GammaTxn btx = (GammaTxn) tx;
                    Tranlocal write = ref.openForWrite(btx, LOCKMODE_NONE);
                    if (write.long_value == 0) {
                        retry();
                    }

                    write.long_value = 100;
                }
            });
        }
    }
}
