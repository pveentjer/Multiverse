package org.multiverse.api;

import org.multiverse.api.closures.*;
import org.multiverse.api.collections.*;
import org.multiverse.api.exceptions.*;
import org.multiverse.api.lifecycle.*;
import org.multiverse.api.references.*;
import java.util.*;

import static org.multiverse.api.GlobalStmInstance.getGlobalStmInstance;
import static org.multiverse.api.TxnThreadLocal.getRequiredThreadLocalTxn;

/**
 * A utility class with convenience methods to access the {@link org.multiverse.api.Stm} or
 * {@link Txn}. These methods can be imported using the static import for a less
 * ugly syntax.
 *
 * @author Peter Veentjer.
 */
public final class StmUtils {

    private final static RefFactory refFactory
        = getGlobalStmInstance().getDefaultRefFactory();
    private final static TxnExecutor defaultTxnExecutor
        = getGlobalStmInstance().getDefaultTxnExecutor();
    private final static OrElseBlock orelseBlock
        = getGlobalStmInstance().newOrElseBlock();
    private final static TxnCollectionsFactory txnCollectionsFactory
        = getGlobalStmInstance().getDefaultTxnCollectionFactory();

    /**
     * Creates a new committed TxnList based on a double linked list.
     *
     * @return the created TxnList.
     */
    public static <E> TxnList<E> newLinkedList(){
        return txnCollectionsFactory.newLinkedList();
    }

    /**
     * Creates a new committed unbound TxnStack.
     *
     * @return the created TxnStack.
     */
    public static <E> TxnStack<E> newStack(){
        return txnCollectionsFactory.newStack();
    }

    /**
     * Creates a new committed bound TxnStack.
     *
     * @param capacity the maximum capacity of the stack. Integer.MAX_VALUE indicates that there is no bound.
     * @return the create TxnStack
     * @throws IllegalArgumentException if capacity smaller than 0.
     */
    public static <E> TxnStack<E> newStack(int capacity){
        return txnCollectionsFactory.newStack(capacity);
    }

    /**
     * Creates a new committed unbound TxnQueue.
     *
     * @return the created TxnQueue.
     */
    public static <E> TxnQueue<E> newQueue(){
        return txnCollectionsFactory.newQueue();
    }

    /**
     * Creates a new committed bound TxnQueue.
     *
     * @param capacity the maximum capacity of the queue. Integer.MAX_VALUE indicates that there is no bound.
     * @return the created TxnQueue
     * @throws IllegalArgumentException if capacity smaller than 0.
     */
    public static <E> TxnQueue<E> newQueue(int capacity){
        return txnCollectionsFactory.newQueue(capacity);
    }

    /**
     * Creates a new committed unbound TxnDeque.
     *
     * @return the created TxnDeque
     */
    public static <E> TxnDeque<E> newDeque(){
        return txnCollectionsFactory.newDeque();
    }

    /**
     * Creates a new committed bound TxnDeque.
     *
     * @param capacity the maximum capacity of the deque. Integer.MAX_VALUE indicates that there is no bound.
     * @return the created TxnDeque.
     * @throws IllegalArgumentException if capacity is smaller than 0.
     */
    public static <E> TxnDeque<E> newDeque(int capacity){
        return txnCollectionsFactory.newDeque(capacity);
    }

    /**
     * Creates a new committed TxnSet that is based on a 'hashtable'.
     *
     * @return the created TxnSet.
     */
    public static <E> TxnSet<E> newHashSet(){
        return txnCollectionsFactory.newHashSet();
    }

    /**
     * Creates a new committed TxnMap.
     *
     * @return the created TxnMap
     */
    public static <K, V> TxnMap<K, V> newHashMap(){
        return txnCollectionsFactory.newHashMap();
    }

