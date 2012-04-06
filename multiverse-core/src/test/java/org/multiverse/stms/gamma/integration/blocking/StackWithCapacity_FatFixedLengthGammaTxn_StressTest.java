package org.multiverse.stms.gamma.integration.blocking;

import org.junit.Test;
import org.multiverse.api.TxnExecutor;
import org.multiverse.api.LockMode;
import org.multiverse.stms.gamma.LeanGammaTxnExecutor;
import org.multiverse.stms.gamma.transactions.GammaTxnConfig;
import org.multiverse.stms.gamma.transactions.fat.FatFixedLengthGammaTxnFactory;


public class StackWithCapacity_FatFixedLengthGammaTxn_StressTest extends StackWithCapacity_AbstractTest {

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
    protected TxnExecutor newPopBlock() {
        GammaTxnConfig config = new GammaTxnConfig(stm)
                .setReadLockMode(lockMode);
        return new LeanGammaTxnExecutor(new FatFixedLengthGammaTxnFactory(config));
    }

    @Override
    protected TxnExecutor newPushBlock() {
        GammaTxnConfig config = new GammaTxnConfig(stm)
                .setReadLockMode(lockMode);
        return new LeanGammaTxnExecutor(new FatFixedLengthGammaTxnFactory(config));
    }
}
