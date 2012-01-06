package org.multiverse.stms.gamma;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.blocking.RetryLatch;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class Listeners_openAllTest {
    private GammaObjectPool pool;

    @Before
    public void setUp() {
        pool = new GammaObjectPool();
    }

    @Test
    public void test() {
        RetryLatch latch1 = mock(RetryLatch.class);
        RetryLatch latch2 = mock(RetryLatch.class);

        Listeners listeners = new Listeners();
        listeners.listener = latch1;
        listeners.listenerEra = 1;

        listeners.next = new Listeners();
        listeners.next.listener = latch2;
        listeners.next.listenerEra = 2;

        listeners.openAll(pool);

        verify(latch1).open(1);
        verify(latch2).open(2);
    }
}
