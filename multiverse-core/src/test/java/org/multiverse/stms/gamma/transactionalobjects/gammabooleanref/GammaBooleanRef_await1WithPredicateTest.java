package org.multiverse.stms.gamma.transactionalobjects.gammabooleanref;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.SomeUncheckedException;
import org.multiverse.api.exceptions.DeadTransactionException;
import org.multiverse.api.exceptions.PreparedTransactionException;
import org.multiverse.api.exceptions.RetryError;
import org.multiverse.api.exceptions.TransactionMandatoryException;
import org.multiverse.api.predicates.BooleanPredicate;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactionalobjects.GammaBooleanRef;
import org.multiverse.stms.gamma.transactions.GammaTransaction;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;
import static org.multiverse.TestUtils.*;
import static org.multiverse.api.ThreadLocalTransaction.clearThreadLocalTransaction;
import static org.multiverse.api.ThreadLocalTransaction.setThreadLocalTransaction;
import static org.multiverse.api.predicates.BooleanPredicate.newEqualsPredicate;
import static org.multiverse.stms.gamma.GammaTestUtils.assertRefHasNoLocks;
import static org.multiverse.stms.gamma.GammaTestUtils.assertVersionAndValue;

public class GammaBooleanRef_await1WithPredicateTest {

    private GammaStm stm;

    @Before
    public void setUp() {
        stm = new GammaStm();
        clearThreadLocalTransaction();
    }

    @Test
    public void whenPredicateEvaluatesToFalse() {
        boolean initialValue = true;
        GammaBooleanRef ref = new GammaBooleanRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTransaction tx = stm.newTransactionFactoryBuilder()
                .setFat()
                .newTransactionFactory()
                .newTransaction();
        setThreadLocalTransaction(tx);

        try {
            ref.await(newEqualsPredicate(!initialValue));
            fail();
        } catch (RetryError expected) {
        }

        assertIsAborted(tx);
        assertRefHasNoLocks(ref);
        assertVersionAndValue(ref, initialVersion, initialValue);
    }

    @Test
    public void whenPredicateReturnsTrue() {
        boolean initialValue = true;
        GammaBooleanRef ref = new GammaBooleanRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTransaction tx = stm.newTransactionFactoryBuilder()
                .setFat()
                .newTransactionFactory()
                .newTransaction();

        setThreadLocalTransaction(tx);

        ref.await(newEqualsPredicate(initialValue));

        assertIsActive(tx);
        assertRefHasNoLocks(ref);
        assertVersionAndValue(ref, initialVersion, initialValue);
    }

    @Test
    public void whenPredicateThrowsException_thenTransactionAborted() {
        boolean initialValue = true;
        GammaBooleanRef ref = new GammaBooleanRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        BooleanPredicate predicate = mock(BooleanPredicate.class);

        when(predicate.evaluate(initialValue)).thenThrow(new SomeUncheckedException());

        GammaTransaction tx = stm.newDefaultTransaction();
        setThreadLocalTransaction(tx);

        try {
            ref.await(predicate);
            fail();
        } catch (SomeUncheckedException expected) {
        }

        assertIsAborted(tx);
        assertRefHasNoLocks(ref);
        assertVersionAndValue(ref, initialVersion, initialValue);
    }

    @Test
    public void whenNullPredicate_thenTransactionAbortedAndNullPointerException() {
        boolean initialValue = true;
        GammaBooleanRef ref = new GammaBooleanRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTransaction tx = stm.newDefaultTransaction();
        setThreadLocalTransaction(tx);

        try {
            ref.await(null);
            fail();
        } catch (NullPointerException expected) {
        }

        assertIsAborted(tx);
        assertRefHasNoLocks(ref);
        assertVersionAndValue(ref, initialVersion, initialValue);
    }

    @Test
    public void whenNoTransaction_thenTransactionMandatoryException() {
        boolean initialValue = true;
        GammaBooleanRef ref = new GammaBooleanRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        BooleanPredicate predicate = mock(BooleanPredicate.class);
        try {
            ref.await(predicate);
            fail();
        } catch (TransactionMandatoryException expected) {

        }

        verifyZeroInteractions(predicate);
        assertRefHasNoLocks(ref);
        assertVersionAndValue(ref, initialVersion, initialValue);
    }

    @Test
    public void whenTransactionPrepared_thenPreparedTransactionException() {
        boolean initialValue = true;
        GammaBooleanRef ref = new GammaBooleanRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTransaction tx = stm.newDefaultTransaction();
        setThreadLocalTransaction(tx);
        tx.prepare();

        BooleanPredicate predicate = mock(BooleanPredicate.class);
        try {
            ref.await(predicate);
            fail();
        } catch (PreparedTransactionException expected) {
        }

        assertIsAborted(tx);
        verifyZeroInteractions(predicate);
        assertRefHasNoLocks(ref);
        assertVersionAndValue(ref, initialVersion, initialValue);
    }

    @Test
    public void whenTransactionAborted_thenDeadTransactionException() {
        boolean initialValue = true;
        GammaBooleanRef ref = new GammaBooleanRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTransaction tx = stm.newDefaultTransaction();
        setThreadLocalTransaction(tx);
        tx.abort();

        BooleanPredicate predicate = mock(BooleanPredicate.class);
        try {
            ref.await(predicate);
            fail();
        } catch (DeadTransactionException expected) {
        }

        assertIsAborted(tx);
        assertRefHasNoLocks(ref);
        verifyZeroInteractions(predicate);
        assertVersionAndValue(ref, initialVersion, initialValue);
    }

    @Test
    public void whenTransactionCommitted_thenDeadTransactionException() {
        boolean initialValue = true;
        GammaBooleanRef ref = new GammaBooleanRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTransaction tx = stm.newDefaultTransaction();
        setThreadLocalTransaction(tx);
        tx.commit();

        BooleanPredicate predicate = mock(BooleanPredicate.class);
        try {
            ref.await(predicate);
            fail();
        } catch (DeadTransactionException expected) {
        }

        assertIsCommitted(tx);
        assertRefHasNoLocks(ref);
        verifyZeroInteractions(predicate);
        assertVersionAndValue(ref, initialVersion, initialValue);
    }

    /*
    @Test
    public void whenSomeWaitingNeeded() {
        int initialValue = 0;
        GammaBooleanRef ref = new GammaBooleanRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        BooleanRefAwaitThread thread1 = new BooleanRefAwaitThread(ref, 10);
        BooleanRefAwaitThread thread2 = new BooleanRefAwaitThread(ref, 20);
        thread1.start();
        thread2.start();

        sleepMs(1000);
        assertAlive(thread1, thread2);

        ref.atomicSet(10);

        sleepMs(500);

        assertNotAlive(thread1);
        thread1.assertNothingThrown();
        assertAlive(thread2);

        ref.atomicSet(20);

        sleepMs(500);
        assertNotAlive(thread2);
        thread2.assertNothingThrown();

        assertRefHasNoLocks(ref);
        assertVersionAndValue(ref, initialVersion + 2, 20);
    }   */
}
