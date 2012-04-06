package org.multiverse.stms.gamma.transactionalobjects;

import org.multiverse.api.Lock;
import org.multiverse.api.LockMode;
import org.multiverse.api.Txn;
import org.multiverse.api.exceptions.PanicError;
import org.multiverse.api.exceptions.TxnMandatoryException;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.Listeners;
import org.multiverse.stms.gamma.transactions.GammaTxn;
import org.multiverse.utils.ToolUnsafe;
import sun.misc.Unsafe;

import static java.lang.String.format;
import static org.multiverse.api.TxnThreadLocal.getThreadLocalTxn;

@SuppressWarnings({"OverlyComplexClass"})
public abstract class AbstractGammaObject implements GammaObject, Lock {

    public static final long MASK_OREC_EXCLUSIVELOCK = 0x8000000000000000L;
    public static final long MASK_OREC_UPDATELOCK = 0x4000000000000000L;
    public static final long MASK_OREC_READBIASED = 0x2000000000000000L;
    public static final long MASK_OREC_READLOCKS = 0x1FFFFF0000000000L;
    public static final long MASK_OREC_SURPLUS = 0x000000FFFFFFFE00L;
    public static final long MASK_OREC_READONLY_COUNT = 0x00000000000003FFL;

    protected static final Unsafe ___unsafe = ToolUnsafe.getUnsafe();
    protected static final long listenersOffset;
    protected static final long valueOffset;

    static {
        try {
            listenersOffset = ___unsafe.objectFieldOffset(
                    AbstractGammaObject.class.getDeclaredField("listeners"));
            valueOffset = ___unsafe.objectFieldOffset(
                    AbstractGammaObject.class.getDeclaredField("orec"));
        } catch (Exception ex) {
            throw new Error(ex);
        }
    }

    public final GammaStm stm;

    @SuppressWarnings({"UnusedDeclaration"})
    public volatile Listeners listeners;

    @SuppressWarnings({"VolatileLongOrDoubleField"})
    public volatile long version;

    @SuppressWarnings({"VolatileLongOrDoubleField"})
    public volatile long orec;

    //This field has a controlled JMM problem (just like the hashcode of String).
    protected int identityHashCode;

    //it is important that the maximum threshold is not larger than 1023 (there are 10 bits for the readonly count)
    private final int readBiasedThreshold;

    public AbstractGammaObject(GammaStm stm) {
        assert stm != null;
        this.stm = stm;
        this.readBiasedThreshold = stm.readBiasedThreshold;
    }

    @Override
    public final long getVersion() {
        return version;
    }

    @Override
    public final GammaStm getStm() {
        return stm;
    }

    @Override
    public final Lock getLock() {
        return this;
    }

    public final Listeners ___removeListenersAfterWrite() {
        if (listeners == null) {
            return null;
        }

        Listeners removedListeners;
        while (true) {
            removedListeners = listeners;
            if (___unsafe.compareAndSwapObject(this, listenersOffset, removedListeners, null)) {
                return removedListeners;
            }
        }
    }

    //a controlled jmm problem here since identityHashCode is not synchronized/volatile/final.
    //this is the same as with the hashcode and String.
    @Override
    public final int identityHashCode() {
        int tmp = identityHashCode;
        if (tmp != 0) {
            return tmp;
        }

        tmp = System.identityHashCode(this);
        identityHashCode = tmp;
        return tmp;
    }

    public final int atomicGetLockModeAsInt() {
        final long current = orec;

        if (hasExclusiveLock(current)) {
            return LOCKMODE_EXCLUSIVE;
        }

        if (hasWriteLock(current)) {
            return LOCKMODE_WRITE;
        }

        if (getReadLockCount(current) > 0) {
            return LOCKMODE_READ;
        }

        return LOCKMODE_NONE;
    }

