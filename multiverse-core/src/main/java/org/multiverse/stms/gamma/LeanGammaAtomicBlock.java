package org.multiverse.stms.gamma;

import org.multiverse.api.*;
import org.multiverse.api.exceptions.*;
import org.multiverse.api.closures.*;
import org.multiverse.stms.gamma.transactions.*;
import java.util.logging.Logger;

import static java.lang.String.format;
import static org.multiverse.api.ThreadLocalTransaction.*;

/**
* An GammaAtomicBlock made for the GammaStm.
*
* This code is generated.
*
* @author Peter Veentjer
*/
public final class LeanGammaAtomicBlock extends AbstractGammaAtomicBlock{
    private static final Logger logger = Logger.getLogger(LeanGammaAtomicBlock.class.getName());


    public LeanGammaAtomicBlock(final GammaTransactionFactory transactionFactory) {
        super(transactionFactory);
    }

    @Override
    public GammaTransactionFactory getTransactionFactory(){
        return transactionFactory;
    }

    @Override
    public final <E> E executeChecked(
        final AtomicClosure<E> closure)throws Exception{

        try{
            return execute(closure);
        }catch(InvisibleCheckedException e){
            throw e.getCause();
        }
    }

    @Override
    public final <E> E execute(final AtomicClosure<E> closure){

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

            tx = transactionFactory.newTransaction(pool);
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
                            if (transactionConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(format("[%s] Encountered a retry",
                                    transactionConfiguration.familyName));
                            }
                        }
                        tx.awaitUpdate();
                    } catch (SpeculativeConfigurationError e) {
                        if(TRACING_ENABLED){
                            if (transactionConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(format("[%s] Encountered a speculative configuration error",
                                    transactionConfiguration.familyName));
                            }
                        }

                        abort = false;
                        GammaTransaction old = tx;
                        tx = transactionFactory.upgradeAfterSpeculativeFailure(tx,pool);
                        pool.put(old);
                        transactionContainer.tx = tx;
                    } catch (ReadWriteConflict e) {
                        cause = e;
                        if(TRACING_ENABLED){
                            if (transactionConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(format("[%s] Encountered a read or write conflict",
                                    transactionConfiguration.familyName));
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
            if (transactionConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                logger.info(format("[%s] Maximum number of %s retries has been reached",
                    transactionConfiguration.familyName, transactionConfiguration.getMaxRetries()));
            }
        }

        throw new TooManyRetriesException(
            format("[%s] Maximum number of %s retries has been reached",
                transactionConfiguration.getFamilyName(), transactionConfiguration.getMaxRetries()), cause);
        }

     @Override
    public final  int executeChecked(
        final AtomicIntClosure closure)throws Exception{

        try{
            return execute(closure);
        }catch(InvisibleCheckedException e){
            throw e.getCause();
        }
    }

    @Override
    public final  int execute(final AtomicIntClosure closure){

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

            tx = transactionFactory.newTransaction(pool);
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
                            if (transactionConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(format("[%s] Encountered a retry",
                                    transactionConfiguration.familyName));
                            }
                        }
                        tx.awaitUpdate();
                    } catch (SpeculativeConfigurationError e) {
                        if(TRACING_ENABLED){
                            if (transactionConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(format("[%s] Encountered a speculative configuration error",
                                    transactionConfiguration.familyName));
                            }
                        }

                        abort = false;
                        GammaTransaction old = tx;
                        tx = transactionFactory.upgradeAfterSpeculativeFailure(tx,pool);
                        pool.put(old);
                        transactionContainer.tx = tx;
                    } catch (ReadWriteConflict e) {
                        cause = e;
                        if(TRACING_ENABLED){
                            if (transactionConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(format("[%s] Encountered a read or write conflict",
                                    transactionConfiguration.familyName));
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
            if (transactionConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                logger.info(format("[%s] Maximum number of %s retries has been reached",
                    transactionConfiguration.familyName, transactionConfiguration.getMaxRetries()));
            }
        }

        throw new TooManyRetriesException(
            format("[%s] Maximum number of %s retries has been reached",
                transactionConfiguration.getFamilyName(), transactionConfiguration.getMaxRetries()), cause);
        }

     @Override
    public final  long executeChecked(
        final AtomicLongClosure closure)throws Exception{

        try{
            return execute(closure);
        }catch(InvisibleCheckedException e){
            throw e.getCause();
        }
    }

    @Override
    public final  long execute(final AtomicLongClosure closure){

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

            tx = transactionFactory.newTransaction(pool);
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
                            if (transactionConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(format("[%s] Encountered a retry",
                                    transactionConfiguration.familyName));
                            }
                        }
                        tx.awaitUpdate();
                    } catch (SpeculativeConfigurationError e) {
                        if(TRACING_ENABLED){
                            if (transactionConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(format("[%s] Encountered a speculative configuration error",
                                    transactionConfiguration.familyName));
                            }
                        }

                        abort = false;
                        GammaTransaction old = tx;
                        tx = transactionFactory.upgradeAfterSpeculativeFailure(tx,pool);
                        pool.put(old);
                        transactionContainer.tx = tx;
                    } catch (ReadWriteConflict e) {
                        cause = e;
                        if(TRACING_ENABLED){
                            if (transactionConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(format("[%s] Encountered a read or write conflict",
                                    transactionConfiguration.familyName));
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
            if (transactionConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                logger.info(format("[%s] Maximum number of %s retries has been reached",
                    transactionConfiguration.familyName, transactionConfiguration.getMaxRetries()));
            }
        }

        throw new TooManyRetriesException(
            format("[%s] Maximum number of %s retries has been reached",
                transactionConfiguration.getFamilyName(), transactionConfiguration.getMaxRetries()), cause);
        }

     @Override
    public final  double executeChecked(
        final AtomicDoubleClosure closure)throws Exception{

        try{
            return execute(closure);
        }catch(InvisibleCheckedException e){
            throw e.getCause();
        }
    }

    @Override
    public final  double execute(final AtomicDoubleClosure closure){

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

            tx = transactionFactory.newTransaction(pool);
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
                            if (transactionConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(format("[%s] Encountered a retry",
                                    transactionConfiguration.familyName));
                            }
                        }
                        tx.awaitUpdate();
                    } catch (SpeculativeConfigurationError e) {
                        if(TRACING_ENABLED){
                            if (transactionConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(format("[%s] Encountered a speculative configuration error",
                                    transactionConfiguration.familyName));
                            }
                        }

                        abort = false;
                        GammaTransaction old = tx;
                        tx = transactionFactory.upgradeAfterSpeculativeFailure(tx,pool);
                        pool.put(old);
                        transactionContainer.tx = tx;
                    } catch (ReadWriteConflict e) {
                        cause = e;
                        if(TRACING_ENABLED){
                            if (transactionConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(format("[%s] Encountered a read or write conflict",
                                    transactionConfiguration.familyName));
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
            if (transactionConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                logger.info(format("[%s] Maximum number of %s retries has been reached",
                    transactionConfiguration.familyName, transactionConfiguration.getMaxRetries()));
            }
        }

        throw new TooManyRetriesException(
            format("[%s] Maximum number of %s retries has been reached",
                transactionConfiguration.getFamilyName(), transactionConfiguration.getMaxRetries()), cause);
        }

     @Override
    public final  boolean executeChecked(
        final AtomicBooleanClosure closure)throws Exception{

        try{
            return execute(closure);
        }catch(InvisibleCheckedException e){
            throw e.getCause();
        }
    }

    @Override
    public final  boolean execute(final AtomicBooleanClosure closure){

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

            tx = transactionFactory.newTransaction(pool);
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
                            if (transactionConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(format("[%s] Encountered a retry",
                                    transactionConfiguration.familyName));
                            }
                        }
                        tx.awaitUpdate();
                    } catch (SpeculativeConfigurationError e) {
                        if(TRACING_ENABLED){
                            if (transactionConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(format("[%s] Encountered a speculative configuration error",
                                    transactionConfiguration.familyName));
                            }
                        }

                        abort = false;
                        GammaTransaction old = tx;
                        tx = transactionFactory.upgradeAfterSpeculativeFailure(tx,pool);
                        pool.put(old);
                        transactionContainer.tx = tx;
                    } catch (ReadWriteConflict e) {
                        cause = e;
                        if(TRACING_ENABLED){
                            if (transactionConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(format("[%s] Encountered a read or write conflict",
                                    transactionConfiguration.familyName));
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
            if (transactionConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                logger.info(format("[%s] Maximum number of %s retries has been reached",
                    transactionConfiguration.familyName, transactionConfiguration.getMaxRetries()));
            }
        }

        throw new TooManyRetriesException(
            format("[%s] Maximum number of %s retries has been reached",
                transactionConfiguration.getFamilyName(), transactionConfiguration.getMaxRetries()), cause);
        }

     @Override
    public final  void executeChecked(
        final AtomicVoidClosure closure)throws Exception{

        try{
            execute(closure);
        }catch(InvisibleCheckedException e){
            throw e.getCause();
        }
    }

    @Override
    public final  void execute(final AtomicVoidClosure closure){

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

            tx = transactionFactory.newTransaction(pool);
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
                            if (transactionConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(format("[%s] Encountered a retry",
                                    transactionConfiguration.familyName));
                            }
                        }
                        tx.awaitUpdate();
                    } catch (SpeculativeConfigurationError e) {
                        if(TRACING_ENABLED){
                            if (transactionConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(format("[%s] Encountered a speculative configuration error",
                                    transactionConfiguration.familyName));
                            }
                        }

                        abort = false;
                        GammaTransaction old = tx;
                        tx = transactionFactory.upgradeAfterSpeculativeFailure(tx,pool);
                        pool.put(old);
                        transactionContainer.tx = tx;
                    } catch (ReadWriteConflict e) {
                        cause = e;
                        if(TRACING_ENABLED){
                            if (transactionConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(format("[%s] Encountered a read or write conflict",
                                    transactionConfiguration.familyName));
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
            if (transactionConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                logger.info(format("[%s] Maximum number of %s retries has been reached",
                    transactionConfiguration.familyName, transactionConfiguration.getMaxRetries()));
            }
        }

        throw new TooManyRetriesException(
            format("[%s] Maximum number of %s retries has been reached",
                transactionConfiguration.getFamilyName(), transactionConfiguration.getMaxRetries()), cause);
        }

   }
