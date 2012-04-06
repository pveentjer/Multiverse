package org.multiverse.stms.gamma.transactions.fat;

import org.multiverse.stms.gamma.transactions.GammaTxnConfiguration;

public class FatFixedLengthGammaTxn_prepareTest extends FatGammaTxn_prepareTest<FatFixedLengthGammaTxn> {

    @Override
    protected FatFixedLengthGammaTxn newTransaction() {
        return new FatFixedLengthGammaTxn(stm);
    }

    @Override
    protected FatFixedLengthGammaTxn newTransaction(GammaTxnConfiguration config) {
        return new FatFixedLengthGammaTxn(config);
    }
}