    @Override
    public final LockMode atomicGetLockMode() {
        switch (atomicGetLockModeAsInt()) {
            case LOCKMODE_NONE:
                return LockMode.None;
            case LOCKMODE_READ:
                return LockMode.Read;
            case LOCKMODE_WRITE:
                return LockMode.Write;
            case LOCKMODE_EXCLUSIVE:
                return LockMode.Exclusive;
            default:
                throw new IllegalStateException();
        }
    }

    @Override
    public final LockMode getLockMode() {
        final GammaTxn tx = (GammaTxn) getThreadLocalTxn();

        if (tx == null) {
            throw new TxnMandatoryException();
        }

        return getLockMode(tx);
    }

    @Override
    public final LockMode getLockMode(final Txn tx) {
        return getLockMode((GammaTxn) tx);
    }

    public final LockMode getLockMode(final GammaTxn tx) {
        final GammaRefTranlocal tranlocal = tx.locate((BaseGammaRef) this);

        if (tranlocal == null) {
            return LockMode.None;
        }

        switch (tranlocal.getLockMode()) {
            case LOCKMODE_NONE:
                return LockMode.None;
            case LOCKMODE_READ:
                return LockMode.Read;
            case LOCKMODE_WRITE:
                return LockMode.Write;
            case LOCKMODE_EXCLUSIVE:
                return LockMode.Exclusive;
            default:
                throw new IllegalStateException();
        }
    }

    private static void yieldIfNeeded(final int remainingSpins) {
        if (remainingSpins % SPIN_YIELD == 0 && remainingSpins > 0) {
            //noinspection CallToThreadYield
            Thread.yield();
        }
    }

    public final boolean waitForExclusiveLockToBecomeFree(int spinCount) {
        do {
            if (!hasExclusiveLock(orec)) {
                return true;
            }

            spinCount--;
        } while (spinCount >= 0);

        return false;
    }

    public final boolean hasWriteLock() {
        return hasWriteLock(orec);
    }

    public final boolean hasExclusiveLock() {
        return hasExclusiveLock(orec);
    }

    public final int getReadBiasedThreshold() {
        return readBiasedThreshold;
    }

    public final long getSurplus() {
        return getSurplus(orec);
    }

    public final boolean isReadBiased() {
        return isReadBiased(orec);
    }

    public final int getReadonlyCount() {
        return getReadonlyCount(orec);
    }

    public final int getReadLockCount() {
        return getReadLockCount(orec);
    }

    /**
     * Arrives. The Arrive is needed for the fast conflict detection (rich mans conflict).
     *
     * @param spinCount the maximum number of times to spin if the exclusive lock is acquired.
     * @return the arrive status.
     */
    public final int arrive(int spinCount) {
        do {
            final long current = orec;

            if (hasExclusiveLock(current)) {
                spinCount--;
                yieldIfNeeded(spinCount);
                continue;
            }

            long surplus = getSurplus(current);

            final boolean isReadBiased = isReadBiased(current);

            if (isReadBiased) {
                if (surplus == 0) {
                    surplus = 1;
                } else if (surplus == 1) {
                    return MASK_SUCCESS + MASK_UNREGISTERED;
                } else {
                    throw new PanicError("Surplus for a readbiased orec can never be larger than 1");
                }
            } else {
                surplus++;
            }

            final long next = setSurplus(current, surplus);

            if (___unsafe.compareAndSwapLong(this, valueOffset, current, next)) {
                int result = MASK_SUCCESS;

                if (isReadBiased) {
                    result += MASK_UNREGISTERED;
                }

                return result;
            }
        } while (spinCount >= 0);

        return FAILURE;
    }

    public final int upgradeReadLock(int spinCount, final boolean exclusiveLock) {
        do {
            final long current = orec;

            int readLockCount = getReadLockCount(current);

            if (readLockCount == 0) {
                throw new PanicError(format("Can't update from readlock to %s if no readlocks are acquired",
                        exclusiveLock ? "exclusiveLock" : "writeLock"));
            }

            if (readLockCount > 1) {
                spinCount--;
                yieldIfNeeded(spinCount);
                continue;
            }

            long next = setReadLockCount(current, 0);
            if (exclusiveLock) {
                next = setExclusiveLock(next, true);
            } else {
                next = setWriteLock(next, true);
            }

            if (___unsafe.compareAndSwapLong(this, valueOffset, current, next)) {
                int result = MASK_SUCCESS;

                if (exclusiveLock) {
                    if (isReadBiased(current) || getSurplus(current) > 1) {
                        result += MASK_CONFLICT;
                    }
                }

                return result;
            }
        } while (spinCount >= 0);

        return FAILURE;
    }


