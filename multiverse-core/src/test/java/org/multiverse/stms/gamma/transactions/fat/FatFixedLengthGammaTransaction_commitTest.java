package org.multiverse.stms.gamma.transactions.fat;

import org.junit.Test;
import org.multiverse.stms.gamma.transactionalobjects.GammaLongRef;
import org.multiverse.stms.gamma.transactionalobjects.GammaRefTranlocal;
import org.multiverse.stms.gamma.transactions.GammaTxnConfiguration;

import static org.junit.Assert.assertNull;
import static org.multiverse.stms.gamma.GammaTestUtils.assertSurplus;

public class FatFixedLengthGammaTransaction_commitTest extends FatGammaTransaction_commitTest<FatFixedLengthGammaTransaction> {

    @Override
    protected void assertCleaned(FatFixedLengthGammaTransaction tx) {
        GammaRefTranlocal node = tx.head;
        while (node != null) {
            assertNull(node.owner);
            node = node.next;
        }
    }

    @Override
    protected FatFixedLengthGammaTransaction newTransaction(GammaTxnConfiguration config) {
        return new FatFixedLengthGammaTransaction(config);
    }

    @Override
    protected FatFixedLengthGammaTransaction newTransaction() {
        return new FatFixedLengthGammaTransaction(stm);
    }

    @Test
    public void richmansConflict_multipleReadsOnSameRef() {
        GammaLongRef ref = new GammaLongRef(stm);

        GammaTxnConfiguration config = new GammaTxnConfiguration(stm)
                .setMaximumPoorMansConflictScanLength(0);

        FatVariableLengthGammaTransaction tx1 = new FatVariableLengthGammaTransaction(config);
        FatVariableLengthGammaTransaction tx2 = new FatVariableLengthGammaTransaction(config);
        FatVariableLengthGammaTransaction tx3 = new FatVariableLengthGammaTransaction(config);

        FatFixedLengthGammaTransaction tx = new FatFixedLengthGammaTransaction(config);

        ref.openForRead(tx1, LOCKMODE_NONE);
        ref.openForRead(tx2, LOCKMODE_NONE);
        ref.openForRead(tx3, LOCKMODE_NONE);
        ref.set(tx, 1);
        tx.commit();

        assertSurplus(ref, 3);
    }

}
