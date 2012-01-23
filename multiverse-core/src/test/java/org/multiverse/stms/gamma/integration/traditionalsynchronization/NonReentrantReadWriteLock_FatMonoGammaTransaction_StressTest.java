package org.multiverse.stms.gamma.integration.traditionalsynchronization;

import org.junit.Test;
import org.multiverse.api.AtomicBlock;
import org.multiverse.api.LockMode;
import org.multiverse.stms.gamma.LeanGammaAtomicBlock;
import org.multiverse.stms.gamma.transactions.GammaTransactionConfiguration;
import org.multiverse.stms.gamma.transactions.fat.FatMonoGammaTransactionFactory;

public class NonReentrantReadWriteLock_FatMonoGammaTransaction_StressTest extends NonReentrantReadWriteLock_AbstractTest {

    private LockMode lockMode;

    @Test
    public void whenNoLock() {
        lockMode = LockMode.None;
        run();
    }

    @Test
    public void whenReadLock() {
        lockMode = LockMode.Read;
        run();
    }

    @Test
    public void whenWriteLock() {
        lockMode = LockMode.Write;
        run();
    }

    @Test
    public void whenExclusiveLock() {
        lockMode = LockMode.Exclusive;
        run();
    }

    protected AtomicBlock newReleaseWriteLockBlock() {
        GammaTransactionConfiguration config = new GammaTransactionConfiguration(stm)
                .setMaxRetries(10000)
                .setReadLockMode(lockMode);
        return new LeanGammaAtomicBlock(new FatMonoGammaTransactionFactory(config));

    }

    protected AtomicBlock newAcquireWriteLockBlock() {
        GammaTransactionConfiguration config = new GammaTransactionConfiguration(stm)
                .setMaxRetries(10000)
                .setReadLockMode(lockMode);
        return new LeanGammaAtomicBlock(new FatMonoGammaTransactionFactory(config));

    }

    protected AtomicBlock newReleaseReadLockBlock() {
        GammaTransactionConfiguration config = new GammaTransactionConfiguration(stm)
                .setMaxRetries(10000)
                .setReadLockMode(lockMode);
        return new LeanGammaAtomicBlock(new FatMonoGammaTransactionFactory(config));

    }

    protected AtomicBlock newAcquireReadLockBlock() {
        GammaTransactionConfiguration config = new GammaTransactionConfiguration(stm)
                .setMaxRetries(10000)
                .setReadLockMode(lockMode);
        return new LeanGammaAtomicBlock(new FatMonoGammaTransactionFactory(config));

    }
}
