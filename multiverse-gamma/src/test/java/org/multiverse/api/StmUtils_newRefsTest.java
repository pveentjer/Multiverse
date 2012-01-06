package org.multiverse.api;

import org.junit.Test;
import org.multiverse.api.references.*;

import static org.junit.Assert.*;
import static org.multiverse.TestUtils.assertEqualsDouble;
import static org.multiverse.api.StmUtils.*;

public class StmUtils_newRefsTest {

    @Test
    public void whenNewRefWithDefaultValue() {
        Ref<String> ref = newRef();
        assertNotNull(ref);
        assertNull(ref.atomicGet());
    }

    @Test
    public void whenNewRef() {
        String value = "foo";
        Ref<String> ref = newRef(value);
        assertNotNull(ref);
        assertSame(value, ref.atomicGet());
    }

    @Test
    public void whenNewIntRefWithDefaultValue() {
        IntRef ref = newIntRef();
        assertNotNull(ref);
        assertEquals(0, ref.atomicGet());
    }

    @Test
    public void whenNewIntRef() {
        int value = 10;
        IntRef ref = newIntRef(value);
        assertNotNull(ref);
        assertEquals(value, ref.atomicGet());
    }

    @Test
    public void whenNewLongRefWithDefaultValue() {
        LongRef ref = newLongRef();
        assertNotNull(ref);
        assertEquals(0, ref.atomicGet());
    }

    @Test
    public void whenNewLongRef() {
        int value = 10;
        LongRef ref = newLongRef(value);
        assertNotNull(ref);
        assertEquals(value, ref.atomicGet());
    }

    @Test
    public void whenNewBooleanRefWithDefaultValue() {
        BooleanRef ref = newBooleanRef();
        assertNotNull(ref);
        assertFalse(ref.atomicGet());
    }

    @Test
    public void whenNewBooleanRef() {
        boolean value = true;
        BooleanRef ref = newBooleanRef(value);
        assertNotNull(ref);
        assertTrue(ref.atomicGet());
    }

    @Test
    public void whenNewDoubleRefWithDefaultValue() {
        DoubleRef ref = newDoubleRef();
        assertNotNull(ref);
        assertEqualsDouble(0, ref.atomicGet());
    }

    @Test
    public void whenNewDoubleRef() {
        double value = 10;
        DoubleRef ref = newDoubleRef(value);
        assertNotNull(ref);
        assertEqualsDouble(value, ref.atomicGet());
    }
}
