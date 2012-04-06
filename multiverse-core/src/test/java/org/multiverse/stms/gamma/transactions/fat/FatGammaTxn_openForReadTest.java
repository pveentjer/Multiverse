package org.multiverse.stms.gamma.transactions.fat;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.IsolationLevel;
import org.multiverse.api.LockMode;
import org.multiverse.api.TxnStatus;
import org.multiverse.api.exceptions.*;
import org.multiverse.api.functions.Functions;
import org.multiverse.stms.gamma.GammaConstants;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactionalobjects.GammaLongRef;
import org.multiverse.stms.gamma.transactionalobjects.GammaRefTranlocal;
import org.multiverse.stms.gamma.transactions.GammaTxn;
import org.multiverse.stms.gamma.transactions.GammaTxnConfiguration;

import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;
import static org.multiverse.TestUtils.assertIsAborted;
import static org.multiverse.TestUtils.assertIsActive;
import static org.multiverse.stms.gamma.GammaTestUtils.*;

public abstract class FatGammaTxn_openForReadTest<T extends GammaTxn> implements GammaConstants {

    protected GammaStm stm;

    @Before
    public void setUp() {
        stm = new GammaStm();
    }

    protected abstract int getMaxCapacity();

    protected abstract T newTransaction();

    protected abstract T newTransaction(GammaTxnConfiguration config);


    @Test
    public void whenArrive() {
        //the mono transaction doesn't support (or need) the richmans conflict
        assumeTrue(getMaxCapacity() > 1);

        GammaLongRef ref = new GammaLongRef(stm);

        GammaTxnConfiguration config = new GammaTxnConfiguration(stm)
                .setMaximumPoorMansConflictScanLength(0);

        T tx = newTransaction(config);
        ref.openForRead(tx, LOCKMODE_NONE);

        assertSurplus(ref, 1);
    }

