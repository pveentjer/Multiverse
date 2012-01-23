package org.multiverse.stms.gamma.transactionalobjects.gammaref;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.exceptions.SpeculativeConfigurationError;
import org.multiverse.stms.gamma.GammaConstants;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.GammaTestUtils;
import org.multiverse.stms.gamma.transactionalobjects.GammaRef;
import org.multiverse.stms.gamma.transactions.fat.FatFixedLengthGammaTransaction;
import org.multiverse.stms.gamma.transactions.fat.FatMonoGammaTransaction;
import org.multiverse.stms.gamma.transactions.lean.LeanFixedLengthGammaTransaction;
import org.multiverse.stms.gamma.transactions.lean.LeanMonoGammaTransaction;

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
    public void withTransaction_whenFatMonoGammaTransactionUsed() {
        FatMonoGammaTransaction tx = new FatMonoGammaTransaction(stm);
        GammaRef ref = new GammaRef(tx, 10);

        assertIsActive(tx);
        assertRefHasExclusiveLock(ref, tx);
        assertTrue(tx.hasWrites);
        assertFalse(tx.config.speculativeConfiguration.get().constructedObjectsDetected);
    }

    @Test
    public void withTransaction_whenFatFixedLengthGammaTransactionUsed() {
        FatFixedLengthGammaTransaction tx = new FatFixedLengthGammaTransaction(stm);
        GammaRef ref = new GammaRef(tx, 10);

        assertIsActive(tx);
        assertRefHasExclusiveLock(ref, tx);
        assertTrue(tx.hasWrites);
        assertFalse(tx.config.speculativeConfiguration.get().constructedObjectsDetected);
    }

    @Test
    public void withTransaction_whenFatVariableLengthGammaTransactionUsed() {
        FatFixedLengthGammaTransaction tx = new FatFixedLengthGammaTransaction(stm);
        GammaRef ref = new GammaRef(tx, 10);

        assertIsActive(tx);
        assertRefHasExclusiveLock(ref, tx);
        assertTrue(tx.hasWrites);
        assertFalse(tx.config.speculativeConfiguration.get().constructedObjectsDetected);
    }

    @Test
    public void withTransaction_whenLeanFixedLengthGammaTransactionUsed() {
        LeanFixedLengthGammaTransaction tx = new LeanFixedLengthGammaTransaction(stm);

        try {
            new GammaRef(tx, 10);
            fail();
        } catch (SpeculativeConfigurationError expected) {
        }

        assertIsAborted(tx);
        assertTrue(tx.config.speculativeConfiguration.get().constructedObjectsDetected);
    }

    @Test
    public void withTransaction_whenLeanMonoGammaTransactionUsed() {
        LeanMonoGammaTransaction tx = new LeanMonoGammaTransaction(stm);

        try {
            new GammaRef(tx, 10);
            fail();
        } catch (SpeculativeConfigurationError expected) {
        }

        assertIsAborted(tx);
        assertTrue(tx.config.speculativeConfiguration.get().constructedObjectsDetected);
    }
}
