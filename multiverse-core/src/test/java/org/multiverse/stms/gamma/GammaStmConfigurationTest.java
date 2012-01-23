package org.multiverse.stms.gamma;

import org.junit.Test;
import org.multiverse.api.LockMode;

public class GammaStmConfigurationTest {

    @Test(expected = IllegalStateException.class)
    public void readBiasedThreshold_whenNegative() {
        GammaStmConfiguration config = new GammaStmConfiguration();
        config.readBiasedThreshold = -1;
        config.validate();
    }

    @Test(expected = IllegalStateException.class)
    public void readBiasedThreshold_whenTooBig() {
        GammaStmConfiguration config = new GammaStmConfiguration();
        config.readBiasedThreshold = 1024;
        config.validate();
    }

    @Test(expected = IllegalStateException.class)
    public void propagationLevel_whenNull() {
        GammaStmConfiguration config = new GammaStmConfiguration();
        config.propagationLevel = null;
        config.validate();
    }

    @Test(expected = IllegalStateException.class)
    public void isolationLevel_whenNull() {
        GammaStmConfiguration config = new GammaStmConfiguration();
        config.isolationLevel = null;
        config.validate();
    }

    @Test(expected = IllegalStateException.class)
    public void readLockMode_whenNull() {
        GammaStmConfiguration config = new GammaStmConfiguration();
        config.readLockMode = null;
        config.validate();
    }

    @Test(expected = IllegalStateException.class)
    public void writeLockMode_whenNull() {
        GammaStmConfiguration config = new GammaStmConfiguration();
        config.writeLockMode = null;
        config.validate();
    }

    @Test(expected = IllegalStateException.class)
    public void writeLockMode_whenSmallerThanReadLockMode() {
        GammaStmConfiguration config = new GammaStmConfiguration();
        config.writeLockMode = LockMode.None;
        config.readLockMode = LockMode.Exclusive;
        config.validate();
    }

    @Test(expected = IllegalStateException.class)
    public void backoffPolicy_whenNull() {
        GammaStmConfiguration config = new GammaStmConfiguration();
        config.backoffPolicy = null;
        config.validate();
    }

    @Test(expected = IllegalStateException.class)
    public void traceLevel_whenNull() {
        GammaStmConfiguration config = new GammaStmConfiguration();
        config.traceLevel = null;
        config.validate();
    }

    @Test(expected = IllegalStateException.class)
    public void maximumPoorMansConflictScanLength_whenNegative() {
        GammaStmConfiguration config = new GammaStmConfiguration();
        config.maximumPoorMansConflictScanLength = -1;
        config.validate();
    }

    @Test(expected = IllegalStateException.class)
    public void maximumFixedLengthTransactionSize_whenSmallerThan1() {
        GammaStmConfiguration config = new GammaStmConfiguration();
        config.maxFixedLengthTransactionSize = 0;
        config.validate();
    }

    @Test(expected = IllegalStateException.class)
    public void spinCount_whenSmallerThanZero() {
        GammaStmConfiguration config = new GammaStmConfiguration();
        config.spinCount = -1;
        config.validate();
    }

    @Test(expected = IllegalStateException.class)
    public void maxRetries_whenSmallerThanZero() {
        GammaStmConfiguration config = new GammaStmConfiguration();
        config.maxRetries = -1;
        config.validate();
    }

    @Test(expected = IllegalStateException.class)
    public void minimalVariableLengthTransactionSize_whenSmallerThan1() {
        GammaStmConfiguration config = new GammaStmConfiguration();
        config.minimalVariableLengthTransactionSize = 0;
        config.validate();
    }

    @Test(expected = IllegalStateException.class)
    public void blockingAllowed_whenNoReadTracking() {
        GammaStmConfiguration config = new GammaStmConfiguration();
        config.blockingAllowed = true;
        config.trackReads = false;
        config.validate();
    }

    @Test(expected = IllegalStateException.class)
    public void timeout_whenSmallerThanZero() {
        GammaStmConfiguration config = new GammaStmConfiguration();
        config.timeoutNs = -1;
        config.validate();
    }
}
