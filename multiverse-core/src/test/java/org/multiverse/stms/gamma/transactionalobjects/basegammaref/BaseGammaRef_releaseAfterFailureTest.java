package org.multiverse.stms.gamma.transactionalobjects.basegammaref;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.LockMode;
import org.multiverse.api.functions.LongFunction;
import org.multiverse.stms.gamma.GammaConstants;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactionalobjects.GammaLongRef;
import org.multiverse.stms.gamma.transactionalobjects.GammaRefTranlocal;
import org.multiverse.stms.gamma.transactions.GammaTransaction;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.multiverse.stms.gamma.GammaTestUtils.*;

public class BaseGammaRef_releaseAfterFailureTest implements GammaConstants {

    private GammaStm stm;

    @Before
    public void setUp() {
        stm = new GammaStm();
    }

    // ================================ misc ==========================

    @Test
    public void writeBiased_whenCommuting() {
        long initialValue = 10;
        GammaLongRef ref = new GammaLongRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTransaction tx = stm.newDefaultTransaction();
        LongFunction function = mock(LongFunction.class);
        ref.commute(tx, function);
        GammaRefTranlocal tranlocal = tx.getRefTranlocal(ref);

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
        GammaLongRef ref = new GammaLongRef(stm);

        GammaTransaction tx = stm.newDefaultTransaction();
        GammaRefTranlocal tranlocal = ref.openForRead(tx, lockMode.asInt());

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
        GammaLongRef ref = new GammaLongRef(stm);

        GammaTransaction tx = stm.newDefaultTransaction();
        GammaRefTranlocal tranlocal = ref.openForWrite(tx, lockMode.asInt());
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
        GammaLongRef ref = new GammaLongRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTransaction tx = stm.newDefaultTransaction();
        LongFunction function = mock(LongFunction.class);
        ref.commute(tx, function);
        GammaRefTranlocal tranlocal = tx.getRefTranlocal(ref);

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
        GammaLongRef ref = makeReadBiased(new GammaLongRef(stm));

        if(additionalSurplus){
            ref.arrive(1);
        }

        GammaTransaction tx = newArrivingTransaction(stm);
        GammaRefTranlocal tranlocal = ref.openForRead(tx, lockMode.asInt());

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
        GammaLongRef ref = makeReadBiased(new GammaLongRef(stm));

        if(additionalSurplus){
            ref.arrive(1);
        }

        GammaTransaction tx = newArrivingTransaction(stm);
        GammaRefTranlocal tranlocal = ref.openForWrite(tx, lockMode.asInt());
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
        GammaTransaction tx = stm.newDefaultTransaction();
        GammaLongRef ref = new GammaLongRef(tx, 0);
        GammaRefTranlocal tranlocal = tx.locate(ref);

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
