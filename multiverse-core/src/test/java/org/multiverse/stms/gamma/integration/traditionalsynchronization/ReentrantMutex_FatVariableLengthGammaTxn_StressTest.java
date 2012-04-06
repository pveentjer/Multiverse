package org.multiverse.stms.gamma.integration.traditionalsynchronization;

import org.junit.Test;
import org.multiverse.api.LockMode;
import org.multiverse.stms.gamma.GammaTxnExecutor;
import org.multiverse.stms.gamma.LeanGammaTxnExecutor;
import org.multiverse.stms.gamma.transactions.GammaTxnConfig;
import org.multiverse.stms.gamma.transactions.fat.FatVariableLengthGammaTxnFactory;

/**
 * @author Peter Veentjer
 */
public class ReentrantMutex_FatVariableLengthGammaTxn_StressTest extends ReentrantMutex_AbstractTest {

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

    protected GammaTxnExecutor newLockBlock() {
        GammaTxnConfig config = new GammaTxnConfig(stm)
                .setMaxRetries(10000)
                .setReadLockMode(lockMode);
        return new LeanGammaTxnExecutor(new FatVariableLengthGammaTxnFactory(config));

    }

    protected GammaTxnExecutor newUnlockBlock() {
        GammaTxnConfig config = new GammaTxnConfig(stm)
                .setMaxRetries(10000)
                .setReadLockMode(lockMode);
        return new LeanGammaTxnExecutor(new FatVariableLengthGammaTxnFactory(config));
    }
}
