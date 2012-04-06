package org.multiverse.stms.gamma.integration.traditionalsynchronization;

import org.junit.Test;
import org.multiverse.api.TxnExecutor;
import org.multiverse.stms.gamma.LeanGammaTxnExecutor;
import org.multiverse.stms.gamma.transactions.GammaTxnConfig;
import org.multiverse.stms.gamma.transactions.lean.LeanMonoGammaTxnFactory;

/**
 * @author Peter Veentjer
 */
public class Semaphore_LeanMonoGammaTxn_StressTest extends Semaphore_AbstractTest {

    @Test
    public void test() {
        run();
    }

    @Override
    protected TxnExecutor newDownBlock() {
        GammaTxnConfig config = new GammaTxnConfig(stm);
        return new LeanGammaTxnExecutor(new LeanMonoGammaTxnFactory(config));
    }

    @Override
    protected TxnExecutor newUpBlock() {
        GammaTxnConfig config = new GammaTxnConfig(stm);
        return new LeanGammaTxnExecutor(new LeanMonoGammaTxnFactory(config));
    }
}



