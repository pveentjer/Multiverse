package org.multiverse.stms.gamma.transactionalobjects.gammaintref;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.multiverse.SomeUncheckedException;
import org.multiverse.TestThread;
import org.multiverse.api.Transaction;
import org.multiverse.api.closures.AtomicVoidClosure;
import org.multiverse.api.exceptions.DeadTransactionException;
import org.multiverse.api.exceptions.PreparedTransactionException;
import org.multiverse.api.exceptions.RetryError;
import org.multiverse.api.predicates.IntPredicate;
import org.multiverse.api.references.IntRef;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactionalobjects.GammaIntRef;
import org.multiverse.stms.gamma.transactions.GammaTransaction;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;
import static org.multiverse.TestUtils.*;
import static org.multiverse.api.ThreadLocalTransaction.clearThreadLocalTransaction;
import static org.multiverse.api.predicates.IntPredicate.newEqualsPredicate;
import static org.multiverse.api.predicates.IntPredicate.newLargerThanOrEqualsPredicate;
import static org.multiverse.stms.gamma.GammaTestUtils.assertVersionAndValue;

public class GammaIntRef_await2WithPredicateTest {

    private GammaStm stm;

    @Before
    public void setUp() {
        stm = new GammaStm();
        clearThreadLocalTransaction();
    }

    @Test
    public void whenPredicateEvaluatesToFalse() {
        int initialValue = 10;
        GammaIntRef ref = new GammaIntRef(stm, initialValue);
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
        int initialValue = 10;
        GammaIntRef ref = new GammaIntRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTransaction tx = stm.newDefaultTransaction();

        ref.await(tx, newEqualsPredicate(initialValue));

        assertIsActive(tx);
        assertVersionAndValue(ref, initialVersion, initialValue);
    }

    @Test
    public void whenPredicateThrowsException_thenTransactionAborted() {
        int initialValue = 10;
        GammaIntRef ref = new GammaIntRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        IntPredicate predicate = mock(IntPredicate.class);

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
        int initialValue = 10;
        GammaIntRef ref = new GammaIntRef(stm, initialValue);
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
        int initialValue = 10;
        GammaIntRef ref = new GammaIntRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        IntPredicate predicate = mock(IntPredicate.class);
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
        int initialValue = 10;
        GammaIntRef ref = new GammaIntRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTransaction tx = stm.newDefaultTransaction();
        tx.prepare();

        IntPredicate predicate = mock(IntPredicate.class);
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
        int initialValue = 10;
        GammaIntRef ref = new GammaIntRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTransaction tx = stm.newDefaultTransaction();
        tx.abort();

        IntPredicate predicate = mock(IntPredicate.class);
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
        int initialValue = 10;
        GammaIntRef ref = new GammaIntRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTransaction tx = stm.newDefaultTransaction();
        tx.commit();

        IntPredicate predicate = mock(IntPredicate.class);
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
    @Ignore
    public void whenSomeWaitingNeeded() {
        int initialValue = 0;
        GammaIntRef ref = new GammaIntRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        AwaitThread thread1 = new AwaitThread(0, 10, ref);
        AwaitThread thread2 = new AwaitThread(1, 20, ref);
        thread1.start();
        thread2.start();

        sleepMs(1000);
        assertAlive(thread1, thread2);

        ref.atomicSet(10);

        sleepMs(1000);

        assertNotAlive(thread1);
        thread1.assertNothingThrown();
        assertAlive(thread2);

        ref.atomicSet(20);

        sleepMs(500);
        assertNotAlive(thread2);
        thread2.assertNothingThrown();

        assertVersionAndValue(ref, initialVersion + 2, 20);
    }

    public class AwaitThread extends TestThread {
        private int minimumValue;
        private IntRef ref;

        public AwaitThread(int id, int minimumValue, IntRef ref) {
            super("AwaitThread-" + id);
            this.minimumValue = minimumValue;
            this.ref = ref;
        }

        @Override
        public void doRun() throws Exception {
            ref.getStm().getDefaultAtomicBlock().execute(new AtomicVoidClosure() {
                @Override
                public void execute(Transaction tx) throws Exception {
                    ref.await(tx, newLargerThanOrEqualsPredicate(minimumValue));
                }
            });
        }
    }
}
