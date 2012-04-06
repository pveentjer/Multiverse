package org.multiverse.stms.gamma.transactionalobjects;

import org.multiverse.api.LockMode;
import org.multiverse.api.Txn;
import org.multiverse.api.exceptions.LockedException;
import org.multiverse.api.functions.Function;
import org.multiverse.api.predicates.Predicate;
import org.multiverse.api.references.TxnRef;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.Listeners;
import org.multiverse.stms.gamma.transactions.GammaTxn;

import static org.multiverse.api.GlobalStmInstance.getGlobalStmInstance;
import static org.multiverse.api.TxnThreadLocal.getRequiredThreadLocalTxn;
import static org.multiverse.stms.gamma.GammaStmUtils.asGammaTxn;
import static org.multiverse.stms.gamma.GammaStmUtils.getRequiredThreadLocalGammaTxn;
import static org.multiverse.stms.gamma.ThreadLocalGammaObjectPool.getThreadLocalGammaObjectPool;

/**
 * A {@link org.multiverse.api.references.TxnRef} tailored for the {@link GammaStm}.
 *
 * @param <E>
 * @author Peter Veentjer.
 */
@SuppressWarnings({"OverlyComplexClass"})
public class GammaTxnRef<E> extends BaseGammaTxnRef implements TxnRef<E> {

    public GammaTxnRef(E value) {
        this((GammaStm) getGlobalStmInstance(), value);
    }

    public GammaTxnRef(final GammaTxn tx) {
        this(tx, null);
    }

    public GammaTxnRef(final GammaTxn tx, final E value) {
        super(tx.getConfiguration().stm, TYPE_REF);

        arriveAndLock(1, LOCKMODE_EXCLUSIVE);
        Tranlocal tranlocal = openForConstruction(tx);
        tranlocal.ref_value = value;
    }

    public GammaTxnRef(final GammaStm stm) {
        this(stm, null);
    }

    public GammaTxnRef(final GammaStm stm, final E value) {
        super(stm, TYPE_REF);

        this.ref_value = value;
        //noinspection PointlessArithmeticExpression
        this.version = VERSION_UNCOMMITTED + 1;
    }

    @Override
    public final E get() {
        return get(getRequiredThreadLocalGammaTxn());
    }

    @Override
    public final E get(final Txn tx) {
        return get(asGammaTxn(tx));
    }

    public final E get(final GammaTxn tx) {
        return (E) openForRead(tx, LOCKMODE_NONE).ref_value;
    }

    @Override
    public final E getAndLock(final LockMode lockMode) {
        return getAndLock(getRequiredThreadLocalGammaTxn(), lockMode);
    }

    @Override
    public final E getAndLock(final Txn tx, final LockMode lockMode) {
        return getAndLock(asGammaTxn(tx), lockMode);
    }

    public final E getAndLock(final GammaTxn tx, final LockMode lockMode) {
        return (E) getObject(tx, lockMode);
    }

    @Override
    public final E set(final E value) {
        return set(getRequiredThreadLocalTxn(), value);
    }

    @Override
    public final E set(final Txn tx, final E value) {
        return set(asGammaTxn(tx), value);
    }

    public final E set(final GammaTxn tx, final E value) {
        final Tranlocal tranlocal = openForWrite(tx, LOCKMODE_NONE);
        tranlocal.ref_value = value;
        return value;
    }

    @Override
    public final E setAndLock(final E value, final LockMode lockMode) {
        return setAndLock(getRequiredThreadLocalGammaTxn(), value, lockMode);
    }

    @Override
    public final E setAndLock(final Txn tx, final E value, final LockMode lockMode) {
        return setAndLock(asGammaTxn(tx), value, lockMode);
    }

    public final E setAndLock(final GammaTxn tx, final E value, final LockMode lockMode) {
        return (E) setObject(tx, lockMode, value, false);
    }

    @Override
    public final E getAndSet(final E value) {
        return getAndSet(getRequiredThreadLocalTxn(), value);
    }

    @Override
    public final E getAndSet(final Txn tx, final E value) {
        return getAndSet(asGammaTxn(tx), value);
    }

    public final E getAndSet(final GammaTxn tx, final E value) {
        Tranlocal tranlocal = openForWrite(tx, LOCKMODE_NONE);
        E oldValue = (E) tranlocal.ref_value;
        tranlocal.ref_value = value;
        return oldValue;
    }

    @Override
    public final E getAndSetAndLock(final E value, final LockMode lockMode) {
        return getAndSetAndLock(getRequiredThreadLocalGammaTxn(), value, lockMode);
    }

    @Override
    public final E getAndSetAndLock(final Txn tx, final E value, final LockMode lockMode) {
        return getAndSetAndLock(asGammaTxn(tx), value, lockMode);
    }

    public final E getAndSetAndLock(final GammaTxn tx, final E value, final LockMode lockMode) {
        return (E) setObject(tx, lockMode, value, true);
    }

    @Override
    public final E atomicGet() {
        return (E) atomicObjectGet();
    }

    @Override
    public final E atomicWeakGet() {
        return (E) ref_value;
    }

    @Override
    public final E atomicSet(final E newValue) {
        return (E) atomicSetObject(newValue, false);
    }

    @Override
    public final E atomicGetAndSet(final E newValue) {
        return (E) atomicSetObject(newValue, true);
    }


    @Override
    public final void commute(final Function<E> function) {
        commute(getRequiredThreadLocalTxn(), function);
    }

    @Override
    public final void commute(final Txn tx, final Function<E> function) {
        commute(asGammaTxn(tx), function);
    }

