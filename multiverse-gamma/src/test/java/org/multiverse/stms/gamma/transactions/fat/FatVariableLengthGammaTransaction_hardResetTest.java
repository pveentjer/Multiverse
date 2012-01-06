package org.multiverse.stms.gamma.transactions.fat;

import org.junit.Test;
import org.multiverse.stms.gamma.transactionalobjects.GammaRef;

import static org.junit.Assert.assertEquals;

public class FatVariableLengthGammaTransaction_hardResetTest extends FatGammaTransaction_hardResetTest<FatVariableLengthGammaTransaction> {

    @Override
    protected FatVariableLengthGammaTransaction newTransaction() {
        return new FatVariableLengthGammaTransaction(stm);
    }

    @Test
    public void testDownsizing(){
        FatVariableLengthGammaTransaction tx = new FatVariableLengthGammaTransaction(stm);
        for(int k=0;k<10000;k++){
            new GammaRef(tx);
        }
        tx.hardReset();
        assertEquals(tx.config.minimalArrayTreeSize, tx.array.length);
    }
}
