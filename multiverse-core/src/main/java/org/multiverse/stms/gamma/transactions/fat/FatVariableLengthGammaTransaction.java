package org.multiverse.stms.gamma.transactions.fat;

import org.multiverse.api.lifecycle.TransactionEvent;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.Listeners;
import org.multiverse.stms.gamma.transactionalobjects.BaseGammaRef;
import org.multiverse.stms.gamma.transactionalobjects.GammaObject;
import org.multiverse.stms.gamma.transactionalobjects.GammaRefTranlocal;
import org.multiverse.stms.gamma.transactions.GammaTransaction;
import org.multiverse.stms.gamma.transactions.GammaTxnConfiguration;
import org.multiverse.stms.gamma.transactions.SpeculativeGammaConfiguration;

import static org.multiverse.utils.Bugshaker.shakeBugs;

@SuppressWarnings({"OverlyComplexClass"})
public final class FatVariableLengthGammaTransaction extends GammaTransaction {

    public GammaRefTranlocal[] array;
    public int size = 0;
    public boolean hasReads = false;
    public long localConflictCount;

    public FatVariableLengthGammaTransaction(GammaStm stm) {
        this(new GammaTxnConfiguration(stm));
    }

    public FatVariableLengthGammaTransaction(GammaTxnConfiguration config) {
        super(config, TRANSACTIONTYPE_FAT_VARIABLE_LENGTH);
        this.array = new GammaRefTranlocal[config.minimalArrayTreeSize];
    }

    @Override
    public final void commit() {
        if (status == TX_COMMITTED) {
            return;
        }

        if (status != TX_ACTIVE && status != TX_PREPARED) {
            throw abortCommitOnBadStatus();
        }

        if (abortOnly) {
            throw abortCommitOnAbortOnly();
        }

        if (status == TX_ACTIVE) {
            notifyListeners(TransactionEvent.PrePrepare);
        }

        if (size > 0) {
            if (hasWrites) {
                if (status == TX_ACTIVE) {
                    GammaObject conflictingObject = doPrepare();
                    if (conflictingObject != null) {
                        throw abortOnReadWriteConflict(conflictingObject);
                    }
                }

                if (commitConflict) {
                    config.globalConflictCounter.signalConflict();
                }

                Listeners[] listenersArray = commitArray();

                if (listenersArray != null) {
                    Listeners.openAll(listenersArray, pool);
                    pool.putListenersArray(listenersArray);
                }
            } else {
                releaseArray(true);
            }
        }

        status = TX_COMMITTED;
        notifyListeners(TransactionEvent.PostCommit);
    }

    private Listeners[] commitArray() {
        Listeners[] listenersArray = null;

        int listenersIndex = 0;
        int itemCount = 0;
        //first write everything without releasing
        for (int k = 0; k < array.length; k++) {
            if (SHAKE_BUGS) shakeBugs();

            final GammaRefTranlocal tranlocal = array[k];

            if (tranlocal == null) {
                continue;
            }

            final BaseGammaRef owner = tranlocal.owner;
            final Listeners listeners = owner.commit(tranlocal, pool);

            if (listeners != null) {
                if (listenersArray == null) {
                    listenersArray = pool.takeListenersArray(size - itemCount);
                }

                listenersArray[listenersIndex] = listeners;
                listenersIndex++;
            }
            pool.put(tranlocal);
            itemCount++;
        }
        return listenersArray;
    }

    private void releaseArray(boolean success) {
        for (int k = 0; k < array.length; k++) {

            final GammaRefTranlocal tranlocal = array[k];

            if (tranlocal != null) {
                if (SHAKE_BUGS) shakeBugs();

                array[k] = null;
                if (success) {
                    tranlocal.owner.releaseAfterReading(tranlocal, pool);
                } else {
                    tranlocal.owner.releaseAfterFailure(tranlocal, pool);
                }
                pool.put(tranlocal);
            }
        }
    }

