package org.multiverse.stms.gamma.integration.blocking;

import org.junit.Test;
import org.multiverse.api.TxnExecutor;
import org.multiverse.stms.gamma.LeanGammaTxnExecutor;
import org.multiverse.stms.gamma.transactions.lean.LeanFixedLengthGammaTxnFactory;

public class QueueWithoutCapacity_LeanFixedLengthGammaTransaction_StressTest extends QueueWithoutCapacity_AbstractTest {

    @Test
    public void test() {
        run();
    }

    @Override
    protected TxnExecutor newPopBlock() {
        return new LeanGammaTxnExecutor(new LeanFixedLengthGammaTxnFactory(stm));
    }

    @Override
    protected TxnExecutor newPushBlock() {
        return new LeanGammaTxnExecutor(new LeanFixedLengthGammaTxnFactory(stm));
    }
}
