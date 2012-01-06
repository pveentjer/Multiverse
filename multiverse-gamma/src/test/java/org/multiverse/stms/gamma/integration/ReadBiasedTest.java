package org.multiverse.stms.gamma.integration;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.stms.gamma.GammaConstants;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactionalobjects.GammaLongRef;
import org.multiverse.stms.gamma.transactions.GammaTransaction;

import static org.multiverse.api.GlobalStmInstance.getGlobalStmInstance;
import static org.multiverse.api.ThreadLocalTransaction.clearThreadLocalTransaction;
import static org.multiverse.stms.gamma.GammaTestUtils.*;

public class ReadBiasedTest implements GammaConstants {
    private GammaStm stm;

    @Before
    public void setUp() {
        clearThreadLocalTransaction();
        stm = (GammaStm) getGlobalStmInstance();
    }

    @Test
    public void test() {
        GammaLongRef ref = new GammaLongRef(stm, 100);
        long version = ref.getVersion();

        for (int k = 0; k < 10000; k++) {
            GammaTransaction tx = stm.newDefaultTransaction();
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
