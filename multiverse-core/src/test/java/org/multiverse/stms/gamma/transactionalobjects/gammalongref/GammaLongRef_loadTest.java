package org.multiverse.stms.gamma.transactionalobjects.gammalongref;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.multiverse.api.LockMode;
import org.multiverse.stms.gamma.GammaConstants;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.GammaTestUtils;
import org.multiverse.stms.gamma.transactionalobjects.GammaLongRef;
import org.multiverse.stms.gamma.transactionalobjects.GammaRefTranlocal;
import org.multiverse.stms.gamma.transactions.GammaTxn;

import java.util.Collection;

import static java.util.Arrays.asList;
import static org.junit.Assert.*;
import static org.multiverse.stms.gamma.GammaTestUtils.*;

@RunWith(Parameterized.class)
public class GammaLongRef_loadTest implements GammaConstants {

    private GammaStm stm;
    private boolean readBiased;
    private boolean arriveNeeded;

    public GammaLongRef_loadTest(boolean readBiased, boolean arriveNeeded) {
        this.readBiased = readBiased;
        this.arriveNeeded = arriveNeeded;
    }

    @Before
    public void setUp() {
        stm = new GammaStm();
    }

    @Parameterized.Parameters
    public static Collection<Boolean[]> configs() {
        return asList(
                new Boolean[]{false, false},
                new Boolean[]{false, true},
                new Boolean[]{true, false},
                new Boolean[]{true, true}
        );
    }

    public GammaLongRef newLongRef(long initialValue) {
        GammaLongRef ref = new GammaLongRef(stm, initialValue);
        if (readBiased) {
            ref = GammaTestUtils.makeReadBiased(ref);
        }
        return ref;
    }

    // ====================== locking ==========================

    @Test
    public void locking_whenNotLockedByOtherAndNoLockNeeded() {
        long initialValue = 10;
        GammaLongRef ref = newLongRef(initialValue);
        long initialVersion = ref.getVersion();

        GammaTxn otherTx = stm.newDefaultTxn();
        ref.getLock().acquire(otherTx, LockMode.None);

        GammaTxn tx = stm.newDefaultTxn();
        GammaRefTranlocal tranlocal = new GammaRefTranlocal();
        boolean result = ref.load(tx,tranlocal, LOCKMODE_NONE, 1, arriveNeeded);

        assertTrue(result);
        assertEquals(LOCKMODE_NONE, tranlocal.getLockMode());
        assertSame(ref, tranlocal.owner);
        assertEquals(initialValue, tranlocal.long_value);
        assertEquals(initialValue, tranlocal.long_oldValue);
        assertEquals(initialVersion, tranlocal.version);
        assertEquals(arriveNeeded && !readBiased, tranlocal.hasDepartObligation());
        assertLockMode(ref, LockMode.None);
        assertSurplus(ref, arriveNeeded ? 1 : 0);
        assertReadonlyCount(ref, 0);
        assertReadBiased(ref, readBiased);
        assertVersionAndValue(ref, initialVersion, initialValue);
    }

    @Test
    public void locking_whenNotLockedByOtherAndReadLockNeeded() {
        long initialValue = 10;
        GammaLongRef ref = newLongRef(initialValue);
        long initialVersion = ref.getVersion();

        GammaTxn otherTx = stm.newDefaultTxn();
        ref.getLock().acquire(otherTx, LockMode.None);

        GammaRefTranlocal tranlocal = new GammaRefTranlocal();
        GammaTxn tx = stm.newDefaultTxn();
        boolean result = ref.load(tx,tranlocal, LOCKMODE_READ, 1, arriveNeeded);

        assertTrue(result);
        assertEquals(LOCKMODE_READ, tranlocal.getLockMode());
        assertSame(ref, tranlocal.owner);
        assertEquals(initialValue, tranlocal.long_value);
        assertEquals(initialValue, tranlocal.long_oldValue);
        assertEquals(initialVersion, tranlocal.version);
        assertEquals(!readBiased, tranlocal.hasDepartObligation());
        assertLockMode(ref, LockMode.Read);
        assertReadLockCount(ref, 1);
        assertSurplus(ref, 1);
        assertReadonlyCount(ref, 0);
        assertReadBiased(ref, readBiased);
        assertVersionAndValue(ref, initialVersion, initialValue);
    }

