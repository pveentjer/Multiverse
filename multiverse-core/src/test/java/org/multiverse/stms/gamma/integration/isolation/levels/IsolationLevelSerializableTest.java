package org.multiverse.stms.gamma.integration.isolation.levels;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.multiverse.api.IsolationLevel;
import org.multiverse.api.Txn;
import org.multiverse.api.closures.TxnVoidClosure;
import org.multiverse.api.exceptions.ReadWriteConflict;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactionalobjects.GammaTxnLong;
import org.multiverse.stms.gamma.transactions.GammaTxn;
import org.multiverse.stms.gamma.transactions.GammaTxnFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.multiverse.api.GlobalStmInstance.getGlobalStmInstance;
import static org.multiverse.api.TxnThreadLocal.clearThreadLocalTxn;
import static org.multiverse.stms.gamma.GammaTestUtils.makeReadBiased;

public class IsolationLevelSerializableTest {

    private GammaStm stm;
    private GammaTxnFactory transactionFactory;

    @Before
    public void setUp() {
        stm = (GammaStm) getGlobalStmInstance();
        clearThreadLocalTxn();
        transactionFactory = stm.newTxnFactoryBuilder()
                .setSpeculative(false)
                .setIsolationLevel(IsolationLevel.Serializable)
                .newTransactionFactory();
    }

    @Test
    public void repeatableRead_whenTracked_thenNoInconsistentRead() {
        final GammaTxnLong ref = new GammaTxnLong(stm);

        transactionFactory = stm.newTxnFactoryBuilder()
                .setSpeculative(false)
                .setReadTrackingEnabled(true)
                .setIsolationLevel(IsolationLevel.Serializable)
                .newTransactionFactory();

        GammaTxn tx = transactionFactory.newTxn();
        ref.get(tx);

        ref.atomicIncrementAndGet(1);

        long read2 = ref.get(tx);
        assertEquals(0, read2);
    }

    @Test
    @Ignore
    public void repeatableRead_whenNotTrackedAndConflictingUpdate_thenReadConflict() {
        final GammaTxnLong ref = makeReadBiased(new GammaTxnLong(stm));

        transactionFactory = stm.newTxnFactoryBuilder()
                .setSpeculative(false)
                .setReadTrackingEnabled(false)
                .setBlockingAllowed(false)
                .setIsolationLevel(IsolationLevel.Serializable)
                .newTransactionFactory();

        GammaTxn tx = transactionFactory.newTxn();
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
        final GammaTxnLong ref1 = new GammaTxnLong(stm);
        final GammaTxnLong ref2 = new GammaTxnLong(stm);

        GammaTxn tx = transactionFactory.newTxn();

        ref1.get(tx);

        stm.getDefaultTxnExecutor().atomic(new TxnVoidClosure() {
            @Override
            public void execute(Txn tx) throws Exception {
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
        final GammaTxnLong ref1 = new GammaTxnLong(stm);
        final GammaTxnLong ref2 = new GammaTxnLong(stm);

        GammaTxn tx = transactionFactory.newTxn();
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
