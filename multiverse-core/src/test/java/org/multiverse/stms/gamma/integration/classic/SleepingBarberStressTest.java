package org.multiverse.stms.gamma.integration.classic;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.TestThread;
import org.multiverse.api.Txn;
import org.multiverse.api.closures.TxnVoidClosure;
import org.multiverse.api.references.TxnBoolean;
import org.multiverse.api.references.TxnInteger;

import static org.multiverse.TestUtils.*;
import static org.multiverse.api.StmUtils.*;
import static org.multiverse.api.TxnThreadLocal.clearThreadLocalTxn;

/**
 * http://en.wikipedia.org/wiki/Sleeping_barber_problem
 */
public class SleepingBarberStressTest {

    private BarberShop barberShop;
    private volatile boolean stop;

    @Before
    public void setUp() {
        clearThreadLocalTxn();
        barberShop = new BarberShop();
        stop = false;
    }

    @Test
    public void test() {
        BarberThread thread = new BarberThread();
        CustomerSpawnThread spawnThread = new CustomerSpawnThread();

        startAll(thread, spawnThread);
        sleepMs(30 * 1000);
        stop = true;
        joinAll(thread, spawnThread);
    }

    @SuppressWarnings({"ObjectAllocationInLoop"})
    class CustomerSpawnThread extends TestThread {
        public CustomerSpawnThread() {
            super("CustomerSpawnThread");
        }

        @Override
        public void doRun() throws Exception {
            int customerId = 1;
            while (!stop) {
                CustomerThread customerThread = new CustomerThread(customerId);
                customerThread.start();
                customerId++;
                sleepRandomMs(100);
            }
        }
    }

    class BarberThread extends TestThread {
        public BarberThread() {
            super("BarberThread");
        }

        @Override
        public void doRun() {
            TxnVoidClosure closure = new TxnVoidClosure() {
                @Override
                public void call(Txn tx) throws Exception {
                    //todo
                }
            };

            while (!stop) {
                atomic(closure);
            }

            barberShop.atomicClose();
        }
    }

    class CustomerThread extends TestThread {
        public CustomerThread(int id) {
            super("CustomerThread-" + id);
        }

        @Override
        public void doRun() throws Exception {
            atomic(new TxnVoidClosure() {
                @Override
                public void call(Txn tx) throws Exception {
                    if (barberShop.closed.get()) {
                        return;
                    }

                    if (barberShop.freeSeats.get() == 0) {
                        return;
                    }

                    //todo
                }
            });
        }
    }

    static class BarberShop {
        private TxnBoolean closed = newTxnBoolean(false);
        private TxnInteger freeSeats = newTxnInteger(5);

        void atomicClose() {
            closed.atomicSet(false);
        }
    }
}
