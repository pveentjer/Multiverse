package org.multiverse.stms.gamma;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.TestThread;
import org.multiverse.api.AtomicBlock;
import org.multiverse.api.Transaction;
import org.multiverse.api.closures.AtomicVoidClosure;
import org.multiverse.api.exceptions.RetryTimeoutException;
import org.multiverse.stms.gamma.transactionalobjects.GammaLongRef;
import org.multiverse.stms.gamma.transactionalobjects.GammaRefTranlocal;
import org.multiverse.stms.gamma.transactions.GammaTransaction;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.multiverse.TestUtils.*;
import static org.multiverse.api.StmUtils.retry;
import static org.multiverse.api.ThreadLocalTransaction.clearThreadLocalTransaction;

public class GammaAtomicBlock_timeoutTest {

    private GammaStm stm;
    private GammaLongRef ref;
    private long timeoutNs;

    @Before
    public void setUp() {
        clearThreadLocalTransaction();
        stm = new GammaStm();
        ref = new GammaLongRef(stm);
        timeoutNs = TimeUnit.SECONDS.toNanos(2);
    }

    @Test
    public void whenTimeout() throws InterruptedException {
        AtomicBlock block = stm.newTransactionFactoryBuilder()
                .setTimeoutNs(timeoutNs)
                .newAtomicBlock();

        AwaitThread t = new AwaitThread(block);
        t.setPrintStackTrace(false);
        t.start();

        t.join();
        t.assertFailedWithException(RetryTimeoutException.class);
        assertEquals(0, ref.atomicGet());
    }

    @Test
    public void whenSuccess() {
        AtomicBlock block = stm.newTransactionFactoryBuilder()
                .setTimeoutNs(timeoutNs)
                .newAtomicBlock();

        AwaitThread t = new AwaitThread(block);
        t.setPrintStackTrace(false);
        t.start();

        sleepMs(500);
        assertAlive(t);

        stm.getDefaultAtomicBlock().execute(new AtomicVoidClosure() {
            @Override
            public void execute(Transaction tx) throws Exception {
                GammaTransaction btx = (GammaTransaction) tx;
                ref.openForWrite(btx, LOCKMODE_NONE).long_value = 1;
            }
        });

        joinAll(t);
        assertNothingThrown(t);
        assertEquals(2, ref.atomicGet());
    }

    @Test
    public void whenNoWaitingNeededAndZeroTimeout() {
        stm.getDefaultAtomicBlock().execute(new AtomicVoidClosure() {
            @Override
            public void execute(Transaction tx) throws Exception {
                GammaTransaction btx = (GammaTransaction) tx;
                ref.openForWrite(btx, LOCKMODE_NONE).long_value = 1;
            }
        });

        AtomicBlock block = stm.newTransactionFactoryBuilder()
                .setTimeoutNs(0)
                .newAtomicBlock();

        AwaitThread t = new AwaitThread(block);
        t.setPrintStackTrace(false);
        t.start();

        joinAll(t);
        assertNothingThrown(t);
        assertEquals(2, ref.atomicGet());
    }

    class AwaitThread extends TestThread {

        private final AtomicBlock block;

        public AwaitThread(AtomicBlock block) {
            this.block = block;
        }

        @Override
        public void doRun() throws Exception {
            block.execute(new AtomicVoidClosure() {
                @Override
                public void execute(Transaction tx) throws Exception {
                    GammaTransaction btx = (GammaTransaction) tx;

                    GammaRefTranlocal write = ref.openForWrite(btx, LOCKMODE_NONE);
                    if (write.long_value == 0) {
                        retry();
                    }

                    write.long_value = 2;
                }
            });
        }
    }
}
