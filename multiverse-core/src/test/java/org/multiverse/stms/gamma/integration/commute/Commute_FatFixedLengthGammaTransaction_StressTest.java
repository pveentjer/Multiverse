package org.multiverse.stms.gamma.integration.commute;

import org.multiverse.api.TransactionExecutor;
import org.multiverse.stms.gamma.LeanGammaTransactionExecutor;
import org.multiverse.stms.gamma.transactions.GammaTxnConfiguration;
import org.multiverse.stms.gamma.transactions.fat.FatFixedLengthGammaTxnFactory;

public class Commute_FatFixedLengthGammaTransaction_StressTest extends Commute_AbstractTest {

    @Override
    protected TransactionExecutor newBlock() {
        GammaTxnConfiguration config = new GammaTxnConfiguration(stm)
                .setMaxRetries(10000);
        return new LeanGammaTransactionExecutor(new FatFixedLengthGammaTxnFactory(config));
    }
}
