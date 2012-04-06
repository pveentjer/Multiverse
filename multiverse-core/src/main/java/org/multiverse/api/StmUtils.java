package org.multiverse.api;

import org.multiverse.api.callables.*;
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

    private final static TxnRefFactory refFactory
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
    public static <E> TxnList<E> newTxnLinkedList(){
        return txnCollectionsFactory.newLinkedList();
    }

    /**
     * Creates a new committed unbound TxnStack.
     *
     * @return the created TxnStack.
     */
    public static <E> TxnStack<E> newTxnStack(){
        return txnCollectionsFactory.newStack();
    }

    /**
     * Creates a new committed bound TxnStack.
     *
     * @param capacity the maximum capacity of the stack. Integer.MAX_VALUE indicates that there is no bound.
     * @return the create TxnStack
     * @throws IllegalArgumentException if capacity smaller than 0.
     */
    public static <E> TxnStack<E> newTxnStack(int capacity){
        return txnCollectionsFactory.newStack(capacity);
    }

    /**
     * Creates a new committed unbound TxnQueue.
     *
     * @return the created TxnQueue.
     */
    public static <E> TxnQueue<E> newTxnQueue(){
        return txnCollectionsFactory.newQueue();
    }

    /**
     * Creates a new committed bound TxnQueue.
     *
     * @param capacity the maximum capacity of the queue. Integer.MAX_VALUE indicates that there is no bound.
     * @return the created TxnQueue
     * @throws IllegalArgumentException if capacity smaller than 0.
     */
    public static <E> TxnQueue<E> newTxnQueue(int capacity){
        return txnCollectionsFactory.newQueue(capacity);
    }

    /**
     * Creates a new committed unbound TxnDeque.
     *
     * @return the created TxnDeque
     */
    public static <E> TxnDeque<E> newTxnDeque(){
        return txnCollectionsFactory.newDeque();
    }

    /**
     * Creates a new committed bound TxnDeque.
     *
     * @param capacity the maximum capacity of the deque. Integer.MAX_VALUE indicates that there is no bound.
     * @return the created TxnDeque.
     * @throws IllegalArgumentException if capacity is smaller than 0.
     */
    public static <E> TxnDeque<E> newTxnDeque(int capacity){
        return txnCollectionsFactory.newDeque(capacity);
    }

    /**
     * Creates a new committed TxnSet that is based on a 'hashtable'.
     *
     * @return the created TxnSet.
     */
    public static <E> TxnSet<E> newTxnHashSet(){
        return txnCollectionsFactory.newHashSet();
    }

    /**
     * Creates a new committed TxnMap.
     *
     * @return the created TxnMap
     */
    public static <K, V> TxnMap<K, V> newTxnHashMap(){
        return txnCollectionsFactory.newHashMap();
    }

    /**
     * Executes the callable transactionally on the GlobalStmInstance using the default TxnExecutor. If a
     * Transaction already is active on the TxnThreadLocal, this transaction will lift on that
     * transaction (so the propagation level is Requires) and will not commit this transaction.
     *
     * <p>This method doesn't throw a checked exception, but if the callable does, it is wrapped inside an
     * InvisibleCheckedException.
     *
     * <p>If you want to get most out of performance, it is best to make use of a customized {@link TxnExecutor} instead
     * of relying on the default TxnExecutor that will always provide the most expensive transaction available.
     *
     * @param callable The callable {@link TxnCallable} to execute.
     * @return the result of the execution
     * @throws NullPointerException if callable is null.
     * @throws org.multiverse.api.exceptions.InvisibleCheckedException
     *                                  if the callable throws a checked exception.
     */
    public static <E> E atomic(TxnCallable<E> callable){
        return defaultTxnExecutor.atomic(callable);
    }

   /**
    * Executes the callable transactionally on the GlobalStmInstance using the default TxnExecutor. If a
    * Transaction already is active on the TxnThreadLocal, this transaction will lift on that
    * transaction (so the propagation level is Requires) and will not commit this transaction.
    *
    * <p>If you want to get most out of performance, it is best to make use of a customized {@link TxnExecutor} instead
    * of relying on the default TxnExecutor that will always provide the most expensive transaction available.
    *
    * @param callable The {@link TxnCallable} to execute.
    * @return the result of the execution
    * @throws NullPointerException if callable is null.
    * @throws Exception is the callable throws an Exception
    */
   public static <E> E atomicChecked(TxnCallable<E> callable) throws Exception{
       return defaultTxnExecutor.atomicChecked(callable);
   }

   /**
    * Executes the either block, or in case of a retry, the orelse block is executed.
    *
    * If in the execution of the callable a checked exception is thrown, the exception
    * is wrapped in a InvisibleCheckedException. The original exception can be retrieved by calling the
    * getCause method.
    *
    * @param either the either block
    * @param orelse the orelse block.
    * @return the result of the execution.
    * @throws NullPointerException if callable is null.
    * @throws org.multiverse.api.exceptions.InvisibleCheckedException if a checked exception is thrown by the callable.
    */
    public static <E> E atomic(TxnCallable<E> either, TxnCallable<E> orelse){
        return orelseBlock.execute(either,orelse);
    }

    /**
    * Executes the either block, or in case of a retry, the orelse block is executed.
    *
    * @param either the either block
    * @param orelse the orelse block
    * @return the result of the execution.
    * @throws NullPointerException if callable is null.
    * @throws Exception if the execute call fails.
    */
    public static <E> E atomicChecked(TxnCallable<E> either, TxnCallable<E> orelse)throws Exception{
        return orelseBlock.executeChecked(either,orelse);
    }

    /**
     * Executes the callable transactionally on the GlobalStmInstance using the default TxnExecutor. If a
     * Transaction already is active on the TxnThreadLocal, this transaction will lift on that
     * transaction (so the propagation level is Requires) and will not commit this transaction.
     *
     * <p>This method doesn't throw a checked exception, but if the callable does, it is wrapped inside an
     * InvisibleCheckedException.
     *
     * <p>If you want to get most out of performance, it is best to make use of a customized {@link TxnExecutor} instead
     * of relying on the default TxnExecutor that will always provide the most expensive transaction available.
     *
     * @param callable The callable {@link TxnIntCallable} to execute.
     * @return the result of the execution
     * @throws NullPointerException if callable is null.
     * @throws org.multiverse.api.exceptions.InvisibleCheckedException
     *                                  if the callable throws a checked exception.
     */
    public static  int atomic(TxnIntCallable callable){
        return defaultTxnExecutor.atomic(callable);
    }

   /**
    * Executes the callable transactionally on the GlobalStmInstance using the default TxnExecutor. If a
    * Transaction already is active on the TxnThreadLocal, this transaction will lift on that
    * transaction (so the propagation level is Requires) and will not commit this transaction.
    *
    * <p>If you want to get most out of performance, it is best to make use of a customized {@link TxnExecutor} instead
    * of relying on the default TxnExecutor that will always provide the most expensive transaction available.
    *
    * @param callable The {@link TxnIntCallable} to execute.
    * @return the result of the execution
    * @throws NullPointerException if callable is null.
    * @throws Exception is the callable throws an Exception
    */
   public static  int atomicChecked(TxnIntCallable callable) throws Exception{
       return defaultTxnExecutor.atomicChecked(callable);
   }

   /**
    * Executes the either block, or in case of a retry, the orelse block is executed.
    *
    * If in the execution of the callable a checked exception is thrown, the exception
    * is wrapped in a InvisibleCheckedException. The original exception can be retrieved by calling the
    * getCause method.
    *
    * @param either the either block
    * @param orelse the orelse block.
    * @return the result of the execution.
    * @throws NullPointerException if callable is null.
    * @throws org.multiverse.api.exceptions.InvisibleCheckedException if a checked exception is thrown by the callable.
    */
    public static  int atomic(TxnIntCallable either, TxnIntCallable orelse){
        return orelseBlock.execute(either,orelse);
    }

    /**
    * Executes the either block, or in case of a retry, the orelse block is executed.
    *
    * @param either the either block
    * @param orelse the orelse block
    * @return the result of the execution.
    * @throws NullPointerException if callable is null.
    * @throws Exception if the execute call fails.
    */
    public static  int atomicChecked(TxnIntCallable either, TxnIntCallable orelse)throws Exception{
        return orelseBlock.executeChecked(either,orelse);
    }

    /**
     * Executes the callable transactionally on the GlobalStmInstance using the default TxnExecutor. If a
     * Transaction already is active on the TxnThreadLocal, this transaction will lift on that
     * transaction (so the propagation level is Requires) and will not commit this transaction.
     *
     * <p>This method doesn't throw a checked exception, but if the callable does, it is wrapped inside an
     * InvisibleCheckedException.
     *
     * <p>If you want to get most out of performance, it is best to make use of a customized {@link TxnExecutor} instead
     * of relying on the default TxnExecutor that will always provide the most expensive transaction available.
     *
     * @param callable The callable {@link TxnLongCallable} to execute.
     * @return the result of the execution
     * @throws NullPointerException if callable is null.
     * @throws org.multiverse.api.exceptions.InvisibleCheckedException
     *                                  if the callable throws a checked exception.
     */
    public static  long atomic(TxnLongCallable callable){
        return defaultTxnExecutor.atomic(callable);
    }

   /**
    * Executes the callable transactionally on the GlobalStmInstance using the default TxnExecutor. If a
    * Transaction already is active on the TxnThreadLocal, this transaction will lift on that
    * transaction (so the propagation level is Requires) and will not commit this transaction.
    *
    * <p>If you want to get most out of performance, it is best to make use of a customized {@link TxnExecutor} instead
    * of relying on the default TxnExecutor that will always provide the most expensive transaction available.
    *
    * @param callable The {@link TxnLongCallable} to execute.
    * @return the result of the execution
    * @throws NullPointerException if callable is null.
    * @throws Exception is the callable throws an Exception
    */
   public static  long atomicChecked(TxnLongCallable callable) throws Exception{
       return defaultTxnExecutor.atomicChecked(callable);
   }

   /**
    * Executes the either block, or in case of a retry, the orelse block is executed.
    *
    * If in the execution of the callable a checked exception is thrown, the exception
    * is wrapped in a InvisibleCheckedException. The original exception can be retrieved by calling the
    * getCause method.
    *
    * @param either the either block
    * @param orelse the orelse block.
    * @return the result of the execution.
    * @throws NullPointerException if callable is null.
    * @throws org.multiverse.api.exceptions.InvisibleCheckedException if a checked exception is thrown by the callable.
    */
    public static  long atomic(TxnLongCallable either, TxnLongCallable orelse){
        return orelseBlock.execute(either,orelse);
    }

    /**
    * Executes the either block, or in case of a retry, the orelse block is executed.
    *
    * @param either the either block
    * @param orelse the orelse block
    * @return the result of the execution.
    * @throws NullPointerException if callable is null.
    * @throws Exception if the execute call fails.
    */
    public static  long atomicChecked(TxnLongCallable either, TxnLongCallable orelse)throws Exception{
        return orelseBlock.executeChecked(either,orelse);
    }

    /**
     * Executes the callable transactionally on the GlobalStmInstance using the default TxnExecutor. If a
     * Transaction already is active on the TxnThreadLocal, this transaction will lift on that
     * transaction (so the propagation level is Requires) and will not commit this transaction.
     *
     * <p>This method doesn't throw a checked exception, but if the callable does, it is wrapped inside an
     * InvisibleCheckedException.
     *
     * <p>If you want to get most out of performance, it is best to make use of a customized {@link TxnExecutor} instead
     * of relying on the default TxnExecutor that will always provide the most expensive transaction available.
     *
     * @param callable The callable {@link TxnDoubleCallable} to execute.
     * @return the result of the execution
     * @throws NullPointerException if callable is null.
     * @throws org.multiverse.api.exceptions.InvisibleCheckedException
     *                                  if the callable throws a checked exception.
     */
    public static  double atomic(TxnDoubleCallable callable){
        return defaultTxnExecutor.atomic(callable);
    }

   /**
    * Executes the callable transactionally on the GlobalStmInstance using the default TxnExecutor. If a
    * Transaction already is active on the TxnThreadLocal, this transaction will lift on that
    * transaction (so the propagation level is Requires) and will not commit this transaction.
    *
    * <p>If you want to get most out of performance, it is best to make use of a customized {@link TxnExecutor} instead
    * of relying on the default TxnExecutor that will always provide the most expensive transaction available.
    *
    * @param callable The {@link TxnDoubleCallable} to execute.
    * @return the result of the execution
    * @throws NullPointerException if callable is null.
    * @throws Exception is the callable throws an Exception
    */
   public static  double atomicChecked(TxnDoubleCallable callable) throws Exception{
       return defaultTxnExecutor.atomicChecked(callable);
   }

   /**
    * Executes the either block, or in case of a retry, the orelse block is executed.
    *
    * If in the execution of the callable a checked exception is thrown, the exception
    * is wrapped in a InvisibleCheckedException. The original exception can be retrieved by calling the
    * getCause method.
    *
    * @param either the either block
    * @param orelse the orelse block.
    * @return the result of the execution.
    * @throws NullPointerException if callable is null.
    * @throws org.multiverse.api.exceptions.InvisibleCheckedException if a checked exception is thrown by the callable.
    */
    public static  double atomic(TxnDoubleCallable either, TxnDoubleCallable orelse){
        return orelseBlock.execute(either,orelse);
    }

    /**
    * Executes the either block, or in case of a retry, the orelse block is executed.
    *
    * @param either the either block
    * @param orelse the orelse block
    * @return the result of the execution.
    * @throws NullPointerException if callable is null.
    * @throws Exception if the execute call fails.
    */
    public static  double atomicChecked(TxnDoubleCallable either, TxnDoubleCallable orelse)throws Exception{
        return orelseBlock.executeChecked(either,orelse);
    }

    /**
     * Executes the callable transactionally on the GlobalStmInstance using the default TxnExecutor. If a
     * Transaction already is active on the TxnThreadLocal, this transaction will lift on that
     * transaction (so the propagation level is Requires) and will not commit this transaction.
     *
     * <p>This method doesn't throw a checked exception, but if the callable does, it is wrapped inside an
     * InvisibleCheckedException.
     *
     * <p>If you want to get most out of performance, it is best to make use of a customized {@link TxnExecutor} instead
     * of relying on the default TxnExecutor that will always provide the most expensive transaction available.
     *
     * @param callable The callable {@link TxnBooleanCallable} to execute.
     * @return the result of the execution
     * @throws NullPointerException if callable is null.
     * @throws org.multiverse.api.exceptions.InvisibleCheckedException
     *                                  if the callable throws a checked exception.
     */
    public static  boolean atomic(TxnBooleanCallable callable){
        return defaultTxnExecutor.atomic(callable);
    }

   /**
    * Executes the callable transactionally on the GlobalStmInstance using the default TxnExecutor. If a
    * Transaction already is active on the TxnThreadLocal, this transaction will lift on that
    * transaction (so the propagation level is Requires) and will not commit this transaction.
    *
    * <p>If you want to get most out of performance, it is best to make use of a customized {@link TxnExecutor} instead
    * of relying on the default TxnExecutor that will always provide the most expensive transaction available.
    *
    * @param callable The {@link TxnBooleanCallable} to execute.
    * @return the result of the execution
    * @throws NullPointerException if callable is null.
    * @throws Exception is the callable throws an Exception
    */
   public static  boolean atomicChecked(TxnBooleanCallable callable) throws Exception{
       return defaultTxnExecutor.atomicChecked(callable);
   }

   /**
    * Executes the either block, or in case of a retry, the orelse block is executed.
    *
    * If in the execution of the callable a checked exception is thrown, the exception
    * is wrapped in a InvisibleCheckedException. The original exception can be retrieved by calling the
    * getCause method.
    *
    * @param either the either block
    * @param orelse the orelse block.
    * @return the result of the execution.
    * @throws NullPointerException if callable is null.
    * @throws org.multiverse.api.exceptions.InvisibleCheckedException if a checked exception is thrown by the callable.
    */
    public static  boolean atomic(TxnBooleanCallable either, TxnBooleanCallable orelse){
        return orelseBlock.execute(either,orelse);
    }

    /**
    * Executes the either block, or in case of a retry, the orelse block is executed.
    *
    * @param either the either block
    * @param orelse the orelse block
    * @return the result of the execution.
    * @throws NullPointerException if callable is null.
    * @throws Exception if the execute call fails.
    */
    public static  boolean atomicChecked(TxnBooleanCallable either, TxnBooleanCallable orelse)throws Exception{
        return orelseBlock.executeChecked(either,orelse);
    }

    /**
     * Executes the callable transactionally on the GlobalStmInstance using the default TxnExecutor. If a
     * Transaction already is active on the TxnThreadLocal, this transaction will lift on that
     * transaction (so the propagation level is Requires) and will not commit this transaction.
     *
     * <p>This method doesn't throw a checked exception, but if the callable does, it is wrapped inside an
     * InvisibleCheckedException.
     *
     * <p>If you want to get most out of performance, it is best to make use of a customized {@link TxnExecutor} instead
     * of relying on the default TxnExecutor that will always provide the most expensive transaction available.
     *
     * @param callable The callable {@link TxnVoidCallable} to execute.
     * @throws NullPointerException if callable is null.
     * @throws org.multiverse.api.exceptions.InvisibleCheckedException
     *                                  if the callable throws a checked exception.
     */
    public static  void atomic(TxnVoidCallable callable){
        defaultTxnExecutor.atomic(callable);
    }

   /**
    * Executes the callable transactionally on the GlobalStmInstance using the default TxnExecutor. If a
    * Transaction already is active on the TxnThreadLocal, this transaction will lift on that
    * transaction (so the propagation level is Requires) and will not commit this transaction.
    *
    * <p>If you want to get most out of performance, it is best to make use of a customized {@link TxnExecutor} instead
    * of relying on the default TxnExecutor that will always provide the most expensive transaction available.
    *
    * @param callable The {@link TxnVoidCallable} to execute.
    * @throws NullPointerException if callable is null.
    * @throws Exception is the callable throws an Exception
    */
   public static  void atomicChecked(TxnVoidCallable callable) throws Exception{
       defaultTxnExecutor.atomicChecked(callable);
   }

   /**
    * Executes the either block, or in case of a retry, the orelse block is executed.
    *
    * If in the execution of the callable a checked exception is thrown, the exception
    * is wrapped in a InvisibleCheckedException. The original exception can be retrieved by calling the
    * getCause method.
    *
    * @param either the either block
    * @param orelse the orelse block.
    * @throws NullPointerException if callable is null.
    * @throws org.multiverse.api.exceptions.InvisibleCheckedException if a checked exception is thrown by the callable.
    */
    public static  void atomic(TxnVoidCallable either, TxnVoidCallable orelse){
        orelseBlock.execute(either,orelse);
    }

    /**
    * Executes the either block, or in case of a retry, the orelse block is executed.
    *
    * @param either the either block
    * @param orelse the orelse block
    * @throws NullPointerException if callable is null.
    * @throws Exception if the execute call fails.
    */
    public static  void atomicChecked(TxnVoidCallable either, TxnVoidCallable orelse)throws Exception{
        orelseBlock.executeChecked(either,orelse);
    }

    /**
     * Creates a committed {@link TxnInteger} with the provided value using the {@link GlobalStmInstance}.
     *
     * @param value the initial value of the TnxInteger
     * @return the created TnxInteger.
     */
    public static TxnInteger newTxnInteger(int value) {
        return refFactory.newTxnInteger(value);
    }

    /**
     * Creates a committed {@link TxnInteger} with 0 as initial value using the {@link GlobalStmInstance}.
     *
     * @return the created TxnInteger.
     */
    public static TxnInteger newTxnInteger() {
        return refFactory.newTxnInteger(0);
    }

    /**
     * Creates a committed {@link TxnLong} with 0 as initial value using the {@link GlobalStmInstance}.
     *
     * @return the created TnxLong.
     */
    public static TxnLong newTxnLong() {
        return refFactory.newTxnLong(0);
    }

    /**
     * Creates a committed {@link TxnLong} with the provided value using the {@link GlobalStmInstance}.
     *
     * @param value the initial value of the TxnLong.
     * @return the created TnxLong.
     */
    public static TxnLong newTxnLong(long value) {
        return refFactory.newTxnLong(value);
    }

    /**
     * Creates a committed {@link TxnDouble} with 0 as initial value using the {@link GlobalStmInstance}.
     *
     * @return the created TxnDouble.
     */
    public static TxnDouble newTxnDouble() {
        return refFactory.newTxnDouble(0);
    }

    /**
     * Creates a committed {@link TxnDouble} with the provided value using the {@link GlobalStmInstance}.
     *
     * @param value the initial value.
     * @return the created TnxDouble.
     */
    public static TxnDouble newTxnDouble(double value) {
        return refFactory.newTxnDouble(value);
    }

    /**
     * Creates a committed {@link TxnBoolean} with false as initial value using the {@link GlobalStmInstance}.
     *
     * @return the created TxnBoolean.
     */
    public static TxnBoolean newTxnBoolean() {
        return refFactory.newTxnBoolean(false);
    }

    /**
     * Creates a committed {@link TxnBoolean} with the provided value using the {@link GlobalStmInstance}.
     *
     * @param value the initial value
     * @return the created TxnBoolean.
     */
    public static TxnBoolean newTxnBoolean(boolean value) {
        return refFactory.newTxnBoolean(value);
    }

    /**
     * Creates a committed {@link TxnRef} with null as initial value using the {@link GlobalStmInstance}.
     *
     * @param <E> the type of the TxnRef.
     * @return the created Ref.
     */
    public static <E> TxnRef<E> newTxnRef() {
        return refFactory.newTxnRef(null);
    }

    /**
     * Creates a committed {@link TxnRef} with the provided value using the {@link GlobalStmInstance}.
     *
     * @param value the initial value of the TxnRef.
     * @param <E>   the type of the TxnRef.
     * @return the created TxnRef.
     */
    public static <E> TxnRef<E> newTxnRef(E value) {
        return refFactory.newTxnRef(value);
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
