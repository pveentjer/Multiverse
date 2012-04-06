package org.multiverse.stms.gamma.transactions.lean;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.LockMode;
import org.multiverse.api.exceptions.DeadTxnException;
import org.multiverse.api.exceptions.ReadWriteConflict;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactionalobjects.GammaTxnRef;
import org.multiverse.stms.gamma.transactionalobjects.Tranlocal;
import org.multiverse.stms.gamma.transactions.GammaTxn;
import org.multiverse.stms.gamma.transactions.GammaTxnConfig;
import org.multiverse.stms.gamma.transactions.fat.FatVariableLengthGammaTxn;

import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;
import static org.multiverse.TestUtils.assertIsAborted;
import static org.multiverse.TestUtils.assertIsCommitted;
import static org.multiverse.stms.gamma.GammaTestUtils.*;

public abstract class LeanGammaTxn_commitTest<T extends GammaTxn> {

    public GammaStm stm;

    @Before
    public void setUp() {
        stm = new GammaStm();
    }

    public abstract T newTransaction();

    public abstract int getMaximumLength();

    public abstract void assertClearedAfterCommit();

    public abstract void assertClearedAfterAbort();

    @Test
    public void conflict_whenReadByOther(){
        String initialValue = null;
        GammaTxnRef<String> ref = new GammaTxnRef<String>(stm, initialValue);
        long initialVersion = ref.getVersion();

        T tx = newTransaction();
        String newValue = "bar";
        ref.set(tx, newValue);

        GammaTxnConfig config = new GammaTxnConfig(stm)
                .setMaximumPoorMansConflictScanLength(0);

        FatVariableLengthGammaTxn otherTx = new FatVariableLengthGammaTxn(config);
        ref.get(otherTx);

        long globalConflictCount = stm.globalConflictCounter.count();
        tx.commit();

        assertGlobalConflictCount(stm, globalConflictCount+1);
        assertVersionAndValue(ref, initialVersion+1, newValue);
    }

    @Test
    public void whenUnused() {
        T tx = newTransaction();
        long globalConflictCount = stm.globalConflictCounter.count();

        tx.commit();

        assertIsCommitted();
        assertClearedAfterCommit();
        assertGlobalConflictCount(stm, globalConflictCount);
    }

    @Test
    public void whenMultipleDirtyWrites() {
        assumeTrue(getMaximumLength() > 1);

        long globalConflictCount = stm.globalConflictCounter.count();

        String initialValue1 = "foo1";
        String updateValue1 = "bar1";
        GammaTxnRef<String> ref1 = new GammaTxnRef<String>(stm, initialValue1);
        long initialVersion1 = ref1.getVersion();

        String initialValue2 = "foo2";
        String updateValue2 = "bar1";
        GammaTxnRef<String> ref2 = new GammaTxnRef<String>(stm, initialValue2);
        long initialVersion2 = ref2.getVersion();

        T tx = newTransaction();
        Tranlocal tranlocal1 = ref1.openForWrite(tx, LOCKMODE_NONE);
        tranlocal1.ref_value = updateValue1;
        Tranlocal tranlocal2 = ref2.openForWrite(tx, LOCKMODE_NONE);
        tranlocal2.ref_value = updateValue2;
        tx.commit();

        assertIsCommitted(tx);
        assertRefHasNoLocks(ref1);
        assertSurplus(ref1, 0);
        assertVersionAndValue(ref1, initialVersion1 + 1, updateValue1);
        assertWriteBiased(ref1);
        assertNull(tranlocal1.owner);
        assertNull(tranlocal1.ref_value);
        assertFalse(tranlocal1.hasDepartObligation);

        assertRefHasNoLocks(ref2);
        assertSurplus(ref2, 0);
        assertVersionAndValue(ref2, initialVersion2 + 1, updateValue2);
        assertWriteBiased(ref2);
        assertNull(tranlocal2.owner);
        assertNull(tranlocal2.ref_value);
        assertFalse(tranlocal2.hasDepartObligation);

        assertGlobalConflictCount(stm, globalConflictCount);
    }

    @Test
    public void whenMultipleNonDirtyWrites() {
        assumeTrue(getMaximumLength() > 1);

        long globalConflictCount = stm.globalConflictCounter.count();

        String initialValue1 = "foo1";
        GammaTxnRef<String> ref1 = new GammaTxnRef<String>(stm, initialValue1);
        long initialVersion1 = ref1.getVersion();

        String initialValue2 = "foo2";
        GammaTxnRef<String> ref2 = new GammaTxnRef<String>(stm, initialValue2);
        long initialVersion2 = ref2.getVersion();

        T tx = newTransaction();
        Tranlocal tranlocal1 = ref1.openForWrite(tx, LOCKMODE_NONE);
        Tranlocal tranlocal2 = ref2.openForWrite(tx, LOCKMODE_NONE);
        tx.commit();

        assertIsCommitted(tx);
        assertRefHasNoLocks(ref1);
        assertSurplus(ref1, 0);
        assertVersionAndValue(ref1, initialVersion1 + 1, initialValue1);
        assertWriteBiased(ref1);
        assertNull(tranlocal1.owner);
        assertNull(tranlocal1.ref_value);
        assertFalse(tranlocal1.hasDepartObligation);

        assertRefHasNoLocks(ref2);
        assertSurplus(ref2, 0);
        assertVersionAndValue(ref2, initialVersion2 + 1, initialValue2);
        assertWriteBiased(ref2);
        assertNull(tranlocal2.owner);
        assertNull(tranlocal2.ref_value);
        assertFalse(tranlocal2.hasDepartObligation);

        assertGlobalConflictCount(stm, globalConflictCount);
    }


