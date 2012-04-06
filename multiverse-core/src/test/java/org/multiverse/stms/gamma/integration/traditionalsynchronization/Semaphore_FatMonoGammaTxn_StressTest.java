package org.multiverse.stms.gamma.integration.traditionalsynchronization;

import org.junit.Test;
import org.multiverse.api.TxnExecutor;
import org.multiverse.api.LockMode;
import org.multiverse.stms.gamma.LeanGammaTxnExecutor;
import org.multiverse.stms.gamma.transactions.GammaTxnConfig;
import org.multiverse.stms.gamma.transactions.fat.FatMonoGammaTxnFactory;

/**
 * @author Peter Veentjer
 */
public class Semaphore_FatMonoGammaTxn_StressTest extends Semaphore_AbstractTest {

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
    protected TxnExecutor newDownBlock() {
        GammaTxnConfig config = new GammaTxnConfig(stm)
                .setReadLockMode(lockMode);
        return new LeanGammaTxnExecutor(new FatMonoGammaTxnFactory(config));
    }

    @Override
    protected TxnExecutor newUpBlock() {
        GammaTxnConfig config = new GammaTxnConfig(stm)
                .setReadLockMode(lockMode);
        return new LeanGammaTxnExecutor(new FatMonoGammaTxnFactory(config));
    }
}

