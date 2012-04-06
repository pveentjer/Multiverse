package org.multiverse.api;

import org.multiverse.*;
import org.multiverse.api.closures.*;

/**
 * An TxnExecutor is responsible for executing an atomic closure. It is created by the {@link TxnFactoryBuilder}
 * and this gives the {@link Stm} the opportunity to return different implementations based on the
 * {@link TxnFactory} configuration. And it also gives the opportunity to provide Stm specific transaction handling
 * mechanism. In the Multiverse 0.6 design and before, a single TransactionTemplate implementation was used that should
 * be used by all Stm's, but that design is limiting.
 *
 * <p>Another useful features of this design is that for certain primitives it doesn't require any form of boxing.
 * It also provides an execute for a AtomicVoidClosure which doesn't force a developer to return something when
 * nothing needs to be returned.
 *
 * <h3>AtomicClosure</h3>
 *
 * <p>The AtomicClosure is the functionality that needs to be executed isolated, consistent and atomically. There
 * are different tasted of Closures but essentially the only difference is the return type. There are primitive closures
 * that prevent unwanted autoboxing and there also is a {@link org.multiverse.api.closures.TxnVoidClosure} that prevents
 * returning a value if none is needed. And last but not least there also is the general purpose
 * {@link org.multiverse.api.closures.TxnClosure} that returns an Object reference.
 *
 * <h3>Automatic retries</h3>
 *
 * <p>If a transaction encounters a {@link org.multiverse.api.exceptions.ReadWriteConflict} or a
 * { @link org.multiverse.api.exceptions.SpeculativeConfigurationError} it will automatically retry the
 * the AtomicClosure until either the next execution completes or the maximum number of retries has been reached.
 * To prevent contention, also a {@link BackoffPolicy} is used, to prevent transactions from causing more contention
 * if there already is contention. For configuring the maximum number of retries, see the {@link TransactionFactoryBuilder#setMaxRetries}
 * and for configuring the BackoffPolicy, see {@link TransactionFactoryBuilder#setBackoffPolicy}.
 *
 * <p>It is very important to realize that automatically retrying a transaction on a conflict is something else than the
 * {@link Transaction#retry}. The latter is really a blocking operation that only retries when there is a reason to retry.
 *
 * <h3>Configuration</h3>
 *
 * <p>The {@link TxnExecutor} can be configured through the {@link TransactionFactoryBuilder}. So that for more detail since
 * there are tons of settings to choose from.
 *
 * <h3>Thread-safety</h3>
 *
 * <p>TxnExecutors are threadsafe. The TxnExecutor is designed to be shared between threads.
 *
 * <h3>Reuse</h3>
 *
 * <p>TxnExecutor can be expensive to create and should be reused. Creating an TxnExecutor can lead to a lot of objects being
 * created and not reusing them leads to a lot of object waste (so put a lot of pressure on the garbage collector).
 *
 * <p>It is best to create the TxnExecutor in the beginning and store it in a (static) field and reuse it. It is very
 * unlikely that an TxnExecutor is going to be a contention point itself since in almost all cases only volatile reads are
 * required and for the rest it will be mostly immutable.
 *
 * <p>This is even more important when speculative transactions are used because speculative transactions learn on the
 * TxnExecutor level. So if the TxnExecutor is not reused, the speculative mechanism will not have full effect.
 *
 * <h3>execute vs executeChecked</h3>
 *
 * <p>The TxnExecutor provides two different types of execute methods:
 * <ol>
 * <li>execute: it will automatically wrap the checked exception that can be thrown from an AtomicClosure in a
 * {@link org.multiverse.api.exceptions.InvisibleCheckedException}. Unchecked exceptions are let through as is.
 * </li>
 * <li>execute checked: it will not do anything with thrown checked of unchecked exceptions and lets them through
 * </li>
 * </ol>
 * If an exception happens inside an AtomicClosure, the Transaction will be always aborted (unless it is caught by the logic
 * inside the AtomicClosure). Catching the exceptions inside the closure should be done with care since an exception could
 * indicate that the system has entered an invalid state.
 *
 * <p>In the future also a rollback-for functionality will be added to let a transaction commit, even though certain types
 * of exceptions have occurred. This is similar with the Spring framework where this can be configured through the
 * <a href="http://static.springsource.org/spring/docs/2.0.x/reference/transaction.html#transaction-declarative-rolling-back}">9.5.3: Rolling back</a>
 *
 * <h3>Atomic operation composition/nesting</h3>
 *
 * <p>Using traditional concurrency control, composing locking operations is extremely hard because it is very likely that
 * it is impossible without knowing implementation details of the structure, or because of deadlocks. With Stm transactional
 * operations can be composed and controlling how the system should react on existing or missing transactions can be controlled
 * through the {@link TransactionFactoryBuilder#setPropagationLevel} where the {@link PropagationLevel#Requires} is the default.
 *
 * <p>Normally the system uses a flat-nesting approach, so only the outermost commit is going to lead to a commit. But if a commit
 * is done before the outer most TxnExecutor completes, that commit is leading.
 *
 * <p>If the Transaction is committed (or aborted) manually, operations on the Transaction will fail with a
 * {@link org.multiverse.api.exceptions.IllegalTxnStateException} exception. So in most cases you want to let the TxnExecutor
 * be in charge of committing/aborting. If also allows for a correct flattening of nested transactions.  If a transaction should
 * not commit, but you don't want to disrupt the code, the {@link Transaction#setAbortOnly} can be called, to make sure that the
 * transaction is not going to commit (or prepare) successfully.
 *
 * <p>The configuration of the outer most TxnExecutor is leading. So if the outer TxnExecutor is not readonly and the inner is,
 * the transaction will not be readonly. If this becomes an issue (e.g. for security) it can be implemented that some form of
 * runtime verification is done to prevent this behavior.
 *
 * @author Peter Veentjer.
 */
