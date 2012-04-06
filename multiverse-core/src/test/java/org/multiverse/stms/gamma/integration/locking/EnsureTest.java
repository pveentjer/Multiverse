package org.multiverse.stms.gamma.integration.locking;

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
import static org.multiverse.stms.gamma.GammaTestUtils.assertRefHasNoLocks;

public class EnsureTest {

    private GammaStm stm;

    @Before
    public void setUp() {
        stm = (GammaStm) getGlobalStmInstance();
        clearThreadLocalTxn();
    }

    @Test
    public void whenOnlyReadsThenIgnored() {
        GammaLongRef ref = new GammaLongRef(stm);

        GammaTxn tx = stm.newDefaultTxn();
        ref.get(tx);
        ref.ensure(tx);

        ref.atomicIncrementAndGet(1);

        tx.commit();

        assertIsCommitted(tx);
        assertRefHasNoLocks(ref);
    }

    @Test
    public void whenUpdateTransactionButNoConflictOnRead_thenSuccess() {
        long initialValue = 10;
        GammaLongRef ref1 = new GammaLongRef(stm, initialValue);
        GammaLongRef ref2 = new GammaLongRef(stm, initialValue);

        GammaTxn tx = stm.newDefaultTxn();
        ref1.get(tx);
        ref1.ensure(tx);
        ref2.increment(tx);

        tx.commit();
        assertIsCommitted(tx);
        assertEquals(initialValue, ref1.atomicGet());
        assertEquals(initialValue + 1, ref2.atomicGet());
    }

    @Test
    public void whenUpdateTransactionAndConflictOnRead_thenReadWriteConflict() {
        long initialValue = 10;
        GammaLongRef ref1 = new GammaLongRef(stm, initialValue);
        GammaLongRef ref2 = new GammaLongRef(stm, initialValue);

        GammaTxn tx = stm.newDefaultTxn();
        ref1.get(tx);
        ref1.ensure(tx);
        ref2.increment(tx);

        ref1.atomicIncrementAndGet(1);

        try {
            tx.commit();
            fail();
        } catch (ReadWriteConflict expected) {

        }
        assertIsAborted(tx);
        assertEquals(initialValue + 1, ref1.atomicGet());
        assertEquals(initialValue, ref2.atomicGet());
    }
}

