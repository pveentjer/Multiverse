package org.multiverse.stms.gamma.transactionalobjects.gammaref;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactionalobjects.GammaRef;

import static org.junit.Assert.assertEquals;

public class GammaRef_toDebugStringTest {

    private GammaStm stm;

    @Before
    public void setUp() {
        stm = new GammaStm();
    }

    @Test
    public void testNullValue() {
        GammaRef<String> ref = new GammaRef<String>(stm);
        String s = ref.toDebugString();
        assertEquals("GammaRef{orec=Orec(hasExclusiveLock=false, hasWriteLock=false, readLocks=0, surplus=0, " +
                "isReadBiased=false, readonlyCount=0), version=1, value=null, hasListeners=false)", s);
    }

    @Test
    public void testNonNullValue() {
        GammaRef<String> ref = new GammaRef<String>(stm,"foo");
        String s = ref.toDebugString();
        assertEquals("GammaRef{orec=Orec(hasExclusiveLock=false, hasWriteLock=false, readLocks=0, surplus=0, " +
                "isReadBiased=false, readonlyCount=0), version=1, value=foo, hasListeners=false)", s);
    }
}
