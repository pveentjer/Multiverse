package org.multiverse.stms.gamma.transactions.fat;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.exceptions.*;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactionalobjects.GammaTxnLong;
import org.multiverse.stms.gamma.transactionalobjects.Tranlocal;
import org.multiverse.stms.gamma.transactions.GammaTxn;
import org.multiverse.stms.gamma.transactions.GammaTxnConfig;

import static org.junit.Assert.*;
import static org.multiverse.TestUtils.assertIsAborted;
import static org.multiverse.TestUtils.assertIsCommitted;
import static org.multiverse.stms.gamma.GammaTestUtils.*;

public abstract class FatGammaTxn_openForConstructionTest<T extends GammaTxn> {
    protected GammaStm stm;

    @Before
    public void setUp() {
        stm = new GammaStm();
    }

    protected abstract T newTransaction();

    protected abstract T newTransaction(GammaTxnConfig config);

    @Test
    public void whenReadonlyTransaction() {
        GammaTxnLong ref = new GammaTxnLong(stm);

        GammaTxnConfig config = new GammaTxnConfig(stm)
                .setReadonly(true);

        GammaTxn tx = newTransaction(config);
        try {
            ref.openForConstruction(tx);
            fail();
        } catch (ReadonlyException expected) {
        }

        assertIsAborted(tx);
    }

    @Test
    public void whenSuccess() {
        T tx = newTransaction();
        long initialVersion = 10;
        GammaTxnLong ref = new GammaTxnLong(tx, initialVersion);
        Tranlocal tranlocal = tx.locate(ref);

        assertNotNull(tranlocal);
        assertRefHasExclusiveLock(ref, tx);
        assertTrue(tx.hasWrites);
        assertSame(ref, tranlocal.owner);
        assertEquals(LOCKMODE_EXCLUSIVE, tranlocal.getLockMode());
        assertEquals(TRANLOCAL_CONSTRUCTING, tranlocal.getMode());
        assertTrue(tranlocal.isDirty);
        assertRefHasExclusiveLock(ref, tx);
    }

    @Test
    public void whenAlreadyOpenedForConstruction() {
        T tx = newTransaction();
        long initialVersion = 10;
        GammaTxnLong ref = new GammaTxnLong(tx, initialVersion);
        Tranlocal tranlocal = ref.openForConstruction(tx);

        assertNotNull(tranlocal);
        assertRefHasExclusiveLock(ref, tx);
        assertTrue(tx.hasWrites);
        assertSame(ref, tranlocal.owner);
        assertEquals(LOCKMODE_EXCLUSIVE, tranlocal.getLockMode());
        assertEquals(TRANLOCAL_CONSTRUCTING, tranlocal.getMode());
        assertTrue(tranlocal.isDirty);
        assertRefHasExclusiveLock(ref, tx);
    }

    @Test
    public void whenStmMismatch() {
        GammaStm otherStm = new GammaStm();
        GammaTxnLong ref = new GammaTxnLong(otherStm);

        GammaTxn tx = newTransaction();
        try {
            ref.openForConstruction(tx);
            fail();
        } catch (StmMismatchException expected) {
        }

        assertIsAborted(tx);
    }

     // ==========================================

     @Test
    public void commuting_whenCommuting_thenFailure() {
        long initialValue = 10;

        T tx = newTransaction();
        tx.evaluatingCommute = true;

        try{
            new GammaTxnLong(tx, initialValue);
            fail();
        }catch(IllegalCommuteException expected){
        }

        assertIsAborted(tx);
    }

    @Test
    public void whenTransactionAlreadyPrepared() {
        GammaTxn tx = newTransaction();
        tx.prepare();

        try {
            new GammaTxnLong(tx);
            fail();
        } catch (PreparedTxnException expected) {
        }

        assertIsAborted(tx);
    }

    @Test
    public void whenTransactionAlreadyAborted() {
        GammaTxn tx = newTransaction();
        tx.abort();

        GammaTxnLong ref = new GammaTxnLong(stm);

        try {
            ref.openForConstruction(tx);
            fail();
        } catch (DeadTxnException expected) {
        }

        assertIsAborted(tx);
    }

    @Test
    public void whenTransactionAlreadyCommitted() {
        GammaTxn tx = newTransaction();
        tx.commit();

        GammaTxnLong ref = new GammaTxnLong(stm);

        try {
            ref.openForConstruction(tx);
            fail();
        } catch (DeadTxnException expected) {
        }

        assertIsCommitted(tx);
    }
}
