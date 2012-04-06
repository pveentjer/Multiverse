package org.multiverse.stms.gamma.transactionalobjects.gammalongref;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.LockMode;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactionalobjects.GammaLongRef;
import org.multiverse.stms.gamma.transactions.GammaTxn;

import static org.junit.Assert.assertEquals;
import static org.multiverse.stms.gamma.GammaTestUtils.assertRefHasLockMode;
import static org.multiverse.stms.gamma.GammaTestUtils.assertVersionAndValue;

public class GammaLongRef_setAndLock3Test {

    private GammaStm stm;

    @Before
    public void setUp() {
        stm = new GammaStm();
    }

    @Test
    public void whenLockFree() {
        whenLockFree(LockMode.None);
        whenLockFree(LockMode.Read);
        whenLockFree(LockMode.Write);
        whenLockFree(LockMode.Exclusive);
    }

    public void whenLockFree(LockMode lockMode) {
        long initialValue = 10;
        GammaLongRef ref = new GammaLongRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTxn tx = stm.newDefaultTxn();
        long newValue = 20;
        long result = ref.setAndLock(tx, newValue, lockMode);

        assertEquals(newValue, result);
        assertVersionAndValue(ref, initialVersion, initialValue);
        assertRefHasLockMode(ref, tx, lockMode.asInt());
    }
}
