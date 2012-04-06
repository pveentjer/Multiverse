package org.multiverse.stms.gamma.transactionalobjects;

import org.multiverse.api.LockMode;
import org.multiverse.api.Txn;
import org.multiverse.api.exceptions.LockedException;
import org.multiverse.api.functions.BooleanFunction;
import org.multiverse.api.predicates.BooleanPredicate;
import org.multiverse.api.references.TxnBoolean;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.Listeners;
import org.multiverse.stms.gamma.transactions.GammaTxn;

import static org.multiverse.api.GlobalStmInstance.getGlobalStmInstance;
import static org.multiverse.stms.gamma.GammaStmUtils.*;
import static org.multiverse.stms.gamma.ThreadLocalGammaObjectPool.getThreadLocalGammaObjectPool;

/**
 * A {@link org.multiverse.api.references.TxnBoolean} for the {@link GammaStm}.
 *
 * @author Peter Veentjer.
 */
public class GammaTxnBoolean extends BaseGammaTxnRef implements TxnBoolean {

    public GammaTxnBoolean(boolean value){
        this((GammaStm) getGlobalStmInstance(),value);
    }

    public GammaTxnBoolean(final GammaTxn tx) {
        this(tx, false);
    }

    public GammaTxnBoolean(final GammaTxn tx, final boolean value) {
        super(tx.getConfig().stm, TYPE_BOOLEAN);

        arriveAndLock(1, LOCKMODE_EXCLUSIVE);
        Tranlocal tranlocal = openForConstruction(tx);
        tranlocal.long_value = booleanAsLong(value);
    }

    public GammaTxnBoolean(final GammaStm stm) {
        this(stm, false);
    }

    public GammaTxnBoolean(final GammaStm stm, final boolean b) {
        super(stm, TYPE_BOOLEAN);
        this.long_value = booleanAsLong(b);
        //noinspection PointlessArithmeticExpression
        this.version = VERSION_UNCOMMITTED + 1;
    }

    @Override
    public final boolean get() {
        return get(getRequiredThreadLocalGammaTxn());
    }

    @Override
    public final boolean get(final Txn tx) {
        return get(asGammaTxn(tx));
    }

    public final boolean get(final GammaTxn tx) {
        return longAsBoolean(openForRead(tx, LOCKMODE_NONE).long_value);
    }

    @Override
    public final boolean getAndLock(LockMode lockMode) {
        return getAndLock(getRequiredThreadLocalGammaTxn(), lockMode);
    }

    @Override
    public final boolean getAndLock(Txn tx, LockMode lockMode) {
        return getAndLock(asGammaTxn(tx), lockMode);
    }

    public final boolean getAndLock(GammaTxn tx, LockMode lockMode) {
        return longAsBoolean(getLong(asGammaTxn(tx), lockMode));
    }

    @Override
    public final boolean set(final boolean value) {
        return set(getRequiredThreadLocalGammaTxn(), value);
    }

    @Override
    public final boolean set(final Txn tx, final boolean value) {
        return set(asGammaTxn(tx), value);
    }

    public final boolean set(final GammaTxn tx, final boolean value) {
        openForWrite(tx, LOCKMODE_NONE).long_value = booleanAsLong(value);
        return value;
    }

    @Override
    public final boolean setAndLock(boolean value, LockMode lockMode) {
        return setAndLock(getRequiredThreadLocalGammaTxn(), value, lockMode);
    }

    @Override
    public final boolean setAndLock(Txn tx, boolean value, LockMode lockMode) {
        return setAndLock(asGammaTxn(tx), value, lockMode);
    }

    public final boolean setAndLock(GammaTxn tx, boolean value, LockMode lockMode) {
        return longAsBoolean(setLong(tx, lockMode, booleanAsLong(value), false));
    }

    @Override
    public final boolean getAndSet(final boolean value) {
        return getAndSet(getRequiredThreadLocalGammaTxn(), value);
    }

