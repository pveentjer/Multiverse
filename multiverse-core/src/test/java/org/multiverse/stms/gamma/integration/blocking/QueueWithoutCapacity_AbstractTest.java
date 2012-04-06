package org.multiverse.stms.gamma.integration.blocking;

import org.junit.Before;
import org.multiverse.TestThread;
import org.multiverse.api.TxnExecutor;
import org.multiverse.api.Transaction;
import org.multiverse.api.closures.AtomicClosure;
import org.multiverse.api.closures.AtomicVoidClosure;
import org.multiverse.stms.gamma.GammaConstants;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactionalobjects.GammaRef;
import org.multiverse.stms.gamma.transactions.GammaTransaction;

import java.util.LinkedList;

import static org.junit.Assert.assertEquals;
import static org.multiverse.TestUtils.*;
import static org.multiverse.api.GlobalStmInstance.getGlobalStmInstance;
import static org.multiverse.api.StmUtils.retry;
import static org.multiverse.api.ThreadLocalTransaction.clearThreadLocalTransaction;

public abstract class QueueWithoutCapacity_AbstractTest implements GammaConstants {

    protected GammaStm stm;
    private Queue<Integer> queue;
    private int itemCount = 10 * 1000 * 1000;

    protected abstract TxnExecutor newPopBlock();

    protected abstract TxnExecutor newPushBlock();

    @Before
    public void setUp() {
        clearThreadLocalTransaction();
        stm = (GammaStm) getGlobalStmInstance();
    }

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
                    sleepMs(100);
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

        public void push(final E item) {
            pushBlock.atomic(new AtomicVoidClosure() {
                @Override
                public void execute(Transaction tx) throws Exception {
                    GammaTransaction btx = (GammaTransaction) tx;
                    pushedStack.push(btx, item);
                }
            });
        }

        public E pop() {
            return popBlock.atomic(new AtomicClosure<E>() {
                @Override
                public E execute(Transaction tx) throws Exception {
                    GammaTransaction btx = (GammaTransaction) tx;

                    if (!readyToPopStack.isEmpty(btx)) {
                        return readyToPopStack.pop(btx);
                    }

                    while (!pushedStack.isEmpty(btx)) {
                        E item = pushedStack.pop(btx);
                        readyToPopStack.push(btx, item);
                    }

                    return readyToPopStack.pop(btx);
                }
            });
        }
    }

    class Stack<E> {
        final GammaRef<Node<E>> head = new GammaRef<Node<E>>(stm);

        void push(GammaTransaction tx, E item) {
            Node<E> newHead = new Node<E>(item, head.get());
            head.set(newHead);
        }

        boolean isEmpty(GammaTransaction tx) {
            return head.isNull();
        }

        E pop(GammaTransaction tx) {
            Node<E> node = head.get();

            if (node == null) {
                retry();
            }

            head.set(node.next);
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
