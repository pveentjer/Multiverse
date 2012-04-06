package org.multiverse.stms.gamma.integration.isolation;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactionalobjects.GammaTxnRef;
import org.multiverse.stms.gamma.transactions.GammaTxn;

import static org.junit.Assert.assertEquals;
import static org.multiverse.api.GlobalStmInstance.getGlobalStmInstance;
import static org.multiverse.api.TxnThreadLocal.clearThreadLocalTxn;
import static org.multiverse.stms.gamma.GammaTestUtils.assertVersionAndValue;

public class DirtyCheckTest {
    private GammaStm stm;

    @Before
    public void setUp() {
        stm = (GammaStm) getGlobalStmInstance();
        clearThreadLocalTxn();
    }

    @Test
    public void whenNoDirtyCheckAndNonDirtyWrite() {
        String initialValue = "foo";
        GammaTxnRef<String> ref = new GammaTxnRef<String>(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTxn tx = stm.newTxnFactoryBuilder()
                .setDirtyCheckEnabled(false)
                .newTransactionFactory()
                .newTransaction();

        ref.set(tx, initialValue);
        tx.commit();

        assertEquals(initialValue, ref.atomicGet());
        assertVersionAndValue(ref, initialVersion + 1, initialValue);
    }

    @Test
    public void whenNoDirtyCheckAndDirtyWrite() {
        String initialValue = "foo";
        GammaTxnRef<String> ref = new GammaTxnRef<String>(stm, initialValue);
        long initialVersion = ref.getVersion();


        GammaTxn tx = stm.newTxnFactoryBuilder()
                .setDirtyCheckEnabled(false)
                .newTransactionFactory()
                .newTransaction();

        String newValue = "bar";
        ref.set(tx, newValue);
        tx.commit();

        assertEquals(newValue, ref.atomicGet());
        assertVersionAndValue(ref, initialVersion + 1, newValue);
    }

    @Test
    public void whenDirtyCheckAndNonDirtyWrite() {
        String initialValue = "foo";
        GammaTxnRef<String> ref = new GammaTxnRef<String>(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTxn tx = stm.newTxnFactoryBuilder()
                .setDirtyCheckEnabled(true)
                .newTransactionFactory()
                .newTransaction();

        ref.set(tx, initialValue);
        tx.commit();

        assertEquals(initialValue, ref.atomicGet());
        assertVersionAndValue(ref, initialVersion, initialValue);
    }

    @Test
    public void whenDirtyCheckAndDirtyWrite() {
        String initialValue = "foo";
        GammaTxnRef<String> ref = new GammaTxnRef<String>(stm, initialValue);
        long initialVersion = ref.getVersion();

        GammaTxn tx = stm.newTxnFactoryBuilder()
                .setDirtyCheckEnabled(true)
                .newTransactionFactory()
                .newTransaction();

        String newValue = "bar";
        ref.set(tx, newValue);
        tx.commit();

        assertEquals(newValue, ref.atomicGet());
        assertVersionAndValue(ref, initialVersion + 1, newValue);
    }
}
