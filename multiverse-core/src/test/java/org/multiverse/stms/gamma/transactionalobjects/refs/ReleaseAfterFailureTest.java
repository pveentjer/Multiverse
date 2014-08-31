package org.multiverse.stms.gamma.transactionalobjects.refs;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.LockMode;
import org.multiverse.api.functions.LongFunction;
import org.multiverse.stms.gamma.GammaConstants;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactionalobjects.GammaTxnLong;
import org.multiverse.stms.gamma.transactionalobjects.Tranlocal;
import org.multiverse.stms.gamma.transactions.GammaTxn;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.multiverse.stms.gamma.GammaTestUtils.*;

public class ReleaseAfterFailureTest implements GammaConstants {

    private GammaStm stm;

    @Before
    public void setUp() {
        stm = new GammaStm();
    }

    // ================================ misc ==========================

    @Test
    public void writeBiased_whenCommuting() {
        long initialValue = 10;
        GammaTxnLong ref = new GammaTxnLong(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTxn tx = stm.newDefaultTxn();
        LongFunction function = mock(LongFunction.class);
        ref.commute(tx, function);
        Tranlocal tranlocal = tx.getRefTranlocal(ref);

        ref.releaseAfterFailure(tranlocal, tx.pool);

        assertRefHasNoLocks(ref);
        assertNull(tranlocal.owner);
        assertEquals(LOCKMODE_NONE, tranlocal.getLockMode());
        assertNull(tranlocal.headCallable);
        assertVersionAndValue(ref, initialVersion, initialValue);
    }

    @Test
    public void writeBiased_whenRead() {
        writeBiased_whenRead(LockMode.None);
        writeBiased_whenRead(LockMode.Read);
        writeBiased_whenRead(LockMode.Write);
        writeBiased_whenRead(LockMode.Exclusive);
    }

    public void writeBiased_whenRead(LockMode lockMode) {
        GammaTxnLong ref = new GammaTxnLong(stm);

        GammaTxn tx = stm.newDefaultTxn();
        Tranlocal tranlocal = ref.openForRead(tx, lockMode.asInt());

        ref.releaseAfterFailure(tranlocal, tx.pool);

        assertNull(tranlocal.owner);
        assertFalse(tranlocal.hasDepartObligation);
        assertEquals(LOCKMODE_NONE, tranlocal.lockMode);
        assertSurplus(ref, 0);
        assertLockMode(ref, LockMode.None);
        assertWriteBiased(ref);
        assertReadonlyCount(ref, 0);
    }

    @Test
    public void writeBiased_whenWrite() {
        writeBiased_whenWrite(LockMode.None);
        writeBiased_whenWrite(LockMode.Read);
        writeBiased_whenWrite(LockMode.Write);
        writeBiased_whenWrite(LockMode.Exclusive);
    }

    public void writeBiased_whenWrite(LockMode lockMode) {
        GammaTxnLong ref = new GammaTxnLong(stm);

        GammaTxn tx = stm.newDefaultTxn();
        Tranlocal tranlocal = ref.openForWrite(tx, lockMode.asInt());
        tranlocal.isDirty = true;

        ref.releaseAfterFailure(tranlocal, tx.pool);

        assertNull(tranlocal.owner);
        assertFalse(tranlocal.hasDepartObligation);
        assertEquals(LOCKMODE_NONE, tranlocal.lockMode);
        assertSurplus(ref, 0);
        assertLockMode(ref, LockMode.None);
        assertWriteBiased(ref);
        assertReadonlyCount(ref, 0);
    }

    // ========================= read biased ================================

    @Test
    public void readBiased_whenCommuting() {
        long initialValue = 10;
        GammaTxnLong ref = new GammaTxnLong(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTxn tx = stm.newDefaultTxn();
        LongFunction function = mock(LongFunction.class);
        ref.commute(tx, function);
        Tranlocal tranlocal = tx.getRefTranlocal(ref);

        ref.releaseAfterFailure(tranlocal, tx.pool);

        assertRefHasNoLocks(ref);
        assertNull(tranlocal.owner);
        assertEquals(LOCKMODE_NONE, tranlocal.getLockMode());
        assertNull(tranlocal.headCallable);
        assertVersionAndValue(ref, initialVersion, initialValue);
    }

    @Test
    public void readBiased_whenRead() {
        readBiased_whenRead(true,LockMode.None);
        readBiased_whenRead(true,LockMode.Read);
        readBiased_whenRead(true,LockMode.Write);
        readBiased_whenRead(true,LockMode.Exclusive);

        readBiased_whenRead(false,LockMode.None);
        readBiased_whenRead(false,LockMode.Read);
        readBiased_whenRead(false,LockMode.Write);
        readBiased_whenRead(false,LockMode.Exclusive);
    }

    public void readBiased_whenRead(boolean additionalSurplus, LockMode lockMode) {
        GammaTxnLong ref = makeReadBiased(new GammaTxnLong(stm));

        if(additionalSurplus){
            ref.arrive(1);
        }

        GammaTxn tx = newArrivingTransaction(stm);
        Tranlocal tranlocal = ref.openForRead(tx, lockMode.asInt());

        ref.releaseAfterFailure(tranlocal, tx.pool);

        assertNull(tranlocal.owner);
        assertFalse(tranlocal.hasDepartObligation);
        assertEquals(LOCKMODE_NONE, tranlocal.lockMode);
        assertSurplus(ref, 1);
        assertLockMode(ref, LockMode.None);
        assertReadBiased(ref);
        assertReadonlyCount(ref, 0);
    }

    @Test
    public void readBiased_whenWrite() {
        readBiased_whenWrite(false,LockMode.None);
        readBiased_whenWrite(false,LockMode.Read);
        readBiased_whenWrite(false,LockMode.Write);
        readBiased_whenWrite(false,LockMode.Exclusive);

        readBiased_whenWrite(true,LockMode.None);
        readBiased_whenWrite(true,LockMode.Read);
        readBiased_whenWrite(true,LockMode.Write);
        readBiased_whenWrite(true,LockMode.Exclusive);
    }

    public void readBiased_whenWrite(boolean additionalSurplus, LockMode lockMode) {
        GammaTxnLong ref = makeReadBiased(new GammaTxnLong(stm));

        if(additionalSurplus){
            ref.arrive(1);
        }

        GammaTxn tx = newArrivingTransaction(stm);
        Tranlocal tranlocal = ref.openForWrite(tx, lockMode.asInt());
        tranlocal.isDirty = true;

        ref.releaseAfterFailure(tranlocal, tx.pool);

        assertNull(tranlocal.owner);
        assertFalse(tranlocal.hasDepartObligation);
        assertEquals(LOCKMODE_NONE, tranlocal.lockMode);
        assertSurplus(ref, 1);
        assertLockMode(ref, LockMode.None);
        assertReadBiased(ref);
        assertReadonlyCount(ref, 0);
    }

    // ================================ misc ==========================

    @Test
    public void whenConstructing() {
        GammaTxn tx = stm.newDefaultTxn();
        GammaTxnLong ref = new GammaTxnLong(tx, 0);
        Tranlocal tranlocal = tx.locate(ref);

        ref.releaseAfterFailure(tranlocal, tx.pool);

        assertNull(tranlocal.owner);
        assertFalse(tranlocal.hasDepartObligation);
        assertEquals(LOCKMODE_NONE, tranlocal.lockMode);
        assertSurplus(ref, 1);
        assertLockMode(ref, LockMode.Exclusive);
        assertWriteBiased(ref);
        assertReadonlyCount(ref, 0);
    }
}
