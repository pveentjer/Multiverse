package org.multiverse.stms.gamma.transactionalobjects;

import org.multiverse.api.LockMode;
import org.multiverse.api.Transaction;
import org.multiverse.api.exceptions.LockedException;
import org.multiverse.api.functions.Functions;
import org.multiverse.api.functions.LongFunction;
import org.multiverse.api.predicates.LongPredicate;
import org.multiverse.api.references.LongRef;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.Listeners;
import org.multiverse.stms.gamma.transactions.GammaTransaction;

import static org.multiverse.api.GlobalStmInstance.getGlobalStmInstance;
import static org.multiverse.stms.gamma.GammaStmUtils.asGammaTransaction;
import static org.multiverse.stms.gamma.GammaStmUtils.getRequiredThreadLocalGammaTransaction;
import static org.multiverse.stms.gamma.ThreadLocalGammaObjectPool.getThreadLocalGammaObjectPool;

/**
 * A {@link LongRef} for the {@link GammaStm}.
 *
 * @author Peter Veentjer.
 */
@SuppressWarnings({"OverlyComplexClass"})
public class GammaLongRef extends BaseGammaRef implements LongRef {

    public GammaLongRef(long value) {
        this((GammaStm) getGlobalStmInstance(), value);
    }

    public GammaLongRef(final GammaStm stm) {
        this(stm, 0);
    }

    public GammaLongRef(final GammaStm stm, long initialValue) {
        super(stm, TYPE_LONG);
        this.long_value = initialValue;
        //noinspection PointlessArithmeticExpression
        this.version = VERSION_UNCOMMITTED + 1;
    }

    public GammaLongRef(final GammaTransaction tx) {
        this(tx, 0);
    }

    public GammaLongRef(final GammaTransaction tx, final long value) {
        super(tx.getConfiguration().stm, TYPE_LONG);

        arriveAndLock(1, LOCKMODE_EXCLUSIVE);
        GammaRefTranlocal tranlocal = openForConstruction(tx);
        tranlocal.long_value = value;
    }

    @Override
    public final long get() {
        return get(getRequiredThreadLocalGammaTransaction());
    }

    @Override
    public final long get(final Transaction tx) {
        return get(asGammaTransaction(tx));
    }

    public final long get(final GammaTransaction tx) {
        return openForRead(tx, LOCKMODE_NONE).long_value;
    }

    @Override
    public final long getAndLock(final LockMode lockMode) {
        return getAndLock(getRequiredThreadLocalGammaTransaction(), lockMode);
    }

    @Override
    public final long getAndLock(final Transaction tx, final LockMode lockMode) {
        return getAndLock(asGammaTransaction(tx), lockMode);
    }

    public final long getAndLock(final GammaTransaction tx, final LockMode lockMode) {
        return getLong(tx, lockMode);
    }

    @Override
    public final long set(final long value) {
        return set(getRequiredThreadLocalGammaTransaction(), value);
    }

    @Override
    public final long set(final Transaction tx, final long value) {
        return set(asGammaTransaction(tx), value);
    }

    public final long set(final GammaTransaction tx, final long value) {
        openForWrite(tx, LOCKMODE_NONE).long_value = value;
        return value;
    }

    @Override
    public final long setAndLock(final long value, final LockMode lockMode) {
        return setAndLock(getRequiredThreadLocalGammaTransaction(), value, lockMode);
    }

    @Override
    public final long setAndLock(final Transaction tx, final long value, final LockMode lockMode) {
        return setAndLock(asGammaTransaction(tx), value, lockMode);
    }

    public final long setAndLock(final GammaTransaction tx, final long value, final LockMode lockMode) {
        return setLong(tx, lockMode, value, false);
    }

    @Override
    public final long getAndSet(final long value) {
        return getAndSet(getRequiredThreadLocalGammaTransaction(), value);
    }

    public final long getAndSet(final Transaction tx, final long value) {
        return getAndSet(asGammaTransaction(tx), value);
    }

