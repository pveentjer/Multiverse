package org.multiverse.stms.gamma.integration.isolation;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.multiverse.TestThread;
import org.multiverse.api.Txn;
import org.multiverse.api.callables.TxnVoidCallable;
import org.multiverse.api.exceptions.ReadWriteConflict;
import org.multiverse.stms.gamma.*;
import org.multiverse.stms.gamma.GammaTxnExecutor;
import org.multiverse.stms.gamma.transactionalobjects.BaseGammaTxnRef;
import org.multiverse.stms.gamma.transactionalobjects.GammaTxnRef;
import org.multiverse.stms.gamma.transactionalobjects.Tranlocal;
import org.multiverse.stms.gamma.transactions.GammaTxn;
import org.multiverse.stms.gamma.transactions.GammaTxnConfig;
import org.multiverse.stms.gamma.transactions.fat.*;
import org.multiverse.stms.gamma.transactions.fat.FatFixedLengthGammaTxn;
import org.multiverse.stms.gamma.transactions.fat.FatVariableLengthGammaTxn;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;
import static org.multiverse.TestUtils.*;

public class Orec_Ref_ReadConsistencyStressTest implements GammaConstants {

    private GammaStm stm;
    private GammaTxnRef[] refs;
    private volatile boolean stop;
    private final AtomicBoolean inconsistencyDetected = new AtomicBoolean();
    private final long durationMs = 360 * 1000;
    private int refCount = 256;
    private int writingThreadCount;
    private int readingThreadCount;

    @Before
    public void setUp() {
        stm = new GammaStm();
        stop = false;
        inconsistencyDetected.set(false);
        readingThreadCount = 10;
        writingThreadCount = 2;
        refs = new GammaTxnRef[refCount];
        for (int k = 0; k < refs.length; k++) {
            refs[k] = new GammaTxnRef(stm, 0);
        }
    }

    @After
    public void after() {
        for (GammaTxnRef ref : refs) {
            System.out.println(ref.toDebugString());
        }
    }

    class UpdatingThread extends TestThread {
        public UpdatingThread(int id) {
            super("UpdatingThread-" + id);
        }

        @Override
        public void doRun() throws Exception {
            GammaTxnExecutor executor = stm.newTxnFactoryBuilder()
                    .setSpeculative(false)
                    .setMaxRetries(100000)
                            //     .setReadLockMode(LockMode.Exclusive)
                    .newTxnExecutor();

            final String name = getName();

            TxnVoidCallable callable = new TxnVoidCallable() {
                @Override
                public void call(Txn tx) throws Exception {
                    for (GammaTxnRef ref : refs) {
                        ref.set(tx, name);
                    }
                }
            };

            int iteration = 0;
            while (!stop) {
                executor.atomic(callable);
                sleepRandomUs(100);
                iteration++;

                if (iteration % 100000 == 0) {
                    System.out.printf("%s is at %s\n", getName(), iteration);
                }
            }
        }
    }

    @Test
    public void basicTest() {
        BasicReadThread[] readingThreads = new BasicReadThread[readingThreadCount];
        for (int k = 0; k < readingThreads.length; k++) {
            readingThreads[k] = new BasicReadThread(k);
        }

        UpdatingThread[] updatingThreads = new UpdatingThread[writingThreadCount];
        for (int k = 0; k < updatingThreads.length; k++) {
            updatingThreads[k] = new UpdatingThread(k);
        }

        startAll(readingThreads);
        startAll(updatingThreads);
        sleepMs(durationMs);
        stop = true;
        sleepMs(1000);
        joinAll(readingThreads);
        joinAll(updatingThreads);
        assertFalse(inconsistencyDetected.get());
    }

    class BasicReadThread extends TestThread {

        private Tranlocal[] tranlocals;
        private long lastConflictCount = stm.getGlobalConflictCounter().count();
        private GammaObjectPool pool = new GammaObjectPool();
        private Tranlocal firstTranlocal;
        private GammaTxn dummyTransaction = stm.newDefaultTxn();

        public BasicReadThread(int id) {
            super("ReadingThread-" + id);

            tranlocals = new Tranlocal[refs.length];
            for (int k = 0; k < tranlocals.length; k++) {
                Tranlocal tranlocal = new Tranlocal();
                tranlocal.owner = refs[k];
                tranlocals[k] = tranlocal;
            }
        }

        @Override
        public void doRun() throws Exception {
            long iteration = 0;
            while (!stop) {
                singleRun();
                iteration++;

                if (iteration % 10000 == 0) {
                    System.out.printf("%s is at %s\n", getName(), iteration);
                }
            }
        }

