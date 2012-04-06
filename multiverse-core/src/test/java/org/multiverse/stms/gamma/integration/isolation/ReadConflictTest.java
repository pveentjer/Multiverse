package org.multiverse.stms.gamma.integration.isolation;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.exceptions.ReadWriteConflict;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactionalobjects.GammaLongRef;
import org.multiverse.stms.gamma.transactions.GammaTxn;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.multiverse.TestUtils.assertIsAborted;
import static org.multiverse.TestUtils.assertIsCommitted;
import static org.multiverse.api.GlobalStmInstance.getGlobalStmInstance;
import static org.multiverse.api.TxnThreadLocal.clearThreadLocalTxn;

public class ReadConflictTest {
    private GammaStm stm;

    @Before
    public void setUp() {
        stm = (GammaStm) getGlobalStmInstance();
        clearThreadLocalTxn();
    }

    @Test
    public void whenAlreadyReadThenNoConflict() {
        GammaLongRef ref = new GammaLongRef(stm, 5);

        GammaTxn tx = stm.newDefaultTransaction();
        ref.get(tx);

        ref.atomicIncrementAndGet(1);

        long result = ref.get(tx);

        assertEquals(5, result);
        tx.commit();

        assertIsCommitted(tx);
    }

    @Test
    public void testCausalConsistency() {
        GammaLongRef ref1 = new GammaLongRef(stm, 0);
        GammaLongRef ref2 = new GammaLongRef(stm, 0);

        GammaTxn tx = stm.newDefaultTransaction();
        ref1.get(tx);

        GammaTxn otherTx = stm.newDefaultTransaction();
        ref1.set(otherTx, 1);
        ref2.set(otherTx, 1);
        otherTx.commit();

        try {
            ref2.get(tx);
            fail();
        } catch (ReadWriteConflict expected) {
        }

        assertIsAborted(tx);
    }
}
