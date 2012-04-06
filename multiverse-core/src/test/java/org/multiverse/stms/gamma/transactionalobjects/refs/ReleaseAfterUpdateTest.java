package org.multiverse.stms.gamma.transactionalobjects.refs;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.stms.gamma.GammaConstants;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactionalobjects.GammaTxnLong;
import org.multiverse.stms.gamma.transactionalobjects.GammaTxnRef;
import org.multiverse.stms.gamma.transactionalobjects.Tranlocal;
import org.multiverse.stms.gamma.transactions.GammaTxn;

import static org.junit.Assert.*;
import static org.multiverse.stms.gamma.GammaTestUtils.assertRefHasNoLocks;
import static org.multiverse.stms.gamma.GammaTestUtils.makeReadBiased;
import static org.multiverse.stms.gamma.GammaTestUtils.newArrivingTransaction;

public class ReleaseAfterUpdateTest implements GammaConstants {

    private GammaStm stm;

    @Before
    public void setUp() {
        stm = new GammaStm();
    }

    @Test
    public void writeBiased_whenNormalRef() {
        String initialValue = "initialValue";
        GammaTxnRef<String> ref = new GammaTxnRef<String>(stm, initialValue);

        GammaTxn tx = newArrivingTransaction(stm);
        Tranlocal tranlocal = ref.openForWrite(tx, LOCKMODE_EXCLUSIVE);
        tranlocal.isDirty = true;

        ref.releaseAfterUpdate(tranlocal, tx.pool);

        assertNull(tranlocal.owner);
        assertEquals(LOCKMODE_NONE, tranlocal.getLockMode());
        assertNull(tranlocal.ref_value);
        assertNull(tranlocal.ref_oldValue);
        assertFalse(tranlocal.hasDepartObligation());
        assertRefHasNoLocks(ref);
    }

    @Test
    public void writeBiased_whenLongRef() {
        GammaTxnLong ref = new GammaTxnLong(stm, 0);

        GammaTxn tx = newArrivingTransaction(stm);
        Tranlocal tranlocal = ref.openForWrite(tx, LOCKMODE_EXCLUSIVE);
        tranlocal.isDirty = true;

        ref.releaseAfterUpdate(tranlocal, tx.pool);

        assertNull(tranlocal.owner);
        assertEquals(LOCKMODE_NONE, tranlocal.getLockMode());
        assertFalse(tranlocal.hasDepartObligation());
        assertRefHasNoLocks(ref);
    }

    @Test
    public void readBiased_whenNormalRef() {
        String initialValue = "initialValue";
        GammaTxnRef<String> ref = makeReadBiased(new GammaTxnRef<String>(stm, initialValue));

        GammaTxn tx = newArrivingTransaction(stm);
        Tranlocal tranlocal = ref.openForWrite(tx, LOCKMODE_EXCLUSIVE);
        tranlocal.isDirty = true;

        ref.releaseAfterUpdate(tranlocal, tx.pool);

        assertNull(tranlocal.owner);
        assertEquals(LOCKMODE_NONE, tranlocal.getLockMode());
        assertNull(tranlocal.ref_value);
        assertNull(tranlocal.ref_oldValue);
        assertFalse(tranlocal.hasDepartObligation());
        assertRefHasNoLocks(ref);
    }

    @Test
    public void readBiased_whenLongRef() {
        GammaTxnLong ref = makeReadBiased(new GammaTxnLong(stm, 0));

        GammaTxn tx = newArrivingTransaction(stm);
        Tranlocal tranlocal = ref.openForWrite(tx, LOCKMODE_EXCLUSIVE);
        tranlocal.isDirty = true;

        ref.releaseAfterUpdate(tranlocal, tx.pool);

        assertNull(tranlocal.owner);
        assertEquals(LOCKMODE_NONE, tranlocal.getLockMode());
        assertFalse(tranlocal.hasDepartObligation());
        assertRefHasNoLocks(ref);
    }
}
