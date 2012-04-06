package org.multiverse.stms.gamma.transactionalobjects.gammalongref;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.multiverse.SomeUncheckedException;
import org.multiverse.TestThread;
import org.multiverse.api.Txn;
import org.multiverse.api.closures.AtomicVoidClosure;
import org.multiverse.api.exceptions.DeadTransactionException;
import org.multiverse.api.exceptions.PreparedTransactionException;
import org.multiverse.api.exceptions.RetryError;
import org.multiverse.api.predicates.LongPredicate;
import org.multiverse.api.references.LongRef;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactionalobjects.GammaLongRef;
import org.multiverse.stms.gamma.transactions.GammaTxn;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;
import static org.multiverse.TestUtils.*;
import static org.multiverse.api.TxnThreadLocal.clearThreadLocalTxn;
import static org.multiverse.api.predicates.LongPredicate.newEqualsPredicate;
import static org.multiverse.api.predicates.LongPredicate.newLargerThanOrEqualsPredicate;
import static org.multiverse.stms.gamma.GammaTestUtils.assertVersionAndValue;

public class GammaLongRef_await2WithPredicateTest {

    private GammaStm stm;

    @Before
    public void setUp() {
        stm = new GammaStm();
        clearThreadLocalTxn();
    }

    @Test
    public void whenPredicateEvaluatesToFalse() {
        long initialValue = 10;
        GammaLongRef ref = new GammaLongRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTxn tx = stm.newTransactionFactoryBuilder()
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
        long initialValue = 10;
        GammaLongRef ref = new GammaLongRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTxn tx = stm.newDefaultTransaction();

        ref.await(tx, newEqualsPredicate(initialValue));

        assertIsActive(tx);
        assertVersionAndValue(ref, initialVersion, initialValue);
    }

    @Test
    public void whenPredicateThrowsException_thenTransactionAborted() {
        long initialValue = 10;
        GammaLongRef ref = new GammaLongRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        LongPredicate predicate = mock(LongPredicate.class);

        when(predicate.evaluate(initialValue)).thenThrow(new SomeUncheckedException());

        GammaTxn tx = stm.newDefaultTransaction();

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
        long initialValue = 10;
        GammaLongRef ref = new GammaLongRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTxn tx = stm.newDefaultTransaction();

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
        long initialValue = 10;
        GammaLongRef ref = new GammaLongRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        LongPredicate predicate = mock(LongPredicate.class);
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
        long initialValue = 10;
        GammaLongRef ref = new GammaLongRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTxn tx = stm.newDefaultTransaction();
        tx.prepare();

        LongPredicate predicate = mock(LongPredicate.class);
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
        long initialValue = 10;
        GammaLongRef ref = new GammaLongRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTxn tx = stm.newDefaultTransaction();
        tx.abort();

        LongPredicate predicate = mock(LongPredicate.class);
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
        long initialValue = 10;
        GammaLongRef ref = new GammaLongRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTxn tx = stm.newDefaultTransaction();
        tx.commit();

        LongPredicate predicate = mock(LongPredicate.class);
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
        GammaLongRef ref = new GammaLongRef(stm, initialValue);
        long initialVersion = ref.getVersion();

        AwaitThread thread1 = new AwaitThread(0, 10, ref);
        AwaitThread thread2 = new AwaitThread(1, 20, ref);
        thread1.start();
        thread2.start();

        sleepMs(1000);
        assertAlive(thread1, thread2);

        ref.atomicSet(10);

        assertEventuallyNotAlive(thread1);
        assertNothingThrown(thread1);
        assertAlive(thread2);

        ref.atomicSet(20);

        assertEventuallyNotAlive(thread2);
        assertNothingThrown(thread2);

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
            ref.getStm().getDefaultTxnExecutor().atomic(new AtomicVoidClosure() {
                @Override
                public void execute(Txn tx) throws Exception {
                    ref.await(tx, newLargerThanOrEqualsPredicate(minimumValue));
                }
            });
        }
    }
}
