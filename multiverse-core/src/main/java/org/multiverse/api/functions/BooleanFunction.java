package org.multiverse.api.functions;

/**
 * A {@link Function} for primitives that accepts an argument of type boolean and returns a new
 * value of the same type.
 *
 * <p>The reason why {@link BooleanFunction} is an abstract class instead of an ordinary interface, is that
 * this class doesn't cause any unwanted boxing of primitives version of the call method is used instead of the one that
 * accepts and returns a/an Boolean).
 *
 * <p>This class is generated.
 *
 * @author Peter Veentjer.
 */
public abstract class BooleanFunction implements Function<Boolean>{

    /**
     * Calculates the new value based on the current value.
     *
     * @param current the current value.
     * @return the new value.
     */
    public abstract boolean call(boolean current);

    @Override
    public final Boolean call(Boolean arg) {
        return call((boolean) arg);
    }
}
