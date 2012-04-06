package org.multiverse.stms.gamma.transactions.fat;

import org.junit.Test;
import org.multiverse.stms.gamma.transactionalobjects.GammaLongRef;
import org.multiverse.stms.gamma.transactionalobjects.GammaRefTranlocal;
import org.multiverse.stms.gamma.transactions.GammaTxnConfig;

import static org.junit.Assert.assertNull;
import static org.multiverse.stms.gamma.GammaTestUtils.assertSurplus;

public class FatFixedLengthGammaTxn_commitTest extends FatGammaTxn_commitTest<FatFixedLengthGammaTxn> {

    @Override
    protected void assertCleaned(FatFixedLengthGammaTxn tx) {
        GammaRefTranlocal node = tx.head;
        while (node != null) {
            assertNull(node.owner);
            node = node.next;
        }
    }

    @Override
    protected FatFixedLengthGammaTxn newTransaction(GammaTxnConfig config) {
        return new FatFixedLengthGammaTxn(config);
    }

    @Override
    protected FatFixedLengthGammaTxn newTransaction() {
        return new FatFixedLengthGammaTxn(stm);
    }

    @Test
    public void richmansConflict_multipleReadsOnSameRef() {
        GammaLongRef ref = new GammaLongRef(stm);

        GammaTxnConfig config = new GammaTxnConfig(stm)
                .setMaximumPoorMansConflictScanLength(0);

        FatVariableLengthGammaTxn tx1 = new FatVariableLengthGammaTxn(config);
        FatVariableLengthGammaTxn tx2 = new FatVariableLengthGammaTxn(config);
        FatVariableLengthGammaTxn tx3 = new FatVariableLengthGammaTxn(config);

        FatFixedLengthGammaTxn tx = new FatFixedLengthGammaTxn(config);

        ref.openForRead(tx1, LOCKMODE_NONE);
        ref.openForRead(tx2, LOCKMODE_NONE);
        ref.openForRead(tx3, LOCKMODE_NONE);
        ref.set(tx, 1);
        tx.commit();

        assertSurplus(ref, 3);
    }

}
