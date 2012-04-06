package org.multiverse.stms.gamma.transactions.fat;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.exceptions.*;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactionalobjects.GammaLongRef;
import org.multiverse.stms.gamma.transactionalobjects.GammaRefTranlocal;
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
        GammaLongRef ref = new GammaLongRef(stm);

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
        GammaLongRef ref = new GammaLongRef(tx, initialVersion);
        GammaRefTranlocal tranlocal = tx.locate(ref);

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
        GammaLongRef ref = new GammaLongRef(tx, initialVersion);
        GammaRefTranlocal tranlocal = ref.openForConstruction(tx);

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
        GammaLongRef ref = new GammaLongRef(otherStm);

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
            new GammaLongRef(tx, initialValue);
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
            new GammaLongRef(tx);
            fail();
        } catch (PreparedTxnException expected) {
        }

        assertIsAborted(tx);
    }

    @Test
    public void whenTransactionAlreadyAborted() {
        GammaTxn tx = newTransaction();
        tx.abort();

        GammaLongRef ref = new GammaLongRef(stm);

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

        GammaLongRef ref = new GammaLongRef(stm);

        try {
            ref.openForConstruction(tx);
            fail();
        } catch (DeadTxnException expected) {
        }

        assertIsCommitted(tx);
    }
}
