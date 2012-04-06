package org.multiverse.stms.gamma.transactionalobjects.txnlong;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.multiverse.api.TxnFactory;
import org.multiverse.api.exceptions.DeadTxnException;
import org.multiverse.api.exceptions.PreparedTxnException;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactionalobjects.GammaTxnLong;
import org.multiverse.stms.gamma.transactions.GammaTxn;
import org.multiverse.stms.gamma.transactions.GammaTxnFactory;
import org.multiverse.stms.gamma.transactions.fat.FatFixedLengthGammaTxnFactory;
import org.multiverse.stms.gamma.transactions.fat.FatMonoGammaTxnFactory;
import org.multiverse.stms.gamma.transactions.fat.FatVariableLengthGammaTxnFactory;

import java.util.Collection;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.multiverse.TestUtils.assertIsAborted;
import static org.multiverse.TestUtils.assertIsCommitted;
import static org.multiverse.api.TxnThreadLocal.clearThreadLocalTxn;
import static org.multiverse.stms.gamma.GammaTestUtils.assertVersionAndValue;

@RunWith(Parameterized.class)
public class GammaTxnLong_getAndIncrement2Test {

    private final GammaTxnFactory transactionFactory;
    private final GammaStm stm;

    public GammaTxnLong_getAndIncrement2Test(GammaTxnFactory transactionFactory) {
        this.transactionFactory = transactionFactory;
        this.stm = transactionFactory.getConfiguration().getStm();
    }

    @Before
    public void setUp() {
        clearThreadLocalTxn();
    }

    @Parameterized.Parameters
    public static Collection<TxnFactory[]> configs() {
        return asList(
                new TxnFactory[]{new FatVariableLengthGammaTxnFactory(new GammaStm())},
                new TxnFactory[]{new FatFixedLengthGammaTxnFactory(new GammaStm())},
                new TxnFactory[]{new FatMonoGammaTxnFactory(new GammaStm())}
        );
    }

    @Test
    public void whenTransactionNull_thenNullPointerException() {
        GammaTxnLong ref = new GammaTxnLong(stm, 10);
        long version = ref.getVersion();

        try {
            ref.getAndIncrement(null, 10);
            fail();
        } catch (NullPointerException expected) {
        }

        assertVersionAndValue(ref, version, 10);
    }

    @Test
    public void whenTransactionCommitted_thenDeadTxnException() {
        GammaTxnLong ref = new GammaTxnLong(stm, 10);
        long version = ref.getVersion();

        GammaTxn tx = transactionFactory.newTransaction();
        tx.commit();
        try {
            ref.getAndIncrement(tx, 10);
            fail();
        } catch (DeadTxnException expected) {
        }

        assertIsCommitted(tx);
        assertVersionAndValue(ref, version, 10);
    }

    @Test
    public void whenTransactionAborted_thenDeadTxnException() {
        GammaTxnLong ref = new GammaTxnLong(stm, 10);
        long version = ref.getVersion();

        GammaTxn tx = stm.newDefaultTxn();
        tx.abort();
        try {
            ref.getAndIncrement(tx, 10);
            fail();
        } catch (DeadTxnException expected) {
        }

        assertIsAborted(tx);
        assertVersionAndValue(ref, version, 10);
    }

    @Test
    public void whenTransactionPrepared_thenPreparedTxnException() {
        GammaTxnLong ref = new GammaTxnLong(stm, 10);
        long version = ref.getVersion();

        GammaTxn tx = transactionFactory.newTransaction();
        tx.prepare();
        try {
            ref.getAndIncrement(tx, 10);
            fail();
        } catch (PreparedTxnException expected) {
        }

        assertIsAborted(tx);
        assertVersionAndValue(ref, version, 10);
    }

    @Test
    public void whenNoChange() {
        GammaTxnLong ref = new GammaTxnLong(stm, 10);
        long version = ref.getVersion();

        GammaTxn tx = transactionFactory.newTransaction();
        long result = ref.getAndIncrement(tx, 0);
        tx.commit();

        assertEquals(10, result);
        assertIsCommitted(tx);
        assertVersionAndValue(ref, version, 10);
    }

    @Test
    public void whenSuccess() {
        GammaTxnLong ref = new GammaTxnLong(stm, 10);
        long version = ref.getVersion();

        GammaTxn tx = transactionFactory.newTransaction();
        long result = ref.getAndIncrement(tx, 20);
        tx.commit();

        assertIsCommitted(tx);
        assertEquals(10, result);
        assertVersionAndValue(ref, version + 1, 30);
    }
}
