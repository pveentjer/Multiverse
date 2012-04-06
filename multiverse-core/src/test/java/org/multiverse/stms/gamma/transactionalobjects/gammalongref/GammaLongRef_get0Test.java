package org.multiverse.stms.gamma.transactionalobjects.gammalongref;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.multiverse.api.LockMode;
import org.multiverse.api.TxnFactory;
import org.multiverse.api.exceptions.DeadTransactionException;
import org.multiverse.api.exceptions.PreparedTransactionException;
import org.multiverse.api.exceptions.ReadWriteConflict;
import org.multiverse.api.exceptions.TransactionMandatoryException;
import org.multiverse.api.references.LongRef;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactionalobjects.GammaLongRef;
import org.multiverse.stms.gamma.transactions.GammaTxn;
import org.multiverse.stms.gamma.transactions.GammaTxnFactory;
import org.multiverse.stms.gamma.transactions.fat.FatFixedLengthGammaTxnFactory;
import org.multiverse.stms.gamma.transactions.fat.FatMonoGammaTxnFactory;
import org.multiverse.stms.gamma.transactions.fat.FatVariableLengthGammaTxnFactory;

import java.util.Collection;

import static java.util.Arrays.asList;
import static org.junit.Assert.*;
import static org.multiverse.TestUtils.*;
import static org.multiverse.api.TxnThreadLocal.*;
import static org.multiverse.stms.gamma.GammaTestUtils.*;

@RunWith(Parameterized.class)
public class GammaLongRef_get0Test {

    private final GammaTxnFactory transactionFactory;
    private final GammaStm stm;

    public GammaLongRef_get0Test(GammaTxnFactory transactionFactory) {
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
    public void whenPreparedTransactionAvailable_thenPreparedTransactionException() {
        LongRef ref = new GammaLongRef(stm, 10);

        GammaTxn tx = transactionFactory.newTransaction();
        tx.prepare();
        setThreadLocalTxn(tx);

        try {
            ref.get();
            fail();
        } catch (PreparedTransactionException expected) {

        }
    }

    @Test
    public void whenPrivatizedBySelf_thenSuccess() {
        GammaLongRef ref = new GammaLongRef(stm, 100);
        long version = ref.getVersion();

        GammaTxn tx = transactionFactory.newTransaction();
        setThreadLocalTxn(tx);

        ref.getLock().acquire(LockMode.Exclusive);

        long value = ref.get();

        assertEquals(100, value);
        assertRefHasExclusiveLock(ref, tx);
        assertSurplus(ref, 1);
        assertWriteBiased(ref);
        assertIsActive(tx);
        assertSame(tx, getThreadLocalTxn());
        assertEquals(version, ref.getVersion());
        assertEquals(100, ref.long_value);
    }

    @Test
    public void whenEnsuredBySelf() {
        GammaLongRef ref = new GammaLongRef(stm, 100);
        long version = ref.getVersion();

        GammaTxn tx = transactionFactory.newTransaction();
        setThreadLocalTxn(tx);

        ref.getLock().acquire(LockMode.Write);
        long value = ref.get();

        assertEquals(100, value);
        assertRefHasWriteLock(ref, tx);
        assertSurplus(ref, 1);
        assertWriteBiased(ref);
        assertIsActive(tx);
        assertSame(tx, getThreadLocalTxn());
        assertVersionAndValue(ref, version, 100);
    }

    @Test
    public void whenPrivatizedByOther_thenReadConflict() {
        GammaLongRef ref = new GammaLongRef(stm, 100);
        long version = ref.getVersion();

        GammaTxn tx = transactionFactory.newTransaction();
        setThreadLocalTxn(tx);

        GammaTxn otherTx = transactionFactory.newTransaction();
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
        assertSame(tx, getThreadLocalTxn());
        assertVersionAndValue(ref, version, 100);
    }

    @Test
    public void whenEnsuredByother_thenReadStillPossible() {
        GammaLongRef ref = new GammaLongRef(stm, 100);
        long version = ref.getVersion();

        GammaTxn tx = transactionFactory.newTransaction();
        setThreadLocalTxn(tx);

        GammaTxn otherTx = transactionFactory.newTransaction();
        ref.getLock().acquire(otherTx, LockMode.Write);

        long value = ref.get();

        assertEquals(100, value);
        assertRefHasWriteLock(ref, otherTx);
        assertSurplus(ref, 1);
        assertWriteBiased(ref);
        assertIsActive(otherTx);
        assertIsActive(tx);
        assertSame(tx, getThreadLocalTxn());
        assertVersionAndValue(ref, version, 100);
    }

    @Test
    public void whenActiveTransactionAvailable_thenPreparedTransactionException() {
        LongRef ref = new GammaLongRef(stm, 10);

        GammaTxn tx = transactionFactory.newTransaction();
        setThreadLocalTxn(tx);

        long value = ref.get();

        assertEquals(10, value);
        assertIsActive(tx);
        assertSame(tx, getThreadLocalTxn());
    }

    @Test
    public void whenNoTransactionAvailable_thenNoTransactionFoundException() {
        long initialValue = 10;
        GammaLongRef ref = new GammaLongRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        try {
            ref.get();
            fail();
        } catch (TransactionMandatoryException expected) {

        }

        assertNull(getThreadLocalTxn());
        assertVersionAndValue(ref, initialVersion, initialValue);
    }

    @Test
    public void whenCommittedTransactionAvailable_thenDeadTransactionException() {
        long initialValue = 10;
        GammaLongRef ref = new GammaLongRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTxn tx = transactionFactory.newTransaction();
        tx.commit();
        setThreadLocalTxn(tx);

        try {
            ref.get();
            fail();
        } catch (DeadTransactionException expected) {

        }

        assertVersionAndValue(ref, initialVersion, initialValue);
        assertIsCommitted(tx);
        assertSame(tx, getThreadLocalTxn());
    }

    @Test
    public void whenAbortedTransactionAvailable_thenDeadTransactionException() {
        long initialValue = 10;
        GammaLongRef ref = new GammaLongRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTxn tx = transactionFactory.newTransaction();
        tx.abort();
        setThreadLocalTxn(tx);

        try {
            ref.get();
            fail();
        } catch (DeadTransactionException expected) {

        }

        assertIsAborted(tx);
        assertSame(tx, getThreadLocalTxn());
        assertVersionAndValue(ref, initialVersion, initialValue);
    }
}
