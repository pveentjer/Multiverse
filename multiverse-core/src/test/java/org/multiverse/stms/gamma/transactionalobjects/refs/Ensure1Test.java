package org.multiverse.stms.gamma.transactionalobjects.refs;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.multiverse.api.LockMode;
import org.multiverse.api.TxnFactory;
import org.multiverse.api.exceptions.DeadTxnException;
import org.multiverse.api.exceptions.PreparedTxnException;
import org.multiverse.api.exceptions.ReadWriteConflict;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactionalobjects.GammaTxnLong;
import org.multiverse.stms.gamma.transactionalobjects.Tranlocal;
import org.multiverse.stms.gamma.transactions.GammaTxn;
import org.multiverse.stms.gamma.transactions.GammaTxnFactory;
import org.multiverse.stms.gamma.transactions.fat.*;
import org.multiverse.stms.gamma.transactions.fat.FatFixedLengthGammaTxnFactory;
import org.multiverse.stms.gamma.transactions.fat.FatVariableLengthGammaTxnFactory;

import java.util.Collection;

import static java.util.Arrays.asList;
import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;
import static org.multiverse.TestUtils.LOCKMODE_EXCLUSIVE;
import static org.multiverse.TestUtils.LOCKMODE_NONE;
import static org.multiverse.TestUtils.LOCKMODE_READ;
import static org.multiverse.TestUtils.LOCKMODE_WRITE;
import static org.multiverse.TestUtils.*;
import static org.multiverse.api.TxnThreadLocal.*;
import static org.multiverse.stms.gamma.GammaTestUtils.*;

@RunWith(Parameterized.class)
public class Ensure1Test {

    private final GammaTxnFactory transactionFactory;
    private final GammaStm stm;

