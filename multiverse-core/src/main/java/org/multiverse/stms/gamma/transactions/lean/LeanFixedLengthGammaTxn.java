package org.multiverse.stms.gamma.transactions.lean;

import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.Listeners;
import org.multiverse.stms.gamma.transactionalobjects.BaseGammaRef;
import org.multiverse.stms.gamma.transactionalobjects.GammaObject;
import org.multiverse.stms.gamma.transactionalobjects.GammaRefTranlocal;
import org.multiverse.stms.gamma.transactions.GammaTxn;
import org.multiverse.stms.gamma.transactions.GammaTxnConfiguration;

import static org.multiverse.utils.Bugshaker.shakeBugs;


/**
 * A Lean GammaTxn that is optimized for a fixed number of GammaRefs.
 */
public final class LeanFixedLengthGammaTxn extends GammaTxn {

    public GammaRefTranlocal head;
    public int size = 0;
    public boolean hasReads = false;
    public final Listeners[] listenersArray;

    public LeanFixedLengthGammaTxn(final GammaStm stm) {
        this(new GammaTxnConfiguration(stm));
    }

    @SuppressWarnings({"ObjectAllocationInLoop"})
    public LeanFixedLengthGammaTxn(final GammaTxnConfiguration config) {
        super(config, TRANSACTIONTYPE_LEAN_FIXED_LENGTH);

        listenersArray = new Listeners[config.maxFixedLengthTransactionSize];

        GammaRefTranlocal h = null;
        for (int k = 0; k < config.maxFixedLengthTransactionSize; k++) {
            GammaRefTranlocal newNode = new GammaRefTranlocal();
            if (h != null) {
                h.previous = newNode;
                newNode.next = h;
            }

            h = newNode;
        }
        head = h;
    }

    @Override
    public final boolean isReadConsistent(GammaRefTranlocal justAdded) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final void commit() {
        int s = status;

        if (s == TX_COMMITTED) {
            return;
        }

        if (s != TX_ACTIVE && s != TX_PREPARED) {
            throw abortCommitOnBadStatus();
        }

        if (hasWrites) {
            if (s == TX_ACTIVE) {
                GammaObject conflictingObject = prepareChainForCommit();
                if (conflictingObject != null) {
                    throw abortOnReadWriteConflict(conflictingObject);
                }
            }

            if (commitConflict) {
                config.globalConflictCounter.signalConflict();
            }

            int listenersIndex = 0;
            GammaRefTranlocal node = head;
            do {
                final BaseGammaRef owner = node.owner;

                if (owner == null) {
                    break;
                }
                if (SHAKE_BUGS) shakeBugs();

                final Listeners listeners = owner.leanCommit(node);
                if (listeners != null) {
                    listenersArray[listenersIndex] = listeners;
                    listenersIndex++;
                }
                node = node.next;
            } while (node != null);

            if (listenersArray != null) {
                Listeners.openAll(listenersArray, pool);
            }
        } else {
            releaseReadonlyChain();
        }

        status = TX_COMMITTED;
    }

    @Override
    public final void prepare() {
        if (status == TX_PREPARED) {
            return;
        }

        if (status != TX_ACTIVE) {
            throw abortPrepareOnBadStatus();
        }

        final GammaObject conflictingObject = prepareChainForCommit();
        if (conflictingObject != null) {
            throw abortOnReadWriteConflict(conflictingObject);
        }

        status = TX_PREPARED;
    }

    @SuppressWarnings({"BooleanMethodIsAlwaysInverted"})
    private GammaObject prepareChainForCommit() {
        GammaRefTranlocal node = head;
        do {
            final BaseGammaRef owner = node.owner;

            if (owner == null) {
                return null;
            }

            if (SHAKE_BUGS) shakeBugs();

            if (node.mode == TRANLOCAL_READ) {
                continue;
            }

            final long version = node.version;
            if (owner.version != version) {
                return owner;
            }

            int arriveStatus = owner.arriveAndExclusiveLock(64);

            if (arriveStatus == FAILURE) {
                return owner;
            }

            if ((arriveStatus & MASK_CONFLICT) != 0) {
                commitConflict = true;
            }

            node.hasDepartObligation = (arriveStatus & MASK_UNREGISTERED) == 0;
            node.lockMode = LOCKMODE_EXCLUSIVE;

            if (owner.version != version) {
                return owner;
            }

            node = node.next;
        } while (node != null);

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

        releaseChainForAbort();
        status = TX_ABORTED;
    }

    private void releaseChainForAbort() {
        GammaRefTranlocal node = head;
        do {
            final BaseGammaRef owner = node.owner;

            if (owner == null) {
                return;
            }

            if (SHAKE_BUGS) shakeBugs();

            if (node.isWrite()) {
                if (node.getLockMode() == LOCKMODE_EXCLUSIVE) {
                    if (node.hasDepartObligation()) {
                        node.setDepartObligation(false);
                        owner.departAfterFailureAndUnlock();
                    } else {
                        owner.unlockByUnregistered();
                    }
                    node.setLockMode(LOCKMODE_NONE);
                }
            }

            node.owner = null;
            node.ref_oldValue = null;
            node.ref_value = null;
            node = node.next;
        } while (node != null);
    }

    private void releaseReadonlyChain() {
        GammaRefTranlocal node = head;
        do {
            final BaseGammaRef owner = node.owner;

            if (owner == null) {
                return;
            }

            if (SHAKE_BUGS) shakeBugs();

            node.owner = null;
            node.ref_oldValue = null;
            node.ref_value = null;
            node = node.next;
        } while (node != null);
    }

    @Override
    public final GammaRefTranlocal getRefTranlocal(final BaseGammaRef ref) {
        GammaRefTranlocal node = head;
        do {
            //noinspection ObjectEquality
            if (node.owner == ref) {
                return node;
            }

            if (node.owner == null) {
                return null;
            }

            node = node.next;
        } while (node != null);
        return null;
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

        GammaRefTranlocal tranlocal = head;
        do {
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
            tranlocal = tranlocal.next;
        } while (tranlocal != null && tranlocal.owner != null);

        status = TX_ABORTED;

        if (!atLeastOneRegistration) {
            throw abortRetryOnNoRetryPossible();
        }

        throw newRetryError();
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
    public final void hardReset() {
        status = TX_ACTIVE;
        hasWrites = false;
        size = 0;
        remainingTimeoutNs = config.timeoutNs;
        attempt = 1;
        commitConflict = false;
        hasReads = false;
    }

    @Override
    public final boolean softReset() {
        if (attempt >= config.getMaxRetries()) {
            return false;
        }

        commitConflict = false;
        status = TX_ACTIVE;
        hasWrites = false;
        size = 0;
        hasReads = false;
        attempt++;
        return true;
    }

    public final void shiftInFront(GammaRefTranlocal newHead) {
        //noinspection ObjectEquality
        if (newHead == head) {
            return;
        }

        head.previous = newHead;
        if (newHead.next != null) {
            newHead.next.previous = newHead.previous;
        }
        newHead.previous.next = newHead.next;
        newHead.next = head;
        newHead.previous = null;
        head = newHead;
    }

    @Override
    public void initLocalConflictCounter() {
        //ignore
    }
}
