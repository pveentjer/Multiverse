package org.multiverse.api.predicates;

/**
 * A predicate that checks if some value leads to true or false.
 *
 * @author Peter Veentjer.
 */
public abstract class IntPredicate implements Predicate<Integer>{

     public static IntPredicate newEqualsPredicate(final int value) {
        return new IntPredicate() {
            @Override
            public boolean evaluate(int current) {
                return current == value;
            }
        };
    }

    public static IntPredicate newNotEqualsPredicate(final int value) {
        return new IntPredicate() {
            @Override
            public boolean evaluate(final int current) {
                return current != value;
            }
        };
    }

    public static IntPredicate newLargerThanPredicate(final int value) {
        return new IntPredicate() {
            @Override
            public boolean evaluate(final int current) {
                return current > value;
            }
        };
    }

    public static IntPredicate newLargerThanOrEqualsPredicate(final int value) {
        return new IntPredicate() {
            @Override
            public boolean evaluate(final int current) {
                return current >= value;
            }
        };
    }

    public static IntPredicate newSmallerThanPredicate(final int value) {
        return new IntPredicate() {
            @Override
            public boolean evaluate(final int current) {
                return current < value;
            }
        };
    }

    public static IntPredicate newSmallerThanOrEqualsPredicate(final int value) {
        return new IntPredicate() {
            @Override
            public boolean evaluate(final int current) {
                return current <= value;
            }
        };
    }

    /**
     * Evaluates the predicate
     *
     * @param current the current value.
     * @return true or false.
     */
    public abstract boolean evaluate(int current);

    @Override
    public final boolean evaluate(Integer arg) {
        return evaluate((int) arg);
    }
}
