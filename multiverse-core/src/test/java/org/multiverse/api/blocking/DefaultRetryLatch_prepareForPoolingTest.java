package org.multiverse.api.blocking;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.multiverse.TestUtils.assertClosed;

public class DefaultRetryLatch_prepareForPoolingTest {

    @Test
    public void whenClosed() {
        DefaultRetryLatch latch = new DefaultRetryLatch();
        long era = latch.getEra();
        latch.reset();

        assertClosed(latch);
        assertEquals(era + 1, latch.getEra());
    }

    @Test
    public void whenOpen() {
        DefaultRetryLatch latch = new DefaultRetryLatch();
        long era = latch.getEra();
        latch.open(era);

        latch.reset();
        assertClosed(latch);
        assertEquals(era + 1, latch.getEra());
    }
}
