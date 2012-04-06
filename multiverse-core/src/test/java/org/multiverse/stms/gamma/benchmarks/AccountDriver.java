package org.multiverse.stms.gamma.benchmarks;

import org.benchy.BenchmarkDriver;
import org.benchy.TestCaseResult;
import org.multiverse.TestThread;
import org.multiverse.api.Txn;
import org.multiverse.api.TxnExecutor;
import org.multiverse.api.callables.TxnDoubleCallable;
import org.multiverse.api.callables.TxnVoidCallable;
import org.multiverse.api.references.TxnDouble;
import org.multiverse.stms.gamma.GammaStm;

import java.util.Random;

import static org.multiverse.TestUtils.joinAll;
import static org.multiverse.TestUtils.startAll;

public class AccountDriver extends BenchmarkDriver {
    public static final int WARMUP_PHASE = 1;
    public static final int TEST_PHASE = 2;
    public static final int SHUTDOWN_PHASE = 3;

    private GammaStm stm;
    private int accountCount;
    private Bank bank;
    private int threadCount;
    private BenchmarkThread[] threads;
    private int readFrequency;
    private int writeFrequency;

    @Override
    public void setUp() {
        stm = new GammaStm();
        bank = new Bank(accountCount);

        threads = new BenchmarkThread[threadCount];
        for (int k = 0; k < threads.length; k++) {
            //threads[k] = new BenchmarkThread(k, );
        }
    }

    @Override
    public void run(TestCaseResult testCaseResult) {
        startAll(threads);
        joinAll(threads);
    }

    @Override
    public void processResults(TestCaseResult testCaseResult) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    class BenchmarkThread extends TestThread {

        final private int id;
        final private int nb;
        final private int max;
        final private int readThreads;
        final private int writeThreads;
        int transferCount;
        int readCount;
        int writeCount;
        final private Random random;
        volatile private int phase;
        private int steps;

        BenchmarkThread(int id, int nb, int max, int readThreads, int writeThreads) {
            phase = WARMUP_PHASE;
            steps = 0;
            this.id = id;
            this.nb = nb;
            this.max = max;
            this.readThreads = readThreads;
            this.writeThreads = writeThreads;
            transferCount = readCount = writeCount = 0;
            random = new Random();
        }

        public void setPhase(int phase) {
            this.phase = phase;
        }

        public int getSteps() {
            return steps;
        }

        public void doRun() {
            while (phase == WARMUP_PHASE) {
                step(WARMUP_PHASE);
            }

            while (phase == TEST_PHASE) {
                step(TEST_PHASE);
                steps++;
            }
        }

        protected void step(int phase) {

            if (id < readThreads) {
                // Compute total of all accounts (read-all transaction)
                bank.computeTotal();
                if (phase == TEST_PHASE)
                    readCount++;
            } else if (id < readThreads + writeThreads) {
                // Add 0% interest (write-all transaction)
                bank.addInterest(0);
                if (phase == TEST_PHASE)
                    writeCount++;
            } else {
                int i = random.nextInt(100);
                if (i < readFrequency) {
                    // Compute total of all accounts (read-all transaction)
                    bank.computeTotal();
                    if (phase == TEST_PHASE)
                        readCount++;
                } else if (i < readFrequency + writeFrequency) {
                    // Add 0% interest (write-all transaction)
                    bank.addInterest(0);
                    if (phase == TEST_PHASE)
                        writeCount++;
                } else {
                    int amount = random.nextInt(max) + 1;
                    Account src;
                    Account dst;
                    if (s_disjoint && nb <= bank.accounts.length) {
                        src = bank.accounts[random.nextInt(bank.accounts.length / nb) * nb + id];
                        dst = bank.accounts[random.nextInt(bank.accounts.length / nb) * nb + id];
                    } else {
                        src = bank.accounts[random.nextInt(bank.accounts.length)];
                        dst = bank.accounts[random.nextInt(bank.accounts.length)];
                    }

                    try {
                        bank.transfer(src, dst, amount);
                        if (phase == TEST_PHASE)
                            transferCount++;
                    } catch (OverdraftException e) {
                        System.err.println("Overdraft: " + e.getMessage());
                    }
                }
            }
        }

        public String getStats() {
            return "T=" + transferCount + ", R=" + readCount + ", W=" + writeCount;
        }
    }

    static volatile boolean s_disjoint = false;
    static volatile boolean s_yield = false;

    class Bank {
        private final Account[] accounts;
        private final TxnExecutor addInterrestBlock = stm.newTxnFactoryBuilder().newTxnExecutor();
        private final TxnExecutor computeTotalBlock = stm.newTxnFactoryBuilder().newTxnExecutor();
        private final TxnExecutor transferBlock = stm.newTxnFactoryBuilder().newTxnExecutor();

        public Bank(int accountCount) {
            accounts = new Account[accountCount];
            for (int k = 0; k < accounts.length; k++) {
                accounts[k] = new Account("user-" + k, 0);
            }
        }

        public void addInterest(final float rate) {
            addInterrestBlock.atomic(new TxnVoidCallable() {
                @Override
                public void call(Txn tx) throws Exception {
                    for (Account a : accounts) {
                        a.deposit(a.getBalance() * rate);
                        if (s_yield)
                            Thread.yield();
                    }
                }
            });
        }

        public double computeTotal() {
            return computeTotalBlock.atomic(new TxnDoubleCallable() {
                @Override
                public double call(Txn tx) throws Exception {
                    double total = 0.0;
                    for (Account a : accounts) {
                        total += a.getBalance();
                        if (s_yield)
                            Thread.yield();
                    }
                    return total;
                }
            });
        }

        public void transfer(final Account src, final Account dst, final float amount) throws OverdraftException {
            transferBlock.atomic(new TxnVoidCallable() {
                @Override
                public void call(Txn tx) throws Exception {
                    dst.deposit(amount);
                    if (s_yield)
                        Thread.yield();
                    src.withdraw(amount);
                }
            });
        }
    }

    public class Account {

        private final String name;
        private final TxnDouble balance;

        public Account(String name, double balance) {
            this.balance = stm.getDefaultRefFactory().newTxnDouble(balance);
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public double getBalance() {
            return balance.get();
        }

        public void deposit(double amount) {
            balance.incrementAndGet(amount);
        }

        public void withdraw(double amount) throws OverdraftException {
            if (balance.get() < amount)
                throw new OverdraftException("Cannot withdraw $" + amount + " from $" + balance.get());
            balance.incrementAndGet(-amount);
        }
    }

    public class OverdraftException extends Exception {

        public OverdraftException(String reason) {
            super(reason);
        }
    }
}
