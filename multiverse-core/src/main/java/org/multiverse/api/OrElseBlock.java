package org.multiverse.api;

import org.multiverse.MultiverseConstants;
import org.multiverse.api.closures.*;

/**
 * The OrElse is responsible for executing the either block, or in case of a retry, the orelse block is executed.
 * <p/>
 * Another useful features of this design is that for certain primitives it doesn't require any form of boxing.
 * It also provides an atomicChecked for a AtomicVoidClosure which doesn't force a developer to return something when
 * nothing needs to be returned.
 *
 * @author Peter Veentjer.
 */
public interface OrElseBlock extends MultiverseConstants {

    /**
     * Executes the either, or when it is retried, the orelse block. This operation makes composable blocking operations
     * possible.
     * <p/>
     * If in the execution of the closure a checked exception is thrown, the exception
     * is wrapped in a InvisibleCheckedException. The original exception can be retrieved by calling the
     * getCause method.
     *
     * @param either
     * @param orelse
     * @return the result of the execution.
     * @throws NullPointerException if either or orelse is null.
     * @throws org.multiverse.api.exceptions.TransactionMandatoryException
     *                              if no transaction is found on the TxnThreadLocal.
     * @throws org.multiverse.api.exceptions.InvisibleCheckedException
     *                              if a checked exception is thrown by the closure.
     */
    <E> E execute(AtomicClosure<E> either, AtomicClosure<E> orelse);

    /**
     * Executes the either, or when it is retried, the orelse block. This operation makes composable blocking operations
     * possible.
     *
     * @param either
     * @param orelse
     * @return the result of the execution.
     * @throws NullPointerException         if either or orelse is null.
     * @throws org.multiverse.api.exceptions.TransactionMandatoryException if no transaction is found on the TxnThreadLocal.
     * @throws Exception                    if the atomicChecked call fails.
     */
    <E> E executeChecked(AtomicClosure<E> either, AtomicClosure<E> orelse) throws Exception;

    /**
     * Executes the either, or when it is retried, the orelse block. This operation makes composable blocking operations
     * possible.
     * <p/>
     * If in the execution of the closure a checked exception is thrown, the exception
     * is wrapped in a InvisibleCheckedException. The original exception can be retrieved by calling the
     * getCause method.
     *
     * @param either
     * @param orelse
     * @return the result of the execution.
     * @throws NullPointerException         if either or orelse is null.
     * @throws org.multiverse.api.exceptions.TransactionMandatoryException if no transaction is found on the TxnThreadLocal.
     * @throws org.multiverse.api.exceptions.InvisibleCheckedException
     *                                      if a checked exception is thrown by the closure.
     */
    int execute(AtomicIntClosure either, AtomicIntClosure orelse);

    /**
     * Executes the either, or when it is retried, the orelse block. This operation makes composable blocking operations
     * possible.
     *
     * @param either
     * @param orelse
     * @return the result of the execution.
     * @throws NullPointerException         if either or orelse is null.
     * @throws TransactionMandatoryException if no transaction is found on the TxnThreadLocal.
     * @throws Exception                    if the atomicChecked call fails.
     */
    int executeChecked(AtomicIntClosure either, AtomicIntClosure orelse) throws Exception;

    /**
     * Executes the either, or when it is retried, the orelse block. This operation makes composable blocking operations
     * possible.
     * <p/>
     * If in the execution of the closure a checked exception is thrown, the exception
     * is wrapped in a InvisibleCheckedException. The original exception can be retrieved by calling the
     * getCause method.
     *
     * @param either
     * @param orelse
     * @return the result of the execution.
     * @throws NullPointerException         if either or orelse is null.
     * @throws TransactionMandatoryException if no transaction is found on the TxnThreadLocal.
     * @throws org.multiverse.api.exceptions.InvisibleCheckedException
     *                                      if a checked exception is thrown by the closure.
     */
    long execute(AtomicLongClosure either, AtomicLongClosure orelse);

