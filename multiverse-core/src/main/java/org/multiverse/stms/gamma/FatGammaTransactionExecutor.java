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
public final class FatGammaTransactionExecutor extends AbstractGammaTransactionExecutor{
    private static final Logger logger = Logger.getLogger(FatGammaTransactionExecutor.class.getName());

    private final PropagationLevel propagationLevel;

    public FatGammaTransactionExecutor(final GammaTransactionFactory transactionFactory) {
        super(transactionFactory);
        this.propagationLevel = transactionConfiguration.propagationLevel;
    }

    @Override
    public GammaTransactionFactory getTransactionFactory(){
        return transactionFactory;
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

     public <E> E atomic(final AtomicClosure<E> closure){

        if(closure == null){
            throw new NullPointerException();
        }

        ThreadLocalTransaction.Container transactionContainer = getThreadLocalTransactionContainer();
        GammaTransactionPool pool = (GammaTransactionPool) transactionContainer.txPool;
        if (pool == null) {
            pool = new GammaTransactionPool();
            transactionContainer.txPool = pool;
        }

        GammaTransaction tx = (GammaTransaction)transactionContainer.tx;
        if(tx == null || !tx.isAlive()){
            tx = null;
        }

        try{
            switch (propagationLevel) {
                case Requires:
                    if (tx == null) {
                        if (TRACING_ENABLED) {
                            if (transactionConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(
                                    format("[%s] Has 'Requires' propagation level and no transaction found, starting a new transaction",
                                    transactionConfiguration.familyName));
                            }
                        }

                        tx = transactionFactory.newTransaction(pool);
                        transactionContainer.tx = tx;
                        return atomic(tx, transactionContainer, pool, closure);
                    } else {
                        if (TRACING_ENABLED) {
                            if (transactionConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(
                                    format("[%s] Has 'Requires' propagation level, and existing transaction [%s] found",
                                        transactionConfiguration.familyName, tx.getConfiguration().getFamilyName()));
                                }
                            }

                        return closure.execute(tx);
                    }
                case Mandatory:
                    if (tx == null) {
                        if (TRACING_ENABLED) {
                            if (transactionConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(
                                    format("[%s] Has 'Mandatory' propagation level, and no transaction is found",
                                        transactionConfiguration.familyName));
                                }
                            }
                            throw new TransactionMandatoryException(
                                format("No transaction is found for TransactionExecutor '%s' with 'Mandatory' propagation level",
                                    transactionConfiguration.familyName));
                        }

                    if (TRACING_ENABLED) {
                        if (transactionConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                            logger.info(
                                format("[%s] Has 'Mandatory' propagation level and transaction [%s] found",
                                    transactionConfiguration.familyName, tx.getConfiguration().getFamilyName()));
                        }
                    }
                    return closure.execute(tx);
                case Never:
                    if (tx != null) {
                        if (TRACING_ENABLED) {
                            if (transactionConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(
                                    format("[%s] Has 'Never' propagation level, but transaction [%s] is found",
                                        transactionConfiguration.familyName, tx.getConfiguration().getFamilyName()));
                            }
                        }

                        throw new TransactionNotAllowedException(
                            format("No transaction is allowed for TransactionExecutor '%s' with propagation level 'Never'"+
                                ", but transaction '%s' was found",
                                    transactionConfiguration.familyName, tx.getConfiguration().getFamilyName())
                            );
                    }

                    if (TRACING_ENABLED) {
                        if (transactionConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                            logger.info(
                                format("[%s] Has 'Never' propagation level and no transaction is found",
                                    transactionConfiguration.familyName));
                        }
                    }
                    return closure.execute(null);
                case RequiresNew:
                    if (tx == null) {
                        if(TRACING_ENABLED){
                            if (transactionConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(
                                    format("[%s] Has 'RequiresNew' propagation level and no transaction is found, starting new transaction",
                                        transactionConfiguration.familyName));
                            }
                        }

                        tx = transactionFactory.newTransaction(pool);
                        transactionContainer.tx = tx;
                        return atomic(tx, transactionContainer, pool, closure);
                    } else {
                        if(TRACING_ENABLED){
                            if (transactionConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(
                                    format("[%s] Has 'RequiresNew' propagationLevel and existing transaction [%s] was found",
                                        transactionConfiguration.familyName, tx.getConfiguration().getFamilyName()));
                            }
                        }

                        GammaTransaction suspendedTransaction = tx;
                        tx = transactionFactory.newTransaction(pool);
                        transactionContainer.tx = tx;
                        try {
                            return atomic(tx, transactionContainer, pool, closure);
                        } finally {
                            transactionContainer.tx = suspendedTransaction;
                        }
                    }
                case Supports:
                    if(TRACING_ENABLED){
                        if(tx!=null){
                            if (transactionConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(
                                    format("[%s] Has 'RequiresNew' propagationLevel and existing transaction [%s] was found",
                                        transactionConfiguration.familyName, tx.getConfiguration().getFamilyName()));
                            }
                        }else{
                            if (transactionConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(
                                    format("[%s] Has 'RequiresNew' propagationLevel and existing transaction [%s] was found",
                                        transactionConfiguration.familyName, tx.getConfiguration().getFamilyName()));
                            }
                        }
                    }

                    return closure.execute(tx);
                default:
                    throw new IllegalStateException();
            }
        }catch(RuntimeException e){
            throw e;
        }catch(Exception e){
            throw new InvisibleCheckedException(e);
        }
    }

    private <E> E atomic(
        GammaTransaction tx, final ThreadLocalTransaction.Container transactionContainer, GammaTransactionPool pool, final AtomicClosure<E> closure)throws Exception{
        Error cause = null;

        try{
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
    public final  int atomicChecked(
        final AtomicIntClosure closure)throws Exception{

        try{
            return atomic(closure);
        }catch(InvisibleCheckedException e){
            throw e.getCause();
        }
    }

     public  int atomic(final AtomicIntClosure closure){

        if(closure == null){
            throw new NullPointerException();
        }

        ThreadLocalTransaction.Container transactionContainer = getThreadLocalTransactionContainer();
        GammaTransactionPool pool = (GammaTransactionPool) transactionContainer.txPool;
        if (pool == null) {
            pool = new GammaTransactionPool();
            transactionContainer.txPool = pool;
        }

        GammaTransaction tx = (GammaTransaction)transactionContainer.tx;
        if(tx == null || !tx.isAlive()){
            tx = null;
        }

        try{
            switch (propagationLevel) {
                case Requires:
                    if (tx == null) {
                        if (TRACING_ENABLED) {
                            if (transactionConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(
                                    format("[%s] Has 'Requires' propagation level and no transaction found, starting a new transaction",
                                    transactionConfiguration.familyName));
                            }
                        }

                        tx = transactionFactory.newTransaction(pool);
                        transactionContainer.tx = tx;
                        return atomic(tx, transactionContainer, pool, closure);
                    } else {
                        if (TRACING_ENABLED) {
                            if (transactionConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(
                                    format("[%s] Has 'Requires' propagation level, and existing transaction [%s] found",
                                        transactionConfiguration.familyName, tx.getConfiguration().getFamilyName()));
                                }
                            }

                        return closure.execute(tx);
                    }
                case Mandatory:
                    if (tx == null) {
                        if (TRACING_ENABLED) {
                            if (transactionConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(
                                    format("[%s] Has 'Mandatory' propagation level, and no transaction is found",
                                        transactionConfiguration.familyName));
                                }
                            }
                            throw new TransactionMandatoryException(
                                format("No transaction is found for TransactionExecutor '%s' with 'Mandatory' propagation level",
                                    transactionConfiguration.familyName));
                        }

                    if (TRACING_ENABLED) {
                        if (transactionConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                            logger.info(
                                format("[%s] Has 'Mandatory' propagation level and transaction [%s] found",
                                    transactionConfiguration.familyName, tx.getConfiguration().getFamilyName()));
                        }
                    }
                    return closure.execute(tx);
                case Never:
                    if (tx != null) {
                        if (TRACING_ENABLED) {
                            if (transactionConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(
                                    format("[%s] Has 'Never' propagation level, but transaction [%s] is found",
                                        transactionConfiguration.familyName, tx.getConfiguration().getFamilyName()));
                            }
                        }

                        throw new TransactionNotAllowedException(
                            format("No transaction is allowed for TransactionExecutor '%s' with propagation level 'Never'"+
                                ", but transaction '%s' was found",
                                    transactionConfiguration.familyName, tx.getConfiguration().getFamilyName())
                            );
                    }

                    if (TRACING_ENABLED) {
                        if (transactionConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                            logger.info(
                                format("[%s] Has 'Never' propagation level and no transaction is found",
                                    transactionConfiguration.familyName));
                        }
                    }
                    return closure.execute(null);
                case RequiresNew:
                    if (tx == null) {
                        if(TRACING_ENABLED){
                            if (transactionConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(
                                    format("[%s] Has 'RequiresNew' propagation level and no transaction is found, starting new transaction",
                                        transactionConfiguration.familyName));
                            }
                        }

                        tx = transactionFactory.newTransaction(pool);
                        transactionContainer.tx = tx;
                        return atomic(tx, transactionContainer, pool, closure);
                    } else {
                        if(TRACING_ENABLED){
                            if (transactionConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(
                                    format("[%s] Has 'RequiresNew' propagationLevel and existing transaction [%s] was found",
                                        transactionConfiguration.familyName, tx.getConfiguration().getFamilyName()));
                            }
                        }

                        GammaTransaction suspendedTransaction = tx;
                        tx = transactionFactory.newTransaction(pool);
                        transactionContainer.tx = tx;
                        try {
                            return atomic(tx, transactionContainer, pool, closure);
                        } finally {
                            transactionContainer.tx = suspendedTransaction;
                        }
                    }
                case Supports:
                    if(TRACING_ENABLED){
                        if(tx!=null){
                            if (transactionConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(
                                    format("[%s] Has 'RequiresNew' propagationLevel and existing transaction [%s] was found",
                                        transactionConfiguration.familyName, tx.getConfiguration().getFamilyName()));
                            }
                        }else{
                            if (transactionConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(
                                    format("[%s] Has 'RequiresNew' propagationLevel and existing transaction [%s] was found",
                                        transactionConfiguration.familyName, tx.getConfiguration().getFamilyName()));
                            }
                        }
                    }

                    return closure.execute(tx);
                default:
                    throw new IllegalStateException();
            }
        }catch(RuntimeException e){
            throw e;
        }catch(Exception e){
            throw new InvisibleCheckedException(e);
        }
    }

    private  int atomic(
        GammaTransaction tx, final ThreadLocalTransaction.Container transactionContainer, GammaTransactionPool pool, final AtomicIntClosure closure)throws Exception{
        Error cause = null;

        try{
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
    public final  long atomicChecked(
        final AtomicLongClosure closure)throws Exception{

        try{
            return atomic(closure);
        }catch(InvisibleCheckedException e){
            throw e.getCause();
        }
    }

     public  long atomic(final AtomicLongClosure closure){

        if(closure == null){
            throw new NullPointerException();
        }

        ThreadLocalTransaction.Container transactionContainer = getThreadLocalTransactionContainer();
        GammaTransactionPool pool = (GammaTransactionPool) transactionContainer.txPool;
        if (pool == null) {
            pool = new GammaTransactionPool();
            transactionContainer.txPool = pool;
        }

        GammaTransaction tx = (GammaTransaction)transactionContainer.tx;
        if(tx == null || !tx.isAlive()){
            tx = null;
        }

        try{
            switch (propagationLevel) {
                case Requires:
                    if (tx == null) {
                        if (TRACING_ENABLED) {
                            if (transactionConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(
                                    format("[%s] Has 'Requires' propagation level and no transaction found, starting a new transaction",
                                    transactionConfiguration.familyName));
                            }
                        }

                        tx = transactionFactory.newTransaction(pool);
                        transactionContainer.tx = tx;
                        return atomic(tx, transactionContainer, pool, closure);
                    } else {
                        if (TRACING_ENABLED) {
                            if (transactionConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(
                                    format("[%s] Has 'Requires' propagation level, and existing transaction [%s] found",
                                        transactionConfiguration.familyName, tx.getConfiguration().getFamilyName()));
                                }
                            }

                        return closure.execute(tx);
                    }
                case Mandatory:
                    if (tx == null) {
                        if (TRACING_ENABLED) {
                            if (transactionConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(
                                    format("[%s] Has 'Mandatory' propagation level, and no transaction is found",
                                        transactionConfiguration.familyName));
                                }
                            }
                            throw new TransactionMandatoryException(
                                format("No transaction is found for TransactionExecutor '%s' with 'Mandatory' propagation level",
                                    transactionConfiguration.familyName));
                        }

                    if (TRACING_ENABLED) {
                        if (transactionConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                            logger.info(
                                format("[%s] Has 'Mandatory' propagation level and transaction [%s] found",
                                    transactionConfiguration.familyName, tx.getConfiguration().getFamilyName()));
                        }
                    }
                    return closure.execute(tx);
                case Never:
                    if (tx != null) {
                        if (TRACING_ENABLED) {
                            if (transactionConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(
                                    format("[%s] Has 'Never' propagation level, but transaction [%s] is found",
                                        transactionConfiguration.familyName, tx.getConfiguration().getFamilyName()));
                            }
                        }

                        throw new TransactionNotAllowedException(
                            format("No transaction is allowed for TransactionExecutor '%s' with propagation level 'Never'"+
                                ", but transaction '%s' was found",
                                    transactionConfiguration.familyName, tx.getConfiguration().getFamilyName())
                            );
                    }

                    if (TRACING_ENABLED) {
                        if (transactionConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                            logger.info(
                                format("[%s] Has 'Never' propagation level and no transaction is found",
                                    transactionConfiguration.familyName));
                        }
                    }
                    return closure.execute(null);
                case RequiresNew:
                    if (tx == null) {
                        if(TRACING_ENABLED){
                            if (transactionConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(
                                    format("[%s] Has 'RequiresNew' propagation level and no transaction is found, starting new transaction",
                                        transactionConfiguration.familyName));
                            }
                        }

                        tx = transactionFactory.newTransaction(pool);
                        transactionContainer.tx = tx;
                        return atomic(tx, transactionContainer, pool, closure);
                    } else {
                        if(TRACING_ENABLED){
                            if (transactionConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(
                                    format("[%s] Has 'RequiresNew' propagationLevel and existing transaction [%s] was found",
                                        transactionConfiguration.familyName, tx.getConfiguration().getFamilyName()));
                            }
                        }

                        GammaTransaction suspendedTransaction = tx;
                        tx = transactionFactory.newTransaction(pool);
                        transactionContainer.tx = tx;
                        try {
                            return atomic(tx, transactionContainer, pool, closure);
                        } finally {
                            transactionContainer.tx = suspendedTransaction;
                        }
                    }
                case Supports:
                    if(TRACING_ENABLED){
                        if(tx!=null){
                            if (transactionConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(
                                    format("[%s] Has 'RequiresNew' propagationLevel and existing transaction [%s] was found",
                                        transactionConfiguration.familyName, tx.getConfiguration().getFamilyName()));
                            }
                        }else{
                            if (transactionConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(
                                    format("[%s] Has 'RequiresNew' propagationLevel and existing transaction [%s] was found",
                                        transactionConfiguration.familyName, tx.getConfiguration().getFamilyName()));
                            }
                        }
                    }

                    return closure.execute(tx);
                default:
                    throw new IllegalStateException();
            }
        }catch(RuntimeException e){
            throw e;
        }catch(Exception e){
            throw new InvisibleCheckedException(e);
        }
    }

    private  long atomic(
        GammaTransaction tx, final ThreadLocalTransaction.Container transactionContainer, GammaTransactionPool pool, final AtomicLongClosure closure)throws Exception{
        Error cause = null;

        try{
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
    public final  double atomicChecked(
        final AtomicDoubleClosure closure)throws Exception{

        try{
            return atomic(closure);
        }catch(InvisibleCheckedException e){
            throw e.getCause();
        }
    }

     public  double atomic(final AtomicDoubleClosure closure){

        if(closure == null){
            throw new NullPointerException();
        }

        ThreadLocalTransaction.Container transactionContainer = getThreadLocalTransactionContainer();
        GammaTransactionPool pool = (GammaTransactionPool) transactionContainer.txPool;
        if (pool == null) {
            pool = new GammaTransactionPool();
            transactionContainer.txPool = pool;
        }

        GammaTransaction tx = (GammaTransaction)transactionContainer.tx;
        if(tx == null || !tx.isAlive()){
            tx = null;
        }

        try{
            switch (propagationLevel) {
                case Requires:
                    if (tx == null) {
                        if (TRACING_ENABLED) {
                            if (transactionConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(
                                    format("[%s] Has 'Requires' propagation level and no transaction found, starting a new transaction",
                                    transactionConfiguration.familyName));
                            }
                        }

                        tx = transactionFactory.newTransaction(pool);
                        transactionContainer.tx = tx;
                        return atomic(tx, transactionContainer, pool, closure);
                    } else {
                        if (TRACING_ENABLED) {
                            if (transactionConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(
                                    format("[%s] Has 'Requires' propagation level, and existing transaction [%s] found",
                                        transactionConfiguration.familyName, tx.getConfiguration().getFamilyName()));
                                }
                            }

                        return closure.execute(tx);
                    }
                case Mandatory:
                    if (tx == null) {
                        if (TRACING_ENABLED) {
                            if (transactionConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(
                                    format("[%s] Has 'Mandatory' propagation level, and no transaction is found",
                                        transactionConfiguration.familyName));
                                }
                            }
                            throw new TransactionMandatoryException(
                                format("No transaction is found for TransactionExecutor '%s' with 'Mandatory' propagation level",
                                    transactionConfiguration.familyName));
                        }

                    if (TRACING_ENABLED) {
                        if (transactionConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                            logger.info(
                                format("[%s] Has 'Mandatory' propagation level and transaction [%s] found",
                                    transactionConfiguration.familyName, tx.getConfiguration().getFamilyName()));
                        }
                    }
                    return closure.execute(tx);
                case Never:
                    if (tx != null) {
                        if (TRACING_ENABLED) {
                            if (transactionConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(
                                    format("[%s] Has 'Never' propagation level, but transaction [%s] is found",
                                        transactionConfiguration.familyName, tx.getConfiguration().getFamilyName()));
                            }
                        }

                        throw new TransactionNotAllowedException(
                            format("No transaction is allowed for TransactionExecutor '%s' with propagation level 'Never'"+
                                ", but transaction '%s' was found",
                                    transactionConfiguration.familyName, tx.getConfiguration().getFamilyName())
                            );
                    }

                    if (TRACING_ENABLED) {
                        if (transactionConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                            logger.info(
                                format("[%s] Has 'Never' propagation level and no transaction is found",
                                    transactionConfiguration.familyName));
                        }
                    }
                    return closure.execute(null);
                case RequiresNew:
                    if (tx == null) {
                        if(TRACING_ENABLED){
                            if (transactionConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(
                                    format("[%s] Has 'RequiresNew' propagation level and no transaction is found, starting new transaction",
                                        transactionConfiguration.familyName));
                            }
                        }

                        tx = transactionFactory.newTransaction(pool);
                        transactionContainer.tx = tx;
                        return atomic(tx, transactionContainer, pool, closure);
                    } else {
                        if(TRACING_ENABLED){
                            if (transactionConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(
                                    format("[%s] Has 'RequiresNew' propagationLevel and existing transaction [%s] was found",
                                        transactionConfiguration.familyName, tx.getConfiguration().getFamilyName()));
                            }
                        }

                        GammaTransaction suspendedTransaction = tx;
                        tx = transactionFactory.newTransaction(pool);
                        transactionContainer.tx = tx;
                        try {
                            return atomic(tx, transactionContainer, pool, closure);
                        } finally {
                            transactionContainer.tx = suspendedTransaction;
                        }
                    }
                case Supports:
                    if(TRACING_ENABLED){
                        if(tx!=null){
                            if (transactionConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(
                                    format("[%s] Has 'RequiresNew' propagationLevel and existing transaction [%s] was found",
                                        transactionConfiguration.familyName, tx.getConfiguration().getFamilyName()));
                            }
                        }else{
                            if (transactionConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(
                                    format("[%s] Has 'RequiresNew' propagationLevel and existing transaction [%s] was found",
                                        transactionConfiguration.familyName, tx.getConfiguration().getFamilyName()));
                            }
                        }
                    }

                    return closure.execute(tx);
                default:
                    throw new IllegalStateException();
            }
        }catch(RuntimeException e){
            throw e;
        }catch(Exception e){
            throw new InvisibleCheckedException(e);
        }
    }

    private  double atomic(
        GammaTransaction tx, final ThreadLocalTransaction.Container transactionContainer, GammaTransactionPool pool, final AtomicDoubleClosure closure)throws Exception{
        Error cause = null;

        try{
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
    public final  boolean atomicChecked(
        final AtomicBooleanClosure closure)throws Exception{

        try{
            return atomic(closure);
        }catch(InvisibleCheckedException e){
            throw e.getCause();
        }
    }

     public  boolean atomic(final AtomicBooleanClosure closure){

        if(closure == null){
            throw new NullPointerException();
        }

        ThreadLocalTransaction.Container transactionContainer = getThreadLocalTransactionContainer();
        GammaTransactionPool pool = (GammaTransactionPool) transactionContainer.txPool;
        if (pool == null) {
            pool = new GammaTransactionPool();
            transactionContainer.txPool = pool;
        }

        GammaTransaction tx = (GammaTransaction)transactionContainer.tx;
        if(tx == null || !tx.isAlive()){
            tx = null;
        }

        try{
            switch (propagationLevel) {
                case Requires:
                    if (tx == null) {
                        if (TRACING_ENABLED) {
                            if (transactionConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(
                                    format("[%s] Has 'Requires' propagation level and no transaction found, starting a new transaction",
                                    transactionConfiguration.familyName));
                            }
                        }

                        tx = transactionFactory.newTransaction(pool);
                        transactionContainer.tx = tx;
                        return atomic(tx, transactionContainer, pool, closure);
                    } else {
                        if (TRACING_ENABLED) {
                            if (transactionConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(
                                    format("[%s] Has 'Requires' propagation level, and existing transaction [%s] found",
                                        transactionConfiguration.familyName, tx.getConfiguration().getFamilyName()));
                                }
                            }

                        return closure.execute(tx);
                    }
                case Mandatory:
                    if (tx == null) {
                        if (TRACING_ENABLED) {
                            if (transactionConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(
                                    format("[%s] Has 'Mandatory' propagation level, and no transaction is found",
                                        transactionConfiguration.familyName));
                                }
                            }
                            throw new TransactionMandatoryException(
                                format("No transaction is found for TransactionExecutor '%s' with 'Mandatory' propagation level",
                                    transactionConfiguration.familyName));
                        }

                    if (TRACING_ENABLED) {
                        if (transactionConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                            logger.info(
                                format("[%s] Has 'Mandatory' propagation level and transaction [%s] found",
                                    transactionConfiguration.familyName, tx.getConfiguration().getFamilyName()));
                        }
                    }
                    return closure.execute(tx);
                case Never:
                    if (tx != null) {
                        if (TRACING_ENABLED) {
                            if (transactionConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(
                                    format("[%s] Has 'Never' propagation level, but transaction [%s] is found",
                                        transactionConfiguration.familyName, tx.getConfiguration().getFamilyName()));
                            }
                        }

                        throw new TransactionNotAllowedException(
                            format("No transaction is allowed for TransactionExecutor '%s' with propagation level 'Never'"+
                                ", but transaction '%s' was found",
                                    transactionConfiguration.familyName, tx.getConfiguration().getFamilyName())
                            );
                    }

                    if (TRACING_ENABLED) {
                        if (transactionConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                            logger.info(
                                format("[%s] Has 'Never' propagation level and no transaction is found",
                                    transactionConfiguration.familyName));
                        }
                    }
                    return closure.execute(null);
                case RequiresNew:
                    if (tx == null) {
                        if(TRACING_ENABLED){
                            if (transactionConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(
                                    format("[%s] Has 'RequiresNew' propagation level and no transaction is found, starting new transaction",
                                        transactionConfiguration.familyName));
                            }
                        }

                        tx = transactionFactory.newTransaction(pool);
                        transactionContainer.tx = tx;
                        return atomic(tx, transactionContainer, pool, closure);
                    } else {
                        if(TRACING_ENABLED){
                            if (transactionConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(
                                    format("[%s] Has 'RequiresNew' propagationLevel and existing transaction [%s] was found",
                                        transactionConfiguration.familyName, tx.getConfiguration().getFamilyName()));
                            }
                        }

                        GammaTransaction suspendedTransaction = tx;
                        tx = transactionFactory.newTransaction(pool);
                        transactionContainer.tx = tx;
                        try {
                            return atomic(tx, transactionContainer, pool, closure);
                        } finally {
                            transactionContainer.tx = suspendedTransaction;
                        }
                    }
                case Supports:
                    if(TRACING_ENABLED){
                        if(tx!=null){
                            if (transactionConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(
                                    format("[%s] Has 'RequiresNew' propagationLevel and existing transaction [%s] was found",
                                        transactionConfiguration.familyName, tx.getConfiguration().getFamilyName()));
                            }
                        }else{
                            if (transactionConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(
                                    format("[%s] Has 'RequiresNew' propagationLevel and existing transaction [%s] was found",
                                        transactionConfiguration.familyName, tx.getConfiguration().getFamilyName()));
                            }
                        }
                    }

                    return closure.execute(tx);
                default:
                    throw new IllegalStateException();
            }
        }catch(RuntimeException e){
            throw e;
        }catch(Exception e){
            throw new InvisibleCheckedException(e);
        }
    }

    private  boolean atomic(
        GammaTransaction tx, final ThreadLocalTransaction.Container transactionContainer, GammaTransactionPool pool, final AtomicBooleanClosure closure)throws Exception{
        Error cause = null;

        try{
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
    public final  void atomicChecked(
        final AtomicVoidClosure closure)throws Exception{

        try{
            atomic(closure);
        }catch(InvisibleCheckedException e){
            throw e.getCause();
        }
    }

     public  void atomic(final AtomicVoidClosure closure){

        if(closure == null){
            throw new NullPointerException();
        }

        ThreadLocalTransaction.Container transactionContainer = getThreadLocalTransactionContainer();
        GammaTransactionPool pool = (GammaTransactionPool) transactionContainer.txPool;
        if (pool == null) {
            pool = new GammaTransactionPool();
            transactionContainer.txPool = pool;
        }

        GammaTransaction tx = (GammaTransaction)transactionContainer.tx;
        if(tx == null || !tx.isAlive()){
            tx = null;
        }

        try{
            switch (propagationLevel) {
                case Requires:
                    if (tx == null) {
                        if (TRACING_ENABLED) {
                            if (transactionConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(
                                    format("[%s] Has 'Requires' propagation level and no transaction found, starting a new transaction",
                                    transactionConfiguration.familyName));
                            }
                        }

                        tx = transactionFactory.newTransaction(pool);
                        transactionContainer.tx = tx;
                        atomic(tx, transactionContainer,pool, closure);
                        return;
                    } else {
                        if (TRACING_ENABLED) {
                            if (transactionConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(
                                    format("[%s] Has 'Requires' propagation level, and existing transaction [%s] found",
                                        transactionConfiguration.familyName, tx.getConfiguration().getFamilyName()));
                                }
                            }

                        closure.execute(tx);
                        return;
                    }
                case Mandatory:
                    if (tx == null) {
                        if (TRACING_ENABLED) {
                            if (transactionConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(
                                    format("[%s] Has 'Mandatory' propagation level, and no transaction is found",
                                        transactionConfiguration.familyName));
                                }
                            }
                            throw new TransactionMandatoryException(
                                format("No transaction is found for TransactionExecutor '%s' with 'Mandatory' propagation level",
                                    transactionConfiguration.familyName));
                        }

                    if (TRACING_ENABLED) {
                        if (transactionConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                            logger.info(
                                format("[%s] Has 'Mandatory' propagation level and transaction [%s] found",
                                    transactionConfiguration.familyName, tx.getConfiguration().getFamilyName()));
                        }
                    }
                    closure.execute(tx);
                    return;
                case Never:
                    if (tx != null) {
                        if (TRACING_ENABLED) {
                            if (transactionConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(
                                    format("[%s] Has 'Never' propagation level, but transaction [%s] is found",
                                        transactionConfiguration.familyName, tx.getConfiguration().getFamilyName()));
                            }
                        }

                        throw new TransactionNotAllowedException(
                            format("No transaction is allowed for TransactionExecutor '%s' with propagation level 'Never'"+
                                ", but transaction '%s' was found",
                                    transactionConfiguration.familyName, tx.getConfiguration().getFamilyName())
                            );
                    }

                    if (TRACING_ENABLED) {
                        if (transactionConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                            logger.info(
                                format("[%s] Has 'Never' propagation level and no transaction is found",
                                    transactionConfiguration.familyName));
                        }
                    }
                    closure.execute(null);
                    return;
                case RequiresNew:
                    if (tx == null) {
                        if(TRACING_ENABLED){
                            if (transactionConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(
                                    format("[%s] Has 'RequiresNew' propagation level and no transaction is found, starting new transaction",
                                        transactionConfiguration.familyName));
                            }
                        }

                        tx = transactionFactory.newTransaction(pool);
                        transactionContainer.tx = tx;
                        atomic(tx, transactionContainer, pool, closure);
                        return;
                    } else {
                        if(TRACING_ENABLED){
                            if (transactionConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(
                                    format("[%s] Has 'RequiresNew' propagationLevel and existing transaction [%s] was found",
                                        transactionConfiguration.familyName, tx.getConfiguration().getFamilyName()));
                            }
                        }

                        GammaTransaction suspendedTransaction = tx;
                        tx = transactionFactory.newTransaction(pool);
                        transactionContainer.tx = tx;
                        try {
                            atomic(tx, transactionContainer, pool, closure);
                            return;
                        } finally {
                            transactionContainer.tx = suspendedTransaction;
                        }
                    }
                case Supports:
                    if(TRACING_ENABLED){
                        if(tx!=null){
                            if (transactionConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(
                                    format("[%s] Has 'RequiresNew' propagationLevel and existing transaction [%s] was found",
                                        transactionConfiguration.familyName, tx.getConfiguration().getFamilyName()));
                            }
                        }else{
                            if (transactionConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(
                                    format("[%s] Has 'RequiresNew' propagationLevel and existing transaction [%s] was found",
                                        transactionConfiguration.familyName, tx.getConfiguration().getFamilyName()));
                            }
                        }
                    }

                    closure.execute(tx);
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

    private  void atomic(
        GammaTransaction tx, final ThreadLocalTransaction.Container transactionContainer, GammaTransactionPool pool, final AtomicVoidClosure closure)throws Exception{
        Error cause = null;

        try{
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