    /**
     * Executes the closure transactionally on the GlobalStmInstance using the default TxnExecutor. If a
     * Transaction already is active on the TxnThreadLocal, this transaction will lift on that
     * transaction (so the propagation level is Requires) and will not commit this transaction.
     *
     * <p>This method doesn't throw a checked exception, but if the closure does, it is wrapped inside an
     * InvisibleCheckedException.
     *
     * <p>If you want to get most out of performance, it is best to make use of a customized {@link TxnExecutor} instead
     * of relying on the default TxnExecutor that will always provide the most expensive transaction available.
     *
     * @param closure The closure {@link TxnClosure} to execute.
     * @return the result of the execution
     * @throws NullPointerException if closure is null.
     * @throws org.multiverse.api.exceptions.InvisibleCheckedException
     *                                  if the closure throws a checked exception.
     */
    public static <E> E atomic(TxnClosure<E> closure){
        return defaultTxnExecutor.atomic(closure);
    }

   /**
    * Executes the closure transactionally on the GlobalStmInstance using the default TxnExecutor. If a
    * Transaction already is active on the TxnThreadLocal, this transaction will lift on that
    * transaction (so the propagation level is Requires) and will not commit this transaction.
    *
    * <p>If you want to get most out of performance, it is best to make use of a customized {@link TxnExecutor} instead
    * of relying on the default TxnExecutor that will always provide the most expensive transaction available.
    *
    * @param closure The {@link TxnClosure} to execute.
    * @return the result of the execution
    * @throws NullPointerException if closure is null.
    * @throws Exception is the closure throws an Exception
    */
   public static <E> E atomicChecked(TxnClosure<E> closure) throws Exception{
       return defaultTxnExecutor.atomicChecked(closure);
   }

   /**
    * Executes the either block, or in case of a retry, the orelse block is executed.
    *
    * If in the execution of the closure a checked exception is thrown, the exception
    * is wrapped in a InvisibleCheckedException. The original exception can be retrieved by calling the
    * getCause method.
    *
    * @param either the either block
    * @param orelse the orelse block.
    * @return the result of the execution.
    * @throws NullPointerException if closure is null.
    * @throws org.multiverse.api.exceptions.InvisibleCheckedException if a checked exception is thrown by the closure.
    */
    public static <E> E atomic(TxnClosure<E> either, TxnClosure<E> orelse){
        return orelseBlock.execute(either,orelse);
    }

    /**
    * Executes the either block, or in case of a retry, the orelse block is executed.
    *
    * @param either the either block
    * @param orelse the orelse block
    * @return the result of the execution.
    * @throws NullPointerException if closure is null.
    * @throws Exception if the execute call fails.
    */
    public static <E> E atomicChecked(TxnClosure<E> either, TxnClosure<E> orelse)throws Exception{
        return orelseBlock.executeChecked(either,orelse);
    }

    /**
     * Executes the closure transactionally on the GlobalStmInstance using the default TxnExecutor. If a
     * Transaction already is active on the TxnThreadLocal, this transaction will lift on that
     * transaction (so the propagation level is Requires) and will not commit this transaction.
     *
     * <p>This method doesn't throw a checked exception, but if the closure does, it is wrapped inside an
     * InvisibleCheckedException.
     *
     * <p>If you want to get most out of performance, it is best to make use of a customized {@link TxnExecutor} instead
     * of relying on the default TxnExecutor that will always provide the most expensive transaction available.
     *
     * @param closure The closure {@link TxnIntClosure} to execute.
     * @return the result of the execution
     * @throws NullPointerException if closure is null.
     * @throws org.multiverse.api.exceptions.InvisibleCheckedException
     *                                  if the closure throws a checked exception.
     */
    public static  int atomic(TxnIntClosure closure){
        return defaultTxnExecutor.atomic(closure);
    }

   /**
    * Executes the closure transactionally on the GlobalStmInstance using the default TxnExecutor. If a
    * Transaction already is active on the TxnThreadLocal, this transaction will lift on that
    * transaction (so the propagation level is Requires) and will not commit this transaction.
    *
    * <p>If you want to get most out of performance, it is best to make use of a customized {@link TxnExecutor} instead
    * of relying on the default TxnExecutor that will always provide the most expensive transaction available.
    *
    * @param closure The {@link TxnIntClosure} to execute.
    * @return the result of the execution
    * @throws NullPointerException if closure is null.
    * @throws Exception is the closure throws an Exception
    */
   public static  int atomicChecked(TxnIntClosure closure) throws Exception{
       return defaultTxnExecutor.atomicChecked(closure);
   }

