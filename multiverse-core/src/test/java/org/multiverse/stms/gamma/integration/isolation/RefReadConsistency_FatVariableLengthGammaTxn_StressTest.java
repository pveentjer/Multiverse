package org.multiverse.stms.gamma.integration.isolation;

import org.junit.Test;
import org.multiverse.api.TxnExecutor;
import org.multiverse.stms.gamma.LeanGammaTxnExecutor;
import org.multiverse.stms.gamma.transactions.GammaTxnConfig;
import org.multiverse.stms.gamma.transactions.fat.FatVariableLengthGammaTxnFactory;

public class RefReadConsistency_FatVariableLengthGammaTxn_StressTest extends RefReadConsistency_AbstractTest {

    private int refCount;
    private boolean poorMansReadConsistency;

    @Test
    public void poorMansConflictScan_testWith2Refs() {
        refCount = 2;
        poorMansReadConsistency = true;
        run(refCount);
    }

    @Test
    public void poorMansConflictScan_testWith4Refs() {
        refCount = 4;
        poorMansReadConsistency = true;
        run(refCount);
    }

    @Test
    public void poorMansConflictScan_testWith8Refs() {
        refCount = 8;
        poorMansReadConsistency = true;
        run(refCount);
    }

    @Test
    public void poorMansConflictScan_testWith16Refs() {
        refCount = 16;
        poorMansReadConsistency = true;
        run(refCount);
    }

    @Test
    public void poorMansConflictScan_testWith32Refs() {
        refCount = 32;
        poorMansReadConsistency = true;
        run(refCount);
    }

    @Test
    public void poorMansConflictScan_testWith128Refs() {
        refCount = 128;
        poorMansReadConsistency = true;
        run(refCount);
    }

    @Test
    public void poorMansConflictScan_testWith512Refs() {
        refCount = 512;
        poorMansReadConsistency = true;
        run(refCount);
    }

    @Test
    public void poorMansConflictScan_testWith2048Refs() {
        poorMansReadConsistency = true;
        refCount = 2048;
        run(refCount);
    }

    @Test
    public void richMansConflictScan_testWith2Refs() {
        refCount = 2;
        poorMansReadConsistency = false;
        run(refCount);
    }

    @Test
    public void richMansConflictScan_testWith4Refs() {
        refCount = 4;
        poorMansReadConsistency = false;
        run(refCount);
    }

    @Test
    public void richMansConflictScan_testWith8Refs() {
        refCount = 8;
        poorMansReadConsistency = false;
        run(refCount);
    }

    @Test
    public void richMansConflictScan_testWith16Refs() {
        refCount = 16;
        poorMansReadConsistency = false;
        run(refCount);
    }

    @Test
    public void richMansConflictScan_testWith32Refs() {
        refCount = 32;
        poorMansReadConsistency = false;
        run(refCount);
    }

    @Test
    public void richMansConflictScan_testWith128Refs() {
        refCount = 128;
        poorMansReadConsistency = false;
        run(refCount);
    }

    @Test
    public void richMansConflictScan_testWith512Refs() {
        refCount = 512;
        poorMansReadConsistency = false;
        run(refCount);
    }

    @Test
    public void richMansConflictScan_testWith2048Refs() {
        poorMansReadConsistency = false;
        refCount = 2048;
        run(refCount);
    }

    @Override
    protected TxnExecutor createReadBlock() {
        GammaTxnConfig config = new GammaTxnConfig(stm)
                .setMaxRetries(10000)
                .setMaximumPoorMansConflictScanLength(poorMansReadConsistency ? Integer.MAX_VALUE : 0);
        return new LeanGammaTxnExecutor(new FatVariableLengthGammaTxnFactory(config));
    }

    @Override
    protected TxnExecutor createWriteBlock() {
        GammaTxnConfig config = new GammaTxnConfig(stm)
                .setMaxRetries(10000)
                .setMaximumPoorMansConflictScanLength(poorMansReadConsistency ? Integer.MAX_VALUE : 0);
        return new LeanGammaTxnExecutor(new FatVariableLengthGammaTxnFactory(config));
    }
}
