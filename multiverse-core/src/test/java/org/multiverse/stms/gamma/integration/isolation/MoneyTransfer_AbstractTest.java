package org.multiverse.stms.gamma.integration.isolation;

import org.junit.After;
import org.junit.Before;
import org.multiverse.TestThread;
import org.multiverse.api.Txn;
import org.multiverse.api.TxnExecutor;
import org.multiverse.api.callables.TxnVoidCallable;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactionalobjects.GammaTxnLong;
import org.multiverse.stms.gamma.transactionalobjects.Tranlocal;
import org.multiverse.stms.gamma.transactions.GammaTxn;

import static org.junit.Assert.assertEquals;
import static org.multiverse.TestUtils.*;
import static org.multiverse.api.GlobalStmInstance.getGlobalStmInstance;
import static org.multiverse.api.TxnThreadLocal.clearThreadLocalTxn;

//todo: testing of different lock modes
//todo: testing if multiple transfers are done
public abstract class MoneyTransfer_AbstractTest {

    private volatile boolean stop;

    private GammaTxnLong[] accounts;
    protected GammaStm stm;

    @Before
    public void setUp() {
        clearThreadLocalTxn();
        stop = false;
        stm = (GammaStm) getGlobalStmInstance();
    }

    @After
    public void tearDown() {
        System.out.println("Stm.GlobalConflictCount: " + stm.getGlobalConflictCounter().count());
        for (GammaTxnLong ref : accounts) {
            System.out.println(ref.toDebugString());
        }
    }

    protected abstract TxnExecutor newTxnExecutor();

    public void run(int accountCount, int threadCount) {
        accounts = new GammaTxnLong[accountCount];

        long initialAmount = 0;
        for (int k = 0; k < accountCount; k++) {
            long amount = randomInt(1000);
            initialAmount += amount;
            accounts[k] = new GammaTxnLong(stm, amount);
        }

        TransferThread[] threads = createThreads(threadCount);

        startAll(threads);

        sleepMs(30 * 1000);

        stop = true;

        joinAll(threads);

        assertEquals(initialAmount, getTotal());

    }

    private long getTotal() {
        long sum = 0;
        for (GammaTxnLong account : accounts) {
            sum += account.atomicGet();
        }
        return sum;
    }

    private TransferThread[] createThreads(int threadCount) {
        TransferThread[] threads = new TransferThread[threadCount];
        for (int k = 0; k < threads.length; k++) {
            threads[k] = new TransferThread(k);
        }
        return threads;
    }

    private class TransferThread extends TestThread {

        public TransferThread(int id) {
            super("TransferThread-" + id);
        }

        public void doRun() {
            TxnExecutor executor = newTxnExecutor();

            TxnVoidCallable callable = new TxnVoidCallable() {
                @Override
                public void call(Txn tx) throws Exception {
                    GammaTxn btx = (GammaTxn) tx;
                    GammaTxnLong from = accounts[randomInt(accounts.length)];
                    GammaTxnLong to = accounts[randomInt(accounts.length)];
                    int amount = randomInt(100);

                    to.openForWrite(btx, LOCKMODE_NONE).long_value += amount;

                    sleepRandomMs(10);

                    Tranlocal toTranlocal = from.openForWrite(btx, LOCKMODE_NONE);
                    if (toTranlocal.long_value < 0) {
                        throw new NotEnoughMoneyException();
                    }

                    toTranlocal.long_value -= amount;
                }
            };

            int k = 0;
            while (!stop) {
                try {
                    executor.execute(callable);
                    if ((k % 500) == 0) {
                        System.out.printf("%s is at iteration %s\n", getName(), k);
                    }
                    k++;
                } catch (NotEnoughMoneyException ignore) {
                }
            }
        }
    }

    private static class NotEnoughMoneyException extends RuntimeException {
    }
}
