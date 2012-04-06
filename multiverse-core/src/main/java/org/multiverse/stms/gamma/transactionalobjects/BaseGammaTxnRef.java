package org.multiverse.stms.gamma.transactionalobjects;

import org.multiverse.api.IsolationLevel;
import org.multiverse.api.LockMode;
import org.multiverse.api.Txn;
import org.multiverse.api.blocking.RetryLatch;
import org.multiverse.api.exceptions.LockedException;
import org.multiverse.api.exceptions.TxnMandatoryException;
import org.multiverse.api.functions.*;
import org.multiverse.stms.gamma.GammaObjectPool;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.GammaStmUtils;
import org.multiverse.stms.gamma.Listeners;
import org.multiverse.stms.gamma.transactions.GammaTxn;
import org.multiverse.stms.gamma.transactions.GammaTxnConfig;
import org.multiverse.stms.gamma.transactions.fat.FatFixedLengthGammaTxn;
import org.multiverse.stms.gamma.transactions.fat.FatMonoGammaTxn;
import org.multiverse.stms.gamma.transactions.fat.FatVariableLengthGammaTxn;
import org.multiverse.stms.gamma.transactions.lean.LeanFixedLengthGammaTxn;
import org.multiverse.stms.gamma.transactions.lean.LeanMonoGammaTxn;

import static java.lang.Math.max;
import static org.multiverse.api.TxnThreadLocal.getThreadLocalTxn;
import static org.multiverse.stms.gamma.GammaStmUtils.asGammaTxn;
import static org.multiverse.stms.gamma.GammaStmUtils.getRequiredThreadLocalGammaTxn;
import static org.multiverse.stms.gamma.ThreadLocalGammaObjectPool.getThreadLocalGammaObjectPool;
import static org.multiverse.utils.Bugshaker.shakeBugs;

@SuppressWarnings({"OverlyComplexClass", "OverlyCoupledClass"})
public abstract class BaseGammaTxnRef extends AbstractGammaObject {

    public final int type;
    @SuppressWarnings({"VolatileLongOrDoubleField"})
    public volatile long long_value;
    public volatile Object ref_value;

    protected BaseGammaTxnRef(GammaStm stm, int type) {
        super(stm);
        this.type = type;
    }

    @SuppressWarnings({"BooleanMethodIsAlwaysInverted"})
    public final boolean flattenCommute(final GammaTxn tx, final Tranlocal tranlocal, final int lockMode) {
        assert tranlocal.mode == TRANLOCAL_COMMUTING;

        final GammaTxnConfig config = tx.config;

        tx.initLocalConflictCounter();

        if (!load(tx, tranlocal, lockMode, config.spinCount, tx.richmansMansConflictScan)) {
            return false;
        }

        tranlocal.setDirty(!config.dirtyCheck);
        tranlocal.mode = TRANLOCAL_WRITE;

        if (!tx.isReadConsistent(tranlocal)) {
            return false;
        }

        boolean abort = true;
        try {
            CallableNode node = tranlocal.headCallable;
            while (node != null) {
                evaluate(tranlocal, tx, node.function);
                CallableNode newNext = node.next;
                tx.pool.putCallableNode(node);
                node = newNext;
            }
            tranlocal.headCallable = null;

            abort = false;
        } finally {
            if (abort) {
                tx.abort();
            }
        }

        return true;
    }

    private void evaluate(final Tranlocal tranlocal, GammaTxn tx, final Function function) {
        tx.evaluatingCommute = true;

        try {
            switch (type) {
                case TYPE_REF:
                    tranlocal.ref_value = function.call(tranlocal.ref_value);
                    break;
                case TYPE_INT:
                    IntFunction intFunction = (IntFunction) function;
                    tranlocal.long_value = intFunction.call((int) tranlocal.long_value);
                    break;
                case TYPE_LONG:
                    LongFunction longFunction = (LongFunction) function;
                    tranlocal.long_value = longFunction.call(tranlocal.long_value);
                    break;
                case TYPE_DOUBLE:
                    DoubleFunction doubleFunction = (DoubleFunction) function;
                    double doubleResult = doubleFunction.call(GammaStmUtils.longAsDouble(tranlocal.long_value));
                    tranlocal.long_value = GammaStmUtils.doubleAsLong(doubleResult);
                    break;
                case TYPE_BOOLEAN:
                    BooleanFunction booleanFunction = (BooleanFunction) function;
                    boolean booleanResult = booleanFunction.call(GammaStmUtils.longAsBoolean(tranlocal.long_value));
                    tranlocal.long_value = GammaStmUtils.booleanAsLong(booleanResult);
                    break;
                default:
                    throw new IllegalStateException();
            }
        } finally {
            tx.evaluatingCommute = false;
        }
    }

    public final Listeners commit(final Tranlocal tranlocal, final GammaObjectPool pool) {
        if (!tranlocal.isDirty) {
            releaseAfterReading(tranlocal, pool);
            return null;
        }

        if (type == TYPE_REF) {
            ref_value = tranlocal.ref_value;
            //we need to set them to null to prevent memory leaks.
            tranlocal.ref_value = null;
            tranlocal.ref_oldValue = null;
        } else {
            long_value = tranlocal.long_value;
        }

        version = tranlocal.version + 1;

        Listeners listenerAfterWrite = listeners;

        if (listenerAfterWrite != null) {
            listenerAfterWrite = ___removeListenersAfterWrite();
        }

        releaseAfterUpdate(tranlocal, pool);
        return listenerAfterWrite;
    }

    public final Listeners leanCommit(final Tranlocal tranlocal) {
        assert type == TYPE_REF;

        if (tranlocal.mode == TRANLOCAL_READ) {
            tranlocal.ref_value = null;
            tranlocal.owner = null;
            return null;
        }

        ref_value = tranlocal.ref_value;
        version = tranlocal.version + 1;

        Listeners listenerAfterWrite = listeners;

        if (listenerAfterWrite != null) {
            listenerAfterWrite = ___removeListenersAfterWrite();
        }

        departAfterUpdateAndUnlock();
        tranlocal.ref_value = null;
        tranlocal.lockMode = LOCKMODE_NONE;
        tranlocal.owner = null;
        tranlocal.hasDepartObligation = false;
        return listenerAfterWrite;
    }

