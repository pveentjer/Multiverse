package org.multiverse.stms.gamma.integration.traditionalsynchronization;

import org.junit.Test;
import org.multiverse.api.TransactionExecutor;
import org.multiverse.stms.gamma.LeanGammaTransactionExecutor;
import org.multiverse.stms.gamma.transactions.lean.LeanFixedLengthGammaTxnFactory;

public class NonReentrantReadWriteLock_LeanFixedLengthGammaTransaction_StressTest extends NonReentrantReadWriteLock_AbstractTest {

    @Test
    public void test() {
        run();
    }

    @Override
    protected TransactionExecutor newReleaseWriteLockBlock() {
        return new LeanGammaTransactionExecutor(new LeanFixedLengthGammaTxnFactory(stm));
    }

    @Override
    protected TransactionExecutor newAcquireWriteLockBlock() {
        return new LeanGammaTransactionExecutor(new LeanFixedLengthGammaTxnFactory(stm));
    }

    @Override
    protected TransactionExecutor newReleaseReadLockBlock() {
        return new LeanGammaTransactionExecutor(new LeanFixedLengthGammaTxnFactory(stm));
    }

    @Override
    protected TransactionExecutor newAcquireReadLockBlock() {
        return new LeanGammaTransactionExecutor(new LeanFixedLengthGammaTxnFactory(stm));
    }
}
