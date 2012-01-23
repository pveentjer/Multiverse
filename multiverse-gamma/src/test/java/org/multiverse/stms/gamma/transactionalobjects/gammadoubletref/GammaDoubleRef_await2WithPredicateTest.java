package org.multiverse.stms.gamma.transactionalobjects.gammadoubletref;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.multiverse.SomeUncheckedException;
import org.multiverse.api.exceptions.DeadTransactionException;
import org.multiverse.api.exceptions.PreparedTransactionException;
import org.multiverse.api.exceptions.RetryError;
import org.multiverse.api.predicates.DoublePredicate;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactionalobjects.GammaDoubleRef;
import org.multiverse.stms.gamma.transactions.GammaTransaction;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;
import static org.multiverse.TestUtils.*;
import static org.multiverse.api.ThreadLocalTransaction.clearThreadLocalTransaction;
import static org.multiverse.api.predicates.DoublePredicate.newEqualsPredicate;
import static org.multiverse.stms.gamma.GammaTestUtils.assertVersionAndValue;

public class GammaDoubleRef_await2WithPredicateTest {

    private GammaStm stm;

    @Before
    public void setUp() {
        stm = new GammaStm();
        clearThreadLocalTransaction();
    }

    @Test
    public void whenPredicateEvaluatesToFalse() {
        double initialValue = 10;
        GammaDoubleRef ref = new GammaDoubleRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTransaction tx = stm.newTransactionFactoryBuilder()
                .setFat()
                .newTransactionFactory()
                .newTransaction();

        try {
            ref.await(tx, newEqualsPredicate(initialValue + 1));
            fail();
        } catch (RetryError expected) {

        }

        assertIsAborted(tx);
        assertVersionAndValue(ref, initialVersion, initialValue);
    }

    @Test
    public void whenPredicateReturnsTrue() {
        double initialValue = 10;
        GammaDoubleRef ref = new GammaDoubleRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTransaction tx = stm.newDefaultTransaction();

        ref.await(tx, newEqualsPredicate(initialValue));

        assertIsActive(tx);
        assertVersionAndValue(ref, initialVersion, initialValue);
    }

    @Test
    public void whenPredicateThrowsException_thenTransactionAborted() {
        double initialValue = 10;
        GammaDoubleRef ref = new GammaDoubleRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        DoublePredicate predicate = mock(DoublePredicate.class);

        when(predicate.evaluate(initialValue)).thenThrow(new SomeUncheckedException());

        GammaTransaction tx = stm.newDefaultTransaction();

        try {
            ref.await(tx, predicate);
            fail();
        } catch (SomeUncheckedException expected) {
        }

        assertIsAborted(tx);
        assertVersionAndValue(ref, initialVersion, initialValue);
    }

    @Test
    public void whenNullPredicate_thenTransactionAbortedAndNullPointerException() {
        double initialValue = 10;
        GammaDoubleRef ref = new GammaDoubleRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTransaction tx = stm.newDefaultTransaction();

        try {
            ref.await(tx, null);
            fail();
        } catch (NullPointerException expected) {
        }

        assertIsAborted(tx);
        assertVersionAndValue(ref, initialVersion, initialValue);
    }

    @Test
    public void whenNullTransaction_thenNullPointerException() {
        double initialValue = 10;
        GammaDoubleRef ref = new GammaDoubleRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        DoublePredicate predicate = mock(DoublePredicate.class);
        try {
            ref.await(null, predicate);
            fail();
        } catch (NullPointerException expected) {

        }

        verifyZeroInteractions(predicate);
        assertVersionAndValue(ref, initialVersion, initialValue);
    }

    @Test
    public void whenTransactionPrepared_thenPreparedTransactionException() {
        double initialValue = 10;
        GammaDoubleRef ref = new GammaDoubleRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTransaction tx = stm.newDefaultTransaction();
        tx.prepare();

        DoublePredicate predicate = mock(DoublePredicate.class);
        try {
            ref.await(tx, predicate);
            fail();
        } catch (PreparedTransactionException expected) {
        }

        assertIsAborted(tx);
        verifyZeroInteractions(predicate);
        assertVersionAndValue(ref, initialVersion, initialValue);
    }

    @Test
    public void whenTransactionAborted_thenDeadTransactionException() {
        double initialValue = 10;
        GammaDoubleRef ref = new GammaDoubleRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTransaction tx = stm.newDefaultTransaction();
        tx.abort();

        DoublePredicate predicate = mock(DoublePredicate.class);
        try {
            ref.await(tx, predicate);
            fail();
        } catch (DeadTransactionException expected) {
        }

        assertIsAborted(tx);
        verifyZeroInteractions(predicate);
        assertVersionAndValue(ref, initialVersion, initialValue);
    }

    @Test
    public void whenTransactionCommitted_thenDeadTransactionException() {
        double initialValue = 10;
        GammaDoubleRef ref = new GammaDoubleRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTransaction tx = stm.newDefaultTransaction();
        tx.commit();

        DoublePredicate predicate = mock(DoublePredicate.class);
        try {
            ref.await(tx, predicate);
            fail();
        } catch (DeadTransactionException expected) {
        }

        assertIsCommitted(tx);
        verifyZeroInteractions(predicate);
        assertVersionAndValue(ref, initialVersion, initialValue);
    }

    @Test
    public void whenSomeWaitingNeeded() {
        int initialValue = 0;
        GammaDoubleRef ref = new GammaDoubleRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaDoubleRefAwaitThread thread1 = new GammaDoubleRefAwaitThread(ref, 10);
        GammaDoubleRefAwaitThread thread2 = new GammaDoubleRefAwaitThread(ref, 1);
        thread1.start();
        thread2.start();

        sleepMs(1000);
        assertAlive(thread1, thread2);

        ref.atomicSet(10);

        sleepMs(1000);

        assertNotAlive(thread1);
        thread1.assertNothingThrown();
        assertAlive(thread2);

        ref.atomicSet(1);

        sleepMs(500);
        assertNotAlive(thread2);
        thread2.assertNothingThrown();

        assertVersionAndValue(ref, initialVersion + 2, 1);
    }
}
