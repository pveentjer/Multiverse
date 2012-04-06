package org.multiverse.stms.gamma;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.TxnExecutor;
import org.multiverse.api.PropagationLevel;
import org.multiverse.api.Transaction;
import org.multiverse.api.closures.AtomicIntClosure;
import org.multiverse.api.closures.AtomicVoidClosure;
import org.multiverse.api.exceptions.TransactionMandatoryException;
import org.multiverse.api.exceptions.TransactionNotAllowedException;
import org.multiverse.stms.gamma.transactionalobjects.GammaLongRef;
import org.multiverse.stms.gamma.transactions.GammaTransaction;
import org.multiverse.stms.gamma.transactions.GammaTxnFactory;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.multiverse.TestUtils.assertIsActive;
import static org.multiverse.api.ThreadLocalTransaction.*;

public class FatGammaTxnExecutor_propagationLevelTest implements GammaConstants {
    private GammaStm stm;

    @Before
    public void setUp() {
        stm = new GammaStm();
        clearThreadLocalTransaction();
    }

    @Test
    public void whenNeverAndTransactionAvailable_thenNoTransactionAllowedException() {
        TxnExecutor block = stm.newTransactionFactoryBuilder()
                .setPropagationLevel(PropagationLevel.Never)
                .newTxnExecutor();

        GammaTransaction otherTx = stm.newDefaultTransaction();
        setThreadLocalTransaction(otherTx);

        AtomicVoidClosure closure = mock(AtomicVoidClosure.class);

        try {
            block.atomic(closure);
            fail();
        } catch (TransactionNotAllowedException expected) {
        }

        verifyZeroInteractions(closure);
        assertIsActive(otherTx);
        assertSame(otherTx, getThreadLocalTransaction());
    }

    @Test
    public void whenNeverAndNoTransactionAvailable() {
        TxnExecutor block = stm.newTransactionFactoryBuilder()
                .setPropagationLevel(PropagationLevel.Never)
                .newTxnExecutor();

        AtomicIntClosure closure = new AtomicIntClosure() {
            @Override
            public int execute(Transaction tx) throws Exception {
                assertNull(tx);
                return 10;
            }
        };

        int result = block.atomic(closure);

        assertEquals(10, result);
        assertNull(getThreadLocalTransaction());
    }

    @Test
    public void whenMandatoryAndNoTransactionAvailable_thenNoTransactionFoundException() {
        TxnExecutor block = stm.newTransactionFactoryBuilder()
                .setPropagationLevel(PropagationLevel.Mandatory)
                .newTxnExecutor();

        AtomicVoidClosure closure = mock(AtomicVoidClosure.class);

        try {
            block.atomic(closure);
            fail();
        } catch (TransactionMandatoryException expected) {
        }

        verifyZeroInteractions(closure);
        assertNull(getThreadLocalTransaction());
    }

    @Test
    public void whenMandatoryAndTransactionAvailable_thenExistingTransactionUsed() {
        TxnExecutor block = stm.newTransactionFactoryBuilder()
                .setPropagationLevel(PropagationLevel.Mandatory)
                .newTxnExecutor();

        final GammaTransaction otherTx = stm.newDefaultTransaction();
        setThreadLocalTransaction(otherTx);

        AtomicIntClosure closure = new AtomicIntClosure() {
            @Override
            public int execute(Transaction tx) throws Exception {
                assertSame(otherTx, tx);
                return 10;
            }
        };

        int result = block.atomic(closure);

        assertEquals(10, result);
        assertIsActive(otherTx);
        assertSame(otherTx, getThreadLocalTransaction());
    }

    @Test
    public void whenRequiresAndNoTransactionAvailable_thenNewTransactionUsed() {
        GammaTxnFactory txFactory = stm.newTransactionFactoryBuilder()
                .setPropagationLevel(PropagationLevel.Requires)
                .newTransactionFactory();

        final GammaLongRef ref = new GammaLongRef(stm);

        AtomicIntClosure closure = new AtomicIntClosure() {
            @Override
            public int execute(Transaction tx) throws Exception {
                assertNotNull(tx);
                GammaTransaction btx = (GammaTransaction) tx;
                ref.incrementAndGet(1);
                return 10;
            }
        };

        int result = new FatGammaTxnExecutor(txFactory).atomic(closure);

        assertEquals(10, result);
        assertNull(getThreadLocalTransaction());
        assertEquals(1, ref.atomicGet());
    }

