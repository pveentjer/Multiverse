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
public final class LeanGammaTxnExecutor extends AbstractGammaTxnExecutor{
    private static final Logger logger = Logger.getLogger(LeanGammaTxnExecutor.class.getName());


    public LeanGammaTxnExecutor(final GammaTxnFactory txnFactory) {
        super(txnFactory);
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

    @Override
    public final <E> E execute(final TxnCallable<E> callable){

        if(callable == null){
            throw new NullPointerException();
        }

        final TxnThreadLocal.Container transactionContainer = getThreadLocalTxnContainer();
        GammaTxnPool pool = (GammaTxnPool) transactionContainer.txPool;
        if (pool == null) {
            pool = new GammaTxnPool();
            transactionContainer.txPool = pool;
        }

        GammaTxn tx = (GammaTxn)transactionContainer.txn;
        if(tx == null || !tx.isAlive()){
            tx = null;
        }

        Throwable cause = null;
        try{
            if(tx != null && tx.isAlive()){
                return callable.call(tx);
            }

            tx = txnFactory.newTransaction(pool);
            transactionContainer.txn=tx;
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

    @Override
    public final  int execute(final TxnIntCallable callable){

        if(callable == null){
            throw new NullPointerException();
        }

        final TxnThreadLocal.Container transactionContainer = getThreadLocalTxnContainer();
        GammaTxnPool pool = (GammaTxnPool) transactionContainer.txPool;
        if (pool == null) {
            pool = new GammaTxnPool();
            transactionContainer.txPool = pool;
        }

        GammaTxn tx = (GammaTxn)transactionContainer.txn;
        if(tx == null || !tx.isAlive()){
            tx = null;
        }

        Throwable cause = null;
        try{
            if(tx != null && tx.isAlive()){
                return callable.call(tx);
            }

            tx = txnFactory.newTransaction(pool);
            transactionContainer.txn=tx;
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

    @Override
    public final  long execute(final TxnLongCallable callable){

        if(callable == null){
            throw new NullPointerException();
        }

        final TxnThreadLocal.Container transactionContainer = getThreadLocalTxnContainer();
        GammaTxnPool pool = (GammaTxnPool) transactionContainer.txPool;
        if (pool == null) {
            pool = new GammaTxnPool();
            transactionContainer.txPool = pool;
        }

        GammaTxn tx = (GammaTxn)transactionContainer.txn;
        if(tx == null || !tx.isAlive()){
            tx = null;
        }

        Throwable cause = null;
        try{
            if(tx != null && tx.isAlive()){
                return callable.call(tx);
            }

            tx = txnFactory.newTransaction(pool);
            transactionContainer.txn=tx;
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

    @Override
    public final  double execute(final TxnDoubleCallable callable){

        if(callable == null){
            throw new NullPointerException();
        }

        final TxnThreadLocal.Container transactionContainer = getThreadLocalTxnContainer();
        GammaTxnPool pool = (GammaTxnPool) transactionContainer.txPool;
        if (pool == null) {
            pool = new GammaTxnPool();
            transactionContainer.txPool = pool;
        }

        GammaTxn tx = (GammaTxn)transactionContainer.txn;
        if(tx == null || !tx.isAlive()){
            tx = null;
        }

        Throwable cause = null;
        try{
            if(tx != null && tx.isAlive()){
                return callable.call(tx);
            }

            tx = txnFactory.newTransaction(pool);
            transactionContainer.txn=tx;
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

    @Override
    public final  boolean execute(final TxnBooleanCallable callable){

        if(callable == null){
            throw new NullPointerException();
        }

        final TxnThreadLocal.Container transactionContainer = getThreadLocalTxnContainer();
        GammaTxnPool pool = (GammaTxnPool) transactionContainer.txPool;
        if (pool == null) {
            pool = new GammaTxnPool();
            transactionContainer.txPool = pool;
        }

        GammaTxn tx = (GammaTxn)transactionContainer.txn;
        if(tx == null || !tx.isAlive()){
            tx = null;
        }

        Throwable cause = null;
        try{
            if(tx != null && tx.isAlive()){
                return callable.call(tx);
            }

            tx = txnFactory.newTransaction(pool);
            transactionContainer.txn=tx;
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

    @Override
    public final  void execute(final TxnVoidCallable callable){

        if(callable == null){
            throw new NullPointerException();
        }

        final TxnThreadLocal.Container transactionContainer = getThreadLocalTxnContainer();
        GammaTxnPool pool = (GammaTxnPool) transactionContainer.txPool;
        if (pool == null) {
            pool = new GammaTxnPool();
            transactionContainer.txPool = pool;
        }

        GammaTxn tx = (GammaTxn)transactionContainer.txn;
        if(tx == null || !tx.isAlive()){
            tx = null;
        }

        Throwable cause = null;
        try{
            if(tx != null && tx.isAlive()){
                callable.call(tx);
                return;
            }

            tx = txnFactory.newTransaction(pool);
            transactionContainer.txn=tx;
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
