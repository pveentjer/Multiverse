package org.multiverse.api.functions;

/**
 * A {@link IntFunction} that increased the value with the supplied amount.
 *
 * @author Peter Veentjer.
 */
public final class IncIntFunction extends IntFunction {

    public final static IncIntFunction INSTANCE = new IncIntFunction();

    private final int inc;

    /**
     * Creates an IncIntFunction that adds one.
     */
    public IncIntFunction() {
        this(1);
    }

    /**
     * Creates an IncIntFunction with the specified
     *
     * @param inc the number to increment with.
     */
    public IncIntFunction(int inc) {
        this.inc = inc;
    }

    @Override
    public int call(int current) {
        return current + inc;
    }

    @Override
    public String toString() {
        return "IncIntFunction{" +
                "inc=" + inc +
                '}';
    }
}
