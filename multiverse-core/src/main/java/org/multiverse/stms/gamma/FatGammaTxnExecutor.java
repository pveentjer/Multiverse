package org.multiverse.stms.gamma;

import org.multiverse.api.*;
import org.multiverse.api.exceptions.*;
import org.multiverse.api.callables.*;
import org.multiverse.stms.gamma.transactions.*;
import java.util.concurrent.Callable;
import java.util.logging.Logger;

import static java.lang.String.format;
import static org.multiverse.api.TxnThreadLocal.*;

/**
 * The {@link TxnExecutor} made for the GammaStm.
 *
 * This code is generated.
 *
 * @author Peter Veentjer
 */
public final class FatGammaTxnExecutor extends AbstractGammaTxnExecutor{
    private static final Logger logger = Logger.getLogger(FatGammaTxnExecutor.class.getName());

    public void execute(Runnable runnable){
        throw new UnsupportedOperationException();
    }

    public <E> E execute(Callable<E> callable){
        throw new UnsupportedOperationException();
    }

    public <E> E executeChecked(Callable<E> callable)throws Exception{
        throw new UnsupportedOperationException();
    }

    private final PropagationLevel propagationLevel;

    public FatGammaTxnExecutor(final GammaTxnFactory txnFactory) {
        super(txnFactory);
        this.propagationLevel = txnConfig.propagationLevel;
    }

    @Override
    public GammaTxnFactory getTxnFactory(){
        return txnFactory;
    }

    @Override
    public final <E> E executeChecked(
        final TxnCallable<E> callable)throws Exception{

        try{
            return execute(callable);
        }catch(InvisibleCheckedException e){
            throw e.getCause();
        }
    }

     public <E> E execute(final TxnCallable<E> callable){

        if(callable == null){
            throw new NullPointerException();
        }

        TxnThreadLocal.Container transactionContainer = getThreadLocalTxnContainer();
        GammaTxnPool pool = (GammaTxnPool) transactionContainer.txPool;
        if (pool == null) {
            pool = new GammaTxnPool();
            transactionContainer.txPool = pool;
        }

        GammaTxn tx = (GammaTxn)transactionContainer.txn;
        if(tx == null || !tx.isAlive()){
            tx = null;
        }

        try{
            switch (propagationLevel) {
                case Requires:
                    if (tx == null) {
                        if (TRACING_ENABLED) {
                            if (txnConfig.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(
                                    format("[%s] Has 'Requires' propagation level and no txn found, starting a new txn",
                                    txnConfig.familyName));
                            }
                        }

                        tx = txnFactory.newTransaction(pool);
                        transactionContainer.txn = tx;
                        return execute(tx, transactionContainer, pool, callable);
                    } else {
                        if (TRACING_ENABLED) {
                            if (txnConfig.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(
                                    format("[%s] Has 'Requires' propagation level, and existing txn [%s] found",
                                        txnConfig.familyName, tx.getConfig().getFamilyName()));
                                }
                            }

                        return callable.call(tx);
                    }
                case Mandatory:
                    if (tx == null) {
                        if (TRACING_ENABLED) {
                            if (txnConfig.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(
                                    format("[%s] Has 'Mandatory' propagation level, and no txn is found",
                                        txnConfig.familyName));
                                }
                            }
                            throw new TxnMandatoryException(
                                format("No txn is found for TxnExecutor '%s' with 'Mandatory' propagation level",
                                    txnConfig.familyName));
                        }

                    if (TRACING_ENABLED) {
                        if (txnConfig.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                            logger.info(
                                format("[%s] Has 'Mandatory' propagation level and txn [%s] found",
                                    txnConfig.familyName, tx.getConfig().getFamilyName()));
                        }
                    }
                    return callable.call(tx);
                case Never:
                    if (tx != null) {
                        if (TRACING_ENABLED) {
                            if (txnConfig.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(
                                    format("[%s] Has 'Never' propagation level, but txn [%s] is found",
                                        txnConfig.familyName, tx.getConfig().getFamilyName()));
                            }
                        }

                        throw new TxnNotAllowedException(
                            format("No txn is allowed for TxnExecutor '%s' with propagation level 'Never'"+
                                ", but txn '%s' was found",
                                    txnConfig.familyName, tx.getConfig().getFamilyName())
                            );
                    }

                    if (TRACING_ENABLED) {
                        if (txnConfig.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                            logger.info(
                                format("[%s] Has 'Never' propagation level and no txn is found",
                                    txnConfig.familyName));
                        }
                    }
                    return callable.call(null);
                case RequiresNew:
                    if (tx == null) {
                        if(TRACING_ENABLED){
                            if (txnConfig.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(
                                    format("[%s] Has 'RequiresNew' propagation level and no txn is found, starting new txn",
                                        txnConfig.familyName));
                            }
                        }

                        tx = txnFactory.newTransaction(pool);
                        transactionContainer.txn = tx;
                        return execute(tx, transactionContainer, pool, callable);
                    } else {
                        if(TRACING_ENABLED){
                            if (txnConfig.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(
                                    format("[%s] Has 'RequiresNew' propagationLevel and existing txn [%s] was found",
                                        txnConfig.familyName, tx.getConfig().getFamilyName()));
                            }
                        }

                        GammaTxn suspendedTransaction = tx;
                        tx = txnFactory.newTransaction(pool);
                        transactionContainer.txn = tx;
                        try {
                            return execute(tx, transactionContainer, pool, callable);
                        } finally {
                            transactionContainer.txn = suspendedTransaction;
                        }
                    }
                case Supports:
                    if(TRACING_ENABLED){
                        if(tx!=null){
                            if (txnConfig.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(
                                    format("[%s] Has 'RequiresNew' propagationLevel and existing txn [%s] was found",
                                        txnConfig.familyName, tx.getConfig().getFamilyName()));
                            }
                        }else{
                            if (txnConfig.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(
                                    format("[%s] Has 'RequiresNew' propagationLevel and existing txn [%s] was found",
                                        txnConfig.familyName, tx.getConfig().getFamilyName()));
                            }
                        }
                    }

                    return callable.call(tx);
                default:
                    throw new IllegalStateException();
            }
        }catch(RuntimeException e){
            throw e;
        }catch(Exception e){
            throw new InvisibleCheckedException(e);
        }
    }

