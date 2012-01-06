package org.multiverse.stms.gamma;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.blocking.DefaultRetryLatch;

import static org.junit.Assert.*;

public class GammaObjectPool_listenersTest {
    private GammaObjectPool pool;

    @Before
    public void setUp() {
        pool = new GammaObjectPool();
    }

    @Test(expected = NullPointerException.class)
    public void whenNullPutInPool_thenNullPointerException() {
        pool.putListeners(null);
    }

    @Test
    public void whenPutInPool_thenPreparedForPooling() {
        DefaultRetryLatch latch = new DefaultRetryLatch();
        latch.reset();

        Listeners next = new Listeners();

        Listeners listeners = new Listeners();
        listeners.listener = latch;
        listeners.listenerEra = latch.getEra();
        listeners.next = next;

        pool.putListeners(listeners);

        assertNull(listeners.next);
        assertNull(listeners.listener);
        assertEquals(Long.MIN_VALUE, listeners.listenerEra);
    }

    @Test
    public void test() {
        Listeners listeners1 = new Listeners();
        Listeners listeners2 = new Listeners();
        Listeners listeners3 = new Listeners();

        pool.putListeners(listeners1);
        pool.putListeners(listeners2);
        pool.putListeners(listeners3);

        assertSame(listeners3, pool.takeListeners());
        assertSame(listeners2, pool.takeListeners());
        assertSame(listeners1, pool.takeListeners());
        assertNotNull(pool.takeListeners());
    }
}