   /**
    * Executes the either block, or in case of a retry, the orelse block is executed.
    *
    * If in the execution of the closure a checked exception is thrown, the exception
    * is wrapped in a InvisibleCheckedException. The original exception can be retrieved by calling the
    * getCause method.
    *
    * @param either the either block
    * @param orelse the orelse block.
    * @return the result of the execution.
    * @throws NullPointerException if closure is null.
    * @throws org.multiverse.api.exceptions.InvisibleCheckedException if a checked exception is thrown by the closure.
    */
    public static  int atomic(TxnIntClosure either, TxnIntClosure orelse){
        return orelseBlock.execute(either,orelse);
    }

    /**
    * Executes the either block, or in case of a retry, the orelse block is executed.
    *
    * @param either the either block
    * @param orelse the orelse block
    * @return the result of the execution.
    * @throws NullPointerException if closure is null.
    * @throws Exception if the execute call fails.
    */
    public static  int atomicChecked(TxnIntClosure either, TxnIntClosure orelse)throws Exception{
        return orelseBlock.executeChecked(either,orelse);
    }

    /**
     * Executes the closure transactionally on the GlobalStmInstance using the default TxnExecutor. If a
     * Transaction already is active on the TxnThreadLocal, this transaction will lift on that
     * transaction (so the propagation level is Requires) and will not commit this transaction.
     *
     * <p>This method doesn't throw a checked exception, but if the closure does, it is wrapped inside an
     * InvisibleCheckedException.
     *
     * <p>If you want to get most out of performance, it is best to make use of a customized {@link TxnExecutor} instead
     * of relying on the default TxnExecutor that will always provide the most expensive transaction available.
     *
     * @param closure The closure {@link TxnLongClosure} to execute.
     * @return the result of the execution
     * @throws NullPointerException if closure is null.
     * @throws org.multiverse.api.exceptions.InvisibleCheckedException
     *                                  if the closure throws a checked exception.
     */
    public static  long atomic(TxnLongClosure closure){
        return defaultTxnExecutor.atomic(closure);
    }

   /**
    * Executes the closure transactionally on the GlobalStmInstance using the default TxnExecutor. If a
    * Transaction already is active on the TxnThreadLocal, this transaction will lift on that
    * transaction (so the propagation level is Requires) and will not commit this transaction.
    *
    * <p>If you want to get most out of performance, it is best to make use of a customized {@link TxnExecutor} instead
    * of relying on the default TxnExecutor that will always provide the most expensive transaction available.
    *
    * @param closure The {@link TxnLongClosure} to execute.
    * @return the result of the execution
    * @throws NullPointerException if closure is null.
    * @throws Exception is the closure throws an Exception
    */
   public static  long atomicChecked(TxnLongClosure closure) throws Exception{
       return defaultTxnExecutor.atomicChecked(closure);
   }

   /**
    * Executes the either block, or in case of a retry, the orelse block is executed.
    *
    * If in the execution of the closure a checked exception is thrown, the exception
    * is wrapped in a InvisibleCheckedException. The original exception can be retrieved by calling the
    * getCause method.
    *
    * @param either the either block
    * @param orelse the orelse block.
    * @return the result of the execution.
    * @throws NullPointerException if closure is null.
    * @throws org.multiverse.api.exceptions.InvisibleCheckedException if a checked exception is thrown by the closure.
    */
    public static  long atomic(TxnLongClosure either, TxnLongClosure orelse){
        return orelseBlock.execute(either,orelse);
    }

    /**
    * Executes the either block, or in case of a retry, the orelse block is executed.
    *
    * @param either the either block
    * @param orelse the orelse block
    * @return the result of the execution.
    * @throws NullPointerException if closure is null.
    * @throws Exception if the execute call fails.
    */
    public static  long atomicChecked(TxnLongClosure either, TxnLongClosure orelse)throws Exception{
        return orelseBlock.executeChecked(either,orelse);
    }

