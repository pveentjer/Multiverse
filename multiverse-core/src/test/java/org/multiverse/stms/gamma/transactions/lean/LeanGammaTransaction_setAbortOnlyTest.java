package org.multiverse.stms.gamma.transactions.lean;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.exceptions.SpeculativeConfigurationError;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactions.GammaTransaction;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.multiverse.TestUtils.assertIsAborted;

public abstract class LeanGammaTransaction_setAbortOnlyTest<T extends GammaTransaction> {

    public GammaStm stm;

    public abstract T newTransaction();

    @Before
    public void setUp() {
        stm = new GammaStm();
    }

    @Test
    public void whenSetAbortOnlyCalled_thenSpeculativeConfigurationError() {
        T tx = newTransaction();

        try {
            tx.setAbortOnly();
            fail();
        } catch (SpeculativeConfigurationError expected) {
        }

        assertIsAborted(tx);
        assertTrue(tx.config.speculativeConfiguration.get().abortOnlyDetected);
    }
}
