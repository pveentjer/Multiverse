package org.multiverse.stms.gamma.transactions.fat;

import org.junit.Test;
import org.multiverse.stms.gamma.transactionalobjects.GammaLongRef;
import org.multiverse.stms.gamma.transactions.GammaTxnConfiguration;

import static org.multiverse.stms.gamma.GammaTestUtils.assertSurplus;

public class FatVariableLengthGammaTransaction_commitTest extends FatGammaTransaction_commitTest<FatVariableLengthGammaTransaction> {

    @Override
    protected FatVariableLengthGammaTransaction newTransaction() {
        return new FatVariableLengthGammaTransaction(stm);
    }

    @Override
    protected FatVariableLengthGammaTransaction newTransaction(GammaTxnConfiguration config) {
        return new FatVariableLengthGammaTransaction(config);
    }

    @Override
    protected void assertCleaned(FatVariableLengthGammaTransaction transaction) {
        //throw new TodoException();
    }
    
    @Test
     public void richmansConflict_multipleReadsOnSameRef() {
         GammaLongRef ref = new GammaLongRef(stm);

         GammaTxnConfiguration config = new GammaTxnConfiguration(stm)
                 .setMaximumPoorMansConflictScanLength(0);

         FatVariableLengthGammaTransaction tx1 = new FatVariableLengthGammaTransaction(config);
         FatVariableLengthGammaTransaction tx2 = new FatVariableLengthGammaTransaction(config);
         FatVariableLengthGammaTransaction tx3 = new FatVariableLengthGammaTransaction(config);

         FatVariableLengthGammaTransaction tx = new FatVariableLengthGammaTransaction(config);

         ref.openForRead(tx1, LOCKMODE_NONE);
         ref.openForRead(tx2, LOCKMODE_NONE);
         ref.openForRead(tx3, LOCKMODE_NONE);
         ref.set(tx, 1);
         tx.commit();

         assertSurplus(ref, 3);
     }
    
}
