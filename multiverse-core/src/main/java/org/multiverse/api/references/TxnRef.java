package org.multiverse.api.references;

import org.multiverse.api.*;
import org.multiverse.api.functions.*;
import org.multiverse.api.predicates.*;

/**
 * A Transactional Reference comparable to the <a href="http://clojure.org/refs">Clojure Ref</a>.
 * If a method is prefixed with atomic, the call will always run under its own txn, no
 * matter if there already is a txn available (so the propagation level is {@link PropagationLevel#RequiresNew}).
 * For the other methods, always an txn needs to be available, else you will get the
 * {@link org.multiverse.api.exceptions.TxnMandatoryException}.
 *
 * <h3>ControlFlowError</h3>
 *
 * <p>All non atomic methods are able to throw a (subclass) of the {@link org.multiverse.api.exceptions.ControlFlowError}. This error should
 * not be caught, it is task of the {@link TxnExecutor} to deal with.
 * 
 * <h3>TransactionExecutionException</h3>
 *
 * <p>Most of the methods can throw a {@link org.multiverse.api.exceptions.TxnExecutionException}.
 * This exception can be caught, but in most cases you want to figure out what the cause is (e.g. because
 * there are too many retries) and solve that problem.
 *
 * <h3>Threadsafe</h3>
 *
 * <p>All methods are threadsafe.
 *
 * @author Peter Veentjer.
 */
public interface TxnRef<E> extends TransactionalObject {

    /**
     * Gets the value using the provided txn.
     *
     * @return the current value.
     * @throws org.multiverse.api.exceptions.TxnExecutionException
     *                  if something failed while using the txn. The txn is guaranteed to have been aborted.
     * @throws org.multiverse.api.exceptions.ControlFlowError
     *                  if the Stm needs to control the flow in a different way than normal returns of exceptions. The txn
     *                  is guaranteed to have been aborted.
     * @see #atomicGet()
     */
    E get();

     /**
     * Gets the value and applies the lock. If the current lockMode already is higher than the provided lockMode
     * the Lock is not upgraded to a higher value.
     *
     * <p>This call lifts on the {@link org.multiverse.api.Txn} stored in the {@link org.multiverse.api.TxnThreadLocal}.
     *
     * @param lockMode the LockMode applied.
     * @return the current value.
     * @throws org.multiverse.api.exceptions.TxnExecutionException
     *                  if something failed while using the txn. The txn is guaranteed to have been aborted.
     * @throws org.multiverse.api.exceptions.ControlFlowError
     *                  if the Stm needs to control the flow in a different way than normal returns of exceptions. The txn
     *                  is guaranteed to have been aborted.
     * @see #atomicGet()
     */
    E getAndLock(LockMode lockMode);

    /**
     * Gets the value using the provided txn.
     *
     * @param txn the {@link Txn} used for this operation.
     * @return the value stored in the ref.
     * @throws NullPointerException if txn is null.
     * @throws org.multiverse.api.exceptions.TxnExecutionException
     *                  if something failed while using the txn. The txn is guaranteed to have been aborted.
     * @throws org.multiverse.api.exceptions.ControlFlowError
     *                  if the Stm needs to control the flow in a different way than normal returns of exceptions. The txn
     *                  is guaranteed to have been aborted.
     */
    E get(Txn txn);

    /**
     * Gets the value using the provided txn and acquired the lock with the specified LockMode.
     *
     * @param txn the {@link Txn} used for this operation.
     * @param lockMode the LockMode used
     * @return the value stored in the ref.
     * @throws NullPointerException if txn is null or if lockMode is null. If LockMode is null and a running txn is available
     *                              it will be aborted.
     * @throws org.multiverse.api.exceptions.TxnExecutionException
     *                  if something failed while using the txn. The txn is guaranteed to have been aborted.
     * @throws org.multiverse.api.exceptions.ControlFlowError
     *                  if the Stm needs to control the flow in a different way than normal returns of exceptions. The txn
     *                  is guaranteed to have been aborted.
     */
    E getAndLock(Txn txn, LockMode lockMode);

