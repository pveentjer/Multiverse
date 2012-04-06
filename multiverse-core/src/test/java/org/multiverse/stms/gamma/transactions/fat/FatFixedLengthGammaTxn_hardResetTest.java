package org.multiverse.stms.gamma.transactions.fat;

import org.multiverse.stms.gamma.transactions.GammaTxn;

public class FatFixedLengthGammaTxn_hardResetTest extends FatGammaTxn_hardResetTest<GammaTxn> {

    @Override
    protected GammaTxn newTransaction() {
        return new FatFixedLengthGammaTxn(stm);
    }
}
