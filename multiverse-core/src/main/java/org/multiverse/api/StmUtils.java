package org.multiverse.api;

import org.multiverse.api.closures.*;
import org.multiverse.api.collections.*;
import org.multiverse.api.exceptions.*;
import org.multiverse.api.lifecycle.*;
import org.multiverse.api.references.*;
import java.util.*;

import static org.multiverse.api.GlobalStmInstance.getGlobalStmInstance;
import static org.multiverse.api.ThreadLocalTransaction.getRequiredThreadLocalTransaction;

/**
 * A utility class with convenience methods to access the {@link org.multiverse.api.Stm} or
 * {@link Transaction}. These methods can be imported using the static import for a less
 * ugly syntax.
 *
 * @author Peter Veentjer.
 */
public final class StmUtils {

    private final static RefFactory refFactory
        = getGlobalStmInstance().getDefaultRefFactory();
    private final static AtomicBlock defaultAtomicBlock
        = getGlobalStmInstance().getDefaultAtomicBlock();
    private final static OrElseBlock orelseBlock
        = getGlobalStmInstance().newOrElseBlock();
    private final static TransactionalCollectionsFactory transactionalCollectionsFactory
        = getGlobalStmInstance().getDefaultTransactionalCollectionFactory();

    /**
     * Creates a new committed TransactionList based on linked nodes.
     *
     * @return the created TransactionalList.
     */
    public static <E> TransactionalList<E> newLinkedList(){
        return transactionalCollectionsFactory.newLinkedList();
    }

    /**
     * Creates a new committed unbound TransactionalStack.
     *
     * @return the created TransactionalStack.
     */
    public static <E> TransactionalStack<E> newStack(){
        return transactionalCollectionsFactory.newStack();
    }

    /**
     * Creates a new committed bound TransactionalStack.
     *
     * @param capacity the maximum capacity of the stack. Integer.MAX_VALUE indicates that there is no bound.
     * @return the create TransactionalStack
     * @throws IllegalArgumentException if capacity smaller than 0.
     */
    public static <E> TransactionalStack<E> newStack(int capacity){
        return transactionalCollectionsFactory.newStack(capacity);
    }

    /**
     * Creates a new committed unbound TransactionalQueue.
     *
     * @return the created TransactionalQueue.
     */
    public static <E> TransactionalQueue<E> newQueue(){
        return transactionalCollectionsFactory.newQueue();
    }

    /**
     * Creates a new committed bound TransactionalQueue.
     *
     * @param capacity the maximum capacity of the queue. Integer.MAX_VALUE indicates that there is no bound.
     * @return the created TransactionalQueue
     * @throws IllegalArgumentException if capacity smaller than 0.
     */
    public static <E> TransactionalQueue<E> newQueue(int capacity){
        return transactionalCollectionsFactory.newQueue(capacity);
    }

    /**
     * Creates a new committed unbound TransactionalDeque.
     *
     * @return the created TransactionalDeque
     */
    public static <E> TransactionalDeque<E> newDeque(){
        return transactionalCollectionsFactory.newDeque();
    }

    /**
     * Creates a new committed bound TransactionalDeque.
     *
     * @param capacity the maximum capacity of the deque. Integer.MAX_VALUE indicates that there is no bound.
     * @return the created TransactionalDeque.
     * @throws IllegalArgumentException if capacity is smaller than 0.
     */
    public static <E> TransactionalDeque<E> newDeque(int capacity){
        return transactionalCollectionsFactory.newDeque(capacity);
    }

    /**
     * Creates a new committed TransactionalSet that is based on a 'hashtable'.
     *
     * @return the created TransactionalSet.
     */
    public static <E> TransactionalSet<E> newHashSet(){
        return transactionalCollectionsFactory.newHashSet();
    }

    /**
     * Creates a new committed TransactionalMap.
     *
     * @return the created TransactionalMap
     */
    public static <K, V> TransactionalMap<K, V> newHashMap(){
        return transactionalCollectionsFactory.newHashMap();
    }

