package org.multiverse.stms.gamma;

import org.multiverse.api.*;
import org.multiverse.api.exceptions.*;
import org.multiverse.api.closures.*;
import org.multiverse.stms.gamma.transactions.*;
import java.util.logging.Logger;

import static java.lang.String.format;
import static org.multiverse.api.ThreadLocalTransaction.*;

/**
* An GammaTransactionExecutor made for the GammaStm.
*
* This code is generated.
*
* @author Peter Veentjer
*/
public final class LeanGammaTransactionExecutor extends AbstractGammaTransactionExecutor{
    private static final Logger logger = Logger.getLogger(LeanGammaTransactionExecutor.class.getName());


    public LeanGammaTransactionExecutor(final GammaTxnFactory txnFactory) {
        super(txnFactory);
    }

    @Override
    public GammaTxnFactory getTransactionFactory(){
        return txnFactory;
    }

    @Override
    public final <E> E atomicChecked(
        final AtomicClosure<E> closure)throws Exception{

        try{
            return atomic(closure);
        }catch(InvisibleCheckedException e){
            throw e.getCause();
        }
    }

    @Override
    public final <E> E atomic(final AtomicClosure<E> closure){

        if(closure == null){
            throw new NullPointerException();
        }

        final ThreadLocalTransaction.Container transactionContainer = getThreadLocalTransactionContainer();
        GammaTransactionPool pool = (GammaTransactionPool) transactionContainer.txPool;
        if (pool == null) {
            pool = new GammaTransactionPool();
            transactionContainer.txPool = pool;
        }

        GammaTransaction tx = (GammaTransaction)transactionContainer.tx;
        if(tx == null || !tx.isAlive()){
            tx = null;
        }

        Throwable cause = null;
        try{
            if(tx != null && tx.isAlive()){
                return closure.execute(tx);
            }

            tx = txnFactory.newTransaction(pool);
            transactionContainer.tx=tx;
            boolean abort = true;
            try {
                do {
                    try {
                        cause = null;
                        E result = closure.execute(tx);
                        tx.commit();
                        abort = false;
                        return result;
                    } catch (RetryError e) {
                        if(TRACING_ENABLED){
                            if (txnConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(format("[%s] Encountered a retry",
                                    txnConfiguration.familyName));
                            }
                        }
                        tx.awaitUpdate();
                    } catch (SpeculativeConfigurationError e) {
                        if(TRACING_ENABLED){
                            if (txnConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(format("[%s] Encountered a speculative configuration error",
                                    txnConfiguration.familyName));
                            }
                        }

                        abort = false;
                        GammaTransaction old = tx;
                        tx = txnFactory.upgradeAfterSpeculativeFailure(tx,pool);
                        pool.put(old);
                        transactionContainer.tx = tx;
                    } catch (ReadWriteConflict e) {
                        cause = e;
                        if(TRACING_ENABLED){
                            if (txnConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(format("[%s] Encountered a read or write conflict",
                                    txnConfiguration.familyName));
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
                transactionContainer.tx = null;
            }
        }catch(RuntimeException e){
            throw e;
        }catch(Exception e){
            throw new InvisibleCheckedException(e);
        }

        if(TRACING_ENABLED){
            if (txnConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                logger.info(format("[%s] Maximum number of %s retries has been reached",
                    txnConfiguration.familyName, txnConfiguration.getMaxRetries()));
            }
        }

        throw new TooManyRetriesException(
            format("[%s] Maximum number of %s retries has been reached",
                txnConfiguration.getFamilyName(), txnConfiguration.getMaxRetries()), cause);
        }

     @Override
    public final  int atomicChecked(
        final AtomicIntClosure closure)throws Exception{

        try{
            return atomic(closure);
        }catch(InvisibleCheckedException e){
            throw e.getCause();
        }
    }

    @Override
    public final  int atomic(final AtomicIntClosure closure){

        if(closure == null){
            throw new NullPointerException();
        }

        final ThreadLocalTransaction.Container transactionContainer = getThreadLocalTransactionContainer();
        GammaTransactionPool pool = (GammaTransactionPool) transactionContainer.txPool;
        if (pool == null) {
            pool = new GammaTransactionPool();
            transactionContainer.txPool = pool;
        }

        GammaTransaction tx = (GammaTransaction)transactionContainer.tx;
        if(tx == null || !tx.isAlive()){
            tx = null;
        }

        Throwable cause = null;
        try{
            if(tx != null && tx.isAlive()){
                return closure.execute(tx);
            }

            tx = txnFactory.newTransaction(pool);
            transactionContainer.tx=tx;
            boolean abort = true;
            try {
                do {
                    try {
                        cause = null;
                        int result = closure.execute(tx);
                        tx.commit();
                        abort = false;
                        return result;
                    } catch (RetryError e) {
                        if(TRACING_ENABLED){
                            if (txnConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(format("[%s] Encountered a retry",
                                    txnConfiguration.familyName));
                            }
                        }
                        tx.awaitUpdate();
                    } catch (SpeculativeConfigurationError e) {
                        if(TRACING_ENABLED){
                            if (txnConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(format("[%s] Encountered a speculative configuration error",
                                    txnConfiguration.familyName));
                            }
                        }

                        abort = false;
                        GammaTransaction old = tx;
                        tx = txnFactory.upgradeAfterSpeculativeFailure(tx,pool);
                        pool.put(old);
                        transactionContainer.tx = tx;
                    } catch (ReadWriteConflict e) {
                        cause = e;
                        if(TRACING_ENABLED){
                            if (txnConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(format("[%s] Encountered a read or write conflict",
                                    txnConfiguration.familyName));
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
                transactionContainer.tx = null;
            }
        }catch(RuntimeException e){
            throw e;
        }catch(Exception e){
            throw new InvisibleCheckedException(e);
        }

        if(TRACING_ENABLED){
            if (txnConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                logger.info(format("[%s] Maximum number of %s retries has been reached",
                    txnConfiguration.familyName, txnConfiguration.getMaxRetries()));
            }
        }

        throw new TooManyRetriesException(
            format("[%s] Maximum number of %s retries has been reached",
                txnConfiguration.getFamilyName(), txnConfiguration.getMaxRetries()), cause);
        }

     @Override
    public final  long atomicChecked(
        final AtomicLongClosure closure)throws Exception{

        try{
            return atomic(closure);
        }catch(InvisibleCheckedException e){
            throw e.getCause();
        }
    }

    @Override
    public final  long atomic(final AtomicLongClosure closure){

        if(closure == null){
            throw new NullPointerException();
        }

        final ThreadLocalTransaction.Container transactionContainer = getThreadLocalTransactionContainer();
        GammaTransactionPool pool = (GammaTransactionPool) transactionContainer.txPool;
        if (pool == null) {
            pool = new GammaTransactionPool();
            transactionContainer.txPool = pool;
        }

        GammaTransaction tx = (GammaTransaction)transactionContainer.tx;
        if(tx == null || !tx.isAlive()){
            tx = null;
        }

        Throwable cause = null;
        try{
            if(tx != null && tx.isAlive()){
                return closure.execute(tx);
            }

            tx = txnFactory.newTransaction(pool);
            transactionContainer.tx=tx;
            boolean abort = true;
            try {
                do {
                    try {
                        cause = null;
                        long result = closure.execute(tx);
                        tx.commit();
                        abort = false;
                        return result;
                    } catch (RetryError e) {
                        if(TRACING_ENABLED){
                            if (txnConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(format("[%s] Encountered a retry",
                                    txnConfiguration.familyName));
                            }
                        }
                        tx.awaitUpdate();
                    } catch (SpeculativeConfigurationError e) {
                        if(TRACING_ENABLED){
                            if (txnConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(format("[%s] Encountered a speculative configuration error",
                                    txnConfiguration.familyName));
                            }
                        }

                        abort = false;
                        GammaTransaction old = tx;
                        tx = txnFactory.upgradeAfterSpeculativeFailure(tx,pool);
                        pool.put(old);
                        transactionContainer.tx = tx;
                    } catch (ReadWriteConflict e) {
                        cause = e;
                        if(TRACING_ENABLED){
                            if (txnConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(format("[%s] Encountered a read or write conflict",
                                    txnConfiguration.familyName));
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
                transactionContainer.tx = null;
            }
        }catch(RuntimeException e){
            throw e;
        }catch(Exception e){
            throw new InvisibleCheckedException(e);
        }

        if(TRACING_ENABLED){
            if (txnConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                logger.info(format("[%s] Maximum number of %s retries has been reached",
                    txnConfiguration.familyName, txnConfiguration.getMaxRetries()));
            }
        }

        throw new TooManyRetriesException(
            format("[%s] Maximum number of %s retries has been reached",
                txnConfiguration.getFamilyName(), txnConfiguration.getMaxRetries()), cause);
        }

     @Override
    public final  double atomicChecked(
        final AtomicDoubleClosure closure)throws Exception{

        try{
            return atomic(closure);
        }catch(InvisibleCheckedException e){
            throw e.getCause();
        }
    }

    @Override
    public final  double atomic(final AtomicDoubleClosure closure){

        if(closure == null){
            throw new NullPointerException();
        }

        final ThreadLocalTransaction.Container transactionContainer = getThreadLocalTransactionContainer();
        GammaTransactionPool pool = (GammaTransactionPool) transactionContainer.txPool;
        if (pool == null) {
            pool = new GammaTransactionPool();
            transactionContainer.txPool = pool;
        }

        GammaTransaction tx = (GammaTransaction)transactionContainer.tx;
        if(tx == null || !tx.isAlive()){
            tx = null;
        }

        Throwable cause = null;
        try{
            if(tx != null && tx.isAlive()){
                return closure.execute(tx);
            }

            tx = txnFactory.newTransaction(pool);
            transactionContainer.tx=tx;
            boolean abort = true;
            try {
                do {
                    try {
                        cause = null;
                        double result = closure.execute(tx);
                        tx.commit();
                        abort = false;
                        return result;
                    } catch (RetryError e) {
                        if(TRACING_ENABLED){
                            if (txnConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(format("[%s] Encountered a retry",
                                    txnConfiguration.familyName));
                            }
                        }
                        tx.awaitUpdate();
                    } catch (SpeculativeConfigurationError e) {
                        if(TRACING_ENABLED){
                            if (txnConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(format("[%s] Encountered a speculative configuration error",
                                    txnConfiguration.familyName));
                            }
                        }

                        abort = false;
                        GammaTransaction old = tx;
                        tx = txnFactory.upgradeAfterSpeculativeFailure(tx,pool);
                        pool.put(old);
                        transactionContainer.tx = tx;
                    } catch (ReadWriteConflict e) {
                        cause = e;
                        if(TRACING_ENABLED){
                            if (txnConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(format("[%s] Encountered a read or write conflict",
                                    txnConfiguration.familyName));
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
                transactionContainer.tx = null;
            }
        }catch(RuntimeException e){
            throw e;
        }catch(Exception e){
            throw new InvisibleCheckedException(e);
        }

        if(TRACING_ENABLED){
            if (txnConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                logger.info(format("[%s] Maximum number of %s retries has been reached",
                    txnConfiguration.familyName, txnConfiguration.getMaxRetries()));
            }
        }

        throw new TooManyRetriesException(
            format("[%s] Maximum number of %s retries has been reached",
                txnConfiguration.getFamilyName(), txnConfiguration.getMaxRetries()), cause);
        }

     @Override
    public final  boolean atomicChecked(
        final AtomicBooleanClosure closure)throws Exception{

        try{
            return atomic(closure);
        }catch(InvisibleCheckedException e){
            throw e.getCause();
        }
    }

    @Override
    public final  boolean atomic(final AtomicBooleanClosure closure){

        if(closure == null){
            throw new NullPointerException();
        }

        final ThreadLocalTransaction.Container transactionContainer = getThreadLocalTransactionContainer();
        GammaTransactionPool pool = (GammaTransactionPool) transactionContainer.txPool;
        if (pool == null) {
            pool = new GammaTransactionPool();
            transactionContainer.txPool = pool;
        }

        GammaTransaction tx = (GammaTransaction)transactionContainer.tx;
        if(tx == null || !tx.isAlive()){
            tx = null;
        }

        Throwable cause = null;
        try{
            if(tx != null && tx.isAlive()){
                return closure.execute(tx);
            }

            tx = txnFactory.newTransaction(pool);
            transactionContainer.tx=tx;
            boolean abort = true;
            try {
                do {
                    try {
                        cause = null;
                        boolean result = closure.execute(tx);
                        tx.commit();
                        abort = false;
                        return result;
                    } catch (RetryError e) {
                        if(TRACING_ENABLED){
                            if (txnConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(format("[%s] Encountered a retry",
                                    txnConfiguration.familyName));
                            }
                        }
                        tx.awaitUpdate();
                    } catch (SpeculativeConfigurationError e) {
                        if(TRACING_ENABLED){
                            if (txnConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(format("[%s] Encountered a speculative configuration error",
                                    txnConfiguration.familyName));
                            }
                        }

                        abort = false;
                        GammaTransaction old = tx;
                        tx = txnFactory.upgradeAfterSpeculativeFailure(tx,pool);
                        pool.put(old);
                        transactionContainer.tx = tx;
                    } catch (ReadWriteConflict e) {
                        cause = e;
                        if(TRACING_ENABLED){
                            if (txnConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(format("[%s] Encountered a read or write conflict",
                                    txnConfiguration.familyName));
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
                transactionContainer.tx = null;
            }
        }catch(RuntimeException e){
            throw e;
        }catch(Exception e){
            throw new InvisibleCheckedException(e);
        }

        if(TRACING_ENABLED){
            if (txnConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                logger.info(format("[%s] Maximum number of %s retries has been reached",
                    txnConfiguration.familyName, txnConfiguration.getMaxRetries()));
            }
        }

        throw new TooManyRetriesException(
            format("[%s] Maximum number of %s retries has been reached",
                txnConfiguration.getFamilyName(), txnConfiguration.getMaxRetries()), cause);
        }

     @Override
    public final  void atomicChecked(
        final AtomicVoidClosure closure)throws Exception{

        try{
            atomic(closure);
        }catch(InvisibleCheckedException e){
            throw e.getCause();
        }
    }

    @Override
    public final  void atomic(final AtomicVoidClosure closure){

        if(closure == null){
            throw new NullPointerException();
        }

        final ThreadLocalTransaction.Container transactionContainer = getThreadLocalTransactionContainer();
        GammaTransactionPool pool = (GammaTransactionPool) transactionContainer.txPool;
        if (pool == null) {
            pool = new GammaTransactionPool();
            transactionContainer.txPool = pool;
        }

        GammaTransaction tx = (GammaTransaction)transactionContainer.tx;
        if(tx == null || !tx.isAlive()){
            tx = null;
        }

        Throwable cause = null;
        try{
            if(tx != null && tx.isAlive()){
                closure.execute(tx);
                return;
            }

            tx = txnFactory.newTransaction(pool);
            transactionContainer.tx=tx;
            boolean abort = true;
            try {
                do {
                    try {
                        cause = null;
                        closure.execute(tx);
                        tx.commit();
                        abort = false;
                        return;
                    } catch (RetryError e) {
                        if(TRACING_ENABLED){
                            if (txnConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(format("[%s] Encountered a retry",
                                    txnConfiguration.familyName));
                            }
                        }
                        tx.awaitUpdate();
                    } catch (SpeculativeConfigurationError e) {
                        if(TRACING_ENABLED){
                            if (txnConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(format("[%s] Encountered a speculative configuration error",
                                    txnConfiguration.familyName));
                            }
                        }

                        abort = false;
                        GammaTransaction old = tx;
                        tx = txnFactory.upgradeAfterSpeculativeFailure(tx,pool);
                        pool.put(old);
                        transactionContainer.tx = tx;
                    } catch (ReadWriteConflict e) {
                        cause = e;
                        if(TRACING_ENABLED){
                            if (txnConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(format("[%s] Encountered a read or write conflict",
                                    txnConfiguration.familyName));
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
                transactionContainer.tx = null;
            }
        }catch(RuntimeException e){
            throw e;
        }catch(Exception e){
            throw new InvisibleCheckedException(e);
        }

        if(TRACING_ENABLED){
            if (txnConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                logger.info(format("[%s] Maximum number of %s retries has been reached",
                    txnConfiguration.familyName, txnConfiguration.getMaxRetries()));
            }
        }

        throw new TooManyRetriesException(
            format("[%s] Maximum number of %s retries has been reached",
                txnConfiguration.getFamilyName(), txnConfiguration.getMaxRetries()), cause);
        }

   }
