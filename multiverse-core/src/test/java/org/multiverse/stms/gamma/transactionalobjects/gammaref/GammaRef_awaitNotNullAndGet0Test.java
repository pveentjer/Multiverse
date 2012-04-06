package org.multiverse.stms.gamma.transactionalobjects.gammaref;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.LockMode;
import org.multiverse.api.exceptions.*;
import org.multiverse.stms.gamma.GammaConstants;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactionalobjects.GammaRef;
import org.multiverse.stms.gamma.transactionalobjects.GammaRefTranlocal;
import org.multiverse.stms.gamma.transactions.GammaTxn;

import static org.junit.Assert.*;
import static org.multiverse.TestUtils.assertIsAborted;
import static org.multiverse.TestUtils.assertIsCommitted;
import static org.multiverse.api.TxnThreadLocal.clearThreadLocalTxn;
import static org.multiverse.api.TxnThreadLocal.setThreadLocalTxn;
import static org.multiverse.stms.gamma.GammaTestUtils.*;

public class GammaRef_awaitNotNullAndGet0Test implements GammaConstants {

    private GammaStm stm;

    @Before
    public void setUp() {
        stm = new GammaStm();
        clearThreadLocalTxn();
    }

    @Test
    public void whenNull_thenReturnImmediately() {
        String initialValue = "foo";
        GammaRef<String> ref = new GammaRef<String>(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTxn tx = stm.newDefaultTransaction();
        setThreadLocalTxn(tx);
        String result = ref.awaitNotNullAndGet();

        assertSame(initialValue, result);
        GammaRefTranlocal tranlocal = tx.locate(ref);
        assertTrue(tranlocal.isRead());
        assertEquals(LOCKMODE_NONE, tranlocal.getLockMode());
        assertRefHasNoLocks(ref);

        tx.commit();

        assertIsCommitted(tx);
        assertVersionAndValue(ref, initialVersion, initialValue);
        assertRefHasNoLocks(ref);
    }

    @Test
    public void whenPrivatizedByOtherBeforeReading_thenReadWriteConflict() {
        GammaRef<String> ref = new GammaRef<String>(stm);
        long initialVersion = ref.getVersion();

        GammaTxn otherTx = stm.newDefaultTransaction();
        ref.getLock().acquire(otherTx, LockMode.Exclusive);

        GammaTxn tx = stm.newDefaultTransaction();
        setThreadLocalTxn(tx);
        try {
            ref.awaitNotNullAndGet();
            fail();
        } catch (ReadWriteConflict expected) {

        }

        assertIsAborted(tx);
        assertVersionAndValue(ref, initialVersion, null);
        assertRefHasExclusiveLock(ref, otherTx);
    }

    @Test
    public void whenEnsuredByOtherBeforeReading_thenSuccess() {
        String initialValue = "foo";
        GammaRef<String> ref = new GammaRef<String>(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTxn otherTx = stm.newDefaultTransaction();
        ref.getLock().acquire(otherTx, LockMode.Write);

        GammaTxn tx = stm.newDefaultTransaction();
        setThreadLocalTxn(tx);
        String result = ref.awaitNotNullAndGet();

        assertSame(initialValue, result);

        GammaRefTranlocal tranlocal = tx.locate(ref);
        assertTrue(tranlocal.isRead());
        assertEquals(LockMode.LOCKMODE_NONE, tranlocal.getLockMode());
        assertRefHasWriteLock(ref, otherTx);

        tx.commit();

        assertIsCommitted(tx);
        assertVersionAndValue(ref, initialVersion, initialValue);
        assertRefHasWriteLock(ref, otherTx);
    }

    @Test
    public void whenNull_thenWait() {
        GammaRef<String> ref = new GammaRef<String>(stm);
        long initialVersion = ref.getVersion();

        GammaTxn tx = stm.newTransactionFactoryBuilder()
                .newTransactionFactory()
                .newTransaction();

        setThreadLocalTxn(tx);

        try {
            ref.awaitNotNullAndGet();
            fail();
        } catch (RetryError expected) {
        }

        assertIsAborted(tx);
        assertVersionAndValue(ref, initialVersion, null);
        assertRefHasNoLocks(ref);
    }

    @Test
    public void whenNoTransactionAvailable_thenTransactionMandatoryException() {
        GammaRef<String> ref = new GammaRef<String>(stm);
        long initialVersion = ref.getVersion();

        try {
            ref.awaitNotNullAndGet();
            fail();
        } catch (TransactionMandatoryException expected) {
        }

        assertVersionAndValue(ref, initialVersion, null);
    }

    @Test
    public void whenPreparedTransaction_thenPreparedTransactionException() {
        GammaRef<String> ref = new GammaRef<String>(stm);
        long initialVersion = ref.getVersion();

        GammaTxn tx = stm.newDefaultTransaction();
        tx.prepare();
        setThreadLocalTxn(tx);

        try {
            ref.awaitNotNullAndGet();
            fail();
        } catch (PreparedTransactionException expected) {
        }

        assertIsAborted(tx);
        assertVersionAndValue(ref, initialVersion, null);
    }

    @Test
    public void whenAbortedTransaction_thenDeadTransactionException() {
        GammaRef<String> ref = new GammaRef<String>(stm);
        long initialVersion = ref.getVersion();

        GammaTxn tx = stm.newDefaultTransaction();
        tx.abort();
        setThreadLocalTxn(tx);

        try {
            ref.awaitNotNullAndGet();
            fail();
        } catch (DeadTransactionException expected) {
        }

        assertIsAborted(tx);
        assertVersionAndValue(ref, initialVersion, null);
    }

    @Test
    public void whenCommittedTransaction_thenDeadTransactionException() {
        GammaRef<String> ref = new GammaRef<String>(stm);
        long initialVersion = ref.getVersion();

        GammaTxn tx = stm.newDefaultTransaction();
        tx.commit();
        setThreadLocalTxn(tx);

        try {
            ref.awaitNotNullAndGet();
            fail();
        } catch (DeadTransactionException expected) {
        }

        assertIsCommitted(tx);
        assertVersionAndValue(ref, initialVersion, null);
    }
}
