package org.multiverse.stms.gamma.transactions.lean;

import org.multiverse.stms.gamma.transactions.GammaTxnConfig;

public class LeanMonoGammaTxn_retryTest extends LeanGammaTxn_retryTest<LeanMonoGammaTxn> {

    @Override
    public LeanMonoGammaTxn newTransaction() {
        return new LeanMonoGammaTxn(stm);
    }

    @Override
    public LeanMonoGammaTxn newTransaction(GammaTxnConfig config) {
        return new LeanMonoGammaTxn(config);
    }
}
