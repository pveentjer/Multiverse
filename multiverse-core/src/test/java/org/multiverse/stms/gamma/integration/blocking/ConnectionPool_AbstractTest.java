package org.multiverse.stms.gamma.integration.blocking;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.TestThread;
import org.multiverse.api.Txn;
import org.multiverse.api.TxnExecutor;
import org.multiverse.api.callables.TxnCallable;
import org.multiverse.api.callables.TxnIntCallable;
import org.multiverse.api.callables.TxnVoidCallable;
import org.multiverse.stms.gamma.GammaConstants;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactionalobjects.GammaTxnInteger;
import org.multiverse.stms.gamma.transactionalobjects.GammaTxnRef;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;
import static org.multiverse.TestUtils.*;
import static org.multiverse.api.GlobalStmInstance.getGlobalStmInstance;
import static org.multiverse.api.TxnThreadLocal.clearThreadLocalTxn;

/**
 * A StressTest that simulates a database connection pool. The code is quite ugly, but that is because
 * no instrumentation is used here.
 *
 * @author Peter Veentjer.
 */
public abstract class ConnectionPool_AbstractTest implements GammaConstants {
    private int poolsize = processorCount();
    private int threadCount = processorCount() * 2;
    private volatile boolean stop;

    private ConnectionPool pool;
    protected GammaStm stm;

    @Before
    public void setUp() {
        clearThreadLocalTxn();
        stm = (GammaStm) getGlobalStmInstance();
        stop = false;
    }

    protected abstract TxnExecutor newReturnBlock();

    protected abstract TxnExecutor newTakeBlock();

    @Test
    public void sanityTest() {
        ConnectionPool pool = new ConnectionPool(2);

        Connection c1 = pool.takeConnection();
        assertEquals(1, pool.size());

        Connection c2 = pool.takeConnection();
        assertEquals(0, pool.size());

        pool.returnConnection(c1);
        assertEquals(1, pool.size());

        pool.returnConnection(c2);
        assertEquals(2, pool.size());
    }

    public void run() {
        pool = new ConnectionPool(poolsize);
        WorkerThread[] threads = createThreads();

        startAll(threads);
        sleepMs(30 * 1000);
        stop = true;
        joinAll(threads);
        assertEquals(poolsize, pool.size());
    }

    class ConnectionPool {
        final TxnExecutor takeConnectionBlock = newTakeBlock();

        final TxnExecutor returnConnectionBlock = newReturnBlock();

        final TxnExecutor sizeBlock = stm.newTxnFactoryBuilder().newTxnExecutor();

        final GammaTxnInteger size = new GammaTxnInteger(stm);
        final GammaTxnRef<Node<Connection>> head = new GammaTxnRef<Node<Connection>>(stm);

        ConnectionPool(final int poolsize) {
            stm.getDefaultTxnExecutor().execute(new TxnVoidCallable() {
                @Override
                public void call(Txn tx) {
                    size.set(poolsize);

                    Node<Connection> h = null;
                    for (int k = 0; k < poolsize; k++) {
                        h = new Node<Connection>(h, new Connection());
                    }
                    head.set(h);
                }
            });
        }

        Connection takeConnection() {
            return takeConnectionBlock.execute(new TxnCallable<Connection>() {
                @Override
                public Connection call(Txn tx) {
                    if (size.get() == 0) {
                        tx.retry();
                    }
                    size.decrement();

                    Node<Connection> current = head.get();
                    head.set(current.next);
                    return current.item;
                }
            });
        }

        void returnConnection(final Connection c) {
            returnConnectionBlock.execute(new TxnVoidCallable() {
                @Override
                public void call(Txn tx) throws Exception {
                    size.incrementAndGet(1);

                    Node<Connection> oldHead = head.get();
                    head.set(new Node<Connection>(oldHead, c));
                }
            });
        }

        int size() {
            return sizeBlock.execute(new TxnIntCallable() {
                @Override
                public int call(Txn tx) throws Exception {
                    return size.get();
                }
            });
        }
    }

    static class Node<E> {
        final Node<E> next;
        final E item;

        Node(Node<E> next, E item) {
            this.next = next;
            this.item = item;
        }
    }

    static class Connection {

        AtomicInteger users = new AtomicInteger();

        void startUsing() {
            if (!users.compareAndSet(0, 1)) {
                fail();
            }
        }

        void stopUsing() {
            if (!users.compareAndSet(1, 0)) {
                fail();
            }
        }
    }

    private WorkerThread[] createThreads() {
        WorkerThread[] threads = new WorkerThread[threadCount];
        for (int k = 0; k < threads.length; k++) {
            threads[k] = new WorkerThread(k);
        }
        return threads;
    }

    class WorkerThread extends TestThread {

        public WorkerThread(int id) {
            super("WorkerThread-" + id);
        }

        @Override
        public void doRun() throws Exception {
            int k = 0;
            while (!stop) {
                if (k % 100 == 0) {
                    System.out.printf("%s is at %s\n", getName(), k);
                }

                Connection c = pool.takeConnection();
                assertNotNull(c);
                c.startUsing();

                try {
                    sleepRandomMs(50);
                } finally {
                    c.stopUsing();
                    pool.returnConnection(c);
                }
                k++;
            }
        }
    }
}
