package org.multiverse.stms.gamma.integration.isolation.writeskew;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.TestThread;
import org.multiverse.api.AtomicBlock;
import org.multiverse.api.IsolationLevel;
import org.multiverse.api.LockMode;
import org.multiverse.api.Transaction;
import org.multiverse.api.closures.AtomicVoidClosure;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactionalobjects.GammaLongRef;
import org.multiverse.stms.gamma.transactions.GammaTransaction;

import java.util.concurrent.atomic.AtomicBoolean;

import static java.lang.String.format;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.multiverse.TestUtils.*;
import static org.multiverse.api.GlobalStmInstance.getGlobalStmInstance;
import static org.multiverse.api.ThreadLocalTransaction.clearThreadLocalTransaction;

/**
 * A Test that checks if the writeskew problem is happening. When
 * pessimisticRead/LockLevel.Read/writeskew=false is used, no writeskew is possible. Otherwise
 * it can happen.
 *
 * @author Peter Veentjer.
 */
public class WriteSkewStressTest {
    private volatile boolean stop;
    private Customer customer1;
    private Customer customer2;

    enum Mode {
        snapshot,
        pessimisticReadLevel,
        pessimisticWriteLevel,
        pessimisticReads,
        pessimisticWrites,
        serialized
    }

    private Mode mode;
    private TransferThread[] threads;
    private AtomicBoolean writeSkewEncountered = new AtomicBoolean();
    private GammaStm stm;
    private int threadCount = 8;

    @Before
    public void setUp() {
        stm = (GammaStm) getGlobalStmInstance();
        clearThreadLocalTransaction();
        customer1 = new Customer();
        customer2 = new Customer();
        stop = false;
        writeSkewEncountered.set(false);

        threads = new TransferThread[threadCount];
        for (int k = 0; k < threads.length; k++) {
            threads[k] = new TransferThread(k);
        }

        GammaLongRef account = customer1.getRandomAccount();
        GammaTransaction tx = stm.newDefaultTransaction();
        account.openForWrite(tx, LOCKMODE_NONE).long_value = 1000;
        tx.commit();
    }

    @Test
    public void whenPessimisticRead_thenNoWriteSkewPossible() {
        mode = Mode.pessimisticReads;
        startAll(threads);
        sleepMs(getStressTestDurationMs(30 * 1000));
        stop = true;

        joinAll(threads);

        System.out.println("User1: " + customer1);
        System.out.println("User2: " + customer2);

        assertFalse("writeskew detected", writeSkewEncountered.get());
    }

    /**
     * If this test fails, the anomaly we are looking for, hasn't occurred yet. Try increasing the
     * running time.
     */
    @Test
    public void whenPessimisticWrite_thenWriteSkewPossible() {
        mode = Mode.pessimisticWrites;
        startAll(threads);
        sleepMs(getStressTestDurationMs(30 * 1000));
        stop = true;

        joinAll(threads);

        System.out.println("User1: " + customer1);
        System.out.println("User2: " + customer2);

        assertTrue("no writeskew detected", writeSkewEncountered.get());
    }

    @Test
    public void whenPessimisticWriteSanityTest() {
        Customer customer1 = new Customer();
        Customer customer2 = new Customer();

        GammaTransaction tx1 = stm.newDefaultTransaction();
        GammaTransaction tx2 = stm.newDefaultTransaction();

        customer1.account1.get(tx1);
        customer1.account2.get(tx1);
        customer1.account2.get(tx1);
        customer2.account2.get(tx1);

        customer1.account1.get(tx2);
        customer1.account2.get(tx2);
        customer2.account1.get(tx2);
        customer2.account2.get(tx2);

        customer1.account1.openForWrite(tx1, LOCKMODE_READ).long_value++;
        customer2.account1.openForWrite(tx1, LOCKMODE_READ).long_value++;

        customer1.account2.openForWrite(tx2, LOCKMODE_READ).long_value++;
        customer2.account2.openForWrite(tx2, LOCKMODE_READ).long_value++;

        tx1.commit();
        tx2.commit();
    }


