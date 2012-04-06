package org.multiverse.stms.gamma.transactionalobjects.basegammaref;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.multiverse.api.LockMode;
import org.multiverse.api.TxnFactory;
import org.multiverse.api.exceptions.DeadTransactionException;
import org.multiverse.api.exceptions.PreparedTransactionException;
import org.multiverse.api.exceptions.ReadWriteConflict;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactionalobjects.GammaLongRef;
import org.multiverse.stms.gamma.transactionalobjects.GammaRefTranlocal;
import org.multiverse.stms.gamma.transactions.GammaTransaction;
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
import static org.multiverse.api.ThreadLocalTransaction.*;
import static org.multiverse.stms.gamma.GammaTestUtils.*;

@RunWith(Parameterized.class)
public class BaseGammaRef_ensure1Test {

    private final GammaTxnFactory transactionFactory;
    private final GammaStm stm;

    public BaseGammaRef_ensure1Test(GammaTxnFactory transactionFactory) {
        this.transactionFactory = transactionFactory;
        this.stm = transactionFactory.getConfiguration().getStm();
    }

    @Before
    public void setUp() {
        clearThreadLocalTransaction();
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
        GammaLongRef ref = new GammaLongRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTransaction tx = transactionFactory.newTransaction();


        setThreadLocalTransaction(tx);
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
        GammaLongRef ref = new GammaLongRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTransaction tx = transactionFactory.newTransaction();
        ref.set(tx, initialValue + 1);
        ref.getLock().acquire(tx, LockMode.Read);
        ref.ensure(tx);

        GammaRefTranlocal tranlocal = tx.getRefTranlocal(ref);
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
        GammaLongRef ref = new GammaLongRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTransaction tx = transactionFactory.newTransaction();
        ref.set(tx, initialValue + 1);
        ref.getLock().acquire(tx, LockMode.Write);
        ref.ensure(tx);

        GammaRefTranlocal tranlocal = tx.getRefTranlocal(ref);
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
        GammaLongRef ref = new GammaLongRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTransaction tx = transactionFactory.newTransaction();
        ref.set(tx, initialValue + 1);
        ref.getLock().acquire(tx, LockMode.Exclusive);
        ref.ensure(tx);

        GammaRefTranlocal tranlocal = tx.getRefTranlocal(ref);
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
        GammaLongRef ref = new GammaLongRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTransaction otherTx = transactionFactory.newTransaction();
        ref.getLock().acquire(otherTx, LockMode.Read);

        GammaTransaction tx = transactionFactory.newTransaction();
        ref.set(tx, initialValue + 1);
        ref.ensure(tx);

        GammaRefTranlocal tranlocal = tx.getRefTranlocal(ref);
        assertIsActive(tx);
        assertTrue(tranlocal.isConflictCheckNeeded());
        assertRefHasReadLock(ref, otherTx);
        assertEquals(LOCKMODE_NONE, tranlocal.getLockMode());
        assertVersionAndValue(ref, initialVersion, initialValue);
    }

    @Test
    public void whenWriteLockAcquiredByOther() {
        long initialValue = 10;
        GammaLongRef ref = new GammaLongRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTransaction otherTx = transactionFactory.newTransaction();
        ref.getLock().acquire(otherTx, LockMode.Write);

        GammaTransaction tx = transactionFactory.newTransaction();
        ref.set(tx, initialValue + 1);
        ref.ensure(tx);

        GammaRefTranlocal tranlocal = tx.getRefTranlocal(ref);
        assertIsActive(tx);
        assertTrue(tranlocal.isConflictCheckNeeded());
        assertRefHasWriteLock(ref, otherTx);
        assertEquals(LOCKMODE_NONE, tranlocal.getLockMode());
        assertVersionAndValue(ref, initialVersion, initialValue);
    }

    @Test
    public void whenPrivatizedByOther_thenDeferredEnsureFails() {
        long initialValue = 10;
        GammaLongRef ref = new GammaLongRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTransaction otherTx = transactionFactory.newTransaction();
        ref.getLock().acquire(otherTx, LockMode.Exclusive);

        GammaTransaction tx = transactionFactory.newTransaction();
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
        GammaLongRef ref = new GammaLongRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        try {
            ref.ensure(null);
            fail();
        } catch (NullPointerException expected) {
        }

        assertNull(getThreadLocalTransaction());
        assertVersionAndValue(ref, initialVersion, initialValue);
        assertRefHasNoLocks(ref);
    }

    @Test
    public void state_whenAlreadyPrepared_thenPreparedTransactionException() {
        GammaTransaction tx = transactionFactory.newTransaction();
        tx.prepare();

        long initialValue = 10;
        GammaLongRef ref = new GammaLongRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        try {
            ref.ensure(tx);
            fail();
        } catch (PreparedTransactionException expected) {
        }

        assertIsAborted(tx);
        assertNull(getThreadLocalTransaction());
        assertVersionAndValue(ref, initialVersion, initialValue);
        assertRefHasNoLocks(ref);
    }

    @Test
    public void state_whenAlreadyAborted_thenDeadTransactionException() {
        GammaTransaction tx = transactionFactory.newTransaction();
        tx.abort();

        long initialValue = 10;
        GammaLongRef ref = new GammaLongRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        try {
            ref.ensure(tx);
            fail();
        } catch (DeadTransactionException expected) {
        }

        assertIsAborted(tx);
        assertNull(getThreadLocalTransaction());
        assertVersionAndValue(ref, initialVersion, initialValue);
        assertRefHasNoLocks(ref);
    }

    @Test
    public void state_whenAlreadyCommitted_thenDeadTransactionException() {
        GammaTransaction tx = transactionFactory.newTransaction();
        tx.commit();

        long initialValue = 10;
        GammaLongRef ref = new GammaLongRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        try {
            ref.ensure(tx);
            fail();
        } catch (DeadTransactionException expected) {
        }

        assertIsCommitted(tx);
        assertNull(getThreadLocalTransaction());
        assertVersionAndValue(ref, initialVersion, initialValue);
        assertRefHasNoLocks(ref);
    }

    @Test
    public void whenPossibleWriteSkew_thenCanBeDetectedWithDeferredEnsure() {
        assumeTrue(!(transactionFactory.newTransaction() instanceof FatMonoGammaTransaction));

        GammaLongRef ref1 = new GammaLongRef(stm);
        GammaLongRef ref2 = new GammaLongRef(stm);

        GammaTransaction tx1 = transactionFactory.newTransaction();
        ref1.get(tx1);
        ref2.incrementAndGet(tx1, 1);

        GammaTransaction tx2 = transactionFactory.newTransaction();
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
