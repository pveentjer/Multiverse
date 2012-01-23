package org.multiverse.api.predicates;

/**
 * A predicate that checks if some value leads to true or false.
 *
 * @author Peter Veentjer.
 */
public abstract class LongPredicate implements Predicate<Long>{

    public static LongPredicate newEqualsPredicate(final long value) {
        return new LongPredicate() {
            @Override
            public boolean evaluate(final long current) {
                return current == value;
            }
        };
    }

    public static LongPredicate newNotEqualsPredicate(final long value) {
        return new LongPredicate() {
            @Override
            public boolean evaluate(final long current) {
                return current != value;
            }
        };
    }

    public static LongPredicate newLargerThanPredicate(final long value) {
        return new LongPredicate() {
            @Override
            public boolean evaluate(final long current) {
                return current > value;
            }
        };
    }

    public static LongPredicate newLargerThanOrEqualsPredicate(final long value) {
        return new LongPredicate() {
            @Override
            public boolean evaluate(final long current) {
                return current >= value;
            }
        };
    }

    public static LongPredicate newSmallerThanPredicate(final long value) {
        return new LongPredicate() {
            @Override
            public boolean evaluate(final long current) {
                return current < value;
            }
        };
    }

    public static LongPredicate newSmallerThanOrEqualsPredicate(final long value) {
        return new LongPredicate() {
            @Override
            public boolean evaluate(final long current) {
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
    public abstract boolean evaluate(long current);

    @Override
    public final boolean evaluate(Long arg) {
        return evaluate((long) arg);
    }
}
