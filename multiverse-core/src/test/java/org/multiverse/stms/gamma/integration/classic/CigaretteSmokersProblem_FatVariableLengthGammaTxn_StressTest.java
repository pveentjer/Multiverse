package org.multiverse.stms.gamma.integration.classic;

import org.junit.Test;
import org.multiverse.api.TxnExecutor;
import org.multiverse.api.LockMode;
import org.multiverse.stms.gamma.LeanGammaTxnExecutor;
import org.multiverse.stms.gamma.transactions.GammaTxnConfiguration;
import org.multiverse.stms.gamma.transactions.fat.FatVariableLengthGammaTxnFactory;

public class CigaretteSmokersProblem_FatVariableLengthGammaTxn_StressTest extends CigaretteSmokersProblem_AbstractTest {

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
    protected TxnExecutor newBlock() {
        GammaTxnConfiguration config = new GammaTxnConfiguration(stm)
                .setReadLockMode(lockMode);
        return new LeanGammaTxnExecutor(new FatVariableLengthGammaTxnFactory(config));
    }
}
