package org.multiverse.stms.gamma.transactionalobjects.refs;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.multiverse.api.LockMode;
import org.multiverse.stms.gamma.GammaConstants;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactionalobjects.GammaTxnLong;
import org.multiverse.stms.gamma.transactionalobjects.Tranlocal;
import org.multiverse.stms.gamma.transactions.GammaTxn;

import static org.junit.Assert.*;
import static org.multiverse.stms.gamma.GammaTestUtils.*;

public class TryLockAndCheckConflictTest implements GammaConstants {

    private GammaStm stm;

    @Before
    public void setUp() {
        stm = new GammaStm();
    }

   //conflicts

    @Test
    public void writeBiased_whenOtherHasLocked() {
        writeBiased_whenOtherHasLocked(LockMode.Read, LockMode.None, true);
        writeBiased_whenOtherHasLocked(LockMode.Read, LockMode.Read, true);
        writeBiased_whenOtherHasLocked(LockMode.Read, LockMode.Write, false);
        writeBiased_whenOtherHasLocked(LockMode.Read, LockMode.Exclusive, false);

        writeBiased_whenOtherHasLocked(LockMode.Write, LockMode.None, true);
        writeBiased_whenOtherHasLocked(LockMode.Write, LockMode.Read, false);
        writeBiased_whenOtherHasLocked(LockMode.Write, LockMode.Write, false);
        writeBiased_whenOtherHasLocked(LockMode.Write, LockMode.Exclusive, false);

        //write_whenOtherHasLocked(LockMode.Exclusive, LockMode.None, false);
        writeBiased_whenOtherHasLocked(LockMode.Exclusive, LockMode.Read, false);
        writeBiased_whenOtherHasLocked(LockMode.Exclusive, LockMode.Write, false);
        writeBiased_whenOtherHasLocked(LockMode.Exclusive, LockMode.Exclusive, false);
    }

    public void writeBiased_whenOtherHasLocked(LockMode otherLockMode, LockMode thisLockMode, boolean success) {
        GammaTxnLong ref = new GammaTxnLong(stm);
        //tx.arriveEnabled = arriveNeeded;
        Tranlocal tranlocal = ref.openForRead(stm.newDefaultTxn(), LOCKMODE_NONE);

        ref.openForRead(stm.newDefaultTxn(), otherLockMode.asInt());


        //todo: null transaction
        boolean result = ref.tryLockAndCheckConflict(null, tranlocal, 1, thisLockMode.asInt());

        assertEquals(success, result);
        //assertEquals(expectedLockMode.asInt(), tranlocal.getLockMode());
        //assertLockMode(ref, expectedLockMode);
        //assertSurplus(ref, expectedSurplus);
    }

    @Test
    public void writeBiased_whenLockFreeAndArriveNeeded() {
        writeBiased(true, LockMode.None, LockMode.None, LockMode.None, 1);
        writeBiased(true, LockMode.None, LockMode.Read, LockMode.Read, 1);
        writeBiased(true, LockMode.None, LockMode.Write, LockMode.Write, 1);
        writeBiased(true, LockMode.None, LockMode.Exclusive, LockMode.Exclusive, 1);

        writeBiased(true, LockMode.Read, LockMode.None, LockMode.Read, 1);
        writeBiased(true, LockMode.Read, LockMode.Read, LockMode.Read, 1);
        writeBiased(true, LockMode.Read, LockMode.Write, LockMode.Write, 1);
        writeBiased(true, LockMode.Read, LockMode.Exclusive, LockMode.Exclusive, 1);

        writeBiased(true, LockMode.Write, LockMode.None, LockMode.Write, 1);
        writeBiased(true, LockMode.Write, LockMode.Read, LockMode.Write, 1);
        writeBiased(true, LockMode.Write, LockMode.Write, LockMode.Write, 1);
        writeBiased(true, LockMode.Write, LockMode.Exclusive, LockMode.Exclusive, 1);

        writeBiased(true, LockMode.Exclusive, LockMode.None, LockMode.Exclusive, 1);
        writeBiased(true, LockMode.Exclusive, LockMode.Read, LockMode.Exclusive, 1);
        writeBiased(true, LockMode.Exclusive, LockMode.Write, LockMode.Exclusive, 1);
        writeBiased(true, LockMode.Exclusive, LockMode.Exclusive, LockMode.Exclusive, 1);
    }

