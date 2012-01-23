package org.multiverse.stms.gamma.integration.traditionalsynchronization;

import org.junit.Test;
import org.multiverse.api.AtomicBlock;
import org.multiverse.stms.gamma.LeanGammaAtomicBlock;
import org.multiverse.stms.gamma.transactions.lean.LeanMonoGammaTransactionFactory;

public class NonReentrantReadWriteLock_LeanMonoGammaTransaction_StressTest extends NonReentrantReadWriteLock_AbstractTest {

    @Test
    public void test() {
        run();
    }

    @Override
    protected AtomicBlock newReleaseWriteLockBlock() {
        return new LeanGammaAtomicBlock(new LeanMonoGammaTransactionFactory(stm));
    }

    @Override
    protected AtomicBlock newAcquireWriteLockBlock() {
        return new LeanGammaAtomicBlock(new LeanMonoGammaTransactionFactory(stm));
    }

    @Override
    protected AtomicBlock newReleaseReadLockBlock() {
        return new LeanGammaAtomicBlock(new LeanMonoGammaTransactionFactory(stm));
    }

    @Override
    protected AtomicBlock newAcquireReadLockBlock() {
        return new LeanGammaAtomicBlock(new LeanMonoGammaTransactionFactory(stm));
    }
}