    @Test
    public void locking_whenNotLockedByOtherAndWriteLockNeeded() {
        long initialValue = 10;
        GammaLongRef ref = newLongRef(initialValue);
        long initialVersion = ref.getVersion();

        GammaTxn otherTx = stm.newDefaultTxn();
        ref.getLock().acquire(otherTx, LockMode.None);

        GammaRefTranlocal tranlocal = new GammaRefTranlocal();
        GammaTxn tx = stm.newDefaultTxn();
        boolean result = ref.load(tx,tranlocal, LOCKMODE_WRITE, 1, arriveNeeded);

        assertTrue(result);
        assertEquals(LOCKMODE_WRITE, tranlocal.getLockMode());
        assertSame(ref, tranlocal.owner);
        assertEquals(initialValue, tranlocal.long_value);
        assertEquals(initialValue, tranlocal.long_oldValue);
        assertEquals(initialVersion, tranlocal.version);
        assertEquals(!readBiased, tranlocal.hasDepartObligation());
        assertLockMode(ref, LockMode.Write);
        assertSurplus(ref, 1);
        assertReadonlyCount(ref, 0);
        assertReadBiased(ref, readBiased);
        assertVersionAndValue(ref, initialVersion, initialValue);
    }

    @Test
    public void locking_whenNotLockedByOtherAndExclusiveLockNeeded() {
        long initialValue = 10;
        GammaLongRef ref = newLongRef(initialValue);
        long initialVersion = ref.getVersion();

        GammaTxn otherTx = stm.newDefaultTxn();
        ref.getLock().acquire(otherTx, LockMode.None);

        GammaRefTranlocal tranlocal = new GammaRefTranlocal();
        GammaTxn tx = stm.newDefaultTxn();
        boolean result = ref.load(tx,tranlocal, LOCKMODE_EXCLUSIVE, 1, arriveNeeded);

        assertTrue(result);
        assertEquals(LOCKMODE_EXCLUSIVE, tranlocal.getLockMode());
        assertSame(ref, tranlocal.owner);
        assertEquals(initialValue, tranlocal.long_value);
        assertEquals(initialValue, tranlocal.long_oldValue);
        assertEquals(initialVersion, tranlocal.version);
        assertEquals(!readBiased, tranlocal.hasDepartObligation());
        assertLockMode(ref, LockMode.Exclusive);
        assertSurplus(ref, 1);
        assertReadonlyCount(ref, 0);
        assertReadBiased(ref, readBiased);
        assertVersionAndValue(ref, initialVersion, initialValue);
    }

    @Test
    public void locking_whenReadLockedByOtherAndNoLockNeeded() {
        long initialValue = 10;
        GammaLongRef ref = newLongRef(initialValue);
        long initialVersion = ref.getVersion();

        GammaTxn otherTx = stm.newDefaultTxn();
        ref.getLock().acquire(otherTx, LockMode.Read);

        GammaRefTranlocal tranlocal = new GammaRefTranlocal();
        GammaTxn tx = stm.newDefaultTxn();
        boolean result = ref.load(tx,tranlocal, LOCKMODE_NONE, 1, arriveNeeded);

        assertTrue(result);
        assertEquals(LOCKMODE_NONE, tranlocal.getLockMode());
        assertSame(ref, tranlocal.owner);
        assertEquals(initialValue, tranlocal.long_value);
        assertEquals(initialValue, tranlocal.long_oldValue);
        assertEquals(initialVersion, tranlocal.version);
        assertEquals(!readBiased && arriveNeeded, tranlocal.hasDepartObligation());
        assertLockMode(ref, LockMode.Read);
        assertSurplus(ref, !readBiased && arriveNeeded ? 2 : 1);
        assertReadLockCount(ref, 1);
        assertReadonlyCount(ref, 0);
        assertReadBiased(ref, readBiased);
        assertVersionAndValue(ref, initialVersion, initialValue);
    }