    @SuppressWarnings({"BooleanMethodIsAlwaysInverted"})
    public final boolean prepare(final GammaTxn tx, final Tranlocal tranlocal) {
        final int mode = tranlocal.getMode();

        if (mode == TRANLOCAL_CONSTRUCTING) {
            return true;
        }

        if (mode == TRANLOCAL_READ) {
            if (!tranlocal.writeSkewCheck) {
                return true;
            }
            return tryLockAndCheckConflict(tx, tranlocal, tx.config.spinCount, LOCKMODE_READ);
        }

        if (mode == TRANLOCAL_COMMUTING) {
            if (!flattenCommute(tx, tranlocal, LOCKMODE_EXCLUSIVE)) {
                return false;
            }
        }

        if (!tranlocal.isDirty) {
            final boolean isDirty = type == TYPE_REF
                    ? tranlocal.ref_value != tranlocal.ref_oldValue
                    : tranlocal.long_value != tranlocal.long_oldValue;

            if (!isDirty) {
                if (!tranlocal.writeSkewCheck) {
                    return true;
                }
                return tryLockAndCheckConflict(tx, tranlocal, tx.config.spinCount, LOCKMODE_READ);
            }

            tranlocal.isDirty = true;
        }

        return tryLockAndCheckConflict(tx, tranlocal, tx.config.spinCount, LOCKMODE_EXCLUSIVE);
    }

    public final void releaseAfterFailure(final Tranlocal tranlocal, final GammaObjectPool pool) {
        if (type == TYPE_REF) {
            tranlocal.ref_value = null;
            tranlocal.ref_oldValue = null;
        }

        if (tranlocal.headCallable != null) {
            CallableNode node = tranlocal.headCallable;
            do {
                CallableNode next = node.next;
                pool.putCallableNode(node);
                node = next;
            } while (node != null);
            tranlocal.headCallable = null;
        }

        if (tranlocal.hasDepartObligation()) {
            if (tranlocal.isConstructing()) {
                tranlocal.setLockMode(LOCKMODE_NONE);
            } else if (tranlocal.getLockMode() != LOCKMODE_NONE) {
                departAfterFailureAndUnlock();
                tranlocal.setLockMode(LOCKMODE_NONE);
            } else {
                departAfterFailure();
            }
            tranlocal.setDepartObligation(false);
        } else if (tranlocal.getLockMode() != LOCKMODE_NONE) {
            unlockByUnregistered();
            tranlocal.setLockMode(LOCKMODE_NONE);
        }

        tranlocal.owner = null;
    }

    public final void releaseAfterUpdate(final Tranlocal tranlocal, final GammaObjectPool pool) {
        if (type == TYPE_REF) {
            tranlocal.ref_value = null;
            tranlocal.ref_oldValue = null;
        }

        departAfterUpdateAndUnlock();
        tranlocal.lockMode = LOCKMODE_NONE;
        tranlocal.owner = null;
        tranlocal.hasDepartObligation = false;
    }

    public final void releaseAfterReading(final Tranlocal tranlocal, final GammaObjectPool pool) {
        if (type == TYPE_REF) {
            tranlocal.ref_value = null;
            tranlocal.ref_oldValue = null;
        }

        if (tranlocal.hasDepartObligation()) {
            if (tranlocal.getLockMode() != LOCKMODE_NONE) {
                departAfterReadingAndUnlock();
                tranlocal.setLockMode(LOCKMODE_NONE);
            } else {
                departAfterReading();
            }
            tranlocal.setDepartObligation(false);
        } else if (tranlocal.getLockMode() != LOCKMODE_NONE) {
            unlockByUnregistered();
            tranlocal.setLockMode(LOCKMODE_NONE);
        }

        tranlocal.owner = null;
    }

    public final boolean load(
            final GammaTxn tx, final Tranlocal tranlocal, final int lockMode, int spinCount, final boolean arriveNeeded) {

        if (lockMode != LOCKMODE_NONE) {
            final int result = arriveAndLock(spinCount, lockMode);

            if (result == FAILURE) {
                return false;
            }

            tranlocal.owner = this;
            tranlocal.version = version;
            if (type == TYPE_REF) {
                final Object value = ref_value;
                tranlocal.ref_value = value;
                tranlocal.ref_oldValue = value;
            } else {
                final long value = long_value;
                tranlocal.long_value = value;
                tranlocal.long_oldValue = value;
            }
            tranlocal.lockMode = lockMode;
            tranlocal.hasDepartObligation = (result & MASK_UNREGISTERED) == 0;
            tx.commitConflict = (result & MASK_CONFLICT) != 0;
            return true;
        }

        while (true) {
            long readLong = 0;
            Object readRef = null;
            long readVersion;
            if (type == TYPE_REF) {
                do {
                    readVersion = version;
                    readRef = ref_value;
                    if (SHAKE_BUGS) shakeBugs();
                } while (readVersion != version);
            } else {
                do {
                    readVersion = version;
                    readLong = long_value;
                    if (SHAKE_BUGS) shakeBugs();
                } while (readVersion != version);
            }

            if (SHAKE_BUGS) shakeBugs();

            int arriveStatus;
            if (arriveNeeded) {
                arriveStatus = arrive(spinCount);
            } else if (waitForExclusiveLockToBecomeFree(spinCount)) {
                arriveStatus = MASK_SUCCESS + MASK_UNREGISTERED;
            } else {
                arriveStatus = FAILURE;
            }

            if (arriveStatus == FAILURE) {
                return false;
            }

            if (SHAKE_BUGS) shakeBugs();

            if (version == readVersion) {
                tranlocal.owner = this;
                tranlocal.version = readVersion;
                tranlocal.lockMode = LOCKMODE_NONE;
                tranlocal.hasDepartObligation = (arriveStatus & MASK_UNREGISTERED) == 0;

                if (type == TYPE_REF) {
                    tranlocal.ref_value = readRef;
                    tranlocal.ref_oldValue = readRef;
                } else {
                    tranlocal.long_value = readLong;
                    tranlocal.long_oldValue = readLong;
                }

                return true;
            }

            //we are not lucky, the value has changed. But before retrying, we need to depart if the arrive was normal
            if ((arriveStatus & MASK_UNREGISTERED) == 0) {
                departAfterFailure();
            }
        }
    }

    public final Tranlocal openForConstruction(GammaTxn tx) {
        if (tx == null) {
            throw new NullPointerException();
        }

        final int type = tx.transactionType;

        if (type == TRANSACTIONTYPE_FAT_MONO) {
            return openForConstruction((FatMonoGammaTxn) tx);
        } else if (type == TRANSACTIONTYPE_FAT_FIXED_LENGTH) {
            return openForConstruction((FatFixedLengthGammaTxn) tx);
        } else if (type == TRANSACTIONTYPE_FAT_VARIABLE_LENGTH) {
            return openForConstruction((FatVariableLengthGammaTxn) tx);
        } else {
            throw tx.abortOpenForConstructionRequired(this);
        }
    }

    private void initTranlocalForConstruction(final Tranlocal tranlocal) {
        tranlocal.isDirty = true;
        tranlocal.mode = TRANLOCAL_CONSTRUCTING;
        tranlocal.setLockMode(LOCKMODE_EXCLUSIVE);
        tranlocal.setDepartObligation(true);
        if (type == TYPE_REF) {
            tranlocal.ref_value = null;
            tranlocal.ref_oldValue = null;
        } else {
            tranlocal.long_value = 0;
            tranlocal.long_oldValue = 0;
        }
    }

