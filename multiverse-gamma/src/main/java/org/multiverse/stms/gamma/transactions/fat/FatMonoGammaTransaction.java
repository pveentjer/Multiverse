package org.multiverse.stms.gamma.transactions.fat;

import org.multiverse.api.lifecycle.TransactionEvent;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.Listeners;
import org.multiverse.stms.gamma.transactionalobjects.BaseGammaRef;
import org.multiverse.stms.gamma.transactionalobjects.GammaRefTranlocal;
import org.multiverse.stms.gamma.transactions.GammaTransaction;
import org.multiverse.stms.gamma.transactions.GammaTransactionConfiguration;

public final class FatMonoGammaTransaction extends GammaTransaction {

    public final GammaRefTranlocal tranlocal = new GammaRefTranlocal();

    public FatMonoGammaTransaction(GammaStm stm) {
        this(new GammaTransactionConfiguration(stm));
    }

    public FatMonoGammaTransaction(GammaTransactionConfiguration config) {
        super(config, TRANSACTIONTYPE_FAT_MONO);
        richmansMansConflictScan = false;
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

        final BaseGammaRef owner = tranlocal.owner;

        if (owner != null) {
            if (hasWrites) {
                if (status == TX_ACTIVE) {
                    if (!skipPrepare()) {
                        if (!owner.prepare(this, tranlocal)) {
                            throw abortOnReadWriteConflict(owner);
                        }
                    }
                }

                if (commitConflict) {
                    config.globalConflictCounter.signalConflict();
                }

                Listeners listeners = owner.commit(tranlocal, pool);
                if (listeners != null) {
                    listeners.openAll(pool);
                }
            } else {
                owner.releaseAfterReading(tranlocal, pool);
            }
        }

        tranlocal.owner = null;
        status = TX_COMMITTED;
        notifyListeners(TransactionEvent.PostCommit);
    }

    @Override
    public final void abort() {
        if (status == TX_ABORTED) {
            return;
        }

        if (status == TX_COMMITTED) {
            throw failAbortOnAlreadyCommitted();
        }

        status = TX_ABORTED;
        BaseGammaRef owner = tranlocal.owner;
        if (owner != null) {
            owner.releaseAfterFailure(tranlocal, pool);
        }

        notifyListeners(TransactionEvent.PostAbort);
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

        final BaseGammaRef owner = tranlocal.owner;
        if (owner != null) {
            if (!owner.prepare(this, tranlocal)) {
                throw abortOnReadWriteConflict(owner);
            }
        }

        status = TX_PREPARED;
    }

    @Override
    public final GammaRefTranlocal getRefTranlocal(BaseGammaRef ref) {
        //noinspection ObjectEquality
        return tranlocal.owner == ref ? tranlocal : null;
    }

    @Override
    public final void retry() {
        if (status != TX_ACTIVE) {
            throw abortRetryOnBadStatus();
        }

        if (!config.isBlockingAllowed()) {
            throw abortRetryOnNoBlockingAllowed();
        }

        if (tranlocal == null) {
            throw abortRetryOnNoRetryPossible();
        }

        final BaseGammaRef owner = tranlocal.owner;
        if (owner == null) {
            throw abortRetryOnNoRetryPossible();
        }

        retryListener.reset();
        final long listenerEra = retryListener.getEra();

        boolean atLeastOneRegistration = false;
        switch (tranlocal.owner.registerChangeListener(retryListener, tranlocal, pool, listenerEra)) {
            case REGISTRATION_DONE:
                atLeastOneRegistration = true;
                break;
            case REGISTRATION_NOT_NEEDED:
                atLeastOneRegistration = true;
                break;
            case REGISTRATION_NONE:
                break;
            default:
                throw new IllegalStateException();
        }

        owner.releaseAfterFailure(tranlocal, pool);

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

        if (listeners != null) {
            listeners.clear();
            pool.putArrayList(listeners);
            listeners = null;
        }

        status = TX_ACTIVE;
        hasWrites = false;
        attempt++;
        abortOnly = false;
        commitConflict = false;
        evaluatingCommute = false;
        return true;
    }

    @Override
    public final void hardReset() {
        if (listeners != null) {
            listeners.clear();
            pool.putArrayList(listeners);
            listeners = null;
        }

        status = TX_ACTIVE;
        hasWrites = false;
        remainingTimeoutNs = config.timeoutNs;
        attempt = 1;
        abortOnly = false;
        commitConflict = false;
        evaluatingCommute = false;
    }

    @Override
    public final boolean isReadConsistent(GammaRefTranlocal justAdded) {
        return true;
    }

    @Override
    public void initLocalConflictCounter() {
        //ignore
    }
}
