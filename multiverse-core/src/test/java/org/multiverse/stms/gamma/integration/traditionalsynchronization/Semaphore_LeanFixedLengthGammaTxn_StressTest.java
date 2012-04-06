package org.multiverse.stms.gamma.integration.traditionalsynchronization;

import org.junit.Test;
import org.multiverse.api.TxnExecutor;
import org.multiverse.stms.gamma.LeanGammaTxnExecutor;
import org.multiverse.stms.gamma.transactions.GammaTxnConfig;
import org.multiverse.stms.gamma.transactions.lean.LeanFixedLengthGammaTxnFactory;

/**
 * @author Peter Veentjer
 */
public class Semaphore_LeanFixedLengthGammaTxn_StressTest extends Semaphore_AbstractTest {

    @Test
    public void test() {
        run();
    }

    @Override
    protected TxnExecutor newDownBlock() {
        GammaTxnConfig config = new GammaTxnConfig(stm);
        return new LeanGammaTxnExecutor(new LeanFixedLengthGammaTxnFactory(config));
    }

    @Override
    protected TxnExecutor newUpBlock() {
        GammaTxnConfig config = new GammaTxnConfig(stm);
        return new LeanGammaTxnExecutor(new LeanFixedLengthGammaTxnFactory(config));
    }
}


