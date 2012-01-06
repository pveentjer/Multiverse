package org.multiverse.stms.gamma;

import org.junit.Before;
import org.junit.Test;

public class GammaObjectPool_listenerArrayTest {
    private GammaObjectPool pool;

    @Before
    public void setUp() {
        pool = new GammaObjectPool();
    }

    @Test(expected = NullPointerException.class)
    public void whenNullPutInPool_thenNullPointerException() {
        pool.putListenersArray(null);
    }
}
