package org.multiverse.stms.gamma;

import org.multiverse.api.Txn;
import org.multiverse.api.exceptions.TxnMandatoryException;
import org.multiverse.stms.gamma.transactionalobjects.GammaObject;
import org.multiverse.stms.gamma.transactions.GammaTxn;

import static java.lang.String.format;
import static org.multiverse.api.TxnThreadLocal.getThreadLocalTxn;

public final class GammaStmUtils {

    public static String toDebugString(GammaObject o) {
        if (o == null) {
            return "null";
        } else {
            return o.getClass().getName() + '@' + System.identityHashCode(o);
        }
    }

    public static GammaTxn getRequiredThreadLocalGammaTxn() {
        final Txn tx = getThreadLocalTxn();

        if (tx == null) {
            throw new TxnMandatoryException();
        }

        return asGammaTxn(tx);
    }

    public static GammaTxn asGammaTxn(final Txn tx) {
        if (tx instanceof GammaTxn) {
            return (GammaTxn) tx;
        }

        if (tx == null) {
            throw new NullPointerException("Txn can't be null");
        }

        tx.abort();
        throw new ClassCastException(
                format("Expected Txn of class %s, found %s", GammaTxn.class.getName(), tx.getClass().getName()));
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
