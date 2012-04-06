package org.multiverse.stms.gamma;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.IsolationLevel;
import org.multiverse.api.LockMode;
import org.multiverse.stms.gamma.transactions.GammaTxnFactoryBuilder;

import static org.junit.Assert.fail;

public class GammaStm_transactionFactoryValidationTest {
    private GammaStm stm;

    @Before
    public void setUp() {
        stm = new GammaStm();
    }

    @Test
    public void whenBlockingEnabled_thenAutomaticReadTrackingShouldBeEnabled() {
        GammaTxnFactoryBuilder builder = stm.newTransactionFactoryBuilder()
                .setReadTrackingEnabled(false)
                .setBlockingAllowed(true);

        try {
            builder.newTransactionFactory();
            fail();
        } catch (IllegalStateException expected) {
        }
    }

    @Test
    public void whenWriteSkewAllowed_thenAutomaticReadTrackingShouldBeEnabled() {
        GammaTxnFactoryBuilder builder = stm.newTransactionFactoryBuilder()
                .setReadonly(false)
                .setReadTrackingEnabled(false)
                .setIsolationLevel(IsolationLevel.Serializable);

        try {
            builder.newTransactionFactory();
            fail();
        } catch (IllegalStateException expected) {
        }
    }

    @Test
    public void whenWriteSkewAllowedAndReadonly_thenThenAutomaticReadTrackingDoesntMatter() {
        whenWriteSkewAllowedAndReadonly(true);
        whenWriteSkewAllowedAndReadonly(false);
    }

    private void whenWriteSkewAllowedAndReadonly(boolean readTrackingEnabled) {
        GammaTxnFactoryBuilder builder = stm.newTransactionFactoryBuilder()
                .setBlockingAllowed(false)
                .setReadonly(true)
                .setReadTrackingEnabled(readTrackingEnabled)
                .setIsolationLevel(IsolationLevel.Serializable);

        builder.newTransactionFactory();
    }

    @Test
    public void whenLockLevelIsRead_thenAutomaticReadTrackingShouldBeEnabled() {
        GammaTxnFactoryBuilder builder = stm.newTransactionFactoryBuilder()
                .setReadTrackingEnabled(false)
                .setReadLockMode(LockMode.Read);

        try {
            builder.newTransactionFactory();
            fail();
        } catch (IllegalStateException expected) {
        }
    }
}