    /**
     * Sets the new value.
     *
     * <p>This call lifts on the {@link org.multiverse.api.Txn} stored in the {@link org.multiverse.api.TxnThreadLocal}.
     *
     * @param value the new value.
     * @return the new value.
     * @throws org.multiverse.api.exceptions.TxnExecutionException
     *                  if something failed while using the txn. The txn is guaranteed to have been aborted.
     * @throws org.multiverse.api.exceptions.ControlFlowError
     *                  if the Stm needs to control the flow in a different way than normal returns of exceptions. The txn
     *                  is guaranteed to have been aborted.
     */
    E set(E value);

    /**
     * Sets the new value and applies the lock.
     *
     * <p>This call lifts on the {@link org.multiverse.api.Txn} stored in the {@link org.multiverse.api.TxnThreadLocal}.
     *
     * @param value the new value.
     * @param lockMode the used LockMode.
     * @return the new value.
     * @throws NullPointerException if lockMode is null (if the txn is alive, it will also be aborted.
     * @throws org.multiverse.api.exceptions.TxnExecutionException
     *                  if something failed while using the txn. The txn is guaranteed to have been aborted.
     * @throws org.multiverse.api.exceptions.ControlFlowError
     *                  if the Stm needs to control the flow in a different way than normal returns of exceptions. The txn
     *                  is guaranteed to have been aborted.
     */
    E setAndLock(E value, LockMode lockMode);

   /**
    * Sets the new value using the provided txn.
    *
    * @param txn the {@link Txn} used for this operation.
    * @param value the new value
    * @return the old value
    * @throws NullPointerException if txn is null.
    * @throws org.multiverse.api.exceptions.TxnExecutionException
    *                  if something failed while using the txn. The txn is guaranteed to have been aborted.
    * @throws org.multiverse.api.exceptions.ControlFlowError
    *                  if the Stm needs to control the flow in a different way than normal returns of exceptions. The txn
    *                  is guaranteed to have been aborted.
    */
    E set(Txn txn, E value);

    /**
    * Sets the new value using the provided txn.
    *
    * @param txn the {@link Txn} used for this operation.
    * @param value the new value
    * @param lockMode the lockMode used.
    * @return the old value
    * @throws NullPointerException if txn is null or lockMode is null. If the lockMode is null and the txn
    *                              is alive, it will be aborted.
    * @throws org.multiverse.api.exceptions.TxnExecutionException
    *                  if something failed while using the txn. The txn is guaranteed to have been aborted.
    * @throws org.multiverse.api.exceptions.ControlFlowError
    *                  if the Stm needs to control the flow in a different way than normal returns of exceptions. The txn
    *                  is guaranteed to have been aborted.
    */
    E setAndLock(Txn txn, E value, LockMode lockMode);

    /**
     * Sets the value the value and returns the new value.
     *
     * <p>This call lifts on the {@link org.multiverse.api.Txn} stored in the {@link org.multiverse.api.TxnThreadLocal}.
     *
     * @param value the new value.
     * @return the old value.
     * @throws org.multiverse.api.exceptions.TxnExecutionException
     *                  if something failed while using the txn. The txn is guaranteed to have been aborted.
     * @throws org.multiverse.api.exceptions.ControlFlowError
     *                  if the Stm needs to control the flow in a different way than normal returns of exceptions. The txn
     *                  is guaranteed to have been aborted.
     */
    E getAndSet(E value);

    /**
     * Sets the value, acquired the Lock with the specified Lockmode and returns the previous value.
     *
     * <p>This call lifts on the {@link org.multiverse.api.Txn} stored in the {@link org.multiverse.api.TxnThreadLocal}.
     *
     * @param value the new value.
     * @return the old value.
     * @param lockMode the LockMode used.
     * @throws NullPointerException if LockMode is null. If a running txn is available, it will be aborted.
     * @throws org.multiverse.api.exceptions.TxnExecutionException
     *                  if something failed while using the txn. The txn is guaranteed to have been aborted.
     * @throws org.multiverse.api.exceptions.ControlFlowError
     *                  if the Stm needs to control the flow in a different way than normal returns of exceptions. The txn
     *                  is guaranteed to have been aborted.
     */
    E getAndSetAndLock(E value, LockMode lockMode);

