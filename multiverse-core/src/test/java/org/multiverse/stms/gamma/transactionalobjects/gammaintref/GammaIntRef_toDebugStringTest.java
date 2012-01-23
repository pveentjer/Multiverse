package org.multiverse.stms.gamma.transactionalobjects.gammaintref;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactionalobjects.GammaIntRef;

import static org.junit.Assert.assertEquals;

public class GammaIntRef_toDebugStringTest {

    private GammaStm stm;

    @Before
    public void setUp() {
        stm = new GammaStm();
    }

    @Test
    public void test() {
        GammaIntRef ref = new GammaIntRef(stm);
        String s = ref.toDebugString();
        assertEquals("GammaIntRef{orec=Orec(hasExclusiveLock=false, hasWriteLock=false, readLocks=0, surplus=0, " +
                "isReadBiased=false, readonlyCount=0), version=1, value=0, hasListeners=false)", s);
    }
}
