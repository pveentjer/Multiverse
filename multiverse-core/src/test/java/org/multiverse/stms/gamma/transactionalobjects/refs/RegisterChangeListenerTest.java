package org.multiverse.stms.gamma.transactionalobjects.refs;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.LockMode;
import org.multiverse.api.blocking.DefaultRetryLatch;
import org.multiverse.api.blocking.RetryLatch;
import org.multiverse.api.functions.LongFunction;
import org.multiverse.stms.gamma.GammaConstants;
import org.multiverse.stms.gamma.GammaObjectPool;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.Listeners;
import org.multiverse.stms.gamma.transactionalobjects.GammaTxnLong;
import org.multiverse.stms.gamma.transactionalobjects.Tranlocal;
import org.multiverse.stms.gamma.transactions.GammaTxn;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.multiverse.TestUtils.getField;
import static org.multiverse.api.TxnThreadLocal.clearThreadLocalTxn;
import static org.multiverse.stms.gamma.GammaTestUtils.*;

public class RegisterChangeListenerTest implements GammaConstants {
    private GammaStm stm;
    private GammaObjectPool pool;

    @Before
    public void setUp() {
        stm = new GammaStm();
        pool = new GammaObjectPool();
        clearThreadLocalTxn();
    }

    @Test
    public void whenCommuting() {
        GammaTxnLong ref = new GammaTxnLong(stm, 0);

        LongFunction function = mock(LongFunction.class);
        GammaTxn tx = stm.newDefaultTxn();
        ref.commute(tx, function);

        RetryLatch latch = new DefaultRetryLatch();
        long listenerEra = latch.getEra();
        Tranlocal tranlocal = tx.getRefTranlocal(ref);
        int result = ref.registerChangeListener(latch, tranlocal, pool, listenerEra);

        assertEquals(REGISTRATION_NONE, result);
        assertNull(getField(ref, "listeners"));
        assertFalse(latch.isOpen());
        verifyZeroInteractions(function);
    }

    @Test
    public void whenInterestingWriteAlreadyHappened_thenLatchOpenedAndNoRegistration() {
        GammaTxnLong ref = new GammaTxnLong(stm);

        GammaTxn tx = stm.newDefaultTxn();
        Tranlocal read = ref.openForRead(tx, LOCKMODE_NONE);

        GammaTxn otherTx = stm.newDefaultTxn();
        Tranlocal write = ref.openForWrite(otherTx, LOCKMODE_NONE);
        write.long_value++;
        otherTx.commit();

        RetryLatch latch = new DefaultRetryLatch();
        long listenerEra = latch.getEra();
        int result = ref.registerChangeListener(latch, read, pool, listenerEra);

        assertEquals(REGISTRATION_NOT_NEEDED, result);
        assertNull(getField(ref, "listeners"));
        assertTrue(latch.isOpen());
    }

    @Test
    public void whenExclusiveLockAndNoConflict_thenRegistered() {
        GammaTxnLong ref = new GammaTxnLong(stm, 10);
        long version = ref.getVersion();

        GammaTxn tx = stm.newDefaultTxn();
        Tranlocal read = ref.openForRead(tx, LOCKMODE_NONE);

        GammaTxn otherTx = stm.newDefaultTxn();
        ref.getLock().acquire(otherTx, LockMode.Exclusive);

        RetryLatch latch = new DefaultRetryLatch();
        long listenerEra = latch.getEra();
        int result = ref.registerChangeListener(latch, read, pool, listenerEra);

        assertEquals(REGISTRATION_DONE, result);
        assertSurplus(ref, 1);
        assertHasListeners(ref, latch);
        assertRefHasExclusiveLock(ref, otherTx);
        assertVersionAndValue(ref, version, 10);
        assertFalse(latch.isOpen());
    }

    @Test
    public void whenWriteLockAndNoConflict_thenRegistered() {
        GammaTxnLong ref = new GammaTxnLong(stm, 10);
        long version = ref.getVersion();

        GammaTxn tx = stm.newDefaultTxn();
        Tranlocal read = ref.openForRead(tx, LOCKMODE_NONE);

        GammaTxn otherTx = stm.newDefaultTxn();
        ref.getLock().acquire(otherTx, LockMode.Write);

        RetryLatch latch = new DefaultRetryLatch();
        long listenerEra = latch.getEra();
        int result = ref.registerChangeListener(latch, read, pool, listenerEra);

        assertEquals(REGISTRATION_DONE, result);
        assertSurplus(ref, 1);
        assertHasListeners(ref, latch);
        assertRefHasWriteLock(ref, otherTx);
        assertVersionAndValue(ref, version, 10);
        assertFalse(latch.isOpen());
    }