    private <E> E execute(
        GammaTxn tx, final TxnThreadLocal.Container transactionContainer, GammaTxnPool pool, final TxnCallable<E> callable)throws Exception{
        Error cause = null;

        try{
            boolean abort = true;
            try {
                do {
                    try {
                        cause = null;
                        E result = callable.call(tx);
                        tx.commit();
                        abort = false;
                        return result;
                    } catch (RetryError e) {
                        if(TRACING_ENABLED){
                            if (txnConfig.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(format("[%s] Encountered a retry",
                                    txnConfig.familyName));
                            }
                        }
                        tx.awaitUpdate();
                    } catch (SpeculativeConfigurationError e) {
                        if(TRACING_ENABLED){
                            if (txnConfig.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(format("[%s] Encountered a speculative configuration error",
                                    txnConfig.familyName));
                            }
                        }

                        abort = false;
                        GammaTxn old = tx;
                        tx = txnFactory.upgradeAfterSpeculativeFailure(tx,pool);
                        pool.put(old);
                        transactionContainer.txn = tx;
                    } catch (ReadWriteConflict e) {
                        cause = e;
                        if(TRACING_ENABLED){
                            if (txnConfig.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(format("[%s] Encountered a read or write conflict",
                                    txnConfig.familyName));
                            }
                        }

                        backoffPolicy.delayUninterruptible(tx.getAttempt());
                    }
                } while (tx.softReset());
            } finally {
                if (abort) {
                    tx.abort();
                }

                pool.put(tx);
                transactionContainer.txn = null;
            }
        }catch(RuntimeException e){
            throw e;
        }catch(Exception e){
            throw new InvisibleCheckedException(e);
        }

        if(TRACING_ENABLED){
            if (txnConfig.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                logger.info(format("[%s] Maximum number of %s retries has been reached",
                    txnConfig.familyName, txnConfig.getMaxRetries()));
            }
        }

        throw new TooManyRetriesException(
            format("[%s] Maximum number of %s retries has been reached",
                txnConfig.getFamilyName(), txnConfig.getMaxRetries()), cause);
        }

         @Override
    public final  int executeChecked(
        final TxnIntCallable callable)throws Exception{

        try{
            return execute(callable);
        }catch(InvisibleCheckedException e){
            throw e.getCause();
        }
    }