        private void singleRun() {
            assertCorrectlyCleared();
            fullRead();
            assertReadConsistent();
            releaseChainAfterSuccess();
        }

        private void fullRead() {
            for (; ;) {
                lastConflictCount = stm.getGlobalConflictCounter().count();
                Object v = null;
                for (int k = 0; k < refs.length; k++) {
                    GammaTxnRef ref = refs[k];
                    Tranlocal tranlocal = tranlocals[k];
                    if (!ref.load(dummyTransaction, tranlocal, LOCKMODE_NONE, 64, true)) {
                        releaseChainAfterFailure();
                        break;
                    }

                    if (!isReadConsistent()) {
                        releaseChainAfterFailure();
                        break;
                    }

                    if (k == 0) {
                        v = tranlocal.ref_value;
                    } else {
                        if (v != tranlocal.ref_value) {
                            System.out.println("Inconsistency detected");
                            stop = true;
                            fail();
                        }
                    }

                    if (k == refs.length - 1) {
                        return;
                    }
                }
            }
        }

        private void assertReadConsistent() {
            firstTranlocal = tranlocals[0];

            for (int k = 1; k < tranlocals.length; k++) {
                boolean badValue = tranlocals[k].ref_value != firstTranlocal.ref_value;
                boolean badVersion = tranlocals[k].version != firstTranlocal.version;

                if (badValue || badVersion) {
                    if (badValue) {
                        System.out.printf("Inconsistency detected on bad value %s %s\n", tranlocals[k].ref_value, firstTranlocal.ref_value);
                    } else {
                        System.out.printf("Inconsistency detected on bad version %s %s\n", tranlocals[k].version, firstTranlocal.version);
                    }
                    inconsistencyDetected.compareAndSet(false, true);
                    stop = true;
                    break;
                }
            }
        }

        private boolean isReadConsistent() {
            long globalConflictCount = stm.getGlobalConflictCounter().count();

            if (lastConflictCount == globalConflictCount) {
                return true;
            }

            lastConflictCount = globalConflictCount;

            for (Tranlocal tranlocal : tranlocals) {
                BaseGammaTxnRef owner = tranlocal.owner;

                if (!tranlocal.hasDepartObligation) {
                    continue;
                }

                if (owner.hasReadConflict(tranlocal)) {
                    return false;
                }

                //if (owner.hasExclusiveLock()) {
                //    return false;
                //}

                //if (tranlocal.version != owner.version) {
                //    return false;
                //}
            }

            return true;
        }

        private void assertCorrectlyCleared() {
            for (Tranlocal tranlocal : tranlocals) {
                assertFalse(tranlocal.hasDepartObligation);
                assertEquals(LOCKMODE_NONE, tranlocal.lockMode);
            }
        }

        private void releaseChainAfterFailure() {
            for (Tranlocal tranlocal : tranlocals) {
                BaseGammaTxnRef owner = tranlocal.owner;
                tranlocal.owner.releaseAfterFailure(tranlocal, pool);
                tranlocal.owner = owner;
                //if (tranlocal.hasDepartObligation) {
                //    tranlocal.hasDepartObligation = false;
                //    if (tranlocal.lockMode == LOCKMODE_NONE) {
                //        tranlocal.owner.departAfterFailure();
                //    } else {
                //        tranlocal.lockMode = LOCKMODE_NONE;
                //        tranlocal.owner.departAfterFailureAndUnlock();
                //    }
                //}
            }
        }

        private void releaseChainAfterSuccess() {
            for (Tranlocal tranlocal : tranlocals) {
                BaseGammaTxnRef owner = tranlocal.owner;
                owner.releaseAfterReading(tranlocal, pool);
                tranlocal.owner = owner;

                //if (tranlocal.hasDepartObligation) {
                //    tranlocal.hasDepartObligation = false;
                //    if (tranlocal.lockMode == LOCKMODE_NONE) {
                //        tranlocal.owner.departAfterReading();
                //    } else {
                //        tranlocal.lockMode = LOCKMODE_NONE;
                //        tranlocal.owner.departAfterUpdateAndUnlock();
                //    }
                //}
            }
        }
    }

    @Test
    public void testFixedLengthTransactionUsingReadThread() {
        FixedLengthTransactionUsingReadThread[] readingThreads = new FixedLengthTransactionUsingReadThread[readingThreadCount];
        for (int k = 0; k < readingThreads.length; k++) {
            readingThreads[k] = new FixedLengthTransactionUsingReadThread(k);
        }

        UpdatingThread[] updatingThreads = new UpdatingThread[writingThreadCount];
        for (int k = 0; k < updatingThreads.length; k++) {
            updatingThreads[k] = new UpdatingThread(k);
        }

        startAll(readingThreads);
        startAll(updatingThreads);

        sleepMs(durationMs);
        stop = true;
        sleepMs(1000);
        for (GammaTxnRef ref : refs) {
            System.out.println(ref.toDebugString());
        }

        joinAll(readingThreads);
        joinAll(updatingThreads);
        assertFalse(inconsistencyDetected.get());
    }