    public final Tranlocal openForConstruction(FatMonoGammaTxn tx) {
        if (tx.status != TX_ACTIVE) {
            throw tx.abortOpenForConstructionOnBadStatus(this);
        }

        final GammaTxnConfig config = tx.config;

        //noinspection ObjectEquality
        if (config.stm != stm) {
            throw tx.abortOpenForConstructionOnBadStm(this);
        }

        if (config.readonly) {
            throw tx.abortOpenForConstructionOnReadonly(this);
        }

        if (tx.evaluatingCommute) {
            throw tx.abortOnOpenForConstructionWhileEvaluatingCommute(this);
        }

        final Tranlocal tranlocal = tx.tranlocal;

        //noinspection ObjectEquality
        if (tranlocal.owner == this) {
            if (!tranlocal.isConstructing()) {
                throw tx.abortOpenForConstructionOnBadReference(this);
            }

            return tranlocal;
        }

        if (tranlocal.owner != null) {
            throw tx.abortOnTransactionTooSmall(2);
        }

        tx.hasWrites = true;
        tranlocal.owner = this;
        initTranlocalForConstruction(tranlocal);
        return tranlocal;
    }

    public final Tranlocal openForConstruction(FatVariableLengthGammaTxn tx) {
        if (tx.status != TX_ACTIVE) {
            throw tx.abortOpenForConstructionOnBadStatus(this);
        }

        final GammaTxnConfig config = tx.config;

        //noinspection ObjectEquality
        if (config.stm != stm) {
            throw tx.abortOpenForConstructionOnBadStm(this);
        }

        if (config.readonly) {
            throw tx.abortOpenForConstructionOnReadonly(this);
        }

        if (tx.evaluatingCommute) {
            throw tx.abortOnOpenForConstructionWhileEvaluatingCommute(this);
        }

        final int identityHash = identityHashCode();
        final int indexOf = tx.indexOf(this, identityHash);

        if (indexOf > -1) {
            final Tranlocal tranlocal = tx.array[indexOf];

            if (!tranlocal.isConstructing()) {
                throw tx.abortOpenForConstructionOnBadReference(this);
            }

            return tranlocal;
        }

        final Tranlocal tranlocal = tx.pool.take(this);
        tranlocal.owner = this;
        initTranlocalForConstruction(tranlocal);
        tx.hasWrites = true;
        tx.attach(tranlocal, identityHash);
        tx.size++;

        return tranlocal;
    }

    public final Tranlocal openForConstruction(FatFixedLengthGammaTxn tx) {
        if (tx.status != TX_ACTIVE) {
            throw tx.abortOpenForConstructionOnBadStatus(this);
        }

        final GammaTxnConfig config = tx.config;

        //noinspection ObjectEquality
        if (config.stm != stm) {
            throw tx.abortOpenForConstructionOnBadStm(this);
        }

        if (config.readonly) {
            throw tx.abortOpenForConstructionOnReadonly(this);
        }

        if (tx.evaluatingCommute) {
            throw tx.abortOnOpenForConstructionWhileEvaluatingCommute(this);
        }

        Tranlocal found = null;
        Tranlocal newNode = null;
        Tranlocal node = tx.head;
        while (true) {
            if (node == null) {
                break;
            } else if (node.owner == this) {
                found = node;
                break;
            } else if (node.owner == null) {
                newNode = node;
                break;
            } else {
                node = node.next;
            }
        }

        if (found != null) {
            if (!found.isConstructing()) {
                throw tx.abortOpenForConstructionOnBadReference(this);
            }

            tx.shiftInFront(found);
            return found;
        }

        if (newNode == null) {
            throw tx.abortOnTransactionTooSmall(config.maxFixedLengthTransactionSize + 1);
        }

        newNode.owner = this;
        initTranlocalForConstruction(newNode);
        tx.size++;
        tx.shiftInFront(newNode);
        tx.hasWrites = true;
        return newNode;
    }
    // ============================================================================================
    // =============================== open for read ==============================================
    // ============================================================================================

    public final Tranlocal openForRead(final GammaTxn tx, final int lockMode) {
        if (tx == null) {
            throw new NullPointerException();
        }

        final int type = tx.transactionType;

        if (type == TRANSACTIONTYPE_LEAN_MONO) {
            return openForRead((LeanMonoGammaTxn) tx, lockMode);
        } else if (type == TRANSACTIONTYPE_LEAN_FIXED_LENGTH) {
            return openForRead((LeanFixedLengthGammaTxn) tx, lockMode);
        } else if (type == TRANSACTIONTYPE_FAT_MONO) {
            return openForRead((FatMonoGammaTxn) tx, lockMode);
        } else if (type == TRANSACTIONTYPE_FAT_FIXED_LENGTH) {
            return openForRead((FatFixedLengthGammaTxn) tx, lockMode);
        } else {
            return openForRead((FatVariableLengthGammaTxn) tx, lockMode);
        }
    }

    public final Tranlocal openForRead(final LeanMonoGammaTxn tx, int lockMode) {
        if (tx.status != TX_ACTIVE) {
            throw tx.abortOpenForReadOnBadStatus(this);
        }

        if (lockMode != LOCKMODE_NONE) {
            throw tx.abortOpenForReadOrWriteOnExplicitLockingDetected(this);
        }

        final Tranlocal tranlocal = tx.tranlocal;

        //noinspection ObjectEquality
        if (tranlocal.owner == this) {
            return tranlocal;
        }

        if (tranlocal.owner != null) {
            throw tx.abortOnTransactionTooSmall(2);
        }

        final GammaTxnConfig config = tx.config;

        if (config.stm != stm) {
            throw tx.abortOpenForReadOnBadStm(this);
        }

        if (type != TYPE_REF) {
            throw tx.abortOpenForReadOnNonRefTypeDetected(this);
        }

        tranlocal.mode = TRANLOCAL_READ;
        tranlocal.owner = this;
        for (; ;) {
            //do the read of the version and ref. It needs to be repeated to make sure that the version we read, belongs to the
            //value.
            Object readRef;
            long readVersion;
            do {
                readVersion = version;
                readRef = ref_value;
                if (SHAKE_BUGS) shakeBugs();

            } while (readVersion != version);

            //wait for the exclusive lock to come available.
            int spinCount = 64;
            for (; ;) {
                if (SHAKE_BUGS) shakeBugs();

                if (!hasExclusiveLock()) {
                    break;
                }
                spinCount--;
                if (spinCount < 0) {
                    throw tx.abortOnReadWriteConflict(this);
                }
            }

            if (SHAKE_BUGS) shakeBugs();

            //check if the version is still the same, if it is not, we have read illegal memory,
            //In that case we are going to try again.
            if (readVersion == version) {
                //at this point we are sure that the read was unlocked.
                tranlocal.version = readVersion;
                tranlocal.ref_value = readRef;
                break;
            }
        }

        return tranlocal;
    }