    /**
     * Upgrades the writeLock to an exclusive lock.
     *
     * @return true if there was at least one conflict write.
     */
    public final boolean upgradeWriteLock() {
        while (true) {
            final long current = orec;

            if (hasExclusiveLock(current)) {
                return false;
            }

            if (!hasWriteLock(current)) {
                throw new PanicError("WriteLock is not acquired");
            }

            long next = setExclusiveLock(current, true);
            next = setWriteLock(next, false);

            if (___unsafe.compareAndSwapLong(this, valueOffset, current, next)) {
                return isReadBiased(current) || getSurplus(current) > 1;
            }
        }
    }

    /**
     * Arrives and tries to acquire the lock. If one of them fails, there will not be any state change.
     *
     * @param spinCount the maximum number of times to spin to wait for the lock to come available.
     * @param lockMode  the desired lockmode. It isn't allowed to be LOCKMODE_NONE.
     * @return the result of this operation.
     */
    public final int arriveAndLock(int spinCount, final int lockMode) {
        assert lockMode != LOCKMODE_NONE;

        do {
            final long current = orec;

            boolean locked = lockMode == LOCKMODE_READ ? hasWriteOrExclusiveLock(current) : hasAnyLock(current);

            if (locked) {
                spinCount--;
                yieldIfNeeded(spinCount);
                continue;
            }

            long currentSurplus = getSurplus(current);
            long surplus = currentSurplus;
            boolean isReadBiased = isReadBiased(current);

            if (isReadBiased) {
                if (surplus == 0) {
                    surplus = 1;
                } else if (surplus > 1) {
                    throw new PanicError("Surplus is larger than 1 and orec is readbiased: " + toOrecString(current));
                }
            } else {
                surplus++;
            }

            long next = setSurplus(current, surplus);

            if (lockMode == LOCKMODE_EXCLUSIVE) {
                next = setExclusiveLock(next, true);
            } else if (lockMode == LOCKMODE_READ) {
                next = setReadLockCount(next, getReadLockCount(current) + 1);
            } else if (lockMode == LOCKMODE_WRITE) {
                next = setWriteLock(next, true);
            }

            if (___unsafe.compareAndSwapLong(this, valueOffset, current, next)) {
                int result = MASK_SUCCESS;

                if (isReadBiased) {
                    result += MASK_UNREGISTERED;
                }

                if (lockMode == LOCKMODE_EXCLUSIVE && currentSurplus > 0) {
                    result += MASK_CONFLICT;
                }

                return result;
            }
        } while (spinCount >= 0);

        return FAILURE;
    }

    /**
     * Tries to acquire the exclusive lock and arrive.
     *
     * @param spinCount the maximum number of spins when it is locked.
     * @return the arrive-status.
     */
    public final int arriveAndExclusiveLock(int spinCount) {
        do {
            final long current = orec;

            if (hasAnyLock(current)) {
                spinCount--;
                yieldIfNeeded(spinCount);
                continue;
            }

            final long currentSurplus = getSurplus(current);
            long surplus = currentSurplus;
            boolean isReadBiased = isReadBiased(current);

            if (isReadBiased) {
                if (surplus == 0) {
                    surplus = 1;
                } else if (surplus > 1) {
                    throw new PanicError("Surplus is larger than 2: " + toOrecString(current));
                }
            } else {
                surplus++;
            }

            long next = setSurplus(current, surplus);
            next = setExclusiveLock(next, true);

            if (___unsafe.compareAndSwapLong(this, valueOffset, current, next)) {
                int result = MASK_SUCCESS;

                if (isReadBiased) {
                    result += MASK_UNREGISTERED;
                }

                if (currentSurplus > 0) {
                    result += MASK_CONFLICT;
                }

                return result;
            }
        } while (spinCount >= 0);

        return FAILURE;
    }