    @Override
    public final void prepare() {
        if (status == TX_PREPARED) {
            return;
        }

        if (status != TX_ACTIVE) {
            throw abortPrepareOnBadStatus();
        }

        if (abortOnly) {
            throw abortPrepareOnAbortOnly();
        }

        notifyListeners(TransactionEvent.PrePrepare);

        if (hasWrites) {
            final GammaObject conflictingObject = doPrepare();
            if (conflictingObject != null) {
                throw abortOnReadWriteConflict(conflictingObject);
            }
        }

        status = TX_PREPARED;
    }

    @SuppressWarnings({"BooleanMethodIsAlwaysInverted"})
    private GammaObject doPrepare() {
        if (skipPrepare()) {
            return null;
        }

        for (int k = 0; k < array.length; k++) {
            if (SHAKE_BUGS) shakeBugs();

            final GammaRefTranlocal tranlocal = array[k];

            if (tranlocal == null) {
                continue;
            }

            final BaseGammaRef owner = tranlocal.owner;

            if (!owner.prepare(this, tranlocal)) {
                return owner;
            }
        }

        return null;
    }

    @Override
    public final void abort() {
        if (status == TX_ABORTED) {
            return;
        }

        if (status == TX_COMMITTED) {
            throw failAbortOnAlreadyCommitted();
        }

        if (size > 0) {
            releaseArray(false);
        }

        status = TX_ABORTED;

        notifyListeners(TransactionEvent.PostAbort);
    }

    @Override
    public final GammaRefTranlocal locate(BaseGammaRef o) {
        if (status != TX_ACTIVE) {
            throw abortLocateOnBadStatus(o);
        }

        if (o == null) {
            throw abortLocateOnNullArgument();
        }

        return getRefTranlocal(o);
    }

    @Override
    public final GammaRefTranlocal getRefTranlocal(BaseGammaRef ref) {
        int indexOf = indexOf(ref, ref.identityHashCode());
        return indexOf == -1 ? null : array[indexOf];
    }

    @Override
    public final void retry() {
        if (status != TX_ACTIVE) {
            throw abortRetryOnBadStatus();
        }

        if (!config.isBlockingAllowed()) {
            throw abortRetryOnNoBlockingAllowed();
        }

        if (size == 0) {
            throw abortRetryOnNoRetryPossible();
        }

        retryListener.reset();
        final long listenerEra = retryListener.getEra();

        boolean furtherRegistrationNeeded = true;
        boolean atLeastOneRegistration = false;

        for (int k = 0; k < array.length; k++) {
            final GammaRefTranlocal tranlocal = array[k];
            if (tranlocal == null) {
                continue;
            }

            array[k] = null;

            final BaseGammaRef owner = tranlocal.owner;

            if (furtherRegistrationNeeded) {
                switch (owner.registerChangeListener(retryListener, tranlocal, pool, listenerEra)) {
                    case REGISTRATION_DONE:
                        atLeastOneRegistration = true;
                        break;
                    case REGISTRATION_NOT_NEEDED:
                        furtherRegistrationNeeded = false;
                        atLeastOneRegistration = true;
                        break;
                    case REGISTRATION_NONE:
                        break;
                    default:
                        throw new IllegalStateException();
                }
            }

            owner.releaseAfterFailure(tranlocal, pool);
            pool.put(tranlocal);
        }

        status = TX_ABORTED;

        if (!atLeastOneRegistration) {
            throw abortRetryOnNoRetryPossible();
        }

        throw newRetryError();
    }

    @Override
    public final boolean softReset() {
        if (attempt >= config.getMaxRetries()) {
            return false;
        }

        status = TX_ACTIVE;
        hasReads = false;
        hasWrites = false;
        size = 0;
        abortOnly = false;
        attempt++;
        commitConflict = false;
        evaluatingCommute = false;
        if (listeners != null) {
            listeners.clear();
            pool.putArrayList(listeners);
            listeners = null;
        }

        return true;
    }

