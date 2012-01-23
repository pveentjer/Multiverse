package org.multiverse.stms.gamma.integration.traditionalsynchronization;

import org.junit.Test;
import org.multiverse.api.LockMode;
import org.multiverse.stms.gamma.GammaAtomicBlock;
import org.multiverse.stms.gamma.LeanGammaAtomicBlock;
import org.multiverse.stms.gamma.transactions.GammaTransactionConfiguration;
import org.multiverse.stms.gamma.transactions.fat.FatFixedLengthGammaTransactionFactory;

/**
 * @author Peter Veentjer
 */
public class ReentrantMutex_FatFixedLengthGammaTransaction_StressTest extends ReentrantMutex_AbstractTest {

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

    protected GammaAtomicBlock newLockBlock() {
        GammaTransactionConfiguration config = new GammaTransactionConfiguration(stm)
                .setMaxRetries(10000)
                .setReadLockMode(lockMode);
        return new LeanGammaAtomicBlock(new FatFixedLengthGammaTransactionFactory(config));

    }

    protected GammaAtomicBlock newUnlockBlock() {
        GammaTransactionConfiguration config = new GammaTransactionConfiguration(stm)
                .setMaxRetries(10000)
                .setReadLockMode(lockMode);
        return new LeanGammaAtomicBlock(new FatFixedLengthGammaTransactionFactory(config));
    }
}
