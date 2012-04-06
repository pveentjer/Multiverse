package org.multiverse.stms.gamma.integration.commute;

import org.multiverse.api.TxnExecutor;
import org.multiverse.stms.gamma.LeanGammaTxnExecutor;
import org.multiverse.stms.gamma.transactions.GammaTxnConfiguration;
import org.multiverse.stms.gamma.transactions.fat.FatVariableLengthGammaTxnFactory;

public class Commute_FatVariableLengthGammaTxn_StressTest extends Commute_AbstractTest {

    @Override
    protected TxnExecutor newBlock() {
        GammaTxnConfiguration config = new GammaTxnConfiguration(stm)
                .setMaxRetries(10000);
        return new LeanGammaTxnExecutor(new FatVariableLengthGammaTxnFactory(config));
    }
}
