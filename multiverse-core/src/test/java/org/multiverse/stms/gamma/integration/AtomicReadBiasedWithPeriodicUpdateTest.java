package org.multiverse.stms.gamma.integration;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactionalobjects.GammaTxnLong;

import static org.junit.Assert.assertEquals;
import static org.multiverse.api.GlobalStmInstance.getGlobalStmInstance;
import static org.multiverse.api.TxnThreadLocal.clearThreadLocalTxn;
import static org.multiverse.stms.gamma.GammaTestUtils.*;

public class AtomicReadBiasedWithPeriodicUpdateTest {

    private GammaStm stm;

    @Before
    public void setUp() {
        clearThreadLocalTxn();
        stm = (GammaStm) getGlobalStmInstance();
    }

    @Test
    public void test() {
        GammaTxnLong ref = new GammaTxnLong(stm);

        for (int l = 0; l < 100; l++) {
            long value = ref.atomicGet() + 1;
            ref.atomicGetAndSet(value);

            for (int k = 0; k < 1000; k++) {
                long result = ref.atomicGet();
                assertEquals(value, result);
            }
        }

        assertSurplus(ref, 0);
        assertWriteBiased(ref);
        assertRefHasNoLocks(ref);

        System.out.println("orec: " + ref.___toOrecString());
    }
}
