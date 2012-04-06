package org.multiverse.stms.gamma.transactions.fat;

public class FatFixedLengthGammaTxn_initTest extends FatGammaTxn_initTest<FatFixedLengthGammaTxn> {
    @Override
    protected FatFixedLengthGammaTxn newTransaction() {
        return new FatFixedLengthGammaTxn(stm);
    }
}
