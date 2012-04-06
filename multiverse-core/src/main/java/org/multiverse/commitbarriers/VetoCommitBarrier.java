package org.multiverse.commitbarriers;

import org.multiverse.api.Txn;

import java.util.List;

import static java.lang.String.format;

/**
 * The VetoCommitBarrier is a synchronization primitive that makes it possible to atomicChecked a 2 phase commit;
 * so all transaction within a VetoCommitBarrier commit, or they all abort. The VetoCommitBarrier is useful if
 * there is a veto situation; so one transaction is able to commit all others.
 * <p/>
 * This structure is thread-safe to use.
 * <p/>
 *
 * @author Peter Veentjer.
 */
public final class VetoCommitBarrier extends CommitBarrier {

    /**
     * Creates a new VetoCommitBarrier that uses an unfair Lock.
     */
    public VetoCommitBarrier() {
        this(false);
    }

    /**
     * Creates a new VetoCommitBarrier.
     *
     * @param fair if the lock should be fair.
     */
    public VetoCommitBarrier(boolean fair) {
        super(Status.Closed, fair);
    }

    @Override
    protected boolean isLastParty() {
        return false;
    }

    /**
     * Veto's the commit so that all prepared Transactions in this VetoCommitBarriers should commit. This call doesn't
     * block (for a long time).
     * <p/>
     * It could be that this method finishes before the other parties have completed their commit. In most
     * cases this won't be an issue, because the other parties have prepared their transactions, so all
     * needed resources are locked.
     * <p/>
     * If the VetoCommitBarrier already is committed, this call is ignored.
     * <p/>
     * After the commit completes, the onCommitTasks are executed.
     *
     * @throws CommitBarrierOpenException if the VetoCommitBarrier already is aborted.
     */
    public void atomicVetoCommit() {
        List<Runnable> postCommitTasks = null;
        lock.lock();
        try {
            switch (getStatus()) {
                case Closed:
                    postCommitTasks = signalCommit();
                    break;
                case Committed:
                    //ignore it.
                    return;
                case Aborted:
                    String abortMsg = "Can't veto a commit on already aborted VetoCommitBarrier";
                    throw new CommitBarrierOpenException(abortMsg);
                default:
                    throw new IllegalStateException();
            }
        } finally {
            lock.unlock();
        }

        executeTasks(postCommitTasks);
    }

    /**
     * Veto's the commit of this VetoCommitBarrier including the provided transaction. If all goes well all
     * pending transactions and the provided transaction are going to commit.
     * <p/>
     * It could be that this method finishes before the other parties have completed their commit. In most
     * cases this won't be an issue, because the other parties have prepared their transactions, so all
     * needed resources are locked.
     * <p/>
     * If the VetoCommitBarrier already is aborted or committed, the transaction is aborted.
     *
     * @param tx the Txn to commit.
     * @throws NullPointerException       if tx is null.
     * @throws org.multiverse.api.exceptions.DeadTransactionException
     *                                    if the Txn already is aborted or committed.
     * @throws org.multiverse.api.exceptions.ReadWriteConflict
     *                                    if the commit was not executed successfully.
     * @throws CommitBarrierOpenException if the VetoCommitBarrier already is open.
     */
    public void vetoCommit(Txn tx) {
        ensureNotDead(tx, "vetoCommit");

        List<Runnable> postCommitTasks = null;
        lock.lock();
        try {
            switch (getStatus()) {
                case Closed:
                    tx.prepare();
                    postCommitTasks = signalCommit();
                    break;
                case Aborted:
                    String abortMsg = format(
                            "[%s] Can't veto commit on already aborted VetoCommitBarrier",
                            tx.getConfiguration().getFamilyName());
                    throw new CommitBarrierOpenException(abortMsg);
                case Committed:
                    String commitMsg = format(
                            "[%s] Can't veto commit on already committed VetoCommitBarrier",
                            tx.getConfiguration().getFamilyName());
                    throw new CommitBarrierOpenException(commitMsg);
                default:
                    throw new IllegalStateException();
            }
        } finally {
            lock.unlock();
        }

        tx.commit();

        executeTasks(postCommitTasks);
    }

    public String toString(){
        return format("VetoCommitBarrier(isClosed=%s)",isClosed());
    }
}
