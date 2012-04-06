package org.multiverse.stms.gamma.integration.traditionalsynchronization;

import org.junit.Test;
import org.multiverse.api.TxnExecutor;
import org.multiverse.stms.gamma.LeanGammaTxnExecutor;
import org.multiverse.stms.gamma.transactions.lean.LeanMonoGammaTxnFactory;

public class NonReentrantReadWriteLock_LeanMonoGammaTxn_StressTest extends NonReentrantReadWriteLock_AbstractTest {

    @Test
    public void test() {
        run();
    }

    @Override
    protected TxnExecutor newReleaseWriteLockBlock() {
        return new LeanGammaTxnExecutor(new LeanMonoGammaTxnFactory(stm));
    }

    @Override
    protected TxnExecutor newAcquireWriteLockBlock() {
        return new LeanGammaTxnExecutor(new LeanMonoGammaTxnFactory(stm));
    }

    @Override
    protected TxnExecutor newReleaseReadLockBlock() {
        return new LeanGammaTxnExecutor(new LeanMonoGammaTxnFactory(stm));
    }

    @Override
    protected TxnExecutor newAcquireReadLockBlock() {
        return new LeanGammaTxnExecutor(new LeanMonoGammaTxnFactory(stm));
    }
}