    /**
     * Sets the value using the provided txn.
     *
     * @param value the new value.
     * @param txn the {@link Txn} used for this operation.
     * @return the old value.
     * @throws NullPointerException if txn is null.
     * @throws org.multiverse.api.exceptions.TxnExecutionException
     *                  if something failed while using the txn. The txn is guaranteed to have been aborted.
     * @throws org.multiverse.api.exceptions.ControlFlowError
     *                  if the Stm needs to control the flow in a different way than normal returns of exceptions. The txn
     *                  is guaranteed to have been aborted.
     */
    E getAndSet(Txn txn, E value);

    /**
     * Sets the value and acquired the Lock with the provided LockMode.
     *
     * <p>This call lifts on the {@link org.multiverse.api.Txn} stored in the {@link org.multiverse.api.TxnThreadLocal}.
     *
     * @param value the new value.
     * @param txn the {@link Txn} used for this operation.
     * @param lockMode the LockMode used.
     * @return the old value.
     * @throws NullPointerException if txn or LockMode is null. If the txn is running, and the LockMode is null,
     *                              it will be aborted.
     * @throws org.multiverse.api.exceptions.TxnExecutionException
     *                  if something failed while using the txn. The txn is guaranteed to have been aborted.
     * @throws org.multiverse.api.exceptions.ControlFlowError
     *                  if the Stm needs to control the flow in a different way than normal returns of exceptions. The txn
     *                  is guaranteed to have been aborted.
     */
    E getAndSetAndLock(Txn txn, E value, LockMode lockMode);

    /**
     * Atomically gets the value. The value could be stale as soon as it is returned. This
     * method doesn't care about any running txns. It could be that this call fails
     * e.g. when a ref is locked. If you don't care about correct orderings, see the
     * {@link #atomicWeakGet()}.
     *
     * @return the current value.
     * @throws org.multiverse.api.exceptions.TxnExecutionException
     */
    E atomicGet();

    /**
     * Atomically gets the value without providing any ordering guarantees. This method is extremely
     * cheap and will never fail. So even if the ref is privatized, this call will still complete.
     *
     * <p>It is the best method to call if you just want to get the current value stored.
     *
     * @return the value.
     */
    E atomicWeakGet();

    /**
     * Atomically sets the value and returns the new value. This method doesn't care about any
     * running txns.
     *
     * @param newValue the new value.
     * @return the new value.
     * @throws org.multiverse.api.exceptions.TxnExecutionException
     */
    E atomicSet(E newValue);

    /**
     * Atomically sets the value and returns the previous value. This method doesn't care about
     * any running txns.
     *
     * @param newValue the new value.
     * @return the old value.
     * @throws org.multiverse.api.exceptions.TxnExecutionException
     */
    E atomicGetAndSet(E newValue);

     /**
     * Applies the function on the ref in a commuting manner. So if there are no dependencies, the function
     * will commute. If somehow there already is a dependency or a dependency is formed on the result of
     * the commuting function, the function will not commute and will be exactly the same as an alter.
     *
     * <p>This is different than the behavior in Clojure where the commute will be re-applied at the end
     * of the txn, even though some dependency is introduced, which can lead to inconsistencies.
     *
     * <p>This call lifts on the {@link org.multiverse.api.Txn} stored in the {@link org.multiverse.api.TxnThreadLocal}.
     *
     * @param function the function to apply to this reference.
     * @throws NullPointerException if function is null.
     * @throws org.multiverse.api.exceptions.TxnExecutionException
     *                  if something failed while using the txn. The txn is guaranteed to have been aborted.
     * @throws org.multiverse.api.exceptions.ControlFlowError
     *                  if the Stm needs to control the flow in a different way than normal returns of exceptions. The txn
     *                  is guaranteed to have been aborted.
     */
    void commute(Function<E> function);

