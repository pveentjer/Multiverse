package org.multiverse.api;

import org.multiverse.api.exceptions.TxnMandatoryException;

/**
 * A {@link ThreadLocal} that contains the current {@link Txn}. The {@link Stm} and the {@link Txn}
 * should not rely on threadlocals, they are only used for convenience to reduce the need to carry around a
 * Txn.
 *
 * <p>This TxnThreadLocal has an optimization that prevents accessing the threadlocal too many times.
 * The Container wraps the Txn, so if a Thread gets a reference to that container and holds it, it
 * can modify the current transaction with a direct field access instead of another threadlocal access. It should
 * be used with extreme care, because the Container should not leak to another thread. It is very useful for the
 * {@link TxnExecutor} for example because a get/getAndSet/clear needs to be called otherwise.
 *
 * @author Peter Veentjer.
 */
public final class TxnThreadLocal {

    public final static ThreadLocal<Container> threadlocal = new ThreadLocal<Container>() {
        protected Container initialValue() {
            return new Container();
        }
    };

    /**
     * Gets the threadlocal {@link Txn}. If no transaction is set, null is returned.
     *
     * <p>No checks are done on the state of the transaction (so it could be that an aborted or committed transaction is
     * returned).
     *
     * @return the threadlocal transaction.
     */
    public static Txn getThreadLocalTxn() {
        return threadlocal.get().txn;
    }

    /**                     ThreadLocalTransaction
     * Gets the ThreadLocal container that stores the Txn. Use this with extreme care because
     * the Container should not leak to another thread. It is purely means as a performance optimization
     * to prevent repeated (expensive) threadlocal access, and replace it by a cheap field access.
     *
     * @return the Container. The returned value will never be null.
     */
    public static Container getThreadLocalTxnContainer() {
        return threadlocal.get();
    }

    /**
     * Gets the threadlocal {@link Txn} or throws a {@link org.multiverse.api.exceptions.TxnMandatoryException} if no transaction is found.
     *
     * <p>No checks are done on the state of the transaction (so it could be that an aborted or committed transaction is
     * returned).
     *
     * @return the threadlocal transaction.
     * @throws org.multiverse.api.exceptions.TxnMandatoryException
     *          if no thread local transaction is found.
     */
    public static Txn getRequiredThreadLocalTxn() {
        Txn txn = threadlocal.get().txn;

        if (txn == null) {
            throw new TxnMandatoryException("No transaction is found on the TxnThreadLocal");
        }

        return txn;
    }

    /**
     * Clears the threadlocal transaction.
     *
     * <p>If a transaction is available, it isn't aborted or committed.
     */
    public static void clearThreadLocalTxn() {
        threadlocal.get().txn = null;
    }

    /**
     * Sets the threadlocal transaction. The transaction is allowed to be null, effectively clearing the
     * current thread local transaction.
     *
     * <p>If a transaction is available, it isn't aborted or committed.
     *
     * @param txn the new thread local transaction.
     */
    public static void setThreadLocalTxn(Txn txn) {
        threadlocal.get().txn = txn;
    }

    //we don't want any instances.

    private TxnThreadLocal() {
    }

    public static class Container {
        public Txn txn;
        public Object txPool;
    }
}
