package org.multiverse.stms.gamma.integration.blocking;

import org.junit.Before;
import org.multiverse.TestThread;
import org.multiverse.api.Txn;
import org.multiverse.api.TxnExecutor;
import org.multiverse.api.closures.TxnClosure;
import org.multiverse.api.closures.TxnVoidClosure;
import org.multiverse.stms.gamma.GammaConstants;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactionalobjects.GammaIntRef;
import org.multiverse.stms.gamma.transactionalobjects.GammaRef;
import org.multiverse.stms.gamma.transactions.GammaTxn;

import java.util.LinkedList;

import static org.junit.Assert.assertEquals;
import static org.multiverse.TestUtils.joinAll;
import static org.multiverse.TestUtils.startAll;
import static org.multiverse.api.GlobalStmInstance.getGlobalStmInstance;
import static org.multiverse.api.StmUtils.retry;
import static org.multiverse.api.TxnThreadLocal.clearThreadLocalTxn;

public abstract class QueueWithCapacity_AbstractTest implements GammaConstants {

    protected GammaStm stm;
    private Queue<Integer> queue;
    private int itemCount = 2 * 1000 * 1000;
    private int maxCapacity = 1000;

    @Before
    public void setUp() {
        clearThreadLocalTxn();
        stm = (GammaStm) getGlobalStmInstance();
    }

    protected abstract TxnExecutor newPopBlock();

    protected abstract TxnExecutor newPushBlock();


    public void run() {
        queue = new Queue<Integer>();
        ProduceThread produceThread = new ProduceThread();
        ConsumeThread consumeThread = new ConsumeThread();

        startAll(produceThread, consumeThread);

        joinAll(produceThread, consumeThread);

        assertEquals(itemCount, produceThread.producedItems.size());
        assertEquals(produceThread.producedItems, consumeThread.consumedItems);
    }

    class ConsumeThread extends TestThread {

        private final LinkedList<Integer> consumedItems = new LinkedList<Integer>();

        public ConsumeThread() {
            super("ConsumeThread");
        }

        @Override
        public void doRun() throws Exception {
            for (int k = 0; k < itemCount; k++) {
                int item = queue.pop();
                consumedItems.add(item);

                if (k % 100000 == 0) {
                    System.out.printf("%s is at %s\n", getName(), k);
                }
            }
        }
    }

    class ProduceThread extends TestThread {

        private final LinkedList<Integer> producedItems = new LinkedList<Integer>();

        public ProduceThread() {
            super("ProduceThread");
        }

        @Override
        public void doRun() throws Exception {
            for (int k = 0; k < itemCount; k++) {
                queue.push(k);
                producedItems.add(k);

                if (k % 100000 == 0) {
                    System.out.printf("%s is at %s\n", getName(), k);
                }
            }
        }
    }

    class Queue<E> {
        final Stack<E> pushedStack = new Stack<E>();
        final Stack<E> readyToPopStack = new Stack<E>();
        final TxnExecutor pushBlock = newPushBlock();
        final TxnExecutor popBlock = newPopBlock();

        final GammaIntRef size = new GammaIntRef(stm);

        public void push(final E item) {
            pushBlock.atomic(new TxnVoidClosure() {
                @Override
                public void execute(Txn tx) throws Exception {
                    if (size.get() >= maxCapacity) {
                        retry();
                    }

                    GammaTxn btx = (GammaTxn) tx;
                    size.incrementAndGet(1);
                    pushedStack.push(btx, item);
                }
            });
        }

        public E pop() {
            return popBlock.atomic(new TxnClosure<E>() {
                @Override
                public E execute(Txn tx) throws Exception {
                    GammaTxn btx = (GammaTxn) tx;

                    if (!readyToPopStack.isEmpty(btx)) {
                        size.decrement();
                        return readyToPopStack.pop(btx);
                    }

                    while (!pushedStack.isEmpty(btx)) {
                        E item = pushedStack.pop(btx);
                        readyToPopStack.push(btx, item);
                    }

                    if (!readyToPopStack.isEmpty(btx)) {
                        size.decrement();
                        return readyToPopStack.pop(btx);
                    }

                    retry();
                    return null;
                }
            });
        }
    }

    class Stack<E> {
        final GammaRef<Node<E>> head = new GammaRef<Node<E>>(stm);

        void push(GammaTxn tx, E item) {
            head.set(tx, new Node<E>(item, head.get(tx)));
        }

        boolean isEmpty(GammaTxn tx) {
            return head.isNull(tx);
        }

        E pop(GammaTxn tx) {
            Node<E> node = head.get();
            if (node == null) {
                tx.retry();
            }

            if (node == null) {
                retry();
            }

            head.set(tx, node.next);
            return node.item;
        }
    }

    class Node<E> {
        final E item;
        Node<E> next;

        Node(E item, Node<E> next) {
            this.item = item;
            this.next = next;
        }
    }
}
