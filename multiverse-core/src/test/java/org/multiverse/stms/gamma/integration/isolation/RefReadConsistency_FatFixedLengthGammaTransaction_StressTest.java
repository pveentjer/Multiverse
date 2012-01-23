package org.multiverse.stms.gamma.integration.isolation;

import org.junit.Test;
import org.multiverse.api.AtomicBlock;
import org.multiverse.stms.gamma.LeanGammaAtomicBlock;
import org.multiverse.stms.gamma.transactions.GammaTransactionConfiguration;
import org.multiverse.stms.gamma.transactions.fat.FatFixedLengthGammaTransactionFactory;

public class RefReadConsistency_FatFixedLengthGammaTransaction_StressTest extends RefReadConsistency_AbstractTest {

    private int refCount;
    private boolean poorMansReadConsistency;

    @Test
    public void poormansReadConsistency_with2Refs() {
        refCount = 2;
        poorMansReadConsistency = true;
        run(refCount);
    }

    @Test
    public void poormansReadConsistency_with4Refs() {
        refCount = 4;
        poorMansReadConsistency = true;
        run(refCount);
    }

    @Test
    public void poormansReadConsistency_with8Refs() {
        poorMansReadConsistency = true;
        refCount = 8;
        run(refCount);
    }

    @Test
    public void poormansReadConsistency_with16Refs() {
        poorMansReadConsistency = true;
        refCount = 16;
        run(refCount);
    }

    @Test
    public void poormansReadConsistency_with32Refs() {
        poorMansReadConsistency = true;
        refCount = 32;
        run(refCount);
    }

    @Test
    public void poormansReadConsistency_with64Refs() {
        poorMansReadConsistency = true;
        refCount = 64;
        run(refCount);
    }

    @Test
    public void richmansReadConsistency_with2Refs() {
        refCount = 2;
        poorMansReadConsistency = false;
        run(refCount);
    }

    @Test
    public void richmansReadConsistency_with4Refs() {
        refCount = 4;
        poorMansReadConsistency = false;
        run(refCount);
    }

    @Test
    public void richmansReadConsistency_with8Refs() {
        poorMansReadConsistency = false;
        refCount = 8;
        run(refCount);
    }

    @Test
    public void richmansReadConsistency_with16Refs() {
        poorMansReadConsistency = false;
        refCount = 16;
        run(refCount);
    }

    @Test
    public void richmansReadConsistency_with32Refs() {
        poorMansReadConsistency = false;
        refCount = 32;
        run(refCount);
    }

    @Test
    public void richmansReadConsistency_with64Refs() {
        poorMansReadConsistency = false;
        refCount = 64;
        run(refCount);
    }

    @Override
    protected AtomicBlock createReadBlock() {
        GammaTransactionConfiguration config = new GammaTransactionConfiguration(stm, refCount)
                .setDirtyCheckEnabled(false)
                .setMaximumPoorMansConflictScanLength(poorMansReadConsistency ? Integer.MAX_VALUE : 0);

        return new LeanGammaAtomicBlock(new FatFixedLengthGammaTransactionFactory(config));
    }

    @Override
    protected AtomicBlock createWriteBlock() {
        GammaTransactionConfiguration config = new GammaTransactionConfiguration(stm, refCount)
                .setDirtyCheckEnabled(false)
                .setMaximumPoorMansConflictScanLength(poorMansReadConsistency ? Integer.MAX_VALUE : 0);

        return new LeanGammaAtomicBlock(new FatFixedLengthGammaTransactionFactory(config));
    }
}