    /**
     * Arrives and tries to acquire the lock. If one of them fails, there will not be any state change.
     *
     * @param spinCount the maximum number of times to spin if a lock is acquired.
     * @param lockMode  the desired lockMode. This is not allowed to be LOCKMODE_NONE.
     * @return the status of the operation.
     */
    public final int lockAfterArrive(int spinCount, final int lockMode) {
        assert lockMode != LOCKMODE_NONE;

        do {
            final long current = orec;

            if (isReadBiased(current)) {
                throw new PanicError("Orec is readbiased " + toOrecString(current));
            }

            boolean locked = lockMode == LOCKMODE_READ ? hasWriteOrExclusiveLock(current) : hasAnyLock(current);

            if (locked) {
                spinCount--;
                yieldIfNeeded(spinCount);
                continue;
            }

            final long currentSurplus = getSurplus(current);
            if (currentSurplus == 0) {
                throw new PanicError("There is no surplus (so if it didn't do a read before)" + toOrecString(current));
            }

            long next = current;
            if (lockMode == LOCKMODE_READ) {
                next = setReadLockCount(next, getReadLockCount(current) + 1);
            } else if (lockMode == LOCKMODE_EXCLUSIVE) {
                next = setExclusiveLock(next, true);
            } else {
                next = setWriteLock(current, true);
            }

            if (___unsafe.compareAndSwapLong(this, valueOffset, current, next)) {
                int result = MASK_SUCCESS;

                if (lockMode == LOCKMODE_EXCLUSIVE && currentSurplus > 1) {
                    result += MASK_CONFLICT;
                }

                return result;
            }
        } while (spinCount >= 0);

        return FAILURE;
    }

    /**
     * Departs after a successful read is done and no lock was acquired.
     * <p/>
     * This call increased the readonly count. If the readonly count threshold is reached, the orec is
     * made readbiased and the readonly count is set to 0.
     */
    public final void departAfterReading() {
        while (true) {
            final long current = orec;

            long surplus = getSurplus(current);
            if (surplus == 0) {
                throw new PanicError("There is no surplus " + toOrecString(current));
            }

            boolean isReadBiased = isReadBiased(current);
            if (isReadBiased) {
                throw new PanicError("Orec is readbiased " + toOrecString(current));
            }

            int readonlyCount = getReadonlyCount(current);
            if (readonlyCount < readBiasedThreshold) {
                readonlyCount++;
            }

            if (surplus <= 1 && hasAnyLock(current)) {
                throw new PanicError("There is not enough surplus " + toOrecString(current));
            }

            surplus--;
            final boolean hasExclusiveLock = hasExclusiveLock(current);
            if (!hasExclusiveLock && surplus == 0 && readonlyCount == readBiasedThreshold) {
                isReadBiased = true;
                readonlyCount = 0;
            }

            long next = setIsReadBiased(current, isReadBiased);
            next = setReadonlyCount(next, readonlyCount);
            next = setSurplus(next, surplus);
            if (___unsafe.compareAndSwapLong(this, valueOffset, current, next)) {
                return;
            }
        }
    }

