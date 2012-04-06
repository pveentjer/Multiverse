package org.multiverse.stms.gamma.transactions.lean;

import org.multiverse.stms.gamma.transactions.GammaTxnConfig;


public class LeanFixedLengthGammaTxn_retryTest extends LeanGammaTxn_retryTest<LeanFixedLengthGammaTxn>{
    @Override
    public LeanFixedLengthGammaTxn newTransaction() {
        return new LeanFixedLengthGammaTxn(stm);
    }

    @Override
    public LeanFixedLengthGammaTxn newTransaction(GammaTxnConfig config) {
        return new LeanFixedLengthGammaTxn(config);
    }
}
