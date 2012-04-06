package org.multiverse.stms.gamma.transactionalobjects;

import org.multiverse.api.LockMode;
import org.multiverse.api.Txn;
import org.multiverse.api.exceptions.LockedException;
import org.multiverse.api.functions.Functions;
import org.multiverse.api.functions.IntFunction;
import org.multiverse.api.predicates.IntPredicate;
import org.multiverse.api.references.TxnInteger;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.Listeners;
import org.multiverse.stms.gamma.transactions.GammaTxn;

import static org.multiverse.api.GlobalStmInstance.getGlobalStmInstance;
import static org.multiverse.stms.gamma.GammaStmUtils.*;
import static org.multiverse.stms.gamma.ThreadLocalGammaObjectPool.getThreadLocalGammaObjectPool;

/**
 * @author Peter Veentjer.
 */
@SuppressWarnings({"OverlyComplexClass"})
public class GammaTxnInteger extends BaseGammaTxnRef implements TxnInteger {

    public GammaTxnInteger(int value) {
        this((GammaStm) getGlobalStmInstance(), value);
    }

    public GammaTxnInteger(final GammaTxn tx) {
        this(tx, 0);
    }

    public GammaTxnInteger(final GammaTxn tx, final int value) {
        super(tx.getConfig().stm, TYPE_INT);

        arriveAndLock(1, LOCKMODE_EXCLUSIVE);
        Tranlocal tranlocal = openForConstruction(tx);
        tranlocal.long_value = value;
    }

    public GammaTxnInteger(final GammaStm stm) {
        this(stm, 0);
    }

    public GammaTxnInteger(final GammaStm stm, final int value) {
        super(stm, TYPE_INT);
        this.long_value = value;
        //noinspection PointlessArithmeticExpression
        this.version = VERSION_UNCOMMITTED + 1;
    }

    @Override
    public final int get() {
        return get(getRequiredThreadLocalGammaTxn());
    }

    @Override
    public final int get(final Txn tx) {
        return get(asGammaTxn(tx));
    }

    public final int get(final GammaTxn tx) {
        return (int) openForRead(tx, LOCKMODE_NONE).long_value;
    }

    @Override
    public int getAndLock(final LockMode lockMode) {
        return getAndLock(getRequiredThreadLocalGammaTxn(), lockMode);
    }

    @Override
    public final int getAndLock(final Txn tx, final LockMode lockMode) {
        return getAndLock(asGammaTxn(tx), lockMode);
    }

    public final int getAndLock(final GammaTxn tx, final LockMode lockMode) {
        return (int) getLong(tx, lockMode);
    }

    @Override
    public final int set(final int value) {
        return set(getRequiredThreadLocalGammaTxn(), value);
    }

    @Override
    public final int set(final Txn tx, final int value) {
        return set(asGammaTxn(tx), value);
    }

    public final int set(final GammaTxn tx, final int value) {
        Tranlocal tranlocal = openForWrite(tx, LOCKMODE_NONE);
        tranlocal.long_value = value;
        return value;
    }

    @Override
    public final int setAndLock(final int value, final LockMode lockMode) {
        return setAndLock(getRequiredThreadLocalGammaTxn(), value, lockMode);
    }

    @Override
    public final int setAndLock(final Txn tx, final int value, final LockMode lockMode) {
        return setAndLock(asGammaTxn(tx), value, lockMode);
    }

    public final int setAndLock(final GammaTxn tx, final int value, final LockMode lockMode) {
        return (int) longAsDouble(setLong(tx, lockMode, value, false));
    }

    @Override
    public final int getAndSet(final int value) {
        return getAndSet(getRequiredThreadLocalGammaTxn(), value);
    }

    @Override
    public final int getAndSet(final Txn tx, final int value) {
        return getAndSet(asGammaTxn(tx), value);
    }

    public final int getAndSet(final GammaTxn tx, final int value) {
        Tranlocal tranlocal = openForWrite(tx, LOCKMODE_NONE);
        int oldValue = (int) tranlocal.long_value;
        tranlocal.long_value = value;
        return oldValue;
    }

    @Override
    public final int getAndSetAndLock(final int value, final LockMode lockMode) {
        return getAndSetLock(getRequiredThreadLocalGammaTxn(), value, lockMode);
    }

    @Override
    public final int getAndSetAndLock(final Txn tx, final int value, final LockMode lockMode) {
        return getAndSetLock(asGammaTxn(tx), value, lockMode);
    }

    public final int getAndSetLock(final GammaTxn tx, final int value, final LockMode lockMode) {
        return (int) setLong(tx, lockMode, value, true);
    }

    @Override
    public final int atomicGet() {
        return (int) atomicGetLong();
    }

    @Override
    public final int atomicWeakGet() {
        return (int) long_value;
    }

    @Override
    public final int atomicSet(final int newValue) {
        return (int) atomicSetLong(newValue, false);
    }

    @Override
    public final int atomicGetAndSet(final int newValue) {
        return (int) atomicSetLong(newValue, true);
    }

    @Override
    public final void commute(final IntFunction function) {
        commute(getRequiredThreadLocalGammaTxn(), function);
    }

    @Override
    public final void commute(final Txn tx, final IntFunction function) {
        commute(asGammaTxn(tx), function);
    }

    public final void commute(final GammaTxn tx, final IntFunction function) {
        openForCommute(tx, function);
    }

    @Override
    public final int atomicAlterAndGet(final IntFunction function) {
        return atomicAlter(function, false);
    }

    @Override
    public final int atomicGetAndAlter(final IntFunction function) {
        return atomicAlter(function, true);
    }

