package org.multiverse.stms.gamma.integration.traditionalsynchronization;

import org.junit.Test;
import org.multiverse.api.TxnExecutor;
import org.multiverse.api.LockMode;
import org.multiverse.stms.gamma.LeanGammaTxnExecutor;
import org.multiverse.stms.gamma.transactions.GammaTxnConfiguration;
import org.multiverse.stms.gamma.transactions.fat.FatMonoGammaTxnFactory;

public class NonReentrantReadWriteLock_FatMonoGammaTxn_StressTest extends NonReentrantReadWriteLock_AbstractTest {

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

    protected TxnExecutor newReleaseWriteLockBlock() {
        GammaTxnConfiguration config = new GammaTxnConfiguration(stm)
                .setMaxRetries(10000)
                .setReadLockMode(lockMode);
        return new LeanGammaTxnExecutor(new FatMonoGammaTxnFactory(config));

    }

    protected TxnExecutor newAcquireWriteLockBlock() {
        GammaTxnConfiguration config = new GammaTxnConfiguration(stm)
                .setMaxRetries(10000)
                .setReadLockMode(lockMode);
        return new LeanGammaTxnExecutor(new FatMonoGammaTxnFactory(config));

    }

    protected TxnExecutor newReleaseReadLockBlock() {
        GammaTxnConfiguration config = new GammaTxnConfiguration(stm)
                .setMaxRetries(10000)
                .setReadLockMode(lockMode);
        return new LeanGammaTxnExecutor(new FatMonoGammaTxnFactory(config));

    }

    protected TxnExecutor newAcquireReadLockBlock() {
        GammaTxnConfiguration config = new GammaTxnConfiguration(stm)
                .setMaxRetries(10000)
                .setReadLockMode(lockMode);
        return new LeanGammaTxnExecutor(new FatMonoGammaTxnFactory(config));

    }
}
