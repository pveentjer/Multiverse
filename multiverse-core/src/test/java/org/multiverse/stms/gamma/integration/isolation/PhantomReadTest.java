package org.multiverse.stms.gamma.integration.isolation;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.multiverse.stms.gamma.GammaStm;

import static org.multiverse.api.GlobalStmInstance.getGlobalStmInstance;

public class PhantomReadTest {
    private GammaStm stm;

    @Before
    public void setUp() {
        stm = (GammaStm) getGlobalStmInstance();
    }

    @Test
    @Ignore
    public void test() {
    }
}
