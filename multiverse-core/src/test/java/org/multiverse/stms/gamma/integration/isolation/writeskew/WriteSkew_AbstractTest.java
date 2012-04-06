package org.multiverse.stms.gamma.integration.isolation.writeskew;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.IsolationLevel;
import org.multiverse.api.LockMode;
import org.multiverse.api.exceptions.ReadWriteConflict;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactionalobjects.GammaLongRef;
import org.multiverse.stms.gamma.transactions.GammaTxn;
import org.multiverse.stms.gamma.transactions.GammaTxnConfiguration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.multiverse.api.GlobalStmInstance.getGlobalStmInstance;
import static org.multiverse.api.TxnThreadLocal.clearThreadLocalTxn;

public abstract class WriteSkew_AbstractTest<T extends GammaTxn> {
    protected GammaStm stm;

    @Before
    public void setUp() {
        clearThreadLocalTxn();
        stm = (GammaStm) getGlobalStmInstance();
    }

    public abstract T newTransaction(GammaTxnConfiguration config);

    @After
    public void tearDown() {
        clearThreadLocalTxn();
    }


    @Test
    public void whenWriteSkewAllowed_thenNotDetected() {
        GammaLongRef ref1 = new GammaLongRef(stm);
        GammaLongRef ref2 = new GammaLongRef(stm);

        GammaTxnConfiguration config = new GammaTxnConfiguration(stm)
                .setSpeculative(false)
                .setIsolationLevel(IsolationLevel.Snapshot);

        GammaTxn tx = newTransaction(config);

        ref1.incrementAndGet(tx, 1);
        ref2.get(tx);

        ref2.atomicIncrementAndGet(1);

        tx.commit();
        assertEquals(1, ref1.atomicGet());
    }

    @Test
    public void whenWritesLocked_thenWriteSkewNotDetected() {
        whenWritesLocked_thenWriteSkewNotDetected(LockMode.None);
        whenWritesLocked_thenWriteSkewNotDetected(LockMode.Read);
        whenWritesLocked_thenWriteSkewNotDetected(LockMode.Write);
        whenWritesLocked_thenWriteSkewNotDetected(LockMode.Exclusive);
    }

    public void whenWritesLocked_thenWriteSkewNotDetected(LockMode writeLockMode) {
        GammaLongRef ref1 = new GammaLongRef(stm);
        GammaLongRef ref2 = new GammaLongRef(stm);

        GammaTxnConfiguration config = new GammaTxnConfiguration(stm)
                .setSpeculative(false)
                .setIsolationLevel(IsolationLevel.Snapshot)
                .setWriteLockMode(writeLockMode);

        GammaTxn tx = newTransaction(config);

        ref1.incrementAndGet(tx, 1);
        ref2.get(tx);

        ref2.atomicIncrementAndGet(1);

        tx.commit();
        assertEquals(1, ref1.atomicGet());
    }

    @Test
    public void whenReadsLocked_thenWriteSkewNotPossible() {
        whenReadsLocked_thenWriteSkewNotPossible(LockMode.Read);
        whenReadsLocked_thenWriteSkewNotPossible(LockMode.Write);
        whenReadsLocked_thenWriteSkewNotPossible(LockMode.Exclusive);
    }

    public void whenReadsLocked_thenWriteSkewNotPossible(LockMode readLockMode) {
        GammaLongRef ref1 = new GammaLongRef(stm);
        GammaLongRef ref2 = new GammaLongRef(stm);

        GammaTxnConfiguration config = new GammaTxnConfiguration(stm)
                .setSpeculative(false)
                .setIsolationLevel(IsolationLevel.Snapshot)
                .setControlFlowErrorsReused(false);

        GammaTxn tx = newTransaction(config);

        ref1.incrementAndGet(tx, 1);
        ref2.get(tx);

        GammaTxn otherTx = stm.newDefaultTransaction();
        ref2.incrementAndGet(otherTx, 1);

        ref2.getLock().acquire(tx, readLockMode);

        try {
            otherTx.commit();
            fail();
        } catch (ReadWriteConflict ignored) {
        }

        tx.commit();
    }

    @Test
    public void whenLocked_thenWriteSkewNotPossible() {
        whenLocked_thenWriteSkewNotPossible(LockMode.Read);
        whenLocked_thenWriteSkewNotPossible(LockMode.Write);
        whenLocked_thenWriteSkewNotPossible(LockMode.Exclusive);
    }

    public void whenLocked_thenWriteSkewNotPossible(LockMode lockMode) {
        GammaLongRef ref1 = new GammaLongRef(stm);
        GammaLongRef ref2 = new GammaLongRef(stm);

        GammaTxnConfiguration config = new GammaTxnConfiguration(stm)
                .setSpeculative(false)
                .setIsolationLevel(IsolationLevel.Snapshot);

        GammaTxn tx = newTransaction(config);

        ref1.incrementAndGet(tx, 1);

        GammaTxn otherTx = stm.newDefaultTransaction();
        ref2.incrementAndGet(otherTx, 1);

        ref2.getLock().acquire(tx, lockMode);
        ref2.get(tx);

        try {
            otherTx.commit();
            fail();
        } catch (ReadWriteConflict expected) {
        }

        tx.commit();
    }

    @Test
    public void whenEnsured_thenWriteSkewNotPossible() {
        GammaLongRef ref1 = new GammaLongRef(stm);
        GammaLongRef ref2 = new GammaLongRef(stm);

        GammaTxnConfiguration config = new GammaTxnConfiguration(stm)
                .setSpeculative(false)
                .setIsolationLevel(IsolationLevel.Snapshot);

        GammaTxn tx = newTransaction(config);

        ref1.incrementAndGet(tx, 1);
        ref2.ensure(tx);

        ref2.atomicIncrementAndGet(1);

        try {
            tx.commit();
            fail();
        } catch (ReadWriteConflict expected) {
        }
    }

    @Test
    public void whenSerializedIsolationLevel_thenWriteSkewNotPossible() {
        GammaLongRef ref1 = new GammaLongRef(stm);
        GammaLongRef ref2 = new GammaLongRef(stm);

        GammaTxnConfiguration config = new GammaTxnConfiguration(stm)
                .setSpeculative(false)
                .setIsolationLevel(IsolationLevel.Serializable);

        GammaTxn tx = newTransaction(config);

        ref1.incrementAndGet(tx, 1);
        ref2.get(tx);

        ref2.atomicIncrementAndGet(1);

        try {
            tx.commit();
            fail();
        } catch (ReadWriteConflict expected) {
        }
    }
}
