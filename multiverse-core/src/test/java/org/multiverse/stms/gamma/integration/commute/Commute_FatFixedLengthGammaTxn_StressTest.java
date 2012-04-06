package org.multiverse.stms.gamma.integration.commute;

import org.multiverse.api.TxnExecutor;
import org.multiverse.stms.gamma.LeanGammaTxnExecutor;
import org.multiverse.stms.gamma.transactions.GammaTxnConfig;
import org.multiverse.stms.gamma.transactions.fat.FatFixedLengthGammaTxnFactory;

public class Commute_FatFixedLengthGammaTxn_StressTest extends Commute_AbstractTest {

    @Override
    protected TxnExecutor newBlock() {
        GammaTxnConfig config = new GammaTxnConfig(stm)
                .setMaxRetries(10000);
        return new LeanGammaTxnExecutor(new FatFixedLengthGammaTxnFactory(config));
    }
}