    public final Tranlocal openForRead(final LeanFixedLengthGammaTxn tx, int lockMode) {
        if (tx.status != TX_ACTIVE) {
            throw tx.abortOpenForReadOnBadStatus(this);
        }

        if (lockMode != LOCKMODE_NONE) {
            throw tx.abortOpenForReadOrWriteOnExplicitLockingDetected(this);
        }

        if (tx.head.owner == this) {
            return tx.head;
        }

        //look inside the transaction if it already is opened for read or otherwise look for an empty spot to
        //place the read.
        Tranlocal found = null;
        Tranlocal newNode = null;
        Tranlocal node = tx.head;
        while (true) {
            if (node == null) {
                break;
            } else if (node.owner == this) {
                found = node;
                break;
            } else if (node.owner == null) {
                newNode = node;
                break;
            } else {
                node = node.next;
            }
        }

        //we have found it.
        if (found != null) {
            tx.shiftInFront(found);
            return found;
        }

        //we have not found it, but there also is no spot available.
        if (newNode == null) {
            throw tx.abortOnTransactionTooSmall(tx.config.maxFixedLengthTransactionSize + 1);
        }

        final GammaTxnConfig config = tx.config;

        if (config.stm != stm) {
            throw tx.abortOpenForReadOnBadStm(this);
        }

        if (type != TYPE_REF) {
            throw tx.abortOpenForReadOnNonRefTypeDetected(this);
        }

        int size = tx.size;
        if (size > config.maximumPoorMansConflictScanLength) {
            throw tx.abortOnRichmanConflictScanDetected();
        }

        //load it
        newNode.mode = TRANLOCAL_READ;
        newNode.isDirty = false;
        newNode.owner = this;
        while (true) {
            //JMM: nothing can jump behind the following statement
            long readVersion;
            Object readRef;
            do {
                readVersion = version;
                readRef = ref_value;
                if (SHAKE_BUGS) shakeBugs();
            } while (readVersion != version);

            //wait for the exclusive lock to come available.
            int spinCount = 64;
            for (; ;) {
                if (SHAKE_BUGS) shakeBugs();
                if (!hasExclusiveLock()) {
                    break;
                }
                spinCount--;
                if (spinCount < 0) {
                    throw tx.abortOnReadWriteConflict(this);
                }
            }
            if (SHAKE_BUGS) shakeBugs();

            //check if the version and value we read are still the same, if they are not, we have read illegal memory,
            //so we are going to try again.
            if (readVersion == version && readRef == ref_value) {
                //at this point we are sure that the read was unlocked.
                newNode.version = readVersion;
                newNode.ref_value = readRef;
                break;
            }
        }

        tx.size = size + 1;
        //lets put it in the front it isn't the first one that is opened.
        if (tx.size > 1) {
            tx.shiftInFront(newNode);
        }

        //check if the transaction still is read consistent.
        if (tx.hasReads) {
            node = tx.head;
            do {
                final BaseGammaTxnRef owner = node.owner;

                //if we are at the end, we are done.
                if (owner == null) {
                    break;
                }

                if (SHAKE_BUGS) shakeBugs();

                if (node != newNode && (owner.hasExclusiveLock() || owner.version != node.version)) {
                    throw tx.abortOnReadWriteConflict(this);
                }

                node = node.next;
            } while (node != null);
        } else {
            tx.hasReads = true;
        }

        //we are done, the load was correct and the transaction still is read consistent.
        return newNode;
    }

    private static void initTranlocalForRead(final GammaTxnConfig config, final Tranlocal tranlocal) {
        tranlocal.isDirty = false;
        tranlocal.mode = TRANLOCAL_READ;
        tranlocal.writeSkewCheck = config.isolationLevel == IsolationLevel.Serializable;
        tranlocal.version = -1;
    }

    public final Tranlocal openForRead(final FatMonoGammaTxn tx, int lockMode) {
        if (tx.status != TX_ACTIVE) {
            throw tx.abortOpenForReadOnBadStatus(this);
        }

        final GammaTxnConfig config = tx.config;

        //noinspection ObjectEquality
        if (config.stm != stm) {
            throw tx.abortOpenForReadOnBadStm(this);
        }

        if (tx.evaluatingCommute) {
            throw tx.abortOnOpenForReadWhileEvaluatingCommute(this);
        }

        lockMode = config.readLockModeAsInt <= lockMode ? lockMode : config.readLockModeAsInt;

        final Tranlocal tranlocal = tx.tranlocal;

        //noinspection ObjectEquality
        if (tranlocal.owner == this) {
            //we have found the tranlocal we are looking for.

            int mode = tranlocal.mode;

            if (mode == TRANLOCAL_CONSTRUCTING) {
                return tranlocal;
            }

            if (mode == TRANLOCAL_COMMUTING) {
                if (!flattenCommute(tx, tranlocal, lockMode)) {
                    throw tx.abortOnReadWriteConflict(this);
                }

                return tranlocal;
            }

            if (lockMode > tranlocal.getLockMode()) {
                if (!tryLockAndCheckConflict(tx, tranlocal, config.spinCount, lockMode)) {
                    throw tx.abortOnReadWriteConflict(this);
                }
            }

            return tranlocal;
        }

        if (tranlocal.owner != null) {
            throw tx.abortOnTransactionTooSmall(2);
        }

        initTranlocalForRead(config, tranlocal);
        if (!load(tx, tranlocal, lockMode, config.spinCount, tx.richmansMansConflictScan)) {
            throw tx.abortOnReadWriteConflict(this);
        }

        return tranlocal;
    }

    public final Tranlocal openForRead(final FatFixedLengthGammaTxn tx, int desiredLockMode) {
        if (tx.status != TX_ACTIVE) {
            throw tx.abortOpenForReadOnBadStatus(this);
        }

        final GammaTxnConfig config = tx.config;

        //noinspection ObjectEquality
        if (config.stm != stm) {
            throw tx.abortOpenForReadOnBadStm(this);
        }

        if (tx.evaluatingCommute) {
            throw tx.abortOnOpenForReadWhileEvaluatingCommute(this);
        }

        Tranlocal found = null;
        Tranlocal newNode = null;
        Tranlocal node = tx.head;
        while (true) {
            if (node == null) {
                break;
            } else if (node.owner == this) {
                found = node;
                break;
            } else if (node.owner == null) {
                newNode = node;
                break;
            } else {
                node = node.next;
            }
        }

        desiredLockMode = config.readLockModeAsInt <= desiredLockMode ? desiredLockMode : config.readLockModeAsInt;

        if (found != null) {
            final int mode = found.mode;

            if (mode == TRANLOCAL_CONSTRUCTING) {
                return found;
            }

            if (mode == TRANLOCAL_COMMUTING) {
                if (!flattenCommute(tx, found, desiredLockMode)) {
                    throw tx.abortOnReadWriteConflict(this);
                }

                return found;
            }

            if (desiredLockMode > found.getLockMode()) {
                if (!tryLockAndCheckConflict(tx, found, config.spinCount, desiredLockMode)) {
                    throw tx.abortOnReadWriteConflict(this);
                }
            }

            tx.shiftInFront(found);
            return found;
        }

        if (newNode == null) {
            throw tx.abortOnTransactionTooSmall(config.maxFixedLengthTransactionSize + 1);
        }

        tx.size++;
        initTranlocalForRead(config, newNode);

        final boolean hasReadsBeforeLoading = tx.hasReads;
        if (!hasReadsBeforeLoading) {
            tx.localConflictCount = config.globalConflictCounter.count();
            tx.hasReads = true;
        }

        if (!load(tx, newNode, desiredLockMode, config.spinCount, tx.richmansMansConflictScan)) {
            throw tx.abortOnReadWriteConflict(this);
        }

        //if (hasReadsBeforeLoading && !tx.isReadConsistent(newNode)) {
        if (!tx.isReadConsistent(newNode)) {
            throw tx.abortOnReadWriteConflict(this);
        }

        tx.shiftInFront(newNode);
        return newNode;
    }