    /**
     * Executes the closure transactionally on the GlobalStmInstance using the default TxnExecutor. If a
     * Transaction already is active on the TxnThreadLocal, this transaction will lift on that
     * transaction (so the propagation level is Requires) and will not commit this transaction.
     *
     * <p>This method doesn't throw a checked exception, but if the closure does, it is wrapped inside an
     * InvisibleCheckedException.
     *
     * <p>If you want to get most out of performance, it is best to make use of a customized {@link TxnExecutor} instead
     * of relying on the default TxnExecutor that will always provide the most expensive transaction available.
     *
     * @param closure The closure {@link TxnDoubleClosure} to execute.
     * @return the result of the execution
     * @throws NullPointerException if closure is null.
     * @throws org.multiverse.api.exceptions.InvisibleCheckedException
     *                                  if the closure throws a checked exception.
     */
    public static  double atomic(TxnDoubleClosure closure){
        return defaultTxnExecutor.atomic(closure);
    }

   /**
    * Executes the closure transactionally on the GlobalStmInstance using the default TxnExecutor. If a
    * Transaction already is active on the TxnThreadLocal, this transaction will lift on that
    * transaction (so the propagation level is Requires) and will not commit this transaction.
    *
    * <p>If you want to get most out of performance, it is best to make use of a customized {@link TxnExecutor} instead
    * of relying on the default TxnExecutor that will always provide the most expensive transaction available.
    *
    * @param closure The {@link TxnDoubleClosure} to execute.
    * @return the result of the execution
    * @throws NullPointerException if closure is null.
    * @throws Exception is the closure throws an Exception
    */
   public static  double atomicChecked(TxnDoubleClosure closure) throws Exception{
       return defaultTxnExecutor.atomicChecked(closure);
   }

   /**
    * Executes the either block, or in case of a retry, the orelse block is executed.
    *
    * If in the execution of the closure a checked exception is thrown, the exception
    * is wrapped in a InvisibleCheckedException. The original exception can be retrieved by calling the
    * getCause method.
    *
    * @param either the either block
    * @param orelse the orelse block.
    * @return the result of the execution.
    * @throws NullPointerException if closure is null.
    * @throws org.multiverse.api.exceptions.InvisibleCheckedException if a checked exception is thrown by the closure.
    */
    public static  double atomic(TxnDoubleClosure either, TxnDoubleClosure orelse){
        return orelseBlock.execute(either,orelse);
    }

    /**
    * Executes the either block, or in case of a retry, the orelse block is executed.
    *
    * @param either the either block
    * @param orelse the orelse block
    * @return the result of the execution.
    * @throws NullPointerException if closure is null.
    * @throws Exception if the execute call fails.
    */
    public static  double atomicChecked(TxnDoubleClosure either, TxnDoubleClosure orelse)throws Exception{
        return orelseBlock.executeChecked(either,orelse);
    }

    /**
     * Executes the closure transactionally on the GlobalStmInstance using the default TxnExecutor. If a
     * Transaction already is active on the TxnThreadLocal, this transaction will lift on that
     * transaction (so the propagation level is Requires) and will not commit this transaction.
     *
     * <p>This method doesn't throw a checked exception, but if the closure does, it is wrapped inside an
     * InvisibleCheckedException.
     *
     * <p>If you want to get most out of performance, it is best to make use of a customized {@link TxnExecutor} instead
     * of relying on the default TxnExecutor that will always provide the most expensive transaction available.
     *
     * @param closure The closure {@link TxnBooleanClosure} to execute.
     * @return the result of the execution
     * @throws NullPointerException if closure is null.
     * @throws org.multiverse.api.exceptions.InvisibleCheckedException
     *                                  if the closure throws a checked exception.
     */
    public static  boolean atomic(TxnBooleanClosure closure){
        return defaultTxnExecutor.atomic(closure);
    }

