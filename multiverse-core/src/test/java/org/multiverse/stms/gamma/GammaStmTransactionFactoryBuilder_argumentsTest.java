package org.multiverse.stms.gamma;

import org.junit.Before;
import org.junit.Test;

public class GammaStmTransactionFactoryBuilder_argumentsTest {
    private GammaStm stm;

    @Before
    public void setUp() {
        stm = new GammaStm();
    }

    @Test(expected = NullPointerException.class)
    public void whenNullBackoffPolicy_thenNullPointerException() {
        stm.newTxnFactoryBuilder().setBackoffPolicy(null);
    }

    @Test(expected = NullPointerException.class)
    public void whenNullFamilyName_thenNullPointerException() {
        stm.newTxnFactoryBuilder().setFamilyName(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenMaxRetriesNegative_thenIllegalArgumentException() {
        stm.newTxnFactoryBuilder().setMaxRetries(-1);
    }

    @Test(expected = NullPointerException.class)
    public void whenNullReadLockLevel_thenNullPointerException() {
        stm.newTxnFactoryBuilder().setReadLockMode(null);
    }

    @Test(expected = NullPointerException.class)
    public void whenNullWriteLockLevel_thenNullPointerException() {
        stm.newTxnFactoryBuilder().setWriteLockMode(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenNegativeSpinCount_thenIllegalArgumentException() {
        stm.newTxnFactoryBuilder().setSpinCount(-1);
    }

    @Test(expected = NullPointerException.class)
    public void whenNullTraceLevel_thenIllegalArgumentException() {
        stm.newTxnFactoryBuilder().setTraceLevel(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenNegativeTimeout_thenIllegalArgumentException() {
        stm.newTxnFactoryBuilder().setTimeoutNs(-1);
    }

    @Test(expected = NullPointerException.class)
    public void whenAddNullPermanentListener_thenNullPointerException() {
        stm.newTxnFactoryBuilder().addPermanentListener(null);
    }
}
