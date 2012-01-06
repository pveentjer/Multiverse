package org.multiverse.stms.gamma.transactionalobjects;

import org.multiverse.api.LockMode;
import org.multiverse.api.Transaction;
import org.multiverse.api.exceptions.LockedException;
import org.multiverse.api.functions.Functions;
import org.multiverse.api.functions.IntFunction;
import org.multiverse.api.predicates.IntPredicate;
import org.multiverse.api.references.IntRef;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.Listeners;
import org.multiverse.stms.gamma.transactions.GammaTransaction;

import static org.multiverse.api.GlobalStmInstance.getGlobalStmInstance;
import static org.multiverse.stms.gamma.GammaStmUtils.*;
import static org.multiverse.stms.gamma.ThreadLocalGammaObjectPool.getThreadLocalGammaObjectPool;

/**
 * @author Peter Veentjer.
 */
@SuppressWarnings({"OverlyComplexClass"})
public class GammaIntRef extends BaseGammaRef implements IntRef {

    public GammaIntRef(int value) {
        this((GammaStm) getGlobalStmInstance(), value);
    }

    public GammaIntRef(final GammaTransaction tx) {
        this(tx, 0);
    }

    public GammaIntRef(final GammaTransaction tx, final int value) {
        super(tx.getConfiguration().stm, TYPE_INT);

        arriveAndLock(1, LOCKMODE_EXCLUSIVE);
        GammaRefTranlocal tranlocal = openForConstruction(tx);
        tranlocal.long_value = value;
    }

    public GammaIntRef(final GammaStm stm) {
        this(stm, 0);
    }

    public GammaIntRef(final GammaStm stm, final int value) {
        super(stm, TYPE_INT);
        this.long_value = value;
        //noinspection PointlessArithmeticExpression
        this.version = VERSION_UNCOMMITTED + 1;
    }

    @Override
    public final int get() {
        return get(getRequiredThreadLocalGammaTransaction());
    }

    @Override
    public final int get(final Transaction tx) {
        return get(asGammaTransaction(tx));
    }

    public final int get(final GammaTransaction tx) {
        return (int) openForRead(tx, LOCKMODE_NONE).long_value;
    }

    @Override
    public int getAndLock(final LockMode lockMode) {
        return getAndLock(getRequiredThreadLocalGammaTransaction(), lockMode);
    }

    @Override
    public final int getAndLock(final Transaction tx, final LockMode lockMode) {
        return getAndLock(asGammaTransaction(tx), lockMode);
    }

    public final int getAndLock(final GammaTransaction tx, final LockMode lockMode) {
        return (int) getLong(tx, lockMode);
    }

    @Override
    public final int set(final int value) {
        return set(getRequiredThreadLocalGammaTransaction(), value);
    }

    @Override
    public final int set(final Transaction tx, final int value) {
        return set(asGammaTransaction(tx), value);
    }

    public final int set(final GammaTransaction tx, final int value) {
        GammaRefTranlocal tranlocal = openForWrite(tx, LOCKMODE_NONE);
        tranlocal.long_value = value;
        return value;
    }

    @Override
    public final int setAndLock(final int value, final LockMode lockMode) {
        return setAndLock(getRequiredThreadLocalGammaTransaction(), value, lockMode);
    }

    @Override
    public final int setAndLock(final Transaction tx, final int value, final LockMode lockMode) {
        return setAndLock(asGammaTransaction(tx), value, lockMode);
    }

    public final int setAndLock(final GammaTransaction tx, final int value, final LockMode lockMode) {
        return (int) longAsDouble(setLong(tx, lockMode, value, false));
    }

    @Override
    public final int getAndSet(final int value) {
        return getAndSet(getRequiredThreadLocalGammaTransaction(), value);
    }

    @Override
    public final int getAndSet(final Transaction tx, final int value) {
        return getAndSet(asGammaTransaction(tx), value);
    }

    public final int getAndSet(final GammaTransaction tx, final int value) {
        GammaRefTranlocal tranlocal = openForWrite(tx, LOCKMODE_NONE);
        int oldValue = (int) tranlocal.long_value;
        tranlocal.long_value = value;
        return oldValue;
    }

    @Override
    public final int getAndSetAndLock(final int value, final LockMode lockMode) {
        return getAndSetLock(getRequiredThreadLocalGammaTransaction(), value, lockMode);
    }

    @Override
    public final int getAndSetAndLock(final Transaction tx, final int value, final LockMode lockMode) {
        return getAndSetLock(asGammaTransaction(tx), value, lockMode);
    }

