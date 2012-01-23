package org.multiverse.stms.gamma.integration.commute;

import org.multiverse.api.AtomicBlock;
import org.multiverse.stms.gamma.LeanGammaAtomicBlock;
import org.multiverse.stms.gamma.transactions.GammaTransactionConfiguration;
import org.multiverse.stms.gamma.transactions.fat.FatVariableLengthGammaTransactionFactory;

public class Commute_FatVariableLengthGammaTransaction_StressTest extends Commute_AbstractTest {

    @Override
    protected AtomicBlock newBlock() {
        GammaTransactionConfiguration config = new GammaTransactionConfiguration(stm)
                .setMaxRetries(10000);
        return new LeanGammaAtomicBlock(new FatVariableLengthGammaTransactionFactory(config));
    }
}