    /**
     * Departs after a successful read is done and release the lock (it doesn't matter which lock is acquired as long is
     * it is a read/write/exclusive lock.
     * <p/>
     * This method increases the readonly count of the orec and upgraded from update-biased to
     * readbiased if the READBIASED_THRESHOLD is reached (also the readonly count is set to zero
     * if that happens).
     */
    public final void departAfterReadingAndUnlock() {
        while (true) {
            final long current = orec;

            long surplus = getSurplus(current);
            if (surplus == 0) {
                throw new PanicError("There is no surplus: " + toOrecString(current));
            }

            int readLockCount = getReadLockCount(current);

            if (readLockCount == 0 && !hasWriteOrExclusiveLock(current)) {
                throw new PanicError("No Lock acquired " + toOrecString(current));
            }

            boolean isReadBiased = isReadBiased(current);
            if (isReadBiased) {
                throw new PanicError("Orec is readbiased " + toOrecString(current));
            }

            int readonlyCount = getReadonlyCount(current);

            surplus--;

            if (readonlyCount < readBiasedThreshold) {
                readonlyCount++;
            }

            if (surplus == 0 && readonlyCount == readBiasedThreshold) {
                isReadBiased = true;
                readonlyCount = 0;
            }

            long next = current;
            if (readLockCount > 0) {
                next = setReadLockCount(next, readLockCount - 1);
            } else {
                next = setExclusiveLock(next, false);
                next = setWriteLock(next, false);
            }

            next = setIsReadBiased(next, isReadBiased);
            next = setReadonlyCount(next, readonlyCount);
            next = setSurplus(next, surplus);
            if (___unsafe.compareAndSwapLong(this, valueOffset, current, next)) {
                return;
            }
        }
    }

    public final void departAfterUpdateAndUnlock() {
        while (true) {
            final long current = orec;

            if (!hasExclusiveLock(current)) {
                throw new PanicError(
                        "Can't departAfterUpdateAndUnlock if the commit lock is not acquired " + toOrecString(current));
            }

            long surplus = getSurplus(current);
            if (surplus == 0) {
                throw new PanicError(
                        "Can't departAfterUpdateAndUnlock is there is no surplus " + toOrecString(current));
            }

            if (isReadBiased(current)) {
                if (surplus > 1) {
                    throw new PanicError(
                            "The surplus can never be larger than 1 if readBiased " + toOrecString(current));
                }

                //there always is a conflict when a readbiased orec is updated.
                surplus = 0;
            } else {
                surplus--;
            }

            if (surplus == 0) {
                orec = 0;
                return;
            }

            final long next = setSurplus(0, surplus);

            if (___unsafe.compareAndSwapLong(this, valueOffset, current, next)) {
                return;
            }
        }
    }

    /**
     * Departs after a transaction fails and has an arrive on this Orec. It doesn't matter what the lock level
     * is, as long as it is higher than LOCKMODE_NONE. This call can safely be made on a read or update biased
     * ref.
     */
    public final void departAfterFailureAndUnlock() {
        while (true) {
            final long current = orec;

            //-1 indicates write or commit lock, value bigger than 0 indicates readlock
            int lockMode;

            if (hasWriteOrExclusiveLock(current)) {
                lockMode = -1;
            } else {
                lockMode = getReadLockCount(current);
            }

            if (lockMode == 0) {
                throw new PanicError(
                        "No lock was not acquired " + toOrecString(current));
            }

            long surplus = getSurplus(current);
            if (surplus == 0) {
                throw new PanicError(
                        "There is no surplus " + toOrecString(current));
            }

            //we can only decrease the surplus if it is not read biased. Because with a read biased
            //orec, we have no idea how many readers there are.
            if (!isReadBiased(current)) {
                surplus--;
            }

            long next = setSurplus(current, surplus);
            if (lockMode == -1) {
                next = setExclusiveLock(next, false);
                next = setWriteLock(next, false);
            } else {
                next = setReadLockCount(next, lockMode - 1);
            }

            if (___unsafe.compareAndSwapLong(this, valueOffset, current, next)) {
                return;
            }
        }
    }

    /**
     * Departs after failure.
     */
    public final void departAfterFailure() {
        while (true) {
            final long current = orec;

            if (isReadBiased(current)) {
                throw new PanicError("Orec is readbiased:" + toOrecString(current));
            }

            long surplus = getSurplus(current);

            if (hasExclusiveLock(current)) {
                if (surplus < 2) {
                    throw new PanicError(
                            "there must be at least 2 readers, the thread that acquired the lock, " +
                                    "and the calling thread " + toOrecString(current));
                }
            } else if (surplus == 0) {
                throw new PanicError("There is no surplus " + toOrecString(current));
            }

            surplus--;

            long next = setSurplus(current, surplus);

            if (___unsafe.compareAndSwapLong(this, valueOffset, current, next)) {
                return;
            }
        }
    }

