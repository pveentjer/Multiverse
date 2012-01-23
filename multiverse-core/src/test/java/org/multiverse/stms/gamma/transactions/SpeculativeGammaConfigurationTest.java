package org.multiverse.stms.gamma.transactions;

import org.junit.Test;

import static org.junit.Assert.*;

public class SpeculativeGammaConfigurationTest {

    @Test
    public void whenLean() {
        SpeculativeGammaConfiguration config = new SpeculativeGammaConfiguration();

        assertFalse(config.fat);
        assertFalse(config.nonRefTypeDetected);
        assertFalse(config.commuteDetected);
        assertFalse(config.orelseDetected);
        assertFalse(config.listenersDetected);
        assertFalse(config.locksDetected);
        assertFalse(config.abortOnlyDetected);
        assertFalse(config.ensureDetected);
        assertFalse(config.constructedObjectsDetected);
        assertEquals(1, config.minimalLength);
    }

    @Test
    public void newWithNonRefType() {
        SpeculativeGammaConfiguration config = new SpeculativeGammaConfiguration()
                .newWithNonRefType();

        assertTrue(config.fat);
        assertTrue(config.nonRefTypeDetected);
        assertFalse(config.commuteDetected);
        assertFalse(config.orelseDetected);
        assertFalse(config.listenersDetected);
        assertFalse(config.locksDetected);
        assertFalse(config.abortOnlyDetected);
        assertFalse(config.ensureDetected);
        assertFalse(config.constructedObjectsDetected);
        assertEquals(1, config.minimalLength);
    }

    @Test
    public void newWithEnsure() {
        SpeculativeGammaConfiguration config = new SpeculativeGammaConfiguration()
                .newWithEnsure();

        assertTrue(config.fat);
        assertFalse(config.nonRefTypeDetected);
        assertFalse(config.commuteDetected);
        assertFalse(config.orelseDetected);
        assertFalse(config.listenersDetected);
        assertFalse(config.locksDetected);
        assertFalse(config.abortOnlyDetected);
        assertTrue(config.ensureDetected);
        assertFalse(config.constructedObjectsDetected);
        assertEquals(1, config.minimalLength);
    }

    @Test
    public void newWithAbortOnly() {
        SpeculativeGammaConfiguration config = new SpeculativeGammaConfiguration()
                .newWithAbortOnly();

        assertTrue(config.fat);
        assertFalse(config.nonRefTypeDetected);
        assertFalse(config.commuteDetected);
        assertFalse(config.orelseDetected);
        assertFalse(config.listenersDetected);
        assertFalse(config.locksDetected);
        assertTrue(config.abortOnlyDetected);
        assertFalse(config.ensureDetected);
        assertFalse(config.constructedObjectsDetected);
        assertEquals(1, config.minimalLength);
    }

    @Test
    public void newWithCommute() {
        SpeculativeGammaConfiguration config = new SpeculativeGammaConfiguration()
                .newWithCommute();

        assertTrue(config.fat);
        assertFalse(config.nonRefTypeDetected);
        assertTrue(config.commuteDetected);
        assertFalse(config.orelseDetected);
        assertFalse(config.listenersDetected);
        assertFalse(config.locksDetected);
        assertFalse(config.abortOnlyDetected);
        assertFalse(config.ensureDetected);
        assertFalse(config.constructedObjectsDetected);
        assertEquals(1, config.minimalLength);
    }

    @Test
    public void newListListeners() {
        SpeculativeGammaConfiguration config = new SpeculativeGammaConfiguration()
                .newWithListeners();

        assertTrue(config.fat);
        assertFalse(config.nonRefTypeDetected);
        assertFalse(config.commuteDetected);
        assertFalse(config.orelseDetected);
        assertTrue(config.listenersDetected);
        assertFalse(config.locksDetected);
        assertFalse(config.abortOnlyDetected);
        assertFalse(config.ensureDetected);
        assertFalse(config.constructedObjectsDetected);
        assertEquals(1, config.minimalLength);
    }

    @Test
    public void newWithOrElse() {
        SpeculativeGammaConfiguration config = new SpeculativeGammaConfiguration()
                .newWithOrElse();

        assertTrue(config.fat);
        assertFalse(config.nonRefTypeDetected);
        assertFalse(config.commuteDetected);
        assertTrue(config.orelseDetected);
        assertFalse(config.listenersDetected);
        assertFalse(config.locksDetected);
        assertFalse(config.abortOnlyDetected);
        assertFalse(config.ensureDetected);
        assertFalse(config.constructedObjectsDetected);
        assertEquals(1, config.minimalLength);
    }

    @Test
    public void newWithMinimalLength() {
        SpeculativeGammaConfiguration config = new SpeculativeGammaConfiguration()
                .newWithMinimalLength(10);

        assertFalse(config.fat);
        assertFalse(config.nonRefTypeDetected);
        assertFalse(config.commuteDetected);
        assertFalse(config.orelseDetected);
        assertFalse(config.listenersDetected);
        assertFalse(config.locksDetected);
        assertFalse(config.abortOnlyDetected);
        assertFalse(config.ensureDetected);
        assertFalse(config.constructedObjectsDetected);
        assertEquals(10, config.minimalLength);
    }

    @Test
    public void newWithLocks() {
        SpeculativeGammaConfiguration config = new SpeculativeGammaConfiguration()
                .newWithLocks();


        assertTrue(config.fat);
        assertFalse(config.nonRefTypeDetected);
        assertFalse(config.commuteDetected);
        assertFalse(config.orelseDetected);
        assertFalse(config.listenersDetected);
        assertTrue(config.locksDetected);
        assertFalse(config.abortOnlyDetected);
        assertFalse(config.ensureDetected);
        assertFalse(config.constructedObjectsDetected);
        assertEquals(1, config.minimalLength);
    }

    @Test
    public void newWithConstructedObjects() {
        SpeculativeGammaConfiguration config = new SpeculativeGammaConfiguration()
                .newWithConstructedObjects();


        assertTrue(config.fat);
        assertFalse(config.nonRefTypeDetected);
        assertFalse(config.commuteDetected);
        assertFalse(config.orelseDetected);
        assertFalse(config.listenersDetected);
        assertFalse(config.locksDetected);
        assertFalse(config.abortOnlyDetected);
        assertFalse(config.ensureDetected);
        assertTrue(config.constructedObjectsDetected);
        assertEquals(1, config.minimalLength);
    }
}
