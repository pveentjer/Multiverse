package org.multiverse.stms.gamma.integration.commute;

import org.multiverse.api.TransactionExecutor;
import org.multiverse.stms.gamma.LeanGammaTransactionExecutor;
import org.multiverse.stms.gamma.transactions.GammaTransactionConfiguration;
import org.multiverse.stms.gamma.transactions.fat.FatVariableLengthGammaTransactionFactory;

public class Commute_FatVariableLengthGammaTransaction_StressTest extends Commute_AbstractTest {

    @Override
    protected TransactionExecutor newBlock() {
        GammaTransactionConfiguration config = new GammaTransactionConfiguration(stm)
                .setMaxRetries(10000);
        return new LeanGammaTransactionExecutor(new FatVariableLengthGammaTransactionFactory(config));
    }
}
