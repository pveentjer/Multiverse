package org.multiverse.stms.gamma.transactionalobjects.gammaintref;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.multiverse.api.TransactionFactory;
import org.multiverse.api.exceptions.DeadTransactionException;
import org.multiverse.api.exceptions.PreparedTransactionException;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactionalobjects.GammaIntRef;
import org.multiverse.stms.gamma.transactions.GammaTransaction;
import org.multiverse.stms.gamma.transactions.GammaTransactionFactory;
import org.multiverse.stms.gamma.transactions.fat.FatFixedLengthGammaTransactionFactory;
import org.multiverse.stms.gamma.transactions.fat.FatMonoGammaTransactionFactory;
import org.multiverse.stms.gamma.transactions.fat.FatVariableLengthGammaTransactionFactory;

import java.util.Collection;

import static java.util.Arrays.asList;
import static org.junit.Assert.*;
import static org.multiverse.TestUtils.*;
import static org.multiverse.api.ThreadLocalTransaction.clearThreadLocalTransaction;
import static org.multiverse.api.ThreadLocalTransaction.getThreadLocalTransaction;
import static org.multiverse.stms.gamma.GammaTestUtils.*;

@RunWith(Parameterized.class)
public class GammaIntRef_getAndSet2Test {
    private final GammaTransactionFactory transactionFactory;
    private final GammaStm stm;

    public GammaIntRef_getAndSet2Test(GammaTransactionFactory transactionFactory) {
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
    public void whenNullTransaction() {
        GammaIntRef ref = new GammaIntRef(stm, 10);
        long version = ref.getVersion();

        try {
            ref.getAndSet(null, 11);
            fail();
        } catch (NullPointerException expected) {
        }

        assertVersionAndValue(ref, version, 10);
    }

    @Test
    public void whenPreparedTransaction_thenPreparedTransactionException() {
        GammaIntRef ref = new GammaIntRef(stm, 10);
        long version = ref.getVersion();

        GammaTransaction tx = transactionFactory.newTransaction();
        tx.prepare();

        try {
            ref.getAndSet(tx, 11);
            fail();
        } catch (PreparedTransactionException expected) {
        }

        assertIsAborted(tx);
        assertVersionAndValue(ref, version, 10);
    }

    @Test
    public void whenAbortedTransaction_thenDeadTransactionException() {
        GammaIntRef ref = new GammaIntRef(stm, 10);
        long version = ref.getVersion();
        GammaTransaction tx = transactionFactory.newTransaction();
        tx.abort();

        try {
            ref.getAndSet(tx, 11);
            fail();
        } catch (DeadTransactionException expected) {
        }

        assertIsAborted(tx);
        assertVersionAndValue(ref, version, 10);
    }

    @Test
    public void whenCommittedTransaction_thenCommittedTransactionException() {
        GammaIntRef ref = new GammaIntRef(stm, 10);
        long version = ref.getVersion();
        GammaTransaction tx = transactionFactory.newTransaction();
        tx.commit();

        try {
            ref.getAndSet(tx, 11);
            fail();
        } catch (DeadTransactionException expected) {
        }

        assertIsCommitted(tx);
        assertVersionAndValue(ref, version, 10);
    }

    @Test
    public void whenSuccess() {
        GammaIntRef ref = new GammaIntRef(stm, 10);
        long version = ref.getVersion();

        GammaTransaction tx = transactionFactory.newTransaction();
        long result = ref.getAndSet(tx, 20);
        tx.commit();

        assertEquals(10, result);
        assertVersionAndValue(ref, version + 1, 20);
    }

    @Test
    public void whenNormalTransactionUsed() {
        GammaIntRef ref = new GammaIntRef(stm, 10);
        long version = ref.getVersion();

        GammaTransaction tx = transactionFactory.newTransaction();
        long result = ref.getAndSet(tx, 20);
        tx.commit();

        assertEquals(10, result);
        assertVersionAndValue(ref, version + 1, 20);
    }

    @Test
    public void whenNoChange() {
        GammaIntRef ref = new GammaIntRef(stm, 10);
        long version = ref.getVersion();

        GammaTransaction tx = transactionFactory.newTransaction();
        long value = ref.getAndSet(tx, 10);
        tx.commit();

        assertEquals(10, value);
        assertIsCommitted(tx);
        assertEquals(10, ref.atomicGet());
        assertNull(getThreadLocalTransaction());
        assertSurplus(ref, 0);
        assertVersionAndValue(ref, version, 10);
    }

    @Test
    public void whenListenersAvailable() {
        int initialValue = 10;
        GammaIntRef ref = new GammaIntRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        int newValue = 20;
        IntRefAwaitThread thread = new IntRefAwaitThread(ref, newValue);
        thread.start();

        sleepMs(500);

        GammaTransaction tx = transactionFactory.newTransaction();
        long result = ref.getAndSet(tx, newValue);
        tx.commit();

        joinAll(thread);

        assertEquals(initialValue, result);
        assertRefHasNoLocks(ref);
        assertVersionAndValue(ref, initialVersion + 1, newValue);
    }
}
