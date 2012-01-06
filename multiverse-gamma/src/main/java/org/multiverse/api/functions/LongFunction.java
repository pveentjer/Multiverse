package org.multiverse.api.functions;

/**
 * A {@link Function} for primitives that accepts an argument of type long and returns a new
 * value of the same type.
 *
 * <p>The reason why {@link LongFunction} is an abstract class instead of an ordinary interface, is that
 * this class doesn't cause any unwanted boxing of primitives version of the call method is used instead of the one that
 * accepts and returns a/an Long).
 *
 * <p>This class is generated.
 *
 * @author Peter Veentjer.
 */
public abstract class LongFunction implements Function<Long>{

    /**
     * Calculates the new value based on the current value.
     *
     * @param current the current value.
     * @return the new value.
     */
    public abstract long call(long current);

    @Override
    public final Long call(Long arg) {
        return call((long) arg);
    }
}