    @Test
    public void whenExclusiveLockAndInterestingChangeAlreadyHappened() {
        GammaTxnLong ref = new GammaTxnLong(stm);

        GammaTxn tx = stm.newDefaultTxn();
        Tranlocal read = ref.openForRead(tx, LOCKMODE_NONE);

        ref.atomicIncrementAndGet(1);
        long version = ref.getVersion();

        GammaTxn otherTx = stm.newDefaultTxn();
        ref.getLock().acquire(otherTx, LockMode.Exclusive);

        RetryLatch latch = new DefaultRetryLatch();
        long listenerEra = latch.getEra();
        int result = ref.registerChangeListener(latch, read, pool, listenerEra);

        assertEquals(REGISTRATION_NOT_NEEDED, result);
        assertNull(getField(ref, "listeners"));
        assertTrue(latch.isOpen());
        assertSurplus(ref, 1);
        assertHasNoListeners(ref);
        assertRefHasExclusiveLock(ref, otherTx);
        assertVersionAndValue(ref, version, 1);
    }

    @Test
    public void wheWriteLockAndInterestingChangeAlreadyHappened() {
        GammaTxnLong ref = new GammaTxnLong(stm);

        GammaTxn tx = stm.newDefaultTxn();
        Tranlocal read = ref.openForRead(tx, LOCKMODE_NONE);

        ref.atomicIncrementAndGet(1);
        long version = ref.getVersion();

        GammaTxn otherTx = stm.newDefaultTxn();
        ref.getLock().acquire(otherTx, LockMode.Write);

        RetryLatch latch = new DefaultRetryLatch();
        long listenerEra = latch.getEra();
        int result = ref.registerChangeListener(latch, read, pool, listenerEra);

        assertEquals(REGISTRATION_NOT_NEEDED, result);
        assertNull(getField(ref, "listeners"));
        assertTrue(latch.isOpen());
        assertSurplus(ref, 1);
        assertHasNoListeners(ref);
        assertRefHasWriteLock(ref, otherTx);
        assertVersionAndValue(ref, version, 1);
    }

    @Test
    public void whenConstructed_thenNoRegistration() {
        GammaTxn tx = stm.newDefaultTxn();
        GammaTxnLong ref = new GammaTxnLong(tx);
        Tranlocal read = tx.locate(ref);

        RetryLatch latch = new DefaultRetryLatch();
        long listenerEra = latch.getEra();
        int result = ref.registerChangeListener(latch, read, pool, listenerEra);

        assertEquals(REGISTRATION_NONE, result);
        assertHasNoListeners(ref);
        assertFalse(latch.isOpen());

    }

    @Test
    public void whenFirstOne_thenRegistrationSuccessful() {
        GammaTxnLong ref = new GammaTxnLong(stm);

        GammaTxn tx = stm.newDefaultTxn();
        Tranlocal read = ref.openForRead(tx, LOCKMODE_NONE);

        RetryLatch latch = new DefaultRetryLatch();
        long listenerEra = latch.getEra();
        int result = ref.registerChangeListener(latch, read, pool, listenerEra);

        assertEquals(REGISTRATION_DONE, result);
        Listeners listeners = (Listeners) getField(ref, "listeners");
        assertNotNull(listeners);
        assertSame(latch, listeners.listener);
        assertNull(listeners.next);
        assertEquals(listenerEra, listeners.listenerEra);
        assertFalse(latch.isOpen());
    }

    @Test
    public void whenSecondOne_thenListenerAddedToChain() {
        GammaTxnLong ref = new GammaTxnLong(stm);

        GammaTxn tx1 = stm.newDefaultTxn();
        Tranlocal read1 = ref.openForRead(tx1, LOCKMODE_NONE);

        RetryLatch latch1 = new DefaultRetryLatch();
        long listenerEra1 = latch1.getEra();
        ref.registerChangeListener(latch1, read1, pool, listenerEra1);

        GammaTxn tx2 = stm.newDefaultTxn();
        Tranlocal read2 = ref.openForRead(tx2, LOCKMODE_NONE);

        RetryLatch latch2 = new DefaultRetryLatch();
        long listenerEra2 = latch2.getEra();
        int result = ref.registerChangeListener(latch2, read2, pool, listenerEra2);

        assertEquals(REGISTRATION_DONE, result);
        Listeners listeners = (Listeners) getField(ref, "listeners");
        assertNotNull(listeners);
        assertSame(latch2, listeners.listener);
        assertEquals(listenerEra2, listeners.listenerEra);
        assertNotNull(listeners.next);
        assertSame(latch1, listeners.next.listener);
        assertEquals(listenerEra1, listeners.next.listenerEra);
        assertFalse(latch1.isOpen());
    }
}
