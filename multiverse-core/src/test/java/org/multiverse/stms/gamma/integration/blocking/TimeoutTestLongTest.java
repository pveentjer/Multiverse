package org.multiverse.stms.gamma.integration.blocking;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.TestThread;
import org.multiverse.api.TxnExecutor;
import org.multiverse.api.Txn;
import org.multiverse.api.callables.TxnVoidCallable;
import org.multiverse.api.exceptions.RetryTimeoutException;
import org.multiverse.api.references.TxnInteger;

import java.util.concurrent.TimeUnit;

import static org.multiverse.TestUtils.assertNothingThrown;
import static org.multiverse.TestUtils.sleepMs;
import static org.multiverse.api.GlobalStmInstance.getGlobalStmInstance;
import static org.multiverse.api.StmUtils.newTxnInteger;
import static org.multiverse.api.TxnThreadLocal.clearThreadLocalTxn;

public class TimeoutTestLongTest {

    @Before
    public void setUp() {
        clearThreadLocalTxn();
    }

    @Test
    public void whenNoRetryNeeded() throws InterruptedException {
        TxnInteger ref = newTxnInteger(1);

        WaitAndTimeoutThread thread = new WaitAndTimeoutThread(ref);
        thread.start();

        thread.join();
        assertNothingThrown(thread);
    }

    @Test
    public void whenTimeout() throws InterruptedException {
        TxnInteger ref = newTxnInteger();

        WaitAndTimeoutThread thread = new WaitAndTimeoutThread(ref);
        thread.setPrintStackTrace(false);
        thread.start();

        sleepMs(1000);

        thread.join();
        thread.assertFailedWithException(RetryTimeoutException.class);
    }

    @Test
    public void whenValueUpdatedInTime() throws InterruptedException {
        TxnInteger ref = newTxnInteger();

        WaitAndTimeoutThread thread = new WaitAndTimeoutThread(ref);
        thread.start();

        sleepMs(1000);
        ref.atomicSet(1);

        thread.join();
        thread.assertNothingThrown();
    }

    class WaitAndTimeoutThread extends TestThread {
        private final TxnInteger ref;


        WaitAndTimeoutThread(TxnInteger ref) {
            super("WaitAndTimeoutThread");
            this.ref = ref;
        }

        @Override
        public void doRun() throws Exception {
            TxnExecutor executor = getGlobalStmInstance()
                    .newTxnFactoryBuilder()
                    .setTimeoutNs(TimeUnit.SECONDS.toNanos(5))
                    .newTxnExecutor();

            executor.atomic(new TxnVoidCallable() {
                @Override
                public void call(Txn tx) throws Exception {
                    ref.await(1);
                }
            });
        }
    }


}
