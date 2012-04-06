package org.multiverse.stms.gamma.integration;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.stms.gamma.GammaConstants;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactionalobjects.GammaTxnLong;
import org.multiverse.stms.gamma.transactions.GammaTxn;

import static org.multiverse.api.GlobalStmInstance.getGlobalStmInstance;
import static org.multiverse.api.TxnThreadLocal.clearThreadLocalTxn;
import static org.multiverse.stms.gamma.GammaTestUtils.*;

public class ReadBiasedTest implements GammaConstants {
    private GammaStm stm;

    @Before
    public void setUp() {
        clearThreadLocalTxn();
        stm = (GammaStm) getGlobalStmInstance();
    }

    @Test
    public void test() {
        GammaTxnLong ref = new GammaTxnLong(stm, 100);
        long version = ref.getVersion();

        for (int k = 0; k < 10000; k++) {
            GammaTxn tx = stm.newDefaultTxn();
            tx.richmansMansConflictScan = true;
            ref.openForRead(tx, LOCKMODE_NONE);
            tx.commit();
        }

        assertSurplus(ref, 1);
        assertReadBiased(ref);
        assertRefHasNoLocks(ref);
        assertVersionAndValue(ref, version, 100);

        System.out.println("orec: " + ref.___toOrecString());
    }
}
