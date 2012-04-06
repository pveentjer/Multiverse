package org.multiverse.stms.gamma.integration.classic;

import org.junit.Before;
import org.multiverse.TestThread;
import org.multiverse.api.Txn;
import org.multiverse.api.TxnExecutor;
import org.multiverse.api.callables.TxnCallable;
import org.multiverse.api.callables.TxnVoidCallable;
import org.multiverse.api.references.TxnInteger;
import org.multiverse.stms.gamma.GammaStm;

import static junit.framework.Assert.assertEquals;
import static org.multiverse.TestUtils.*;
import static org.multiverse.api.GlobalStmInstance.getGlobalStmInstance;
import static org.multiverse.api.StmUtils.newTxnInteger;
import static org.multiverse.api.StmUtils.retry;
import static org.multiverse.api.TxnThreadLocal.clearThreadLocalTxn;

/**
 * http://en.wikipedia.org/wiki/Producer-consumer_problem
 */
public abstract class ProducerConsumer_AbstractTest {

    private Buffer buffer;
    private volatile boolean stop;
    private static final int MAX_CAPACITY = 100;
    protected GammaStm stm;

    protected abstract TxnExecutor newPutBlock();

    protected abstract TxnExecutor newTakeBlock();

    @Before
    public void setUp() {
        clearThreadLocalTxn();
        stm = (GammaStm) getGlobalStmInstance();
        stop = false;
    }


    public void run() {
        buffer = new Buffer();
        ProducerThread producerThread = new ProducerThread();
        ConsumerThread consumerThread = new ConsumerThread();

        startAll(producerThread, consumerThread);
        sleepMs(30 * 1000);
        stop = true;
        joinAll(producerThread, consumerThread);

        assertEquals(producerThread.produced, buffer.size.atomicGet() + consumerThread.consumed);
    }

    public class ProducerThread extends TestThread {
        private int produced;

        public ProducerThread() {
            super("ProducerThread");
        }

        @Override
        public void doRun() {
            produced = 0;
            while (!stop) {
                buffer.put(produced);
                produced++;

                if (produced % 1000000 == 0) {
                    System.out.printf("%s is at %d\n", getName(), produced);
                }
            }

            buffer.put(-1);
            produced++;
        }
    }

    public class ConsumerThread extends TestThread {
        public int consumed;

        public ConsumerThread() {
            super("ConsumerThread");
        }

        @Override
        public void doRun() {
            int item;
            do {
                item = buffer.take();
                consumed++;
                if (consumed % 1000000 == 0) {
                    System.out.printf("%s is at %d\n", getName(), consumed);
                }
            } while (item != -1);
        }
    }

    class Buffer {
        private final TxnInteger size = newTxnInteger();
        private final TxnInteger[] items;
        private final TxnExecutor takeBlock = newTakeBlock();
        private final TxnExecutor putBlock = newPutBlock();


        Buffer() {
            this.items = new TxnInteger[MAX_CAPACITY];
            for (int k = 0; k < items.length; k++) {
                items[k] = newTxnInteger();
            }
        }

        int take() {
            return takeBlock.atomic(new TxnCallable<Integer>() {
                @Override
                public Integer call(Txn tx) throws Exception {
                    if (size.get() == 0) {
                        retry();
                    }

                    size.decrement();
                    return items[size.get()].get();
                }
            });
        }

        void put(final int item) {
            putBlock.atomic(new TxnVoidCallable() {
                @Override
                public void call(Txn tx) throws Exception {
                    if (size.get() >= MAX_CAPACITY) {
                        retry();
                    }

                    items[size.get()].set(item);
                    size.increment();
                }
            });
        }
    }
}
