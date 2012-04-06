package org.multiverse.stms.gamma.integration.locking;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.exceptions.ReadWriteConflict;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactionalobjects.GammaTxnLong;
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
        GammaTxnLong ref = new GammaTxnLong(stm);

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
        GammaTxnLong ref1 = new GammaTxnLong(stm, initialValue);
        GammaTxnLong ref2 = new GammaTxnLong(stm, initialValue);

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
        GammaTxnLong ref1 = new GammaTxnLong(stm, initialValue);
        GammaTxnLong ref2 = new GammaTxnLong(stm, initialValue);

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