    public final int getAndSetLock(final GammaTransaction tx, final int value, final LockMode lockMode) {
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
        commute(getRequiredThreadLocalGammaTransaction(), function);
    }

    @Override
    public final void commute(final Transaction tx, final IntFunction function) {
        commute(asGammaTransaction(tx), function);
    }

    public final void commute(final GammaTransaction tx, final IntFunction function) {
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
        return alterAndGet(getRequiredThreadLocalGammaTransaction(), function);
    }

    @Override
    public final int alterAndGet(final Transaction tx, final IntFunction function) {
        return alterAndGet(asGammaTransaction(tx), function);
    }

    public final int alterAndGet(final GammaTransaction tx, final IntFunction function) {
        return alter(tx, function, false);
    }

    @Override
    public final int getAndAlter(final IntFunction function) {
        return getAndAlter(getRequiredThreadLocalGammaTransaction(), function);
    }

    @Override
    public final int getAndAlter(final Transaction tx, final IntFunction function) {
        return getAndAlter(asGammaTransaction(tx), function);
    }

    public final int getAndAlter(final GammaTransaction tx, final IntFunction function) {
        return alter(tx, function, true);
    }

    private int alter(final GammaTransaction tx, final IntFunction function, final boolean returnOld) {
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
        return getAndIncrement(getRequiredThreadLocalGammaTransaction(), amount);
    }

    @Override
    public final int getAndIncrement(final Transaction tx, final int amount) {
        return getAndIncrement(asGammaTransaction(tx), amount);
    }

    public final int getAndIncrement(final GammaTransaction tx, final int amount) {
        return increment(tx, amount, true);
    }

    @Override
    public final int incrementAndGet(final int amount) {
        return incrementAndGet(getRequiredThreadLocalGammaTransaction(), amount);
    }

    @Override
    public final int incrementAndGet(final Transaction tx, final int amount) {
        return incrementAndGet(asGammaTransaction(tx), amount);
    }

    public final int incrementAndGet(final GammaTransaction tx, final int amount) {
        return increment(tx, amount, false);
    }

    private int increment(final GammaTransaction tx, final int amount, final boolean returnOld) {
        GammaRefTranlocal tranlocal = openForWrite(tx, LOCKMODE_NONE);
        int oldValue = (int) tranlocal.long_value;
        tranlocal.long_value += amount;
        return returnOld ? oldValue : (int) tranlocal.long_value;
    }

    @Override
    public final void increment() {
        increment(getRequiredThreadLocalGammaTransaction(), 1);
    }

    @Override
    public final void increment(final Transaction tx) {
        increment(asGammaTransaction(tx), 1);
    }

    @Override
    public final void increment(final int amount) {
        increment(getRequiredThreadLocalGammaTransaction(), amount);
    }

    @Override
    public final void increment(final Transaction tx, final int amount) {
        increment(asGammaTransaction(tx), amount);
    }

    public final void increment(final GammaTransaction tx, final int amount) {
        commute(tx, Functions.incIntFunction(amount));
    }

    @Override
    public final void decrement() {
        increment(getRequiredThreadLocalGammaTransaction(), -1);
    }

    @Override
    public final void decrement(Transaction tx) {
        increment(asGammaTransaction(tx), -1);
    }

    @Override
    public final void decrement(final int amount) {
        increment(getRequiredThreadLocalGammaTransaction(), -amount);
    }

    @Override
    public final void decrement(final Transaction tx, final int amount) {
        increment(asGammaTransaction(tx), -amount);
    }

    @Override
    public final void await(final int value) {
        await(getRequiredThreadLocalGammaTransaction(), value);
    }

    @Override
    public final void await(final Transaction tx, final int value) {
        await(asGammaTransaction(tx), value);
    }

    public final void await(final GammaTransaction tx, final int value) {
        if (get(tx) != value) {
            tx.retry();
        }
    }

    @Override
    public final void await(final IntPredicate predicate) {
        await(getRequiredThreadLocalGammaTransaction(), predicate);
    }

    @Override
    public final void await(final Transaction tx, final IntPredicate predicate) {
        await(asGammaTransaction(tx), predicate);
    }

    public final void await(final GammaTransaction tx, final IntPredicate predicate) {
        final GammaRefTranlocal tranlocal = openForRead(tx, LOCKMODE_NONE);
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
        return String.format("GammaIntRef{orec=%s, version=%s, value=%s, hasListeners=%s)",
                ___toOrecString(), version, long_value, listeners != null);
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
        return Integer.toString(get(tx));
    }

    @Override
    public final String atomicToString() {
        return Integer.toString(atomicGet());
    }
}
