package org.multiverse.stms.gamma.integration.blocking;

import org.junit.Test;
import org.multiverse.api.AtomicBlock;
import org.multiverse.api.LockMode;
import org.multiverse.stms.gamma.LeanGammaAtomicBlock;
import org.multiverse.stms.gamma.transactions.GammaTransactionConfiguration;
import org.multiverse.stms.gamma.transactions.fat.FatVariableLengthGammaTransactionFactory;

public class QueueWithoutCapacity_FatVariableLengthGammaTransaction_StressTest extends QueueWithoutCapacity_AbstractTest {

    private LockMode lockMode;

    @Test
    public void testNoLock() {
        lockMode = LockMode.None;
        run();
    }

    @Test
    public void testReadLock() {
        lockMode = LockMode.Read;
        run();
    }

    @Test
    public void testWriteLock() {
        lockMode = LockMode.Write;
        run();
    }

    @Test
    public void testExclusiveLock() {
        lockMode = LockMode.Exclusive;
        run();
    }

    @Override
    protected AtomicBlock newPopBlock() {
        GammaTransactionConfiguration config = new GammaTransactionConfiguration(stm)
                .setReadLockMode(lockMode);
        return new LeanGammaAtomicBlock(new FatVariableLengthGammaTransactionFactory(config));
    }

    @Override
    protected AtomicBlock newPushBlock() {
        GammaTransactionConfiguration config = new GammaTransactionConfiguration(stm)
                .setReadLockMode(lockMode);
        return new LeanGammaAtomicBlock(new FatVariableLengthGammaTransactionFactory(config));
    }
}
