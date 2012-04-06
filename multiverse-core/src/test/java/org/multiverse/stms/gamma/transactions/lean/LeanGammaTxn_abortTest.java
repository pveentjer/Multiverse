package org.multiverse.stms.gamma.transactions.lean;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.exceptions.DeadTransactionException;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactionalobjects.GammaRef;
import org.multiverse.stms.gamma.transactionalobjects.GammaRefTranlocal;
import org.multiverse.stms.gamma.transactions.GammaTxn;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.multiverse.TestUtils.assertIsAborted;
import static org.multiverse.TestUtils.assertIsCommitted;
import static org.multiverse.stms.gamma.GammaTestUtils.*;

public abstract class LeanGammaTxn_abortTest<T extends GammaTxn> {

    public GammaStm stm;

    @Before
    public void setUp() {
        stm = new GammaStm();
    }

    public abstract T newTransaction();

    @Test
    public void whenUnused() {
        T tx = newTransaction();
        tx.abort();

        assertIsAborted(tx);
    }

    @Test
    public void whenContainsRead() {
        String initialValue = "foo";
        GammaRef<String> ref = new GammaRef<String>(stm, initialValue);
        long initialVersion = ref.getVersion();

        T tx = newTransaction();
        GammaRefTranlocal tranlocal = ref.openForRead(tx, LOCKMODE_NONE);
        tx.abort();

        assertIsAborted(tx);
        assertVersionAndValue(ref, initialVersion, initialValue);
        assertSurplus(ref, 0);
        assertWriteBiased(ref);
        assertReadonlyCount(ref, 0);
        assertRefHasNoLocks(ref);

        assertNull(tranlocal.ref_value);
        assertNull(tranlocal.ref_oldValue);
        assertNull(tranlocal.owner);
    }

    @Test
    public void whenContainsWrite() {
        String initialValue = "foo";
        GammaRef<String> ref = new GammaRef<String>(stm, initialValue);
        long initialVersion = ref.getVersion();

        T tx = newTransaction();
        GammaRefTranlocal tranlocal = ref.openForWrite(tx, LOCKMODE_NONE);
        tx.abort();

        assertIsAborted(tx);
        assertVersionAndValue(ref, initialVersion, initialValue);
        assertSurplus(ref, 0);
        assertWriteBiased(ref);
        assertReadonlyCount(ref, 0);
        assertRefHasNoLocks(ref);

        assertNull(tranlocal.ref_value);
        assertNull(tranlocal.ref_oldValue);
        assertNull(tranlocal.owner);
    }

    @Test
    public void whenUnusedAndPrepared() {
        T tx = newTransaction();
        tx.prepare();

        tx.abort();
        assertIsAborted(tx);
    }

    @Test
    public void whenAborted() {
        T tx = newTransaction();
        tx.abort();

        tx.abort();
        assertIsAborted(tx);
    }

    @Test
    public void whenCommitted() {
        T tx = newTransaction();
        tx.commit();

        try {
            tx.abort();
            fail();
        } catch (DeadTransactionException expected) {

        }
        assertIsCommitted(tx);
    }


}
