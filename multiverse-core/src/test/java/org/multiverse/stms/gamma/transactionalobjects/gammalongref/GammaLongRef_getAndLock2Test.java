package org.multiverse.stms.gamma.transactionalobjects.gammalongref;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.LockMode;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactionalobjects.GammaRef;
import org.multiverse.stms.gamma.transactions.GammaTxn;

import static org.junit.Assert.assertSame;
import static org.multiverse.stms.gamma.GammaTestUtils.assertRefHasLockMode;
import static org.multiverse.stms.gamma.GammaTestUtils.assertVersionAndValue;


public class GammaLongRef_getAndLock2Test {

     private GammaStm stm;

    @Before
    public void setUp(){
        stm = new GammaStm();
    }

    @Test
    public void whenLockFree(){
        whenLockFree(LockMode.None);
        whenLockFree(LockMode.Read);
        whenLockFree(LockMode.Write);
        whenLockFree(LockMode.Exclusive);
    }

    public void whenLockFree(LockMode lockMode){
        String initialValue = "initialValue";
        GammaRef<String> ref = new GammaRef<String>(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTxn tx = stm.newDefaultTransaction();
        String result = ref.getAndLock(tx, lockMode);

        assertSame(initialValue, result);
        assertVersionAndValue(ref,initialVersion, initialValue);
        assertRefHasLockMode(ref, tx, lockMode.asInt());
    }
}
