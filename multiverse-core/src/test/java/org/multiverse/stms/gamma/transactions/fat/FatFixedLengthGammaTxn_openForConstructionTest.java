package org.multiverse.stms.gamma.transactions.fat;

import org.multiverse.stms.gamma.transactions.GammaTxnConfiguration;

public class FatFixedLengthGammaTxn_openForConstructionTest
        extends FatGammaTxn_openForConstructionTest<FatFixedLengthGammaTxn> {

    @Override
    protected FatFixedLengthGammaTxn newTransaction() {
        return new FatFixedLengthGammaTxn(stm);
    }

    @Override
    protected FatFixedLengthGammaTxn newTransaction(GammaTxnConfiguration config) {
        return new FatFixedLengthGammaTxn(config);
    }
}
