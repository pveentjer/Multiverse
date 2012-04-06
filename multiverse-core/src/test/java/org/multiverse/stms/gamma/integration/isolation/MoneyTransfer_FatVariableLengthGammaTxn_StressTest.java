package org.multiverse.stms.gamma.integration.isolation;

import org.junit.Test;
import org.multiverse.api.TxnExecutor;
import org.multiverse.api.LockMode;
import org.multiverse.stms.gamma.LeanGammaTxnExecutor;
import org.multiverse.stms.gamma.transactions.GammaTxnConfiguration;
import org.multiverse.stms.gamma.transactions.fat.FatVariableLengthGammaTxnFactory;

public class MoneyTransfer_FatVariableLengthGammaTxn_StressTest extends MoneyTransfer_AbstractTest {

    private LockMode lockMode;

    @Test
    public void when10AccountsAnd2ThreadsAndOptimistic() {
        lockMode = LockMode.None;
        run(10, 2);
    }

    @Test
    public void when10AccountsAnd2ThreadsAndPessimistic() {
        lockMode = LockMode.Exclusive;
        run(10, 2);
    }

    @Test
    public void when100AccountAnd10ThreadsAndOptimistic() {
        lockMode = LockMode.None;
        run(100, 10);
    }

    @Test
    public void when100AccountAnd10ThreadsAndPessimistic() {
        lockMode = LockMode.Exclusive;
        run(100, 10);
    }

    @Test
    public void when1000AccountsAnd10ThreadsAndOptimistic() {
        lockMode = LockMode.None;
        run(1000, 10);
    }

    @Test
    public void when1000AccountsAnd10ThreadsAndPessimistic() {
        lockMode = LockMode.Exclusive;
        run(1000, 10);
    }

    @Test
    public void when30AccountsAnd30ThreadsAndOptimistic() {
        lockMode = LockMode.None;
        run(30, 30);
    }

    @Test
    public void when30AccountsAnd30ThreadsAndPessimistic() {
        lockMode = LockMode.Exclusive;
        run(30, 30);
    }

    @Override
    protected TxnExecutor newTxnExecutor() {
        GammaTxnConfiguration config = new GammaTxnConfiguration(stm)
                .setReadLockMode(lockMode);
        return new LeanGammaTxnExecutor(new FatVariableLengthGammaTxnFactory(config));
    }
}
