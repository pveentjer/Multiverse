package org.multiverse.stms.gamma;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.TestThread;
import org.multiverse.api.Txn;
import org.multiverse.api.TxnExecutor;
import org.multiverse.api.closures.TxnVoidClosure;
import org.multiverse.api.exceptions.RetryTimeoutException;
import org.multiverse.stms.gamma.transactionalobjects.GammaTxnLong;
import org.multiverse.stms.gamma.transactionalobjects.Tranlocal;
import org.multiverse.stms.gamma.transactions.GammaTxn;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.multiverse.TestUtils.*;
import static org.multiverse.api.StmUtils.retry;
import static org.multiverse.api.TxnThreadLocal.clearThreadLocalTxn;

public class GammaTxnExecutor_timeoutTest {

    private GammaStm stm;
    private GammaTxnLong ref;
    private long timeoutNs;

    @Before
    public void setUp() {
        clearThreadLocalTxn();
        stm = new GammaStm();
        ref = new GammaTxnLong(stm);
        timeoutNs = TimeUnit.SECONDS.toNanos(2);
    }

    @Test
    public void whenTimeout() throws InterruptedException {
        TxnExecutor block = stm.newTxnFactoryBuilder()
                .setTimeoutNs(timeoutNs)
                .newTxnExecutor();

        AwaitThread t = new AwaitThread(block);
        t.setPrintStackTrace(false);
        t.start();

        t.join();
        t.assertFailedWithException(RetryTimeoutException.class);
        assertEquals(0, ref.atomicGet());
    }

    @Test
    public void whenSuccess() {
        TxnExecutor block = stm.newTxnFactoryBuilder()
                .setTimeoutNs(timeoutNs)
                .newTxnExecutor();

        AwaitThread t = new AwaitThread(block);
        t.setPrintStackTrace(false);
        t.start();

        sleepMs(500);
        assertAlive(t);

        stm.getDefaultTxnExecutor().atomic(new TxnVoidClosure() {
            @Override
            public void execute(Txn tx) throws Exception {
                GammaTxn btx = (GammaTxn) tx;
                ref.openForWrite(btx, LOCKMODE_NONE).long_value = 1;
            }
        });

        joinAll(t);
        assertNothingThrown(t);
        assertEquals(2, ref.atomicGet());
    }

    @Test
    public void whenNoWaitingNeededAndZeroTimeout() {
        stm.getDefaultTxnExecutor().atomic(new TxnVoidClosure() {
            @Override
            public void execute(Txn tx) throws Exception {
                GammaTxn btx = (GammaTxn) tx;
                ref.openForWrite(btx, LOCKMODE_NONE).long_value = 1;
            }
        });

        TxnExecutor block = stm.newTxnFactoryBuilder()
                .setTimeoutNs(0)
                .newTxnExecutor();

        AwaitThread t = new AwaitThread(block);
        t.setPrintStackTrace(false);
        t.start();

        joinAll(t);
        assertNothingThrown(t);
        assertEquals(2, ref.atomicGet());
    }

    class AwaitThread extends TestThread {

        private final TxnExecutor block;

        public AwaitThread(TxnExecutor block) {
            this.block = block;
        }

        @Override
        public void doRun() throws Exception {
            block.atomic(new TxnVoidClosure() {
                @Override
                public void execute(Txn tx) throws Exception {
                    GammaTxn btx = (GammaTxn) tx;

                    Tranlocal write = ref.openForWrite(btx, LOCKMODE_NONE);
                    if (write.long_value == 0) {
                        retry();
                    }

                    write.long_value = 2;
                }
            });
        }
    }
}