    class FixedLengthTransactionUsingReadThread extends TestThread {

        public final FatFixedLengthGammaTxn tx = new FatFixedLengthGammaTxn(
                new GammaTxnConfig(stm, refs.length + 1)
                        .setMaxRetries(10000000)
                        .setMaximumPoorMansConflictScanLength(0)
                        .setDirtyCheckEnabled(false)
                        .setSpeculative(false)
        );

        public FixedLengthTransactionUsingReadThread(int id) {
            super("ReadingThread-" + id);
        }

        @Override
        public void doRun() throws Exception {
            long iteration = 0;
            while (!stop) {
                singleRun();
                iteration++;

                if (iteration % 10000 == 0) {
                    System.out.printf("%s is at %s\n", getName(), iteration);
                }
            }
        }

        private void singleRun() {
            tx.hardReset();
            while (true) {
                try {
                    fullRead();
                    assertReadConsistent(tx);
                    tx.commit();
                    return;
                } catch (ReadWriteConflict expected) {
                    tx.hardReset();
                }
            }
        }

        private void fullRead() {
            for (int k = 0; k < refs.length; k++) {
                GammaTxnRef ref = refs[k];
                Tranlocal tranlocal = tx.head;

                while (tranlocal.owner != null) {
                    tranlocal = tranlocal.next;
                }
                tx.size++;

                if (!tx.hasReads) {
                    tx.localConflictCount = stm.globalConflictCounter.count();
                    tx.hasReads = true;
                }

                if (!ref.load(tx, tranlocal, LOCKMODE_NONE, 64, true)) {
                    throw tx.abortOnReadWriteConflict(ref);
                }

                if (!tx.isReadConsistent(tranlocal)) {
                    throw tx.abortOnReadWriteConflict(ref);
                }

                //ref.openForRead(tx, LOCKMODE_NONE);

                if (k == refs.length - 1) {
                    return;
                }
            }
        }
    }

    class FixedLengthTransactionReadingThread extends TestThread {

        private FatFixedLengthGammaTxn tx = new FatFixedLengthGammaTxn(
                new GammaTxnConfig(stm, refs.length)
                        .setMaximumPoorMansConflictScanLength(0)
                        .setDirtyCheckEnabled(false)
        );

        public FixedLengthTransactionReadingThread(int id) {
            super("ReadingThread-" + id);
        }

        @Override
        public void doRun() throws Exception {
            long iteration = 0;
            while (!stop) {
                singleRun(System.nanoTime() % 10 == 0 && false);
                iteration++;

                if (iteration % 1000 == 0) {
                    System.out.printf("%s is at %s\n", getName(), iteration);
                }
            }
        }

        private void singleRun(boolean write) {
            tx.hardReset();
            boolean success = false;
            while (!success) {
                fullRead(write);
                assertReadConsistent(tx);
                try {
                    tx.commit();
                    success = true;
                } catch (ReadWriteConflict expected) {
                    success = false;
                    tx.attempt = 1;
                    tx.softReset();
                }
            }

            tx.commit();
        }

        private void fullRead(boolean write) {
            for (; ;) {
                try {
                    for (int k = 0; k < refs.length; k++) {
                        if (write) {
                            Tranlocal tranlocal = refs[k].openForWrite(tx, LOCKMODE_NONE);
                            tranlocal.ref_value = getName();
                        } else {
                            refs[k].openForRead(tx, LOCKMODE_NONE);
                        }
                        if (k == refs.length - 1) {
                            return;
                        }
                    }
                } catch (ReadWriteConflict expected) {
                    tx.attempt = 1;
                    tx.softReset();
                }
            }

        }
    }

    class VariableLengthReadingThread extends TestThread {

        private FatVariableLengthGammaTxn tx = new FatVariableLengthGammaTxn(
                new GammaTxnConfig(stm, refs.length)
                        .setMaximumPoorMansConflictScanLength(0)
                        .setDirtyCheckEnabled(false)
        );

        public VariableLengthReadingThread(int id) {
            super("ReadingThread-" + id);
        }

        @Override
        public void doRun() throws Exception {
            long iteration = 0;
            while (!stop) {
                singleRun(System.nanoTime() % 10 == 0 && false);
                iteration++;

                if (iteration % 1000 == 0) {
                    System.out.printf("%s is at %s\n", getName(), iteration);
                }
            }
        }

