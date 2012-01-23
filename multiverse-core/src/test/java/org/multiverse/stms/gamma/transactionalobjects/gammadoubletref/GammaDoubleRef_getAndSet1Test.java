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
public class GammaDoubleRef_getAndSet1Test {
    private final GammaTransactionFactory transactionFactory;
    private final GammaStm stm;

    public GammaDoubleRef_getAndSet1Test(GammaTransactionFactory transactionFactory) {
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
    public void whenActiveTransactionAvailable() {
        double initialValue = 10;
        GammaDoubleRef ref = new GammaDoubleRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTransaction tx = transactionFactory.newTransaction();
        setThreadLocalTransaction(tx);
        double value = ref.getAndSet(initialValue + 2);
        tx.commit();

        assertEqualsDouble(initialValue, value);
        assertIsCommitted(tx);
        assertSame(tx, getThreadLocalTransaction());
        assertVersionAndValue(ref, initialVersion + 1, initialValue + 2);
    }

    @Test
    public void whenNoChange() {
        double initialValue = 10;
        GammaDoubleRef ref = new GammaDoubleRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTransaction tx = transactionFactory.newTransaction();
        setThreadLocalTransaction(tx);
        double value = ref.getAndSet(initialValue);
        tx.commit();

        assertEqualsDouble(initialValue, value);
        assertIsCommitted(tx);
        assertSame(tx, getThreadLocalTransaction());
        assertSurplus(ref, 0);
        assertVersionAndValue(ref, initialVersion, initialValue);
    }

    @Test
    public void whenEnsuredBySelf() {
        GammaDoubleRef ref = new GammaDoubleRef(stm, 10);
        long version = ref.getVersion();

        GammaTransaction tx = transactionFactory.newTransaction();
        setThreadLocalTransaction(tx);

        ref.getLock().acquire(LockMode.Write);
        double result = ref.getAndSet(20);

        assertEqualsDouble(10, result);
        assertIsActive(tx);
        assertRefHasWriteLock(ref, tx);
        assertSurplus(ref, 1);
        assertSame(tx, getThreadLocalTransaction());
        assertVersionAndValue(ref, version, 10);
    }

    @Test
    public void whenPrivatizedBySelf() {
        GammaDoubleRef ref = new GammaDoubleRef(stm, 10);
        long version = ref.getVersion();

        GammaTransaction tx = transactionFactory.newTransaction();
        setThreadLocalTransaction(tx);

        ref.getLock().acquire(LockMode.Exclusive);
        double result = ref.getAndSet(20);

        assertEqualsDouble(10, result);
        assertIsActive(tx);
        assertRefHasExclusiveLock(ref, tx);
        assertSurplus(ref, 1);
        assertSame(tx, getThreadLocalTransaction());
        assertVersionAndValue(ref, version, 10);
    }

    @Test
    public void whenEnsuredByOther_thenGetAndSetSucceedsButCommitFails() {
        GammaDoubleRef ref = new GammaDoubleRef(stm, 10);
        long version = ref.getVersion();

        GammaTransaction tx = transactionFactory.newTransaction();
        setThreadLocalTransaction(tx);

        GammaTransaction otherTx = transactionFactory.newTransaction();

        ref.getLock().acquire(otherTx, LockMode.Write);

        double result = ref.getAndSet(20);
        assertEqualsDouble(10, result);
        assertIsActive(tx);
        assertRefHasWriteLock(ref, otherTx);
        assertSurplus(ref, 1);
        assertIsActive(otherTx);
        assertSame(tx, getThreadLocalTransaction());
        assertVersionAndValue(ref, version, 10);

        try {
            tx.commit();
            fail();
        } catch (ReadWriteConflict expected) {
        }

        assertIsAborted(tx);
        assertIsActive(otherTx);
        assertRefHasWriteLock(ref, otherTx);
        assertSurplus(ref, 1);
        assertSame(tx, getThreadLocalTransaction());
        assertVersionAndValue(ref, version, 10);
    }

    @Test
    public void whenPrivatizedByOther_thenReadConflict() {
        GammaDoubleRef ref = new GammaDoubleRef(stm, 10);
        long version = ref.getVersion();

        GammaTransaction tx = transactionFactory.newTransaction();
        setThreadLocalTransaction(tx);

        GammaTransaction otherTx = transactionFactory.newTransaction();
        ref.getLock().acquire(otherTx, LockMode.Exclusive);

        try {
            ref.getAndSet(20);
            fail();
        } catch (ReadWriteConflict expected) {
        }

        assertIsAborted(tx);
        assertRefHasExclusiveLock(ref, otherTx);
        assertSurplus(ref, 1);
        assertIsActive(otherTx);
        assertSame(tx, getThreadLocalTransaction());
        assertVersionAndValue(ref, version, 10);
    }

    @Test
    public void whenListenersAvailable() {
        double initialValue = 10;
        GammaDoubleRef ref = new GammaDoubleRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        long newValue = 20;
        GammaDoubleRefAwaitThread thread = new GammaDoubleRefAwaitThread(ref, newValue);
        thread.start();

        sleepMs(500);

        GammaTransaction tx = transactionFactory.newTransaction();
        setThreadLocalTransaction(tx);
        double result = ref.getAndSet(newValue);
        tx.commit();

        joinAll(thread);

        assertEqualsDouble(initialValue, result);
        assertRefHasNoLocks(ref);
        assertVersionAndValue(ref, initialVersion + 1, newValue);
    }

    @Test
    public void whenNoTransactionAvailable_thenNoTransactionFoundException() {
        double initialValue = 10;
        GammaDoubleRef ref = new GammaDoubleRef(stm, 10);
        long initialVersion = ref.getVersion();

        long newValue = 20;
        try {
            ref.getAndSet(newValue);
            fail();
        } catch (TransactionMandatoryException expected) {
        }

        assertSurplus(ref, 0);
        assertRefHasNoLocks(ref);
        assertVersionAndValue(ref, initialVersion, initialValue);
    }

    @Test
    public void whenPreparedTransactionAvailable_thenPreparedTransactionException() {
        double initialValue = 10;
        GammaDoubleRef ref = new GammaDoubleRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTransaction tx = transactionFactory.newTransaction();
        tx.prepare();
        setThreadLocalTransaction(tx);

        try {
            ref.getAndSet(30);
            fail();
        } catch (PreparedTransactionException expected) {

        }

        assertIsAborted(tx);
        assertVersionAndValue(ref, initialVersion, initialValue);
    }

    @Test
    public void whenCommittedTransactionAvailable_thenDeadTransactionException() {
        double initialValue = 10;
        GammaDoubleRef ref = new GammaDoubleRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTransaction tx = transactionFactory.newTransaction();
        tx.commit();
        setThreadLocalTransaction(tx);

        try {
            ref.getAndSet(initialValue + 1);
            fail();
        } catch (DeadTransactionException expected) {

        }

        assertIsCommitted(tx);
        assertVersionAndValue(ref, initialVersion, initialValue);
    }

    @Test
    public void whenAbortedTransactionAvailable_thenDeadTransactionException() {
        double initialValue = 10;
        GammaDoubleRef ref = new GammaDoubleRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTransaction tx = transactionFactory.newTransaction();
        tx.abort();
        setThreadLocalTransaction(tx);

        try {
            ref.getAndSet(initialValue + 1);
            fail();
        } catch (DeadTransactionException expected) {

        }

        assertIsAborted(tx);
        assertVersionAndValue(ref, initialVersion, initialValue);
    }
}