     public  int execute(final TxnIntCallable callable){

        if(callable == null){
            throw new NullPointerException();
        }

        TxnThreadLocal.Container transactionContainer = getThreadLocalTxnContainer();
        GammaTxnPool pool = (GammaTxnPool) transactionContainer.txPool;
        if (pool == null) {
            pool = new GammaTxnPool();
            transactionContainer.txPool = pool;
        }

        GammaTxn tx = (GammaTxn)transactionContainer.txn;
        if(tx == null || !tx.isAlive()){
            tx = null;
        }

        try{
            switch (propagationLevel) {
                case Requires:
                    if (tx == null) {
                        if (TRACING_ENABLED) {
                            if (txnConfig.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(
                                    format("[%s] Has 'Requires' propagation level and no txn found, starting a new txn",
                                    txnConfig.familyName));
                            }
                        }

                        tx = txnFactory.newTransaction(pool);
                        transactionContainer.txn = tx;
                        return execute(tx, transactionContainer, pool, callable);
                    } else {
                        if (TRACING_ENABLED) {
                            if (txnConfig.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(
                                    format("[%s] Has 'Requires' propagation level, and existing txn [%s] found",
                                        txnConfig.familyName, tx.getConfig().getFamilyName()));
                                }
                            }

                        return callable.call(tx);
                    }
                case Mandatory:
                    if (tx == null) {
                        if (TRACING_ENABLED) {
                            if (txnConfig.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(
                                    format("[%s] Has 'Mandatory' propagation level, and no txn is found",
                                        txnConfig.familyName));
                                }
                            }
                            throw new TxnMandatoryException(
                                format("No txn is found for TxnExecutor '%s' with 'Mandatory' propagation level",
                                    txnConfig.familyName));
                        }

                    if (TRACING_ENABLED) {
                        if (txnConfig.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                            logger.info(
                                format("[%s] Has 'Mandatory' propagation level and txn [%s] found",
                                    txnConfig.familyName, tx.getConfig().getFamilyName()));
                        }
                    }
                    return callable.call(tx);
                case Never:
                    if (tx != null) {
                        if (TRACING_ENABLED) {
                            if (txnConfig.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(
                                    format("[%s] Has 'Never' propagation level, but txn [%s] is found",
                                        txnConfig.familyName, tx.getConfig().getFamilyName()));
                            }
                        }

                        throw new TxnNotAllowedException(
                            format("No txn is allowed for TxnExecutor '%s' with propagation level 'Never'"+
                                ", but txn '%s' was found",
                                    txnConfig.familyName, tx.getConfig().getFamilyName())
                            );
                    }

                    if (TRACING_ENABLED) {
                        if (txnConfig.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                            logger.info(
                                format("[%s] Has 'Never' propagation level and no txn is found",
                                    txnConfig.familyName));
                        }
                    }
                    return callable.call(null);
                case RequiresNew:
                    if (tx == null) {
                        if(TRACING_ENABLED){
                            if (txnConfig.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(
                                    format("[%s] Has 'RequiresNew' propagation level and no txn is found, starting new txn",
                                        txnConfig.familyName));
                            }
                        }

                        tx = txnFactory.newTransaction(pool);
                        transactionContainer.txn = tx;
                        return execute(tx, transactionContainer, pool, callable);
                    } else {
                        if(TRACING_ENABLED){
                            if (txnConfig.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(
                                    format("[%s] Has 'RequiresNew' propagationLevel and existing txn [%s] was found",
                                        txnConfig.familyName, tx.getConfig().getFamilyName()));
                            }
                        }

                        GammaTxn suspendedTransaction = tx;
                        tx = txnFactory.newTransaction(pool);
                        transactionContainer.txn = tx;
                        try {
                            return execute(tx, transactionContainer, pool, callable);
                        } finally {
                            transactionContainer.txn = suspendedTransaction;
                        }
                    }
                case Supports:
                    if(TRACING_ENABLED){
                        if(tx!=null){
                            if (txnConfig.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(
                                    format("[%s] Has 'RequiresNew' propagationLevel and existing txn [%s] was found",
                                        txnConfig.familyName, tx.getConfig().getFamilyName()));
                            }
                        }else{
                            if (txnConfig.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(
                                    format("[%s] Has 'RequiresNew' propagationLevel and existing txn [%s] was found",
                                        txnConfig.familyName, tx.getConfig().getFamilyName()));
                            }
                        }
                    }

                    return callable.call(tx);
                default:
                    throw new IllegalStateException();
            }
        }catch(RuntimeException e){
            throw e;
        }catch(Exception e){
            throw new InvisibleCheckedException(e);
        }
    }

    private  int execute(
        GammaTxn tx, final TxnThreadLocal.Container transactionContainer, GammaTxnPool pool, final TxnIntCallable callable)throws Exception{
        Error cause = null;

        try{
            boolean abort = true;
            try {
                do {
                    try {
                        cause = null;
                        int result = callable.call(tx);
                        tx.commit();
                        abort = false;
                        return result;
                    } catch (RetryError e) {
                        if(TRACING_ENABLED){
                            if (txnConfig.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(format("[%s] Encountered a retry",
                                    txnConfig.familyName));
                            }
                        }
                        tx.awaitUpdate();
                    } catch (SpeculativeConfigurationError e) {
                        if(TRACING_ENABLED){
                            if (txnConfig.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(format("[%s] Encountered a speculative configuration error",
                                    txnConfig.familyName));
                            }
                        }

                        abort = false;
                        GammaTxn old = tx;
                        tx = txnFactory.upgradeAfterSpeculativeFailure(tx,pool);
                        pool.put(old);
                        transactionContainer.txn = tx;
                    } catch (ReadWriteConflict e) {
                        cause = e;
                        if(TRACING_ENABLED){
                            if (txnConfig.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(format("[%s] Encountered a read or write conflict",
                                    txnConfig.familyName));
                            }
                        }

                        backoffPolicy.delayUninterruptible(tx.getAttempt());
                    }
                } while (tx.softReset());
            } finally {
                if (abort) {
                    tx.abort();
                }

                pool.put(tx);
                transactionContainer.txn = null;
            }
        }catch(RuntimeException e){
            throw e;
        }catch(Exception e){
            throw new InvisibleCheckedException(e);
        }

        if(TRACING_ENABLED){
            if (txnConfig.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                logger.info(format("[%s] Maximum number of %s retries has been reached",
                    txnConfig.familyName, txnConfig.getMaxRetries()));
            }
        }

        throw new TooManyRetriesException(
            format("[%s] Maximum number of %s retries has been reached",
                txnConfig.getFamilyName(), txnConfig.getMaxRetries()), cause);
        }

         @Override
    public final  long executeChecked(
        final TxnLongCallable callable)throws Exception{

        try{
            return execute(callable);
        }catch(InvisibleCheckedException e){
            throw e.getCause();
        }
    }

