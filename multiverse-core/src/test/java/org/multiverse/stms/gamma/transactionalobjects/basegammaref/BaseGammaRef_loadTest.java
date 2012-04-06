package org.multiverse.stms.gamma.transactionalobjects.basegammaref;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.LockMode;
import org.multiverse.stms.gamma.GammaConstants;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactionalobjects.GammaLongRef;
import org.multiverse.stms.gamma.transactionalobjects.GammaRef;
import org.multiverse.stms.gamma.transactionalobjects.GammaRefTranlocal;
import org.multiverse.stms.gamma.transactions.GammaTxn;

import static org.junit.Assert.*;
import static org.multiverse.stms.gamma.GammaTestUtils.assertLockMode;
import static org.multiverse.stms.gamma.GammaTestUtils.assertSurplus;

public class BaseGammaRef_loadTest implements GammaConstants {

    private GammaStm stm;

    @Before
    public void setUp() {
        stm = new GammaStm();
    }

    @Test
    public void whenLongRef() {
        whenLongRef(LockMode.None, true);
        whenLongRef(LockMode.None, false);
        whenLongRef(LockMode.Read, true);
        whenLongRef(LockMode.Read, false);
        whenLongRef(LockMode.Write, true);
        whenLongRef(LockMode.Write, false);
        whenLongRef(LockMode.Exclusive, true);
        whenLongRef(LockMode.Exclusive, false);
    }

    public void whenLongRef(LockMode lockMode, boolean arriveNeeded) {
        long initialValue = 100;
        GammaLongRef ref = new GammaLongRef(stm, initialValue);
        GammaTxn tx = stm.newDefaultTxn();
        long initialVersion = ref.getVersion();

        GammaRefTranlocal tranlocal = new GammaRefTranlocal();

        boolean result = ref.load(tx,tranlocal, lockMode.asInt(), 1, arriveNeeded);

        assertTrue(result);
        assertSame(ref, tranlocal.owner);
        assertEquals(lockMode.asInt(), tranlocal.lockMode);
        assertEquals(initialVersion, tranlocal.version);
        assertEquals(initialValue, tranlocal.long_value);
        assertEquals(initialValue, tranlocal.long_oldValue);

        if (arriveNeeded || lockMode.asInt() > LOCKMODE_NONE) {
            assertSurplus(ref, 1);
            assertTrue(tranlocal.hasDepartObligation);
        } else {
            assertSurplus(ref, 0);
            assertFalse(tranlocal.hasDepartObligation);
        }
        assertLockMode(ref, lockMode);
    }

    @Test
    public void whenRef() {
        whenRef(LockMode.None, true);
        whenRef(LockMode.None, false);
        whenRef(LockMode.Read, true);
        whenRef(LockMode.Read, false);
        whenRef(LockMode.Write, true);
        whenRef(LockMode.Write, false);
        whenRef(LockMode.Exclusive, true);
        whenRef(LockMode.Exclusive, false);
    }

    public void whenRef(LockMode lockMode, boolean arriveNeeded) {
        String initialValue = "foo";
        GammaRef<String> ref = new GammaRef<String>(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTxn tx = stm.newDefaultTxn();
        GammaRefTranlocal tranlocal = new GammaRefTranlocal();

        boolean result = ref.load(tx,tranlocal, lockMode.asInt(), 1, arriveNeeded);

        assertTrue(result);
        assertSame(ref, tranlocal.owner);
        assertEquals(lockMode.asInt(), tranlocal.lockMode);
        assertEquals(initialVersion, tranlocal.version);
        assertSame(initialValue, tranlocal.ref_value);
        assertSame(initialValue, tranlocal.ref_oldValue);
        if (arriveNeeded || lockMode.asInt() > LOCKMODE_NONE) {
            assertSurplus(ref, 1);
            assertTrue(tranlocal.hasDepartObligation);
        } else {
            assertSurplus(ref, 0);
            assertFalse(tranlocal.hasDepartObligation);
        }
        assertLockMode(ref, lockMode);
    }
}
