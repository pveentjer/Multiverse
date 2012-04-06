package org.multiverse.stms.gamma.transactionalobjects.basegammaref;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.exceptions.SpeculativeConfigurationError;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactionalobjects.GammaRef;
import org.multiverse.stms.gamma.transactions.lean.LeanFixedLengthGammaTxn;
import org.multiverse.stms.gamma.transactions.lean.LeanMonoGammaTxn;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.multiverse.TestUtils.assertIsAborted;

public class BaseGammaRef_ensureLeanTransactionTest {

    private GammaStm stm;

    @Before
    public void setUp() {
        stm = new GammaStm();
    }

    @Test
    public void whenLeanMonoGammaTxnUsed() {
        GammaRef<String> ref = new GammaRef<String>(stm, null);

        LeanMonoGammaTxn tx = new LeanMonoGammaTxn(stm);
        try {
            ref.ensure(tx);
            fail();
        } catch (SpeculativeConfigurationError expected) {
        }

        assertIsAborted(tx);
        assertTrue(tx.config.speculativeConfiguration.get().ensureDetected);
    }

    @Test
    public void whenLeanFixedLengthGammaTxnUsed() {
        GammaRef<String> ref = new GammaRef<String>(stm, null);

        LeanFixedLengthGammaTxn tx = new LeanFixedLengthGammaTxn(stm);
        try {
            ref.ensure(tx);
            fail();
        } catch (SpeculativeConfigurationError expected) {
        }

        assertIsAborted(tx);
        assertTrue(tx.config.speculativeConfiguration.get().ensureDetected);
    }
}