    @Test
    public void whenRequiresAndTransactionAvailable_thenExistingTransactionUsed() {
        GammaTxnFactory txFactory = stm.newTransactionFactoryBuilder()
                .setPropagationLevel(PropagationLevel.Requires)
                .newTransactionFactory();

        final GammaTransaction existingTx = stm.newDefaultTransaction();
        setThreadLocalTransaction(existingTx);

        final GammaLongRef ref = new GammaLongRef(stm);

        AtomicIntClosure closure = new AtomicIntClosure() {
            @Override
            public int execute(Transaction tx) throws Exception {
                assertSame(existingTx, tx);
                GammaTransaction btx = (GammaTransaction) tx;
                ref.incrementAndGet(btx, 1);
                return 10;
            }
        };

        int result = new FatGammaTxnExecutor(txFactory).atomic(closure);

        assertEquals(10, result);
        assertSame(existingTx, getThreadLocalTransaction());
        assertIsActive(existingTx);
        //since the value hasn't committed yet, it still is zero (the value before the transaction began).
        assertEquals(0, ref.atomicGet());
    }

    @Test
    public void whenRequiresNewAndNoTransactionAvailable_thenNewTransactionCreated() {
        TxnExecutor block = stm.newTransactionFactoryBuilder()
                .setPropagationLevel(PropagationLevel.RequiresNew)
                .newTxnExecutor();

        final GammaLongRef ref = new GammaLongRef(stm, 0);

        AtomicIntClosure closure = new AtomicIntClosure() {
            @Override
            public int execute(Transaction tx) throws Exception {
                assertNotNull(tx);
                GammaTransaction btx = (GammaTransaction) tx;
                ref.incrementAndGet(btx, 1);
                return 10;
            }
        };

        int result = block.atomic(closure);

        assertEquals(10, result);
        assertEquals(1, ref.atomicGet());
        assertNull(getThreadLocalTransaction());
    }

    @Test
    public void whenRequiresNewAndTransactionAvailable_thenExistingTransactionSuspended() {
        TxnExecutor block = stm.newTransactionFactoryBuilder()
                .setPropagationLevel(PropagationLevel.RequiresNew)
                .newTxnExecutor();

        final GammaTransaction otherTx = stm.newDefaultTransaction();
        setThreadLocalTransaction(otherTx);

        final GammaLongRef ref = new GammaLongRef(stm, 10);

        AtomicIntClosure closure = new AtomicIntClosure() {
            @Override
            public int execute(Transaction tx) throws Exception {
                assertNotNull(tx);
                assertNotSame(otherTx, tx);
                GammaTransaction btx = (GammaTransaction) tx;
                ref.incrementAndGet(btx, 1);
                return 1;
            }
        };

        int result = block.atomic(closure);

        assertEquals(1, result);
        assertEquals(11, ref.atomicGet());
        assertSame(otherTx, getThreadLocalTransaction());
        assertIsActive(otherTx);
    }

    @Test
    public void whenSupportsAndTransactionAvailable() {
        TxnExecutor block = stm.newTransactionFactoryBuilder()
                .setPropagationLevel(PropagationLevel.Supports)
                .newTxnExecutor();

        final GammaTransaction otherTx = stm.newDefaultTransaction();
        setThreadLocalTransaction(otherTx);

        AtomicIntClosure closure = new AtomicIntClosure() {
            @Override
            public int execute(Transaction tx) throws Exception {
                assertSame(otherTx, tx);
                return 10;
            }
        };

        int result = block.atomic(closure);

        assertEquals(10, result);
        assertIsActive(otherTx);
        assertSame(otherTx, getThreadLocalTransaction());
    }

    @Test
    public void whenSupportsAndNoTransactionAvailable() {
        TxnExecutor block = stm.newTransactionFactoryBuilder()
                .setPropagationLevel(PropagationLevel.Supports)
                .newTxnExecutor();

        AtomicIntClosure closure = new AtomicIntClosure() {
            @Override
            public int execute(Transaction tx) throws Exception {
                assertNull(tx);
                return 10;
            }
        };

        int result = block.atomic(closure);

        assertEquals(10, result);
        assertNull(getThreadLocalTransaction());
    }
}
