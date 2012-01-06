package org.multiverse.stms.gamma.integration.isolation;

import org.junit.Test;
import org.multiverse.api.AtomicBlock;
import org.multiverse.api.LockMode;
import org.multiverse.stms.gamma.LeanGammaAtomicBlock;
import org.multiverse.stms.gamma.transactions.GammaTransactionConfiguration;
import org.multiverse.stms.gamma.transactions.fat.FatFixedLengthGammaTransactionFactory;

public class MoneyTransfer_FatFixedLengthGammaTransaction_StressTest extends MoneyTransfer_AbstractTest {

    private LockMode lockMode;
    private int accountCount;

    @Test
    public void when10AccountsAnd2ThreadsAndOptimistic() {
        lockMode = LockMode.None;
        accountCount = 10;
        run(accountCount, 2);
    }

    @Test
    public void when10AccountsAnd2ThreadsAndPessimistic() {
        lockMode = LockMode.Exclusive;
        accountCount = 10;
        run(accountCount, 2);
    }

    @Test
    public void when100AccountAnd10ThreadsAndOptimistic() {
        lockMode = LockMode.None;
        accountCount = 100;
        run(accountCount, 10);
    }

    @Test
    public void when100AccountAnd10ThreadsAndPessimistic() {
        lockMode = LockMode.Exclusive;
        accountCount = 100;
        run(accountCount, 10);
    }


    @Test
    public void when30AccountsAnd30ThreadsAndOptimistic() {
        lockMode = LockMode.None;
        accountCount = 30;
        run(accountCount, 30);
    }

    @Override
    protected AtomicBlock newAtomicBlock() {
        GammaTransactionConfiguration config = new GammaTransactionConfiguration(stm, accountCount)
                .setReadLockMode(lockMode);
        return new LeanGammaAtomicBlock(new FatFixedLengthGammaTransactionFactory(config));
    }
}
