package org.multiverse.stms.gamma.integration.isolation.levels;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.multiverse.api.IsolationLevel;
import org.multiverse.api.Transaction;
import org.multiverse.api.closures.AtomicVoidClosure;
import org.multiverse.api.exceptions.ReadWriteConflict;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactionalobjects.GammaLongRef;
import org.multiverse.stms.gamma.transactions.GammaTransaction;
import org.multiverse.stms.gamma.transactions.GammaTransactionFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.multiverse.api.GlobalStmInstance.getGlobalStmInstance;
import static org.multiverse.api.ThreadLocalTransaction.clearThreadLocalTransaction;
import static org.multiverse.stms.gamma.GammaTestUtils.makeReadBiased;

public class IsolationLevelSerializableTest {

    private GammaStm stm;
    private GammaTransactionFactory transactionFactory;

    @Before
    public void setUp() {
        stm = (GammaStm) getGlobalStmInstance();
        clearThreadLocalTransaction();
        transactionFactory = stm.newTransactionFactoryBuilder()
                .setSpeculative(false)
                .setIsolationLevel(IsolationLevel.Serializable)
                .newTransactionFactory();
    }

    @Test
    public void repeatableRead_whenTracked_thenNoInconsistentRead() {
        final GammaLongRef ref = new GammaLongRef(stm);

        transactionFactory = stm.newTransactionFactoryBuilder()
                .setSpeculative(false)
                .setReadTrackingEnabled(true)
                .setIsolationLevel(IsolationLevel.Serializable)
                .newTransactionFactory();

        GammaTransaction tx = transactionFactory.newTransaction();
        ref.get(tx);

        ref.atomicIncrementAndGet(1);

        long read2 = ref.get(tx);
        assertEquals(0, read2);
    }

    @Test
    @Ignore
    public void repeatableRead_whenNotTrackedAndConflictingUpdate_thenReadConflict() {
        final GammaLongRef ref = makeReadBiased(new GammaLongRef(stm));

        transactionFactory = stm.newTransactionFactoryBuilder()
                .setSpeculative(false)
                .setReadTrackingEnabled(false)
                .setBlockingAllowed(false)
                .setIsolationLevel(IsolationLevel.Serializable)
                .newTransactionFactory();

        GammaTransaction tx = transactionFactory.newTransaction();
        ref.get(tx);

        ref.atomicIncrementAndGet(1);

        try {
            ref.get(tx);
            fail();
        } catch (ReadWriteConflict expected) {

        }
    }

    @Test
    public void causalConsistency_whenConflictingWrite_thenReadWriteConflict() {
        final GammaLongRef ref1 = new GammaLongRef(stm);
        final GammaLongRef ref2 = new GammaLongRef(stm);

        GammaTransaction tx = transactionFactory.newTransaction();

        ref1.get(tx);

        stm.getDefaultAtomicBlock().execute(new AtomicVoidClosure() {
            @Override
            public void execute(Transaction tx) throws Exception {
                ref1.incrementAndGet(1);
                ref2.incrementAndGet(1);
            }
        });

        try {
            ref2.get(tx);
            fail();
        } catch (ReadWriteConflict expected) {

        }
    }

    @Test
    public void writeSkewNotPossible() {
        final GammaLongRef ref1 = new GammaLongRef(stm);
        final GammaLongRef ref2 = new GammaLongRef(stm);

        GammaTransaction tx = transactionFactory.newTransaction();
        ref1.get(tx);

        ref2.incrementAndGet(tx, 1);

        ref1.atomicIncrementAndGet(1);

        try {
            tx.commit();
            fail();
        } catch (ReadWriteConflict expected) {

        }

        assertEquals(1, ref1.atomicGet());
        assertEquals(0, ref2.atomicGet());
    }
}
