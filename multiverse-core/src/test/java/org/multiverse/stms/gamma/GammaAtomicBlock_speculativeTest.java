package org.multiverse.stms.gamma;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.AtomicBlock;
import org.multiverse.api.LockMode;
import org.multiverse.api.Transaction;
import org.multiverse.api.closures.AtomicVoidClosure;
import org.multiverse.api.functions.Function;
import org.multiverse.api.lifecycle.TransactionListener;
import org.multiverse.stms.gamma.transactionalobjects.GammaLongRef;
import org.multiverse.stms.gamma.transactionalobjects.GammaRef;
import org.multiverse.stms.gamma.transactions.GammaTransaction;
import org.multiverse.stms.gamma.transactions.fat.FatMonoGammaTransaction;
import org.multiverse.stms.gamma.transactions.fat.FatVariableLengthGammaTransaction;
import org.multiverse.stms.gamma.transactions.lean.LeanFixedLengthGammaTransaction;
import org.multiverse.stms.gamma.transactions.lean.LeanMonoGammaTransaction;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.multiverse.TestUtils.assertInstanceof;
import static org.multiverse.api.ThreadLocalTransaction.clearThreadLocalTransaction;
import static org.multiverse.api.ThreadLocalTransaction.getThreadLocalTransaction;

public class GammaAtomicBlock_speculativeTest implements GammaConstants {

    private GammaStm stm;

    @Before
    public void setUp() {
        stm = new GammaStm();
        clearThreadLocalTransaction();
    }

    @Test
    public void whenTransactionGrowing() {
        final GammaRef<Long>[] refs = new GammaRef[1000];
        for (int k = 0; k < refs.length; k++) {
            refs[k] = new GammaRef<Long>(stm, 0L);
        }

        final List<GammaTransaction> transactions = new LinkedList<GammaTransaction>();
        final AtomicInteger attempt = new AtomicInteger(1);

        AtomicBlock block = stm.newTransactionFactoryBuilder()
                .setSpeculative(true)
                .setControlFlowErrorsReused(false)
                .setDirtyCheckEnabled(false)
                .newAtomicBlock();

        block.atomic(new AtomicVoidClosure() {
            @Override
            public void execute(Transaction tx) throws Exception {
                System.out.println(tx.getClass());
                assertSame(tx, getThreadLocalTransaction());
                GammaTransaction btx = (GammaTransaction) tx;
                assertEquals(attempt.get(), tx.getAttempt());
                attempt.incrementAndGet();

                transactions.add(btx);

                for (GammaRef<Long> ref : refs) {
                    ref.set(1L);
                }
            }
        });

        for (GammaRef ref : refs) {
            assertEquals(1L, ref.atomicGet());
        }

        assertEquals(4, transactions.size());
        assertInstanceof(LeanMonoGammaTransaction.class, transactions.get(0));
        assertInstanceof(LeanFixedLengthGammaTransaction.class, transactions.get(1));
        assertInstanceof(FatVariableLengthGammaTransaction.class, transactions.get(2));
        assertInstanceof(FatVariableLengthGammaTransaction.class, transactions.get(3));
    }

    @Test
    public void whenCommute() {
        final List<GammaTransaction> transactions = new LinkedList<GammaTransaction>();
        final GammaRef<String> ref = new GammaRef<String>(stm);
        final Function<String> function = mock(Function.class);

        AtomicBlock block = stm.newTransactionFactoryBuilder()
                .setDirtyCheckEnabled(false)
                .setSpeculative(true)
                .newAtomicBlock();

        block.atomic(new AtomicVoidClosure() {
            @Override
            public void execute(Transaction tx) throws Exception {
                GammaTransaction btx = (GammaTransaction) tx;
                transactions.add(btx);
                ref.commute(btx, function);
            }
        });

        assertEquals(2, transactions.size());
        assertTrue(transactions.get(0) instanceof LeanMonoGammaTransaction);
        assertTrue(transactions.get(1) instanceof FatMonoGammaTransaction);
    }

    @Test
    public void whenEnsure() {
        final List<GammaTransaction> transactions = new LinkedList<GammaTransaction>();
        final GammaRef<String> ref = new GammaRef<String>(stm);

        AtomicBlock block = stm.newTransactionFactoryBuilder()
                .setDirtyCheckEnabled(false)
                .setSpeculative(true)
                .newAtomicBlock();

        block.atomic(new AtomicVoidClosure() {
            @Override
            public void execute(Transaction tx) throws Exception {
                GammaTransaction btx = (GammaTransaction) tx;
                transactions.add(btx);
                ref.get(btx);
                ref.ensure(btx);
            }
        });

        assertEquals(2, transactions.size());
        assertTrue(transactions.get(0) instanceof LeanMonoGammaTransaction);
        assertTrue(transactions.get(1) instanceof FatMonoGammaTransaction);
    }


