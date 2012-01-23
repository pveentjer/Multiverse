package org.multiverse.api.predicates;

/**
 * Predicate utility class.
 *
 * @author Peter Veentjer.
 */
public final class Predicates {

    private final static Predicate IsNullPredicate = new Predicate() {
        public boolean evaluate(Object value) {
            return value == null;
        }
    };

    private final static Predicate IsNotNullPredicate = new Predicate() {
        public boolean evaluate(Object value) {
            return value != null;
        }
    };

    /**
     * Creates a Predicate that checks if the passed object is null. You will get an existing instance.
     *
     * @return the Predicate.
     */
    public static <E> Predicate<E> newIsNullPredicate() {
        return IsNullPredicate;
    }

    /**
     * Creates a Predicate that checks if the passed object is not null. You will get an existing instance.
     *
     * @return the Predicate.
     */
    public static <E> Predicate<E> newIsNotNullPredicate() {
        return IsNotNullPredicate;
    }

    //we don't want instances.
    private Predicates() {
    }
}
