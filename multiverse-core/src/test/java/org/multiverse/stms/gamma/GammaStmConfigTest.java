package org.multiverse.stms.gamma;

import org.junit.Test;
import org.multiverse.api.LockMode;

public class GammaStmConfigTest {

    @Test(expected = IllegalStateException.class)
    public void readBiasedThreshold_whenNegative() {
        GammaStmConfig config = new GammaStmConfig();
        config.readBiasedThreshold = -1;
        config.validate();
    }

    @Test(expected = IllegalStateException.class)
    public void readBiasedThreshold_whenTooBig() {
        GammaStmConfig config = new GammaStmConfig();
        config.readBiasedThreshold = 1024;
        config.validate();
    }

    @Test(expected = IllegalStateException.class)
    public void propagationLevel_whenNull() {
        GammaStmConfig config = new GammaStmConfig();
        config.propagationLevel = null;
        config.validate();
    }

    @Test(expected = IllegalStateException.class)
    public void isolationLevel_whenNull() {
        GammaStmConfig config = new GammaStmConfig();
        config.isolationLevel = null;
        config.validate();
    }

    @Test(expected = IllegalStateException.class)
    public void readLockMode_whenNull() {
        GammaStmConfig config = new GammaStmConfig();
        config.readLockMode = null;
        config.validate();
    }

    @Test(expected = IllegalStateException.class)
    public void writeLockMode_whenNull() {
        GammaStmConfig config = new GammaStmConfig();
        config.writeLockMode = null;
        config.validate();
    }

    @Test(expected = IllegalStateException.class)
    public void writeLockMode_whenSmallerThanReadLockMode() {
        GammaStmConfig config = new GammaStmConfig();
        config.writeLockMode = LockMode.None;
        config.readLockMode = LockMode.Exclusive;
        config.validate();
    }

    @Test(expected = IllegalStateException.class)
    public void backoffPolicy_whenNull() {
        GammaStmConfig config = new GammaStmConfig();
        config.backoffPolicy = null;
        config.validate();
    }

    @Test(expected = IllegalStateException.class)
    public void traceLevel_whenNull() {
        GammaStmConfig config = new GammaStmConfig();
        config.traceLevel = null;
        config.validate();
    }

    @Test(expected = IllegalStateException.class)
    public void maximumPoorMansConflictScanLength_whenNegative() {
        GammaStmConfig config = new GammaStmConfig();
        config.maximumPoorMansConflictScanLength = -1;
        config.validate();
    }

    @Test(expected = IllegalStateException.class)
    public void maximumFixedLengthTransactionSize_whenSmallerThan1() {
        GammaStmConfig config = new GammaStmConfig();
        config.maxFixedLengthTransactionSize = 0;
        config.validate();
    }

    @Test(expected = IllegalStateException.class)
    public void spinCount_whenSmallerThanZero() {
        GammaStmConfig config = new GammaStmConfig();
        config.spinCount = -1;
        config.validate();
    }

    @Test(expected = IllegalStateException.class)
    public void maxRetries_whenSmallerThanZero() {
        GammaStmConfig config = new GammaStmConfig();
        config.maxRetries = -1;
        config.validate();
    }

    @Test(expected = IllegalStateException.class)
    public void minimalVariableLengthTransactionSize_whenSmallerThan1() {
        GammaStmConfig config = new GammaStmConfig();
        config.minimalVariableLengthTransactionSize = 0;
        config.validate();
    }

    @Test(expected = IllegalStateException.class)
    public void blockingAllowed_whenNoReadTracking() {
        GammaStmConfig config = new GammaStmConfig();
        config.blockingAllowed = true;
        config.trackReads = false;
        config.validate();
    }

    @Test(expected = IllegalStateException.class)
    public void timeout_whenSmallerThanZero() {
        GammaStmConfig config = new GammaStmConfig();
        config.timeoutNs = -1;
        config.validate();
    }
}