    @Test
    public void writeBiased_whenLockFreeAndNoArriveNeeded() {
        writeBiased(false, LockMode.None, LockMode.None, LockMode.None, 0);
        writeBiased(false, LockMode.None, LockMode.Read, LockMode.Read, 1);
        writeBiased(false, LockMode.None, LockMode.Write, LockMode.Write, 1);
        writeBiased(false, LockMode.None, LockMode.Exclusive, LockMode.Exclusive, 1);

        writeBiased(false, LockMode.Read, LockMode.None, LockMode.Read, 1);
        writeBiased(false, LockMode.Read, LockMode.Read, LockMode.Read, 1);
        writeBiased(false, LockMode.Read, LockMode.Write, LockMode.Write, 1);
        writeBiased(false, LockMode.Read, LockMode.Exclusive, LockMode.Exclusive, 1);

        writeBiased(false, LockMode.Write, LockMode.None, LockMode.Write, 1);
        writeBiased(false, LockMode.Write, LockMode.Read, LockMode.Write, 1);
        writeBiased(false, LockMode.Write, LockMode.Write, LockMode.Write, 1);
        writeBiased(false, LockMode.Write, LockMode.Exclusive, LockMode.Exclusive, 1);

        writeBiased(false, LockMode.Exclusive, LockMode.None, LockMode.Exclusive, 1);
        writeBiased(false, LockMode.Exclusive, LockMode.Read, LockMode.Exclusive, 1);
        writeBiased(false, LockMode.Exclusive, LockMode.Write, LockMode.Exclusive, 1);
        writeBiased(false, LockMode.Exclusive, LockMode.Exclusive, LockMode.Exclusive, 1);
    }

    public void writeBiased(boolean arriveNeeded, LockMode firstLockMode, LockMode secondLockMode,
                            LockMode expectedLockMode, int expectedSurplus) {
        GammaTxnLong ref = new GammaTxnLong(stm);
        GammaTxn tx = stm.newDefaultTxn();
        tx.richmansMansConflictScan = arriveNeeded;
        Tranlocal tranlocal = ref.openForRead(tx, firstLockMode.asInt());

        boolean result = ref.tryLockAndCheckConflict(tx, tranlocal, 1, secondLockMode.asInt());

        assertTrue(result);
        assertEquals(expectedLockMode.asInt(), tranlocal.getLockMode());
        assertLockMode(ref, expectedLockMode);
        assertSurplus(ref, expectedSurplus);
    }

    @Test
    public void readBiased_whenLockFree() {
        readBiased(LockMode.None, LockMode.None, LockMode.None);
        readBiased(LockMode.None, LockMode.Read, LockMode.Read);
        readBiased(LockMode.None, LockMode.Write, LockMode.Write);
        readBiased(LockMode.None, LockMode.Exclusive, LockMode.Exclusive);

        readBiased(LockMode.Read, LockMode.None, LockMode.Read);
        readBiased(LockMode.Read, LockMode.Read, LockMode.Read);
        readBiased(LockMode.Read, LockMode.Write, LockMode.Write);
        readBiased(LockMode.Read, LockMode.Exclusive, LockMode.Exclusive);

        readBiased(LockMode.Write, LockMode.None, LockMode.Write);
        readBiased(LockMode.Write, LockMode.Read, LockMode.Write);
        readBiased(LockMode.Write, LockMode.Write, LockMode.Write);
        readBiased(LockMode.Write, LockMode.Exclusive, LockMode.Exclusive);

        readBiased(LockMode.Exclusive, LockMode.None, LockMode.Exclusive);
        readBiased(LockMode.Exclusive, LockMode.Read, LockMode.Exclusive);
        readBiased(LockMode.Exclusive, LockMode.Write, LockMode.Exclusive);
        readBiased(LockMode.Exclusive, LockMode.Exclusive, LockMode.Exclusive);
    }

    public void readBiased(LockMode firstLockMode, LockMode secondLockMode, LockMode expectedLockMode) {
        GammaTxnLong ref = makeReadBiased(new GammaTxnLong(stm));
        GammaTxn tx = stm.newDefaultTxn();
        tx.richmansMansConflictScan = true;
        Tranlocal tranlocal = ref.openForRead(tx, firstLockMode.asInt());

        boolean result = ref.tryLockAndCheckConflict(tx, tranlocal, 1, secondLockMode.asInt());

        assertTrue(result);
        assertFalse(tranlocal.hasDepartObligation());
        assertEquals(expectedLockMode.asInt(), tranlocal.getLockMode());
        assertLockMode(ref, expectedLockMode);
        assertSurplus(ref, 1);
    }

    @Test
    @Ignore
    public void lockNotFree() {

    }

     // ===================== lock free ==================================

