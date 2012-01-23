package org.multiverse.stms.gamma.integration.failureatomicity;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.TestThread;
import org.multiverse.api.AtomicBlock;
import org.multiverse.api.Transaction;
import org.multiverse.api.closures.AtomicVoidClosure;
import org.multiverse.api.exceptions.TooManyRetriesException;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactionalobjects.GammaLongRef;
import org.multiverse.stms.gamma.transactions.GammaTransaction;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.multiverse.TestUtils.joinAll;
import static org.multiverse.api.GlobalStmInstance.getGlobalStmInstance;
import static org.multiverse.api.StmUtils.retry;
import static org.multiverse.api.ThreadLocalTransaction.clearThreadLocalTransaction;

public class TooManyRetriesRollbackTest {
    private GammaLongRef modifyRef;
    private GammaLongRef retryRef;
    private volatile boolean finished;
    private GammaStm stm;

    @Before
    public void setUp() {
        clearThreadLocalTransaction();
        stm = (GammaStm) getGlobalStmInstance();
        modifyRef = new GammaLongRef(stm);
        retryRef = new GammaLongRef(stm);
        finished = false;
    }

    @Test
    public void test() {
        NotifyThread notifyThread = new NotifyThread();
        notifyThread.start();

        try {
            setAndAwaitUneven(1);
            fail();
        } catch (TooManyRetriesException expected) {
        }

        finished = true;
        assertEquals(0, modifyRef.atomicGet());
        joinAll(notifyThread);
    }

    public void setAndAwaitUneven(final int value) {
        AtomicBlock block = stm.newTransactionFactoryBuilder()
                .setMaxRetries(10)
                .newAtomicBlock();

        block.execute(new AtomicVoidClosure() {
            @Override
            public void execute(Transaction tx) throws Exception {
                GammaTransaction btx = (GammaTransaction) tx;

                modifyRef.getAndSet(btx, value);

                if (retryRef.get(btx) % 2 == 0) {
                    retry();
                }
            }
        });
    }

    class NotifyThread extends TestThread {

        public NotifyThread() {
            super("NotifyThread");
        }

        @Override
        public void doRun() throws Exception {
            AtomicBlock block = stm.newTransactionFactoryBuilder()
                    .newAtomicBlock();
            AtomicVoidClosure closure = new AtomicVoidClosure() {
                @Override
                public void execute(Transaction tx) throws Exception {
                    GammaTransaction btx = (GammaTransaction) tx;

                    long value = retryRef.get(btx);
                    retryRef.getAndSet(btx, value + 2);
                }
            };

            while (!finished) {
                block.execute(closure);
            }
        }
    }
}
