package org.multiverse.stms.gamma.transactionalobjects.txnref;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactionalobjects.GammaTxnRef;

import static org.junit.Assert.assertEquals;

public class GammaTxnRef_toDebugStringTest {

    private GammaStm stm;

    @Before
    public void setUp() {
        stm = new GammaStm();
    }

    @Test
    public void testNullValue() {
        GammaTxnRef<String> ref = new GammaTxnRef<String>(stm);
        String s = ref.toDebugString();
        assertEquals("GammaTxnRef{orec=Orec(hasExclusiveLock=false, hasWriteLock=false, readLocks=0, surplus=0, " +
                "isReadBiased=false, readonlyCount=0), version=1, value=null, hasListeners=false)", s);
    }

    @Test
    public void testNonNullValue() {
        GammaTxnRef<String> ref = new GammaTxnRef<String>(stm,"foo");
        String s = ref.toDebugString();
        assertEquals("GammaTxnRef{orec=Orec(hasExclusiveLock=false, hasWriteLock=false, readLocks=0, surplus=0, " +
                "isReadBiased=false, readonlyCount=0), version=1, value=foo, hasListeners=false)", s);
    }
}
