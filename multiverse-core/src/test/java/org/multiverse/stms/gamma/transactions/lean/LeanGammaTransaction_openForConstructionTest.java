package org.multiverse.stms.gamma.transactions.lean;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.exceptions.SpeculativeConfigurationError;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactionalobjects.GammaRef;
import org.multiverse.stms.gamma.transactions.GammaTransaction;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.multiverse.TestUtils.assertIsAborted;

public abstract class LeanGammaTransaction_openForConstructionTest<T extends GammaTransaction> {

    protected GammaStm stm;

    @Before
    public void setUp() {
        stm = new GammaStm();
    }

    public abstract T newTransaction();

    @Test
    public void whenOpenedForConstruction_thenSpeculativeConfigurationError() {
        T tx = newTransaction();
        try {
            new GammaRef(tx);
            fail();
        } catch (SpeculativeConfigurationError expected) {
        }

        assertIsAborted(tx);
        assertTrue(tx.getConfiguration().speculativeConfiguration.get().constructedObjectsDetected);
    }
}
