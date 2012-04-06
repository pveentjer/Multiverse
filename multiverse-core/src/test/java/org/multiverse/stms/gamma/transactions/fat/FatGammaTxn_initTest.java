package org.multiverse.stms.gamma.transactions.fat;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactions.GammaTxn;
import org.multiverse.stms.gamma.transactions.GammaTxnConfig;

import static org.junit.Assert.assertSame;

public abstract class FatGammaTxn_initTest<T extends GammaTxn> {

    protected GammaStm stm;

    @Before
    public void setUp() {
        stm = new GammaStm();
    }

    protected abstract T newTransaction();

    @Test(expected = NullPointerException.class)
    public void whenNullConfig_thenNullPointerException() {
        T tx = newTransaction();

        tx.init(null);
    }

    @Test
    public void whenSuccess() {
        T tx = newTransaction();

        GammaTxnConfig config = new GammaTxnConfig(stm);
        config.init();
        tx.init(config);

        assertSame(config, tx.getConfiguration());
    }
}
