package org.multiverse.stms.gamma.transactionalobjects;

import org.multiverse.api.LockMode;
import org.multiverse.api.Transaction;
import org.multiverse.api.exceptions.LockedException;
import org.multiverse.api.functions.BooleanFunction;
import org.multiverse.api.predicates.BooleanPredicate;
import org.multiverse.api.references.BooleanRef;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.Listeners;
import org.multiverse.stms.gamma.transactions.GammaTransaction;

import static org.multiverse.api.GlobalStmInstance.getGlobalStmInstance;
import static org.multiverse.stms.gamma.GammaStmUtils.*;
import static org.multiverse.stms.gamma.ThreadLocalGammaObjectPool.getThreadLocalGammaObjectPool;

/**
 * A {@link BooleanRef} for the {@link GammaStm}.
 *
 * @author Peter Veentjer.
 */
public class GammaBooleanRef extends BaseGammaRef implements BooleanRef {

    public GammaBooleanRef(boolean value){
        this((GammaStm) getGlobalStmInstance(),value);
    }

    public GammaBooleanRef(final GammaTransaction tx) {
        this(tx, false);
    }

    public GammaBooleanRef(final GammaTransaction tx, final boolean value) {
        super(tx.getConfiguration().stm, TYPE_BOOLEAN);

        arriveAndLock(1, LOCKMODE_EXCLUSIVE);
        GammaRefTranlocal tranlocal = openForConstruction(tx);
        tranlocal.long_value = booleanAsLong(value);
    }

    public GammaBooleanRef(final GammaStm stm) {
        this(stm, false);
    }

    public GammaBooleanRef(final GammaStm stm, final boolean b) {
        super(stm, TYPE_BOOLEAN);
        this.long_value = booleanAsLong(b);
        //noinspection PointlessArithmeticExpression
        this.version = VERSION_UNCOMMITTED + 1;
    }

    @Override
    public final boolean get() {
        return get(getRequiredThreadLocalGammaTransaction());
    }

    @Override
    public final boolean get(final Transaction tx) {
        return get(asGammaTransaction(tx));
    }

    public final boolean get(final GammaTransaction tx) {
        return longAsBoolean(openForRead(tx, LOCKMODE_NONE).long_value);
    }

    @Override
    public final boolean getAndLock(LockMode lockMode) {
        return getAndLock(getRequiredThreadLocalGammaTransaction(), lockMode);
    }

    @Override
    public final boolean getAndLock(Transaction tx, LockMode lockMode) {
        return getAndLock(asGammaTransaction(tx), lockMode);
    }

    public final boolean getAndLock(GammaTransaction tx, LockMode lockMode) {
        return longAsBoolean(getLong(asGammaTransaction(tx), lockMode));
    }

    @Override
    public final boolean set(final boolean value) {
        return set(getRequiredThreadLocalGammaTransaction(), value);
    }

    @Override
    public final boolean set(final Transaction tx, final boolean value) {
        return set(asGammaTransaction(tx), value);
    }

    public final boolean set(final GammaTransaction tx, final boolean value) {
        openForWrite(tx, LOCKMODE_NONE).long_value = booleanAsLong(value);
        return value;
    }

    @Override
    public final boolean setAndLock(boolean value, LockMode lockMode) {
        return setAndLock(getRequiredThreadLocalGammaTransaction(), value, lockMode);
    }

    @Override
    public final boolean setAndLock(Transaction tx, boolean value, LockMode lockMode) {
        return setAndLock(asGammaTransaction(tx), value, lockMode);
    }

    public final boolean setAndLock(GammaTransaction tx, boolean value, LockMode lockMode) {
        return longAsBoolean(setLong(tx, lockMode, booleanAsLong(value), false));
    }

    @Override
    public final boolean getAndSet(final boolean value) {
        return getAndSet(getRequiredThreadLocalGammaTransaction(), value);
    }

    @Override
    public final boolean getAndSet(final Transaction tx, final boolean value) {
        return getAndSet(asGammaTransaction(tx), value);
    }

    @Override
    public final boolean getAndSetAndLock(boolean value, LockMode lockMode) {
        return getAndSetAndLock(getRequiredThreadLocalGammaTransaction(), value, lockMode);
    }

    @Override
    public final boolean getAndSetAndLock(Transaction tx, boolean value, LockMode lockMode) {
        return getAndSetAndLock(asGammaTransaction(tx), value, lockMode);
    }

    public final boolean getAndSetAndLock(GammaTransaction tx, boolean value, LockMode lockMode) {
        return longAsBoolean(setLong(tx, lockMode, booleanAsLong(value), true));
    }

    public final boolean getAndSet(final GammaTransaction tx, final boolean value) {
        GammaRefTranlocal tranlocal = openForWrite(tx, LOCKMODE_NONE);
        boolean oldValue = longAsBoolean(tranlocal.long_value);
        tranlocal.long_value = booleanAsLong(value);
        return oldValue;
    }

    @Override
    public final boolean atomicGet() {
        return longAsBoolean(atomicGetLong());
    }

    @Override
    public final boolean atomicWeakGet() {
        return longAsBoolean(long_value);
    }

    @Override
    public final boolean atomicSet(final boolean newValue) {
        return longAsBoolean(atomicSetLong(booleanAsLong(newValue), false));
    }

    @Override
    public final boolean atomicGetAndSet(final boolean newValue) {
        return longAsBoolean(atomicSetLong(booleanAsLong(newValue), true));
    }

