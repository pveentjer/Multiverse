package org.multiverse.stms.gamma.integration.classic;

import org.junit.Test;
import org.multiverse.api.TransactionExecutor;
import org.multiverse.api.LockMode;
import org.multiverse.stms.gamma.LeanGammaTransactionExecutor;
import org.multiverse.stms.gamma.transactions.GammaTxnConfiguration;
import org.multiverse.stms.gamma.transactions.fat.FatFixedLengthGammaTxnFactory;

public class ProducerConsumer_FatFixedLengthGammaTransaction_StressTest extends ProducerConsumer_AbstractTest {

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
    protected TransactionExecutor newPutBlock() {
        GammaTxnConfiguration config = new GammaTxnConfiguration(stm)
                .setMaxRetries(10000)
                .setReadLockMode(lockMode);
        return new LeanGammaTransactionExecutor(new FatFixedLengthGammaTxnFactory(config));
    }

    @Override
    protected TransactionExecutor newTakeBlock() {
        GammaTxnConfiguration config = new GammaTxnConfiguration(stm)
                .setMaxRetries(10000)
                .setReadLockMode(lockMode);
        return new LeanGammaTransactionExecutor(new FatFixedLengthGammaTxnFactory(config));
    }
}
