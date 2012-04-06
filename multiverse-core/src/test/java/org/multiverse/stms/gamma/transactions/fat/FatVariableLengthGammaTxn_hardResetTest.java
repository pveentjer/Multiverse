package org.multiverse.stms.gamma.transactions.fat;

import org.junit.Test;
import org.multiverse.stms.gamma.transactionalobjects.GammaTxnRef;

import static org.junit.Assert.assertEquals;

public class FatVariableLengthGammaTxn_hardResetTest extends FatGammaTxn_hardResetTest<FatVariableLengthGammaTxn> {

    @Override
    protected FatVariableLengthGammaTxn newTransaction() {
        return new FatVariableLengthGammaTxn(stm);
    }

    @Test
    public void testDownsizing(){
        FatVariableLengthGammaTxn tx = new FatVariableLengthGammaTxn(stm);
        for(int k=0;k<10000;k++){
            new GammaTxnRef(tx);
        }
        tx.hardReset();
        assertEquals(tx.config.minimalArrayTreeSize, tx.array.length);
    }
}