    public final Tranlocal openForRead(final FatVariableLengthGammaTxn tx, int desiredLockMode) {
        if (tx.status != TX_ACTIVE) {
            throw tx.abortOpenForReadOnBadStatus(this);
        }

        final GammaTxnConfig config = tx.config;

        //noinspection ObjectEquality
        if (config.stm != stm) {
            throw tx.abortOpenForReadOnBadStm(this);
        }

        if (tx.evaluatingCommute) {
            throw tx.abortOnOpenForReadWhileEvaluatingCommute(this);
        }

        desiredLockMode = config.readLockModeAsInt <= desiredLockMode ? desiredLockMode : config.readLockModeAsInt;

        final int identityHash = identityHashCode();
        final int indexOf = tx.indexOf(this, identityHash);

        if (indexOf > -1) {
            final Tranlocal tranlocal = tx.array[indexOf];
            final int mode = tranlocal.mode;

            if (mode == TRANLOCAL_CONSTRUCTING) {
                return tranlocal;
            }

            if (mode == TRANLOCAL_COMMUTING) {
                if (!flattenCommute(tx, tranlocal, desiredLockMode)) {
                    throw tx.abortOnReadWriteConflict(this);
                }

                return tranlocal;
            }

            if (desiredLockMode > tranlocal.getLockMode()) {
                if (!tryLockAndCheckConflict(tx, tranlocal, config.spinCount, desiredLockMode)) {
                    throw tx.abortOnReadWriteConflict(this);
                }
            }

            return tranlocal;
        }

        final Tranlocal tranlocal = tx.pool.take(this);
        initTranlocalForRead(config, tranlocal);
        tx.attach(tranlocal, identityHash);
        tx.size++;

        final boolean hasReadsBeforeLoading = tx.hasReads;
        if (!hasReadsBeforeLoading) {
            tx.hasReads = true;
            tx.localConflictCount = config.globalConflictCounter.count();
        }

        if (!load(tx, tranlocal, desiredLockMode, config.spinCount, tx.richmansMansConflictScan)) {
            throw tx.abortOnReadWriteConflict(this);
        }

        //if (hasReadsBeforeLoading && !tx.isReadConsistent(tranlocal)) {
        if (!tx.isReadConsistent(tranlocal)) {
            throw tx.abortOnReadWriteConflict(this);
        }

        return tranlocal;
    }

    // ============================================================================================
    // =============================== open for write =============================================
    // ============================================================================================

    public final Tranlocal openForWrite(final GammaTxn tx, final int lockMode) {
        if (tx == null) {
            throw new NullPointerException();
        }

        final int type = tx.transactionType;

        if (type == TRANSACTIONTYPE_LEAN_MONO) {
            return openForWrite((LeanMonoGammaTxn) tx, lockMode);
        } else if (type == TRANSACTIONTYPE_LEAN_FIXED_LENGTH) {
            return openForWrite((LeanFixedLengthGammaTxn) tx, lockMode);
        } else if (type == TRANSACTIONTYPE_FAT_MONO) {
            return openForWrite((FatMonoGammaTxn) tx, lockMode);
        } else if (type == TRANSACTIONTYPE_FAT_FIXED_LENGTH) {
            return openForWrite((FatFixedLengthGammaTxn) tx, lockMode);
        } else {
            return openForWrite((FatVariableLengthGammaTxn) tx, lockMode);
        }
    }

    public final Tranlocal openForWrite(final LeanMonoGammaTxn tx, int lockMode) {
        final Tranlocal tranlocal = openForRead(tx, lockMode);

        if (!tx.hasWrites) {
            tx.hasWrites = true;
        }

        if (tranlocal.mode == TRANLOCAL_READ) {
            tranlocal.mode = TRANLOCAL_WRITE;
        }

        return tranlocal;
    }

    public final Tranlocal openForWrite(final LeanFixedLengthGammaTxn tx, int lockMode) {
        final Tranlocal tranlocal = openForRead(tx, lockMode);

        if (!tx.hasWrites) {
            tx.hasWrites = true;
        }

        if (tranlocal.mode == TRANLOCAL_READ) {
            tranlocal.mode = TRANLOCAL_WRITE;
        }

        return tranlocal;
    }

    public final Tranlocal openForWrite(final FatMonoGammaTxn tx, final int desiredLockMode) {
        GammaTxnConfig config = tx.config;

        Tranlocal tranlocal = openForRead(tx, max(desiredLockMode, config.writeLockModeAsInt));

        if (config.readonly) {
            throw tx.abortOpenForWriteOnReadonly(this);
        }

        if (!tx.hasWrites) {
            tx.hasWrites = true;
        }

        if (tranlocal.mode == TRANLOCAL_READ) {
            tranlocal.mode = TRANLOCAL_WRITE;
            tranlocal.writeSkewCheck = config.isolationLevel == IsolationLevel.Serializable;
            tranlocal.setDirty(!config.dirtyCheck);
        }

        return tranlocal;
    }

    public final Tranlocal openForWrite(final FatFixedLengthGammaTxn tx, final int lockMode) {
        GammaTxnConfig config = tx.config;

        Tranlocal tranlocal = openForRead(tx, max(lockMode, config.writeLockModeAsInt));

        if (config.readonly) {
            throw tx.abortOpenForWriteOnReadonly(this);
        }

        if (!tx.hasWrites) {
            tx.hasWrites = true;
        }

        if (tranlocal.mode == TRANLOCAL_READ) {
            tranlocal.mode = TRANLOCAL_WRITE;
            tranlocal.writeSkewCheck = config.isolationLevel == IsolationLevel.Serializable;
            tranlocal.setDirty(!config.dirtyCheck);
        }

        return tranlocal;
    }