    @Test
    public void whenPessimisticWriteLevel_thenWriteSkewPossible() {
        mode = Mode.pessimisticWriteLevel;
        startAll(threads);
        sleepMs(getStressTestDurationMs(30 * 1000));
        stop = true;

        joinAll(threads);

        System.out.println("User1: " + customer1);
        System.out.println("User2: " + customer2);

        assertTrue("no writeskew detected", writeSkewEncountered.get());
    }

    @Test
    public void whenPessimisticWriteLevelSanityTest() {
        Customer customer1 = new Customer();
        Customer customer2 = new Customer();

        GammaTransaction tx1 = stm.newTransactionFactoryBuilder()
                .setSpeculative(false)
                .setWriteLockMode(LockMode.Read)
                .newTransactionFactory()
                .newTransaction();
        GammaTransaction tx2 = stm.newTransactionFactoryBuilder()
                .setSpeculative(false)
                .setWriteLockMode(LockMode.Read)
                .newTransactionFactory()
                .newTransaction();

        customer1.account1.get(tx1);
        customer1.account2.get(tx1);
        customer1.account2.get(tx1);
        customer2.account2.get(tx1);

        customer1.account1.get(tx2);
        customer1.account2.get(tx2);
        customer2.account1.get(tx2);
        customer2.account2.get(tx2);

        customer1.account1.openForWrite(tx1, LOCKMODE_NONE).long_value++;
        customer2.account1.openForWrite(tx1, LOCKMODE_NONE).long_value++;

        customer1.account2.openForWrite(tx2, LOCKMODE_NONE).long_value++;
        customer2.account2.openForWrite(tx2, LOCKMODE_NONE).long_value++;

        tx1.commit();
        tx2.commit();
    }

    @Test
    public void whenPessimisticReadLevel_thenNoWriteSkewPossible() {
        mode = Mode.pessimisticReadLevel;
        startAll(threads);
        sleepMs(getStressTestDurationMs(30 * 1000));
        stop = true;

        joinAll(threads);

        System.out.println("User1: " + customer1);
        System.out.println("User2: " + customer2);

        assertFalse("writeskew detected", writeSkewEncountered.get());
    }


    @Test
    public void whenSnapshotIsolation_thenWriteSkewPossible() {
        mode = Mode.snapshot;
        startAll(threads);
        sleepMs(getStressTestDurationMs(30 * 1000));
        stop = true;
        joinAll(threads);

        System.out.println("User1: " + customer1);
        System.out.println("User2: " + customer2);

        System.out.println("User1.account1: " + customer1.account1.toDebugString());
        System.out.println("User1.account2: " + customer1.account2.toDebugString());

        System.out.println("User2.account1: " + customer2.account1.toDebugString());
        System.out.println("User2.account2: " + customer2.account2.toDebugString());

        assertTrue(writeSkewEncountered.get());
    }

    @Test
    public void whenSerializedIsolationLevel_thenWriteSkewNotPossible() {
        mode = Mode.serialized;
        startAll(threads);
        sleepMs(getStressTestDurationMs(30 * 1000));
        stop = true;

        joinAll(threads);

        System.out.println("User1: " + customer1);
        System.out.println("User2: " + customer2);

        assertFalse("writeskew detected", writeSkewEncountered.get());
    }

    public class TransferThread extends TestThread {

        private final AtomicBlock snapshotBlock = stm.newTransactionFactoryBuilder()
                .setSpeculative(false)
                .setMaxRetries(10000)
                .newAtomicBlock();
        private final AtomicBlock serializedBlock = stm.newTransactionFactoryBuilder()
                .setSpeculative(false)
                .setIsolationLevel(IsolationLevel.Serializable)
                .setMaxRetries(10000)
                .newAtomicBlock();
        private final AtomicBlock pessimisticReadsBlock = stm.newTransactionFactoryBuilder()
                .setSpeculative(false)
                .setReadLockMode(LockMode.Read)
                .setMaxRetries(10000)
                .newAtomicBlock();
        private final AtomicBlock pessimisticWritesBlock = stm.newTransactionFactoryBuilder()
                .setSpeculative(false)
                .setWriteLockMode(LockMode.Read)
                .setMaxRetries(10000)
                .newAtomicBlock();


        public TransferThread(int id) {
            super("TransferThread-" + id);
        }