     public  long execute(final TxnLongCallable callable){

        if(callable == null){
            throw new NullPointerException();
        }

        TxnThreadLocal.Container transactionContainer = getThreadLocalTxnContainer();
        GammaTxnPool pool = (GammaTxnPool) transactionContainer.txPool;
        if (pool == null) {
            pool = new GammaTxnPool();
            transactionContainer.txPool = pool;
        }

        GammaTxn tx = (GammaTxn)transactionContainer.txn;
        if(tx == null || !tx.isAlive()){
            tx = null;
        }

        try{
            switch (propagationLevel) {
                case Requires:
                    if (tx == null) {
                        if (TRACING_ENABLED) {
                            if (txnConfig.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(
                                    format("[%s] Has 'Requires' propagation level and no txn found, starting a new txn",
                                    txnConfig.familyName));
                            }
                        }

                        tx = txnFactory.newTransaction(pool);
                        transactionContainer.txn = tx;
                        return execute(tx, transactionContainer, pool, callable);
                    } else {
                        if (TRACING_ENABLED) {
                            if (txnConfig.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(
                                    format("[%s] Has 'Requires' propagation level, and existing txn [%s] found",
                                        txnConfig.familyName, tx.getConfig().getFamilyName()));
                                }
                            }

                        return callable.call(tx);
                    }
                case Mandatory:
                    if (tx == null) {
                        if (TRACING_ENABLED) {
                            if (txnConfig.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(
                                    format("[%s] Has 'Mandatory' propagation level, and no txn is found",
                                        txnConfig.familyName));
                                }
                            }
                            throw new TxnMandatoryException(
                                format("No txn is found for TxnExecutor '%s' with 'Mandatory' propagation level",
                                    txnConfig.familyName));
                        }

                    if (TRACING_ENABLED) {
                        if (txnConfig.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                            logger.info(
                                format("[%s] Has 'Mandatory' propagation level and txn [%s] found",
                                    txnConfig.familyName, tx.getConfig().getFamilyName()));
                        }
                    }
                    return callable.call(tx);
                case Never:
                    if (tx != null) {
                        if (TRACING_ENABLED) {
                            if (txnConfig.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(
                                    format("[%s] Has 'Never' propagation level, but txn [%s] is found",
                                        txnConfig.familyName, tx.getConfig().getFamilyName()));
                            }
                        }

                        throw new TxnNotAllowedException(
                            format("No txn is allowed for TxnExecutor '%s' with propagation level 'Never'"+
                                ", but txn '%s' was found",
                                    txnConfig.familyName, tx.getConfig().getFamilyName())
                            );
                    }

                    if (TRACING_ENABLED) {
                        if (txnConfig.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                            logger.info(
                                format("[%s] Has 'Never' propagation level and no txn is found",
                                    txnConfig.familyName));
                        }
                    }
                    return callable.call(null);
                case RequiresNew:
                    if (tx == null) {
                        if(TRACING_ENABLED){
                            if (txnConfig.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(
                                    format("[%s] Has 'RequiresNew' propagation level and no txn is found, starting new txn",
                                        txnConfig.familyName));
                            }
                        }

                        tx = txnFactory.newTransaction(pool);
                        transactionContainer.txn = tx;
                        return execute(tx, transactionContainer, pool, callable);
                    } else {
                        if(TRACING_ENABLED){
                            if (txnConfig.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(
                                    format("[%s] Has 'RequiresNew' propagationLevel and existing txn [%s] was found",
                                        txnConfig.familyName, tx.getConfig().getFamilyName()));
                            }
                        }

                        GammaTxn suspendedTransaction = tx;
                        tx = txnFactory.newTransaction(pool);
                        transactionContainer.txn = tx;
                        try {
                            return execute(tx, transactionContainer, pool, callable);
                        } finally {
                            transactionContainer.txn = suspendedTransaction;
                        }
                    }
                case Supports:
                    if(TRACING_ENABLED){
                        if(tx!=null){
                            if (txnConfig.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(
                                    format("[%s] Has 'RequiresNew' propagationLevel and existing txn [%s] was found",
                                        txnConfig.familyName, tx.getConfig().getFamilyName()));
                            }
                        }else{
                            if (txnConfig.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(
                                    format("[%s] Has 'RequiresNew' propagationLevel and existing txn [%s] was found",
                                        txnConfig.familyName, tx.getConfig().getFamilyName()));
                            }
                        }
                    }

                    return callable.call(tx);
                default:
                    throw new IllegalStateException();
            }
        }catch(RuntimeException e){
            throw e;
        }catch(Exception e){
            throw new InvisibleCheckedException(e);
        }
    }

    private  long execute(
        GammaTxn tx, final TxnThreadLocal.Container transactionContainer, GammaTxnPool pool, final TxnLongCallable callable)throws Exception{
        Error cause = null;

        try{
            boolean abort = true;
            try {
                do {
                    try {
                        cause = null;
                        long result = callable.call(tx);
                        tx.commit();
                        abort = false;
                        return result;
                    } catch (RetryError e) {
                        if(TRACING_ENABLED){
                            if (txnConfig.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(format("[%s] Encountered a retry",
                                    txnConfig.familyName));
                            }
                        }
                        tx.awaitUpdate();
                    } catch (SpeculativeConfigurationError e) {
                        if(TRACING_ENABLED){
                            if (txnConfig.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(format("[%s] Encountered a speculative configuration error",
                                    txnConfig.familyName));
                            }
                        }

                        abort = false;
                        GammaTxn old = tx;
                        tx = txnFactory.upgradeAfterSpeculativeFailure(tx,pool);
                        pool.put(old);
                        transactionContainer.txn = tx;
                    } catch (ReadWriteConflict e) {
                        cause = e;
                        if(TRACING_ENABLED){
                            if (txnConfig.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(format("[%s] Encountered a read or write conflict",
                                    txnConfig.familyName));
                            }
                        }

                        backoffPolicy.delayUninterruptible(tx.getAttempt());
                    }
                } while (tx.softReset());
            } finally {
                if (abort) {
                    tx.abort();
                }

                pool.put(tx);
                transactionContainer.txn = null;
            }
        }catch(RuntimeException e){
            throw e;
        }catch(Exception e){
            throw new InvisibleCheckedException(e);
        }

        if(TRACING_ENABLED){
            if (txnConfig.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                logger.info(format("[%s] Maximum number of %s retries has been reached",
                    txnConfig.familyName, txnConfig.getMaxRetries()));
            }
        }

        throw new TooManyRetriesException(
            format("[%s] Maximum number of %s retries has been reached",
                txnConfig.getFamilyName(), txnConfig.getMaxRetries()), cause);
        }