    /**
     * Applies the function on the ref in a commuting manner. So if there are no dependencies, the function
     * will commute. If somehow there already is a dependency or a dependency is formed on the result of
     * the commuting function, the function will not commute and will be exactly the same as an alter.
     *
     * <p>This is different than the behavior in Clojure where the commute will be re-applied at the end
     * of the txn, even though some dependency is introduced, which can lead to inconsistencies.
     *
     * <p>This call lifts on the {@link org.multiverse.api.Txn} stored in the {@link org.multiverse.api.TxnThreadLocal}.
     *
     * @param txn the {@link Txn} used for this operation.
     * @param function the function to apply to this reference.
     * @throws NullPointerException  if function is null. If there is an active txn, it will be aborted.
     * @throws org.multiverse.api.exceptions.TxnExecutionException
     *                  if something failed while using the txn. The txn is guaranteed to have been aborted.
     * @throws org.multiverse.api.exceptions.ControlFlowError
     *                  if the Stm needs to control the flow in a different way than normal returns of exceptions. The txn
     *                  is guaranteed to have been aborted.
     */
    void commute(Txn txn,Function<E> function);

    /**
     * Atomically applies the function to the current value in this ref and returns the new value. This method doesn't care about
     * any running txns.
     *
     * @param function the Function used
     * @return the new value.
     * @throws NullPointerException if function is null.
     */
    E atomicAlterAndGet(Function<E> function);

    /**
     * Alters the value stored in this Ref using the provided function and returns the result.
     *
     * <p>This call lifts on the {@link org.multiverse.api.Txn} stored in the {@link org.multiverse.api.TxnThreadLocal}.
     *
     * @param function the function that alters the value stored in this Ref.
     * @return the new value.
     * @throws NullPointerException if function is null. The Txn will also be aborted.
     * @throws org.multiverse.api.exceptions.TxnExecutionException
     *                  if something failed while using the txn. The txn is guaranteed to have been aborted.
     * @throws org.multiverse.api.exceptions.ControlFlowError
     *                  if the Stm needs to control the flow in a different way than normal returns of exceptions. The txn
     *                  is guaranteed to have been aborted.
     */
    E alterAndGet(Function<E> function);

    /**
     * Alters the value stored in this Ref using the provided function and lifting on the provided txn.
     *
     * @param function the function that alters the value stored in this Ref.
     * @param txn the {@link Txn} used for this operation.
     * @return the new value.
     * @throws NullPointerException if function or txn is null.
     * @throws org.multiverse.api.exceptions.TxnExecutionException
     *                  if something failed while using the txn. The txn is guaranteed to have been aborted.
     * @throws org.multiverse.api.exceptions.ControlFlowError
     *                  if the Stm needs to control the flow in a different way than normal returns of exceptions. The txn
     *                  is guaranteed to have been aborted.
     */
    E alterAndGet(Txn txn,Function<E> function);

    /**
     * Atomically applies the function to alter the value stored in this ref and returns the old value. This method doesn't care about
     * any running txns.
     *
     * @param function the Function used
     * @return the old value.
     * @throws NullPointerException if function is null.
     * @throws org.multiverse.api.exceptions.TxnExecutionException
     */
    E atomicGetAndAlter(Function<E> function);

    /**
     * Alters the value stored in this Ref using the provided function amd returns the old value.
     *
     * <p>This call lifts on the {@link org.multiverse.api.Txn} stored in the {@link org.multiverse.api.TxnThreadLocal}.
     *
     * @param function the function that alters the value stored in this Ref.
     * @return the old value.
     * @throws NullPointerException if function is null. The txn will be aborted as well.
     * @throws org.multiverse.api.exceptions.TxnExecutionException
     *                  if something failed while using the txn. The txn is guaranteed to have been aborted.
     * @throws org.multiverse.api.exceptions.ControlFlowError
     *                  if the Stm needs to control the flow in a different way than normal returns of exceptions. The txn
     *                  is guaranteed to have been aborted.
     */
    E getAndAlter(Function<E> function);

