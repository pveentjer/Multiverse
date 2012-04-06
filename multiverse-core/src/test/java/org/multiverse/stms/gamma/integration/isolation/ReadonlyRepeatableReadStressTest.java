package org.multiverse.stms.gamma.integration.isolation;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.TestThread;
import org.multiverse.TestUtils;
import org.multiverse.api.TxnExecutor;
import org.multiverse.api.Txn;
import org.multiverse.api.closures.TxnVoidClosure;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactionalobjects.GammaTxnLong;
import org.multiverse.stms.gamma.transactions.GammaTxn;

import static org.junit.Assert.assertEquals;
import static org.multiverse.TestUtils.*;
import static org.multiverse.api.GlobalStmInstance.getGlobalStmInstance;
import static org.multiverse.api.TxnThreadLocal.clearThreadLocalTxn;

public class ReadonlyRepeatableReadStressTest {

    private volatile boolean stop;
    private GammaTxnLong ref;
    private int readThreadCount = 5;
    private int modifyThreadCount = 2;
    private GammaStm stm;

    @Before
    public void setUp() {
        clearThreadLocalTxn();
        stm = (GammaStm) getGlobalStmInstance();
        ref = new GammaTxnLong(stm);
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
            TxnExecutor executor = stm.newTxnFactoryBuilder()
                    .newTxnExecutor();
            TxnVoidClosure closure = new TxnVoidClosure() {
                @Override
                public void call(Txn tx) throws Exception {
                    GammaTxn btx = (GammaTxn) tx;
                    ref.getAndSet(btx, ref.get(btx));
                }
            };


            while (!stop) {
                executor.atomic(closure);
                sleepRandomMs(5);
            }
        }
    }

    class ReadThread extends TestThread {

        private final TxnExecutor readTrackingReadonlyBlock = stm.newTxnFactoryBuilder()
                .setReadonly(true)
                .setReadTrackingEnabled(true)
                .newTxnExecutor();

        private final TxnExecutor readTrackingUpdateBlock = stm.newTxnFactoryBuilder()
                .setReadonly(false)
                .setReadTrackingEnabled(true)
                .newTxnExecutor();

        private final TxnVoidClosure closure = new TxnVoidClosure() {
            @Override
            public void call(Txn tx) throws Exception {
                GammaTxn btx = (GammaTxn) tx;

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
                        readTrackingReadonlyBlock.atomic(closure);
                        break;
                    case 1:
                        readTrackingUpdateBlock.atomic(closure);
                        break;
                    default:
                        throw new IllegalStateException();
                }
                k++;
            }
        }
    }
}