    public final Tranlocal openForWrite(final FatVariableLengthGammaTxn tx, final int lockMode) {
        GammaTxnConfig config = tx.config;

        Tranlocal tranlocal = openForRead(tx, max(lockMode, config.writeLockModeAsInt));

        if (config.readonly) {
            throw tx.abortOpenForWriteOnReadonly(this);
        }

        if (!tx.hasWrites) {
            tx.hasWrites = true;
        }

        if (tranlocal.mode == TRANLOCAL_READ) {
            tranlocal.mode = TRANLOCAL_WRITE;
            tranlocal.writeSkewCheck = config.isolationLevel == IsolationLevel.Serializable;
            tranlocal.setDirty(!config.dirtyCheck);
        }

        return tranlocal;
    }

    // ============================================================================================
    // ================================= open for commute =========================================
    // ============================================================================================

    public final void openForCommute(final GammaTxn tx, final Function function) {
        if (tx == null) {
            throw new NullPointerException("txn can't be null");
        }

        final int type = tx.transactionType;

        if (type == TRANSACTIONTYPE_FAT_MONO) {
            openForCommute((FatMonoGammaTxn) tx, function);
        } else if (type == TRANSACTIONTYPE_FAT_FIXED_LENGTH) {
            openForCommute((FatFixedLengthGammaTxn) tx, function);
        } else if (type == TRANSACTIONTYPE_FAT_VARIABLE_LENGTH) {
            openForCommute((FatVariableLengthGammaTxn) tx, function);
        } else {
            throw tx.abortCommuteOnCommuteDetected(this);
        }
    }

    private void initTranlocalForCommute(final GammaTxnConfig config, final Tranlocal tranlocal) {
        tranlocal.owner = this;
        tranlocal.mode = TRANLOCAL_COMMUTING;
        tranlocal.isDirty = !config.dirtyCheck;
        tranlocal.writeSkewCheck = false;
    }

    public final void openForCommute(final FatMonoGammaTxn tx, final Function function) {
        if (tx == null) {
            throw new NullPointerException();
        }

        if (tx.status != TX_ACTIVE) {
            throw tx.abortCommuteOnBadStatus(this, function);
        }

        if (function == null) {
            throw tx.abortCommuteOnNullFunction(this);
        }

        if (tx.evaluatingCommute) {
            throw tx.abortOnOpenForCommuteWhileEvaluatingCommute(this);
        }

        final GammaTxnConfig config = tx.config;

        //noinspection ObjectEquality
        if (config.stm != stm) {
            throw tx.abortCommuteOnBadStm(this);
        }

        if (config.isReadonly()) {
            throw tx.abortCommuteOnReadonly(this);
        }

        if (config.writeLockModeAsInt > LOCKMODE_NONE) {

        }

        final Tranlocal tranlocal = tx.tranlocal;

        //noinspection ObjectEquality
        if (tranlocal.owner == this) {
            if (tranlocal.isCommuting()) {
                tranlocal.addCommutingFunction(tx.pool, function);
                return;
            }

            if (tranlocal.isRead()) {
                tranlocal.mode = TRANLOCAL_WRITE;
                tx.hasWrites = true;
            }

            boolean abort = true;
            try {
                evaluate(tranlocal, tx, function);
                abort = false;
            } finally {
                if (abort) {
                    tx.abort();
                }
            }
            return;
        }

        if (tranlocal.owner != null) {
            throw tx.abortOnTransactionTooSmall(2);
        }

        tx.hasWrites = true;
        initTranlocalForCommute(config, tranlocal);
        tranlocal.addCommutingFunction(tx.pool, function);

        int writeLockMode = config.writeLockModeAsInt;
        if (writeLockMode > LOCKMODE_NONE) {
            flattenCommute(tx, tranlocal, writeLockMode);
        }
    }

    public final void openForCommute(final FatFixedLengthGammaTxn tx, final Function function) {
        if (tx == null) {
            throw new NullPointerException();
        }

        if (tx.status != TX_ACTIVE) {
            throw tx.abortCommuteOnBadStatus(this, function);
        }

        if (function == null) {
            throw tx.abortCommuteOnNullFunction(this);
        }

        if (tx.evaluatingCommute) {
            throw tx.abortOnOpenForCommuteWhileEvaluatingCommute(this);
        }

        final GammaTxnConfig config = tx.config;

        //noinspection ObjectEquality
        if (config.stm != stm) {
            throw tx.abortCommuteOnBadStm(this);
        }

        if (config.isReadonly()) {
            throw tx.abortCommuteOnReadonly(this);
        }
        Tranlocal found = null;
        Tranlocal newNode = null;
        Tranlocal node = tx.head;

        if (config.writeLockModeAsInt > LOCKMODE_NONE) {
            found = openForWrite(tx, config.writeLockModeAsInt);
        } else {
            while (true) {
                if (node == null) {
                    break;
                } else //noinspection ObjectEquality
                    if (node.owner == this) {
                        found = node;
                        break;
                    } else if (node.owner == null) {
                        newNode = node;
                        break;
                    } else {
                        node = node.next;
                    }
            }
        }

        if (found != null) {
            if (found.isCommuting()) {
                found.addCommutingFunction(tx.pool, function);
                return;
            }

            if (found.isRead()) {
                found.mode = TRANLOCAL_WRITE;
                tx.hasWrites = true;
            }

            boolean abort = true;
            try {
                evaluate(found, tx, function);
                abort = false;
            } finally {
                if (abort) {
                    tx.abort();
                }
            }
            return;
        }

        if (newNode == null) {
            throw tx.abortOnTransactionTooSmall(config.maxFixedLengthTransactionSize + 1);
        }

        tx.size++;
        tx.shiftInFront(newNode);
        tx.hasWrites = true;
        initTranlocalForCommute(config, newNode);
        newNode.addCommutingFunction(tx.pool, function);

        int writeLockMode = config.writeLockModeAsInt;
        if (writeLockMode > LOCKMODE_NONE) {
            flattenCommute(tx, newNode, writeLockMode);
        }
    }