    /**
     * Alters the value stored in this Ref using the function and returns the old value, using the provided txn.
     *
     * @param function the function that alters the value stored in this Ref.
     * @param txn the {@link Txn} used for this operation.
     * @return the old value
     * @throws NullPointerException if function or txn is null. The txn will be aborted as well.
     * @throws org.multiverse.api.exceptions.TxnExecutionException
     *                  if something failed while using the txn. The txn is guaranteed to have been aborted.
     * @throws org.multiverse.api.exceptions.ControlFlowError
     *                  if the Stm needs to control the flow in a different way than normal returns of exceptions. The txn
     *                  is guaranteed to have been aborted.
     */
    E getAndAlter(Txn txn, Function<E> function);

    /**
     * Executes a compare and set atomically. This method doesn't care about any running txns.
     *
     * @param expectedValue the expected value.
     * @param newValue the new value.
     * @return true if the compareAndSwap was a success, false otherwise.
     * @throws org.multiverse.api.exceptions.TxnExecutionException
     */
    boolean atomicCompareAndSet(E expectedValue, E newValue);

    /**
     * Checks if the current value is null.
     *
     * <p>This call lifts on the {@link org.multiverse.api.Txn} stored in the {@link org.multiverse.api.TxnThreadLocal}.
     *
     * @return true if null, false otherwise.
     * @throws org.multiverse.api.exceptions.TxnExecutionException
     *                  if something failed while using the txn. The txn is guaranteed to have been aborted.
     * @throws org.multiverse.api.exceptions.ControlFlowError
     *                  if the Stm needs to control the flow in a different way than normal returns of exceptions. The txn
     *                  is guaranteed to have been aborted.
     */
    boolean isNull();

    /**
     * Checks if the current value is null using the provided txn.
     *
     * @param txn the {@link Txn} used for this operation.
     * @return true if the value is null, false otherwise.
     * @throws NullPointerException if txn is null.
     * @throws org.multiverse.api.exceptions.TxnExecutionException
     *                  if something failed while using the txn. The txn is guaranteed to have been aborted.
     * @throws org.multiverse.api.exceptions.ControlFlowError
     *                  if the Stm needs to control the flow in a different way than normal returns of exceptions. The txn
     *                  is guaranteed to have been aborted.
     */
    boolean isNull(Txn txn);

    /**
     * Atomically check if the current value is null. This method doesn't care about any running txns.
     *
     * @return true if null, false otherwise.
     * @throws org.multiverse.api.exceptions.TxnExecutionException
     */
    boolean atomicIsNull();

    /**
     * Awaits for the value to become not null. If the value already is not null,
     * this call returns the stored value. If the value is null, a retry is done.
     *
     * <p>This call lifts on the {@link org.multiverse.api.Txn} stored in the {@link org.multiverse.api.TxnThreadLocal}.
     *
     * @return the stored value.
     * @throws org.multiverse.api.exceptions.TxnExecutionException
     *                  if something failed while using the txn. The txn is guaranteed to have been aborted.
     * @throws org.multiverse.api.exceptions.ControlFlowError
     *                  if the Stm needs to control the flow in a different way than normal returns of exceptions. The txn
     *                  is guaranteed to have been aborted.
     */
    E awaitNotNullAndGet();

    /**
     * Awaits for the value to become not null using the provided txn. If the value already is not null,
     * this call returns the stored value. If the value is null, a retry is done.
     *
     * @param txn the {@link Txn} used for this operation.
     * @return the stored value.
     * @throws NullPointerException if txn is null.
     * @throws org.multiverse.api.exceptions.TxnExecutionException
     *                  if something failed while using the txn. The txn is guaranteed to have been aborted.
     * @throws org.multiverse.api.exceptions.ControlFlowError
     *                  if the Stm needs to control the flow in a different way than normal returns of exceptions. The txn
     *                  is guaranteed to have been aborted.
     */
    E awaitNotNullAndGet(Txn txn);