    @Override
    public final void hardReset() {
        status = TX_ACTIVE;
        hasReads = false;
        hasWrites = false;
        size = 0;
        abortOnly = false;

        attempt = 1;
        remainingTimeoutNs = config.timeoutNs;
        //todo: only change when the array size is different.
        if (array != null) {
            pool.putTranlocalArray(array);
        }
        array = pool.takeTranlocalArray(config.minimalArrayTreeSize);
        final SpeculativeGammaConfiguration speculativeConfig = config.speculativeConfiguration.get();
        richmansMansConflictScan = speculativeConfig.richMansConflictScanRequired;
        commitConflict = false;
        evaluatingCommute = false;
        if (listeners != null) {
            listeners.clear();
            pool.putArrayList(listeners);
            listeners = null;
        }

    }

    @Override
    public void initLocalConflictCounter() {
        if (richmansMansConflictScan && !hasReads) {
            localConflictCount = config.globalConflictCounter.count();
        }
    }

    @Override
    public final boolean isReadConsistent(GammaRefTranlocal justAdded) {
        if (!hasReads) {
            return true;
        }

        if (config.readLockModeAsInt > LOCKMODE_NONE) {
            return true;
        }

        if (config.inconsistentReadAllowed) {
            return true;
        }

        if (richmansMansConflictScan) {
            if (SHAKE_BUGS) shakeBugs();

            final long conflictCount = config.globalConflictCounter.count();

            if (localConflictCount == conflictCount) {
                return true;
            }

            localConflictCount = conflictCount;
            //we are going to fall through to do a full conflict scan
        } else if (size > config.maximumPoorMansConflictScanLength) {
            throw abortOnRichmanConflictScanDetected();
        }

        //doing a full conflict scan
        for (int k = 0; k < array.length; k++) {
            if (SHAKE_BUGS) shakeBugs();

            final GammaRefTranlocal tranlocal = array[k];

            //noinspection ObjectEquality
            final boolean skip = tranlocal == null || (!richmansMansConflictScan && justAdded == tranlocal);

            if (!skip && tranlocal.owner.hasReadConflict(tranlocal)) {
                return false;
            }
        }

        return true;
    }

    public final float getUsage() {
        return (size * 1.0f) / array.length;
    }

    public final int size() {
        return size;
    }

    public final int indexOf(final BaseGammaRef ref, final int hash) {
        int jump = 0;
        boolean goLeft = true;

        do {
            final int offset = goLeft ? -jump : jump;
            final int index = (hash + offset) % array.length;

            final GammaRefTranlocal current = array[index];
            if (current == null || current.owner == null) {
                return -1;
            }

            //noinspection ObjectEquality
            if (current.owner == ref) {
                return index;
            }

            final int currentHash = current.owner.identityHashCode();
            goLeft = currentHash > hash;
            jump = jump == 0 ? 1 : jump * 2;
        } while (jump < array.length);

        return -1;
    }

    public final void attach(final GammaRefTranlocal tranlocal, final int hash) {
        int jump = 0;
        boolean goLeft = true;

        do {
            final int offset = goLeft ? -jump : jump;
            final int index = (hash + offset) % array.length;

            GammaRefTranlocal current = array[index];
            if (current == null) {
                array[index] = tranlocal;
                return;
            }

            final int currentHash = current.owner.identityHashCode();
            goLeft = currentHash > hash;
            jump = jump == 0 ? 1 : jump * 2;
        } while (jump < array.length);

        expand();
        attach(tranlocal, hash);
    }

    private void expand() {
        GammaRefTranlocal[] oldArray = array;
        int newSize = oldArray.length * 2;
        array = pool.takeTranlocalArray(newSize);

        for (int k = 0; k < oldArray.length; k++) {
            final GammaRefTranlocal tranlocal = oldArray[k];

            if (tranlocal == null) {
                continue;
            }

            oldArray[k] = null;
            attach(tranlocal, tranlocal.owner.identityHashCode());
        }

        pool.putTranlocalArray(oldArray);
    }
}
