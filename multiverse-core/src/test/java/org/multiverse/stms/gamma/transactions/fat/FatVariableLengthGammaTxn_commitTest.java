package org.multiverse.stms.gamma.transactions.fat;

import org.junit.Test;
import org.multiverse.stms.gamma.transactionalobjects.GammaLongRef;
import org.multiverse.stms.gamma.transactions.GammaTxnConfiguration;

import static org.multiverse.stms.gamma.GammaTestUtils.assertSurplus;

public class FatVariableLengthGammaTxn_commitTest extends FatGammaTxn_commitTest<FatVariableLengthGammaTxn> {

    @Override
    protected FatVariableLengthGammaTxn newTransaction() {
        return new FatVariableLengthGammaTxn(stm);
    }

    @Override
    protected FatVariableLengthGammaTxn newTransaction(GammaTxnConfiguration config) {
        return new FatVariableLengthGammaTxn(config);
    }

    @Override
    protected void assertCleaned(FatVariableLengthGammaTxn transaction) {
        //throw new TodoException();
    }
    
    @Test
     public void richmansConflict_multipleReadsOnSameRef() {
         GammaLongRef ref = new GammaLongRef(stm);

         GammaTxnConfiguration config = new GammaTxnConfiguration(stm)
                 .setMaximumPoorMansConflictScanLength(0);

         FatVariableLengthGammaTxn tx1 = new FatVariableLengthGammaTxn(config);
         FatVariableLengthGammaTxn tx2 = new FatVariableLengthGammaTxn(config);
         FatVariableLengthGammaTxn tx3 = new FatVariableLengthGammaTxn(config);

         FatVariableLengthGammaTxn tx = new FatVariableLengthGammaTxn(config);

         ref.openForRead(tx1, LOCKMODE_NONE);
         ref.openForRead(tx2, LOCKMODE_NONE);
         ref.openForRead(tx3, LOCKMODE_NONE);
         ref.set(tx, 1);
         tx.commit();

         assertSurplus(ref, 3);
     }
    
}
