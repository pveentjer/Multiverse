package org.multiverse.stms.gamma.integration.blocking;

import org.junit.Test;
import org.multiverse.api.TxnExecutor;
import org.multiverse.stms.gamma.LeanGammaTxnExecutor;
import org.multiverse.stms.gamma.transactions.lean.LeanFixedLengthGammaTxnFactory;

public class StackWithoutCapacity_LeanFixedLengthGammaTransaction_StressTest extends StackWithoutCapacity_AbstractTest {

    @Test
    public void test() {
        run();
    }

    @Override
    protected TxnExecutor newPopTxnExecutor() {
        return new LeanGammaTxnExecutor(new LeanFixedLengthGammaTxnFactory(stm));
    }

    @Override
    protected TxnExecutor newPushTxnExecutor() {
        return new LeanGammaTxnExecutor(new LeanFixedLengthGammaTxnFactory(stm));
    }
}