    @Override
    public final void commute(final BooleanFunction function) {
        commute(getRequiredThreadLocalGammaTransaction(), function);
    }

    @Override
    public final void commute(final Transaction tx, final BooleanFunction function) {
        commute(asGammaTransaction(tx), function);
    }

    public final void commute(final GammaTransaction tx, final BooleanFunction function) {
        openForCommute(tx, function);
    }

    @Override
    public final boolean getAndAlter(final BooleanFunction function) {
        return getAndAlter(getRequiredThreadLocalGammaTransaction(), function);
    }

    @Override
    public final boolean getAndAlter(final Transaction tx, final BooleanFunction function) {
        return getAndAlter(asGammaTransaction(tx), function);
    }

    public final boolean getAndAlter(final GammaTransaction tx, final BooleanFunction function) {
        return alter(tx, function, true);
    }

    @Override
    public final boolean alterAndGet(final BooleanFunction function) {
        return alterAndGet(getRequiredThreadLocalGammaTransaction(), function);
    }

    @Override
    public final boolean alterAndGet(final Transaction tx, final BooleanFunction function) {
        return alterAndGet(asGammaTransaction(tx), function);
    }

    public final boolean alterAndGet(final GammaTransaction tx, final BooleanFunction function) {
        return alter(tx, function, false);
    }

    public final boolean alter(final GammaTransaction tx, final BooleanFunction function, final boolean returnOld) {
        if (tx == null) {
            throw new NullPointerException();
        }

        if (function == null) {
            tx.abort();
            throw new NullPointerException("Function can't be null");
        }

        final GammaRefTranlocal write = openForWrite(tx, LOCKMODE_NONE);

        boolean abort = true;

        try {
            boolean oldValue = longAsBoolean(write.long_value);
            write.long_value = booleanAsLong(function.call(oldValue));
            abort = false;
            return returnOld ? oldValue : longAsBoolean(write.long_value);
        } finally {
            if (abort) {
                tx.abort();
            }
        }
    }

    @Override
    public final boolean atomicAlterAndGet(final BooleanFunction function) {
        return atomicAlter(function, false);
    }

    @Override
    public final boolean atomicGetAndAlter(final BooleanFunction function) {
        return atomicAlter(function, true);
    }

    private boolean atomicAlter(final BooleanFunction function, final boolean returnOld) {
        if (function == null) {
            throw new NullPointerException("Function can't be null");
        }

        final int arriveStatus = arriveAndExclusiveLockOrBackoff();

        if (arriveStatus == FAILURE) {
            throw new LockedException();
        }

        final boolean oldValue = longAsBoolean(long_value);
        boolean newValue;
        boolean abort = true;
        try {
            newValue = function.call(oldValue);
            abort = false;
        } finally {
            if (abort) {
                departAfterFailureAndUnlock();
            }
        }

        if (oldValue == newValue) {
            if ((arriveStatus & MASK_UNREGISTERED) != 0) {
                unlockByUnregistered();
            } else {
                departAfterReadingAndUnlock();
            }

            return oldValue;
        }

        if ((arriveStatus & MASK_CONFLICT) != 0) {
            stm.globalConflictCounter.signalConflict();
        }

        long_value = booleanAsLong(newValue);
        //noinspection NonAtomicOperationOnVolatileField
        version++;

        final Listeners listeners = ___removeListenersAfterWrite();

        departAfterUpdateAndUnlock();

        if (listeners != null) {
            listeners.openAll(getThreadLocalGammaObjectPool());
        }

        return returnOld ? oldValue : newValue;
    }


    @Override
    public final boolean atomicCompareAndSet(final boolean expectedValue, final boolean newValue) {
        return atomicCompareAndSetLong(booleanAsLong(expectedValue), booleanAsLong(newValue));
    }

    @Override
    public final void await(final boolean value) {
        await(getRequiredThreadLocalGammaTransaction(), value);
    }

    @Override
    public final void await(final Transaction tx, final boolean value) {
        await(asGammaTransaction(tx), value);
    }

    public final void await(final GammaTransaction tx, final boolean value) {
        if (longAsBoolean(openForRead(tx, LOCKMODE_NONE).long_value) != value) {
            tx.retry();
        }
    }

    @Override
    public final void await(final BooleanPredicate predicate) {
        await(getRequiredThreadLocalGammaTransaction(), predicate);
    }

    @Override
    public final void await(final Transaction tx, final BooleanPredicate predicate) {
        await(asGammaTransaction(tx), predicate);
    }

    public final void await(final GammaTransaction tx, final BooleanPredicate predicate) {
        final GammaRefTranlocal tranlocal = openForRead(tx, LOCKMODE_NONE);
        boolean abort = true;
        try {
            if (!predicate.evaluate(longAsBoolean(tranlocal.long_value))) {
                tx.retry();
            }
            abort = false;
        } finally {
            if (abort) {
                tx.abort();
            }
        }
    }

    @Override
    public final String toDebugString() {
        return String.format("GammaBooleanRef{orec=%s, version=%s, value=%s, hasListeners=%s)",
                ___toOrecString(), version, longAsBoolean(long_value), listeners != null);
    }

    @Override
    public final String toString() {
        return toString(getRequiredThreadLocalGammaTransaction());
    }

    @Override
    public final String toString(Transaction tx) {
        return toString(asGammaTransaction(tx));
    }

    public final String toString(GammaTransaction tx) {
        return Boolean.toString(get(tx));
    }

    @Override
    public final String atomicToString() {
        return Boolean.toString(atomicGet());
    }
}
