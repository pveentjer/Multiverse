package org.multiverse.stms.gamma.transactionalobjects.gammabooleanref;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.SomeUncheckedException;
import org.multiverse.api.exceptions.DeadTransactionException;
import org.multiverse.api.exceptions.PreparedTransactionException;
import org.multiverse.api.exceptions.RetryError;
import org.multiverse.api.predicates.BooleanPredicate;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactionalobjects.GammaBooleanRef;
import org.multiverse.stms.gamma.transactions.GammaTransaction;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;
import static org.multiverse.TestUtils.*;
import static org.multiverse.api.ThreadLocalTransaction.clearThreadLocalTransaction;
import static org.multiverse.api.predicates.BooleanPredicate.newEqualsPredicate;
import static org.multiverse.stms.gamma.GammaTestUtils.assertVersionAndValue;

public class GammaBooleanRef_await2WithPredicateTest {

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

        try {
            ref.await(tx, newEqualsPredicate(!initialValue));
            fail();
        } catch (RetryError expected) {

        }

        assertIsAborted(tx);
        assertVersionAndValue(ref, initialVersion, initialValue);
    }

    @Test
    public void whenPredicateReturnsTrue() {
        boolean initialValue = true;
        GammaBooleanRef ref = new GammaBooleanRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTransaction tx = stm.newDefaultTransaction();

        ref.await(tx, newEqualsPredicate(initialValue));

        assertIsActive(tx);
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
        boolean initialValue = true;
        GammaBooleanRef ref = new GammaBooleanRef(stm, initialValue);
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
        boolean initialValue = true;
        GammaBooleanRef ref = new GammaBooleanRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        BooleanPredicate predicate = mock(BooleanPredicate.class);
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
        boolean initialValue = true;
        GammaBooleanRef ref = new GammaBooleanRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTransaction tx = stm.newDefaultTransaction();
        tx.prepare();

        BooleanPredicate predicate = mock(BooleanPredicate.class);
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
        boolean initialValue = true;
        GammaBooleanRef ref = new GammaBooleanRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTransaction tx = stm.newDefaultTransaction();
        tx.abort();

        BooleanPredicate predicate = mock(BooleanPredicate.class);
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
        boolean initialValue = true;
        GammaBooleanRef ref = new GammaBooleanRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTransaction tx = stm.newDefaultTransaction();
        tx.commit();

        BooleanPredicate predicate = mock(BooleanPredicate.class);
        try {
            ref.await(tx, predicate);
            fail();
        } catch (DeadTransactionException expected) {
        }

        assertIsCommitted(tx);
        verifyZeroInteractions(predicate);
        assertVersionAndValue(ref, initialVersion, initialValue);
    }

    /*
    @Test
    public void whenSomeWaitingNeeded() {
        int initialValue = 0;
        GammaBooleanRef ref = new GammaBooleanRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        AwaitThread thread1 = new AwaitThread(0, 10, ref);
        AwaitThread thread2 = new AwaitThread(1, 20, ref);
        thread1.start();
        thread2.start();

        sleepMs(2000);
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

        assertVersionAndValue(ref, initialVersion + 2, 20);
    }

    public class AwaitThread extends TestThread {
        private long minimumValue;
        private LongRef ref;

        public AwaitThread(int id, long minimumValue, LongRef ref) {
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
    } */
}
