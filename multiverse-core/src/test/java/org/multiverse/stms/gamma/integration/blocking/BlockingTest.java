package org.multiverse.stms.gamma.integration.blocking;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactionalobjects.GammaTxnLong;
import org.multiverse.stms.gamma.transactionalobjects.txnlong.TxnLongAwaitThread;

import static org.multiverse.TestUtils.assertAlive;
import static org.multiverse.TestUtils.sleepMs;
import static org.multiverse.api.GlobalStmInstance.getGlobalStmInstance;
import static org.multiverse.api.TxnThreadLocal.clearThreadLocalTxn;

public class BlockingTest {

    private GammaStm stm;

    @Before
    public void setUp() {
        stm = (GammaStm) getGlobalStmInstance();
        clearThreadLocalTxn();
    }

    @Test
    public void whenDesiredValueNotAvailable_thenThreadBlocks() {
        GammaTxnLong ref = new GammaTxnLong(stm, 0);

        TxnLongAwaitThread t = new TxnLongAwaitThread(ref, 1);
        t.start();

        sleepMs(1000);
        assertAlive(t);
    }

    @Test
    public void whenDesiredValueStillNotAvailable_thenThreadBlocks() {
        GammaTxnLong ref = new GammaTxnLong(stm, 0);

        TxnLongAwaitThread t = new TxnLongAwaitThread(ref, 2);
        t.start();

        sleepMs(2000);
        ref.atomicSet(1);

        sleepMs(1000);
        assertAlive(t);
    }
}