    /**
     * Executes the closure transactionally on the GlobalStmInstance using the default AtomicBlock. If a
     * Transaction already is active on the ThreadLocalTransaction, this transaction will lift on that
     * transaction (so the propagation level is Requires) and will not commit this transaction.
     *
     * <p>This method doesn't throw a checked exception, but if the closure does, it is wrapped inside an
     * InvisibleCheckedException.
     *
     * <p>If you want to get most out of performance, it is best to make use of a customized {@link AtomicBlock} instead
     * of relying on the default AtomicBlock that will always provide the most expensive transaction available.
     *
     * @param closure The closure {@link AtomicClosure} to execute.
     * @return the result of the execution
     * @throws NullPointerException if closure is null.
     * @throws org.multiverse.api.exceptions.InvisibleCheckedException
     *                                  if the closure throws a checked exception.
     */
    public static <E> E execute(AtomicClosure<E> closure){
        return defaultAtomicBlock.execute(closure);
    }

   /**
    * Executes the closure transactionally on the GlobalStmInstance using the default AtomicBlock. If a
    * Transaction already is active on the ThreadLocalTransaction, this transaction will lift on that
    * transaction (so the propagation level is Requires) and will not commit this transaction.
    *
    * <p>If you want to get most out of performance, it is best to make use of a customized {@link AtomicBlock} instead
    * of relying on the default AtomicBlock that will always provide the most expensive transaction available.
    *
    * @param closure The {@link AtomicClosure} to execute.
    * @return the result of the execution
    * @throws NullPointerException if closure is null.
    * @throws Exception is the closure throws an Exception
    */
   public static <E> E executeChecked(AtomicClosure<E> closure) throws Exception{
       return defaultAtomicBlock.executeChecked(closure);
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
    public static <E> E execute(AtomicClosure<E> either, AtomicClosure<E> orelse){
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
    public static <E> E executeChecked(AtomicClosure<E> either, AtomicClosure<E> orelse)throws Exception{
        return orelseBlock.executeChecked(either,orelse);
    }

    /**
     * Executes the closure transactionally on the GlobalStmInstance using the default AtomicBlock. If a
     * Transaction already is active on the ThreadLocalTransaction, this transaction will lift on that
     * transaction (so the propagation level is Requires) and will not commit this transaction.
     *
     * <p>This method doesn't throw a checked exception, but if the closure does, it is wrapped inside an
     * InvisibleCheckedException.
     *
     * <p>If you want to get most out of performance, it is best to make use of a customized {@link AtomicBlock} instead
     * of relying on the default AtomicBlock that will always provide the most expensive transaction available.
     *
     * @param closure The closure {@link AtomicIntClosure} to execute.
     * @return the result of the execution
     * @throws NullPointerException if closure is null.
     * @throws org.multiverse.api.exceptions.InvisibleCheckedException
     *                                  if the closure throws a checked exception.
     */
    public static  int execute(AtomicIntClosure closure){
        return defaultAtomicBlock.execute(closure);
    }

   /**
    * Executes the closure transactionally on the GlobalStmInstance using the default AtomicBlock. If a
    * Transaction already is active on the ThreadLocalTransaction, this transaction will lift on that
    * transaction (so the propagation level is Requires) and will not commit this transaction.
    *
    * <p>If you want to get most out of performance, it is best to make use of a customized {@link AtomicBlock} instead
    * of relying on the default AtomicBlock that will always provide the most expensive transaction available.
    *
    * @param closure The {@link AtomicIntClosure} to execute.
    * @return the result of the execution
    * @throws NullPointerException if closure is null.
    * @throws Exception is the closure throws an Exception
    */
   public static  int executeChecked(AtomicIntClosure closure) throws Exception{
       return defaultAtomicBlock.executeChecked(closure);
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
    public static  int execute(AtomicIntClosure either, AtomicIntClosure orelse){
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
    public static  int executeChecked(AtomicIntClosure either, AtomicIntClosure orelse)throws Exception{
        return orelseBlock.executeChecked(either,orelse);
    }

    /**
     * Executes the closure transactionally on the GlobalStmInstance using the default AtomicBlock. If a
     * Transaction already is active on the ThreadLocalTransaction, this transaction will lift on that
     * transaction (so the propagation level is Requires) and will not commit this transaction.
     *
     * <p>This method doesn't throw a checked exception, but if the closure does, it is wrapped inside an
     * InvisibleCheckedException.
     *
     * <p>If you want to get most out of performance, it is best to make use of a customized {@link AtomicBlock} instead
     * of relying on the default AtomicBlock that will always provide the most expensive transaction available.
     *
     * @param closure The closure {@link AtomicLongClosure} to execute.
     * @return the result of the execution
     * @throws NullPointerException if closure is null.
     * @throws org.multiverse.api.exceptions.InvisibleCheckedException
     *                                  if the closure throws a checked exception.
     */
    public static  long execute(AtomicLongClosure closure){
        return defaultAtomicBlock.execute(closure);
    }

   /**
    * Executes the closure transactionally on the GlobalStmInstance using the default AtomicBlock. If a
    * Transaction already is active on the ThreadLocalTransaction, this transaction will lift on that
    * transaction (so the propagation level is Requires) and will not commit this transaction.
    *
    * <p>If you want to get most out of performance, it is best to make use of a customized {@link AtomicBlock} instead
    * of relying on the default AtomicBlock that will always provide the most expensive transaction available.
    *
    * @param closure The {@link AtomicLongClosure} to execute.
    * @return the result of the execution
    * @throws NullPointerException if closure is null.
    * @throws Exception is the closure throws an Exception
    */
   public static  long executeChecked(AtomicLongClosure closure) throws Exception{
       return defaultAtomicBlock.executeChecked(closure);
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
    public static  long execute(AtomicLongClosure either, AtomicLongClosure orelse){
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
    public static  long executeChecked(AtomicLongClosure either, AtomicLongClosure orelse)throws Exception{
        return orelseBlock.executeChecked(either,orelse);
    }

    /**
     * Executes the closure transactionally on the GlobalStmInstance using the default AtomicBlock. If a
     * Transaction already is active on the ThreadLocalTransaction, this transaction will lift on that
     * transaction (so the propagation level is Requires) and will not commit this transaction.
     *
     * <p>This method doesn't throw a checked exception, but if the closure does, it is wrapped inside an
     * InvisibleCheckedException.
     *
     * <p>If you want to get most out of performance, it is best to make use of a customized {@link AtomicBlock} instead
     * of relying on the default AtomicBlock that will always provide the most expensive transaction available.
     *
     * @param closure The closure {@link AtomicDoubleClosure} to execute.
     * @return the result of the execution
     * @throws NullPointerException if closure is null.
     * @throws org.multiverse.api.exceptions.InvisibleCheckedException
     *                                  if the closure throws a checked exception.
     */
    public static  double execute(AtomicDoubleClosure closure){
        return defaultAtomicBlock.execute(closure);
    }

   /**
    * Executes the closure transactionally on the GlobalStmInstance using the default AtomicBlock. If a
    * Transaction already is active on the ThreadLocalTransaction, this transaction will lift on that
    * transaction (so the propagation level is Requires) and will not commit this transaction.
    *
    * <p>If you want to get most out of performance, it is best to make use of a customized {@link AtomicBlock} instead
    * of relying on the default AtomicBlock that will always provide the most expensive transaction available.
    *
    * @param closure The {@link AtomicDoubleClosure} to execute.
    * @return the result of the execution
    * @throws NullPointerException if closure is null.
    * @throws Exception is the closure throws an Exception
    */
   public static  double executeChecked(AtomicDoubleClosure closure) throws Exception{
       return defaultAtomicBlock.executeChecked(closure);
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
    public static  double execute(AtomicDoubleClosure either, AtomicDoubleClosure orelse){
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
    public static  double executeChecked(AtomicDoubleClosure either, AtomicDoubleClosure orelse)throws Exception{
        return orelseBlock.executeChecked(either,orelse);
    }

    /**
     * Executes the closure transactionally on the GlobalStmInstance using the default AtomicBlock. If a
     * Transaction already is active on the ThreadLocalTransaction, this transaction will lift on that
     * transaction (so the propagation level is Requires) and will not commit this transaction.
     *
     * <p>This method doesn't throw a checked exception, but if the closure does, it is wrapped inside an
     * InvisibleCheckedException.
     *
     * <p>If you want to get most out of performance, it is best to make use of a customized {@link AtomicBlock} instead
     * of relying on the default AtomicBlock that will always provide the most expensive transaction available.
     *
     * @param closure The closure {@link AtomicBooleanClosure} to execute.
     * @return the result of the execution
     * @throws NullPointerException if closure is null.
     * @throws org.multiverse.api.exceptions.InvisibleCheckedException
     *                                  if the closure throws a checked exception.
     */
    public static  boolean execute(AtomicBooleanClosure closure){
        return defaultAtomicBlock.execute(closure);
    }

   /**
    * Executes the closure transactionally on the GlobalStmInstance using the default AtomicBlock. If a
    * Transaction already is active on the ThreadLocalTransaction, this transaction will lift on that
    * transaction (so the propagation level is Requires) and will not commit this transaction.
    *
    * <p>If you want to get most out of performance, it is best to make use of a customized {@link AtomicBlock} instead
    * of relying on the default AtomicBlock that will always provide the most expensive transaction available.
    *
    * @param closure The {@link AtomicBooleanClosure} to execute.
    * @return the result of the execution
    * @throws NullPointerException if closure is null.
    * @throws Exception is the closure throws an Exception
    */
   public static  boolean executeChecked(AtomicBooleanClosure closure) throws Exception{
       return defaultAtomicBlock.executeChecked(closure);
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
    public static  boolean execute(AtomicBooleanClosure either, AtomicBooleanClosure orelse){
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
    public static  boolean executeChecked(AtomicBooleanClosure either, AtomicBooleanClosure orelse)throws Exception{
        return orelseBlock.executeChecked(either,orelse);
    }

    /**
     * Executes the closure transactionally on the GlobalStmInstance using the default AtomicBlock. If a
     * Transaction already is active on the ThreadLocalTransaction, this transaction will lift on that
     * transaction (so the propagation level is Requires) and will not commit this transaction.
     *
     * <p>This method doesn't throw a checked exception, but if the closure does, it is wrapped inside an
     * InvisibleCheckedException.
     *
     * <p>If you want to get most out of performance, it is best to make use of a customized {@link AtomicBlock} instead
     * of relying on the default AtomicBlock that will always provide the most expensive transaction available.
     *
     * @param closure The closure {@link AtomicVoidClosure} to execute.
     * @throws NullPointerException if closure is null.
     * @throws org.multiverse.api.exceptions.InvisibleCheckedException
     *                                  if the closure throws a checked exception.
     */
    public static  void execute(AtomicVoidClosure closure){
        defaultAtomicBlock.execute(closure);
    }

   /**
    * Executes the closure transactionally on the GlobalStmInstance using the default AtomicBlock. If a
    * Transaction already is active on the ThreadLocalTransaction, this transaction will lift on that
    * transaction (so the propagation level is Requires) and will not commit this transaction.
    *
    * <p>If you want to get most out of performance, it is best to make use of a customized {@link AtomicBlock} instead
    * of relying on the default AtomicBlock that will always provide the most expensive transaction available.
    *
    * @param closure The {@link AtomicVoidClosure} to execute.
    * @throws NullPointerException if closure is null.
    * @throws Exception is the closure throws an Exception
    */
   public static  void executeChecked(AtomicVoidClosure closure) throws Exception{
       defaultAtomicBlock.executeChecked(closure);
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
    public static  void execute(AtomicVoidClosure either, AtomicVoidClosure orelse){
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
    public static  void executeChecked(AtomicVoidClosure either, AtomicVoidClosure orelse)throws Exception{
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
     * (by the {@link AtomicBlock} for example). The Retry should not be caught by user code in almost all cases.
     */
    public static void retry() {
        Transaction tx = getRequiredThreadLocalTransaction();
        tx.retry();
    }

    /**
     * Prepares the Transaction in the ThreadLocalTransaction transaction.
     *
     * <p>For more information see {@link Transaction#prepare()}.
     *
     * @throws TransactionMandatoryException if no active transaction is found.
     * @throws IllegalTransactionStateException if the active transaction is not in the correct
     *                                           state for this operation.
     * @throws ControlFlowError
     */
    public static void prepare() {
        Transaction tx = getRequiredThreadLocalTransaction();
        tx.prepare();
    }

    /**
     * Aborts the Transaction in the ThreadLocalTransaction transaction.
     *
     * <p>For more information see {@link Transaction#abort()}.
     *
     * @throws TransactionMandatoryException if no active transaction is found.
     * @throws IllegalTransactionStateException if the active transaction is not in the correct
     *                                           state for this operation.
     * @throws ControlFlowError
     */
    public static void abort() {
        Transaction tx = getRequiredThreadLocalTransaction();
        tx.abort();
    }

    /**
     * Commits the Transaction in the ThreadLocalTransaction transaction.
     *
     * <p>For more information see {@link Transaction#commit()}.
     *
     * @throws TransactionMandatoryException if no active transaction is found.
     * @throws IllegalTransactionStateException if the active transaction is not in the correct
     * state for this operation.
     * @throws ControlFlowError
     */
     public static void commit() {
         Transaction tx = getRequiredThreadLocalTransaction();
         tx.commit();
     }

     /**
      * Scheduled an deferred or compensating task on the Transaction in the ThreadLocalTransaction. Thistask is
      * executed after the transaction commits or aborts.
      *
      * @param task the deferred task to execute.
      * @throws NullPointerException if task is null.
      * @throws org.multiverse.api.exceptions.TransactionMandatoryException
                        if no Transaction is set on the {@link org.multiverse.api.ThreadLocalTransaction}.
      * @throws org.multiverse.api.exceptions.IllegalTransactionStateException
      *                 if the transaction is not in the correct state to accept a compensating or deferred task.
      */
      public static void scheduleCompensatingOrDeferredTask(final Runnable task) {
            if (task == null) {
                throw new NullPointerException();
            }

            Transaction tx = getRequiredThreadLocalTransaction();
            tx.register(new TransactionListener() {
                @Override
                public void notify(Transaction tx, TransactionEvent event) {
                    if (event == TransactionEvent.PostCommit || event == TransactionEvent.PostAbort) {
                        task.run();
                    }
                }
            });
       }

       /**
        * Scheduled an deferred task on the {@link Transaction} in the {@link ThreadLocalTransaction}. This task is executed after
        * the transaction commits and one of the use cases is starting transactions.
        *
        * @param task the deferred task to execute.
        * @throws NullPointerException if task is null.
        * @throws org.multiverse.api.exceptions.TransactionMandatoryException
        *                   if no Transaction is set on the {@link org.multiverse.api.ThreadLocalTransaction}.
        * @throws org.multiverse.api.exceptions.IllegalTransactionStateException
        *                   if the transaction is not in the correct state to accept a deferred task.
        */
       public static void scheduleDeferredTask(final Runnable task) {
            if (task == null) {
                throw new NullPointerException();
            }

            Transaction tx = getRequiredThreadLocalTransaction();
            tx.register(new TransactionListener() {
                @Override
                public void notify(Transaction tx, TransactionEvent event) {
                    if (event == TransactionEvent.PostCommit) {
                        task.run();
                    }
                }
            });
       }

       /**
        * Scheduled a compensating task on the {@link Transaction} in the {@link ThreadLocalTransaction}. This task is executed after
        * the transaction aborts and one of the use cases is cleaning up non transaction resources like the file system.
        *
        * @param task the deferred task to execute.
        * @throws NullPointerException if task is null.
        * @throws org.multiverse.api.exceptions.TransactionMandatoryException
        *                       if no Transaction is set on the {@link org.multiverse.api.ThreadLocalTransaction}.
        * @throws org.multiverse.api.exceptions.IllegalTransactionStateException
        *                       if the transaction is not in the correct state to accept a compensating task.
        */
       public static void scheduleCompensatingTask(final Runnable task) {
            if (task == null) {
                throw new NullPointerException();
            }

            Transaction tx = getRequiredThreadLocalTransaction();
            tx.register(new TransactionListener() {
                @Override
                public void notify(Transaction tx, TransactionEvent event) {
                    if (event == TransactionEvent.PostAbort) {
                        task.run();
                    }
                }
            });
       }

       //we don want instances
       private StmUtils() {
       }
}
