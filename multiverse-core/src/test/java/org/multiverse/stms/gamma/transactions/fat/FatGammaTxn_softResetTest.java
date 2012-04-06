package org.multiverse.stms.gamma.transactions.fat;

import org.junit.Before;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactions.GammaTxn;

public abstract class FatGammaTxn_softResetTest<T extends GammaTxn> {

    protected GammaStm stm;

    @Before
    public void setUp() {
        stm = new GammaStm();
    }

    public abstract T newTransaction();
}
