package org.multiverse.stms.gamma.transactions.lean;

import org.multiverse.stms.gamma.transactions.GammaTxnConfiguration;

public class LeanMonoGammaTxn_retryTest extends LeanGammaTxn_retryTest<LeanMonoGammaTxn> {

    @Override
    public LeanMonoGammaTxn newTransaction() {
        return new LeanMonoGammaTxn(stm);
    }

    @Override
    public LeanMonoGammaTxn newTransaction(GammaTxnConfiguration config) {
        return new LeanMonoGammaTxn(config);
    }
}
