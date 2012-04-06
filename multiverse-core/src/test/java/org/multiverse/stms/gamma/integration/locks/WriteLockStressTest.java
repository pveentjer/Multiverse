package org.multiverse.stms.gamma.integration.locks;

import org.junit.Ignore;
import org.junit.Test;
import org.multiverse.stms.gamma.GammaStm;

import static org.multiverse.api.GlobalStmInstance.getGlobalStmInstance;
import static org.multiverse.api.TxnThreadLocal.clearThreadLocalTxn;

public class WriteLockStressTest {
    private GammaStm stm;

    public void setUp() {
        clearThreadLocalTxn();
        stm = (GammaStm) getGlobalStmInstance();
    }

    @Test
    @Ignore
    public void test() {
    }
}