         @Override
    public final  double executeChecked(
        final TxnDoubleCallable callable)throws Exception{

        try{
            return execute(callable);
        }catch(InvisibleCheckedException e){
            throw e.getCause();
        }
    }

     public  double execute(final TxnDoubleCallable callable){

        if(callable == null){
            throw new NullPointerException();
        }

        TxnThreadLocal.Container transactionContainer = getThreadLocalTxnContainer();
        GammaTxnPool pool = (GammaTxnPool) transactionContainer.txPool;
        if (pool == null) {
            pool = new GammaTxnPool();
            transactionContainer.txPool = pool;
        }

        GammaTxn tx = (GammaTxn)transactionContainer.txn;
        if(tx == null || !tx.isAlive()){
            tx = null;
        }

        try{
            switch (propagationLevel) {
                case Requires:
                    if (tx == null) {
                        if (TRACING_ENABLED) {
                            if (txnConfig.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(
                                    format("[%s] Has 'Requires' propagation level and no txn found, starting a new txn",
                                    txnConfig.familyName));
                            }
                        }

                        tx = txnFactory.newTransaction(pool);
                        transactionContainer.txn = tx;
                        return execute(tx, transactionContainer, pool, callable);
                    } else {
                        if (TRACING_ENABLED) {
                            if (txnConfig.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(
                                    format("[%s] Has 'Requires' propagation level, and existing txn [%s] found",
                                        txnConfig.familyName, tx.getConfig().getFamilyName()));
                                }
                            }

                        return callable.call(tx);
                    }
                case Mandatory:
                    if (tx == null) {
                        if (TRACING_ENABLED) {
                            if (txnConfig.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(
                                    format("[%s] Has 'Mandatory' propagation level, and no txn is found",
                                        txnConfig.familyName));
                                }
                            }
                            throw new TxnMandatoryException(
                                format("No txn is found for TxnExecutor '%s' with 'Mandatory' propagation level",
                                    txnConfig.familyName));
                        }

                    if (TRACING_ENABLED) {
                        if (txnConfig.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                            logger.info(
                                format("[%s] Has 'Mandatory' propagation level and txn [%s] found",
                                    txnConfig.familyName, tx.getConfig().getFamilyName()));
                        }
                    }
                    return callable.call(tx);
                case Never:
                    if (tx != null) {
                        if (TRACING_ENABLED) {
                            if (txnConfig.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(
                                    format("[%s] Has 'Never' propagation level, but txn [%s] is found",
                                        txnConfig.familyName, tx.getConfig().getFamilyName()));
                            }
                        }

                        throw new TxnNotAllowedException(
                            format("No txn is allowed for TxnExecutor '%s' with propagation level 'Never'"+
                                ", but txn '%s' was found",
                                    txnConfig.familyName, tx.getConfig().getFamilyName())
                            );
                    }

                    if (TRACING_ENABLED) {
                        if (txnConfig.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                            logger.info(
                                format("[%s] Has 'Never' propagation level and no txn is found",
                                    txnConfig.familyName));
                        }
                    }
                    return callable.call(null);
                case RequiresNew:
                    if (tx == null) {
                        if(TRACING_ENABLED){
                            if (txnConfig.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(
                                    format("[%s] Has 'RequiresNew' propagation level and no txn is found, starting new txn",
                                        txnConfig.familyName));
                            }
                        }

                        tx = txnFactory.newTransaction(pool);
                        transactionContainer.txn = tx;
                        return execute(tx, transactionContainer, pool, callable);
                    } else {
                        if(TRACING_ENABLED){
                            if (txnConfig.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(
                                    format("[%s] Has 'RequiresNew' propagationLevel and existing txn [%s] was found",
                                        txnConfig.familyName, tx.getConfig().getFamilyName()));
                            }
                        }

                        GammaTxn suspendedTransaction = tx;
                        tx = txnFactory.newTransaction(pool);
                        transactionContainer.txn = tx;
                        try {
                            return execute(tx, transactionContainer, pool, callable);
                        } finally {
                            transactionContainer.txn = suspendedTransaction;
                        }
                    }
                case Supports:
                    if(TRACING_ENABLED){
                        if(tx!=null){
                            if (txnConfig.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(
                                    format("[%s] Has 'RequiresNew' propagationLevel and existing txn [%s] was found",
                                        txnConfig.familyName, tx.getConfig().getFamilyName()));
                            }
                        }else{
                            if (txnConfig.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(
                                    format("[%s] Has 'RequiresNew' propagationLevel and existing txn [%s] was found",
                                        txnConfig.familyName, tx.getConfig().getFamilyName()));
                            }
                        }
                    }

                    return callable.call(tx);
                default:
                    throw new IllegalStateException();
            }
        }catch(RuntimeException e){
            throw e;
        }catch(Exception e){
            throw new InvisibleCheckedException(e);
        }
    }

