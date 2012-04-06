package org.multiverse.stms.gamma.integration.blocking;

import org.junit.Test;
import org.multiverse.api.TxnExecutor;
import org.multiverse.api.LockMode;
import org.multiverse.stms.gamma.LeanGammaTxnExecutor;
import org.multiverse.stms.gamma.transactions.GammaTxnConfiguration;
import org.multiverse.stms.gamma.transactions.fat.FatVariableLengthGammaTxnFactory;


public class ConnectionPool_FatVariableLengthGammaTxn_StressTest extends ConnectionPool_AbstractTest {

    private LockMode lockMode = LockMode.None;

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
    protected TxnExecutor newTakeBlock() {
        GammaTxnConfiguration config = new GammaTxnConfiguration(stm)
                .setReadLockMode(lockMode);
        return new LeanGammaTxnExecutor(new FatVariableLengthGammaTxnFactory(config));
    }

    @Override
    protected TxnExecutor newReturnBlock() {
        GammaTxnConfiguration config = new GammaTxnConfiguration(stm)
                .setReadLockMode(lockMode);
        return new LeanGammaTxnExecutor(new FatVariableLengthGammaTxnFactory(config));
    }
}