    public final void unlockByUnregistered() {
        while (true) {
            final long current = orec;

            //-1 indicates write or commit lock, value bigger than 0 indicates readlock
            if (!isReadBiased(current)) {
                throw new PanicError(
                        "Can't ___unlockByReadBiased when it is not readbiased " + toOrecString(current));
            }

            int lockMode;
            if (hasWriteOrExclusiveLock(current)) {
                lockMode = -1;
            } else {
                lockMode = getReadLockCount(current);
            }

            if (lockMode == 0) {
                throw new PanicError("No Lock " + toOrecString(current));
            }

            if (getSurplus(current) > 1) {
                throw new PanicError("Surplus for readbiased orec larger than 1 " + toOrecString(current));
            }

            long next = current;
            if (lockMode > 0) {
                next = setReadLockCount(next, lockMode - 1);
            } else {
                next = setExclusiveLock(next, false);
                next = setWriteLock(next, false);
            }

            if (___unsafe.compareAndSwapLong(this, valueOffset, current, next)) {
                return;
            }
        }
    }

    public final String ___toOrecString() {
        return toOrecString(orec);
    }

    public static long setReadLockCount(final long value, final long readLockCount) {
        return (value & ~MASK_OREC_READLOCKS) | (readLockCount << 40);
    }

    public static int getReadLockCount(final long value) {
        return (int) ((value & MASK_OREC_READLOCKS) >> 40);
    }

    public static long setExclusiveLock(final long value, final boolean exclusiveLock) {
        return (value & ~MASK_OREC_EXCLUSIVELOCK) | ((exclusiveLock ? 1L : 0L) << 63);
    }

    public static boolean hasWriteOrExclusiveLock(final long value) {
        return ((value & (MASK_OREC_EXCLUSIVELOCK + MASK_OREC_UPDATELOCK)) != 0);
    }

    public static boolean hasAnyLock(final long value) {
        return ((value & (MASK_OREC_EXCLUSIVELOCK + MASK_OREC_UPDATELOCK + MASK_OREC_READLOCKS)) != 0);
    }

    public static boolean hasExclusiveLock(final long value) {
        return (value & MASK_OREC_EXCLUSIVELOCK) != 0;
    }

    public static boolean isReadBiased(final long value) {
        return (value & MASK_OREC_READBIASED) != 0;
    }

    public static long setIsReadBiased(final long value, final boolean isReadBiased) {
        return (value & ~MASK_OREC_READBIASED) | ((isReadBiased ? 1L : 0L) << 61);
    }

    public static boolean hasWriteLock(final long value) {
        return (value & MASK_OREC_UPDATELOCK) != 0;
    }

    public static long setWriteLock(final long value, final boolean updateLock) {
        return (value & ~MASK_OREC_UPDATELOCK) | ((updateLock ? 1L : 0L) << 62);
    }

    public static int getReadonlyCount(final long value) {
        return (int) (value & MASK_OREC_READONLY_COUNT);
    }

    public static long setReadonlyCount(final long value, final int readonlyCount) {
        return (value & ~MASK_OREC_READONLY_COUNT) | readonlyCount;
    }

    public static long setSurplus(final long value, final long surplus) {
        return (value & ~MASK_OREC_SURPLUS) | (surplus << 10);
    }

    public static long getSurplus(final long value) {
        return (value & MASK_OREC_SURPLUS) >> 10;
    }

    private static String toOrecString(final long value) {
        return format(
                "Orec(hasExclusiveLock=%s, hasWriteLock=%s, readLocks=%s, surplus=%s, isReadBiased=%s, readonlyCount=%s)",
                hasExclusiveLock(value),
                hasWriteLock(value),
                getReadLockCount(value),
                getSurplus(value),
                isReadBiased(value),
                getReadonlyCount(value));
    }
}
