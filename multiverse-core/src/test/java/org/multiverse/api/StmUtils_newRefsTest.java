package org.multiverse.api;

import org.junit.Test;
import org.multiverse.api.references.*;

import static org.junit.Assert.*;
import static org.multiverse.TestUtils.assertEqualsDouble;
import static org.multiverse.api.StmUtils.*;

public class StmUtils_newRefsTest {

    @Test
    public void whenNewRefWithDefaultValue() {
        TxnRef<String> ref = newTxnRef();
        assertNotNull(ref);
        assertNull(ref.atomicGet());
    }

    @Test
    public void whennewTxnRef() {
        String value = "foo";
        TxnRef<String> ref = newTxnRef(value);
        assertNotNull(ref);
        assertSame(value, ref.atomicGet());
    }

    @Test
    public void whennewTxnIntegerWithDefaultValue() {
        TxnInteger ref = newTxnInteger();
        assertNotNull(ref);
        assertEquals(0, ref.atomicGet());
    }

    @Test
    public void whennewTxnInteger() {
        int value = 10;
        TxnInteger ref = newTxnInteger(value);
        assertNotNull(ref);
        assertEquals(value, ref.atomicGet());
    }

    @Test
    public void whennewTxnLongWithDefaultValue() {
        TxnLong ref = newTxnLong();
        assertNotNull(ref);
        assertEquals(0, ref.atomicGet());
    }

    @Test
    public void whennewTxnLong() {
        int value = 10;
        TxnLong ref = newTxnLong(value);
        assertNotNull(ref);
        assertEquals(value, ref.atomicGet());
    }

    @Test
    public void whennewTxnBooleanWithDefaultValue() {
        TxnBoolean ref = newTxnBoolean();
        assertNotNull(ref);
        assertFalse(ref.atomicGet());
    }

    @Test
    public void whennewTxnBoolean() {
        boolean value = true;
        TxnBoolean ref = newTxnBoolean(value);
        assertNotNull(ref);
        assertTrue(ref.atomicGet());
    }

    @Test
    public void whennewTxnDoubleWithDefaultValue() {
        TxnDouble ref = newTxnDouble();
        assertNotNull(ref);
        assertEqualsDouble(0, ref.atomicGet());
    }

    @Test
    public void whennewTxnDouble() {
        double value = 10;
        TxnDouble ref = newTxnDouble(value);
        assertNotNull(ref);
        assertEqualsDouble(value, ref.atomicGet());
    }
}
