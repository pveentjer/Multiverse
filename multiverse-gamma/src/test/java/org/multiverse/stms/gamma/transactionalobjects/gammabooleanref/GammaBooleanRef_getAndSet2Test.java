package org.multiverse.stms.gamma.transactionalobjects.gammabooleanref;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.multiverse.api.TransactionFactory;
import org.multiverse.api.exceptions.DeadTransactionException;
import org.multiverse.api.exceptions.PreparedTransactionException;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactionalobjects.GammaBooleanRef;
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
public class GammaBooleanRef_getAndSet2Test {
    private final GammaTransactionFactory transactionFactory;
    private final GammaStm stm;

    public GammaBooleanRef_getAndSet2Test(GammaTransactionFactory transactionFactory) {
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
        boolean initialValue = true;
        GammaBooleanRef ref = new GammaBooleanRef(stm, initialValue);
        long version = ref.getVersion();

        try {
            ref.getAndSet(null, !initialValue);
            fail();
        } catch (NullPointerException expected) {
        }

        assertVersionAndValue(ref, version, initialValue);
    }

    @Test
    public void whenPreparedTransaction_thenPreparedTransactionException() {
        boolean initialValue = false;
        GammaBooleanRef ref = new GammaBooleanRef(stm, initialValue);
        long version = ref.getVersion();

        GammaTransaction tx = transactionFactory.newTransaction();
        tx.prepare();

        try {
            ref.getAndSet(tx,!initialValue);
            fail();
        } catch (PreparedTransactionException expected) {
        }

        assertIsAborted(tx);
        assertVersionAndValue(ref, version, initialValue);
    }

    @Test
    public void whenAbortedTransaction_thenDeadTransactionException() {
        boolean initialValue = true;
        GammaBooleanRef ref = new GammaBooleanRef(stm, initialValue);
        long version = ref.getVersion();
        GammaTransaction tx = transactionFactory.newTransaction();
        tx.abort();

        try {
            ref.getAndSet(tx, !initialValue);
            fail();
        } catch (DeadTransactionException expected) {
        }

        assertIsAborted(tx);
        assertVersionAndValue(ref, version, initialValue);
    }

    @Test
    public void whenCommittedTransaction_thenCommittedTransactionException() {
        boolean initialValue = true;
        GammaBooleanRef ref = new GammaBooleanRef(stm, initialValue);
        long version = ref.getVersion();
        GammaTransaction tx = transactionFactory.newTransaction();
        tx.commit();

        try {
            ref.getAndSet(tx, !initialValue);
            fail();
        } catch (DeadTransactionException expected) {
        }

        assertIsCommitted(tx);
        assertVersionAndValue(ref, version, initialValue);
    }

    @Test
    public void whenSuccess() {
        boolean initialValue = true;
        GammaBooleanRef ref = new GammaBooleanRef(stm, initialValue);
        long version = ref.getVersion();

        GammaTransaction tx = transactionFactory.newTransaction();
        boolean result = ref.getAndSet(tx, !initialValue);
        tx.commit();

        assertEquals(initialValue, result);
        assertVersionAndValue(ref, version + 1, !initialValue);
    }

    @Test
    public void whenNormalTransactionUsed() {
        boolean initialValue = true;
        GammaBooleanRef ref = new GammaBooleanRef(stm, initialValue);
        long version = ref.getVersion();

        GammaTransaction tx = transactionFactory.newTransaction();
        boolean result = ref.getAndSet(tx, !initialValue);
        tx.commit();

        assertEquals(initialValue, result);
        assertVersionAndValue(ref, version + 1, !initialValue);
    }

    @Test
    public void whenNoChange() {
        boolean initialValue = true;
        GammaBooleanRef ref = new GammaBooleanRef(stm, initialValue);
        long version = ref.getVersion();

        GammaTransaction tx = transactionFactory.newTransaction();
        boolean value = ref.getAndSet(tx, initialValue);
        tx.commit();

        assertEquals(initialValue, value);
        assertIsCommitted(tx);
        assertEquals(initialValue, ref.atomicGet());
        assertNull(getThreadLocalTransaction());
        assertSurplus(ref, 0);
        assertVersionAndValue(ref, version, initialValue);
    }

    @Test
    public void whenListenersAvailable() {
        boolean initialValue = true;
        GammaBooleanRef ref = new GammaBooleanRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        BooleanRefAwaitThread thread = new BooleanRefAwaitThread(ref, !initialValue);
        thread.start();

        sleepMs(500);

        GammaTransaction tx = transactionFactory.newTransaction();
        boolean result = ref.getAndSet(tx, !initialValue);
        tx.commit();

        joinAll(thread);

        assertEquals(initialValue, result);
        assertRefHasNoLocks(ref);
        assertVersionAndValue(ref, initialVersion + 1, !initialValue);
    }
}