    private  double execute(
        GammaTxn tx, final TxnThreadLocal.Container transactionContainer, GammaTxnPool pool, final TxnDoubleCallable callable)throws Exception{
        Error cause = null;

        try{
            boolean abort = true;
            try {
                do {
                    try {
                        cause = null;
                        double result = callable.call(tx);
                        tx.commit();
                        abort = false;
                        return result;
                    } catch (RetryError e) {
                        if(TRACING_ENABLED){
                            if (txnConfig.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(format("[%s] Encountered a retry",
                                    txnConfig.familyName));
                            }
                        }
                        tx.awaitUpdate();
                    } catch (SpeculativeConfigurationError e) {
                        if(TRACING_ENABLED){
                            if (txnConfig.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(format("[%s] Encountered a speculative configuration error",
                                    txnConfig.familyName));
                            }
                        }

                        abort = false;
                        GammaTxn old = tx;
                        tx = txnFactory.upgradeAfterSpeculativeFailure(tx,pool);
                        pool.put(old);
                        transactionContainer.txn = tx;
                    } catch (ReadWriteConflict e) {
                        cause = e;
                        if(TRACING_ENABLED){
                            if (txnConfig.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(format("[%s] Encountered a read or write conflict",
                                    txnConfig.familyName));
                            }
                        }

                        backoffPolicy.delayUninterruptible(tx.getAttempt());
                    }
                } while (tx.softReset());
            } finally {
                if (abort) {
                    tx.abort();
                }

                pool.put(tx);
                transactionContainer.txn = null;
            }
        }catch(RuntimeException e){
            throw e;
        }catch(Exception e){
            throw new InvisibleCheckedException(e);
        }

        if(TRACING_ENABLED){
            if (txnConfig.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                logger.info(format("[%s] Maximum number of %s retries has been reached",
                    txnConfig.familyName, txnConfig.getMaxRetries()));
            }
        }

        throw new TooManyRetriesException(
            format("[%s] Maximum number of %s retries has been reached",
                txnConfig.getFamilyName(), txnConfig.getMaxRetries()), cause);
        }

         @Override
    public final  boolean executeChecked(
        final TxnBooleanCallable callable)throws Exception{

        try{
            return execute(callable);
        }catch(InvisibleCheckedException e){
            throw e.getCause();
        }
    }

     public  boolean execute(final TxnBooleanCallable callable){

        if(callable == null){
            throw new NullPointerException();
        }

        TxnThreadLocal.Container transactionContainer = getThreadLocalTxnContainer();
        GammaTxnPool pool = (GammaTxnPool) transactionContainer.txPool;
        if (pool == null) {
            pool = new GammaTxnPool();
            transactionContainer.txPool = pool;
        }

        GammaTxn tx = (GammaTxn)transactionContainer.txn;
        if(tx == null || !tx.isAlive()){
            tx = null;
        }

        try{
            switch (propagationLevel) {
                case Requires:
                    if (tx == null) {
                        if (TRACING_ENABLED) {
                            if (txnConfig.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(
                                    format("[%s] Has 'Requires' propagation level and no txn found, starting a new txn",
                                    txnConfig.familyName));
                            }
                        }

                        tx = txnFactory.newTransaction(pool);
                        transactionContainer.txn = tx;
                        return execute(tx, transactionContainer, pool, callable);
                    } else {
                        if (TRACING_ENABLED) {
                            if (txnConfig.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(
                                    format("[%s] Has 'Requires' propagation level, and existing txn [%s] found",
                                        txnConfig.familyName, tx.getConfig().getFamilyName()));
                                }
                            }

                        return callable.call(tx);
                    }
                case Mandatory:
                    if (tx == null) {
                        if (TRACING_ENABLED) {
                            if (txnConfig.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(
                                    format("[%s] Has 'Mandatory' propagation level, and no txn is found",
                                        txnConfig.familyName));
                                }
                            }
                            throw new TxnMandatoryException(
                                format("No txn is found for TxnExecutor '%s' with 'Mandatory' propagation level",
                                    txnConfig.familyName));
                        }

                    if (TRACING_ENABLED) {
                        if (txnConfig.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                            logger.info(
                                format("[%s] Has 'Mandatory' propagation level and txn [%s] found",
                                    txnConfig.familyName, tx.getConfig().getFamilyName()));
                        }
                    }
                    return callable.call(tx);
                case Never:
                    if (tx != null) {
                        if (TRACING_ENABLED) {
                            if (txnConfig.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(
                                    format("[%s] Has 'Never' propagation level, but txn [%s] is found",
                                        txnConfig.familyName, tx.getConfig().getFamilyName()));
                            }
                        }

                        throw new TxnNotAllowedException(
                            format("No txn is allowed for TxnExecutor '%s' with propagation level 'Never'"+
                                ", but txn '%s' was found",
                                    txnConfig.familyName, tx.getConfig().getFamilyName())
                            );
                    }

                    if (TRACING_ENABLED) {
                        if (txnConfig.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                            logger.info(
                                format("[%s] Has 'Never' propagation level and no txn is found",
                                    txnConfig.familyName));
                        }
                    }
                    return callable.call(null);
                case RequiresNew:
                    if (tx == null) {
                        if(TRACING_ENABLED){
                            if (txnConfig.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(
                                    format("[%s] Has 'RequiresNew' propagation level and no txn is found, starting new txn",
                                        txnConfig.familyName));
                            }
                        }

                        tx = txnFactory.newTransaction(pool);
                        transactionContainer.txn = tx;
                        return execute(tx, transactionContainer, pool, callable);
                    } else {
                        if(TRACING_ENABLED){
                            if (txnConfig.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(
                                    format("[%s] Has 'RequiresNew' propagationLevel and existing txn [%s] was found",
                                        txnConfig.familyName, tx.getConfig().getFamilyName()));
                            }
                        }

                        GammaTxn suspendedTransaction = tx;
                        tx = txnFactory.newTransaction(pool);
                        transactionContainer.txn = tx;
                        try {
                            return execute(tx, transactionContainer, pool, callable);
                        } finally {
                            transactionContainer.txn = suspendedTransaction;
                        }
                    }
                case Supports:
                    if(TRACING_ENABLED){
                        if(tx!=null){
                            if (txnConfig.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(
                                    format("[%s] Has 'RequiresNew' propagationLevel and existing txn [%s] was found",
                                        txnConfig.familyName, tx.getConfig().getFamilyName()));
                            }
                        }else{
                            if (txnConfig.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(
                                    format("[%s] Has 'RequiresNew' propagationLevel and existing txn [%s] was found",
                                        txnConfig.familyName, tx.getConfig().getFamilyName()));
                            }
                        }
                    }

                    return callable.call(tx);
                default:
                    throw new IllegalStateException();
            }
        }catch(RuntimeException e){
            throw e;
        }catch(Exception e){
            throw new InvisibleCheckedException(e);
        }
    }

