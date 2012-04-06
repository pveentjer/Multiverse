package org.multiverse.stms.gamma.transactionalobjects.lock;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.multiverse.api.LockMode;
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
import static org.junit.Assert.*;
import static org.multiverse.TestUtils.*;
import static org.multiverse.api.TxnThreadLocal.clearThreadLocalTxn;
import static org.multiverse.stms.gamma.GammaTestUtils.assertRefHasLockMode;

@RunWith(Parameterized.class)
public class Lock_getLockMode1Test {

    private final GammaTxnFactory transactionFactory;
    private final GammaStm stm;

    public Lock_getLockMode1Test(GammaTxnFactory transactionFactory) {
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

    @Test(expected = NullPointerException.class)
    public void whenNullTransaction() {
        GammaTxnLong ref = new GammaTxnLong(stm);

        ref.getLock().getLockMode(null);
    }

     @Test
    public void other_whenLockedForWriteByOther() {
        whenLockedByOther(LockMode.Read);
        whenLockedByOther(LockMode.Write);
        whenLockedByOther(LockMode.Exclusive);
    }

    public void whenLockedByOther(LockMode lockMode) {
        GammaTxnLong ref = new GammaTxnLong(stm);
        GammaTxn otherTx = transactionFactory.newTransaction();
        ref.getLock().acquire(otherTx, lockMode);

        GammaTxn tx = transactionFactory.newTransaction();
        LockMode result = ref.getLock().getLockMode(tx);

        assertEquals(LockMode.None, result);
        assertRefHasLockMode(ref, otherTx, lockMode.asInt());
    }

    @Test
    public void whenLockedBySelf(){
        self_whenLocked(LockMode.None);
        self_whenLocked(LockMode.Read);
        self_whenLocked(LockMode.Write);
        self_whenLocked(LockMode.Exclusive);
    }

    public void self_whenLocked(LockMode lockMode){
        GammaTxnLong ref = new GammaTxnLong(stm);

        GammaTxn tx = transactionFactory.newTransaction();
        ref.getLock().acquire(tx, lockMode);
        LockMode result = ref.getLock().getLockMode(tx);

        assertEquals(lockMode, result);
        assertIsActive(tx);
    }
    @Test
    public void whenTransactionPrepared_thenPreparedTxnException() {
        GammaTxnLong ref = new GammaTxnLong(stm);

        GammaTxn tx = transactionFactory.newTransaction();
        tx.prepare();

        try {
            ref.getLock().getLockMode(tx);
            fail();
        } catch (PreparedTxnException expected) {

        }

        assertIsAborted(tx);
    }

    @Test
    public void whenTransactionAborted_thenDeadTxnException() {
        GammaTxnLong ref = new GammaTxnLong(stm);

        GammaTxn tx = transactionFactory.newTransaction();
        tx.abort();

        try {
            ref.getLock().getLockMode(tx);
            fail();
        } catch (DeadTxnException expected) {

        }

        assertIsAborted(tx);
    }

    @Test
    public void whenTransactionCommitted_thenDeadTxnException() {
        GammaTxnLong ref = new GammaTxnLong(stm);

        GammaTxn tx = transactionFactory.newTransaction();
        tx.commit();

        try {
            ref.getLock().getLockMode(tx);
            fail();
        } catch (DeadTxnException expected) {

        }

        assertIsCommitted(tx);
    }
}
