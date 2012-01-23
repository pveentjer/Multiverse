package org.multiverse.stms.gamma.integration.traditionalsynchronization;

import org.junit.Test;
import org.multiverse.api.AtomicBlock;
import org.multiverse.api.LockMode;
import org.multiverse.stms.gamma.LeanGammaAtomicBlock;
import org.multiverse.stms.gamma.transactions.GammaTransactionConfiguration;
import org.multiverse.stms.gamma.transactions.fat.FatMonoGammaTransactionFactory;

/**
 * @author Peter Veentjer
 */
public class Semaphore_FatMonoGammaTransaction_StressTest extends Semaphore_AbstractTest {

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

    @Override
    protected AtomicBlock newDownBlock() {
        GammaTransactionConfiguration config = new GammaTransactionConfiguration(stm)
                .setReadLockMode(lockMode);
        return new LeanGammaAtomicBlock(new FatMonoGammaTransactionFactory(config));
    }

    @Override
    protected AtomicBlock newUpBlock() {
        GammaTransactionConfiguration config = new GammaTransactionConfiguration(stm)
                .setReadLockMode(lockMode);
        return new LeanGammaAtomicBlock(new FatMonoGammaTransactionFactory(config));
    }
}