    public final long getAndSet(final GammaTransaction tx, final long value) {
        final GammaRefTranlocal tranlocal = openForWrite(tx, LOCKMODE_NONE);
        final long oldValue = tranlocal.long_value;
        tranlocal.long_value = value;
        return oldValue;
    }

    @Override
    public final long getAndSetAndLock(final long value, final LockMode lockMode) {
        return getAndSetAndLock(getRequiredThreadLocalGammaTransaction(), value, lockMode);
    }

    @Override
    public final long getAndSetAndLock(final Transaction tx, final long value, final LockMode lockMode) {
        return getAndSetLock(asGammaTransaction(tx), value, lockMode);
    }

    public final long getAndSetLock(final GammaTransaction tx, final long value, final LockMode lockMode) {
        return setLong(tx, lockMode, value, true);
    }

    @Override
    public final long atomicGet() {
        return atomicGetLong();
    }

    @Override
    public final long atomicWeakGet() {
        return long_value;
    }

    @Override
    public final long atomicSet(final long newValue) {
        return atomicSetLong(newValue, false);
    }

    @Override
    public final long atomicGetAndSet(final long newValue) {
        return atomicSetLong(newValue, true);
    }

    @Override
    public final void commute(final LongFunction function) {
        commute(getRequiredThreadLocalGammaTransaction(), function);
    }

    @Override
    public final void commute(Transaction tx, LongFunction function) {
        openForCommute(asGammaTransaction(tx), function);
    }

    public final void commute(GammaTransaction tx, LongFunction function) {
        openForCommute(tx, function);
    }

    @Override
    public final long atomicAlterAndGet(final LongFunction function) {
        return atomicAlter(function, false);
    }

    private long atomicAlter(final LongFunction function, final boolean returnOld) {
        if (function == null) {
            throw new NullPointerException("Function can't be null");
        }

        final int arriveStatus = arriveAndExclusiveLockOrBackoff();

        if (arriveStatus == FAILURE) {
            throw new LockedException();
        }

        final long oldValue = long_value;
        long newValue;
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
    public final long alterAndGet(final LongFunction function) {
        return alterAndGet(getRequiredThreadLocalGammaTransaction(), function);
    }

    @Override
    public final long alterAndGet(final Transaction tx, final LongFunction function) {
        return alterAndGet(asGammaTransaction(tx), function);
    }

    public final long alterAndGet(final GammaTransaction tx, final LongFunction function) {
        return alter(tx, function, false);
    }

    @Override
    public final long atomicGetAndAlter(final LongFunction function) {
        return atomicAlter(function, true);
    }

    @Override
    public final long getAndAlter(final LongFunction function) {
        return getAndAlter(getRequiredThreadLocalGammaTransaction(), function);
    }

    @Override
    public final long getAndAlter(final Transaction tx, final LongFunction function) {
        return getAndAlter(asGammaTransaction(tx), function);
    }

    public final long getAndAlter(final GammaTransaction tx, final LongFunction function) {
        return alter(tx, function, true);
    }

    private long alter(final GammaTransaction tx, final LongFunction function, final boolean returnOld) {
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
            long oldValue = write.long_value;
            write.long_value = function.call(oldValue);
            abort = false;
            return returnOld ? oldValue : write.long_value;
        } finally {
            if (abort) {
                tx.abort();
            }
        }
    }

    @Override
    public final boolean atomicCompareAndSet(final long expectedValue, final long newValue) {
        return atomicCompareAndSetLong(expectedValue, newValue);
    }

    @Override
    public final long atomicGetAndIncrement(final long amount) {
        final long result = atomicIncrementAndGet(amount);
        return result - amount;
    }

    @Override
    public final long getAndIncrement(final long amount) {
        return getAndIncrement(getRequiredThreadLocalGammaTransaction(), amount);
    }

    @Override
    public final long getAndIncrement(final Transaction tx, final long amount) {
        return getAndIncrement((GammaTransaction) tx, amount);
    }

