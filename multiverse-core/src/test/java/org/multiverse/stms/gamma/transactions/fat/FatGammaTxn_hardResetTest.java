package org.multiverse.stms.gamma.transactions.fat;

import org.junit.Before;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactions.GammaTxn;

public abstract class FatGammaTxn_hardResetTest<T extends GammaTxn> {

    protected GammaStm stm;

    @Before
    public void setUp() {
        stm = new GammaStm();
    }

    protected abstract T newTransaction();
}
