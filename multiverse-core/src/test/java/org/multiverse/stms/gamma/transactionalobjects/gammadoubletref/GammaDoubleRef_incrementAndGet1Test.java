package org.multiverse.stms.gamma.transactionalobjects.gammadoubletref;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.multiverse.api.LockMode;
import org.multiverse.api.TransactionFactory;
import org.multiverse.api.exceptions.DeadTransactionException;
import org.multiverse.api.exceptions.PreparedTransactionException;
import org.multiverse.api.exceptions.ReadWriteConflict;
import org.multiverse.api.exceptions.TransactionMandatoryException;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactionalobjects.GammaDoubleRef;
import org.multiverse.stms.gamma.transactions.GammaTransaction;
import org.multiverse.stms.gamma.transactions.GammaTransactionFactory;
import org.multiverse.stms.gamma.transactions.fat.FatFixedLengthGammaTransactionFactory;
import org.multiverse.stms.gamma.transactions.fat.FatMonoGammaTransactionFactory;
import org.multiverse.stms.gamma.transactions.fat.FatVariableLengthGammaTransactionFactory;

import java.util.Collection;

import static java.util.Arrays.asList;
import static org.junit.Assert.*;
import static org.multiverse.TestUtils.*;
import static org.multiverse.api.ThreadLocalTransaction.*;
import static org.multiverse.stms.gamma.GammaTestUtils.*;

@RunWith(Parameterized.class)
public class GammaDoubleRef_incrementAndGet1Test {

    private final GammaTransactionFactory transactionFactory;
    private final GammaStm stm;

