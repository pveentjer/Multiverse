package org.multiverse.stms.gamma.transactions.fat;

import org.junit.Test;
import org.multiverse.api.exceptions.ReadWriteConflict;
import org.multiverse.stms.gamma.transactionalobjects.GammaLongRef;
import org.multiverse.stms.gamma.transactionalobjects.GammaRefTranlocal;
import org.multiverse.stms.gamma.transactions.GammaTxn;
import org.multiverse.stms.gamma.transactions.GammaTxnConfig;

import static org.junit.Assert.*;
import static org.multiverse.TestUtils.assertIsAborted;
import static org.multiverse.TestUtils.assertIsActive;
import static org.multiverse.stms.gamma.GammaTestUtils.*;

public class FatFixedLengthGammaTxn_openForWriteTest extends FatGammaTxn_openForWriteTest {

    @Override
    protected GammaTxn newTransaction(GammaTxnConfig config) {
        return new FatFixedLengthGammaTxn(config);
    }

    @Override
    protected GammaTxn newTransaction() {
        return new FatFixedLengthGammaTxn(stm);
    }

    @Override
    protected int getMaxCapacity() {
        return new GammaTxnConfig(stm).maxFixedLengthTransactionSize;
    }

    @Test
       public void richmansConflict_multipleReadsOnSameRef() {
           GammaLongRef ref = new GammaLongRef(stm);

           GammaTxnConfig config = new GammaTxnConfig(stm)
                   .setMaximumPoorMansConflictScanLength(0);

           FatFixedLengthGammaTxn tx1 = new FatFixedLengthGammaTxn(config);
           FatFixedLengthGammaTxn tx2 = new FatFixedLengthGammaTxn(config);
           FatFixedLengthGammaTxn tx3 = new FatFixedLengthGammaTxn(config);

           ref.openForWrite(tx1, LOCKMODE_NONE);
           ref.openForWrite(tx2, LOCKMODE_NONE);
           ref.openForWrite(tx3, LOCKMODE_NONE);

           assertSurplus(ref, 3);
       }

    @Test
    public void richmansConflictScan_whenFirstRead() {
        GammaTxnConfig config = new GammaTxnConfig(stm)
                .setMaximumPoorMansConflictScanLength(0);

        causeLotsOfConflicts(stm);

        long initialValue = 10;
        GammaLongRef ref = new GammaLongRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        FatFixedLengthGammaTxn tx = new FatFixedLengthGammaTxn(config);
        GammaRefTranlocal tranlocal = ref.openForWrite(tx, LOCKMODE_NONE);

        assertNotNull(tranlocal);
        assertTrue(tranlocal.hasDepartObligation);
        assertEquals(initialValue, tranlocal.long_value);
        assertEquals(initialValue, tranlocal.long_oldValue);
        assertEquals(LOCKMODE_NONE, tranlocal.lockMode);
        assertEquals(TRANLOCAL_WRITE, tranlocal.mode);
        assertSurplus(ref, 1);
        assertWriteBiased(ref);
        assertReadonlyCount(ref, 0);

        assertIsActive(tx);
        assertTrue(tx.hasReads);
        assertEquals(stm.getGlobalConflictCounter().count(), tx.localConflictCount);
        assertVersionAndValue(ref, initialVersion, initialValue);
        assertRefHasNoLocks(ref);
    }

    @Test
    public void richmansConflictScan_whenUnrealConflict() {
        GammaTxnConfig config = new GammaTxnConfig(stm)
                .setMaximumPoorMansConflictScanLength(0);

        causeLotsOfConflicts(stm);

        GammaLongRef ref1 = new GammaLongRef(stm, 10);

        long initialValue2 = 10;
        GammaLongRef ref2 = new GammaLongRef(stm, initialValue2);
        long initialVersion2 = ref2.getVersion();

        FatFixedLengthGammaTxn tx = new FatFixedLengthGammaTxn(config);
        ref1.openForRead(tx, LOCKMODE_NONE);

        causeLotsOfConflicts(stm);
        long newConflictCount = stm.getGlobalConflictCounter().count();

        GammaRefTranlocal tranlocal2 = ref2.openForWrite(tx, LOCKMODE_NONE);

        assertNotNull(tranlocal2);
        assertTrue(tranlocal2.hasDepartObligation);
        assertEquals(initialValue2, tranlocal2.long_value);
        assertEquals(initialValue2, tranlocal2.long_oldValue);
        assertEquals(LOCKMODE_NONE, tranlocal2.lockMode);
        assertEquals(TRANLOCAL_WRITE, tranlocal2.mode);
        assertSurplus(ref2, 1);
        assertWriteBiased(ref2);
        assertReadonlyCount(ref2, 0);

        assertIsActive(tx);
        assertTrue(tx.hasReads);
        assertEquals(newConflictCount, tx.localConflictCount);
        assertVersionAndValue(ref2, initialVersion2, initialValue2);
        assertRefHasNoLocks(ref2);
    }

    @Test
    public void richmansConflictScan_whenConflict() {
        GammaTxnConfig config = new GammaTxnConfig(stm)
                .setMaximumPoorMansConflictScanLength(0);

        causeLotsOfConflicts(stm);

        GammaLongRef ref1 = new GammaLongRef(stm, 10);

        long initialValue2 = 10;
        GammaLongRef ref2 = new GammaLongRef(stm, initialValue2);
        long initialVersion2 = ref2.getVersion();

        FatFixedLengthGammaTxn tx = new FatFixedLengthGammaTxn(config);
        ref1.openForWrite(tx, LOCKMODE_NONE);

        ref1.atomicIncrementAndGet(1);

        try {
            ref2.openForRead(tx, LOCKMODE_NONE);
            fail();
        } catch (ReadWriteConflict expected) {

        }

        assertSurplus(ref2, 0);
        assertWriteBiased(ref2);
        assertReadonlyCount(ref2, 0);

        assertIsAborted(tx);
        assertVersionAndValue(ref2, initialVersion2, initialValue2);
        assertRefHasNoLocks(ref2);
    }
}
