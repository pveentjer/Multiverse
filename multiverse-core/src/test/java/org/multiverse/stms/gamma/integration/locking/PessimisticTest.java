package org.multiverse.stms.gamma.integration.locking;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactionalobjects.GammaLongRef;
import org.multiverse.stms.gamma.transactions.GammaTxn;

import static org.multiverse.api.GlobalStmInstance.getGlobalStmInstance;
import static org.multiverse.api.TxnThreadLocal.clearThreadLocalTxn;
import static org.multiverse.stms.gamma.GammaTestUtils.assertRefHasExclusiveLock;

public class PessimisticTest {
    private GammaStm stm;

    @Before
    public void setUp() {
        stm = (GammaStm) getGlobalStmInstance();
        clearThreadLocalTxn();
    }

    @Test
    public void constructedObjectAutomaticallyIsLocked() {
        GammaTxn tx = stm.newDefaultTransaction();
        GammaLongRef ref = new GammaLongRef(tx);
        ref.openForConstruction(tx);

        assertRefHasExclusiveLock(ref, tx);
    }
}
