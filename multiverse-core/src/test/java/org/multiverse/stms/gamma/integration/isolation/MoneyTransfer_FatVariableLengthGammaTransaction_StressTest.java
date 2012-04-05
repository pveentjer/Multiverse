package org.multiverse.stms.gamma.integration.isolation;

import org.junit.Test;
import org.multiverse.api.TransactionExecutor;
import org.multiverse.api.LockMode;
import org.multiverse.stms.gamma.LeanGammaTransactionExecutor;
import org.multiverse.stms.gamma.transactions.GammaTransactionConfiguration;
import org.multiverse.stms.gamma.transactions.fat.FatVariableLengthGammaTransactionFactory;

public class MoneyTransfer_FatVariableLengthGammaTransaction_StressTest extends MoneyTransfer_AbstractTest {

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
    protected TransactionExecutor newTransactionExecutor() {
        GammaTransactionConfiguration config = new GammaTransactionConfiguration(stm)
                .setReadLockMode(lockMode);
        return new LeanGammaTransactionExecutor(new FatVariableLengthGammaTransactionFactory(config));
    }
}
