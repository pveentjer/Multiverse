package org.multiverse.api.predicates;

/**
 * A predicate that checks if some value leads to true or false.
 *
 * @author Peter Veentjer.
 */
public abstract class BooleanPredicate implements Predicate<Boolean>{

   public static BooleanPredicate newEqualsPredicate(final boolean value) {
        return new BooleanPredicate() {
            @Override
            public boolean evaluate(final boolean current) {
                return current == value;
            }
        };
    }

     public static BooleanPredicate newNotEqualsPredicate(final boolean value) {
        return new BooleanPredicate() {
            @Override
            public boolean evaluate(final boolean current) {
                return current != value;
            }
        };
    }


    /**
     * Evaluates the predicate
     *
     * @param current the current value.
     * @return true or false.
     */
    public abstract boolean evaluate(boolean current);

    @Override
    public final boolean evaluate(Boolean arg) {
        return evaluate((boolean) arg);
    }
}
