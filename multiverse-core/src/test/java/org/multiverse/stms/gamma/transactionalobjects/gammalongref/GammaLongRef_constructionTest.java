package org.multiverse.stms.gamma.transactionalobjects.gammalongref;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.exceptions.SpeculativeConfigurationError;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactionalobjects.GammaLongRef;
import org.multiverse.stms.gamma.transactions.fat.FatFixedLengthGammaTxn;
import org.multiverse.stms.gamma.transactions.fat.FatMonoGammaTxn;
import org.multiverse.stms.gamma.transactions.lean.LeanFixedLengthGammaTxn;
import org.multiverse.stms.gamma.transactions.lean.LeanMonoGammaTxn;

import static org.junit.Assert.*;
import static org.multiverse.TestUtils.assertIsAborted;
import static org.multiverse.TestUtils.assertIsActive;
import static org.multiverse.stms.gamma.GammaTestUtils.assertRefHasExclusiveLock;

public class GammaLongRef_constructionTest {

    private GammaStm stm;

    @Before
    public void setUp() {
        stm = new GammaStm();
    }

    @Test
    public void withTransaction_whenFatMonoGammaTxnUsed() {
        FatMonoGammaTxn tx = new FatMonoGammaTxn(stm);
        GammaLongRef ref = new GammaLongRef(tx, 10);

        assertIsActive(tx);
        assertRefHasExclusiveLock(ref, tx);
        assertTrue(tx.hasWrites);
        assertFalse(tx.config.speculativeConfiguration.get().constructedObjectsDetected);
    }

    @Test
    public void withTransaction_whenFatFixedLengthGammaTxnUsed() {
        FatFixedLengthGammaTxn tx = new FatFixedLengthGammaTxn(stm);
        GammaLongRef ref = new GammaLongRef(tx, 10);

        assertIsActive(tx);
        assertRefHasExclusiveLock(ref, tx);
        assertTrue(tx.hasWrites);
        assertFalse(tx.config.speculativeConfiguration.get().constructedObjectsDetected);
    }

    @Test
    public void withTransaction_whenFatVariableLengthGammaTxnUsed() {
        FatFixedLengthGammaTxn tx = new FatFixedLengthGammaTxn(stm);
        GammaLongRef ref = new GammaLongRef(tx, 10);

        assertIsActive(tx);
        assertRefHasExclusiveLock(ref, tx);
        assertTrue(tx.hasWrites);
        assertFalse(tx.config.speculativeConfiguration.get().constructedObjectsDetected);
    }

    @Test
    public void withTransaction_whenLeanFixedLengthGammaTxnUsed() {
        LeanFixedLengthGammaTxn tx = new LeanFixedLengthGammaTxn(stm);

        try {
            new GammaLongRef(tx, 10);
            fail();
        } catch (SpeculativeConfigurationError expected) {
        }

        assertIsAborted(tx);
        assertTrue(tx.config.speculativeConfiguration.get().constructedObjectsDetected);
    }

    @Test
    public void withTransaction_whenLeanMonoGammaTxnUsed() {
        LeanMonoGammaTxn tx = new LeanMonoGammaTxn(stm);

        try {
            new GammaLongRef(tx, 10);
            fail();
        } catch (SpeculativeConfigurationError expected) {
        }

        assertIsAborted(tx);
        assertTrue(tx.config.speculativeConfiguration.get().constructedObjectsDetected);
    }
}
