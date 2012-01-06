package org.multiverse.api;

import org.multiverse.api.exceptions.TransactionMandatoryException;

/**
 * A {@link ThreadLocal} that contains the current {@link Transaction}. The {@link Stm} and the {@link Transaction}
 * should not rely on threadlocals, they are only used for convenience to reduce the need to carry around a
 * Transaction.
 *
 * <p>This ThreadLocalTransaction has an optimization that prevents accessing the threadlocal too many times.
 * The Container wraps the Transaction, so if a Thread gets a reference to that container and holds it, it
 * can modify the current transaction with a direct field access instead of another threadlocal access. It should
 * be used with extreme care, because the Container should not leak to another thread. It is very useful for the
 * {@link AtomicBlock} for example because a get/getAndSet/clear needs to be called otherwise.
 *
 * @author Peter Veentjer.
 */
public final class ThreadLocalTransaction {

    public final static ThreadLocal<Container> threadlocal = new ThreadLocal<Container>() {
        protected Container initialValue() {
            return new Container();
        }
    };

    /**
     * Gets the threadlocal {@link Transaction}. If no transaction is set, null is returned.
     *
     * <p>No checks are done on the state of the transaction (so it could be that an aborted or committed transaction is
     * returned).
     *
     * @return the threadlocal transaction.
     */
    public static Transaction getThreadLocalTransaction() {
        return threadlocal.get().tx;
    }

    /**
     * Gets the ThreadLocal container that stores the Transaction. Use this with extreme care because
     * the Container should not leak to another thread. It is purely means as a performance optimization
     * to prevent repeated (expensive) threadlocal access, and replace it by a cheap field access.
     *
     * @return the Container. The returned value will never be null.
     */
    public static Container getThreadLocalTransactionContainer() {
        return threadlocal.get();
    }

    /**
     * Gets the threadlocal {@link Transaction} or throws a {@link org.multiverse.api.exceptions.TransactionMandatoryException} if no transaction is found.
     *
     * <p>No checks are done on the state of the transaction (so it could be that an aborted or committed transaction is
     * returned).
     *
     * @return the threadlocal transaction.
     * @throws org.multiverse.api.exceptions.TransactionMandatoryException
     *          if no thread local transaction is found.
     */
    public static Transaction getRequiredThreadLocalTransaction() {
        Transaction tx = threadlocal.get().tx;

        if (tx == null) {
            throw new TransactionMandatoryException("No transaction is found on the ThreadLocalTransaction");
        }

        return tx;
    }

    /**
     * Clears the threadlocal transaction.
     *
     * <p>If a transaction is available, it isn't aborted or committed.
     */
    public static void clearThreadLocalTransaction() {
        threadlocal.get().tx = null;
    }

    /**
     * Sets the threadlocal transaction. The transaction is allowed to be null, effectively clearing the
     * current thread local transaction.
     *
     * <p>If a transaction is available, it isn't aborted or committed.
     *
     * @param tx the new thread local transaction.
     */
    public static void setThreadLocalTransaction(Transaction tx) {
        threadlocal.get().tx = tx;
    }

    //we don't want any instances.

    private ThreadLocalTransaction() {
    }

    public static class Container {
        public Transaction tx;
        public Object txPool;
    }
}
