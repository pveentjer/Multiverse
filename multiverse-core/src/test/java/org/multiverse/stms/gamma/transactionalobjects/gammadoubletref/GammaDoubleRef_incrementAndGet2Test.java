package org.multiverse.stms.gamma.transactionalobjects.gammadoubletref;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.multiverse.api.TransactionFactory;
import org.multiverse.api.exceptions.DeadTransactionException;
import org.multiverse.api.exceptions.PreparedTransactionException;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactionalobjects.GammaDoubleRef;
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
import static org.multiverse.stms.gamma.GammaTestUtils.assertRefHasNoLocks;
import static org.multiverse.stms.gamma.GammaTestUtils.assertVersionAndValue;

@RunWith(Parameterized.class)
public class GammaDoubleRef_incrementAndGet2Test {

    private final GammaTransactionFactory transactionFactory;
    private final GammaStm stm;

    public GammaDoubleRef_incrementAndGet2Test(GammaTransactionFactory transactionFactory) {
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
    public void whenTransactionNull_thenNullPointerException() {
        GammaDoubleRef ref = new GammaDoubleRef(stm, 10);
        long version = ref.getVersion();

        try {
            ref.incrementAndGet(null, 10);
            fail();
        } catch (NullPointerException expected) {
        }

        assertVersionAndValue(ref, version, 10);
    }

    @Test
    public void whenTransactionCommitted_thenDeadTransactionException() {
        GammaDoubleRef ref = new GammaDoubleRef(stm, 10);
        long version = ref.getVersion();

        GammaTransaction tx = transactionFactory.newTransaction();
        tx.commit();
        try {
            ref.incrementAndGet(tx, 10);
            fail();
        } catch (DeadTransactionException expected) {
        }

        assertIsCommitted(tx);
        assertVersionAndValue(ref, version, 10);
    }

    @Test
    public void whenTransactionAborted_thenDeadTransactionException() {
        GammaDoubleRef ref = new GammaDoubleRef(stm, 10);
        long version = ref.getVersion();

        GammaTransaction tx = transactionFactory.newTransaction();
        tx.abort();
        try {
            ref.incrementAndGet(tx, 10);
            fail();
        } catch (DeadTransactionException expected) {
        }

        assertIsAborted(tx);
        assertVersionAndValue(ref, version, 10);
    }

    @Test
    public void whenTransactionPrepared_thenPreparedTransactionException() {
        GammaDoubleRef ref = new GammaDoubleRef(stm, 10);
        long version = ref.getVersion();

        GammaTransaction tx = transactionFactory.newTransaction();
        tx.prepare();
        try {
            ref.incrementAndGet(tx, 10);
            fail();
        } catch (PreparedTransactionException expected) {
        }

        assertIsAborted(tx);
        assertVersionAndValue(ref, version, 10);
    }

    @Test
    public void whenNoChange() {
        GammaDoubleRef ref = new GammaDoubleRef(stm, 10);
        long version = ref.getVersion();

        GammaTransaction tx = transactionFactory.newTransaction();
        double result = ref.incrementAndGet(tx, 0);
        tx.commit();

        assertEqualsDouble(10, result);
        assertIsCommitted(tx);
        assertVersionAndValue(ref, version, 10);
    }

    @Test
    public void whenSuccess() {
        GammaDoubleRef ref = new GammaDoubleRef(stm, 10);
        long version = ref.getVersion();

        GammaTransaction tx = transactionFactory.newTransaction();
        double result = ref.incrementAndGet(tx, 20);
        tx.commit();

        assertIsCommitted(tx);
        assertEqualsDouble(30, result);
        assertVersionAndValue(ref, version + 1, 30);
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
        double result = ref.incrementAndGet(tx, amount);
        tx.commit();

        joinAll(thread);

        assertEqualsDouble(initialValue + amount, result);
        assertRefHasNoLocks(ref);
        assertVersionAndValue(ref, initialVersion + 1, initialValue + amount);
    }
}
