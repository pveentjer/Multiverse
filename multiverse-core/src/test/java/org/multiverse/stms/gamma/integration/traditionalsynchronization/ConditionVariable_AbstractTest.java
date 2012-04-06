package org.multiverse.stms.gamma.integration.traditionalsynchronization;

import org.junit.Before;
import org.multiverse.TestThread;
import org.multiverse.api.Txn;
import org.multiverse.api.TxnExecutor;
import org.multiverse.api.closures.TxnClosure;
import org.multiverse.api.closures.TxnVoidClosure;
import org.multiverse.api.references.TxnBoolean;
import org.multiverse.api.references.TxnInteger;
import org.multiverse.api.references.TxnRef;
import org.multiverse.stms.gamma.GammaStm;

import static org.multiverse.TestUtils.joinAll;
import static org.multiverse.TestUtils.startAll;
import static org.multiverse.api.GlobalStmInstance.getGlobalStmInstance;
import static org.multiverse.api.StmUtils.*;
import static org.multiverse.api.TxnThreadLocal.clearThreadLocalTxn;

public abstract class ConditionVariable_AbstractTest {
    protected GammaStm stm;
    private Stack stack;
    private int itemCount = 10000000;

    @Before
    public void setUp() {
        stm = (GammaStm) getGlobalStmInstance();
        clearThreadLocalTxn();
    }

    protected abstract TxnExecutor newPopBlock();

    protected abstract TxnExecutor newPushBlock();


    public void run() {
        stack = new Stack(100);

        PushThread pushThread = new PushThread();
        PopThread popThread = new PopThread();

        startAll(pushThread, popThread);
        joinAll(pushThread, popThread);
    }

    public class PushThread extends TestThread {

        public PushThread() {
            super("PushThread");
        }

        @Override
        public void doRun() throws Exception {
            for (int k = 0; k < itemCount; k++) {
                stack.push("foo");

                if (k % 100000 == 0) {
                    System.out.printf("%s is at %s\n", getName(), k);
                }
            }
        }
    }

    public class PopThread extends TestThread {
        @Override
        public void doRun() throws Exception {
            for (int k = 0; k < itemCount; k++) {
                stack.pop();

                if (k % 100000 == 0) {
                    System.out.printf("%s is at %s\n", getName(), k);
                }
            }
        }
    }

    class Stack {
        ConditionVariable isNotFull = new ConditionVariable(true);
        ConditionVariable isNotEmpty = new ConditionVariable(false);
        TxnRef<Node> head = newTxnRef();
        TxnInteger size = newTxnInteger();
        final int capacity;
        final TxnExecutor pushBlock = newPushBlock();
        final TxnExecutor popBlock = newPopBlock();

        Stack(int capacity) {
            this.capacity = capacity;
        }

        void push(final String item) {
            pushBlock.atomic(new TxnVoidClosure() {
                @Override
                public void call(Txn tx) throws Exception {
                    isNotFull.awaitTrue();

                    head.set(new Node(item, head.get()));
                    size.increment();
                    if (size.get() == capacity) {
                        isNotFull.set(false);
                    }

                    isNotEmpty.set(true);
                }
            });
        }

        String pop() {
            return popBlock.atomic(new TxnClosure<String>() {
                @Override
                public String call(Txn tx) throws Exception {
                    isNotEmpty.awaitTrue();

                    Node node = head.get();
                    head.set(node.next);
                    size.decrement();
                    if (size.get() == 0) {
                        isNotEmpty.set(false);
                    }
                    isNotFull.set(true);
                    return node.value;
                }
            });
        }
    }

    class Node {
        final String value;
        final Node next;

        Node(String value, Node next) {
            this.value = value;
            this.next = next;
        }
    }

    class ConditionVariable {
        final TxnBoolean ref;

        ConditionVariable(boolean value) {
            this.ref = newTxnBoolean(value);
        }

        void awaitTrue() {
            ref.await(true);
        }

        void awaitFalse() {
            ref.await(false);
        }

        void set(boolean value) {
            ref.set(value);
        }
    }
}