    public final void commute(final GammaTxn tx, final Function<E> function) {
        openForCommute(tx, function);
    }

    @Override
    public final E atomicAlterAndGet(final Function<E> function) {
        return atomicAlter(function, false);
    }

    @Override
    public final E atomicGetAndAlter(final Function<E> function) {
        return atomicAlter(function, true);
    }

    private E atomicAlter(final Function<E> function, final boolean returnOld) {
        if (function == null) {
            throw new NullPointerException("Function can't be null");
        }

        final int arriveStatus = arriveAndExclusiveLockOrBackoff();

        if (arriveStatus == FAILURE) {
            throw new LockedException();
        }

        final E oldValue = (E) ref_value;
        E newValue;
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

        ref_value = newValue;
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
    public final E alterAndGet(final Function<E> function) {
        return alterAndGet(getRequiredThreadLocalTxn(), function);
    }

    @Override
    public final E alterAndGet(final Txn tx, final Function<E> function) {
        return alterAndGet(asGammaTxn(tx), function);
    }

    public final E alterAndGet(final GammaTxn tx, final Function<E> function) {
        return alter(tx, function, false);
    }

    @Override
    public final E getAndAlter(final Function<E> function) {
        return getAndAlter(getRequiredThreadLocalTxn(), function);
    }

    @Override
    public final E getAndAlter(final Txn tx, final Function<E> function) {
        return getAndAlter(asGammaTxn(tx), function);
    }

    public final E getAndAlter(final GammaTxn tx, final Function<E> function) {
        return alter(tx, function, true);
    }

    private E alter(final GammaTxn tx, final Function<E> function, final boolean returnOld) {
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
            E oldValue = (E) write.ref_value;
            write.ref_value = function.call(oldValue);
            abort = false;
            return returnOld ? oldValue : (E) write.ref_value;
        } finally {
            if (abort) {
                tx.abort();
            }
        }
    }

    @Override
    public final boolean atomicCompareAndSet(final E expectedValue, final E newValue) {
        final int arriveStatus = arriveAndExclusiveLockOrBackoff();

        if (arriveStatus == FAILURE) {
            throw new LockedException();
        }

        final E currentValue = (E) ref_value;

        if (currentValue != expectedValue) {
            departAfterFailureAndUnlock();
            return false;
        }

        if (expectedValue == newValue) {
            if ((arriveStatus & MASK_UNREGISTERED) != 0) {
                unlockByUnregistered();
            } else {
                departAfterReadingAndUnlock();
            }

            return true;
        }

        if ((arriveStatus & MASK_CONFLICT) != 0) {
            stm.globalConflictCounter.signalConflict();
        }

        ref_value = newValue;
        //noinspection NonAtomicOperationOnVolatileField
        version++;
        final Listeners listeners = ___removeListenersAfterWrite();

        departAfterUpdateAndUnlock();

        if (listeners != null) {
            listeners.openAll(getThreadLocalGammaObjectPool());
        }

        return true;
    }


    @Override
    public final boolean isNull() {
        return isNull(getRequiredThreadLocalGammaTxn());
    }

    @Override
    public final boolean isNull(final Txn tx) {
        return isNull(asGammaTxn(tx));
    }

    public final boolean isNull(final GammaTxn tx) {
        return openForRead(tx, LOCKMODE_NONE).ref_value == null;
    }

    @Override
    public final boolean atomicIsNull() {
        return atomicGet() == null;
    }

    @Override
    public final E awaitNotNullAndGet() {
        return awaitNotNullAndGet(getRequiredThreadLocalGammaTxn());
    }

    @Override
    public final E awaitNotNullAndGet(final Txn tx) {
        return awaitNotNullAndGet(asGammaTxn(tx));
    }

    public final E awaitNotNullAndGet(final GammaTxn tx) {
        final Tranlocal tranlocal = openForRead(tx, LOCKMODE_NONE);

        if (tranlocal.ref_value == null) {
            tx.retry();
        }

        return (E) tranlocal.ref_value;
    }

    @Override
    public final void awaitNull() {
        await(getRequiredThreadLocalGammaTxn(), (E) null);
    }

    @Override
    public final void awaitNull(final Txn tx) {
        await(asGammaTxn(tx), (E) null);
    }

    public final void awaitNull(final GammaTxn tx) {
        await(tx, (E) null);
    }

    @Override
    public final void await(final E value) {
        await(getRequiredThreadLocalTxn(), value);
    }

    @Override
    public final void await(final Txn tx, final E value) {
        await(asGammaTxn(tx), value);
    }

    public final void await(final GammaTxn tx, final E value) {
        //noinspection ObjectEquality
        if (openForRead(tx, LOCKMODE_NONE).ref_value != value) {
            tx.retry();
        }
    }

    @Override
    public final void await(final Predicate<E> predicate) {
        await(getRequiredThreadLocalTxn(), predicate);
    }

    @Override
    public final void await(final Txn tx, final Predicate<E> predicate) {
        await(asGammaTxn(tx), predicate);
    }

    public final void await(final GammaTxn tx, final Predicate<E> predicate) {
        final Tranlocal tranlocal = openForRead(tx, LOCKMODE_NONE);
        boolean abort = true;
        try {
            if (!predicate.evaluate((E) tranlocal.ref_value)) {
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
        return String.format("GammaTxnRef{orec=%s, version=%s, value=%s, hasListeners=%s)",
                ___toOrecString(), version, ref_value, listeners != null);
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
        final E value = get(tx);
        return value == null ? "null" : value.toString();
    }

    @Override
    public final String atomicToString() {
        final E value = atomicGet();
        return value == null ? "null" : value.toString();
    }
}