        private void singleRun(boolean write) {
            tx.hardReset();
            boolean success = false;
            while (!success) {
                fullRead(write);
                assertReadConsistent(tx);
                try {
                    tx.commit();
                    success = true;
                } catch (ReadWriteConflict expected) {
                    success = false;
                    tx.attempt = 1;
                    tx.softReset();
                }
            }

            tx.commit();
        }

        private void fullRead(boolean write) {
            for (; ;) {
                try {
                    for (int k = 0; k < refs.length; k++) {
                        if (write) {
                            Tranlocal tranlocal = refs[k].openForWrite(tx, LOCKMODE_NONE);
                            tranlocal.ref_value = getName();
                        } else {
                            refs[k].openForRead(tx, LOCKMODE_NONE);
                        }

                        if (k == refs.length - 1) {
                            return;
                        }
                    }
                } catch (ReadWriteConflict expected) {
                    tx.attempt = 1;
                    tx.softReset();
                }
            }
        }
    }

    class VariableReadingWithBlockThread extends TestThread {

        private LeanGammaTxnExecutor executor;

        public VariableReadingWithBlockThread(int id) {
            super("VariableReadingWithBlockThread-" + id);
        }

        @Override
        public void doRun() throws Exception {
            GammaTxnConfig config = new GammaTxnConfig(stm, refs.length)
                    .setMaximumPoorMansConflictScanLength(0)
                    .setMaxRetries(100000)
                    .setSpeculative(false)
                    .setDirtyCheckEnabled(false);

            executor = new LeanGammaTxnExecutor(new FatVariableLengthGammaTxnFactory(config));

            long iteration = 0;
            while (!stop) {
                singleRun();
                iteration++;

                if (iteration % 10000 == 0) {
                    System.out.printf("%s is at %s\n", getName(), iteration);
                }
            }
        }

        private void singleRun() {
            executor.atomic(new TxnVoidCallable() {
                @Override
                public void call(Txn tx) throws Exception {
                    fullRead((GammaTxn) tx);
                    assertReadConsistent((GammaTxn) tx);
                }
            });
        }

        private void fullRead(GammaTxn tx) {
            Object value = refs[0].get(tx);
            for (int k = 0; k < refs.length; k++) {
                assertSame(value, refs[k].openForRead(tx, LOCKMODE_NONE).ref_value);
            }
        }
    }


    class FixedReadingWithBlockThread extends TestThread {

        private LeanGammaTxnExecutor executor;
        private GammaTxnConfig config;

        public FixedReadingWithBlockThread(int id) {
            super("VariableReadingWithBlockThread-" + id);
        }

        @Override
        public void doRun() throws Exception {
            config = new GammaTxnConfig(stm, refs.length)
                    .setMaximumPoorMansConflictScanLength(0)
                    .setDirtyCheckEnabled(false);

            executor = new LeanGammaTxnExecutor(new FatFixedLengthGammaTxnFactory(config));

            long iteration = 0;
            while (!stop) {
                singleRun();
                iteration++;

                if (iteration % 10000 == 0) {
                    System.out.printf("%s is at %s\n", getName(), iteration);
                }
            }
        }

        private void singleRun() {
            TxnVoidCallable callable = new TxnVoidCallable() {
                @Override
                public void call(Txn tx) throws Exception {
                    fullRead((GammaTxn) tx);
                    assertReadConsistent((GammaTxn) tx);
                }
            };

            FatFixedLengthGammaTxn tx = new FatFixedLengthGammaTxn(config);
            while (true) {
                try {
                    callable.call(tx);
                    tx.commit();
                    return;
                } catch (ReadWriteConflict expected) {
                    tx.hardReset();
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }

            //executor.atomicChecked(callable);
        }

        private void fullRead(GammaTxn tx) {
            for (int k = 0; k < refs.length; k++) {
                refs[k].openForRead(tx, LOCKMODE_NONE);
            }
        }


    }

    private void assertReadConsistent(GammaTxn tx) {
        long version = tx.getRefTranlocal(refs[0]).version;
        Object value = tx.getRefTranlocal(refs[0]).ref_value;
        for (int k = 1; k < refs.length; k++) {
            boolean badVersion = version != tx.getRefTranlocal(refs[k]).version;
            boolean badValue = value != tx.getRefTranlocal(refs[k]).ref_value;

            if (badValue || badVersion) {
                System.out.printf("Inconsistency detected badValue=%s badVersion=%s\n", badValue, badVersion);
                inconsistencyDetected.compareAndSet(false, true);
                stop = true;
                break;
            }
        }
    }
}
