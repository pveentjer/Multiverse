package org.multiverse.stms.gamma.transactionalobjects.txnlong;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.multiverse.api.LockMode;
import org.multiverse.api.Txn;
import org.multiverse.api.TxnFactory;
import org.multiverse.api.exceptions.DeadTxnException;
import org.multiverse.api.exceptions.PreparedTxnException;
import org.multiverse.api.exceptions.ReadWriteConflict;
import org.multiverse.api.references.TxnLong;
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
import static org.multiverse.TestUtils.*;
import static org.multiverse.api.TxnThreadLocal.clearThreadLocalTxn;
import static org.multiverse.stms.gamma.GammaTestUtils.*;

@RunWith(Parameterized.class)
public class GammaTxnLong_get1Test {

    private final GammaTxnFactory transactionFactory;
    private final GammaStm stm;

    public GammaTxnLong_get1Test(GammaTxnFactory transactionFactory) {
        this.transactionFactory = transactionFactory;
        this.stm = transactionFactory.getConfig().getStm();
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
    public void whenPreparedTransactionAvailable_thenPreparedTxnException() {
        TxnLong ref = new GammaTxnLong(stm, 10);

        GammaTxn tx = transactionFactory.newTxn();
        tx.prepare();

        try {
            ref.get(tx);
            fail();
        } catch (PreparedTxnException expected) {

        }
    }

    @Test
    public void whenLockedForCommitBySelf_thenSuccess() {
        GammaTxnLong ref = new GammaTxnLong(stm, 100);
        long version = ref.getVersion();

        GammaTxn tx = transactionFactory.newTxn();

        ref.getLock().acquire(tx, LockMode.Exclusive);

        long value = ref.get(tx);

        assertEquals(100, value);
        assertRefHasExclusiveLock(ref, tx);
        assertSurplus(ref, 1);
        assertWriteBiased(ref);
        assertIsActive(tx);
        assertEquals(version, ref.getVersion());
        assertEquals(100, ref.long_value);
    }

    @Test
    public void whenLockedForWriteBySelf() {
        GammaTxnLong ref = new GammaTxnLong(stm, 100);
        long version = ref.getVersion();

        GammaTxn tx = transactionFactory.newTxn();

        ref.getLock().acquire(tx, LockMode.Write);
        long value = ref.get(tx);

        assertEquals(100, value);
        assertRefHasWriteLock(ref, tx);
        assertSurplus(ref, 1);
        assertWriteBiased(ref);
        assertIsActive(tx);
        assertVersionAndValue(ref, version, 100);
    }

    @Test
    public void whenLockedForReadBySelf() {
        GammaTxnLong ref = new GammaTxnLong(stm, 100);
        long version = ref.getVersion();

        GammaTxn tx = transactionFactory.newTxn();

        ref.getLock().acquire(tx, LockMode.Read);
        long value = ref.get(tx);

        assertEquals(100, value);
        assertRefHasReadLock(ref, tx);
        assertSurplus(ref, 1);
        assertWriteBiased(ref);
        assertIsActive(tx);
        assertVersionAndValue(ref, version, 100);
    }

    @Test
    public void whenCommtLockAcquiredByOther_thenReadConflict() {
        GammaTxnLong ref = new GammaTxnLong(stm, 100);
        long version = ref.getVersion();

        GammaTxn tx = transactionFactory.newTxn();

        GammaTxn otherTx = transactionFactory.newTxn();
        ref.getLock().acquire(otherTx, LockMode.Exclusive);

        try {
            ref.get(tx);
            fail();
        } catch (ReadWriteConflict expected) {
        }

        assertRefHasExclusiveLock(ref, otherTx);
        assertSurplus(ref, 1);
        assertWriteBiased(ref);
        assertIsActive(otherTx);
        assertIsAborted(tx);
        assertVersionAndValue(ref, version, 100);
    }

    @Test
    public void whenEnsuredByother_thenReadStillPossible() {
        GammaTxnLong ref = new GammaTxnLong(stm, 100);
        long version = ref.getVersion();

        GammaTxn tx = transactionFactory.newTxn();

        GammaTxn otherTx = transactionFactory.newTxn();

        ref.getLock().acquire(otherTx, LockMode.Write);

        long value = ref.get(tx);

        assertEquals(100, value);
        assertRefHasWriteLock(ref, otherTx);
        assertSurplus(ref, 1);
        assertWriteBiased(ref);
        assertIsActive(otherTx);
        assertIsActive(tx);
        assertVersionAndValue(ref, version, 100);
    }

    @Test
    public void whenActiveTransactionAvailable_thenPreparedTxnException() {
        TxnLong ref = new GammaTxnLong(stm, 10);

        GammaTxn tx = transactionFactory.newTxn();

        long value = ref.get(tx);

        assertEquals(10, value);
        assertIsActive(tx);
    }

    @Test
    public void whenNullTransactionAvailable_thenNullPointerException() {
        long initialValue = 10;
        GammaTxnLong ref = new GammaTxnLong(stm, initialValue);
        long initialVersion = ref.getVersion();

        try {
            ref.get((Txn)null);
            fail();
        } catch (NullPointerException expected) {

        }

        assertVersionAndValue(ref, initialVersion, initialValue);
    }

    @Test
    public void whenCommittedTransactionAvailable_thenDeadTxnException() {
        long initialValue = 10;
        GammaTxnLong ref = new GammaTxnLong(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTxn tx = transactionFactory.newTxn();
        tx.commit();

        try {
            ref.get(tx);
            fail();
        } catch (DeadTxnException expected) {

        }

        assertVersionAndValue(ref, initialVersion, initialValue);
        assertIsCommitted(tx);
    }

    @Test
    public void whenAbortedTransactionAvailable_thenDeadTxnException() {
        long initialValue = 10;
        GammaTxnLong ref = new GammaTxnLong(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTxn tx = transactionFactory.newTxn();
        tx.abort();

        try {
            ref.get(tx);
            fail();
        } catch (DeadTxnException expected) {

        }

        assertIsAborted(tx);
        assertVersionAndValue(ref, initialVersion, initialValue);
    }
}