    public GammaDoubleRef_incrementAndGet1Test(GammaTransactionFactory transactionFactory) {
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
    public void whenPreparedTransactionAvailable_thenPreparedTransactionException() {
        double initialValue = 10;
        GammaDoubleRef ref = new GammaDoubleRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTransaction tx = transactionFactory.newTransaction();
        tx.prepare();
        setThreadLocalTransaction(tx);

        long amount = 30;
        try {
            ref.incrementAndGet(amount);
            fail();
        } catch (PreparedTransactionException expected) {

        }

        assertIsAborted(tx);
        assertVersionAndValue(ref, initialVersion, initialValue);
        assertRefHasNoLocks(ref);
    }

    @Test
    public void whenActiveTransactionAvailable() {
        int initialValue = 10;
        GammaDoubleRef ref = new GammaDoubleRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTransaction tx = transactionFactory.newTransaction();
        setThreadLocalTransaction(tx);
        long amount = 20;
        double value = ref.incrementAndGet(amount);
        tx.commit();

        assertEqualsDouble(initialValue + amount, value);
        assertIsCommitted(tx);
        assertVersionAndValue(ref, initialVersion + 1, initialValue + amount);
        assertRefHasNoLocks(ref);
        assertSame(tx, getThreadLocalTransaction());
    }

    @Test
    public void whenNoTransactionAvailable_thenNoTransactionFoundException() {
        double initialValue = 10;
        GammaDoubleRef ref = new GammaDoubleRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        try {
            ref.incrementAndGet(1);
            fail();
        } catch (TransactionMandatoryException expected) {

        }

        assertVersionAndValue(ref, initialVersion, initialValue);
        assertNull(getThreadLocalTransaction());
        assertSurplus(ref, 0);
        assertRefHasNoLocks(ref);
    }

    @Test
    public void whenCommittedTransactionAvailable_thenExecutedAtomically() {
        double initialValue = 10;
        GammaDoubleRef ref = new GammaDoubleRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTransaction tx = transactionFactory.newTransaction();
        setThreadLocalTransaction(tx);
        tx.commit();

        try {
            ref.incrementAndGet(20);
            fail();
        } catch (DeadTransactionException expected) {

        }

        assertIsCommitted(tx);
        assertVersionAndValue(ref, initialVersion, initialValue);
        assertSame(tx, getThreadLocalTransaction());
        assertSurplus(ref, 0);
        assertRefHasNoLocks(ref);
    }

    @Test
    public void whenAbortedTransactionAvailable_thenDeadTransactionException() {
        double initialValue = 10;
        GammaDoubleRef ref = new GammaDoubleRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTransaction tx = transactionFactory.newTransaction();
        setThreadLocalTransaction(tx);
        tx.abort();

        try {
            ref.incrementAndGet(20);
            fail();
        } catch (DeadTransactionException expected) {
        }

        assertIsAborted(tx);
        assertVersionAndValue(ref, initialVersion, initialValue);
        assertSame(tx, getThreadLocalTransaction());
        assertSurplus(ref, 0);
        assertRefHasNoLocks(ref);
    }

    @Test
    public void whenReadLockAlreadyAcquiredBySelf_thenSuccess() {
        double initialValue = 10;
        GammaDoubleRef ref = new GammaDoubleRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTransaction tx = transactionFactory.newTransaction();
        setThreadLocalTransaction(tx);

        ref.getLock().acquire(LockMode.Read);
        int amount = 1;
        double result = ref.incrementAndGet(amount);

        assertEqualsDouble(initialValue + amount, result);
        assertIsActive(tx);
        assertSame(tx, getThreadLocalTransaction());
        assertSurplus(ref, 1);
        assertRefHasReadLock(ref, tx);
    }

    @Test
    public void whenWriteLockAlreadyAcquiredBySelf_thenSuccess() {
        double initialValue = 10;
        GammaDoubleRef ref = new GammaDoubleRef(stm, initialValue);

        GammaTransaction tx = transactionFactory.newTransaction();
        setThreadLocalTransaction(tx);

        ref.getLock().acquire(LockMode.Write);
        int amount = 1;
        double result = ref.incrementAndGet(amount);

        assertEqualsDouble(initialValue + amount, result);
        assertIsActive(tx);
        assertSame(tx, getThreadLocalTransaction());
        assertSurplus(ref, 1);
        assertRefHasWriteLock(ref, tx);
    }

    @Test
    public void whenExclusiveLockAlreadyAcquiredBySelf_thenSuccess() {
        GammaDoubleRef ref = new GammaDoubleRef(stm, 10);

        GammaTransaction tx = transactionFactory.newTransaction();
        setThreadLocalTransaction(tx);

        ref.getLock().acquire(LockMode.Exclusive);
        double result = ref.incrementAndGet(1);

        assertEqualsDouble(11, result);
        assertIsActive(tx);
        assertSame(tx, getThreadLocalTransaction());
        assertSurplus(ref, 1);
        assertRefHasExclusiveLock(ref, tx);
    }

    @Test
    public void whenExclusiveLockAcquiredByOther_thenReadConflict() {
        GammaDoubleRef ref = new GammaDoubleRef(stm, 10);
        long version = ref.getVersion();

        GammaTransaction tx = transactionFactory.newTransaction();
        setThreadLocalTransaction(tx);

        GammaTransaction otherTx = transactionFactory.newTransaction();

        ref.getLock().acquire(otherTx, LockMode.Exclusive);

        try {
            ref.incrementAndGet(1);
            fail();
        } catch (ReadWriteConflict expected) {

        }

        assertIsAborted(tx);
        assertSame(tx, getThreadLocalTransaction());
        assertSurplus(ref, 1);
        assertRefHasExclusiveLock(ref, otherTx);
        assertVersionAndValue(ref, version, 10);
    }

    @Test
    public void whenWriteLockAcquiredByOther_thenIncrementSucceedsButCommitFails() {
        GammaDoubleRef ref = new GammaDoubleRef(stm, 10);
        long version = ref.getVersion();

        GammaTransaction tx = transactionFactory.newTransaction();
        setThreadLocalTransaction(tx);

        GammaTransaction otherTx = transactionFactory.newTransaction();

        ref.getLock().acquire(otherTx, LockMode.Write);

        double result = ref.incrementAndGet(1);
        assertEqualsDouble(11, result);

        assertIsActive(tx);
        assertSame(tx, getThreadLocalTransaction());
        assertSurplus(ref, 1);
        assertRefHasWriteLock(ref, otherTx);
        assertSame(version, ref.getVersion());
        assertEqualsDouble(10, ref.atomicWeakGet());

        try {
            tx.commit();
            fail();
        } catch (ReadWriteConflict expected) {
        }

        assertIsAborted(tx);
        assertSame(tx, getThreadLocalTransaction());
        assertSurplus(ref, 1);
        assertRefHasWriteLock(ref, otherTx);
        assertSame(version, ref.getVersion());
        assertEqualsDouble(10, ref.atomicWeakGet());
    }

    @Test
    public void whenListenersAvailable() {
        double initialValue = 10;
        GammaDoubleRef ref = new GammaDoubleRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        long amount = 4;
        GammaDoubleRefAwaitThread thread = new GammaDoubleRefAwaitThread(ref, initialValue + amount);
        thread.start();

        sleepMs(500);

        GammaTransaction tx = transactionFactory.newTransaction();
        setThreadLocalTransaction(tx);
        double result = ref.incrementAndGet(amount);
        tx.commit();

        joinAll(thread);

        assertEqualsDouble(initialValue + amount, result);
        assertRefHasNoLocks(ref);
        assertVersionAndValue(ref, initialVersion + 1, initialValue + amount);
    }
}