    @Test
    public void whenNonDirtyUpdate() {
        long globalConflictCount = stm.globalConflictCounter.count();

        String initialValue = "foo";
        GammaTxnRef<String> ref = new GammaTxnRef<String>(stm, initialValue);
        long initialVersion = ref.getVersion();

        T tx = newTransaction();
        Tranlocal tranlocal = ref.openForWrite(tx, LOCKMODE_NONE);
        tranlocal.ref_value = initialValue;
        tx.commit();

        assertNull(tranlocal.owner);
        assertNull(tranlocal.ref_value);
        assertNull(tranlocal.ref_oldValue);
        assertIsCommitted(tx);
        assertSurplus(ref, 0);
        assertReadonlyCount(ref, 0);
        assertWriteBiased(ref);
        assertIsCommitted(tx);
        assertRefHasNoLocks(ref);
        assertVersionAndValue(ref, initialVersion + 1, initialValue);
        assertIsCommitted(tx);

        assertGlobalConflictCount(stm, globalConflictCount);
    }

    @Test
    public void whenDirtyUpdate() {
        long globalConflictCount = stm.globalConflictCounter.count();

        String initialValue = "foo";
        String newValue = "bar";
        GammaTxnRef<String> ref = new GammaTxnRef<String>(stm, initialValue);
        long initialVersion = ref.getVersion();

        T tx = newTransaction();
        Tranlocal tranlocal = ref.openForWrite(tx, LOCKMODE_NONE);
        tranlocal.ref_value = newValue;
        tx.commit();

        assertNull(tranlocal.owner);
        assertNull(tranlocal.ref_value);
        assertNull(tranlocal.ref_oldValue);
        assertIsCommitted(tx);
        assertSurplus(ref, 0);
        assertReadonlyCount(ref, 0);
        assertWriteBiased(ref);
        assertIsCommitted(tx);
        assertRefHasNoLocks(ref);
        assertVersionAndValue(ref, initialVersion + 1, newValue);
        assertIsCommitted(tx);

        assertGlobalConflictCount(stm, globalConflictCount);
    }

    @Test
    public void whenLockedByOtherAndWrite() {
        whenLockedByOtherAndWrite(LockMode.Read);
        whenLockedByOtherAndWrite(LockMode.Write);
        whenLockedByOtherAndWrite(LockMode.Exclusive);
    }

    protected void whenLockedByOtherAndWrite(LockMode lockMode) {
        long globalConflictCount = stm.globalConflictCounter.count();

        String initialValue = "foo";
        GammaTxnRef<String> ref = new GammaTxnRef<String>(stm, initialValue);
        long initialVersion = ref.getVersion();

        T tx = newTransaction();
        Tranlocal tranlocal = ref.openForWrite(tx, LOCKMODE_NONE);

        GammaTxn otherTx = stm.newDefaultTxn();
        ref.getLock().acquire(otherTx, lockMode);

        try {
            tx.commit();
            fail();
        } catch (ReadWriteConflict expected) {
        }

        assertIsAborted(tx);
        assertWriteBiased(ref);
        assertReadonlyCount(ref, 0);
        assertSurplus(ref, 1);
        assertNull(tranlocal.owner);
        assertNull(tranlocal.ref_value);
        assertNull(tranlocal.ref_oldValue);
        assertVersionAndValue(ref, initialVersion, initialValue);
        assertRefHasLockMode(ref, otherTx, lockMode.asInt());
        assertGlobalConflictCount(stm, globalConflictCount);
    }

    @Test
    public void whenNormalRead() {
        long globalConflictCount = stm.globalConflictCounter.count();

        String initialValue = "foo";
        GammaTxnRef<String> ref = new GammaTxnRef<String>(stm, initialValue);
        long initialVersion = ref.getVersion();

        T tx = newTransaction();
        Tranlocal tranlocal = ref.openForRead(tx, LOCKMODE_NONE);
        tx.commit();

        assertIsCommitted(tx);
        assertNull(tranlocal.owner);
        assertNull(tranlocal.ref_value);
        assertNull(tranlocal.ref_oldValue);
        assertRefHasNoLocks(ref);
        assertSurplus(ref, 0);
        assertReadonlyCount(ref, 0);
        assertWriteBiased(ref);
        assertVersionAndValue(ref, initialVersion, initialValue);
        assertGlobalConflictCount(stm, globalConflictCount);
    }

    @Test
    public void whenAlreadyPreparedAndUnused() {
        long globalConflictCount = stm.globalConflictCounter.count();

        T tx = newTransaction();
        tx.prepare();

        tx.commit();

        assertIsCommitted(tx);
        assertGlobalConflictCount(stm, globalConflictCount);
    }

    @Test
    public void whenAlreadyCommitted() {
        long globalConflictCount = stm.globalConflictCounter.count();

        String initialValue = "foo";
        GammaTxnRef<String> ref = new GammaTxnRef<String>(stm, initialValue);
        long initialVersion = ref.getVersion();

        T tx = newTransaction();
        tx.commit();

        tx.commit();

        assertIsCommitted(tx);
        assertRefHasNoLocks(ref);
        assertVersionAndValue(ref, initialVersion, initialValue);
        assertGlobalConflictCount(stm, globalConflictCount);
    }

    @Test
    public void whenAlreadyAborted() {
        long globalConflictCount = stm.globalConflictCounter.count();

        T tx = newTransaction();
        tx.abort();

        try {
            tx.commit();
            fail();
        } catch (DeadTxnException expected) {
        }

        assertIsAborted(tx);
        assertGlobalConflictCount(stm, globalConflictCount);
    }
}
