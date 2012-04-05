package org.multiverse.stms.gamma.integration.blocking;

import org.junit.After;
import org.junit.Before;
import org.multiverse.TestThread;
import org.multiverse.api.AtomicBlock;
import org.multiverse.api.Transaction;
import org.multiverse.api.closures.AtomicClosure;
import org.multiverse.api.closures.AtomicVoidClosure;
import org.multiverse.stms.gamma.GammaConstants;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactionalobjects.GammaRef;

import java.util.HashSet;
import java.util.LinkedList;

import static org.junit.Assert.assertEquals;
import static org.multiverse.TestUtils.joinAll;
import static org.multiverse.TestUtils.startAll;
import static org.multiverse.api.ThreadLocalTransaction.clearThreadLocalTransaction;

/**
 * The test is not very efficient since a lot of temporary objects like the transaction template are created.
 * But that is alright for this test since it isn't a benchmark.
 *
 * @author Peter Veentjer.
 */
public abstract class StackWithoutCapacity_AbstractTest implements GammaConstants {

    public GammaStm stm;
    private int itemCount = 5 * 1000 * 1000;
    private Stack<Integer> stack;

    @Before
    public void setUp() {
        clearThreadLocalTransaction();
        stm = new GammaStm();
    }

    @After
    public void tearDown() {
        if (stack != null) {
            System.out.println(stack.head.toDebugString());
        }
    }

    public void run() {
        stack = new Stack<Integer>();

        ProduceThread produceThread = new ProduceThread();
        ConsumeThread consumeThread = new ConsumeThread();

        startAll(produceThread, consumeThread);
        joinAll(produceThread, consumeThread);

        System.out.println("finished executing, checking if content is correct (can take some time)");

        assertEquals(itemCount, produceThread.producedItems.size());

        assertEquals(
                new HashSet<Integer>(produceThread.producedItems),
                new HashSet<Integer>(consumeThread.consumedItems));

        System.out.println("Finished comparing content");
    }

    class ConsumeThread extends TestThread {

        private final LinkedList<Integer> consumedItems = new LinkedList<Integer>();

        public ConsumeThread() {
            super("ConsumeThread");
        }

        @Override
        public void doRun() throws Exception {
            for (int k = 0; k < itemCount; k++) {
                int item = stack.pop();
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
                stack.push(k);
                producedItems.add(k);

                if (k % 100000 == 0) {
                    System.out.printf("%s is at %s\n", getName(), k);
                }
            }
        }
    }

    class Stack<E> {
        private final GammaRef<Node<E>> head = new GammaRef<Node<E>>(stm);
        private final AtomicBlock pushBlock = newPushAtomicBLock();

        private final AtomicBlock popBlock = newPopAtomicBLock();

        public void push(final E item) {
            pushBlock.atomic(new AtomicVoidClosure() {
                @Override
                public void execute(Transaction tx) throws Exception {
                    head.set(new Node<E>(item, head.get()));
                }
            });
        }

        public E pop() {
            return popBlock.atomic(new AtomicClosure<E>() {
                @Override
                public E execute(Transaction tx) throws Exception {
                    Node<E> node = head.awaitNotNullAndGet();
                    head.set(node.next);
                    return node.item;
                }
            });
        }
    }

    protected abstract AtomicBlock newPopAtomicBLock();

    protected abstract AtomicBlock newPushAtomicBLock();

    class Node<E> {
        final E item;
        Node<E> next;

        Node(E item, Node<E> next) {
            this.item = item;
            this.next = next;
        }
    }
}
