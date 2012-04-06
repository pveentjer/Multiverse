package org.multiverse.stms.gamma;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.stms.gamma.transactionalobjects.GammaTxnLong;
import org.multiverse.stms.gamma.transactionalobjects.Tranlocal;
import org.multiverse.stms.gamma.transactions.GammaTxn;

import static org.junit.Assert.*;

public class GammaObjectPool_tranlocalArrayTest implements GammaConstants {
    private GammaObjectPool pool;
    private GammaStm stm;

    @Before
    public void setUp() {
        stm = new GammaStm();
        pool = new GammaObjectPool();
    }

    @Test
    public void whenItemPutInPool_thenPreparedForPooling() {
        Tranlocal[] array = new Tranlocal[2];
        GammaTxn tx = stm.newDefaultTxn();

        array[0] = new GammaTxnLong(stm).openForRead(tx, LOCKMODE_NONE);
        array[1] = new GammaTxnLong(stm).openForRead(tx, LOCKMODE_NONE);

        pool.putTranlocalArray(array);

        assertNull(array[0]);
        assertNull(array[1]);
    }

    @Test(expected = NullPointerException.class)
    public void whenNullArrayAdded_thenNullPointerException() {
        pool.putTranlocalArray(null);
    }

    @Test
    public void normalScenario_0() {
        normalScenario(0);
    }

    @Test
    public void normalScenario_1() {
        normalScenario(1);
    }

    @Test
    public void normalScenario_10() {
        normalScenario(10);
    }

    @Test
    public void normalScenario_100() {
        normalScenario(100);
    }

    public void normalScenario(int size) {
        Tranlocal[] array = new Tranlocal[size];
        pool.putTranlocalArray(array);

        Tranlocal[] result = pool.takeTranlocalArray(array.length);
        assertSame(array, result);

        Tranlocal[] result2 = pool.takeTranlocalArray(array.length);
        assertNotNull(result2);
        assertNotSame(result, result2);
    }
}
