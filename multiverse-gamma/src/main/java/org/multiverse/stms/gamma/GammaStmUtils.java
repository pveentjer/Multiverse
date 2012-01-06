package org.multiverse.stms.gamma;

import org.multiverse.api.Transaction;
import org.multiverse.api.exceptions.TransactionMandatoryException;
import org.multiverse.stms.gamma.transactionalobjects.GammaObject;
import org.multiverse.stms.gamma.transactions.GammaTransaction;

import static java.lang.String.format;
import static org.multiverse.api.ThreadLocalTransaction.getThreadLocalTransaction;

public final class GammaStmUtils {

    public static String toDebugString(GammaObject o) {
        if (o == null) {
            return "null";
        } else {
            return o.getClass().getName() + '@' + System.identityHashCode(o);
        }
    }

    public static GammaTransaction getRequiredThreadLocalGammaTransaction() {
        final Transaction tx = getThreadLocalTransaction();

        if (tx == null) {
            throw new TransactionMandatoryException();
        }

        return asGammaTransaction(tx);
    }

    public static GammaTransaction asGammaTransaction(final Transaction tx) {
        if (tx instanceof GammaTransaction) {
            return (GammaTransaction) tx;
        }

        if (tx == null) {
            throw new NullPointerException("Transaction can't be null");
        }

        tx.abort();
        throw new ClassCastException(
                format("Expected Transaction of class %s, found %s", GammaTransaction.class.getName(), tx.getClass().getName()));
    }

    public static boolean longAsBoolean(long value) {
        return value == 1;
    }

    public static long booleanAsLong(boolean b) {
        return b ? 1 : 0;
    }

    public static double longAsDouble(long value) {
        return Double.longBitsToDouble(value);
    }

    public static long doubleAsLong(double value) {
        return Double.doubleToLongBits(value);
    }

    //we don't want instances.
    private GammaStmUtils() {
    }
}