    @Test
    public void lockFree_tryNoneLock() {
        long initialValue = 10;
        GammaTxnLong ref = new GammaTxnLong(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTxn tx = stm.newDefaultTxn();
        Tranlocal tranlocal = ref.openForRead(tx, LOCKMODE_NONE);
        boolean result = ref.tryLockAndCheckConflict(tx,tranlocal, 1, LOCKMODE_NONE);

        assertTrue(result);
        assertFalse(tranlocal.hasDepartObligation());
        assertEquals(LOCKMODE_NONE, tranlocal.getLockMode());
        assertRefHasNoLocks(ref);
        assertVersionAndValue(ref, initialVersion, initialValue);
        assertSurplus(ref, 0);
    }

    @Test
    public void lockFree_tryReadLock() {
        long initialValue = 10;
        GammaTxnLong ref = new GammaTxnLong(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTxn tx = stm.newDefaultTxn();
        Tranlocal tranlocal = ref.openForRead(tx, LOCKMODE_NONE);
        boolean result = ref.tryLockAndCheckConflict(tx,tranlocal, 1, LOCKMODE_READ);

        assertTrue(result);
        assertTrue(tranlocal.hasDepartObligation());
        assertEquals(LOCKMODE_READ, tranlocal.getLockMode());
        assertRefHasReadLock(ref, tx);
        assertReadLockCount(ref, 1);
        assertVersionAndValue(ref, initialVersion, initialValue);
        assertSurplus(ref, 1);
    }

    @Test
    public void lockFree_tryWriteLock() {
        long initialValue = 10;
        GammaTxnLong ref = new GammaTxnLong(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTxn tx = stm.newDefaultTxn();
        Tranlocal tranlocal = ref.openForRead(tx, LOCKMODE_NONE);
        boolean result = ref.tryLockAndCheckConflict(tx,tranlocal, 1, LOCKMODE_WRITE);

        assertTrue(result);
        assertTrue(tranlocal.hasDepartObligation());
        assertEquals(LOCKMODE_WRITE, tranlocal.getLockMode());
        assertRefHasWriteLock(ref, tx);
        assertVersionAndValue(ref, initialVersion, initialValue);
        assertSurplus(ref, 1);
    }

    @Test
    public void lockFree_tryExclusiveLock() {
        long initialValue = 10;
        GammaTxnLong ref = new GammaTxnLong(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTxn tx = stm.newDefaultTxn();
        Tranlocal tranlocal = ref.openForRead(tx, LOCKMODE_NONE);
        boolean result = ref.tryLockAndCheckConflict(tx,tranlocal, 1, LOCKMODE_EXCLUSIVE);

        assertTrue(result);
        assertTrue(tranlocal.hasDepartObligation());
        assertEquals(LOCKMODE_EXCLUSIVE, tranlocal.getLockMode());
        assertRefHasExclusiveLock(ref, tx);
        assertVersionAndValue(ref, initialVersion, initialValue);
        assertSurplus(ref, 1);
    }

    // ==================== lock upgrade ========================

    @Test
    public void lockUpgrade_readLockAcquired_tryNoLock() {
        long initialValue = 10;
        GammaTxnLong ref = new GammaTxnLong(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTxn tx = stm.newDefaultTxn();
        Tranlocal tranlocal = ref.openForRead(tx, LOCKMODE_READ);
        boolean result = ref.tryLockAndCheckConflict(tx,tranlocal, 1, LOCKMODE_NONE);

        assertTrue(result);
        assertTrue(tranlocal.hasDepartObligation());
        assertEquals(LOCKMODE_READ, tranlocal.getLockMode());
        assertRefHasReadLock(ref, tx);
        assertReadLockCount(ref, 1);
        assertVersionAndValue(ref, initialVersion, initialValue);
        assertSurplus(ref, 1);
    }

    @Test
    public void lockUpgrade_readLockAcquired_tryReadLock() {
        long initialValue = 10;
        GammaTxnLong ref = new GammaTxnLong(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTxn tx = stm.newDefaultTxn();
        Tranlocal tranlocal = ref.openForRead(tx, LOCKMODE_READ);
        boolean result = ref.tryLockAndCheckConflict(tx,tranlocal, 1, LOCKMODE_READ);

        assertTrue(result);
        assertTrue(tranlocal.hasDepartObligation());
        assertEquals(LOCKMODE_READ, tranlocal.getLockMode());
        assertRefHasReadLock(ref, tx);
        assertReadLockCount(ref, 1);
        assertVersionAndValue(ref, initialVersion, initialValue);
        assertSurplus(ref, 1);
    }

    @Test
    public void lockUpgrade_readLockAcquired_otherTransactionAlreadyAcquiredReadLock_tryReadLock() {
        long initialValue = 10;
        GammaTxnLong ref = new GammaTxnLong(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTxn tx = stm.newDefaultTxn();
        Tranlocal tranlocal = ref.openForRead(tx, LOCKMODE_READ);

        GammaTxn otherTx = stm.newDefaultTxn();
        ref.getLock().acquire(otherTx, LockMode.Read);

        boolean result = ref.tryLockAndCheckConflict(tx,tranlocal, 1, LOCKMODE_READ);

        assertTrue(result);
        assertTrue(tranlocal.hasDepartObligation());
        assertEquals(LOCKMODE_READ, tranlocal.getLockMode());
        assertRefHasReadLock(ref, tx);
        assertReadLockCount(ref, 2);
        assertVersionAndValue(ref, initialVersion, initialValue);
        assertSurplus(ref, 2);
    }

    @Test
    public void lockUpgrade_readLockAcquired_tryWriteLock() {
        long initialValue = 10;
        GammaTxnLong ref = new GammaTxnLong(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTxn tx = stm.newDefaultTxn();
        Tranlocal tranlocal = ref.openForRead(tx, LOCKMODE_READ);
        boolean result = ref.tryLockAndCheckConflict(tx,tranlocal, 1, LOCKMODE_WRITE);

        assertTrue(result);

        assertTrue(tranlocal.hasDepartObligation());
        assertEquals(LOCKMODE_WRITE, tranlocal.getLockMode());
        assertRefHasWriteLock(ref, tx);
        assertVersionAndValue(ref, initialVersion, initialValue);
        assertSurplus(ref, 1);
    }

    @Test
    public void lockUpgrade_readLockAcquired_otherTransactionAlsoAcquiredReadLock_tryWriteLock() {
        long initialValue = 10;
        GammaTxnLong ref = new GammaTxnLong(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTxn tx = stm.newDefaultTxn();
        Tranlocal tranlocal = ref.openForRead(tx, LOCKMODE_READ);

        GammaTxn otherTx = stm.newDefaultTxn();
        ref.getLock().acquire(otherTx, LockMode.Read);

        boolean result = ref.tryLockAndCheckConflict(tx,tranlocal, 1, LOCKMODE_WRITE);

        assertFalse(result);
        assertTrue(tranlocal.hasDepartObligation());
        assertEquals(LOCKMODE_READ, tranlocal.getLockMode());
        assertRefHasReadLock(ref, tx);
        assertReadLockCount(ref, 2);
        assertVersionAndValue(ref, initialVersion, initialValue);
        assertSurplus(ref, 2);
    }

    @Test
    public void lockUpgrade_readLockAcquired_tryExclusiveLock() {
        long initialValue = 10;
        GammaTxnLong ref = new GammaTxnLong(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTxn tx = stm.newDefaultTxn();
        Tranlocal tranlocal = ref.openForRead(tx, LOCKMODE_READ);
        boolean result = ref.tryLockAndCheckConflict(tx,tranlocal, 1, LOCKMODE_EXCLUSIVE);

        assertTrue(result);
        assertTrue(tranlocal.hasDepartObligation());
        assertEquals(LOCKMODE_EXCLUSIVE, tranlocal.getLockMode());
        assertRefHasExclusiveLock(ref, tx);
        assertVersionAndValue(ref, initialVersion, initialValue);
        assertSurplus(ref, 1);
    }

    @Test
    public void lockUpgrade_readLockAcquired_otherTransactionAlsoAcquiredReadLock_tryExclusiveLock() {
        long initialValue = 10;
        GammaTxnLong ref = new GammaTxnLong(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTxn tx = stm.newDefaultTxn();
        Tranlocal tranlocal = ref.openForRead(tx, LOCKMODE_READ);

        GammaTxn otherTx = stm.newDefaultTxn();
        ref.getLock().acquire(otherTx, LockMode.Read);

        boolean result = ref.tryLockAndCheckConflict(tx,tranlocal, 1, LOCKMODE_EXCLUSIVE);

        assertFalse(result);
        assertTrue(tranlocal.hasDepartObligation());
        assertEquals(LOCKMODE_READ, tranlocal.getLockMode());
        assertRefHasReadLock(ref, tx);
        assertReadLockCount(ref, 2);
        assertVersionAndValue(ref, initialVersion, initialValue);
        assertSurplus(ref, 2);
    }

    @Test
    public void lockUpgrade_writeLockAcquired_tryNoLock() {
        long initialValue = 10;
        GammaTxnLong ref = new GammaTxnLong(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTxn tx = stm.newDefaultTxn();
        Tranlocal tranlocal = ref.openForRead(tx, LOCKMODE_WRITE);
        boolean result = ref.tryLockAndCheckConflict(tx,tranlocal, 1, LOCKMODE_NONE);

        assertTrue(result);
        assertTrue(tranlocal.hasDepartObligation());
        assertEquals(LOCKMODE_WRITE, tranlocal.getLockMode());
        assertRefHasWriteLock(ref, tx);
        assertVersionAndValue(ref, initialVersion, initialValue);
        assertSurplus(ref, 1);
    }

    @Test
    public void lockUpgrade_writeLockAcquired_tryReadLock() {
        long initialValue = 10;
        GammaTxnLong ref = new GammaTxnLong(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTxn tx = stm.newDefaultTxn();
        Tranlocal tranlocal = ref.openForRead(tx, LOCKMODE_WRITE);
        boolean result = ref.tryLockAndCheckConflict(tx,tranlocal, 1, LOCKMODE_READ);

        assertTrue(result);
        assertTrue(tranlocal.hasDepartObligation());
        assertEquals(LOCKMODE_WRITE, tranlocal.getLockMode());
        assertRefHasWriteLock(ref, tx);
        assertVersionAndValue(ref, initialVersion, initialValue);
        assertSurplus(ref, 1);
    }

    @Test
    public void lockAcquired_writeLockAcquired_tryWriteLock() {
        long initialValue = 10;
        GammaTxnLong ref = new GammaTxnLong(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTxn tx = stm.newDefaultTxn();
        Tranlocal tranlocal = ref.openForRead(tx, LOCKMODE_WRITE);
        boolean result = ref.tryLockAndCheckConflict(tx,tranlocal, 1, LOCKMODE_WRITE);

        assertTrue(result);
        assertTrue(tranlocal.hasDepartObligation());
        assertEquals(LOCKMODE_WRITE, tranlocal.getLockMode());
        assertRefHasWriteLock(ref, tx);
        assertVersionAndValue(ref, initialVersion, initialValue);
        assertSurplus(ref, 1);
    }

    @Test
    public void lockAcquired_writeLockAcquired_tryExclusiveLock() {
        long initialValue = 10;
        GammaTxnLong ref = new GammaTxnLong(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTxn tx = stm.newDefaultTxn();
        Tranlocal tranlocal = ref.openForRead(tx, LOCKMODE_WRITE);
        boolean result = ref.tryLockAndCheckConflict(tx,tranlocal, 1, LOCKMODE_EXCLUSIVE);

        assertTrue(result);
        assertTrue(tranlocal.hasDepartObligation());
        assertEquals(LOCKMODE_EXCLUSIVE, tranlocal.getLockMode());
        assertRefHasExclusiveLock(ref, tx);
        assertVersionAndValue(ref, initialVersion, initialValue);
        assertSurplus(ref, 1);
    }

    @Test
    public void lockUpgrade_exclusiveLockAcquired_tryNoLock() {
        long initialValue = 10;
        GammaTxnLong ref = new GammaTxnLong(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTxn tx = stm.newDefaultTxn();
        Tranlocal tranlocal = ref.openForRead(tx, LOCKMODE_EXCLUSIVE);
        boolean result = ref.tryLockAndCheckConflict(tx,tranlocal, 1, LOCKMODE_NONE);

        assertTrue(result);
        assertTrue(tranlocal.hasDepartObligation());
        assertEquals(LOCKMODE_EXCLUSIVE, tranlocal.getLockMode());
        assertRefHasExclusiveLock(ref, tx);
        assertVersionAndValue(ref, initialVersion, initialValue);
        assertSurplus(ref, 1);
    }

    @Test
    public void lockUpgrade_exclusiveLockAcquired_tryReadLock() {
        long initialValue = 10;
        GammaTxnLong ref = new GammaTxnLong(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTxn tx = stm.newDefaultTxn();
        Tranlocal tranlocal = ref.openForRead(tx, LOCKMODE_EXCLUSIVE);
        boolean result = ref.tryLockAndCheckConflict(tx,tranlocal, 1, LOCKMODE_READ);

        assertTrue(result);
        assertTrue(tranlocal.hasDepartObligation());
        assertEquals(LOCKMODE_EXCLUSIVE, tranlocal.getLockMode());
        assertRefHasExclusiveLock(ref, tx);
        assertVersionAndValue(ref, initialVersion, initialValue);
        assertSurplus(ref, 1);
    }

    @Test
    public void lockAcquired_exclusiveLockAcquired_tryWriteLock() {
        long initialValue = 10;
        GammaTxnLong ref = new GammaTxnLong(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTxn tx = stm.newDefaultTxn();
        Tranlocal tranlocal = ref.openForRead(tx, LOCKMODE_EXCLUSIVE);
        boolean result = ref.tryLockAndCheckConflict(tx,tranlocal, 1, LOCKMODE_WRITE);

        assertTrue(result);
        assertTrue(tranlocal.hasDepartObligation());
        assertEquals(LOCKMODE_EXCLUSIVE, tranlocal.getLockMode());
        assertRefHasExclusiveLock(ref, tx);
        assertVersionAndValue(ref, initialVersion, initialValue);
        assertSurplus(ref, 1);
    }

    @Test
    public void lockAcquired_exclusiveLockAcquired_tryExclusiveLock() {
        long initialValue = 10;
        GammaTxnLong ref = new GammaTxnLong(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTxn tx = stm.newDefaultTxn();
        Tranlocal tranlocal = ref.openForRead(tx, LOCKMODE_EXCLUSIVE);
        boolean result = ref.tryLockAndCheckConflict(tx,tranlocal, 1, LOCKMODE_EXCLUSIVE);

        assertTrue(result);
        assertTrue(tranlocal.hasDepartObligation());
        assertEquals(LOCKMODE_EXCLUSIVE, tranlocal.getLockMode());
        assertRefHasExclusiveLock(ref, tx);
        assertVersionAndValue(ref, initialVersion, initialValue);
        assertSurplus(ref, 1);
    }

    // ===================== lock free ==================================

    @Test
    public void lockFreeButConflictingUpdate_tryNoneLock() {
        long initialValue = 10;
        GammaTxnLong ref = new GammaTxnLong(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTxn tx = stm.newDefaultTxn();
        Tranlocal tranlocal = ref.openForRead(tx, LOCKMODE_NONE);

        ref.atomicIncrementAndGet(1);

        boolean result = ref.tryLockAndCheckConflict(tx,tranlocal, 1, LOCKMODE_NONE);

        assertTrue(result);
        assertFalse(tranlocal.hasDepartObligation());
        assertEquals(LOCKMODE_NONE, tranlocal.getLockMode());
        assertRefHasNoLocks(ref);
        assertVersionAndValue(ref, initialVersion + 1, initialValue + 1);
        assertSurplus(ref, 0);
    }

    @Test
    public void lockFreeButConflictingUpdate__tryReadLock() {
        long initialValue = 10;
        GammaTxnLong ref = new GammaTxnLong(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTxn tx = stm.newDefaultTxn();
        Tranlocal tranlocal = ref.openForRead(tx, LOCKMODE_NONE);

        ref.atomicIncrementAndGet(1);

        boolean result = ref.tryLockAndCheckConflict(tx,tranlocal, 1, LOCKMODE_READ);

        assertFalse(result);
        assertFalse(tranlocal.hasDepartObligation());
        assertEquals(LOCKMODE_NONE, tranlocal.getLockMode());
        assertRefHasNoLocks(ref);
        assertVersionAndValue(ref, initialVersion + 1, initialValue + 1);
        assertSurplus(ref, 0);
    }

    @Test
    public void lockFreeButConflictingUpdate__tryWriteLock() {
        long initialValue = 10;
        GammaTxnLong ref = new GammaTxnLong(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTxn tx = stm.newDefaultTxn();
        Tranlocal tranlocal = ref.openForRead(tx, LOCKMODE_NONE);

        ref.atomicIncrementAndGet(1);

        boolean result = ref.tryLockAndCheckConflict(tx,tranlocal, 1, LOCKMODE_WRITE);

        assertFalse(result);
        assertFalse(tranlocal.hasDepartObligation());
        assertEquals(LOCKMODE_NONE, tranlocal.getLockMode());
        assertRefHasNoLocks(ref);
        assertVersionAndValue(ref, initialVersion + 1, initialValue + 1);
        assertSurplus(ref, 0);
    }

    @Test
    public void lockFreeButConflictingUpdate__tryExclusiveLock() {
        long initialValue = 10;
        GammaTxnLong ref = new GammaTxnLong(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTxn tx = stm.newDefaultTxn();
        Tranlocal tranlocal = ref.openForRead(tx, LOCKMODE_NONE);

        ref.atomicIncrementAndGet(1);

        boolean result = ref.tryLockAndCheckConflict(tx,tranlocal, 1, LOCKMODE_EXCLUSIVE);

        assertFalse(result);
        assertFalse(tranlocal.hasDepartObligation());
        assertEquals(LOCKMODE_NONE, tranlocal.getLockMode());
        assertRefHasNoLocks(ref);
        assertVersionAndValue(ref, initialVersion + 1, initialValue + 1);
        assertSurplus(ref, 0);
    }

    // ===================== lock not free ==================================

    @Test
    public void lockNotFree_readLockAcquired_acquireNone() {
        long initialValue = 10;
        GammaTxnLong ref = new GammaTxnLong(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTxn tx = stm.newDefaultTxn();
        Tranlocal tranlocal = ref.openForRead(tx, LOCKMODE_NONE);

        GammaTxn otherTx = stm.newDefaultTxn();
        ref.getLock().acquire(otherTx, LockMode.Read);

        boolean result = ref.tryLockAndCheckConflict(tx,tranlocal, 1, LOCKMODE_NONE);

        assertTrue(result);
        assertFalse(tranlocal.hasDepartObligation());
        assertEquals(LOCKMODE_NONE, tranlocal.getLockMode());
        assertRefHasReadLock(ref, otherTx);
        assertReadLockCount(ref, 1);
        assertVersionAndValue(ref, initialVersion, initialValue);
        assertSurplus(ref, 1);
    }

    @Test
    public void lockNotFree_readLockAcquired_acquireReadLock() {
        long initialValue = 10;
        GammaTxnLong ref = new GammaTxnLong(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTxn tx = stm.newDefaultTxn();
        Tranlocal tranlocal = ref.openForRead(tx, LOCKMODE_NONE);

        GammaTxn otherTx = stm.newDefaultTxn();
        ref.getLock().acquire(otherTx, LockMode.Read);

        boolean result = ref.tryLockAndCheckConflict(tx,tranlocal, 1, LOCKMODE_READ);

        assertTrue(result);
        assertTrue(tranlocal.hasDepartObligation());
        assertEquals(LOCKMODE_READ, tranlocal.getLockMode());
        assertRefHasReadLock(ref, tx);
        assertReadLockCount(ref, 2);
        assertVersionAndValue(ref, initialVersion, initialValue);
        assertSurplus(ref, 2);
    }

    @Test
    public void lockNotFree_readLockAcquired_acquireWriteLock() {
        long initialValue = 10;
        GammaTxnLong ref = new GammaTxnLong(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTxn tx = stm.newDefaultTxn();
        Tranlocal tranlocal = ref.openForRead(tx, LOCKMODE_NONE);

        GammaTxn otherTx = stm.newDefaultTxn();
        ref.getLock().acquire(otherTx, LockMode.Read);

        boolean result = ref.tryLockAndCheckConflict(tx,tranlocal, 1, LOCKMODE_WRITE);

        assertFalse(result);
        assertFalse(tranlocal.hasDepartObligation());
        assertEquals(LOCKMODE_NONE, tranlocal.getLockMode());
        assertRefHasReadLock(ref, otherTx);
        assertReadLockCount(ref, 1);
        assertVersionAndValue(ref, initialVersion, initialValue);
        assertSurplus(ref, 1);
    }

    @Test
    public void lockNotFree_readLockAcquired_acquireExclusiveLock() {
        long initialValue = 10;
        GammaTxnLong ref = new GammaTxnLong(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTxn tx = stm.newDefaultTxn();
        Tranlocal tranlocal = ref.openForRead(tx, LOCKMODE_NONE);

        GammaTxn otherTx = stm.newDefaultTxn();
        ref.getLock().acquire(otherTx, LockMode.Read);

        boolean result = ref.tryLockAndCheckConflict(tx,tranlocal, 1, LOCKMODE_EXCLUSIVE);

        assertFalse(result);
        assertFalse(tranlocal.hasDepartObligation());
        assertEquals(LOCKMODE_NONE, tranlocal.getLockMode());
        assertRefHasReadLock(ref, otherTx);
        assertReadLockCount(ref, 1);
        assertVersionAndValue(ref, initialVersion, initialValue);
        assertSurplus(ref, 1);
    }

    @Test
    public void lockNotFree_writeLockAcquired_acquireNoLock() {
        long initialValue = 10;
        GammaTxnLong ref = new GammaTxnLong(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTxn tx = stm.newDefaultTxn();
        Tranlocal tranlocal = ref.openForRead(tx, LOCKMODE_NONE);

        GammaTxn otherTx = stm.newDefaultTxn();
        ref.getLock().acquire(otherTx, LockMode.Write);

        boolean result = ref.tryLockAndCheckConflict(tx,tranlocal, 1, LOCKMODE_NONE);

        assertTrue(result);
        assertFalse(tranlocal.hasDepartObligation());
        assertEquals(LOCKMODE_NONE, tranlocal.getLockMode());
        assertRefHasWriteLock(ref, otherTx);
        assertVersionAndValue(ref, initialVersion, initialValue);
        assertSurplus(ref, 1);
    }

    @Test
    public void lockNotFree_writeLockAcquired_acquireReadLock() {
        long initialValue = 10;
        GammaTxnLong ref = new GammaTxnLong(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTxn tx = stm.newDefaultTxn();
        Tranlocal tranlocal = ref.openForRead(tx, LOCKMODE_NONE);

        GammaTxn otherTx = stm.newDefaultTxn();
        ref.getLock().acquire(otherTx, LockMode.Write);

        boolean result = ref.tryLockAndCheckConflict(tx,tranlocal, 1, LOCKMODE_READ);

        assertFalse(result);
        assertFalse(tranlocal.hasDepartObligation());
        assertEquals(LOCKMODE_NONE, tranlocal.getLockMode());
        assertRefHasWriteLock(ref, otherTx);
        assertVersionAndValue(ref, initialVersion, initialValue);
        assertSurplus(ref, 1);
    }

    @Test
    public void lockNotFree_writeLockAcquired_acquireWriteLock() {
        long initialValue = 10;
        GammaTxnLong ref = new GammaTxnLong(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTxn tx = stm.newDefaultTxn();
        Tranlocal tranlocal = ref.openForRead(tx, LOCKMODE_NONE);

        GammaTxn otherTx = stm.newDefaultTxn();
        ref.getLock().acquire(otherTx, LockMode.Write);

        boolean result = ref.tryLockAndCheckConflict(tx,tranlocal, 1, LOCKMODE_WRITE);

        assertFalse(result);
        assertFalse(tranlocal.hasDepartObligation());
        assertEquals(LOCKMODE_NONE, tranlocal.getLockMode());
        assertRefHasWriteLock(ref, otherTx);
        assertVersionAndValue(ref, initialVersion, initialValue);
        assertSurplus(ref, 1);
    }

    @Test
    public void lockNotFree_writeLockAcquired_acquireExclusiveLock() {
        long initialValue = 10;
        GammaTxnLong ref = new GammaTxnLong(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTxn tx = stm.newDefaultTxn();
        Tranlocal tranlocal = ref.openForRead(tx, LOCKMODE_NONE);

        GammaTxn otherTx = stm.newDefaultTxn();
        ref.getLock().acquire(otherTx, LockMode.Write);

        boolean result = ref.tryLockAndCheckConflict(tx,tranlocal, 1, LOCKMODE_EXCLUSIVE);

        assertFalse(result);
        assertFalse(tranlocal.hasDepartObligation());
        assertEquals(LOCKMODE_NONE, tranlocal.getLockMode());
        assertRefHasWriteLock(ref, otherTx);
        assertVersionAndValue(ref, initialVersion, initialValue);
        assertSurplus(ref, 1);
    }

    @Test
    public void lockNotFree_exclusiveLockAcquired_acquireNoLock() {
        long initialValue = 10;
        GammaTxnLong ref = new GammaTxnLong(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTxn tx = stm.newDefaultTxn();
        Tranlocal tranlocal = ref.openForRead(tx, LOCKMODE_NONE);

        GammaTxn otherTx = stm.newDefaultTxn();
        ref.getLock().acquire(otherTx, LockMode.Exclusive);

        boolean result = ref.tryLockAndCheckConflict(tx,tranlocal, 1, LOCKMODE_NONE);

        assertTrue(result);
        assertFalse(tranlocal.hasDepartObligation());
        assertEquals(LOCKMODE_NONE, tranlocal.getLockMode());
        assertRefHasExclusiveLock(ref, otherTx);
        assertVersionAndValue(ref, initialVersion, initialValue);
        assertSurplus(ref, 1);
    }

    @Test
    public void lockNotFree_exclusiveLockAcquired_acquireReadLock() {
        long initialValue = 10;
        GammaTxnLong ref = new GammaTxnLong(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTxn tx = stm.newDefaultTxn();
        Tranlocal tranlocal = ref.openForRead(tx, LOCKMODE_NONE);

        GammaTxn otherTx = stm.newDefaultTxn();
        ref.getLock().acquire(otherTx, LockMode.Exclusive);

        boolean result = ref.tryLockAndCheckConflict(tx,tranlocal, 1, LOCKMODE_READ);

        assertFalse(result);
        assertFalse(tranlocal.hasDepartObligation());
        assertEquals(LOCKMODE_NONE, tranlocal.getLockMode());
        assertRefHasExclusiveLock(ref, otherTx);
        assertVersionAndValue(ref, initialVersion, initialValue);
        assertSurplus(ref, 1);
    }

    @Test
    public void lockNotFree_exclusiveLockAcquired_acquireWriteLock() {
        long initialValue = 10;
        GammaTxnLong ref = new GammaTxnLong(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTxn tx = stm.newDefaultTxn();
        Tranlocal tranlocal = ref.openForRead(tx, LOCKMODE_NONE);

        GammaTxn otherTx = stm.newDefaultTxn();
        ref.getLock().acquire(otherTx, LockMode.Exclusive);

        boolean result = ref.tryLockAndCheckConflict(tx,tranlocal, 1, LOCKMODE_WRITE);

        assertFalse(result);
        assertFalse(tranlocal.hasDepartObligation());
        assertEquals(LOCKMODE_NONE, tranlocal.getLockMode());
        assertRefHasExclusiveLock(ref, otherTx);
        assertVersionAndValue(ref, initialVersion, initialValue);
        assertSurplus(ref, 1);
    }

    @Test
    public void lockNotFree_exclusiveLockAcquired_acquireExclusiveLock() {
        long initialValue = 10;
        GammaTxnLong ref = new GammaTxnLong(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTxn tx = stm.newDefaultTxn();
        Tranlocal tranlocal = ref.openForRead(tx, LOCKMODE_NONE);

        GammaTxn otherTx = stm.newDefaultTxn();
        ref.getLock().acquire(otherTx, LockMode.Exclusive);

        boolean result = ref.tryLockAndCheckConflict(tx,tranlocal, 1, LOCKMODE_EXCLUSIVE);

        assertFalse(result);
        assertFalse(tranlocal.hasDepartObligation());
        assertEquals(LOCKMODE_NONE, tranlocal.getLockMode());
        assertRefHasExclusiveLock(ref, otherTx);
        assertVersionAndValue(ref, initialVersion, initialValue);
        assertSurplus(ref, 1);
    }
}