    @Test
    public void whenConstructing() {
        final List<GammaTransaction> transactions = new LinkedList<GammaTransaction>();

        AtomicBlock block = stm.newTransactionFactoryBuilder()
                .setSpeculative(true)
                .setDirtyCheckEnabled(false)
                .newAtomicBlock();

        block.atomic(new AtomicVoidClosure() {
            @Override
            public void execute(Transaction tx) throws Exception {
                GammaTransaction btx = (GammaTransaction) tx;
                transactions.add(btx);

                new GammaRef(btx);
            }
        });

        assertEquals(2, transactions.size());
        assertTrue(transactions.get(0) instanceof LeanMonoGammaTransaction);
        assertTrue(transactions.get(1) instanceof FatMonoGammaTransaction);
    }

    @Test
    public void whenNonRef() {
        final List<GammaTransaction> transactions = new LinkedList<GammaTransaction>();
        final GammaLongRef ref = new GammaLongRef(stm);

        AtomicBlock block = stm.newTransactionFactoryBuilder()
                .setSpeculative(true)
                .setDirtyCheckEnabled(false)
                .newAtomicBlock();

        block.atomic(new AtomicVoidClosure() {
            @Override
            public void execute(Transaction tx) throws Exception {
                GammaTransaction btx = (GammaTransaction) tx;
                transactions.add(btx);
                ref.get(tx);
            }
        });

        assertEquals(2, transactions.size());
        assertTrue(transactions.get(0) instanceof LeanMonoGammaTransaction);
        assertTrue(transactions.get(1) instanceof FatMonoGammaTransaction);
    }

    @Test
    public void whenExplicitLocking() {
        final List<GammaTransaction> transactions = new LinkedList<GammaTransaction>();
        final GammaRef ref = new GammaRef(stm);

        AtomicBlock block = stm.newTransactionFactoryBuilder()
                .setSpeculative(true)
                .setDirtyCheckEnabled(false)
                .newAtomicBlock();

        block.atomic(new AtomicVoidClosure() {
            @Override
            public void execute(Transaction tx) throws Exception {
                GammaTransaction btx = (GammaTransaction) tx;
                transactions.add(btx);
                ref.getLock().acquire(LockMode.Read);
            }
        });

        assertEquals(2, transactions.size());
        assertTrue(transactions.get(0) instanceof LeanMonoGammaTransaction);
        assertTrue(transactions.get(1) instanceof FatMonoGammaTransaction);
    }

    @Test
    public void whenNormalListenerAdded() {
        final List<GammaTransaction> transactions = new LinkedList<GammaTransaction>();
        final AtomicBoolean added = new AtomicBoolean();
        final TransactionListener listener = mock(TransactionListener.class);

        AtomicBlock block = stm.newTransactionFactoryBuilder()
                .setSpeculative(true)
                .setDirtyCheckEnabled(false)
                .newAtomicBlock();

        block.atomic(new AtomicVoidClosure() {
            @Override
            public void execute(Transaction tx) throws Exception {
                assertSame(tx, getThreadLocalTransaction());
                GammaTransaction btx = (GammaTransaction) tx;
                transactions.add(btx);

                if (!added.get()) {
                    btx.register(listener);
                }

            }
        });

        assertEquals(2, transactions.size());
        assertTrue(transactions.get(0) instanceof LeanMonoGammaTransaction);
        assertTrue(transactions.get(1) instanceof FatMonoGammaTransaction);
    }

    @Test
    public void whenTimeoutAvailable_thenCopied() {
        final GammaLongRef ref1 = new GammaLongRef(stm);
        final GammaLongRef ref2 = new GammaLongRef(stm);

        final List<GammaTransaction> transactions = new LinkedList<GammaTransaction>();

        AtomicBlock block = stm.newTransactionFactoryBuilder()
                .setTimeoutNs(1000)
                .setSpeculative(true)
                .setDirtyCheckEnabled(false)
                .newAtomicBlock();

        block.atomic(new AtomicVoidClosure() {
            @Override
            public void execute(Transaction tx) throws Exception {
                assertSame(tx, getThreadLocalTransaction());
                GammaTransaction btx = (GammaTransaction) tx;
                transactions.add(btx);

                if (transactions.size() == 1) {
                    btx.remainingTimeoutNs = 500;
                } else {
                    assertEquals(500, btx.getRemainingTimeoutNs());
                }

                ref1.openForWrite(btx, LOCKMODE_NONE);
                ref2.openForWrite(btx, LOCKMODE_NONE);
            }
        });
    }
}
