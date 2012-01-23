package org.multiverse.utils;

import sun.misc.Unsafe;

import java.lang.reflect.Field;

/**
 * A Utility class for accessing the {@link Unsafe}.
 *
 * @author Peter Veentjer
 */
public final class ToolUnsafe {

    /**
     * Fetch the Unsafe.  Use With Caution.
     *
     * @return an Unsafe instance.
     */
    public static Unsafe getUnsafe() {
        // Not on bootclasspath
        if (ToolUnsafe.class.getClassLoader() == null) {
            return Unsafe.getUnsafe();
        }

        try {
            final Field field = Unsafe.class.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            return (Unsafe) field.get(ToolUnsafe.class);
        } catch (Exception e) {
            throw new RuntimeException("Could not access sun.misc.Unsafe", e);
        }
    }

    //we don't want instances.
    private ToolUnsafe() {
    }
}