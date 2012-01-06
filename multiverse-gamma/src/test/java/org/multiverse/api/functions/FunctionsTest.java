package org.multiverse.api.functions;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.multiverse.api.functions.Functions.identityIntFunction;
import static org.multiverse.api.functions.Functions.identityLongFunction;

public class FunctionsTest {

    @Test
    public void testIntIdentityFunction() {
        IntFunction function = identityIntFunction();

        assertEquals(0, function.call(0));
        assertEquals(10, function.call(10));
        assertEquals(-10, function.call(-10));
    }

    @Test
    public void testLongIdentityFunction() {
        LongFunction function = identityLongFunction();

        assertEquals(0, function.call(0));
        assertEquals(10, function.call(10));
        assertEquals(-10, function.call(-10));
    }
}