    public final void openForCommute(final FatVariableLengthGammaTxn tx, final Function function) {
        if (tx == null) {
            throw new NullPointerException();
        }

        if (tx.status != TX_ACTIVE) {
            throw tx.abortCommuteOnBadStatus(this, function);
        }

        if (function == null) {
            throw tx.abortCommuteOnNullFunction(this);
        }

        if (tx.evaluatingCommute) {
            throw tx.abortOnOpenForCommuteWhileEvaluatingCommute(this);
        }

        final GammaTxnConfig config = tx.config;

        //noinspection ObjectEquality
        if (config.stm != stm) {
            throw tx.abortCommuteOnBadStm(this);
        }

        if (config.isReadonly()) {
            throw tx.abortCommuteOnReadonly(this);
        }

        final int identityHash = identityHashCode();
        final int indexOf = tx.indexOf(this, identityHash);

        if (indexOf > -1) {
            final Tranlocal tranlocal = tx.array[indexOf];

            if (tranlocal.isCommuting()) {
                tranlocal.addCommutingFunction(tx.pool, function);
                return;
            }

            if (tranlocal.isRead()) {
                tranlocal.mode = TRANLOCAL_WRITE;
                tx.hasWrites = true;
            }

            boolean abort = true;
            try {
                evaluate(tranlocal, tx, function);
                abort = false;
            } finally {
                if (abort) {
                    tx.abort();
                }
            }
            return;
        }

        final Tranlocal tranlocal = tx.pool.take(this);
        initTranlocalForCommute(config, tranlocal);
        tx.hasWrites = true;
        tx.attach(tranlocal, identityHash);
        tx.size++;
        tranlocal.addCommutingFunction(tx.pool, function);

        int writeLockMode = config.writeLockModeAsInt;
        if (writeLockMode > LOCKMODE_NONE) {
            flattenCommute(tx, tranlocal, writeLockMode);
        }
    }

    public final void ensure() {
        ensure(getRequiredThreadLocalGammaTxn());
    }

    public final void ensure(final Txn self) {
        ensure(asGammaTxn(self));
    }

    public final void ensure(final GammaTxn tx) {
        if (tx == null) {
            throw new NullPointerException();
        }

        if (tx.status != TX_ACTIVE) {
            throw tx.abortEnsureOnBadStatus(this);
        }

        if (tx.isLean()) {
            throw tx.abortEnsureOnEnsureDetected(this);
        }

        if (tx.config.readonly) {
            return;
        }

        final Tranlocal tranlocal = openForRead(tx, LOCKMODE_NONE);
        tranlocal.writeSkewCheck = true;
    }

    protected final long getLong(final GammaTxn tx, final LockMode lockMode) {
        assert type != TYPE_REF;

        if (tx == null) {
            throw new NullPointerException();
        }

        if (tx.status != TX_ACTIVE) {
            throw tx.abortOpenForReadOnBadStatus(this);
        }

        if (lockMode == null) {
            throw tx.abortOpenForReadOnNullLockMode(this);
        }

        return openForRead(tx, lockMode.asInt()).long_value;
    }

    protected final Object getObject(final GammaTxn tx, final LockMode lockMode) {
        assert type == TYPE_REF;

        if (tx == null) {
            throw new NullPointerException();
        }

        if (tx.status != TX_ACTIVE) {
            throw tx.abortOpenForReadOnBadStatus(this);
        }

        if (lockMode == null) {
            throw tx.abortOpenForReadOnNullLockMode(this);
        }

        return openForRead(tx, lockMode.asInt()).ref_value;
    }

    protected final long setLong(final GammaTxn tx, final LockMode lockMode, final long newValue, final boolean returnOld) {
        assert type != TYPE_REF;

        if (tx == null) {
            throw new NullPointerException();
        }

        if (tx.status != TX_ACTIVE) {
            throw tx.abortOpenForReadOnBadStatus(this);
        }

        if (lockMode == null) {
            throw tx.abortOpenForReadOnNullLockMode(this);
        }

        final Tranlocal tranlocal = openForWrite(tx, lockMode.asInt());
        final long oldValue = tranlocal.long_value;
        tranlocal.long_value = newValue;
        return returnOld ? oldValue : newValue;
    }

    protected final Object setObject(final GammaTxn tx, final LockMode lockMode, final Object newValue, final boolean returnOld) {
        assert type == TYPE_REF;

        if (tx == null) {
            throw new NullPointerException();
        }

        if (tx.status != TX_ACTIVE) {
            throw tx.abortOpenForReadOnBadStatus(this);
        }

        if (lockMode == null) {
            throw tx.abortOpenForReadOnNullLockMode(this);
        }

        final Tranlocal tranlocal = openForWrite(tx, lockMode.asInt());
        final Object oldValue = tranlocal.ref_value;
        tranlocal.ref_value = newValue;
        return returnOld ? oldValue : newValue;
    }

    public final long atomicGetLong() {
        assert type != TYPE_REF;

        int attempt = 1;
        do {
            if (!hasExclusiveLock()) {
                long read = long_value;

                if (!hasExclusiveLock()) {
                    return read;
                }
            }
            stm.defaultBackoffPolicy.delayUninterruptible(attempt);
            attempt++;
        } while (attempt <= stm.spinCount);

        throw new LockedException();
    }

    public final Object atomicObjectGet() {
        assert type == TYPE_REF;

        int attempt = 1;
        do {
            if (!hasExclusiveLock()) {
                Object read = ref_value;
                if (!hasExclusiveLock()) {
                    return read;
                }
            }
            stm.defaultBackoffPolicy.delayUninterruptible(attempt);
            attempt++;
        } while (attempt <= stm.spinCount);

        throw new LockedException();
    }

    public final long atomicSetLong(final long newValue, boolean returnOld) {
        assert type != TYPE_REF;

        final int arriveStatus = arriveAndExclusiveLockOrBackoff();

        if (arriveStatus == FAILURE) {
            throw new LockedException();
        }

        final long oldValue = long_value;

        if (oldValue == newValue) {
            if ((arriveStatus & MASK_UNREGISTERED) != 0) {
                unlockByUnregistered();
            } else {
                departAfterReadingAndUnlock();
            }

            return newValue;
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
            final GammaObjectPool pool = getThreadLocalGammaObjectPool();
            listeners.openAll(pool);
        }

        return returnOld ? oldValue : newValue;
    }

    public final Object atomicSetObject(final Object newValue, boolean returnOld) {
        assert type == TYPE_REF;

        final int arriveStatus = arriveAndExclusiveLockOrBackoff();

        if (arriveStatus == FAILURE) {
            throw new LockedException();
        }

        final Object oldValue = ref_value;

        if (oldValue == newValue) {
            if ((arriveStatus & MASK_UNREGISTERED) != 0) {
                unlockByUnregistered();
            } else {
                departAfterReadingAndUnlock();
            }

            return newValue;
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
            final GammaObjectPool pool = getThreadLocalGammaObjectPool();
            listeners.openAll(pool);
        }

        return returnOld ? oldValue : newValue;
    }

