package org.multiverse.stms.gamma.integration;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.stms.gamma.GammaConstants;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactionalobjects.GammaTxnLong;
import org.multiverse.stms.gamma.transactions.GammaTxn;
import org.multiverse.stms.gamma.transactions.fat.FatMonoGammaTxn;

import static org.multiverse.api.GlobalStmInstance.getGlobalStmInstance;
import static org.multiverse.api.TxnThreadLocal.clearThreadLocalTxn;
import static org.multiverse.stms.gamma.GammaTestUtils.*;

public class ReadBiasedWithPeriodicUpdateTest implements GammaConstants {

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
            GammaTxn tx = new FatMonoGammaTxn(stm);
            tx.richmansMansConflictScan = true;
            ref.openForWrite(tx, LOCKMODE_NONE).long_value++;
            tx.commit();

            for (int k = 0; k < 1000; k++) {
                GammaTxn readonlyTx = new FatMonoGammaTxn(stm);
                readonlyTx.richmansMansConflictScan = true;
                ref.openForRead(readonlyTx, LOCKMODE_NONE);
                readonlyTx.commit();
            }
        }

        assertSurplus(ref, 1);
        assertReadBiased(ref);
        assertRefHasNoLocks(ref);

        System.out.println("orec: " + ref.___toOrecString());
    }
}
