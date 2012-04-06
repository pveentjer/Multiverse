package org.multiverse.stms.gamma.transactions.fat;

public class FatFixedLengthGammaTxn_isAbortOnlyTest extends FatGammaTxn_isAbortOnlyTest<FatFixedLengthGammaTxn> {

    @Override
    protected FatFixedLengthGammaTxn newTransaction() {
        return new FatFixedLengthGammaTxn(stm);
    }
}