    @Test
    public void locking_whenReadLockedByOtherAndReadLockNeeded() {
        long initialValue = 10;
        GammaLongRef ref = newLongRef(initialValue);
        long initialVersion = ref.getVersion();

        GammaTxn otherTx = stm.newDefaultTxn();
        ref.getLock().acquire(otherTx, LockMode.Read);

        GammaRefTranlocal tranlocal = new GammaRefTranlocal();
        GammaTxn tx = stm.newDefaultTxn();
        boolean result = ref.load(tx,tranlocal, LOCKMODE_READ, 1, arriveNeeded);

        assertTrue(result);
        assertEquals(LOCKMODE_READ, tranlocal.getLockMode());
        assertSame(ref, tranlocal.owner);
        assertEquals(initialValue, tranlocal.long_value);
        assertEquals(initialValue, tranlocal.long_oldValue);
        assertEquals(initialVersion, tranlocal.version);
        assertEquals(!readBiased, tranlocal.hasDepartObligation());
        assertLockMode(ref, LockMode.Read);
        assertSurplus(ref, readBiased ? 1 : 2);
        assertReadLockCount(ref, 2);
        assertReadonlyCount(ref, 0);
        assertReadBiased(ref, readBiased);
        assertVersionAndValue(ref, initialVersion, initialValue);
    }

    @Test
    public void locking_whenReadLockedByOtherAndWriteLockNeeded() {
        long initialValue = 10;
        GammaLongRef ref = newLongRef(initialValue);
        long initialVersion = ref.getVersion();

        GammaTxn otherTx = stm.newDefaultTxn();
        ref.getLock().acquire(otherTx, LockMode.Read);

        GammaRefTranlocal tranlocal = new GammaRefTranlocal();
        GammaTxn tx = stm.newDefaultTxn();
        boolean result = ref.load(tx,tranlocal, LOCKMODE_WRITE, 1, arriveNeeded);

        assertFalse(result);
        assertEquals(LOCKMODE_NONE, tranlocal.getLockMode());
        assertNull(tranlocal.owner);
        assertLockMode(ref, LockMode.Read);
        assertSurplus(ref, 1);
        assertReadLockCount(ref, 1);
        assertReadonlyCount(ref, 0);
        assertReadBiased(ref, readBiased);
        assertVersionAndValue(ref, initialVersion, initialValue);
    }

    @Test
    public void locking_whenReadLockedByOtherAndExclusiveLockNeeded() {
        long initialValue = 10;
        GammaLongRef ref = newLongRef(initialValue);
        long initialVersion = ref.getVersion();

        GammaTxn otherTx = stm.newDefaultTxn();
        ref.getLock().acquire(otherTx, LockMode.Read);

        GammaRefTranlocal tranlocal = new GammaRefTranlocal();
        GammaTxn tx = stm.newDefaultTxn();
        boolean result = ref.load(tx,tranlocal, LOCKMODE_EXCLUSIVE, 1, arriveNeeded);

        assertFalse(result);
        assertEquals(LOCKMODE_NONE, tranlocal.getLockMode());
        assertNull(tranlocal.owner);
        assertLockMode(ref, LockMode.Read);
        assertSurplus(ref, 1);
        assertReadLockCount(ref, 1);
        assertReadonlyCount(ref, 0);
        assertReadBiased(ref, readBiased);
        assertVersionAndValue(ref, initialVersion, initialValue);
    }