    private int atomicAlter(final IntFunction function, final boolean returnOld) {
        if (function == null) {
            throw new NullPointerException("Function can't be null");
        }

        final int arriveStatus = arriveAndExclusiveLockOrBackoff();

        if (arriveStatus == FAILURE) {
            throw new LockedException();
        }

        final int oldValue = (int) long_value;
        int newValue;
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

        long_value = newValue;
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
    public final int alterAndGet(final IntFunction function) {
        return alterAndGet(getRequiredThreadLocalGammaTxn(), function);
    }

    @Override
    public final int alterAndGet(final Txn tx, final IntFunction function) {
        return alterAndGet(asGammaTxn(tx), function);
    }

    public final int alterAndGet(final GammaTxn tx, final IntFunction function) {
        return alter(tx, function, false);
    }

    @Override
    public final int getAndAlter(final IntFunction function) {
        return getAndAlter(getRequiredThreadLocalGammaTxn(), function);
    }

    @Override
    public final int getAndAlter(final Txn tx, final IntFunction function) {
        return getAndAlter(asGammaTxn(tx), function);
    }

    public final int getAndAlter(final GammaTxn tx, final IntFunction function) {
        return alter(tx, function, true);
    }

    private int alter(final GammaTxn tx, final IntFunction function, final boolean returnOld) {
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
            int oldValue = (int) write.long_value;
            write.long_value = function.call(oldValue);
            abort = false;
            return returnOld ? oldValue : (int) write.long_value;
        } finally {
            if (abort) {
                tx.abort();
            }
        }
    }

    @Override
    public final boolean atomicCompareAndSet(final int expectedValue, final int newValue) {
        return atomicCompareAndSetLong(expectedValue, newValue);
    }

    @Override
    public final int atomicGetAndIncrement(final int amount) {
        return atomicIncrement(amount, true);
    }

    @Override
    public final int atomicIncrementAndGet(final int amount) {
        return atomicIncrement(amount, false);
    }

    private int atomicIncrement(final int amount, boolean returnOld) {
        final int arriveStatus = arriveAndExclusiveLockOrBackoff();

        if (arriveStatus == FAILURE) {
            throw new LockedException();
        }

        final int oldValue = (int) long_value;

        if (amount == 0) {
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

        final int newValue = oldValue + amount;
        long_value = newValue;
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
    public final int getAndIncrement(final int amount) {
        return getAndIncrement(getRequiredThreadLocalGammaTxn(), amount);
    }

    @Override
    public final int getAndIncrement(final Txn tx, final int amount) {
        return getAndIncrement(asGammaTxn(tx), amount);
    }

    public final int getAndIncrement(final GammaTxn tx, final int amount) {
        return increment(tx, amount, true);
    }

    @Override
    public final int incrementAndGet(final int amount) {
        return incrementAndGet(getRequiredThreadLocalGammaTxn(), amount);
    }

    @Override
    public final int incrementAndGet(final Txn tx, final int amount) {
        return incrementAndGet(asGammaTxn(tx), amount);
    }

    public final int incrementAndGet(final GammaTxn tx, final int amount) {
        return increment(tx, amount, false);
    }

    private int increment(final GammaTxn tx, final int amount, final boolean returnOld) {
        Tranlocal tranlocal = openForWrite(tx, LOCKMODE_NONE);
        int oldValue = (int) tranlocal.long_value;
        tranlocal.long_value += amount;
        return returnOld ? oldValue : (int) tranlocal.long_value;
    }

    @Override
    public final void increment() {
        increment(getRequiredThreadLocalGammaTxn(), 1);
    }

    @Override
    public final void increment(final Txn tx) {
        increment(asGammaTxn(tx), 1);
    }

    @Override
    public final void increment(final int amount) {
        increment(getRequiredThreadLocalGammaTxn(), amount);
    }

    @Override
    public final void increment(final Txn tx, final int amount) {
        increment(asGammaTxn(tx), amount);
    }

    public final void increment(final GammaTxn tx, final int amount) {
        commute(tx, Functions.incIntFunction(amount));
    }

    @Override
    public final void decrement() {
        increment(getRequiredThreadLocalGammaTxn(), -1);
    }

    @Override
    public final void decrement(Txn tx) {
        increment(asGammaTxn(tx), -1);
    }

    @Override
    public final void decrement(final int amount) {
        increment(getRequiredThreadLocalGammaTxn(), -amount);
    }

    @Override
    public final void decrement(final Txn tx, final int amount) {
        increment(asGammaTxn(tx), -amount);
    }

    @Override
    public final void await(final int value) {
        await(getRequiredThreadLocalGammaTxn(), value);
    }

    @Override
    public final void await(final Txn tx, final int value) {
        await(asGammaTxn(tx), value);
    }

    public final void await(final GammaTxn tx, final int value) {
        if (get(tx) != value) {
            tx.retry();
        }
    }

    @Override
    public final void await(final IntPredicate predicate) {
        await(getRequiredThreadLocalGammaTxn(), predicate);
    }

    @Override
    public final void await(final Txn tx, final IntPredicate predicate) {
        await(asGammaTxn(tx), predicate);
    }

    public final void await(final GammaTxn tx, final IntPredicate predicate) {
        final Tranlocal tranlocal = openForRead(tx, LOCKMODE_NONE);
        boolean abort = true;
        try {
            if (!predicate.evaluate((int) tranlocal.long_value)) {
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
        return String.format("GammaTxnInteger{orec=%s, version=%s, value=%s, hasListeners=%s)",
                ___toOrecString(), version, long_value, listeners != null);
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
        return Integer.toString(get(tx));
    }

    @Override
    public final String atomicToString() {
        return Integer.toString(atomicGet());
    }
}
