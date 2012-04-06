package org.multiverse.stms.gamma.integration.blocking;

import org.junit.Test;
import org.multiverse.api.TxnExecutor;
import org.multiverse.stms.gamma.LeanGammaTxnExecutor;
import org.multiverse.stms.gamma.transactions.lean.LeanMonoGammaTxnFactory;

public class StackWithoutCapacity_LeanMonoGammaTxn_StressTest extends StackWithoutCapacity_AbstractTest {

    @Test
    public void test() {
        run();
    }

    @Override
    protected TxnExecutor newPopTxnExecutor() {
        return new LeanGammaTxnExecutor(new LeanMonoGammaTxnFactory(stm));
    }

    @Override
    protected TxnExecutor newPushTxnExecutor() {
        return new LeanGammaTxnExecutor(new LeanMonoGammaTxnFactory(stm));
    }
}