   /**
    * Executes the closure transactionally on the GlobalStmInstance using the default TxnExecutor. If a
    * Transaction already is active on the TxnThreadLocal, this transaction will lift on that
    * transaction (so the propagation level is Requires) and will not commit this transaction.
    *
    * <p>If you want to get most out of performance, it is best to make use of a customized {@link TxnExecutor} instead
    * of relying on the default TxnExecutor that will always provide the most expensive transaction available.
    *
    * @param closure The {@link TxnBooleanClosure} to execute.
    * @return the result of the execution
    * @throws NullPointerException if closure is null.
    * @throws Exception is the closure throws an Exception
    */
   public static  boolean atomicChecked(TxnBooleanClosure closure) throws Exception{
       return defaultTxnExecutor.atomicChecked(closure);
   }

   /**
    * Executes the either block, or in case of a retry, the orelse block is executed.
    *
    * If in the execution of the closure a checked exception is thrown, the exception
    * is wrapped in a InvisibleCheckedException. The original exception can be retrieved by calling the
    * getCause method.
    *
    * @param either the either block
    * @param orelse the orelse block.
    * @return the result of the execution.
    * @throws NullPointerException if closure is null.
    * @throws org.multiverse.api.exceptions.InvisibleCheckedException if a checked exception is thrown by the closure.
    */
    public static  boolean atomic(TxnBooleanClosure either, TxnBooleanClosure orelse){
        return orelseBlock.execute(either,orelse);
    }

    /**
    * Executes the either block, or in case of a retry, the orelse block is executed.
    *
    * @param either the either block
    * @param orelse the orelse block
    * @return the result of the execution.
    * @throws NullPointerException if closure is null.
    * @throws Exception if the execute call fails.
    */
    public static  boolean atomicChecked(TxnBooleanClosure either, TxnBooleanClosure orelse)throws Exception{
        return orelseBlock.executeChecked(either,orelse);
    }

    /**
     * Executes the closure transactionally on the GlobalStmInstance using the default TxnExecutor. If a
     * Transaction already is active on the TxnThreadLocal, this transaction will lift on that
     * transaction (so the propagation level is Requires) and will not commit this transaction.
     *
     * <p>This method doesn't throw a checked exception, but if the closure does, it is wrapped inside an
     * InvisibleCheckedException.
     *
     * <p>If you want to get most out of performance, it is best to make use of a customized {@link TxnExecutor} instead
     * of relying on the default TxnExecutor that will always provide the most expensive transaction available.
     *
     * @param closure The closure {@link TxnVoidClosure} to execute.
     * @throws NullPointerException if closure is null.
     * @throws org.multiverse.api.exceptions.InvisibleCheckedException
     *                                  if the closure throws a checked exception.
     */
    public static  void atomic(TxnVoidClosure closure){
        defaultTxnExecutor.atomic(closure);
    }

   /**
    * Executes the closure transactionally on the GlobalStmInstance using the default TxnExecutor. If a
    * Transaction already is active on the TxnThreadLocal, this transaction will lift on that
    * transaction (so the propagation level is Requires) and will not commit this transaction.
    *
    * <p>If you want to get most out of performance, it is best to make use of a customized {@link TxnExecutor} instead
    * of relying on the default TxnExecutor that will always provide the most expensive transaction available.
    *
    * @param closure The {@link TxnVoidClosure} to execute.
    * @throws NullPointerException if closure is null.
    * @throws Exception is the closure throws an Exception
    */
   public static  void atomicChecked(TxnVoidClosure closure) throws Exception{
       defaultTxnExecutor.atomicChecked(closure);
   }

   /**
    * Executes the either block, or in case of a retry, the orelse block is executed.
    *
    * If in the execution of the closure a checked exception is thrown, the exception
    * is wrapped in a InvisibleCheckedException. The original exception can be retrieved by calling the
    * getCause method.
    *
    * @param either the either block
    * @param orelse the orelse block.
    * @throws NullPointerException if closure is null.
    * @throws org.multiverse.api.exceptions.InvisibleCheckedException if a checked exception is thrown by the closure.
    */
    public static  void atomic(TxnVoidClosure either, TxnVoidClosure orelse){
        orelseBlock.execute(either,orelse);
    }