    @Test
    public void locking_whenWriteLockedByOtherAndNoLockNeeded() {
        long initialValue = 10;
        GammaLongRef ref = newLongRef(initialValue);
        long initialVersion = ref.getVersion();

        GammaTxn otherTx = stm.newDefaultTxn();
        ref.getLock().acquire(otherTx, LockMode.Write);

        GammaRefTranlocal tranlocal = new GammaRefTranlocal();
        GammaTxn tx = stm.newDefaultTxn();
        boolean result = ref.load(tx,tranlocal, LOCKMODE_NONE, 1, arriveNeeded);

        assertTrue(result);
        assertEquals(LOCKMODE_NONE, tranlocal.getLockMode());
        assertSame(ref, tranlocal.owner);
        assertEquals(initialValue, tranlocal.long_value);
        assertEquals(initialValue, tranlocal.long_oldValue);
        assertEquals(initialVersion, tranlocal.version);
        assertEquals(arriveNeeded && !readBiased, tranlocal.hasDepartObligation());
        assertLockMode(ref, LockMode.Write);
        assertSurplus(ref, arriveNeeded && !readBiased ? 2 : 1);
        assertReadBiased(ref, readBiased);
        assertVersionAndValue(ref, initialVersion, initialValue);
    }

    @Test
    public void locking_whenWritetLockedByOtherAndReadLockNeeded() {
        long initialValue = 10;
        GammaLongRef ref = newLongRef(initialValue);
        long initialVersion = ref.getVersion();

        GammaTxn otherTx = stm.newDefaultTxn();
        ref.getLock().acquire(otherTx, LockMode.Write);

        GammaRefTranlocal tranlocal = new GammaRefTranlocal();
        GammaTxn tx = stm.newDefaultTxn();
        boolean result = ref.load(tx,tranlocal, LOCKMODE_READ, 1, arriveNeeded);

        assertFalse(result);
        assertEquals(LOCKMODE_NONE, tranlocal.getLockMode());
        assertNull(tranlocal.owner);
        assertLockMode(ref, LockMode.Write);
        assertSurplus(ref, 1);
        assertReadBiased(ref, readBiased);
        assertVersionAndValue(ref, initialVersion, initialValue);
    }

    @Test
    public void locking_whenWriteLockedByOtherAndWriteLockNeeded() {
        long initialValue = 10;
        GammaLongRef ref = newLongRef(initialValue);
        long initialVersion = ref.getVersion();

        GammaTxn otherTx = stm.newDefaultTxn();
        ref.getLock().acquire(otherTx, LockMode.Write);

        GammaRefTranlocal tranlocal = new GammaRefTranlocal();
        GammaTxn tx = stm.newDefaultTxn();
        boolean result = ref.load(tx,tranlocal, LOCKMODE_WRITE, 1, arriveNeeded);

        assertFalse(result);
        assertEquals(LOCKMODE_NONE, tranlocal.getLockMode());
        assertNull(tranlocal.owner);
        assertLockMode(ref, LockMode.Write);
        assertSurplus(ref, 1);
        assertReadBiased(ref, readBiased);
        assertVersionAndValue(ref, initialVersion, initialValue);
    }

    @Test
    public void locking_whenWriteLockedByOtherAndExclusiveLockNeeded() {
        long initialValue = 10;
        GammaLongRef ref = newLongRef(initialValue);
        long initialVersion = ref.getVersion();

        GammaTxn otherTx = stm.newDefaultTxn();
        ref.getLock().acquire(otherTx, LockMode.Write);

        GammaRefTranlocal tranlocal = new GammaRefTranlocal();
        GammaTxn tx = stm.newDefaultTxn();
        boolean result = ref.load(tx,tranlocal, LOCKMODE_EXCLUSIVE, 1, arriveNeeded);

        assertFalse(result);
        assertEquals(LOCKMODE_NONE, tranlocal.getLockMode());
        assertNull(tranlocal.owner);
        assertLockMode(ref, LockMode.Write);
        assertSurplus(ref, 1);
        assertReadBiased(ref, readBiased);
        assertVersionAndValue(ref, initialVersion, initialValue);
    }