    @Test
    public void whenStmMismatch() {
        GammaStm otherStm = new GammaStm();
        long initialValue = 10;
        GammaLongRef ref = new GammaLongRef(otherStm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTxn tx = stm.newDefaultTransaction();

        try {
            ref.openForRead(tx, LOCKMODE_NONE);
            fail();
        } catch (StmMismatchException expected) {
        }

        assertIsAborted(tx);
        assertRefHasNoLocks(ref);
        assertVersionAndValue(ref, initialVersion, initialValue);
    }

    @Test
    public void whenTransactionAbortOnly_thenReadStillPossible() {
        GammaLongRef ref = new GammaLongRef(stm, 0);

        GammaTxn tx = stm.newDefaultTransaction();
        tx.setAbortOnly();
        GammaRefTranlocal tranlocal = ref.openForRead(tx, LOCKMODE_NONE);

        assertNotNull(tranlocal);
        assertTrue(tx.isAbortOnly());
        assertIsActive(tx);
    }

    @Test
    public void whenTransactionAbortOnly_thenRereadStillPossible() {
        GammaLongRef ref = new GammaLongRef(stm, 0);

        GammaTxn tx = newTransaction();
        GammaRefTranlocal read = ref.openForRead(tx, LOCKMODE_NONE);
        tx.setAbortOnly();
        GammaRefTranlocal reread = ref.openForRead(tx, LOCKMODE_NONE);

        assertSame(read, reread);
        assertTrue(tx.isAbortOnly());
        assertIsActive(tx);
    }

    @Test
    public void whenReadFirstAndExclusivelyLockedByOtherAndThenReread_thenNoProblem() {
        whenReadFirstAndExclusivelyLockedByOtherAndThenReread_thenNoProblem(LockMode.None);
        whenReadFirstAndExclusivelyLockedByOtherAndThenReread_thenNoProblem(LockMode.Read);
        whenReadFirstAndExclusivelyLockedByOtherAndThenReread_thenNoProblem(LockMode.Write);
        whenReadFirstAndExclusivelyLockedByOtherAndThenReread_thenNoProblem(LockMode.Exclusive);
    }

    public void whenReadFirstAndExclusivelyLockedByOtherAndThenReread_thenNoProblem(LockMode lockMode) {
        long initialValue = 10;
        GammaLongRef ref = new GammaLongRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTxn tx = newTransaction();
        GammaRefTranlocal read = ref.openForRead(tx, LOCKMODE_NONE);

        GammaTxn otherTx = newTransaction();
        ref.getLock().acquire(otherTx, lockMode);

        GammaRefTranlocal read2 = ref.openForRead(tx, LOCKMODE_NONE);

        assertIsActive(tx);
        assertSame(read, read2);
        assertRefHasLockMode(ref, otherTx, lockMode.asInt());
        assertVersionAndValue(ref, initialVersion, initialValue);
    }

    @Test
    public void whenAlreadyOpenedForConstruction() {
        T tx = newTransaction();
        GammaLongRef ref = new GammaLongRef(tx, 0);

        GammaRefTranlocal tranlocal = ref.openForRead(tx, LOCKMODE_NONE);

        assertNotNull(tranlocal);
        assertSame(ref, tranlocal.owner);
        assertEquals(LOCKMODE_EXCLUSIVE, tranlocal.getLockMode());
        assertEquals(TRANLOCAL_CONSTRUCTING, tranlocal.getMode());
        assertRefHasExclusiveLock(ref, tx);
        assertIsActive(tx);
    }

    @Test
    public void whenAlreadyOpenedForCommute() {
        long initialValue = 10;
        GammaLongRef ref = new GammaLongRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        T tx = newTransaction();
        ref.commute(tx, Functions.incLongFunction());
        GammaRefTranlocal tranlocal = ref.openForRead(tx, LOCKMODE_NONE);

        assertNotNull(tranlocal);
        assertSame(ref, tranlocal.owner);
        assertEquals(LOCKMODE_NONE, tranlocal.getLockMode());
        assertEquals(TRANLOCAL_WRITE, tranlocal.getMode());
        assertIsActive(tx);
        assertEquals(11, tranlocal.long_value);
        assertEquals(10, tranlocal.long_oldValue);
        assertRefHasNoLocks(ref);
        assertVersionAndValue(ref, initialVersion, initialValue);
    }

    @Test
    public void whenAlreadyOpenedForCommuteAndLockingConflicts() {
        long initialValue = 10;
        GammaLongRef ref = new GammaLongRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        T tx = newTransaction();
        ref.commute(tx, Functions.incLongFunction());
        GammaRefTranlocal tranlocal = tx.locate(ref);

        T otherTx = newTransaction();
        ref.getLock().acquire(otherTx, LockMode.Exclusive);

        try{
            ref.openForRead(tx, LOCKMODE_NONE);
            fail();
        }catch(ReadWriteConflict expected){

        }

        assertNotNull(tranlocal);
        assertNull(tranlocal.owner);
        assertEquals(LOCKMODE_NONE, tranlocal.getLockMode());
        assertIsAborted(tx);
        assertRefHasExclusiveLock(ref, otherTx);
        assertVersionAndValue(ref, initialVersion, initialValue);
    }

    @Test
    public void whenOverflowing() {
        int maxCapacity = getMaxCapacity();
        assumeTrue(maxCapacity < Integer.MAX_VALUE);

        GammaTxn tx = newTransaction();
        for (int k = 0; k < maxCapacity; k++) {
            GammaLongRef ref = new GammaLongRef(stm, 0);
            ref.openForRead(tx, LOCKMODE_NONE);
        }

        GammaLongRef ref = new GammaLongRef(stm, 0);
        try {
            ref.openForRead(tx, LOCKMODE_NONE);
            fail();
        } catch (SpeculativeConfigurationError expected) {
        }

        assertEquals(TxnStatus.Aborted, tx.getStatus());
        assertEquals(maxCapacity + 1, tx.getConfiguration().getSpeculativeConfiguration().minimalLength);
    }

    @Test
    public void whenReadonly() {
        long initialValue = 10;
        GammaLongRef ref = new GammaLongRef(stm, initialValue);
        long initialVersion = ref.getVersion();


        GammaTxnConfiguration config = new GammaTxnConfiguration(stm);
        config.readonly = true;
        GammaTxn tx = newTransaction(config);
        GammaRefTranlocal tranlocal = ref.openForRead(tx, LOCKMODE_NONE);

        assertNotNull(tranlocal);
        assertSame(ref, tranlocal.owner);
        assertEquals(initialVersion, tranlocal.version);
        assertEquals(initialValue, tranlocal.long_value);
        assertEquals(initialValue, tranlocal.long_oldValue);
        assertTrue(tranlocal.isRead());
        assertFalse(tx.hasWrites());
    }

    @Test
    public void whenNotOpenedBefore() {
        long initialValue = 10;
        GammaLongRef ref = new GammaLongRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTxn tx = newTransaction();
        GammaRefTranlocal tranlocal = ref.openForRead(tx, LOCKMODE_NONE);

        assertNotNull(tranlocal);
        assertSame(ref, tranlocal.owner);
        assertEquals(initialVersion, tranlocal.version);
        assertEquals(initialValue, tranlocal.long_value);
        assertEquals(initialValue, tranlocal.long_oldValue);
        assertTrue(tranlocal.isRead());
        assertFalse(tx.hasWrites());
    }

    @Test
    public void whenRefAlreadyOpenedForRead() {
        whenRefAlreadyOpenedForRead(LockMode.None, LockMode.None, LockMode.None);
        whenRefAlreadyOpenedForRead(LockMode.None, LockMode.Read, LockMode.Read);
        whenRefAlreadyOpenedForRead(LockMode.None, LockMode.Write, LockMode.Write);
        whenRefAlreadyOpenedForRead(LockMode.None, LockMode.Exclusive, LockMode.Exclusive);

        whenRefAlreadyOpenedForRead(LockMode.Read, LockMode.None, LockMode.Read);
        whenRefAlreadyOpenedForRead(LockMode.Read, LockMode.Read, LockMode.Read);
        whenRefAlreadyOpenedForRead(LockMode.Read, LockMode.Write, LockMode.Write);
        whenRefAlreadyOpenedForRead(LockMode.Read, LockMode.Exclusive, LockMode.Exclusive);

        whenRefAlreadyOpenedForRead(LockMode.Write, LockMode.None, LockMode.Write);
        whenRefAlreadyOpenedForRead(LockMode.Write, LockMode.Read, LockMode.Write);
        whenRefAlreadyOpenedForRead(LockMode.Write, LockMode.Write, LockMode.Write);
        whenRefAlreadyOpenedForRead(LockMode.Write, LockMode.Exclusive, LockMode.Exclusive);

        whenRefAlreadyOpenedForRead(LockMode.Exclusive, LockMode.None, LockMode.Exclusive);
        whenRefAlreadyOpenedForRead(LockMode.Exclusive, LockMode.Read, LockMode.Exclusive);
        whenRefAlreadyOpenedForRead(LockMode.Exclusive, LockMode.Write, LockMode.Exclusive);
        whenRefAlreadyOpenedForRead(LockMode.Exclusive, LockMode.Exclusive, LockMode.Exclusive);
    }

    public void whenRefAlreadyOpenedForRead(LockMode firstReadLockMode, LockMode secondReadLockMode, LockMode expectedLockMode) {
        long initialValue = 10;
        GammaLongRef ref = new GammaLongRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTxn tx = newTransaction();
        GammaRefTranlocal first = ref.openForWrite(tx, firstReadLockMode.asInt());
        GammaRefTranlocal second = ref.openForRead(tx, secondReadLockMode.asInt());

        assertSame(first, second);
        assertNotNull(second);
        assertSame(ref, second.owner);
        assertEquals(initialVersion, second.version);
        assertEquals(initialValue, second.long_value);
        assertEquals(initialValue, second.long_oldValue);
        assertLockMode(ref, expectedLockMode);
        assertTrue(second.isWrite());
        assertTrue(tx.hasWrites());
    }

    @Test
    public void whenRefAlreadyOpenedForWrite() {
        whenRefAlreadyOpenedForWrite(LockMode.None, LockMode.None, LockMode.None);
        whenRefAlreadyOpenedForWrite(LockMode.None, LockMode.Read, LockMode.Read);
        whenRefAlreadyOpenedForWrite(LockMode.None, LockMode.Write, LockMode.Write);
        whenRefAlreadyOpenedForWrite(LockMode.None, LockMode.Exclusive, LockMode.Exclusive);

        whenRefAlreadyOpenedForWrite(LockMode.Read, LockMode.None, LockMode.Read);
        whenRefAlreadyOpenedForWrite(LockMode.Read, LockMode.Read, LockMode.Read);
        whenRefAlreadyOpenedForWrite(LockMode.Read, LockMode.Write, LockMode.Write);
        whenRefAlreadyOpenedForWrite(LockMode.Read, LockMode.Exclusive, LockMode.Exclusive);

        whenRefAlreadyOpenedForWrite(LockMode.Write, LockMode.None, LockMode.Write);
        whenRefAlreadyOpenedForWrite(LockMode.Write, LockMode.Read, LockMode.Write);
        whenRefAlreadyOpenedForWrite(LockMode.Write, LockMode.Write, LockMode.Write);
        whenRefAlreadyOpenedForWrite(LockMode.Write, LockMode.Exclusive, LockMode.Exclusive);

        whenRefAlreadyOpenedForWrite(LockMode.Exclusive, LockMode.None, LockMode.Exclusive);
        whenRefAlreadyOpenedForWrite(LockMode.Exclusive, LockMode.Read, LockMode.Exclusive);
        whenRefAlreadyOpenedForWrite(LockMode.Exclusive, LockMode.Write, LockMode.Exclusive);
        whenRefAlreadyOpenedForWrite(LockMode.Exclusive, LockMode.Exclusive, LockMode.Exclusive);
    }

    public void whenRefAlreadyOpenedForWrite(LockMode writeLockMode, LockMode readLockMode, LockMode expectedLockMode) {
        long initialValue = 10;
        GammaLongRef ref = new GammaLongRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTxn tx = newTransaction();
        GammaRefTranlocal first = ref.openForWrite(tx, writeLockMode.asInt());
        GammaRefTranlocal second = ref.openForRead(tx, readLockMode.asInt());

        assertSame(first, second);
        assertSame(ref, second.owner);
        assertEquals(initialVersion, second.version);
        assertEquals(initialValue, second.long_value);
        assertEquals(initialValue, second.long_oldValue);
        assertLockMode(ref, expectedLockMode);
        assertTrue(second.isWrite());
        assertTrue(tx.hasWrites());
    }

    @Test
    public void readConsistency_whenNotConsistent() {
        assumeTrue(getMaxCapacity() > 1);

        GammaLongRef ref1 = new GammaLongRef(stm, 0);
        GammaLongRef ref2 = new GammaLongRef(stm, 0);

        GammaTxn tx = newTransaction();
        ref1.openForRead(tx, LOCKMODE_NONE);

        ref1.atomicIncrementAndGet(1);

        try {
            ref2.openForRead(tx, LOCKMODE_NONE);
            fail();
        } catch (ReadWriteConflict expected) {
        }

        assertIsAborted(tx);
    }

    // ====================== lock level ========================================

    @Test
    public void lockLevel() {
        lockLevel(LockMode.None, LockMode.None, LockMode.None);
        lockLevel(LockMode.None, LockMode.Read, LockMode.Read);
        lockLevel(LockMode.None, LockMode.Write, LockMode.Write);
        lockLevel(LockMode.None, LockMode.Exclusive, LockMode.Exclusive);

        lockLevel(LockMode.Read, LockMode.Read, LockMode.Read);
        lockLevel(LockMode.Read, LockMode.Write, LockMode.Write);
        lockLevel(LockMode.Read, LockMode.Exclusive, LockMode.Exclusive);

        lockLevel(LockMode.Write, LockMode.Write, LockMode.Write);
        lockLevel(LockMode.Write, LockMode.Exclusive, LockMode.Exclusive);

        lockLevel(LockMode.Exclusive, LockMode.Exclusive, LockMode.Exclusive);
    }

    public void lockLevel(LockMode transactionReadLockMode, LockMode readLockMode, LockMode expectedReadLockMode) {
        long initialValue = 10;
        GammaLongRef ref = new GammaLongRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTxnConfiguration config = new GammaTxnConfiguration(stm)
                .setReadLockMode(transactionReadLockMode)
                .setWriteLockMode(transactionReadLockMode);


        GammaTxn tx = newTransaction(config);
        GammaRefTranlocal tranlocal = ref.openForRead(tx, readLockMode.asInt());

        assertEquals(expectedReadLockMode.asInt(), tranlocal.getLockMode());
        assertEquals(TRANLOCAL_READ, tranlocal.getMode());
        assertIsActive(tx);
        assertVersionAndValue(ref, initialVersion, initialValue);
        assertRefHasLockMode(ref, tx, expectedReadLockMode.asInt());
    }

    // ===================== lock upgrade ======================================

    @Test
    public void lockUpgrade() {
        lockUpgrade(LOCKMODE_NONE, LOCKMODE_NONE, LOCKMODE_NONE);
        lockUpgrade(LOCKMODE_NONE, LOCKMODE_READ, LOCKMODE_READ);
        lockUpgrade(LOCKMODE_NONE, LOCKMODE_WRITE, LOCKMODE_WRITE);
        lockUpgrade(LOCKMODE_NONE, LOCKMODE_EXCLUSIVE, LOCKMODE_EXCLUSIVE);

        lockUpgrade(LOCKMODE_READ, LOCKMODE_NONE, LOCKMODE_READ);
        lockUpgrade(LOCKMODE_READ, LOCKMODE_READ, LOCKMODE_READ);
        lockUpgrade(LOCKMODE_READ, LOCKMODE_WRITE, LOCKMODE_WRITE);
        lockUpgrade(LOCKMODE_READ, LOCKMODE_EXCLUSIVE, LOCKMODE_EXCLUSIVE);

        lockUpgrade(LOCKMODE_WRITE, LOCKMODE_NONE, LOCKMODE_WRITE);
        lockUpgrade(LOCKMODE_WRITE, LOCKMODE_READ, LOCKMODE_WRITE);
        lockUpgrade(LOCKMODE_WRITE, LOCKMODE_WRITE, LOCKMODE_WRITE);
        lockUpgrade(LOCKMODE_WRITE, LOCKMODE_EXCLUSIVE, LOCKMODE_EXCLUSIVE);

        lockUpgrade(LOCKMODE_EXCLUSIVE, LOCKMODE_NONE, LOCKMODE_EXCLUSIVE);
        lockUpgrade(LOCKMODE_EXCLUSIVE, LOCKMODE_READ, LOCKMODE_EXCLUSIVE);
        lockUpgrade(LOCKMODE_EXCLUSIVE, LOCKMODE_WRITE, LOCKMODE_EXCLUSIVE);
        lockUpgrade(LOCKMODE_EXCLUSIVE, LOCKMODE_EXCLUSIVE, LOCKMODE_EXCLUSIVE);
    }

    public void lockUpgrade(int firstMode, int secondLockMode, int expectedLockMode) {
        long initialValue = 10;
        GammaLongRef ref = new GammaLongRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTxn tx = newTransaction();
        ref.openForRead(tx, firstMode);
        GammaRefTranlocal tranlocal = ref.openForRead(tx, secondLockMode);

        assertEquals(expectedLockMode, tranlocal.getLockMode());
        assertEquals(TRANLOCAL_READ, tranlocal.getMode());
        assertIsActive(tx);
        assertVersionAndValue(ref, initialVersion, initialValue);
        assertRefHasLockMode(ref, tx, expectedLockMode);
    }

    // ===================== locking ============================================

    @Test
    public void locking_noLockRequired_whenLockedForReadByOther() {
        long intitialValue = 10;
        GammaLongRef ref = new GammaLongRef(stm, intitialValue);
        long initialVersion = ref.getVersion();

        T otherTx = newTransaction();
        ref.getLock().acquire(otherTx, LockMode.Read);

        T tx = newTransaction();
        GammaRefTranlocal tranlocal = ref.openForRead(tx, LOCKMODE_NONE);

        assertEquals(LOCKMODE_NONE, tranlocal.getLockMode());
        assertIsActive(tx);
        assertVersionAndValue(ref, initialVersion, intitialValue);
        assertRefHasReadLock(ref, otherTx);
        assertReadLockCount(ref, 1);
        assertEquals(ref, tranlocal.owner);
        assertEquals(TRANLOCAL_READ, tranlocal.getMode());
    }

    @Test
    public void locking_noLockRequired_whenLockedForWriteByOther() {
        long intitialValue = 10;
        GammaLongRef ref = new GammaLongRef(stm, intitialValue);
        long initialVersion = ref.getVersion();

        T otherTx = newTransaction();
        ref.getLock().acquire(otherTx, LockMode.Write);

        T tx = newTransaction();
        GammaRefTranlocal tranlocal = ref.openForRead(tx, LOCKMODE_NONE);

        assertEquals(LOCKMODE_NONE, tranlocal.getLockMode());
        assertIsActive(tx);
        assertVersionAndValue(ref, initialVersion, intitialValue);
        assertRefHasWriteLock(ref, otherTx);
        assertEquals(ref, tranlocal.owner);
        assertEquals(TRANLOCAL_READ, tranlocal.getMode());
    }

    @Test
    public void locking_noLockReqyired_whenLockedForCommitByOther() {
        long intitialValue = 10;
        GammaLongRef ref = new GammaLongRef(stm, intitialValue);
        long initialVersion = ref.getVersion();

        T otherTx = newTransaction();
        ref.getLock().acquire(otherTx, LockMode.Exclusive);

        T tx = newTransaction();
        try {
            ref.openForRead(tx, LOCKMODE_NONE);
            fail();
        } catch (ReadWriteConflict expected) {
        }

        assertIsAborted(tx);
        assertVersionAndValue(ref, initialVersion, intitialValue);
        assertRefHasExclusiveLock(ref, otherTx);
    }

    @Test
    public void locking_readLockRequired_whenFree() {
        long intitialValue = 10;
        GammaLongRef ref = new GammaLongRef(stm, intitialValue);
        long initialVersion = ref.getVersion();

        T tx = newTransaction();
        GammaRefTranlocal tranlocal = ref.openForRead(tx, LOCKMODE_READ);

        assertEquals(LOCKMODE_READ, tranlocal.getLockMode());
        assertVersionAndValue(ref, initialVersion, intitialValue);
        assertRefHasReadLock(ref, tx);
        assertReadLockCount(ref, 1);
        assertEquals(ref, tranlocal.owner);
        assertEquals(TRANLOCAL_READ, tranlocal.getMode());
    }

    @Test
    public void locking_readLockRequired_whenLockedForReadByOther() {
        long intitialValue = 10;
        GammaLongRef ref = new GammaLongRef(stm, intitialValue);
        long initialVersion = ref.getVersion();

        T otherTx = newTransaction();
        ref.getLock().acquire(otherTx, LockMode.Read);

        T tx = newTransaction();
        GammaRefTranlocal tranlocal = ref.openForRead(tx, LOCKMODE_READ);

        assertEquals(LOCKMODE_READ, tranlocal.getLockMode());
        assertIsActive(tx);
        assertVersionAndValue(ref, initialVersion, intitialValue);
        assertRefHasReadLock(ref, otherTx);
        assertRefHasReadLock(ref, tx);
        assertReadLockCount(ref, 2);
        assertEquals(ref, tranlocal.owner);
        assertEquals(TRANLOCAL_READ, tranlocal.getMode());
    }

    @Test
    public void locking_readLockRequired_whenLockedForWriteByOther() {
        long intitialValue = 10;
        GammaLongRef ref = new GammaLongRef(stm, intitialValue);
        long initialVersion = ref.getVersion();

        T otherTx = newTransaction();
        ref.getLock().acquire(otherTx, LockMode.Write);

        T tx = newTransaction();
        try {
            ref.openForRead(tx, LOCKMODE_READ);
            fail();
        } catch (ReadWriteConflict expected) {
        }

        assertIsAborted(tx);
        assertVersionAndValue(ref, initialVersion, intitialValue);
        assertRefHasWriteLock(ref, otherTx);
    }

    @Test
    public void locking_readLockReqyired_whenLockedForCommitByOther() {
        long intitialValue = 10;
        GammaLongRef ref = new GammaLongRef(stm, intitialValue);
        long initialVersion = ref.getVersion();

        T otherTx = newTransaction();
        ref.getLock().acquire(otherTx, LockMode.Exclusive);

        T tx = newTransaction();
        try {
            ref.openForRead(tx, LOCKMODE_READ);
            fail();
        } catch (ReadWriteConflict expected) {
        }

        assertIsAborted(tx);
        assertVersionAndValue(ref, initialVersion, intitialValue);
        assertRefHasExclusiveLock(ref, otherTx);
    }

    @Test
    public void locking_writeLockRequired_whenFree() {
        long intitialValue = 10;
        GammaLongRef ref = new GammaLongRef(stm, intitialValue);
        long initialVersion = ref.getVersion();

        T tx = newTransaction();
        GammaRefTranlocal tranlocal = ref.openForRead(tx, LOCKMODE_WRITE);

        assertEquals(LOCKMODE_WRITE, tranlocal.getLockMode());
        assertVersionAndValue(ref, initialVersion, intitialValue);
        assertRefHasWriteLock(ref, tx);
        assertEquals(ref, tranlocal.owner);
        assertEquals(TRANLOCAL_READ, tranlocal.getMode());
    }

    @Test
    public void locking_writeLockRequired_whenLockedForReadByOther() {
        long intitialValue = 10;
        GammaLongRef ref = new GammaLongRef(stm, intitialValue);
        long initialVersion = ref.getVersion();

        T otherTx = newTransaction();
        ref.getLock().acquire(otherTx, LockMode.Read);

        T tx = newTransaction();
        try {
            ref.openForRead(tx, LOCKMODE_WRITE);
            fail();
        } catch (ReadWriteConflict expected) {

        }

        assertIsAborted(tx);
        assertVersionAndValue(ref, initialVersion, intitialValue);
        assertRefHasReadLock(ref, otherTx);
    }

    @Test
    public void locking_writeLockRequired_whenLockedForWriteByOther() {
        long intitialValue = 10;
        GammaLongRef ref = new GammaLongRef(stm, intitialValue);
        long initialVersion = ref.getVersion();

        T otherTx = newTransaction();
        ref.getLock().acquire(otherTx, LockMode.Write);

        T tx = newTransaction();
        try {
            ref.openForRead(tx, LOCKMODE_WRITE);
            fail();
        } catch (ReadWriteConflict expected) {
        }

        assertIsAborted(tx);
        assertVersionAndValue(ref, initialVersion, intitialValue);
        assertRefHasWriteLock(ref, otherTx);
    }

    @Test
    public void locking_writeLockReqyired_whenLockedForCommitByOther() {
        long intitialValue = 10;
        GammaLongRef ref = new GammaLongRef(stm, intitialValue);
        long initialVersion = ref.getVersion();

        T otherTx = newTransaction();
        ref.getLock().acquire(otherTx, LockMode.Exclusive);

        T tx = newTransaction();
        try {
            ref.openForRead(tx, LOCKMODE_WRITE);
            fail();
        } catch (ReadWriteConflict expected) {
        }

        assertIsAborted(tx);
        assertVersionAndValue(ref, initialVersion, intitialValue);
        assertRefHasExclusiveLock(ref, otherTx);
    }

    @Test
    public void locking_exclusiveLockRequired_whenFree() {
        long intitialValue = 10;
        GammaLongRef ref = new GammaLongRef(stm, intitialValue);
        long initialVersion = ref.getVersion();

        T tx = newTransaction();
        GammaRefTranlocal tranlocal = ref.openForRead(tx, LOCKMODE_EXCLUSIVE);

        assertEquals(LOCKMODE_EXCLUSIVE, tranlocal.getLockMode());
        assertVersionAndValue(ref, initialVersion, intitialValue);
        assertRefHasExclusiveLock(ref, tx);
        assertEquals(ref, tranlocal.owner);
        assertEquals(TRANLOCAL_READ, tranlocal.getMode());
    }

    @Test
    public void locking_exclusiveLockRequired_whenLockedForReadByOther() {
        long intitialValue = 10;
        GammaLongRef ref = new GammaLongRef(stm, intitialValue);
        long initialVersion = ref.getVersion();

        T otherTx = newTransaction();
        ref.getLock().acquire(otherTx, LockMode.Read);

        T tx = newTransaction();
        try {
            ref.openForRead(tx, LOCKMODE_EXCLUSIVE);
            fail();
        } catch (ReadWriteConflict expected) {
        }

        assertIsAborted(tx);
        assertVersionAndValue(ref, initialVersion, intitialValue);
        assertRefHasReadLock(ref, otherTx);
        assertReadLockCount(ref, 1);
    }

    @Test
    public void locking_exclusiveLockRequired_whenLockedForWriteByOther() {
        long intitialValue = 10;
        GammaLongRef ref = new GammaLongRef(stm, intitialValue);
        long initialVersion = ref.getVersion();

        T otherTx = newTransaction();
        ref.getLock().acquire(otherTx, LockMode.Write);

        T tx = newTransaction();
        try {
            ref.openForRead(tx, LOCKMODE_EXCLUSIVE);
            fail();
        } catch (ReadWriteConflict expected) {
        }

        assertIsAborted(tx);
        assertVersionAndValue(ref, initialVersion, intitialValue);
        assertRefHasWriteLock(ref, otherTx);
    }

    @Test
    public void locking_exclusiveLockReqyired_whenLockedForCommitByOther() {
        long intitialValue = 10;
        GammaLongRef ref = new GammaLongRef(stm, intitialValue);
        long initialVersion = ref.getVersion();

        T otherTx = newTransaction();
        ref.getLock().acquire(otherTx, LockMode.Exclusive);

        T tx = newTransaction();
        try {
            ref.openForRead(tx, LOCKMODE_EXCLUSIVE);
            fail();
        } catch (ReadWriteConflict expected) {
        }

        assertIsAborted(tx);
        assertVersionAndValue(ref, initialVersion, intitialValue);
        assertRefHasExclusiveLock(ref, otherTx);
    }

    // =================== while commuting ============================

    @Test
    public void commuting_whenCommuting_thenFailure() {
        long initialValue = 10;
        GammaLongRef ref = new GammaLongRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        T tx = newTransaction();
        tx.evaluatingCommute = true;

        try {
            ref.openForRead(tx, LOCKMODE_NONE);
            fail();
        } catch (IllegalCommuteException expected) {
        }

        assertIsAborted(tx);
        assertVersionAndValue(ref, initialVersion, initialValue);
        assertRefHasNoLocks(ref);
    }

    // ================================================================

    @Test
    public void whenRepeatableReadIsolationLevel(){
         assumeTrue(getMaxCapacity()>1);

        long initialValue = 1;
        GammaLongRef ref1 = new GammaLongRef(stm, initialValue);
        GammaLongRef ref2 = new GammaLongRef(stm, initialValue);

        GammaTxnConfiguration config = new GammaTxnConfiguration(stm)
                .setIsolationLevel(IsolationLevel.RepeatableRead);
        T tx = newTransaction(config);

        ref1.get(tx);
        ref1.atomicIncrementAndGet(1);
        long value = ref2.get(tx);

        assertIsActive(tx);
        assertEquals(1, value);
    }

    // ================================================================

    @Test
    public void whenTransactionPrepared_thenPreparedTransactionException() {
        GammaTxn tx = newTransaction();
        tx.prepare();

        long initialValue = 10;
        GammaLongRef ref = new GammaLongRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        try {
            ref.openForRead(tx, LOCKMODE_NONE);
            fail();
        } catch (PreparedTransactionException expected) {
        }

        assertIsAborted(tx);
        assertEquals(initialValue, ref.long_value);
        assertEquals(initialVersion, ref.version);
    }

    @Test
    public void state_whenTransactionAlreadyAborted_thenDeadTransactionException() {
        GammaTxn tx = newTransaction();
        tx.abort();

        long initialValue = 10;
        GammaLongRef ref = new GammaLongRef(stm, initialValue);
        long initialVersion = ref.getVersion();


        try {
            ref.openForRead(tx, LOCKMODE_NONE);
            fail();
        } catch (DeadTransactionException expected) {
        }

        assertEquals(initialValue, ref.long_value);
        assertEquals(initialVersion, ref.version);
    }

    @Test
    public void state_whenTransactionAlreadyCommitted_thenDeadTransactionException() {
        GammaTxn tx = newTransaction();
        tx.commit();

        long initialValue = 10;
        GammaLongRef ref = new GammaLongRef(stm, initialValue);
        long initialVersion = ref.getVersion();


        try {
            ref.openForRead(tx, LOCKMODE_NONE);
            fail();
        } catch (DeadTransactionException expected) {
        }

        assertEquals(initialValue, ref.long_value);
        assertEquals(initialVersion, ref.version);
    }
}