    /**
    * Executes the either block, or in case of a retry, the orelse block is executed.
    *
    * @param either the either block
    * @param orelse the orelse block
    * @throws NullPointerException if closure is null.
    * @throws Exception if the execute call fails.
    */
    public static  void atomicChecked(TxnVoidClosure either, TxnVoidClosure orelse)throws Exception{
        orelseBlock.executeChecked(either,orelse);
    }

    /**
     * Creates a committed {@link IntRef} with the provided value using the {@link GlobalStmInstance}.
     *
     * @param value the initial value of the IntRef
     * @return the created IntRef.
     */
    public static IntRef newIntRef(int value) {
        return refFactory.newIntRef(value);
    }

    /**
     * Creates a committed {@link IntRef} with 0 as initial value using the {@link GlobalStmInstance}.
     *
     * @return the created IntRef.
     */
    public static IntRef newIntRef() {
        return refFactory.newIntRef(0);
    }

    /**
     * Creates a committed {@link LongRef} with 0 as initial value using the {@link GlobalStmInstance}.
     *
     * @return the created LongRef.
     */
    public static LongRef newLongRef() {
        return refFactory.newLongRef(0);
    }

    /**
     * Creates a committed {@link LongRef} with the provided value using the {@link GlobalStmInstance}.
     *
     * @param value the initial value of the LongRef.
     * @return the created LongRef.
     */
    public static LongRef newLongRef(long value) {
        return refFactory.newLongRef(value);
    }

    /**
     * Creates a committed {@link DoubleRef} with 0 as initial value using the {@link GlobalStmInstance}.
     *
     * @return the created DoubleRef.
     */
    public static DoubleRef newDoubleRef() {
        return refFactory.newDoubleRef(0);
    }

    /**
     * Creates a committed {@link DoubleRef} with the provided value using the {@link GlobalStmInstance}.
     *
     * @param value the initial value.
     * @return the created DoubleRef.
     */
    public static DoubleRef newDoubleRef(double value) {
        return refFactory.newDoubleRef(value);
    }

    /**
     * Creates a committed {@link BooleanRef} with false as initial value using the {@link GlobalStmInstance}.
     *
     * @return the created BooleanRef.
     */
    public static BooleanRef newBooleanRef() {
        return refFactory.newBooleanRef(false);
    }

    /**
     * Creates a committed {@link BooleanRef} with the provided value using the {@link GlobalStmInstance}.
     *
     * @param value the initial value
     * @return the created BooleanRef.
     */
    public static BooleanRef newBooleanRef(boolean value) {
        return refFactory.newBooleanRef(value);
    }

    /**
     * Creates a committed {@link Ref} with null as initial value using the {@link GlobalStmInstance}.
     *
     * @param <E> the type of the Ref.
     * @return the created Ref.
     */
    public static <E> Ref<E> newRef() {
        return refFactory.newRef(null);
    }

    /**
     * Creates a committed {@link Ref} with the provided value using the {@link GlobalStmInstance}.
     *
     * @param value the initial value of the Ref.
     * @param <E>   the type of the Ref.
     * @return the created Ref.
     */
    public static <E> Ref<E> newRef(E value) {
        return refFactory.newRef(value);
    }

    /**
     * Does a retry. This behavior is needed for blocking transactions; transaction that wait for a state change
     * to happen on certain datastructures, e.g. an item to come available on a transactional blocking queue.
     *
     * <p>Under the hood the retry throws an Retry that will be caught up the callstack
     * (by the {@link TxnExecutor} for example). The Retry should not be caught by user code in almost all cases.
     */
    public static void retry() {
        Txn txn = getRequiredThreadLocalTxn();
        txn.retry();
    }

    /**
     * Prepares the Transaction in the TxnThreadLocal transaction.
     *
     * <p>For more information see {@link Txn#prepare()}.
     *
     * @throws TxnMandatoryException if no active transaction is found.
     * @throws IllegalTransactionStateException if the active transaction is not in the correct
     *                                           state for this operation.
     * @throws ControlFlowError
     */
    public static void prepare() {
        Txn txn = getRequiredThreadLocalTxn();
        txn.prepare();
    }

