package org.multiverse.stms.gamma.transactionalobjects.gammaref;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.exceptions.SpeculativeConfigurationError;
import org.multiverse.stms.gamma.GammaConstants;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.GammaTestUtils;
import org.multiverse.stms.gamma.transactionalobjects.GammaRef;
import org.multiverse.stms.gamma.transactions.fat.FatFixedLengthGammaTxn;
import org.multiverse.stms.gamma.transactions.fat.FatMonoGammaTxn;
import org.multiverse.stms.gamma.transactions.lean.LeanFixedLengthGammaTxn;
import org.multiverse.stms.gamma.transactions.lean.LeanMonoGammaTxn;

import static org.junit.Assert.*;
import static org.multiverse.TestUtils.assertIsAborted;
import static org.multiverse.TestUtils.assertIsActive;
import static org.multiverse.stms.gamma.GammaTestUtils.*;

public class GammaRef_constructionTest {

    private GammaStm stm;

    @Before
    public void setUp() {
        stm = new GammaStm();
    }

    @Test
    public void whenInitialValueUsed() {
        String initialValue = "foo";
        GammaRef<String> ref = new GammaRef<String>(stm, initialValue);

        GammaTestUtils.assertVersionAndValue(ref, GammaConstants.VERSION_UNCOMMITTED + 1, initialValue);
        assertRefHasNoLocks(ref);
        assertReadonlyCount(ref, 0);
        assertSurplus(ref, 0);
    }

    @Test
    public void whenDefaultValueUsed() {
        GammaRef<String> ref = new GammaRef<String>(stm);

        assertVersionAndValue(ref, GammaConstants.VERSION_UNCOMMITTED + 1, null);
        assertRefHasNoLocks(ref);
        assertReadonlyCount(ref, 0);
        assertSurplus(ref, 0);
    }

    @Test
    public void withTransaction_whenFatMonoGammaTxnUsed() {
        FatMonoGammaTxn tx = new FatMonoGammaTxn(stm);
        GammaRef ref = new GammaRef(tx, 10);

        assertIsActive(tx);
        assertRefHasExclusiveLock(ref, tx);
        assertTrue(tx.hasWrites);
        assertFalse(tx.config.speculativeConfiguration.get().constructedObjectsDetected);
    }

    @Test
    public void withTransaction_whenFatFixedLengthGammaTxnUsed() {
        FatFixedLengthGammaTxn tx = new FatFixedLengthGammaTxn(stm);
        GammaRef ref = new GammaRef(tx, 10);

        assertIsActive(tx);
        assertRefHasExclusiveLock(ref, tx);
        assertTrue(tx.hasWrites);
        assertFalse(tx.config.speculativeConfiguration.get().constructedObjectsDetected);
    }

    @Test
    public void withTransaction_whenFatVariableLengthGammaTxnUsed() {
        FatFixedLengthGammaTxn tx = new FatFixedLengthGammaTxn(stm);
        GammaRef ref = new GammaRef(tx, 10);

        assertIsActive(tx);
        assertRefHasExclusiveLock(ref, tx);
        assertTrue(tx.hasWrites);
        assertFalse(tx.config.speculativeConfiguration.get().constructedObjectsDetected);
    }

    @Test
    public void withTransaction_whenLeanFixedLengthGammaTxnUsed() {
        LeanFixedLengthGammaTxn tx = new LeanFixedLengthGammaTxn(stm);

        try {
            new GammaRef(tx, 10);
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
            new GammaRef(tx, 10);
            fail();
        } catch (SpeculativeConfigurationError expected) {
        }

        assertIsAborted(tx);
        assertTrue(tx.config.speculativeConfiguration.get().constructedObjectsDetected);
    }
}
