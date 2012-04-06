package org.multiverse.stms.gamma.transactions.lean;

import org.multiverse.stms.gamma.transactions.GammaTxnConfiguration;


public class LeanFixedLengthGammaTxn_retryTest extends LeanGammaTxn_retryTest<LeanFixedLengthGammaTxn>{
    @Override
    public LeanFixedLengthGammaTxn newTransaction() {
        return new LeanFixedLengthGammaTxn(stm);
    }

    @Override
    public LeanFixedLengthGammaTxn newTransaction(GammaTxnConfiguration config) {
        return new LeanFixedLengthGammaTxn(config);
    }
}
