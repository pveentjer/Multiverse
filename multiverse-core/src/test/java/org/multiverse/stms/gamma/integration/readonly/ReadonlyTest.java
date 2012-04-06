package org.multiverse.stms.gamma.integration.readonly;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.Txn;
import org.multiverse.api.TxnExecutor;
import org.multiverse.api.callables.TxnCallable;
import org.multiverse.api.callables.TxnLongCallable;
import org.multiverse.api.callables.TxnVoidCallable;
import org.multiverse.api.exceptions.ReadonlyException;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactionalobjects.GammaTxnLong;
import org.multiverse.stms.gamma.transactions.GammaTxn;

import static org.junit.Assert.*;
import static org.multiverse.api.GlobalStmInstance.getGlobalStmInstance;
import static org.multiverse.api.TxnThreadLocal.clearThreadLocalTxn;

public class ReadonlyTest {

    private GammaStm stm;

    @Before
    public void setUp() {
        clearThreadLocalTxn();
        stm = (GammaStm) getGlobalStmInstance();
    }

    @Test
    public void whenReadonly_thenUpdateFails() {
        GammaTxnLong ref = new GammaTxnLong(stm);
        try {
            updateInReadonlyMethod(ref, 10);
            fail();
        } catch (ReadonlyException expected) {
        }

        assertEquals(0, ref.atomicGet());
    }

    public void updateInReadonlyMethod(final GammaTxnLong ref, final int newValue) {
        TxnExecutor executor = stm.newTxnFactoryBuilder()
                .setReadonly(true)
                .newTxnExecutor();

        executor.atomic(new TxnVoidCallable() {
            @Override
            public void call(Txn tx) throws Exception {
                GammaTxn btx = (GammaTxn) tx;
                ref.getAndSet(btx, newValue);
            }
        });
    }

    @Test
    public void whenReadonly_thenCreationOfNewTxnObjectNotFails() {
        try {
            readonly_createNewTransactionObject(10);
            fail();
        } catch (ReadonlyException expected) {
        }
    }

    public void readonly_createNewTransactionObject(final long value) {
        TxnExecutor executor = stm.newTxnFactoryBuilder()
                .setReadonly(true)
                .newTxnExecutor();

        executor.atomic(new TxnVoidCallable() {
            @Override
            public void call(Txn tx) throws Exception {
                GammaTxn btx = (GammaTxn) tx;
                GammaTxnLong ref = new GammaTxnLong(btx);
                ref.openForConstruction(btx).long_value = value;
            }
        });
    }

    @Test
    public void whenReadonly_thenCreationOfNonTxnObjectSucceeds() {
        Integer ref = readonly_createNormalObject(100);
        assertNotNull(ref);
        assertEquals(100, ref.intValue());
    }

    public Integer readonly_createNormalObject(final int value) {
        TxnExecutor executor = stm.newTxnFactoryBuilder()
                .setReadonly(true)
                .newTxnExecutor();

        return executor.atomic(new TxnCallable<Integer>() {
            @Override
            public Integer call(Txn tx) throws Exception {
                return new Integer(value);
            }
        });
    }

    @Test
    public void whenReadonly_thenReadAllowed() {
        GammaTxnLong ref = new GammaTxnLong(stm, 10);
        long result = readInReadonlyMethod(ref);
        assertEquals(10, result);
        assertEquals(10, ref.atomicGet());
    }

    public long readInReadonlyMethod(final GammaTxnLong ref) {
        TxnExecutor executor = stm.newTxnFactoryBuilder()
                .setReadonly(true)
                .newTxnExecutor();

        return executor.atomic(new TxnLongCallable() {
            @Override
            public long call(Txn tx) throws Exception {
                GammaTxn btx = (GammaTxn) tx;
                return ref.get(btx);
            }
        });
    }

    @Test
    public void whenUpdate_thenCreationOfNewTxnObjectsSucceeds() {
        GammaTxnLong ref = update_createNewTransactionObject(100);
        assertNotNull(ref);
        assertEquals(100, ref.atomicGet());
    }

    public GammaTxnLong update_createNewTransactionObject(final int value) {
        TxnExecutor executor = stm.newTxnFactoryBuilder()
                .setReadonly(false)
                .newTxnExecutor();

        return executor.atomic(new TxnCallable<GammaTxnLong>() {
            @Override
            public GammaTxnLong call(Txn tx) throws Exception {
                GammaTxn btx = (GammaTxn) tx;
                GammaTxnLong ref = new GammaTxnLong(btx);
                ref.openForConstruction(btx).long_value = value;
                return ref;
            }
        });
    }

    @Test
    public void whenUpdate_thenCreationOfNonTxnObjectSucceeds() {
        Integer ref = update_createNormalObject(100);
        assertNotNull(ref);
        assertEquals(100, ref.intValue());
    }

    public Integer update_createNormalObject(final int value) {
        TxnExecutor executor = stm.newTxnFactoryBuilder()
                .setReadonly(false)
                .newTxnExecutor();

        return executor.atomic(new TxnCallable<Integer>() {
            @Override
            public Integer call(Txn tx) throws Exception {
                return new Integer(value);
            }
        });
    }

    @Test
    public void whenUpdate_thenReadSucceeds() {
        GammaTxnLong ref = new GammaTxnLong(stm, 10);
        long result = readInUpdateMethod(ref);
        assertEquals(10, result);
        assertEquals(10, ref.atomicGet());
    }


    public long readInUpdateMethod(final GammaTxnLong ref) {
        TxnExecutor executor = stm.newTxnFactoryBuilder()
                .setReadonly(false)
                .newTxnExecutor();

        return executor.atomic(new TxnLongCallable() {
            @Override
            public long call(Txn tx) throws Exception {
                GammaTxn btx = (GammaTxn) tx;
                return ref.get(btx);
            }
        });
    }

    @Test
    public void whenUpdate_thenUpdateSucceeds() {
        GammaTxnLong ref = new GammaTxnLong(stm);
        updateInUpdateMethod(ref, 10);
        assertEquals(10, ref.atomicGet());
    }

    public void updateInUpdateMethod(final GammaTxnLong ref, final int newValue) {
        TxnExecutor executor = stm.newTxnFactoryBuilder()
                .setReadonly(false)
                .newTxnExecutor();

        executor.atomic(new TxnVoidCallable() {
            @Override
            public void call(Txn tx) throws Exception {
                assertFalse(tx.getConfig().isReadonly());
                GammaTxn btx = (GammaTxn) tx;
                ref.getAndSet(btx, newValue);
            }
        });
    }

    @Test
    public void whenDefault_thenUpdateSuccess() {
        GammaTxnLong ref = new GammaTxnLong(stm);
        defaultTransactionalMethod(ref);

        assertEquals(1, ref.atomicGet());
    }


    public void defaultTransactionalMethod(final GammaTxnLong ref) {
        stm.newTxnFactoryBuilder().newTxnExecutor().atomic(new TxnVoidCallable() {
            @Override
            public void call(Txn tx) throws Exception {
                assertFalse(tx.getConfig().isReadonly());
                GammaTxn btx = (GammaTxn) tx;
                ref.getAndSet(btx, ref.get(btx) + 1);
            }
        });
    }
}
