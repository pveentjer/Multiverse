package org.multiverse.stms.gamma.integration.liveness;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.exceptions.ReadWriteConflict;
import org.multiverse.stms.gamma.GammaConstants;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactionalobjects.GammaLongRef;
import org.multiverse.stms.gamma.transactions.GammaTxn;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.multiverse.TestUtils.assertIsAborted;
import static org.multiverse.api.GlobalStmInstance.getGlobalStmInstance;
import static org.multiverse.api.TxnThreadLocal.clearThreadLocalTxn;

public class DeadLockTest implements GammaConstants {
    private GammaStm stm;

    @Before
    public void setUp() {
        clearThreadLocalTxn();
        stm = (GammaStm) getGlobalStmInstance();
    }

    @Test
    public void test() {
        GammaLongRef ref1 = new GammaLongRef(stm);
        GammaLongRef ref2 = new GammaLongRef(stm);

        GammaTxn tx1 = stm.newDefaultTxn();
        GammaTxn tx2 = stm.newDefaultTxn();

        ref1.openForWrite(tx1, LOCKMODE_EXCLUSIVE).long_value++;
        ref2.openForWrite(tx2, LOCKMODE_EXCLUSIVE).long_value++;

        try {
            ref2.openForWrite(tx1, LOCKMODE_EXCLUSIVE);
            fail();
        } catch (ReadWriteConflict expected) {
        }

        assertIsAborted(tx1);

        ref1.openForWrite(tx2, LOCKMODE_EXCLUSIVE).long_value++;
        tx2.commit();

        assertEquals(1, ref1.atomicGet());
        assertEquals(1, ref2.atomicGet());
    }
}
