package org.multiverse.commitbarriers;

import org.multiverse.api.Txn;
import org.multiverse.api.TxnStatus;
import org.multiverse.api.exceptions.DeadTxnException;
import org.multiverse.api.exceptions.PreparedTxnException;
import org.multiverse.api.lifecycle.TxnEvent;
import org.multiverse.api.lifecycle.TxnListener;

import java.util.List;

import static java.lang.String.format;

/**
 * A synchronization aid that allows a set of threads and transaction to all wait for each other to reach a common
 * barrier point; once this barrier is opened, all transaction atomically commit. A CountDownCommitBarrier is useful
 * in programs involving a fixed sized party of threads/transactions that must occasionally wait for each other.
 * <p/>
 * The CountDownCommitBarrier looks a lot like the {@link java.util.concurrent.CountDownLatch}. So if you have
 * experience with that functionality, this one should feel familiar.
 * <p/>
 * A {@code CountDownCommitBarrier} is initialized with a given <em>count</em>. The
 * {@link #joinCommit(org.multiverse.api.Txn)}  await} methods block until the current count reaches
 * zero due to invocations of the {@link #countDown} method, after which  all waiting threads are released. Unlike the
 * CountDownLatch, it isn't allowed for a new transaction to call one of the join methods after the barrier has
 * aborted or committed.
 * <p/>
 * This functionality is useful for two phase commit related functionality.
 * <p/>
 * The CountDownCommitBarrier can't be reused, so it is not cyclic like the {@link java.util.concurrent.CyclicBarrier}.
 * <p/>
 * A CountDownCommitBarrier is thread-safe to use of course.
 *
 * @author Peter Veentjer.
 * @see VetoCommitBarrier
 */
public final class CountDownCommitBarrier extends CommitBarrier {

    private volatile int parties;

    /**
     * Create a new CountDownCommitBarrier that uses an unfair lock.
     *
     * @param parties the number of parties waiting. If the number of parties is 0, the VetoCommitBarrier is created
     *                committed, else it will be closed.
     * @throws IllegalArgumentException if parties is smaller than 0.
     */
    public CountDownCommitBarrier(int parties) {
        this(parties, false);
    }

    /**
     * Creates a new CountDownCommitBarrier.
     *
     * @param parties the number of parties waiting. If the number of parties is 0, the VetoCommitBarrier is created
     *                committed, else it will be closed.
     * @param fair    if the lock bu this CountDownCommitBarrier is fair.
     * @throws IllegalArgumentException if parties smaller than 0.
     */
    public CountDownCommitBarrier(int parties, boolean fair) {
        super(parties == 0 ? Status.Committed : Status.Closed, fair);

        if (parties < 0) {
            throw new IllegalArgumentException();
        }

        this.parties = parties;
    }

    /**
     * Returns the number of parties that want to join this CountDownCommitBarrier.
     *
     * @return the number of parties.
     */
    public int getParties() {
        return parties;
    }

    protected boolean isLastParty() {
        return getNumberWaiting() == parties;
    }

    /**
     * Signal that one party has returned. If this is the last party to returned, all transactions
     * will commit.
     * <p/>
     * If the all parties already have returned, this call is ignored. This is the same behavior
     * as the {@link java.util.concurrent.CountDownLatch#countDown()} method provides.
     */
    public void countDown() {
        List<Runnable> onCommitTasks = null;

        lock.lock();
        try {
            switch (getStatus()) {
                case Closed:
                    addJoiner();

                    if (isLastParty()) {
                        onCommitTasks = signalCommit();
                    }
                    break;
                case Aborted:
                    break;
                case Committed:
                    break;
                default:
                    throw new IllegalStateException();
            }
        } finally {
            lock.unlock();
        }

        executeTasks(onCommitTasks);
    }

    /**
     * Adds 1 additional party to this CountDownCommitBarrier.
     *
     * @throws CommitBarrierOpenException if this CountDownCommitBarrier already is committed or aborted.
     * @see #atomicIncParties(int)
     */
    public void atomicIncParties() {
        atomicIncParties(1);
    }

