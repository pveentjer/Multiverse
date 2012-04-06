package org.multiverse.stms.gamma.transactions.fat;

public class FatFixedLengthGammaTxn_registerTest extends FatGammaTxn_registerTest<FatFixedLengthGammaTxn> {

    @Override
    protected FatFixedLengthGammaTxn newTransaction() {
        return new FatFixedLengthGammaTxn(stm);
    }
}
