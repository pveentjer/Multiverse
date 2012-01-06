package org.multiverse.stms.gamma.integration.isolation;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.TestThread;
import org.multiverse.TestUtils;
import org.multiverse.api.AtomicBlock;
import org.multiverse.api.Transaction;
import org.multiverse.api.closures.AtomicVoidClosure;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactionalobjects.GammaLongRef;
import org.multiverse.stms.gamma.transactions.GammaTransaction;

import static org.junit.Assert.assertEquals;
import static org.multiverse.TestUtils.*;
import static org.multiverse.api.GlobalStmInstance.getGlobalStmInstance;
import static org.multiverse.api.ThreadLocalTransaction.clearThreadLocalTransaction;

public class ReadonlyRepeatableReadStressTest {

    private volatile boolean stop;
    private GammaLongRef ref;
    private int readThreadCount = 5;
    private int modifyThreadCount = 2;
    private GammaStm stm;

    @Before
    public void setUp() {
        clearThreadLocalTransaction();
        stm = (GammaStm) getGlobalStmInstance();
        ref = new GammaLongRef(stm);
        stop = false;
    }

    @Test
    public void test() {
        ModifyThread[] modifyThreads = new ModifyThread[modifyThreadCount];
        for (int k = 0; k < modifyThreadCount; k++) {
            modifyThreads[k] = new ModifyThread(k);
        }

        ReadThread[] readerThread = new ReadThread[readThreadCount];
        for (int k = 0; k < readThreadCount; k++) {
            readerThread[k] = new ReadThread(k);
        }

        startAll(modifyThreads);
        startAll(readerThread);
        sleepMs(TestUtils.getStressTestDurationMs(30 * 1000));
        stop = true;
        joinAll(modifyThreads);
        joinAll(readerThread);
    }

    class ModifyThread extends TestThread {

        public ModifyThread(int id) {
            super("ModifyThread-" + id);
        }

        @Override
        public void doRun() {
            AtomicBlock block = stm.newTransactionFactoryBuilder()
                    .newAtomicBlock();
            AtomicVoidClosure closure = new AtomicVoidClosure() {
                @Override
                public void execute(Transaction tx) throws Exception {
                    GammaTransaction btx = (GammaTransaction) tx;
                    ref.getAndSet(btx, ref.get(btx));
                }
            };


            while (!stop) {
                block.execute(closure);
                sleepRandomMs(5);
            }
        }
    }

    class ReadThread extends TestThread {

        private final AtomicBlock readTrackingReadonlyBlock = stm.newTransactionFactoryBuilder()
                .setReadonly(true)
                .setReadTrackingEnabled(true)
                .newAtomicBlock();

        private final AtomicBlock readTrackingUpdateBlock = stm.newTransactionFactoryBuilder()
                .setReadonly(false)
                .setReadTrackingEnabled(true)
                .newAtomicBlock();

        private final AtomicVoidClosure closure = new AtomicVoidClosure() {
            @Override
            public void execute(Transaction tx) throws Exception {
                GammaTransaction btx = (GammaTransaction) tx;

                long firstTime = ref.get(btx);
                sleepRandomMs(2);
                long secondTime = ref.get(btx);
                assertEquals(firstTime, secondTime);
            }
        };

        public ReadThread(int id) {
            super("ReadThread-" + id);
        }

        @Override
        public void doRun() {
            int k = 0;
            while (!stop) {
                switch (k % 2) {
                    case 0:
                        readTrackingReadonlyBlock.execute(closure);
                        break;
                    case 1:
                        readTrackingUpdateBlock.execute(closure);
                        break;
                    default:
                        throw new IllegalStateException();
                }
                k++;
            }
        }
    }
}
