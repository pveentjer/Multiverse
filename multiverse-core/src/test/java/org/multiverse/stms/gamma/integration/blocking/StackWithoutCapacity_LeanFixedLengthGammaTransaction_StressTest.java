package org.multiverse.stms.gamma.integration.blocking;

import org.junit.Test;
import org.multiverse.api.TransactionExecutor;
import org.multiverse.stms.gamma.LeanGammaTransactionExecutor;
import org.multiverse.stms.gamma.transactions.lean.LeanFixedLengthGammaTxnFactory;

public class StackWithoutCapacity_LeanFixedLengthGammaTransaction_StressTest extends StackWithoutCapacity_AbstractTest {

    @Test
    public void test() {
        run();
    }

    @Override
    protected TransactionExecutor newPopTransactionExecutor() {
        return new LeanGammaTransactionExecutor(new LeanFixedLengthGammaTxnFactory(stm));
    }

    @Override
    protected TransactionExecutor newPushTransactionExecutor() {
        return new LeanGammaTransactionExecutor(new LeanFixedLengthGammaTxnFactory(stm));
    }
}
