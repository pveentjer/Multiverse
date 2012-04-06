package org.multiverse.stms.gamma.integration.isolation.levels;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.multiverse.api.IsolationLevel;
import org.multiverse.api.Txn;
import org.multiverse.api.closures.TxnVoidClosure;
import org.multiverse.api.exceptions.ReadWriteConflict;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactionalobjects.GammaLongRef;
import org.multiverse.stms.gamma.transactions.GammaTxn;
import org.multiverse.stms.gamma.transactions.GammaTxnFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.multiverse.api.GlobalStmInstance.getGlobalStmInstance;
import static org.multiverse.api.TxnThreadLocal.clearThreadLocalTxn;

public class IsolationLevelSnapshotTest {
    private GammaStm stm;
    private GammaTxnFactory transactionFactory;

    @Before
    public void setUp() {
        stm = (GammaStm) getGlobalStmInstance();
        clearThreadLocalTxn();
        transactionFactory = stm.newTxnFactoryBuilder()
                .setSpeculative(false)
                .setIsolationLevel(IsolationLevel.Snapshot)
                .newTransactionFactory();
    }

    @Test
    @Ignore
    public void unrepeatableRead() {

    }

    @Test
    public void causalConsistencyViolationNotPossible() {
        final GammaLongRef ref1 = new GammaLongRef(stm);
        final GammaLongRef ref2 = new GammaLongRef(stm);

        GammaTxn tx = transactionFactory.newTransaction();

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
    public void writeSkewPossible() {
        final GammaLongRef ref1 = new GammaLongRef(stm);
        final GammaLongRef ref2 = new GammaLongRef(stm);

        GammaTxn tx = transactionFactory.newTransaction();
        ref1.get(tx);

        ref2.incrementAndGet(tx, 1);

        ref1.atomicIncrementAndGet(1);

        tx.commit();

        assertEquals(1, ref1.atomicGet());
        assertEquals(1, ref2.atomicGet());
    }
}
