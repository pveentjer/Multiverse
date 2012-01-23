package org.multiverse.stms.gamma.transactionalobjects.gammaintref;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.multiverse.api.LockMode;
import org.multiverse.api.TransactionFactory;
import org.multiverse.api.exceptions.*;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactionalobjects.GammaIntRef;
import org.multiverse.stms.gamma.transactions.GammaTransaction;
import org.multiverse.stms.gamma.transactions.GammaTransactionFactory;
import org.multiverse.stms.gamma.transactions.fat.FatFixedLengthGammaTransactionFactory;
import org.multiverse.stms.gamma.transactions.fat.FatMonoGammaTransactionFactory;
import org.multiverse.stms.gamma.transactions.fat.FatVariableLengthGammaTransactionFactory;

import java.util.Collection;

import static java.util.Arrays.asList;
import static org.junit.Assert.fail;
import static org.multiverse.TestUtils.*;
import static org.multiverse.api.ThreadLocalTransaction.clearThreadLocalTransaction;
import static org.multiverse.api.ThreadLocalTransaction.setThreadLocalTransaction;
import static org.multiverse.stms.gamma.GammaTestUtils.*;

@RunWith(Parameterized.class)
public class GammaIntRef_increment0Test {
    private final GammaTransactionFactory transactionFactory;
    private final GammaStm stm;

    public GammaIntRef_increment0Test(GammaTransactionFactory transactionFactory) {
        this.transactionFactory = transactionFactory;
        this.stm = transactionFactory.getConfiguration().getStm();
    }

    @Before
    public void setUp() {
        clearThreadLocalTransaction();
    }

    @Parameterized.Parameters
    public static Collection<TransactionFactory[]> configs() {
        return asList(
                new TransactionFactory[]{new FatVariableLengthGammaTransactionFactory(new GammaStm())},
                new TransactionFactory[]{new FatFixedLengthGammaTransactionFactory(new GammaStm())},
                new TransactionFactory[]{new FatMonoGammaTransactionFactory(new GammaStm())}
        );
    }

    @Test
    public void whenSuccess() {
        int initialValue = 10;
        GammaIntRef ref = new GammaIntRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTransaction tx = transactionFactory.newTransaction();
        setThreadLocalTransaction(tx);

        ref.increment();

        tx.commit();

        assertIsCommitted(tx);
        assertVersionAndValue(ref, initialVersion + 1, initialValue + 1);
    }

    @Test
    public void whenReadonlyTransaction_thenReadonlyException() {
        int initialValue = 10;
        GammaIntRef ref = new GammaIntRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTransaction tx = stm.newTransactionFactoryBuilder()
                .setReadonly(true)
                .setSpeculative(false)
                .newTransactionFactory()
                .newTransaction();
        setThreadLocalTransaction(tx);

        try {
            ref.increment();
            fail();
        } catch (ReadonlyException expected) {
        }

        assertIsAborted(tx);
        assertVersionAndValue(ref, initialVersion, initialValue);
    }

    @Test
    public void whenReadLockAcquiredByOther_thenIncrementSucceedsButCommitFails() {
        int initialValue = 10;
        GammaIntRef ref = new GammaIntRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTransaction otherTx = transactionFactory.newTransaction();
        ref.getLock().acquire(otherTx, LockMode.Read);

        GammaTransaction tx = transactionFactory.newTransaction();
        setThreadLocalTransaction(tx);

        ref.increment();

        try {
            tx.commit();
            fail();
        } catch (ReadWriteConflict expected) {
        }

        assertIsAborted(tx);
        assertVersionAndValue(ref, initialVersion, initialValue);
        assertRefHasReadLock(ref, otherTx);
        assertReadLockCount(ref, 1);
    }

    @Test
    public void whenWriteLockAcquiredByOther_thenIncrementSucceedsButCommitFails() {
        int initialValue = 10;
        GammaIntRef ref = new GammaIntRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTransaction otherTx = transactionFactory.newTransaction();
        ref.getLock().acquire(otherTx, LockMode.Write);

        GammaTransaction tx = transactionFactory.newTransaction();
        setThreadLocalTransaction(tx);

        ref.increment();

        try {
            tx.commit();
            fail();
        } catch (ReadWriteConflict expected) {
        }

        assertIsAborted(tx);
        assertVersionAndValue(ref, initialVersion, initialValue);
        assertRefHasWriteLock(ref, otherTx);
    }

    @Test
    public void whenExclusiveLockAcquiredByOther_thenIncrementSucceedsButCommitFails() {
        int initialValue = 10;
        GammaIntRef ref = new GammaIntRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTransaction otherTx = transactionFactory.newTransaction();
        ref.getLock().acquire(otherTx, LockMode.Exclusive);

        GammaTransaction tx = transactionFactory.newTransaction();
        setThreadLocalTransaction(tx);

        ref.increment();

        try {
            tx.commit();
            fail();
        } catch (ReadWriteConflict expected) {
        }

        assertIsAborted(tx);
        assertVersionAndValue(ref, initialVersion, initialValue);
        assertRefHasExclusiveLock(ref, otherTx);
    }


    @Test
    public void whenCommittedTransactionFound() {
        int initialValue = 10;
        GammaIntRef ref = new GammaIntRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTransaction tx = transactionFactory.newTransaction();
        setThreadLocalTransaction(tx);
        tx.commit();

        try {
            ref.increment();
            fail();
        } catch (DeadTransactionException expected) {
        }

        assertIsCommitted(tx);
        assertVersionAndValue(ref, initialVersion, initialValue);
    }

    @Test
    public void whenAbortedTransactionFound_thenDeadTransactionException() {
        int initialValue = 10;
        GammaIntRef ref = new GammaIntRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTransaction tx = transactionFactory.newTransaction();
        setThreadLocalTransaction(tx);
        tx.abort();

        try {
            ref.increment();
            fail();
        } catch (DeadTransactionException expected) {
        }

        assertIsAborted(tx);
        assertVersionAndValue(ref, initialVersion, initialValue);
    }

    @Test
    public void whenPreparedTransactionFound_thenPreparedTransactionException() {
        int initialValue = 10;
        GammaIntRef ref = new GammaIntRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTransaction tx = transactionFactory.newTransaction();
        setThreadLocalTransaction(tx);
        tx.prepare();

        try {
            ref.increment();
            fail();
        } catch (PreparedTransactionException expected) {
        }

        assertIsAborted(tx);
        assertVersionAndValue(ref, initialVersion, initialValue);
    }

    @Test
    public void whenNoTransaction_thenTransactionMandatoryException() {
        int initialValue = 10;
        GammaIntRef ref = new GammaIntRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        try {
            ref.increment();
            fail();
        } catch (TransactionMandatoryException expected) {
        }

        assertVersionAndValue(ref, initialVersion, initialValue);
    }

    @Test
    public void whenListenersAvailable() {
        int initialValue = 10;
        GammaIntRef ref = new GammaIntRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        IntRefAwaitThread thread = new IntRefAwaitThread(ref, initialValue + 1);
        thread.start();

        sleepMs(500);

        GammaTransaction tx = transactionFactory.newTransaction();
        setThreadLocalTransaction(tx);
        ref.increment();
        tx.commit();

        joinAll(thread);

        assertVersionAndValue(ref, initialVersion + 1, initialValue + 1);
    }
}
