package org.multiverse.stms.gamma.integration.blocking;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.TestThread;
import org.multiverse.api.AtomicBlock;
import org.multiverse.api.Transaction;
import org.multiverse.api.closures.AtomicVoidClosure;
import org.multiverse.api.exceptions.RetryTimeoutException;
import org.multiverse.api.references.IntRef;

import java.util.concurrent.TimeUnit;

import static org.multiverse.TestUtils.assertNothingThrown;
import static org.multiverse.TestUtils.sleepMs;
import static org.multiverse.api.GlobalStmInstance.getGlobalStmInstance;
import static org.multiverse.api.StmUtils.newIntRef;
import static org.multiverse.api.ThreadLocalTransaction.clearThreadLocalTransaction;

public class TimeoutTestLongTest {

    @Before
    public void setUp() {
        clearThreadLocalTransaction();
    }

    @Test
    public void whenNoRetryNeeded() throws InterruptedException {
        IntRef ref = newIntRef(1);

        WaitAndTimeoutThread thread = new WaitAndTimeoutThread(ref);
        thread.start();

        thread.join();
        assertNothingThrown(thread);
    }

    @Test
    public void whenTimeout() throws InterruptedException {
        IntRef ref = newIntRef();

        WaitAndTimeoutThread thread = new WaitAndTimeoutThread(ref);
        thread.setPrintStackTrace(false);
        thread.start();

        sleepMs(1000);

        thread.join();
        thread.assertFailedWithException(RetryTimeoutException.class);
    }

    @Test
    public void whenValueUpdatedInTime() throws InterruptedException {
        IntRef ref = newIntRef();

        WaitAndTimeoutThread thread = new WaitAndTimeoutThread(ref);
        thread.start();

        sleepMs(1000);
        ref.atomicSet(1);

        thread.join();
        thread.assertNothingThrown();
    }

    class WaitAndTimeoutThread extends TestThread {
        private final IntRef ref;


        WaitAndTimeoutThread(IntRef ref) {
            super("WaitAndTimeoutThread");
            this.ref = ref;
        }

        @Override
        public void doRun() throws Exception {
            AtomicBlock block = getGlobalStmInstance()
                    .newTransactionFactoryBuilder()
                    .setTimeoutNs(TimeUnit.SECONDS.toNanos(5))
                    .newAtomicBlock();

            block.atomic(new AtomicVoidClosure() {
                @Override
                public void execute(Transaction tx) throws Exception {
                    ref.await(1);
                }
            });
        }
    }


}
