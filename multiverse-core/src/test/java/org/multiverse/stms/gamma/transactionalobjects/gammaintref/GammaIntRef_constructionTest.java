package org.multiverse.stms.gamma.transactionalobjects.gammaintref;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.exceptions.SpeculativeConfigurationError;
import org.multiverse.stms.gamma.GammaConstants;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.GammaTestUtils;
import org.multiverse.stms.gamma.transactionalobjects.GammaIntRef;
import org.multiverse.stms.gamma.transactions.fat.FatFixedLengthGammaTransaction;
import org.multiverse.stms.gamma.transactions.fat.FatMonoGammaTransaction;
import org.multiverse.stms.gamma.transactions.lean.LeanFixedLengthGammaTransaction;
import org.multiverse.stms.gamma.transactions.lean.LeanMonoGammaTransaction;

import static org.junit.Assert.*;
import static org.multiverse.TestUtils.assertIsAborted;
import static org.multiverse.TestUtils.assertIsActive;
import static org.multiverse.stms.gamma.GammaTestUtils.*;

public class GammaIntRef_constructionTest {

    private GammaStm stm;

    @Before
    public void setUp() {
        stm = new GammaStm();
    }

    @Test
    public void whenInitialValueUsed() {
        int initialValue = 10;
        GammaIntRef ref = new GammaIntRef(stm, initialValue);

        GammaTestUtils.assertVersionAndValue(ref, GammaConstants.VERSION_UNCOMMITTED + 1, initialValue);
        assertRefHasNoLocks(ref);
        assertReadonlyCount(ref, 0);
        assertSurplus(ref, 0);
    }

    @Test
    public void whenDefaultValueUsed() {
        GammaIntRef ref = new GammaIntRef(stm);

        assertVersionAndValue(ref, GammaConstants.VERSION_UNCOMMITTED + 1, 0);
        assertRefHasNoLocks(ref);
        assertReadonlyCount(ref, 0);
        assertSurplus(ref, 0);
    }

    @Test
    public void withTransaction_whenFatMonoGammaTransactionUsed() {
        FatMonoGammaTransaction tx = new FatMonoGammaTransaction(stm);
        GammaIntRef ref = new GammaIntRef(tx, 10);

        assertIsActive(tx);
        assertRefHasExclusiveLock(ref, tx);
        assertTrue(tx.hasWrites);
        assertFalse(tx.config.speculativeConfiguration.get().constructedObjectsDetected);
    }

    @Test
    public void withTransaction_whenFatFixedLengthGammaTransactionUsed() {
        FatFixedLengthGammaTransaction tx = new FatFixedLengthGammaTransaction(stm);
        GammaIntRef ref = new GammaIntRef(tx, 10);

        assertIsActive(tx);
        assertRefHasExclusiveLock(ref, tx);
        assertTrue(tx.hasWrites);
        assertFalse(tx.config.speculativeConfiguration.get().constructedObjectsDetected);
    }

    @Test
    public void withTransaction_whenFatVariableLengthGammaTransactionUsed() {
        FatFixedLengthGammaTransaction tx = new FatFixedLengthGammaTransaction(stm);
        GammaIntRef ref = new GammaIntRef(tx, 10);

        assertIsActive(tx);
        assertRefHasExclusiveLock(ref, tx);
        assertTrue(tx.hasWrites);
        assertFalse(tx.config.speculativeConfiguration.get().constructedObjectsDetected);
    }

    @Test
    public void withTransaction_whenLeanFixedLengthGammaTransactionUsed() {
        LeanFixedLengthGammaTransaction tx = new LeanFixedLengthGammaTransaction(stm);

        try {
            new GammaIntRef(tx, 10);
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
            new GammaIntRef(tx, 10);
            fail();
        } catch (SpeculativeConfigurationError expected) {
        }

        assertIsAborted(tx);
        assertTrue(tx.config.speculativeConfiguration.get().constructedObjectsDetected);
    }
}