    /**
     * Executes the either, or when it is retried, the orelse block. This operation makes composable blocking operations
     * possible.
     *
     * @param either
     * @param orelse
     * @return the result of the execution.
     * @throws NullPointerException         if either or orelse is null.
     * @throws TransactionMandatoryException if no transaction is found on the TxnThreadLocal.
     * @throws Exception                    if the atomicChecked call fails.
     */
    long executeChecked(AtomicLongClosure either, AtomicLongClosure orelse) throws Exception;

    /**
     * Executes the either, or when it is retried, the orelse block. This operation makes composable blocking operations
     * possible.
     * <p/>
     * If in the execution of the closure a checked exception is thrown, the exception
     * is wrapped in a InvisibleCheckedException. The original exception can be retrieved by calling the
     * getCause method.
     *
     * @param either
     * @param orelse
     * @return the result of the execution.
     * @throws NullPointerException         if either or orelse is null.
     * @throws TransactionMandatoryException if no transaction is found on the TxnThreadLocal.
     * @throws org.multiverse.api.exceptions.InvisibleCheckedException
     *                                      if a checked exception is thrown by the closure.
     */
    double execute(AtomicDoubleClosure either, AtomicDoubleClosure orelse);

    /**
     * Executes the either, or when it is retried, the orelse block. This operation makes composable blocking operations
     * possible.
     *
     * @param either
     * @param orelse
     * @return the result of the execution.
     * @throws NullPointerException         if either or orelse is null.
     * @throws TransactionMandatoryException if no transaction is found on the TxnThreadLocal.
     * @throws Exception                    if the atomicChecked call fails.
     */
    double executeChecked(AtomicDoubleClosure either, AtomicDoubleClosure orelse) throws Exception;

    /**
     * Executes the either, or when it is retried, the orelse block. This operation makes composable blocking operations
     * possible.
     * <p/>
     * If in the execution of the closure a checked exception is thrown, the exception
     * is wrapped in a InvisibleCheckedException. The original exception can be retrieved by calling the
     * getCause method.
     *
     * @param either
     * @param orelse
     * @return the result of the execution.
     * @throws NullPointerException         if either or orelse is null.
     * @throws TransactionMandatoryException if no transaction is found on the TxnThreadLocal.
     * @throws org.multiverse.api.exceptions.InvisibleCheckedException
     *                                      if a checked exception is thrown by the closure.
     */
    boolean execute(AtomicBooleanClosure either, AtomicBooleanClosure orelse);

    /**
     * Executes the either, or when it is retried, the orelse block. This operation makes composable blocking operations
     * possible.
     *
     * @param either
     * @param orelse
     * @return the result of the execution.
     * @throws NullPointerException         if either or orelse is null.
     * @throws TransactionMandatoryException if no transaction is found on the TxnThreadLocal.
     * @throws Exception                    if the atomicChecked call fails.
     */
    boolean executeChecked(AtomicBooleanClosure either, AtomicBooleanClosure orelse) throws Exception;

    /**
     * Executes the either, or when it is retried, the orelse block. This operation makes composable blocking operations
     * possible.
     * <p/>
     * If in the execution of the closure a checked exception is thrown, the exception
     * is wrapped in a InvisibleCheckedException. The original exception can be retrieved by calling the
     * getCause method.
     *
     * @param either
     * @param orelse
     * @throws NullPointerException         if either or orelse is null.
     * @throws TransactionMandatoryException if no transaction is found on the TxnThreadLocal.
     * @throws org.multiverse.api.exceptions.InvisibleCheckedException
     *                                      if a checked exception is thrown by the closure.
     */
    void execute(AtomicVoidClosure either, AtomicVoidClosure orelse);

    /**
     * Executes the either, or when it is retried, the orelse block. This operation makes composable blocking operations
     * possible.
     *
     * @param either
     * @param orelse
     * @throws NullPointerException         if either or orelse is null.
     * @throws TransactionMandatoryException if no transaction is found on the TxnThreadLocal.
     * @throws Exception                    if the atomicChecked call fails.
     */
    void executeChecked(AtomicVoidClosure either, AtomicVoidClosure orelse) throws Exception;

}
