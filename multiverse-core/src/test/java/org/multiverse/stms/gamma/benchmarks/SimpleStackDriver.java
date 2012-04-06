package org.multiverse.stms.gamma.benchmarks;

import org.benchy.BenchmarkDriver;
import org.benchy.TestCaseResult;
import org.multiverse.TestThread;
import org.multiverse.api.Txn;
import org.multiverse.api.TxnExecutor;
import org.multiverse.api.LockMode;
import org.multiverse.api.callables.TxnBooleanCallable;
import org.multiverse.api.callables.TxnVoidCallable;
import org.multiverse.api.references.TxnInteger;
import org.multiverse.api.references.TxnRef;
import org.multiverse.stms.gamma.GammaStm;

import static org.benchy.BenchyUtils.format;
import static org.multiverse.TestUtils.joinAll;
import static org.multiverse.TestUtils.startAll;

public class SimpleStackDriver extends BenchmarkDriver {

    private int pushThreadCount = 1;
    private int popThreadCount = 1;
    private int capacity = Integer.MAX_VALUE;
    private boolean poolCallables = false;
    private LockMode readLockMode = LockMode.None;
    private LockMode writeLockMode = LockMode.None;
    private boolean dirtyCheck = false;

    private GammaStm stm;
    private PopThread[] popThreads;
    private PushThread[] pushThreads;
    private Stack<String> stack;

    @Override
    public void setUp() {
        System.out.printf("Multiverse > Pop threadcount %s\n", pushThreadCount);
        System.out.printf("Multiverse > Push threadcount %s\n", popThreadCount);
        if (capacity == Integer.MAX_VALUE) {
            System.out.printf("Multiverse > Capacity unbound\n");
        } else {
            System.out.printf("Multiverse > Capacity %s\n", capacity);
        }
        System.out.printf("Multiverse > Pool Callables %s\n", poolCallables);
        System.out.printf("Multiverse > LockLevel %s\n", readLockMode);
        System.out.printf("Multiverse > DirtyCheck %s\n", dirtyCheck);

        stm = new GammaStm();
        stack = new Stack<String>();

        pushThreads = new PushThread[pushThreadCount];
        for (int k = 0; k < pushThreadCount; k++) {
            pushThreads[k] = new PushThread(k, stack);
        }

        popThreads = new PopThread[popThreadCount];
        for (int k = 0; k < popThreadCount; k++) {
            popThreads[k] = new PopThread(k, stack);
        }
    }

    @Override
    public void run(TestCaseResult testCaseResult) {
        startAll(pushThreads);
        startAll(popThreads);

        joinAll(pushThreads);
        joinAll(popThreads);
    }

    @Override
    public void processResults(TestCaseResult testCaseResult) {
        long pushCount = 0;
        long totalDurationMs = 0;
        for (PushThread t : pushThreads) {
            pushCount += t.count;
            totalDurationMs += t.getDurationMs();
        }

        long popCount = 0;
        for (PopThread t : popThreads) {
            popCount += t.count;
            totalDurationMs += t.getDurationMs();
        }

        int threadCount = pushThreadCount + popThreadCount;
        long count = pushCount + popCount;
        System.out.printf("Multiverse > Total number of transactions %s\n", count);
        double transactionsPerSecond = (count * 1000.0d) / totalDurationMs;
        System.out.printf("Multiverse > Performance %s transactions/second with %s threads\n",
                format(transactionsPerSecond), threadCount);

        testCaseResult.put("transactionsPerSecond", transactionsPerSecond);
    }

    class PushThread extends TestThread {
        private final Stack<String> stack;
        private long count;
        private final TxnExecutor pushBlock = stm.newTxnFactoryBuilder()
                .setDirtyCheckEnabled(dirtyCheck)
                .setReadLockMode(readLockMode)
                .setWriteLockMode(writeLockMode)
                .newTxnExecutor();

        public PushThread(int id, Stack<String> stack) {
            super("PushThread-" + id);
            this.stack = stack;
        }

        @Override
        public void doRun() throws Exception {
            if (poolCallables) {
                runWithPooledCallables();
            } else {
                runWithoutPooledCallables();
            }
        }

        private void runWithoutPooledCallables() {
            while (!shutdown) {
                pushBlock.atomic(new TxnVoidCallable() {
                    @Override
                    public void call(Txn tx) throws Exception {
                        stack.push(tx, "item");
                    }
                });
                count++;
            }


            for (int k = 0; k < popThreadCount; k++) {
                pushBlock.atomic(new TxnVoidCallable() {
                    @Override
                    public void call(Txn tx) throws Exception {
                        stack.push(tx, "end");

                    }
                });
            }
        }

        private void runWithPooledCallables() {
            final PushCallable pushCallable = new PushCallable();

            while (!shutdown) {
                pushCallable.item = "item";
                pushBlock.atomic(pushCallable);
                count++;
            }

            for (int k = 0; k < popThreadCount; k++) {
                pushCallable.item = "end";
                pushBlock.atomic(pushCallable);
            }
        }

        class PushCallable implements TxnVoidCallable {
            String item;

            @Override
            public void call(Txn tx) throws Exception {
                stack.push(tx, item);
            }
        }
    }

    class PopThread extends TestThread {

        private final Stack<String> stack;
        private long count;
        private final TxnExecutor popBlock = stm.newTxnFactoryBuilder()
                .setDirtyCheckEnabled(dirtyCheck)
                .setReadLockMode(readLockMode)
                .setWriteLockMode(writeLockMode)
                .newTxnExecutor();

        public PopThread(int id, Stack<String> stack) {
            super("PopThread-" + id);
            this.stack = stack;
        }

        @Override
        public void doRun() throws Exception {
            if (poolCallables) {
                runWithoutPooledCallable();
            } else {
                runWithPooledCallable();
            }
        }

        private void runWithPooledCallable() {
            boolean end = false;
            while (!end) {
                end = popBlock.atomic(new TxnBooleanCallable() {
                    @Override
                    public boolean call(Txn tx) throws Exception {
                        return !stack.pop(tx).equals("end");
                    }
                });

                count++;
            }
        }

        private void runWithoutPooledCallable() {
            PopCallable popCallable = new PopCallable();
            boolean end = false;
            while (!end) {
                end = popBlock.atomic(popCallable);
                count++;
            }
        }

        class PopCallable implements TxnBooleanCallable {
            @Override
            public boolean call(Txn tx) throws Exception {
                return !stack.pop(tx).endsWith("end");
            }
        }
    }

    class Stack<E> {
        private final TxnRef<StackNode<E>> head = stm.getDefaultRefFactory().newTxnRef(null);
        private final TxnInteger size = stm.getTxRefFactoryBuilder().build().newTxnInteger(0);

        public void push(Txn tx, final E item) {
            if (capacity != Integer.MAX_VALUE) {
                if (size.get(tx) == capacity) {
                    tx.retry();
                }
                size.increment(tx);
            }
            head.set(tx, new StackNode<E>(item, head.get(tx)));
        }

        public E pop(Txn tx) {
            E value = head.awaitNotNullAndGet(tx).value;

            if (capacity != Integer.MAX_VALUE) {
                size.decrement(tx);
            }
            return value;
        }
    }

    static class StackNode<E> {
        final E value;
        final StackNode<E> next;

        StackNode(E value, StackNode<E> next) {
            this.value = value;
            this.next = next;
        }
    }
}