    /**
     * Atomically adds additional parties to this CountDownCommitBarrier.
     * <p/>
     * Call is ignored when extra is 0.
     * <p/>
     * This method is not transactional, so be very careful calling it within a transaction. Transactions can be
     * retried, so this method could be called more than once. This means that the number of added parties could
     * be completely bogus. For a transactional version see {@link #incParties(org.multiverse.api.Txn, int)}.
     *
     * @param extra the additional parties.
     * @throws IllegalArgumentException   if extra smaller than 0.
     * @throws CommitBarrierOpenException if this CountDownCommitBarrier already is open.
     */
    public void atomicIncParties(int extra) {
        if (extra < 0) {
            throw new IllegalArgumentException();
        }

        lock.lock();
        try {
            switch (getStatus()) {
                case Closed:
                    if (extra == 0) {
                        return;
                    }

                    parties += extra;
                    break;
                case Aborted:
                    String abortMsg = "Can't call countDown on already aborted CountDownCommitBarrier";
                    throw new CommitBarrierOpenException(abortMsg);
                case Committed:
                    String commitMsg = "Can't call countDown on already committed CountDownCommitBarrier";
                    throw new CommitBarrierOpenException(commitMsg);
                default:
                    throw new IllegalStateException();
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Increases the number of parties that need to return before this CommitBarrier can open.
     * The parties are only increased after the transaction has committed.
     * <p/>
     * If extra is 0, this call is ignored (unless the Txn is not active, then a
     * IllegalTransactionState will be thrown.
     * <p/>
     * This is the call you want to use when you are doing an atomicIncParties inside a transaction.
     * A transaction can be retried multiple times, and if number of parties is incremented more than
     * once, you run into problems. That is why a transaction can be passed where a compensating
     * tasks is registered on, that removes the added parties when the transaction is aborted.
     *
     * @param tx    the transaction where this operation lifts on.
     * @param extra the number of extra parties
     * @throws NullPointerException     if tx is null.
     * @throws IllegalArgumentException is extra smaller than zero.
     * @throws org.multiverse.api.exceptions.IllegalTxnStateException
     *                                  if the transaction is not in the correct
     *                                  state for this operation (so not active).
     */
    public void incParties(Txn tx, int extra) {
        if (tx == null) {
            throw new NullPointerException();
        }

        if (tx.getStatus() != TxnStatus.Active) {
            if (tx.getStatus() == TxnStatus.Prepared) {
                tx.abort();
                throw new PreparedTxnException(
                        format("[%s] Can't call incParties on non active transaction because it is %s",
                                tx.getConfiguration().getFamilyName(), tx.getStatus()));
            } else {
                throw new DeadTxnException(
                        format("[%s] Can't call incParties on non active transaction because it is %s",
                                tx.getConfiguration().getFamilyName(), tx.getStatus()));
            }

        }

        if (extra < 0) {
            throw new IllegalArgumentException();
        }

        lock.lock();
        try {
            switch (getStatus()) {
                case Closed:
                    if (extra == 0) {
                        return;
                    }

                    parties += extra;
                    tx.register(new RestorePartiesCompensatingTask(extra));
                    break;
                case Aborted:
                    String abortMsg = format("[%s] Can't call incParties on already aborted CountDownCommitBarrier",
                            tx.getConfiguration().getFamilyName());
                    throw new CommitBarrierOpenException(abortMsg);
                case Committed:
                    String commitMsg = format("[%s] Can't call incParties on already committed CountDownCommitBarrier",
                            tx.getConfiguration().getFamilyName());
                    throw new CommitBarrierOpenException(commitMsg);
                default:
                    throw new IllegalStateException();
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * A TransactionLifecycleListener that is responsible for restoring the the number of
     * parties after the transaction that increased them, was aborted.
     */
    private class RestorePartiesCompensatingTask implements TxnListener {
        private final int extra;

        RestorePartiesCompensatingTask(int extra) {
            this.extra = extra;
        }

        @Override
        public void notify(Txn tx, TxnEvent event) {
            //todo: in the 0.6 release of multiverse this was pre-abort.. but since the pre-abort doesn't
            //exist anymore..
            if (event != TxnEvent.PostAbort) {
                return;
            }

            List<Runnable> onCommitTasks = null;
            lock.lock();
            try {
                if (getStatus() == Status.Closed) {
                    parties -= extra;
                    if (isLastParty()) {
                        onCommitTasks = signalCommit();
                    }
                }
            } finally {
                lock.unlock();
            }
            executeTasks(onCommitTasks);
        }
    }
}
