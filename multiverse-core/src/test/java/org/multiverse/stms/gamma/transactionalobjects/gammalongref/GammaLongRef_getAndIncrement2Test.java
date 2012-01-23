package org.multiverse.stms.gamma.transactionalobjects.gammalongref;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.multiverse.api.TransactionFactory;
import org.multiverse.api.exceptions.DeadTransactionException;
import org.multiverse.api.exceptions.PreparedTransactionException;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactionalobjects.GammaLongRef;
import org.multiverse.stms.gamma.transactions.GammaTransaction;
import org.multiverse.stms.gamma.transactions.GammaTransactionFactory;
import org.multiverse.stms.gamma.transactions.fat.FatFixedLengthGammaTransactionFactory;
import org.multiverse.stms.gamma.transactions.fat.FatMonoGammaTransactionFactory;
import org.multiverse.stms.gamma.transactions.fat.FatVariableLengthGammaTransactionFactory;

import java.util.Collection;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.multiverse.TestUtils.assertIsAborted;
import static org.multiverse.TestUtils.assertIsCommitted;
import static org.multiverse.api.ThreadLocalTransaction.clearThreadLocalTransaction;
import static org.multiverse.stms.gamma.GammaTestUtils.assertVersionAndValue;

@RunWith(Parameterized.class)
public class GammaLongRef_getAndIncrement2Test {

    private final GammaTransactionFactory transactionFactory;
    private final GammaStm stm;

    public GammaLongRef_getAndIncrement2Test(GammaTransactionFactory transactionFactory) {
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
        GammaLongRef ref = new GammaLongRef(stm, 10);
        long version = ref.getVersion();

        try {
            ref.getAndIncrement(null, 10);
            fail();
        } catch (NullPointerException expected) {
        }

        assertVersionAndValue(ref, version, 10);
    }

    @Test
    public void whenTransactionCommitted_thenDeadTransactionException() {
        GammaLongRef ref = new GammaLongRef(stm, 10);
        long version = ref.getVersion();

        GammaTransaction tx = transactionFactory.newTransaction();
        tx.commit();
        try {
            ref.getAndIncrement(tx, 10);
            fail();
        } catch (DeadTransactionException expected) {
        }

        assertIsCommitted(tx);
        assertVersionAndValue(ref, version, 10);
    }

    @Test
    public void whenTransactionAborted_thenDeadTransactionException() {
        GammaLongRef ref = new GammaLongRef(stm, 10);
        long version = ref.getVersion();

        GammaTransaction tx = stm.newDefaultTransaction();
        tx.abort();
        try {
            ref.getAndIncrement(tx, 10);
            fail();
        } catch (DeadTransactionException expected) {
        }

        assertIsAborted(tx);
        assertVersionAndValue(ref, version, 10);
    }

    @Test
    public void whenTransactionPrepared_thenPreparedTransactionException() {
        GammaLongRef ref = new GammaLongRef(stm, 10);
        long version = ref.getVersion();

        GammaTransaction tx = transactionFactory.newTransaction();
        tx.prepare();
        try {
            ref.getAndIncrement(tx, 10);
            fail();
        } catch (PreparedTransactionException expected) {
        }

        assertIsAborted(tx);
        assertVersionAndValue(ref, version, 10);
    }

    @Test
    public void whenNoChange() {
        GammaLongRef ref = new GammaLongRef(stm, 10);
        long version = ref.getVersion();

        GammaTransaction tx = transactionFactory.newTransaction();
        long result = ref.getAndIncrement(tx, 0);
        tx.commit();

        assertEquals(10, result);
        assertIsCommitted(tx);
        assertVersionAndValue(ref, version, 10);
    }

    @Test
    public void whenSuccess() {
        GammaLongRef ref = new GammaLongRef(stm, 10);
        long version = ref.getVersion();

        GammaTransaction tx = transactionFactory.newTransaction();
        long result = ref.getAndIncrement(tx, 20);
        tx.commit();

        assertIsCommitted(tx);
        assertEquals(10, result);
        assertVersionAndValue(ref, version + 1, 30);
    }
}