    /**
     * Awaits for the value to become null. If the value already is null,
     * this call continues. If the reference is not null, a retry is done.
     *
     * <p>This call lifts on the {@link org.multiverse.api.Txn} stored in the {@link org.multiverse.api.TxnThreadLocal}.
     *
     * @throws org.multiverse.api.exceptions.TxnExecutionException
     *                  if something failed while using the txn. The txn is guaranteed to have been aborted.
     * @throws org.multiverse.api.exceptions.ControlFlowError
     *                  if the Stm needs to control the flow in a different way than normal returns of exceptions. The txn
     *                  is guaranteed to have been aborted.
     */
    void awaitNull();

   /**
    * Awaits for the value to become not null using the provided txn. If the value already is null,
    * this call continues. If the value is not null, a retry is done.
    *
    * @param txn the {@link Txn} used for this operation.
    * @throws NullPointerException if txn is null.
    * @throws org.multiverse.api.exceptions.TxnExecutionException
     *                  if something failed while using the txn. The txn is guaranteed to have been aborted.
     * @throws org.multiverse.api.exceptions.ControlFlowError
     *                  if the Stm needs to control the flow in a different way than normal returns of exceptions. The txn
     *                  is guaranteed to have been aborted.
    */
    void awaitNull(Txn txn);

    /**
     * Awaits for the value to become the given value. If the value already has the
     * the specified value, the call continues, else a retry is done.
     *
     * <p>This call lifts on the {@link org.multiverse.api.Txn} stored in the {@link org.multiverse.api.TxnThreadLocal}.
     *
     * @param value the value to wait for.
     * @throws org.multiverse.api.exceptions.TxnExecutionException
     *                  if something failed while using the txn. The txn is guaranteed to have been aborted.
     * @throws org.multiverse.api.exceptions.ControlFlowError
     *                  if the Stm needs to control the flow in a different way than normal returns of exceptions. The txn
     *                  is guaranteed to have been aborted.
     */
    void await(E value);

    /**
     * Awaits for the reference to become the given value. If the value already has the
     * the specified value, the call continues, else a retry is done.
     *
     * @param txn the {@link Txn} used for this operation.
     * @param value the value to wait for.
     * @throws NullPointerException if txn is null.
     * @throws org.multiverse.api.exceptions.TxnExecutionException
     *                  if something failed while using the txn. The txn is guaranteed to have been aborted.
     * @throws org.multiverse.api.exceptions.ControlFlowError
     *                  if the Stm needs to control the flow in a different way than normal returns of exceptions. The txn
     *                  is guaranteed to have been aborted.
     */
    void await(Txn txn,E value);

    /**
     * Awaits until the predicate holds.  If the value already evaluates to true, the call continues
     * else a retry is done. If the predicate throws an exception, the txn is aborted and the
     * throwable is propagated.
     *
     * <p>This call lifts on the {@link org.multiverse.api.Txn} stored in the {@link org.multiverse.api.TxnThreadLocal}.
     *
     * @param predicate the predicate to evaluate.
     * @throws NullPointerException if predicate is null. When there is a non dead txn,
     *                              it will be aborted.
     * @throws org.multiverse.api.exceptions.TxnExecutionException
     *                  if something failed while using the txn. The txn is guaranteed to have been aborted.
     * @throws org.multiverse.api.exceptions.ControlFlowError
     *                  if the Stm needs to control the flow in a different way than normal returns of exceptions. The txn
     *                  is guaranteed to have been aborted.
     */
    void await(Predicate<E> predicate);

    /**
     * Awaits until the predicate holds using the provided txn.  If the value already evaluates to true, the call continues
     * else a retry is done. If the predicate throws an exception, the txn is aborted and the
     * throwable is propagated.
     *
     * @param txn the {@link Txn} used for this operation.
     * @param predicate the predicate to evaluate.
     * @throws NullPointerException if predicate is null or txn is null. When there is a non dead txn,
     *                              it will be aborted.
     * @throws org.multiverse.api.exceptions.TxnExecutionException
     *                  if something failed while using the txn. The txn is guaranteed to have been aborted.
     * @throws org.multiverse.api.exceptions.ControlFlowError
     *                  if the Stm needs to control the flow in a different way than normal returns of exceptions. The txn
     *                  is guaranteed to have been aborted.
     */
    void await(Txn txn, Predicate<E> predicate);
}