    @Test
    public void locking_whenExclusiveLockedByOtherAndNoLockNeeded() {
        long initialValue = 10;
        GammaLongRef ref = newLongRef(initialValue);
        long initialVersion = ref.getVersion();

        GammaTxn otherTx = stm.newDefaultTxn();
        ref.getLock().acquire(otherTx, LockMode.Exclusive);

        GammaRefTranlocal tranlocal = new GammaRefTranlocal();
        GammaTxn tx = stm.newDefaultTxn();
        boolean result = ref.load(tx, tranlocal, LOCKMODE_NONE, 1, arriveNeeded);

        assertFalse(result);
        assertEquals(LOCKMODE_NONE, tranlocal.getLockMode());
        assertNull(tranlocal.owner);
        assertLockMode(ref, LockMode.Exclusive);
        assertSurplus(ref, 1);
        assertReadBiased(ref, readBiased);
        assertVersionAndValue(ref, initialVersion, initialValue);
    }

    @Test
    public void locking_whenExclusiveLockedByOtherAndReadLockNeeded() {
        long initialValue = 10;
        GammaLongRef ref = newLongRef(initialValue);
        long initialVersion = ref.getVersion();

        GammaTxn otherTx = stm.newDefaultTxn();
        ref.getLock().acquire(otherTx, LockMode.Exclusive);

        GammaRefTranlocal tranlocal = new GammaRefTranlocal();
        GammaTxn tx = stm.newDefaultTxn();
        boolean result = ref.load(tx,tranlocal, LOCKMODE_READ, 1, arriveNeeded);

        assertFalse(result);
        assertEquals(LOCKMODE_NONE, tranlocal.getLockMode());
        assertNull(tranlocal.owner);
        assertLockMode(ref, LockMode.Exclusive);
        assertSurplus(ref, 1);
        assertReadBiased(ref, readBiased);
        assertVersionAndValue(ref, initialVersion, initialValue);
    }

    @Test
    public void locking_whenExclusiveLockedByOtherAndWriteLockNeeded() {
        long initialValue = 10;
        GammaLongRef ref = newLongRef(initialValue);
        long initialVersion = ref.getVersion();

        GammaTxn otherTx = stm.newDefaultTxn();
        ref.getLock().acquire(otherTx, LockMode.Exclusive);

        GammaRefTranlocal tranlocal = new GammaRefTranlocal();
        GammaTxn tx = stm.newDefaultTxn();
        boolean result = ref.load(tx,tranlocal, LOCKMODE_WRITE, 1, arriveNeeded);

        assertFalse(result);
        assertEquals(LOCKMODE_NONE, tranlocal.getLockMode());
        assertNull(tranlocal.owner);
        assertLockMode(ref, LockMode.Exclusive);
        assertSurplus(ref, 1);
        assertReadBiased(ref, readBiased);
        assertVersionAndValue(ref, initialVersion, initialValue);
    }

    @Test
    public void locking_whenExclusiveLockedByOtherAndExclusiveLockNeeded() {
        long initialValue = 10;
        GammaLongRef ref = newLongRef(initialValue);
        long initialVersion = ref.getVersion();

        GammaTxn otherTx = stm.newDefaultTxn();
        ref.getLock().acquire(otherTx, LockMode.Exclusive);

        GammaRefTranlocal tranlocal = new GammaRefTranlocal();
        GammaTxn tx = stm.newDefaultTxn();
        boolean result = ref.load(tx,tranlocal, LOCKMODE_EXCLUSIVE, 1, arriveNeeded);

        assertFalse(result);
        assertEquals(LOCKMODE_NONE, tranlocal.getLockMode());
        assertNull(tranlocal.owner);
        assertLockMode(ref, LockMode.Exclusive);
        assertSurplus(ref, 1);
        assertReadBiased(ref, readBiased);
        assertVersionAndValue(ref, initialVersion, initialValue);
    }
}
