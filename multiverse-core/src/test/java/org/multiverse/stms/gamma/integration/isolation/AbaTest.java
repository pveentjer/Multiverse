package org.multiverse.stms.gamma.integration.isolation;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.exceptions.ReadWriteConflict;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactionalobjects.GammaLongRef;
import org.multiverse.stms.gamma.transactions.GammaTxn;

import static org.junit.Assert.fail;
import static org.multiverse.api.GlobalStmInstance.getGlobalStmInstance;
import static org.multiverse.api.TxnThreadLocal.clearThreadLocalTxn;

public class AbaTest {
    private GammaStm stm;

    @Before
    public void setUp() {
        stm = (GammaStm) getGlobalStmInstance();
        clearThreadLocalTxn();
    }

    @Test
    public void test() {
        GammaLongRef ref = new GammaLongRef(stm);

        GammaTxn tx = stm.newDefaultTransaction();
        ref.get(tx);

        ref.atomicIncrementAndGet(1);
        ref.atomicIncrementAndGet(-1);

        ref.incrementAndGet(tx, 1);

        try {
            tx.commit();
            fail();
        } catch (ReadWriteConflict expected) {
        }
    }
}
