package org.multiverse.stms.gamma;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.TestThread;
import org.multiverse.api.TransactionExecutor;
import org.multiverse.api.Transaction;
import org.multiverse.api.closures.AtomicVoidClosure;
import org.multiverse.api.exceptions.RetryInterruptedException;
import org.multiverse.stms.gamma.transactionalobjects.GammaLongRef;
import org.multiverse.stms.gamma.transactionalobjects.GammaRefTranlocal;
import org.multiverse.stms.gamma.transactions.GammaTransaction;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.multiverse.TestUtils.assertAlive;
import static org.multiverse.TestUtils.sleepMs;
import static org.multiverse.api.StmUtils.retry;
import static org.multiverse.api.ThreadLocalTransaction.clearThreadLocalTransaction;

public class GammaTransactionExecutor_interruptibleTest implements GammaConstants {

    private GammaStm stm;

    @Before
    public void setUp() {
        clearThreadLocalTransaction();
        stm = new GammaStm();
    }

    @Test
    public void whenNoTimeoutAndInterruptible() throws InterruptedException {
        final GammaLongRef ref = new GammaLongRef(stm);

        TransactionExecutor block = stm.newTransactionFactoryBuilder()
                .setInterruptible(true)
                .newTransactionExecutor();

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

        TransactionExecutor block = stm.newTransactionFactoryBuilder()
                .setTimeoutNs(TimeUnit.SECONDS.toNanos(10))
                .setInterruptible(true)
                .newTransactionExecutor();

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
        private TransactionExecutor block;

        public WaitWithoutTimeoutThread(GammaLongRef ref, TransactionExecutor block) {
            this.ref = ref;
            this.block = block;
        }

        @Override
        public void doRun() throws Exception {
            block.atomic(new AtomicVoidClosure() {
                @Override
                public void execute(Transaction tx) throws Exception {
                    GammaTransaction btx = (GammaTransaction) tx;
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
