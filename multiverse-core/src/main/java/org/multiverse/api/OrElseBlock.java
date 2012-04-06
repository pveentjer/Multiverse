package org.multiverse.api;

import org.multiverse.MultiverseConstants;
import org.multiverse.api.closures.*;

/**
 * The OrElse is responsible for executing the either block, or in case of a retry, the orelse block is executed.
 * <p/>
 * Another useful features of this design is that for certain primitives it doesn't require any form of boxing.
 * It also provides an atomicChecked for a TxnVoidClosure which doesn't force a developer to return something when
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
     * @throws org.multiverse.api.exceptions.TxnMandatoryException
     *                              if no transaction is found on the TxnThreadLocal.
     * @throws org.multiverse.api.exceptions.InvisibleCheckedException
     *                              if a checked exception is thrown by the closure.
     */
    <E> E execute(TxnClosure<E> either, TxnClosure<E> orelse);

    /**
     * Executes the either, or when it is retried, the orelse block. This operation makes composable blocking operations
     * possible.
     *
     * @param either
     * @param orelse
     * @return the result of the execution.
     * @throws NullPointerException         if either or orelse is null.
     * @throws org.multiverse.api.exceptions.TxnMandatoryException if no transaction is found on the TxnThreadLocal.
     * @throws Exception                    if the atomicChecked call fails.
     */
    <E> E executeChecked(TxnClosure<E> either, TxnClosure<E> orelse) throws Exception;

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
     * @throws org.multiverse.api.exceptions.TxnMandatoryException if no transaction is found on the TxnThreadLocal.
     * @throws org.multiverse.api.exceptions.InvisibleCheckedException
     *                                      if a checked exception is thrown by the closure.
     */
    int execute(TxnIntClosure either, TxnIntClosure orelse);

    /**
     * Executes the either, or when it is retried, the orelse block. This operation makes composable blocking operations
     * possible.
     *
     * @param either
     * @param orelse
     * @return the result of the execution.
     * @throws NullPointerException         if either or orelse is null.
     * @throws TxnMandatoryException if no transaction is found on the TxnThreadLocal.
     * @throws Exception                    if the atomicChecked call fails.
     */
    int executeChecked(TxnIntClosure either, TxnIntClosure orelse) throws Exception;

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
     * @throws TxnMandatoryException if no transaction is found on the TxnThreadLocal.
     * @throws org.multiverse.api.exceptions.InvisibleCheckedException
     *                                      if a checked exception is thrown by the closure.
     */
    long execute(TxnLongClosure either, TxnLongClosure orelse);

    /**
     * Executes the either, or when it is retried, the orelse block. This operation makes composable blocking operations
     * possible.
     *
     * @param either
     * @param orelse
     * @return the result of the execution.
     * @throws NullPointerException         if either or orelse is null.
     * @throws TxnMandatoryException if no transaction is found on the TxnThreadLocal.
     * @throws Exception                    if the atomicChecked call fails.
     */
    long executeChecked(TxnLongClosure either, TxnLongClosure orelse) throws Exception;

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
     * @throws TxnMandatoryException if no transaction is found on the TxnThreadLocal.
     * @throws org.multiverse.api.exceptions.InvisibleCheckedException
     *                                      if a checked exception is thrown by the closure.
     */
    double execute(TxnDoubleClosure either, TxnDoubleClosure orelse);

    /**
     * Executes the either, or when it is retried, the orelse block. This operation makes composable blocking operations
     * possible.
     *
     * @param either
     * @param orelse
     * @return the result of the execution.
     * @throws NullPointerException         if either or orelse is null.
     * @throws TxnMandatoryException if no transaction is found on the TxnThreadLocal.
     * @throws Exception                    if the atomicChecked call fails.
     */
    double executeChecked(TxnDoubleClosure either, TxnDoubleClosure orelse) throws Exception;

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
     * @throws TxnMandatoryException if no transaction is found on the TxnThreadLocal.
     * @throws org.multiverse.api.exceptions.InvisibleCheckedException
     *                                      if a checked exception is thrown by the closure.
     */
    boolean execute(TxnBooleanClosure either, TxnBooleanClosure orelse);

    /**
     * Executes the either, or when it is retried, the orelse block. This operation makes composable blocking operations
     * possible.
     *
     * @param either
     * @param orelse
     * @return the result of the execution.
     * @throws NullPointerException         if either or orelse is null.
     * @throws TxnMandatoryException if no transaction is found on the TxnThreadLocal.
     * @throws Exception                    if the atomicChecked call fails.
     */
    boolean executeChecked(TxnBooleanClosure either, TxnBooleanClosure orelse) throws Exception;

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
     * @throws TxnMandatoryException if no transaction is found on the TxnThreadLocal.
     * @throws org.multiverse.api.exceptions.InvisibleCheckedException
     *                                      if a checked exception is thrown by the closure.
     */
    void execute(TxnVoidClosure either, TxnVoidClosure orelse);

    /**
     * Executes the either, or when it is retried, the orelse block. This operation makes composable blocking operations
     * possible.
     *
     * @param either
     * @param orelse
     * @throws NullPointerException         if either or orelse is null.
     * @throws TxnMandatoryException if no transaction is found on the TxnThreadLocal.
     * @throws Exception                    if the atomicChecked call fails.
     */
    void executeChecked(TxnVoidClosure either, TxnVoidClosure orelse) throws Exception;

}