        @Override
        public void doRun() throws Exception {
            int k = 0;
            while (!stop) {
                if (k % 100 == 0) {
                    System.out.printf("%s is at %s\n", getName(), k);
                }

                switch (mode) {
                    case snapshot:
                        runWithSnapshotIsolation();
                        break;
                    case serialized:
                        runWithSerializedIsolation();
                        break;
                    case pessimisticReadLevel:
                        runWithPessimisticReadLevel();
                        break;
                    case pessimisticWriteLevel:
                        runWithPessimisticWriteLevel();
                        break;
                    case pessimisticReads:
                        runWithPessimisticReads();
                        break;
                    case pessimisticWrites:
                        runWithPessimisticWrites();
                        break;
                    default:
                        throw new IllegalStateException();
                }

                k++;
            }
        }

        private void runWithPessimisticReadLevel() {
            pessimisticReadsBlock.atomic(new AtomicVoidClosure() {
                @Override
                public void execute(Transaction tx) throws Exception {
                    GammaTransaction btx = (GammaTransaction) tx;
                    run(btx, LockMode.None, LockMode.None);
                }
            });
        }

        private void runWithPessimisticWriteLevel() {
            pessimisticWritesBlock.atomic(new AtomicVoidClosure() {
                @Override
                public void execute(Transaction tx) throws Exception {
                    GammaTransaction btx = (GammaTransaction) tx;
                    run(btx, LockMode.None, LockMode.None);
                }
            });
        }

        private void runWithSerializedIsolation() {
            serializedBlock.atomic(new AtomicVoidClosure() {
                @Override
                public void execute(Transaction tx) throws Exception {
                    GammaTransaction btx = (GammaTransaction) tx;
                    run(btx, LockMode.None, LockMode.None);
                }
            });
        }

        private void runWithSnapshotIsolation() {
            snapshotBlock.atomic(new AtomicVoidClosure() {
                @Override
                public void execute(Transaction tx) throws Exception {
                    GammaTransaction btx = (GammaTransaction) tx;
                    run(btx, LockMode.None, LockMode.None);
                }
            });
        }

        private void runWithPessimisticReads() {
            snapshotBlock.atomic(new AtomicVoidClosure() {
                @Override
                public void execute(Transaction tx) throws Exception {
                    GammaTransaction btx = (GammaTransaction) tx;
                    run(btx, LockMode.Read, LockMode.None);
                }
            });
        }

        private void runWithPessimisticWrites() {
            snapshotBlock.atomic(new AtomicVoidClosure() {
                @Override
                public void execute(Transaction tx) throws Exception {
                    GammaTransaction btx = (GammaTransaction) tx;
                    run(btx, LockMode.None, LockMode.Read);
                }
            });
        }

        public void run(GammaTransaction tx, LockMode readLockMode, LockMode writeLockMode) {
            int amount = randomInt(100);

            Customer from = random(customer1, customer2);
            Customer to = random(customer1, customer2);

            long sum = from.account1.openForRead(tx, readLockMode.asInt()).long_value
                    + from.account2.openForRead(tx, readLockMode.asInt()).long_value;

            if (sum < 0) {
                if (writeSkewEncountered.compareAndSet(false, true)) {
                    System.out.println("writeskew detected");
                }
            }

            sleepRandomMs(5);

            if (sum >= amount) {
                GammaLongRef fromAccount = from.getRandomAccount();
                fromAccount.openForWrite(tx, writeLockMode.asInt()).long_value -= amount;

                GammaLongRef toAccount = to.getRandomAccount();
                toAccount.openForWrite(tx, writeLockMode.asInt()).long_value += amount;
            }

            sleepRandomMs(5);
        }
    }

    public Customer random(Customer customer1, Customer customer2) {
        return randomBoolean() ? customer1 : customer2;
    }

    public class Customer {
        private GammaLongRef account1 = new GammaLongRef(stm);
        private GammaLongRef account2 = new GammaLongRef(stm);

        public GammaLongRef getRandomAccount() {
            return randomBoolean() ? account1 : account2;
        }

        public String toString() {
            return format("User(account1 = %s, account2 = %s, sum=%s)",
                    account1.atomicToString(), account2.atomicToString(), account1.atomicGet() + account2.atomicGet());
        }
    }
}