    public final long getAndIncrement(final GammaTransaction tx, final long amount) {
        final GammaRefTranlocal tranlocal = openForWrite(tx, LOCKMODE_NONE);
        final long oldValue = tranlocal.long_value;
        tranlocal.long_value += amount;
        return oldValue;
    }

    @Override
    public final long atomicIncrementAndGet(final long amount) {
        final int arriveStatus = arriveAndExclusiveLockOrBackoff();

        if (arriveStatus == FAILURE) {
            throw new LockedException();
        }

        final long oldValue = long_value;

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

        final long newValue = oldValue + amount;
        long_value = newValue;
        //noinspection NonAtomicOperationOnVolatileField
        version++;

        final Listeners listeners = ___removeListenersAfterWrite();

        departAfterUpdateAndUnlock();

        if (listeners != null) {
            listeners.openAll(getThreadLocalGammaObjectPool());
        }

        return newValue;
    }

    @Override
    public final long incrementAndGet(final long amount) {
        return incrementAndGet(getRequiredThreadLocalGammaTransaction(), amount);
    }

    @Override
    public final long incrementAndGet(final Transaction tx, final long amount) {
        return incrementAndGet(asGammaTransaction(tx), amount);
    }

    public final long incrementAndGet(final GammaTransaction tx, final long amount) {
        final GammaRefTranlocal tranlocal = openForWrite(tx, LOCKMODE_NONE);
        tranlocal.long_value += amount;
        return tranlocal.long_value;
    }

    @Override
    public final void increment() {
        increment(getRequiredThreadLocalGammaTransaction());
    }

    @Override
    public final void increment(final Transaction tx) {
        commute(asGammaTransaction(tx), Functions.incLongFunction());
    }

    public final void increment(final GammaTransaction tx) {
        commute(tx, Functions.incLongFunction());
    }

    @Override
    public final void increment(final long amount) {
        commute(getRequiredThreadLocalGammaTransaction(), Functions.incLongFunction(amount));
    }

    @Override
    public final void increment(final Transaction tx, final long amount) {
        commute(asGammaTransaction(tx), Functions.incLongFunction(amount));
    }

    @Override
    public final void decrement() {
        commute(getRequiredThreadLocalGammaTransaction(), Functions.decLongFunction());
    }

    @Override
    public final void decrement(final Transaction tx) {
        commute(asGammaTransaction(tx), Functions.decLongFunction());
    }

    @Override
    public final void decrement(final long amount) {
        commute(getRequiredThreadLocalGammaTransaction(), Functions.incLongFunction(-amount));
    }

    @Override
    public final void decrement(final Transaction tx, final long amount) {
        commute(asGammaTransaction(tx), Functions.incLongFunction(-amount));
    }

    @Override
    public final void await(final long value) {
        await(getRequiredThreadLocalGammaTransaction(), value);
    }

    @Override
    public final void await(final Transaction tx, final long value) {
        await(asGammaTransaction(tx), value);
    }

    public final void await(final GammaTransaction tx, final long value) {
        if (openForRead(tx, LOCKMODE_NONE).long_value != value) {
            tx.retry();
        }
    }

    @Override
    public final void await(final LongPredicate predicate) {
        await(getRequiredThreadLocalGammaTransaction(), predicate);
    }

    @Override
    public final void await(final Transaction tx, final LongPredicate predicate) {
        await(asGammaTransaction(tx), predicate);
    }

    public final void await(final GammaTransaction tx, final LongPredicate predicate) {
        final GammaRefTranlocal tranlocal = openForRead(tx, LOCKMODE_NONE);
        boolean abort = true;
        try {
            if (!predicate.evaluate(tranlocal.long_value)) {
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
        return String.format("GammaLongRef{orec=%s, version=%s, value=%s, hasListeners=%s)",
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
        return Long.toString(get(tx));
    }

    @Override
    public final String atomicToString() {
        return Long.toString(atomicGet());
    }
}
