package org.multiverse.stms.gamma.transactions.fat;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.LockMode;
import org.multiverse.api.TxnStatus;
import org.multiverse.api.exceptions.DeadTransactionException;
import org.multiverse.api.functions.LongFunction;
import org.multiverse.api.lifecycle.TransactionEvent;
import org.multiverse.api.lifecycle.TransactionListener;
import org.multiverse.stms.gamma.GammaConstants;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactionalobjects.GammaLongRef;
import org.multiverse.stms.gamma.transactionalobjects.GammaRefTranlocal;
import org.multiverse.stms.gamma.transactions.GammaTransaction;
import org.multiverse.stms.gamma.transactions.GammaTxnConfiguration;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.multiverse.TestUtils.assertIsAborted;
import static org.multiverse.stms.gamma.GammaTestUtils.*;

public abstract class FatGammaTransaction_abortTest<T extends GammaTransaction> implements GammaConstants {

    protected GammaStm stm;

    @Before
    public void setUp() {
        stm = new GammaStm();
    }

    protected abstract T newTransaction();

    protected abstract T newTransaction(GammaTxnConfiguration config);

    protected abstract void assertCleaned(T tx);

    @Test
    public void listener_whenNormalListenerAvailable() {
        T tx = newTransaction();
        TransactionListener listener = mock(TransactionListener.class);
        tx.register(listener);

        tx.abort();

        assertIsAborted(tx);
        //verify(listener).notify(tx, TransactionEvent.PrePrepare);
        verify(listener).notify(tx, TransactionEvent.PostAbort);
    }

    @Test
    public void listener_whenPermanentListenerAvailable() {
        TransactionListener listener = mock(TransactionListener.class);

        GammaTxnConfiguration config = new GammaTxnConfiguration(stm)
                .addPermanentListener(listener);

        T tx = newTransaction(config);

        tx.abort();

        assertIsAborted(tx);
        //verify(listener).notify(tx, TransactionEvent.PrePrepare);
        verify(listener).notify(tx, TransactionEvent.PostAbort);
    }

    @Test
    public void whenUnused() {
        T tx = newTransaction();

        tx.abort();

        assertEquals(TxnStatus.Aborted, tx.getStatus());
    }

    @Test
    public void locking_whenHasConstructed_thenRemainLocked() {
        GammaTransaction tx = newTransaction();
        GammaLongRef ref = new GammaLongRef(tx);
        GammaRefTranlocal write = tx.getRefTranlocal(ref);
        tx.abort();

        assertIsAborted(tx);

        assertLockMode(ref, LOCKMODE_EXCLUSIVE);
        assertSurplus(ref, 1);
        assertWriteBiased(ref);
        assertVersionAndValue(ref, 0, 0);
        assertFalse(write.hasDepartObligation());
        assertTrue(write.isConstructing());
    }

    @Test
    public void whenContainsCommutes() {
        long initialValue = 10;
        GammaLongRef ref = new GammaLongRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        T tx = newTransaction();
        LongFunction function = mock(LongFunction.class);
        ref.commute(tx, function);
        GammaRefTranlocal tranlocal = tx.getRefTranlocal(ref);
        tx.abort();

        assertIsAborted(tx);
        assertRefHasNoLocks(ref);
        assertVersionAndValue(ref, initialVersion, initialValue);
        assertNull(tranlocal.headCallable);
    }

    @Test
    public void whenHasRead() {
        whenHasRead(LockMode.None);
        whenHasRead(LockMode.Read);
        whenHasRead(LockMode.Write);
        whenHasRead(LockMode.Exclusive);
    }

    public void whenHasRead(LockMode readLockMode) {
        long initialValue = 10;
        GammaLongRef ref = new GammaLongRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        T tx = newTransaction();
        GammaRefTranlocal tranlocal = ref.openForRead(tx, readLockMode.asInt());
        tx.abort();

        assertIsAborted(tx);
        assertEquals(LOCKMODE_NONE, tranlocal.lockMode);
        assertNull(tranlocal.owner);
        assertEquals(initialValue, ref.long_value);
        assertEquals(initialVersion, ref.getVersion());
        assertCleaned(tx);
    }

    @Test
    public void whenHasWrite() {
        whenHasWrite(LockMode.None);
        whenHasWrite(LockMode.Read);
        whenHasWrite(LockMode.Write);
        whenHasWrite(LockMode.Exclusive);
    }

    public void whenHasWrite(LockMode writeLockMode) {
        long initialValue = 10;
        GammaLongRef ref = new GammaLongRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        T tx = newTransaction();
        GammaRefTranlocal tranlocal = ref.openForWrite(tx, writeLockMode.asInt());
        tx.abort();

        assertEquals(TxnStatus.Aborted, tx.getStatus());
        assertEquals(LOCKMODE_NONE, tranlocal.lockMode);
        assertNull(tranlocal.owner);
        assertEquals(initialValue, ref.long_value);
        assertEquals(initialVersion, ref.getVersion());
        assertCleaned(tx);
    }

    @Test
    public void whenAborted() {
        T tx = newTransaction();
        tx.abort();

        tx.abort();

        assertEquals(TxnStatus.Aborted, tx.getStatus());
        assertCleaned(tx);
    }

    @Test
    public void whenCommitted_thenDeadTransactionException() {
        T tx = newTransaction();
        tx.commit();

        try {
            tx.abort();
            fail();
        } catch (DeadTransactionException expected) {

        }

        assertEquals(TxnStatus.Committed, tx.getStatus());
        assertCleaned(tx);
    }
}