public interface TxnExecutor extends MultiverseConstants{

   /**
    * Returns the TransactionFactory that is used by this TxnExecutor to create Transactions used inside.
    *
    * @return the TransactionFactory used by this TxnExecutor.
    */
    TxnFactory getTransactionFactory();

   /**
    * Executes the closure. If in the execution of the closure a checked exception is thrown, the exception
    * is wrapped in a InvisibleCheckedException. The original exception can be retrieved by calling the
    * getCause method.
    *
    * @param closure the closure to execute.
    * @return the result of the execution.
    * @throws NullPointerException if closure is null.
    * @throws org.multiverse.api.exceptions.InvisibleCheckedException if a checked exception is thrown by the closure.
    */
    <E> E atomic(TxnClosure<E> closure);

   /**
    * Executes the closure.
    *
    * @param closure the closure to execute.
    * @return the result of the execution.
    * @throws NullPointerException if closure is null.
    * @throws Exception if the execute call fails.
    */
    <E> E atomicChecked(TxnClosure<E> closure)throws Exception;

   /**
    * Executes the closure. If in the execution of the closure a checked exception is thrown, the exception
    * is wrapped in a InvisibleCheckedException. The original exception can be retrieved by calling the
    * getCause method.
    *
    * @param closure the closure to execute.
    * @return the result of the execution.
    * @throws NullPointerException if closure is null.
    * @throws org.multiverse.api.exceptions.InvisibleCheckedException if a checked exception is thrown by the closure.
    */
     int atomic(TxnIntClosure closure);

   /**
    * Executes the closure.
    *
    * @param closure the closure to execute.
    * @return the result of the execution.
    * @throws NullPointerException if closure is null.
    * @throws Exception if the execute call fails.
    */
     int atomicChecked(TxnIntClosure closure)throws Exception;

   /**
    * Executes the closure. If in the execution of the closure a checked exception is thrown, the exception
    * is wrapped in a InvisibleCheckedException. The original exception can be retrieved by calling the
    * getCause method.
    *
    * @param closure the closure to execute.
    * @return the result of the execution.
    * @throws NullPointerException if closure is null.
    * @throws org.multiverse.api.exceptions.InvisibleCheckedException if a checked exception is thrown by the closure.
    */
     long atomic(TxnLongClosure closure);

   /**
    * Executes the closure.
    *
    * @param closure the closure to execute.
    * @return the result of the execution.
    * @throws NullPointerException if closure is null.
    * @throws Exception if the execute call fails.
    */
     long atomicChecked(TxnLongClosure closure)throws Exception;

   /**
    * Executes the closure. If in the execution of the closure a checked exception is thrown, the exception
    * is wrapped in a InvisibleCheckedException. The original exception can be retrieved by calling the
    * getCause method.
    *
    * @param closure the closure to execute.
    * @return the result of the execution.
    * @throws NullPointerException if closure is null.
    * @throws org.multiverse.api.exceptions.InvisibleCheckedException if a checked exception is thrown by the closure.
    */
     double atomic(TxnDoubleClosure closure);

   /**
    * Executes the closure.
    *
    * @param closure the closure to execute.
    * @return the result of the execution.
    * @throws NullPointerException if closure is null.
    * @throws Exception if the execute call fails.
    */
     double atomicChecked(TxnDoubleClosure closure)throws Exception;

   /**
    * Executes the closure. If in the execution of the closure a checked exception is thrown, the exception
    * is wrapped in a InvisibleCheckedException. The original exception can be retrieved by calling the
    * getCause method.
    *
    * @param closure the closure to execute.
    * @return the result of the execution.
    * @throws NullPointerException if closure is null.
    * @throws org.multiverse.api.exceptions.InvisibleCheckedException if a checked exception is thrown by the closure.
    */
     boolean atomic(TxnBooleanClosure closure);

   /**
    * Executes the closure.
    *
    * @param closure the closure to execute.
    * @return the result of the execution.
    * @throws NullPointerException if closure is null.
    * @throws Exception if the execute call fails.
    */
     boolean atomicChecked(TxnBooleanClosure closure)throws Exception;

   /**
    * Executes the closure. If in the execution of the closure a checked exception is thrown, the exception
    * is wrapped in a InvisibleCheckedException. The original exception can be retrieved by calling the
    * getCause method.
    *
    * @param closure the closure to execute.
    * @throws NullPointerException if closure is null.
    * @throws org.multiverse.api.exceptions.InvisibleCheckedException if a checked exception is thrown by the closure.
    */
     void atomic(TxnVoidClosure closure);

   /**
    * Executes the closure.
    *
    * @param closure the closure to execute.
    * @throws NullPointerException if closure is null.
    * @throws Exception if the execute call fails.
    */
     void atomicChecked(TxnVoidClosure closure)throws Exception;

}