    /**
     * Aborts the Transaction in the TxnThreadLocal transaction.
     *
     * <p>For more information see {@link Txn#abort()}.
     *
     * @throws TxnMandatoryException if no active transaction is found.
     * @throws IllegalTransactionStateException if the active transaction is not in the correct
     *                                           state for this operation.
     * @throws ControlFlowError
     */
    public static void abort() {
        Txn txn = getRequiredThreadLocalTxn();
        txn.abort();
    }

    /**
     * Commits the Transaction in the TxnThreadLocal transaction.
     *
     * <p>For more information see {@link Txn#commit()}.
     *
     * @throws TxnMandatoryException if no active transaction is found.
     * @throws IllegalTransactionStateException if the active transaction is not in the correct
     * state for this operation.
     * @throws ControlFlowError
     */
     public static void commit() {
         Txn txn = getRequiredThreadLocalTxn();
         txn.commit();
     }

     /**
      * Scheduled an deferred or compensating task on the {@link Txn} in the TxnThreadLocal. This task is
      * executed after the transaction commits or aborts.
      *
      * @param task the deferred task to execute.
      * @throws NullPointerException if task is null.
      * @throws org.multiverse.api.exceptions.TxnMandatoryException
                        if no transaction is set on the {@link org.multiverse.api.TxnThreadLocal}.
      * @throws org.multiverse.api.exceptions.IllegalTxnStateException
      *                 if the transaction is not in the correct state to accept a compensating or deferred task.
      */
      public static void scheduleCompensatingOrDeferredTask(final Runnable task) {
            if (task == null) {
                throw new NullPointerException();
            }

            Txn txn = getRequiredThreadLocalTxn();
            txn.register(new TxnListener() {
                @Override
                public void notify(Txn txn, TxnEvent event) {
                    if (event == TxnEvent.PostCommit || event == TxnEvent.PostAbort) {
                        task.run();
                    }
                }
            });
       }

       /**
        * Scheduled an deferred task on the {@link Txn} in the {@link TxnThreadLocal}. This task is executed after
        * the transaction commits and one of the use cases is starting transactions.
        *
        * @param task the deferred task to execute.
        * @throws NullPointerException if task is null.
        * @throws org.multiverse.api.exceptions.TxnMandatoryException
        *                   if no transaction is set on the {@link org.multiverse.api.TxnThreadLocal}.
        * @throws org.multiverse.api.exceptions.IllegalTxnStateException
        *                   if the transaction is not in the correct state to accept a deferred task.
        */
       public static void scheduleDeferredTask(final Runnable task) {
            if (task == null) {
                throw new NullPointerException();
            }

            Txn txn = getRequiredThreadLocalTxn();
            txn.register(new TxnListener() {
                @Override
                public void notify(Txn txn, TxnEvent event) {
                    if (event == TxnEvent.PostCommit) {
                        task.run();
                    }
                }
            });
       }

       /**
        * Scheduled a compensating task on the {@link Txn} in the {@link TxnThreadLocal}. This task is executed after
        * the transaction aborts and one of the use cases is cleaning up non transaction resources like the file system.
        *
        * @param task the deferred task to execute.
        * @throws NullPointerException if task is null.
        * @throws org.multiverse.api.exceptions.TxnMandatoryException
        *                       if no transaction is set on the {@link org.multiverse.api.TxnThreadLocal}.
        * @throws org.multiverse.api.exceptions.IllegalTxnStateException
        *                       if the transaction is not in the correct state to accept a compensating task.
        */
       public static void scheduleCompensatingTask(final Runnable task) {
            if (task == null) {
                throw new NullPointerException();
            }

            Txn txn = getRequiredThreadLocalTxn();
            txn.register(new TxnListener() {
                @Override
                public void notify(Txn txn, TxnEvent event) {
                    if (event == TxnEvent.PostAbort) {
                        task.run();
                    }
                }
            });
       }

       //we don want instances
       private StmUtils() {
       }
}