    private  boolean execute(
        GammaTxn tx, final TxnThreadLocal.Container transactionContainer, GammaTxnPool pool, final TxnBooleanCallable callable)throws Exception{
        Error cause = null;

        try{
            boolean abort = true;
            try {
                do {
                    try {
                        cause = null;
                        boolean result = callable.call(tx);
                        tx.commit();
                        abort = false;
                        return result;
                    } catch (RetryError e) {
                        if(TRACING_ENABLED){
                            if (txnConfig.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(format("[%s] Encountered a retry",
                                    txnConfig.familyName));
                            }
                        }
                        tx.awaitUpdate();
                    } catch (SpeculativeConfigurationError e) {
                        if(TRACING_ENABLED){
                            if (txnConfig.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(format("[%s] Encountered a speculative configuration error",
                                    txnConfig.familyName));
                            }
                        }

                        abort = false;
                        GammaTxn old = tx;
                        tx = txnFactory.upgradeAfterSpeculativeFailure(tx,pool);
                        pool.put(old);
                        transactionContainer.txn = tx;
                    } catch (ReadWriteConflict e) {
                        cause = e;
                        if(TRACING_ENABLED){
                            if (txnConfig.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(format("[%s] Encountered a read or write conflict",
                                    txnConfig.familyName));
                            }
                        }

                        backoffPolicy.delayUninterruptible(tx.getAttempt());
                    }
                } while (tx.softReset());
            } finally {
                if (abort) {
                    tx.abort();
                }

                pool.put(tx);
                transactionContainer.txn = null;
            }
        }catch(RuntimeException e){
            throw e;
        }catch(Exception e){
            throw new InvisibleCheckedException(e);
        }

        if(TRACING_ENABLED){
            if (txnConfig.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                logger.info(format("[%s] Maximum number of %s retries has been reached",
                    txnConfig.familyName, txnConfig.getMaxRetries()));
            }
        }

        throw new TooManyRetriesException(
            format("[%s] Maximum number of %s retries has been reached",
                txnConfig.getFamilyName(), txnConfig.getMaxRetries()), cause);
        }

         @Override
    public final  void executeChecked(
        final TxnVoidCallable callable)throws Exception{

        try{
            execute(callable);
        }catch(InvisibleCheckedException e){
            throw e.getCause();
        }
    }

