package org.multiverse.stms.gamma.transactions.fat;

public class FatFixedLengthGammaTxn_locateTest extends FatGammaTxn_locateTest<FatFixedLengthGammaTxn> {

    @Override
    protected FatFixedLengthGammaTxn newTransaction() {
        return new FatFixedLengthGammaTxn(stm);
    }
}