    public Ensure1Test(GammaTxnFactory transactionFactory) {
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
    public void whenReadonlyAndConflictingWrite_thenCommitSucceeds() {
        long initialValue = 10;
        GammaTxnLong ref = new GammaTxnLong(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTxn tx = transactionFactory.newTxn();


        setThreadLocalTxn(tx);
        ref.get(tx);
        ref.ensure(tx);

        ref.atomicIncrementAndGet(1);

        tx.commit();

        assertIsCommitted(tx);
        assertRefHasNoLocks(ref);
        assertVersionAndValue(ref, initialVersion + 1, initialValue + 1);
        assertSurplus(ref, 0);
    }

    @Test
    public void whenReadLockAcquiredBySelf() {
        long initialValue = 10;
        GammaTxnLong ref = new GammaTxnLong(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTxn tx = transactionFactory.newTxn();
        ref.set(tx, initialValue + 1);
        ref.getLock().acquire(tx, LockMode.Read);
        ref.ensure(tx);

        Tranlocal tranlocal = tx.getRefTranlocal(ref);
        assertIsActive(tx);
        assertTrue(tranlocal.isConflictCheckNeeded());
        assertEquals(LOCKMODE_READ, tranlocal.getLockMode());

        tx.commit();

        assertIsCommitted(tx);
        assertRefHasNoLocks(ref);
        assertVersionAndValue(ref, initialVersion + 1, initialValue + 1);
        assertSurplus(ref, 0);
    }

    @Test
    public void whenWriteLockAcquiredBySelf() {
        long initialValue = 10;
        GammaTxnLong ref = new GammaTxnLong(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTxn tx = transactionFactory.newTxn();
        ref.set(tx, initialValue + 1);
        ref.getLock().acquire(tx, LockMode.Write);
        ref.ensure(tx);

        Tranlocal tranlocal = tx.getRefTranlocal(ref);
        assertIsActive(tx);
        assertTrue(tranlocal.isConflictCheckNeeded());
        assertEquals(LOCKMODE_WRITE, tranlocal.getLockMode());

        tx.commit();

        assertIsCommitted(tx);
        assertRefHasNoLocks(ref);
        assertVersionAndValue(ref, initialVersion + 1, initialValue + 1);
        assertSurplus(ref, 0);
    }

    @Test
    public void whenExclusiveLockAcquiredBySelf() {
        long initialValue = 10;
        GammaTxnLong ref = new GammaTxnLong(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTxn tx = transactionFactory.newTxn();
        ref.set(tx, initialValue + 1);
        ref.getLock().acquire(tx, LockMode.Exclusive);
        ref.ensure(tx);

        Tranlocal tranlocal = tx.getRefTranlocal(ref);
        assertIsActive(tx);
        assertTrue(tranlocal.isConflictCheckNeeded());
        assertRefHasExclusiveLock(ref, tx);
        assertEquals(LOCKMODE_EXCLUSIVE, tranlocal.getLockMode());

        tx.commit();

        assertRefHasNoLocks(ref);
        assertIsCommitted(tx);
        assertVersionAndValue(ref, initialVersion + 1, initialValue + 1);
        assertSurplus(ref, 0);
    }

    @Test
    public void whenReadLockAcquiredByOther() {
        long initialValue = 10;
        GammaTxnLong ref = new GammaTxnLong(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTxn otherTx = transactionFactory.newTxn();
        ref.getLock().acquire(otherTx, LockMode.Read);

        GammaTxn tx = transactionFactory.newTxn();
        ref.set(tx, initialValue + 1);
        ref.ensure(tx);

        Tranlocal tranlocal = tx.getRefTranlocal(ref);
        assertIsActive(tx);
        assertTrue(tranlocal.isConflictCheckNeeded());
        assertRefHasReadLock(ref, otherTx);
        assertEquals(LOCKMODE_NONE, tranlocal.getLockMode());
        assertVersionAndValue(ref, initialVersion, initialValue);
    }

    @Test
    public void whenWriteLockAcquiredByOther() {
        long initialValue = 10;
        GammaTxnLong ref = new GammaTxnLong(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTxn otherTx = transactionFactory.newTxn();
        ref.getLock().acquire(otherTx, LockMode.Write);

        GammaTxn tx = transactionFactory.newTxn();
        ref.set(tx, initialValue + 1);
        ref.ensure(tx);

        Tranlocal tranlocal = tx.getRefTranlocal(ref);
        assertIsActive(tx);
        assertTrue(tranlocal.isConflictCheckNeeded());
        assertRefHasWriteLock(ref, otherTx);
        assertEquals(LOCKMODE_NONE, tranlocal.getLockMode());
        assertVersionAndValue(ref, initialVersion, initialValue);
    }

    @Test
    public void whenPrivatizedByOther_thenDeferredEnsureFails() {
        long initialValue = 10;
        GammaTxnLong ref = new GammaTxnLong(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTxn otherTx = transactionFactory.newTxn();
        ref.getLock().acquire(otherTx, LockMode.Exclusive);

        GammaTxn tx = transactionFactory.newTxn();
        try {
            ref.ensure(tx);
            fail();
        } catch (ReadWriteConflict expected) {
        }

        assertIsAborted(tx);
        assertRefHasExclusiveLock(ref, otherTx);
        assertVersionAndValue(ref, initialVersion, initialValue);
    }

    @Test
    public void state_whenNullTransaction_thenNullPointerException() {
        long initialValue = 10;
        GammaTxnLong ref = new GammaTxnLong(stm, initialValue);
        long initialVersion = ref.getVersion();

        try {
            ref.ensure(null);
            fail();
        } catch (NullPointerException expected) {
        }

        assertNull(getThreadLocalTxn());
        assertVersionAndValue(ref, initialVersion, initialValue);
        assertRefHasNoLocks(ref);
    }

    @Test
    public void state_whenAlreadyPrepared_thenPreparedTxnException() {
        GammaTxn tx = transactionFactory.newTxn();
        tx.prepare();

        long initialValue = 10;
        GammaTxnLong ref = new GammaTxnLong(stm, initialValue);
        long initialVersion = ref.getVersion();

        try {
            ref.ensure(tx);
            fail();
        } catch (PreparedTxnException expected) {
        }

        assertIsAborted(tx);
        assertNull(getThreadLocalTxn());
        assertVersionAndValue(ref, initialVersion, initialValue);
        assertRefHasNoLocks(ref);
    }

    @Test
    public void state_whenAlreadyAborted_thenDeadTxnException() {
        GammaTxn tx = transactionFactory.newTxn();
        tx.abort();

        long initialValue = 10;
        GammaTxnLong ref = new GammaTxnLong(stm, initialValue);
        long initialVersion = ref.getVersion();

        try {
            ref.ensure(tx);
            fail();
        } catch (DeadTxnException expected) {
        }

        assertIsAborted(tx);
        assertNull(getThreadLocalTxn());
        assertVersionAndValue(ref, initialVersion, initialValue);
        assertRefHasNoLocks(ref);
    }

    @Test
    public void state_whenAlreadyCommitted_thenDeadTxnException() {
        GammaTxn tx = transactionFactory.newTxn();
        tx.commit();

        long initialValue = 10;
        GammaTxnLong ref = new GammaTxnLong(stm, initialValue);
        long initialVersion = ref.getVersion();

        try {
            ref.ensure(tx);
            fail();
        } catch (DeadTxnException expected) {
        }

        assertIsCommitted(tx);
        assertNull(getThreadLocalTxn());
        assertVersionAndValue(ref, initialVersion, initialValue);
        assertRefHasNoLocks(ref);
    }

    @Test
    public void whenPossibleWriteSkew_thenCanBeDetectedWithDeferredEnsure() {
        assumeTrue(!(transactionFactory.newTxn() instanceof FatMonoGammaTxn));

        GammaTxnLong ref1 = new GammaTxnLong(stm);
        GammaTxnLong ref2 = new GammaTxnLong(stm);

        GammaTxn tx1 = transactionFactory.newTxn();
        ref1.get(tx1);
        ref2.incrementAndGet(tx1, 1);

        GammaTxn tx2 = transactionFactory.newTxn();
        ref1.incrementAndGet(tx2, 1);
        ref2.get(tx2);
        ref2.ensure(tx2);

        tx1.prepare();

        try {
            tx2.prepare();
            fail();
        } catch (ReadWriteConflict expected) {

        }

        assertIsAborted(tx2);
    }

}
