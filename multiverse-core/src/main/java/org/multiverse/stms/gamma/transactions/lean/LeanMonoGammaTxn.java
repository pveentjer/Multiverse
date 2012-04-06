package org.multiverse.stms.gamma.transactions.lean;

import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.Listeners;
import org.multiverse.stms.gamma.transactionalobjects.BaseGammaTxnRef;
import org.multiverse.stms.gamma.transactionalobjects.Tranlocal;
import org.multiverse.stms.gamma.transactions.GammaTxn;
import org.multiverse.stms.gamma.transactions.GammaTxnConfig;

import static org.multiverse.utils.Bugshaker.shakeBugs;


/**
 * A Lean GammaTxn implementation that is optimized for dealing with only a single
 * transactional reference.
 */
public final class LeanMonoGammaTxn extends GammaTxn {

    public final Tranlocal tranlocal = new Tranlocal();

    public LeanMonoGammaTxn(GammaStm stm) {
        this(new GammaTxnConfig(stm));
    }

    public LeanMonoGammaTxn(GammaTxnConfig config) {
        super(config, TRANSACTIONTYPE_LEAN_MONO);
    }

    @Override
    public final Tranlocal locate(BaseGammaTxnRef o) {
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

        final BaseGammaTxnRef owner = tranlocal.owner;

        if (owner == null) {
            status = TX_COMMITTED;
            return;
        }

        if (!hasWrites) {
            tranlocal.owner = null;
            tranlocal.ref_value = null;
            status = TX_COMMITTED;
            return;
        }

        final long version = tranlocal.version;

        //if the transaction still is active, we need to prepare the transaction.
        if (status == TX_ACTIVE) {
            if (owner.version != version) {
                throw abortOnReadWriteConflict(owner);
            }

            final int arriveStatus = owner.arriveAndExclusiveLock(64);

            if (arriveStatus == FAILURE) {
                throw abortOnReadWriteConflict(owner);
            }

            if (owner.version != version) {
                if ((arriveStatus & MASK_UNREGISTERED) == 0) {
                    owner.departAfterFailureAndUnlock();
                } else {
                    owner.unlockByUnregistered();
                }
                throw abortOnReadWriteConflict(owner);
            }

            if((arriveStatus & MASK_CONFLICT)!=0){
                commitConflict = true;
            }
        }

        if (commitConflict) {
            config.globalConflictCounter.signalConflict();
        }

        if(SHAKE_BUGS) shakeBugs();
        owner.ref_value = tranlocal.ref_value;
        owner.version = version + 1;

        Listeners listeners = owner.listeners;

        if (listeners != null) {
            listeners = owner.___removeListenersAfterWrite();
        }

        owner.departAfterUpdateAndUnlock();

        tranlocal.owner = null;
        //we need to set them to null to prevent memory leaks.
        tranlocal.ref_value = null;
        tranlocal.ref_oldValue = null;

        if (listeners != null) {
            listeners.openAll(pool);
        }

        status = TX_COMMITTED;
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
        BaseGammaTxnRef owner = tranlocal.owner;
        if (owner != null) {
            owner.releaseAfterFailure(tranlocal, pool);
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

        final BaseGammaTxnRef owner = tranlocal.owner;
        if (owner != null) {
            if (!owner.prepare(this, tranlocal)) {
                throw abortOnReadWriteConflict(owner);
            }
        }

        status = TX_PREPARED;
    }

    @Override
    public final Tranlocal getRefTranlocal(BaseGammaTxnRef ref) {
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

        final BaseGammaTxnRef owner = tranlocal.owner;
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

        commitConflict = false;
        status = TX_ACTIVE;
        hasWrites = false;
        attempt++;
        return true;
    }

    @Override
    public final void hardReset() {
        commitConflict = false;
        status = TX_ACTIVE;
        hasWrites = false;
        remainingTimeoutNs = config.timeoutNs;
        attempt = 1;
    }

    @Override
    public final boolean isReadConsistent(Tranlocal justAdded) {
        return true;
    }

    @Override
    public void initLocalConflictCounter() {
        //ignore
    }
}