    public final boolean atomicCompareAndSetLong(final long expectedValue, final long newValue) {
        final int arriveStatus = arriveAndExclusiveLockOrBackoff();

        if (arriveStatus == FAILURE) {
            throw new LockedException();
        }

        final long currentValue = long_value;

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

        long_value = newValue;
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
    public final void acquire(final LockMode desiredLockMode) {
        final GammaTxn tx = (GammaTxn) getThreadLocalTxn();

        if (tx == null) {
            throw new TxnMandatoryException();
        }

        acquire(tx, desiredLockMode);
    }

    @Override
    public final void acquire(final Txn tx, final LockMode desiredLockMode) {
        acquire((GammaTxn) tx, desiredLockMode);
    }

    public final void acquire(final GammaTxn tx, final LockMode lockMode) {
        if (tx == null) {
            throw new NullPointerException();
        }

        if (lockMode == null) {
            throw tx.abortAcquireOnNullLockMode(this);
        }

        openForRead(tx, lockMode.asInt());
    }

    /**
     * Tries to acquire a lock on a previous read/written tranlocal and checks for conflict.
     * <p/>
     * If the lockMode == LOCKMODE_NONE, this call is ignored.
     * <p/>
     * The call to this method can safely made if the current lock level is higher the the desired LockMode.
     * <p/>
     * If the can't be acquired, no changes are made on the tranlocal.
     *
     * @param tx
     * @param tranlocal       the tranlocal
     * @param spinCount       the maximum number of times to spin
     * @param desiredLockMode
     * @return true if the lock was acquired successfully and there was no conflict.
     */
    public final boolean tryLockAndCheckConflict(
            final GammaTxn tx,
            final Tranlocal tranlocal,
            final int spinCount,
            final int desiredLockMode) {

        final int currentLockMode = tranlocal.getLockMode();

        //if the currentLockMode mode is higher or equal than the desired lockmode, we are done.
        if (currentLockMode >= desiredLockMode) {
            return true;
        }

        //no lock currently is acquired, lets acquire it.
        if (currentLockMode == LOCKMODE_NONE) {
            final long expectedVersion = tranlocal.version;

            //if the version already is different, there is a conflict, we are done since since the lock doesn't need to be acquired.
            if (expectedVersion != version) {
                return false;
            }

            if (tranlocal.hasDepartObligation()) {
                int result = lockAfterArrive(spinCount, desiredLockMode);
                if (result == FAILURE) {
                    return false;
                }

                if ((result & MASK_CONFLICT) != 0) {
                    tx.commitConflict = true;
                }

                if (version != expectedVersion) {
                    tranlocal.setDepartObligation(false);
                    departAfterFailureAndUnlock();
                    return false;
                }
            } else {
                //we need to arrive as well because the the tranlocal was readbiased, and no real arrive was done.
                final int result = arriveAndLock(spinCount, desiredLockMode);

                if (result == FAILURE) {
                    return false;
                }

                tranlocal.setLockMode(desiredLockMode);

                if ((result & MASK_UNREGISTERED) == 0) {
                    tranlocal.hasDepartObligation = true;
                }

                if ((result & MASK_CONFLICT) != 0) {
                    tx.commitConflict = true;
                }

                if (version != expectedVersion) {
                    return false;
                }
            }

            tranlocal.setLockMode(desiredLockMode);
            return true;
        }

        //if a readlock is acquired, we need to upgrade it to a write/exclusive-lock
        if (currentLockMode == LOCKMODE_READ) {
            int result = upgradeReadLock(spinCount, desiredLockMode == LOCKMODE_EXCLUSIVE);

            if (result == FAILURE) {
                return false;
            }

            if ((result & MASK_CONFLICT) != 0) {
                tx.commitConflict = true;
            }

            tranlocal.setLockMode(desiredLockMode);
            return true;
        }

        //so we have the write lock, its needs to be upgraded to a commit lock.
        if (upgradeWriteLock()) {
            tx.commitConflict = true;
        }

        tranlocal.setLockMode(LOCKMODE_EXCLUSIVE);
        return true;
    }

    public final int registerChangeListener(
            final RetryLatch latch,
            final Tranlocal tranlocal,
            final GammaObjectPool pool,
            final long listenerEra) {

        if (tranlocal.isCommuting() || tranlocal.isConstructing()) {
            return REGISTRATION_NONE;
        }

        final long version = tranlocal.version;

        if (version != this.version) {
            //if it currently already contains a different version, we are done.
            latch.open(listenerEra);
            return REGISTRATION_NOT_NEEDED;
        }

        //we are going to register the listener since the current value still matches with is active.
        //But it could be that the registration completes after the write has happened.

        Listeners update = pool.takeListeners();
        //update.threadName = Thread.currentThread().getName();
        update.listener = latch;
        update.listenerEra = listenerEra;

        //we need to do this in a loop because other register thread could be contending for the same
        //listeners field.
        while (true) {
            if (version != this.version) {
                //if it currently already contains a different version, we are done.
                latch.open(listenerEra);
                return REGISTRATION_NOT_NEEDED;
            }

            //the listeners object is mutable, but as long as it isn't yet registered, this calling
            //thread has full ownership of it.
            final Listeners current = listeners;
            update.next = current;

            //lets try to register our listeners.
            final boolean registered = ___unsafe.compareAndSwapObject(this, listenersOffset, current, update);
            if (!registered) {
                //so we are contending with another register thread, so lets try it again. Since the compareAndSwap
                //didn't succeed, we know that the current thread still has exclusive ownership on the Listeners object
                //so we can try to register it again, but now with the newly found listeners
                continue;
            }

            //the registration was a success. We need to make sure that the ___version hasn't changed.
            //JMM: the volatile read of ___version can't jump in front of the unsafe.compareAndSwap.
            if (version == this.version) {
                //we are lucky, the registration was done successfully and we managed to cas the listener
                //before the update (since the update we are interested in, hasn't happened yet). This means that
                //the updating thread is now responsible for notifying the listeners. Retrieval of the most recently
                //published listener, always happens after the version is updated
                return REGISTRATION_DONE;
            }

            //the version has changed, so an interesting write has happened. No registration is needed.
            //JMM: the unsafe.compareAndSwap can't jump over the volatile read this.___version.
            //the update has taken place, we need to check if our listeners still is in place.
            //if it is, it should be removed and the listeners notified. If the listeners already has changed,
            //it is the task for the other to do the listener cleanup and notify them
            while (true) {
                update = listeners;
                final boolean removed = ___unsafe.compareAndSwapObject(this, listenersOffset, update, null);

                if (!removed) {
                    continue;
                }

                if (update != null) {
                    //we have complete ownership of the listeners that are removed, so lets open them.
                    update.openAll(pool);
                }
                return REGISTRATION_NOT_NEEDED;
            }
        }
    }


    @SuppressWarnings({"SimplifiableIfStatement"})
    public final boolean hasReadConflict(final Tranlocal tranlocal) {
        if (tranlocal.lockMode != LOCKMODE_NONE) {
            return false;
        }

        if (hasExclusiveLock()) {
            return true;
        }

        return tranlocal.version != version;
    }

    protected final int arriveAndExclusiveLockOrBackoff() {
        final int maxRetries = stm.defaultMaxRetries;
        final int spinCount = stm.spinCount;

        for (int k = 0; k <= maxRetries; k++) {
            final int arriveStatus = arriveAndExclusiveLock(spinCount);

            if (arriveStatus != FAILURE) {
                return arriveStatus;
            }

            stm.defaultBackoffPolicy.delayUninterruptible(k + 1);
        }

        return FAILURE;
    }
}
