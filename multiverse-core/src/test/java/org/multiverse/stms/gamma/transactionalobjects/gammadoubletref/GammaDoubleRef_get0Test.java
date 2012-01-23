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
public class GammaDoubleRef_get0Test {

    private final GammaTransactionFactory transactionFactory;
    private final GammaStm stm;

    public GammaDoubleRef_get0Test(GammaTransactionFactory transactionFactory) {
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
        GammaDoubleRef ref = new GammaDoubleRef(stm, 10);

        GammaTransaction tx = transactionFactory.newTransaction();
        tx.prepare();
        setThreadLocalTransaction(tx);

        try {
            ref.get();
            fail();
        } catch (PreparedTransactionException expected) {

        }
    }

    @Test
    public void whenPrivatizedBySelf_thenSuccess() {
        GammaDoubleRef ref = new GammaDoubleRef(stm, 100);
        long version = ref.getVersion();

        GammaTransaction tx = transactionFactory.newTransaction();
        setThreadLocalTransaction(tx);

        ref.getLock().acquire(LockMode.Exclusive);

        double value = ref.get();

        assertEqualsDouble(100, value);
        assertRefHasExclusiveLock(ref, tx);
        assertSurplus(ref, 1);
        assertWriteBiased(ref);
        assertIsActive(tx);
        assertSame(tx, getThreadLocalTransaction());
        assertEquals(version, ref.getVersion());
        assertEqualsDouble(100, ref.atomicWeakGet());
    }

    @Test
    public void whenEnsuredBySelf() {
        GammaDoubleRef ref = new GammaDoubleRef(stm, 100);
        long version = ref.getVersion();

        GammaTransaction tx = transactionFactory.newTransaction();
        setThreadLocalTransaction(tx);

        ref.getLock().acquire(LockMode.Write);
        double value = ref.get();

        assertEqualsDouble(100, value);
        assertRefHasWriteLock(ref, tx);
        assertSurplus(ref, 1);
        assertWriteBiased(ref);
        assertIsActive(tx);
        assertSame(tx, getThreadLocalTransaction());
        assertVersionAndValue(ref, version, 100);
    }

    @Test
    public void whenPrivatizedByOther_thenReadConflict() {
        GammaDoubleRef ref = new GammaDoubleRef(stm, 100);
        long version = ref.getVersion();

        GammaTransaction tx = transactionFactory.newTransaction();
        setThreadLocalTransaction(tx);

        GammaTransaction otherTx = transactionFactory.newTransaction();
        ref.getLock().acquire(otherTx, LockMode.Exclusive);

        try {
            ref.get();
            fail();
        } catch (ReadWriteConflict expected) {
        }

        assertRefHasExclusiveLock(ref, otherTx);
        assertSurplus(ref, 1);
        assertWriteBiased(ref);
        assertIsActive(otherTx);
        assertIsAborted(tx);
        assertSame(tx, getThreadLocalTransaction());
        assertVersionAndValue(ref, version, 100);
    }

    @Test
    public void whenEnsuredByother_thenReadStillPossible() {
        GammaDoubleRef ref = new GammaDoubleRef(stm, 100);
        long version = ref.getVersion();

        GammaTransaction tx = transactionFactory.newTransaction();
        setThreadLocalTransaction(tx);

        GammaTransaction otherTx = transactionFactory.newTransaction();
        ref.getLock().acquire(otherTx, LockMode.Write);

        double value = ref.get();

        assertEqualsDouble(100, value);
        assertRefHasWriteLock(ref, otherTx);
        assertSurplus(ref, 1);
        assertWriteBiased(ref);
        assertIsActive(otherTx);
        assertIsActive(tx);
        assertSame(tx, getThreadLocalTransaction());
        assertVersionAndValue(ref, version, 100);
    }

    @Test
    public void whenActiveTransactionAvailable_thenPreparedTransactionException() {
        GammaDoubleRef ref = new GammaDoubleRef(stm, 10);

        GammaTransaction tx = transactionFactory.newTransaction();
        setThreadLocalTransaction(tx);

        double value = ref.get();

        assertEqualsDouble(10, value);
        assertIsActive(tx);
        assertSame(tx, getThreadLocalTransaction());
    }

    @Test
    public void whenNoTransactionAvailable_thenNoTransactionFoundException() {
        double initialValue = 10;
        GammaDoubleRef ref = new GammaDoubleRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        try {
            ref.get();
            fail();
        } catch (TransactionMandatoryException expected) {

        }

        assertNull(getThreadLocalTransaction());
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
            ref.get();
            fail();
        } catch (DeadTransactionException expected) {

        }

        assertVersionAndValue(ref, initialVersion, initialValue);
        assertIsCommitted(tx);
        assertSame(tx, getThreadLocalTransaction());
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
            ref.get();
            fail();
        } catch (DeadTransactionException expected) {

        }

        assertIsAborted(tx);
        assertSame(tx, getThreadLocalTransaction());
        assertVersionAndValue(ref, initialVersion, initialValue);
    }
}