    @Override
    public final boolean getAndSet(final Txn tx, final boolean value) {
        return getAndSet(asGammaTxn(tx), value);
    }

    @Override
    public final boolean getAndSetAndLock(boolean value, LockMode lockMode) {
        return getAndSetAndLock(getRequiredThreadLocalGammaTxn(), value, lockMode);
    }

    @Override
    public final boolean getAndSetAndLock(Txn tx, boolean value, LockMode lockMode) {
        return getAndSetAndLock(asGammaTxn(tx), value, lockMode);
    }

    public final boolean getAndSetAndLock(GammaTxn tx, boolean value, LockMode lockMode) {
        return longAsBoolean(setLong(tx, lockMode, booleanAsLong(value), true));
    }

    public final boolean getAndSet(final GammaTxn tx, final boolean value) {
        Tranlocal tranlocal = openForWrite(tx, LOCKMODE_NONE);
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
        commute(getRequiredThreadLocalGammaTxn(), function);
    }

    @Override
    public final void commute(final Txn tx, final BooleanFunction function) {
        commute(asGammaTxn(tx), function);
    }

    public final void commute(final GammaTxn tx, final BooleanFunction function) {
        openForCommute(tx, function);
    }

    @Override
    public final boolean getAndAlter(final BooleanFunction function) {
        return getAndAlter(getRequiredThreadLocalGammaTxn(), function);
    }

    @Override
    public final boolean getAndAlter(final Txn tx, final BooleanFunction function) {
        return getAndAlter(asGammaTxn(tx), function);
    }

    public final boolean getAndAlter(final GammaTxn tx, final BooleanFunction function) {
        return alter(tx, function, true);
    }

    @Override
    public final boolean alterAndGet(final BooleanFunction function) {
        return alterAndGet(getRequiredThreadLocalGammaTxn(), function);
    }

    @Override
    public final boolean alterAndGet(final Txn tx, final BooleanFunction function) {
        return alterAndGet(asGammaTxn(tx), function);
    }

    public final boolean alterAndGet(final GammaTxn tx, final BooleanFunction function) {
        return alter(tx, function, false);
    }

    public final boolean alter(final GammaTxn tx, final BooleanFunction function, final boolean returnOld) {
        if (tx == null) {
            throw new NullPointerException();
        }

        if (function == null) {
            tx.abort();
            throw new NullPointerException("Function can't be null");
        }

        final Tranlocal write = openForWrite(tx, LOCKMODE_NONE);

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
        await(getRequiredThreadLocalGammaTxn(), value);
    }

    @Override
    public final void await(final Txn tx, final boolean value) {
        await(asGammaTxn(tx), value);
    }

    public final void await(final GammaTxn tx, final boolean value) {
        if (longAsBoolean(openForRead(tx, LOCKMODE_NONE).long_value) != value) {
            tx.retry();
        }
    }

    @Override
    public final void await(final BooleanPredicate predicate) {
        await(getRequiredThreadLocalGammaTxn(), predicate);
    }

    @Override
    public final void await(final Txn tx, final BooleanPredicate predicate) {
        await(asGammaTxn(tx), predicate);
    }

    public final void await(final GammaTxn tx, final BooleanPredicate predicate) {
        final Tranlocal tranlocal = openForRead(tx, LOCKMODE_NONE);
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
        return String.format("GammaTxnBoolean{orec=%s, version=%s, value=%s, hasListeners=%s)",
                ___toOrecString(), version, longAsBoolean(long_value), listeners != null);
    }

    @Override
    public final String toString() {
        return toString(getRequiredThreadLocalGammaTxn());
    }

    @Override
    public final String toString(Txn tx) {
        return toString(asGammaTxn(tx));
    }

    public final String toString(GammaTxn tx) {
        return Boolean.toString(get(tx));
    }

    @Override
    public final String atomicToString() {
        return Boolean.toString(atomicGet());
    }
}