     public  void execute(final TxnVoidCallable callable){

        if(callable == null){
            throw new NullPointerException();
        }

        TxnThreadLocal.Container transactionContainer = getThreadLocalTxnContainer();
        GammaTxnPool pool = (GammaTxnPool) transactionContainer.txPool;
        if (pool == null) {
            pool = new GammaTxnPool();
            transactionContainer.txPool = pool;
        }

        GammaTxn tx = (GammaTxn)transactionContainer.txn;
        if(tx == null || !tx.isAlive()){
            tx = null;
        }

        try{
            switch (propagationLevel) {
                case Requires:
                    if (tx == null) {
                        if (TRACING_ENABLED) {
                            if (txnConfig.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(
                                    format("[%s] Has 'Requires' propagation level and no txn found, starting a new txn",
                                    txnConfig.familyName));
                            }
                        }

                        tx = txnFactory.newTransaction(pool);
                        transactionContainer.txn = tx;
                        execute(tx, transactionContainer,pool, callable);
                        return;
                    } else {
                        if (TRACING_ENABLED) {
                            if (txnConfig.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(
                                    format("[%s] Has 'Requires' propagation level, and existing txn [%s] found",
                                        txnConfig.familyName, tx.getConfig().getFamilyName()));
                                }
                            }

                        callable.call(tx);
                        return;
                    }
                case Mandatory:
                    if (tx == null) {
                        if (TRACING_ENABLED) {
                            if (txnConfig.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(
                                    format("[%s] Has 'Mandatory' propagation level, and no txn is found",
                                        txnConfig.familyName));
                                }
                            }
                            throw new TxnMandatoryException(
                                format("No txn is found for TxnExecutor '%s' with 'Mandatory' propagation level",
                                    txnConfig.familyName));
                        }

                    if (TRACING_ENABLED) {
                        if (txnConfig.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                            logger.info(
                                format("[%s] Has 'Mandatory' propagation level and txn [%s] found",
                                    txnConfig.familyName, tx.getConfig().getFamilyName()));
                        }
                    }
                    callable.call(tx);
                    return;
                case Never:
                    if (tx != null) {
                        if (TRACING_ENABLED) {
                            if (txnConfig.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(
                                    format("[%s] Has 'Never' propagation level, but txn [%s] is found",
                                        txnConfig.familyName, tx.getConfig().getFamilyName()));
                            }
                        }

                        throw new TxnNotAllowedException(
                            format("No txn is allowed for TxnExecutor '%s' with propagation level 'Never'"+
                                ", but txn '%s' was found",
                                    txnConfig.familyName, tx.getConfig().getFamilyName())
                            );
                    }

                    if (TRACING_ENABLED) {
                        if (txnConfig.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                            logger.info(
                                format("[%s] Has 'Never' propagation level and no txn is found",
                                    txnConfig.familyName));
                        }
                    }
                    callable.call(null);
                    return;
                case RequiresNew:
                    if (tx == null) {
                        if(TRACING_ENABLED){
                            if (txnConfig.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(
                                    format("[%s] Has 'RequiresNew' propagation level and no txn is found, starting new txn",
                                        txnConfig.familyName));
                            }
                        }

                        tx = txnFactory.newTransaction(pool);
                        transactionContainer.txn = tx;
                        execute(tx, transactionContainer, pool, callable);
                        return;
                    } else {
                        if(TRACING_ENABLED){
                            if (txnConfig.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(
                                    format("[%s] Has 'RequiresNew' propagationLevel and existing txn [%s] was found",
                                        txnConfig.familyName, tx.getConfig().getFamilyName()));
                            }
                        }

                        GammaTxn suspendedTransaction = tx;
                        tx = txnFactory.newTransaction(pool);
                        transactionContainer.txn = tx;
                        try {
                            execute(tx, transactionContainer, pool, callable);
                            return;
                        } finally {
                            transactionContainer.txn = suspendedTransaction;
                        }
                    }
                case Supports:
                    if(TRACING_ENABLED){
                        if(tx!=null){
                            if (txnConfig.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(
                                    format("[%s] Has 'RequiresNew' propagationLevel and existing txn [%s] was found",
                                        txnConfig.familyName, tx.getConfig().getFamilyName()));
                            }
                        }else{
                            if (txnConfig.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(
                                    format("[%s] Has 'RequiresNew' propagationLevel and existing txn [%s] was found",
                                        txnConfig.familyName, tx.getConfig().getFamilyName()));
                            }
                        }
                    }

                    callable.call(tx);
                    return;
                default:
                    throw new IllegalStateException();
            }
        }catch(RuntimeException e){
            throw e;
        }catch(Exception e){
            throw new InvisibleCheckedException(e);
        }
    }

    private  void execute(
        GammaTxn tx, final TxnThreadLocal.Container transactionContainer, GammaTxnPool pool, final TxnVoidCallable callable)throws Exception{
        Error cause = null;

        try{
            boolean abort = true;
            try {
                do {
                    try {
                        cause = null;
                        callable.call(tx);
                        tx.commit();
                        abort = false;
                        return;
                    } catch (RetryError e) {
                        if(TRACING_ENABLED){
                            if (txnConfig.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(format("[%s] Encountered a retry",
                                    txnConfig.familyName));
                            }
                        }
                        tx.awaitUpdate();
                    } catch (SpeculativeConfigurationError e) {
                        if(TRACING_ENABLED){
                            if (txnConfig.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(format("[%s] Encountered a speculative configuration error",
                                    txnConfig.familyName));
                            }
                        }

                        abort = false;
                        GammaTxn old = tx;
                        tx = txnFactory.upgradeAfterSpeculativeFailure(tx,pool);
                        pool.put(old);
                        transactionContainer.txn = tx;
                    } catch (ReadWriteConflict e) {
                        cause = e;
                        if(TRACING_ENABLED){
                            if (txnConfig.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(format("[%s] Encountered a read or write conflict",
                                    txnConfig.familyName));
                            }
                        }

                        backoffPolicy.delayUninterruptible(tx.getAttempt());
                    }
                } while (tx.softReset());
            } finally {
                if (abort) {
                    tx.abort();
                }

                pool.put(tx);
                transactionContainer.txn = null;
            }
        }catch(RuntimeException e){
            throw e;
        }catch(Exception e){
            throw new InvisibleCheckedException(e);
        }

        if(TRACING_ENABLED){
            if (txnConfig.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                logger.info(format("[%s] Maximum number of %s retries has been reached",
                    txnConfig.familyName, txnConfig.getMaxRetries()));
            }
        }

        throw new TooManyRetriesException(
            format("[%s] Maximum number of %s retries has been reached",
                txnConfig.getFamilyName(), txnConfig.getMaxRetries()), cause);
        }

       }
