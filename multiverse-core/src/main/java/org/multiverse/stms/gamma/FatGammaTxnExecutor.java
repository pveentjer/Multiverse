package org.multiverse.stms.gamma;

import org.multiverse.api.*;
import org.multiverse.api.exceptions.*;
import org.multiverse.api.closures.*;
import org.multiverse.stms.gamma.transactions.*;
import java.util.logging.Logger;

import static java.lang.String.format;
import static org.multiverse.api.ThreadLocalTransaction.*;

/**
* An GammaTxnExecutor made for the GammaStm.
*
* This code is generated.
*
* @author Peter Veentjer
*/
public final class FatGammaTxnExecutor extends AbstractGammaTxnExecutor{
    private static final Logger logger = Logger.getLogger(FatGammaTxnExecutor.class.getName());

    private final PropagationLevel propagationLevel;

    public FatGammaTxnExecutor(final GammaTxnFactory txnFactory) {
        super(txnFactory);
        this.propagationLevel = txnConfiguration.propagationLevel;
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
                            if (txnConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(
                                    format("[%s] Has 'Requires' propagation level and no transaction found, starting a new transaction",
                                    txnConfiguration.familyName));
                            }
                        }

                        tx = txnFactory.newTransaction(pool);
                        transactionContainer.tx = tx;
                        return atomic(tx, transactionContainer, pool, closure);
                    } else {
                        if (TRACING_ENABLED) {
                            if (txnConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(
                                    format("[%s] Has 'Requires' propagation level, and existing transaction [%s] found",
                                        txnConfiguration.familyName, tx.getConfiguration().getFamilyName()));
                                }
                            }

                        return closure.execute(tx);
                    }
                case Mandatory:
                    if (tx == null) {
                        if (TRACING_ENABLED) {
                            if (txnConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(
                                    format("[%s] Has 'Mandatory' propagation level, and no transaction is found",
                                        txnConfiguration.familyName));
                                }
                            }
                            throw new TransactionMandatoryException(
                                format("No transaction is found for TxnExecutor '%s' with 'Mandatory' propagation level",
                                    txnConfiguration.familyName));
                        }

                    if (TRACING_ENABLED) {
                        if (txnConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                            logger.info(
                                format("[%s] Has 'Mandatory' propagation level and transaction [%s] found",
                                    txnConfiguration.familyName, tx.getConfiguration().getFamilyName()));
                        }
                    }
                    return closure.execute(tx);
                case Never:
                    if (tx != null) {
                        if (TRACING_ENABLED) {
                            if (txnConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(
                                    format("[%s] Has 'Never' propagation level, but transaction [%s] is found",
                                        txnConfiguration.familyName, tx.getConfiguration().getFamilyName()));
                            }
                        }

                        throw new TransactionNotAllowedException(
                            format("No transaction is allowed for TxnExecutor '%s' with propagation level 'Never'"+
                                ", but transaction '%s' was found",
                                    txnConfiguration.familyName, tx.getConfiguration().getFamilyName())
                            );
                    }

                    if (TRACING_ENABLED) {
                        if (txnConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                            logger.info(
                                format("[%s] Has 'Never' propagation level and no transaction is found",
                                    txnConfiguration.familyName));
                        }
                    }
                    return closure.execute(null);
                case RequiresNew:
                    if (tx == null) {
                        if(TRACING_ENABLED){
                            if (txnConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(
                                    format("[%s] Has 'RequiresNew' propagation level and no transaction is found, starting new transaction",
                                        txnConfiguration.familyName));
                            }
                        }

                        tx = txnFactory.newTransaction(pool);
                        transactionContainer.tx = tx;
                        return atomic(tx, transactionContainer, pool, closure);
                    } else {
                        if(TRACING_ENABLED){
                            if (txnConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(
                                    format("[%s] Has 'RequiresNew' propagationLevel and existing transaction [%s] was found",
                                        txnConfiguration.familyName, tx.getConfiguration().getFamilyName()));
                            }
                        }

                        GammaTransaction suspendedTransaction = tx;
                        tx = txnFactory.newTransaction(pool);
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
                            if (txnConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(
                                    format("[%s] Has 'RequiresNew' propagationLevel and existing transaction [%s] was found",
                                        txnConfiguration.familyName, tx.getConfiguration().getFamilyName()));
                            }
                        }else{
                            if (txnConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(
                                    format("[%s] Has 'RequiresNew' propagationLevel and existing transaction [%s] was found",
                                        txnConfiguration.familyName, tx.getConfiguration().getFamilyName()));
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
                            if (txnConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(
                                    format("[%s] Has 'Requires' propagation level and no transaction found, starting a new transaction",
                                    txnConfiguration.familyName));
                            }
                        }

                        tx = txnFactory.newTransaction(pool);
                        transactionContainer.tx = tx;
                        return atomic(tx, transactionContainer, pool, closure);
                    } else {
                        if (TRACING_ENABLED) {
                            if (txnConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(
                                    format("[%s] Has 'Requires' propagation level, and existing transaction [%s] found",
                                        txnConfiguration.familyName, tx.getConfiguration().getFamilyName()));
                                }
                            }

                        return closure.execute(tx);
                    }
                case Mandatory:
                    if (tx == null) {
                        if (TRACING_ENABLED) {
                            if (txnConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(
                                    format("[%s] Has 'Mandatory' propagation level, and no transaction is found",
                                        txnConfiguration.familyName));
                                }
                            }
                            throw new TransactionMandatoryException(
                                format("No transaction is found for TxnExecutor '%s' with 'Mandatory' propagation level",
                                    txnConfiguration.familyName));
                        }

                    if (TRACING_ENABLED) {
                        if (txnConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                            logger.info(
                                format("[%s] Has 'Mandatory' propagation level and transaction [%s] found",
                                    txnConfiguration.familyName, tx.getConfiguration().getFamilyName()));
                        }
                    }
                    return closure.execute(tx);
                case Never:
                    if (tx != null) {
                        if (TRACING_ENABLED) {
                            if (txnConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(
                                    format("[%s] Has 'Never' propagation level, but transaction [%s] is found",
                                        txnConfiguration.familyName, tx.getConfiguration().getFamilyName()));
                            }
                        }

                        throw new TransactionNotAllowedException(
                            format("No transaction is allowed for TxnExecutor '%s' with propagation level 'Never'"+
                                ", but transaction '%s' was found",
                                    txnConfiguration.familyName, tx.getConfiguration().getFamilyName())
                            );
                    }

                    if (TRACING_ENABLED) {
                        if (txnConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                            logger.info(
                                format("[%s] Has 'Never' propagation level and no transaction is found",
                                    txnConfiguration.familyName));
                        }
                    }
                    return closure.execute(null);
                case RequiresNew:
                    if (tx == null) {
                        if(TRACING_ENABLED){
                            if (txnConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(
                                    format("[%s] Has 'RequiresNew' propagation level and no transaction is found, starting new transaction",
                                        txnConfiguration.familyName));
                            }
                        }

                        tx = txnFactory.newTransaction(pool);
                        transactionContainer.tx = tx;
                        return atomic(tx, transactionContainer, pool, closure);
                    } else {
                        if(TRACING_ENABLED){
                            if (txnConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(
                                    format("[%s] Has 'RequiresNew' propagationLevel and existing transaction [%s] was found",
                                        txnConfiguration.familyName, tx.getConfiguration().getFamilyName()));
                            }
                        }

                        GammaTransaction suspendedTransaction = tx;
                        tx = txnFactory.newTransaction(pool);
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
                            if (txnConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(
                                    format("[%s] Has 'RequiresNew' propagationLevel and existing transaction [%s] was found",
                                        txnConfiguration.familyName, tx.getConfiguration().getFamilyName()));
                            }
                        }else{
                            if (txnConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(
                                    format("[%s] Has 'RequiresNew' propagationLevel and existing transaction [%s] was found",
                                        txnConfiguration.familyName, tx.getConfiguration().getFamilyName()));
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
                            if (txnConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(
                                    format("[%s] Has 'Requires' propagation level and no transaction found, starting a new transaction",
                                    txnConfiguration.familyName));
                            }
                        }

                        tx = txnFactory.newTransaction(pool);
                        transactionContainer.tx = tx;
                        return atomic(tx, transactionContainer, pool, closure);
                    } else {
                        if (TRACING_ENABLED) {
                            if (txnConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(
                                    format("[%s] Has 'Requires' propagation level, and existing transaction [%s] found",
                                        txnConfiguration.familyName, tx.getConfiguration().getFamilyName()));
                                }
                            }

                        return closure.execute(tx);
                    }
                case Mandatory:
                    if (tx == null) {
                        if (TRACING_ENABLED) {
                            if (txnConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(
                                    format("[%s] Has 'Mandatory' propagation level, and no transaction is found",
                                        txnConfiguration.familyName));
                                }
                            }
                            throw new TransactionMandatoryException(
                                format("No transaction is found for TxnExecutor '%s' with 'Mandatory' propagation level",
                                    txnConfiguration.familyName));
                        }

                    if (TRACING_ENABLED) {
                        if (txnConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                            logger.info(
                                format("[%s] Has 'Mandatory' propagation level and transaction [%s] found",
                                    txnConfiguration.familyName, tx.getConfiguration().getFamilyName()));
                        }
                    }
                    return closure.execute(tx);
                case Never:
                    if (tx != null) {
                        if (TRACING_ENABLED) {
                            if (txnConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(
                                    format("[%s] Has 'Never' propagation level, but transaction [%s] is found",
                                        txnConfiguration.familyName, tx.getConfiguration().getFamilyName()));
                            }
                        }

                        throw new TransactionNotAllowedException(
                            format("No transaction is allowed for TxnExecutor '%s' with propagation level 'Never'"+
                                ", but transaction '%s' was found",
                                    txnConfiguration.familyName, tx.getConfiguration().getFamilyName())
                            );
                    }

                    if (TRACING_ENABLED) {
                        if (txnConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                            logger.info(
                                format("[%s] Has 'Never' propagation level and no transaction is found",
                                    txnConfiguration.familyName));
                        }
                    }
                    return closure.execute(null);
                case RequiresNew:
                    if (tx == null) {
                        if(TRACING_ENABLED){
                            if (txnConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(
                                    format("[%s] Has 'RequiresNew' propagation level and no transaction is found, starting new transaction",
                                        txnConfiguration.familyName));
                            }
                        }

                        tx = txnFactory.newTransaction(pool);
                        transactionContainer.tx = tx;
                        return atomic(tx, transactionContainer, pool, closure);
                    } else {
                        if(TRACING_ENABLED){
                            if (txnConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(
                                    format("[%s] Has 'RequiresNew' propagationLevel and existing transaction [%s] was found",
                                        txnConfiguration.familyName, tx.getConfiguration().getFamilyName()));
                            }
                        }

                        GammaTransaction suspendedTransaction = tx;
                        tx = txnFactory.newTransaction(pool);
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
                            if (txnConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(
                                    format("[%s] Has 'RequiresNew' propagationLevel and existing transaction [%s] was found",
                                        txnConfiguration.familyName, tx.getConfiguration().getFamilyName()));
                            }
                        }else{
                            if (txnConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(
                                    format("[%s] Has 'RequiresNew' propagationLevel and existing transaction [%s] was found",
                                        txnConfiguration.familyName, tx.getConfiguration().getFamilyName()));
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
                            if (txnConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(
                                    format("[%s] Has 'Requires' propagation level and no transaction found, starting a new transaction",
                                    txnConfiguration.familyName));
                            }
                        }

                        tx = txnFactory.newTransaction(pool);
                        transactionContainer.tx = tx;
                        return atomic(tx, transactionContainer, pool, closure);
                    } else {
                        if (TRACING_ENABLED) {
                            if (txnConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(
                                    format("[%s] Has 'Requires' propagation level, and existing transaction [%s] found",
                                        txnConfiguration.familyName, tx.getConfiguration().getFamilyName()));
                                }
                            }

                        return closure.execute(tx);
                    }
                case Mandatory:
                    if (tx == null) {
                        if (TRACING_ENABLED) {
                            if (txnConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(
                                    format("[%s] Has 'Mandatory' propagation level, and no transaction is found",
                                        txnConfiguration.familyName));
                                }
                            }
                            throw new TransactionMandatoryException(
                                format("No transaction is found for TxnExecutor '%s' with 'Mandatory' propagation level",
                                    txnConfiguration.familyName));
                        }

                    if (TRACING_ENABLED) {
                        if (txnConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                            logger.info(
                                format("[%s] Has 'Mandatory' propagation level and transaction [%s] found",
                                    txnConfiguration.familyName, tx.getConfiguration().getFamilyName()));
                        }
                    }
                    return closure.execute(tx);
                case Never:
                    if (tx != null) {
                        if (TRACING_ENABLED) {
                            if (txnConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(
                                    format("[%s] Has 'Never' propagation level, but transaction [%s] is found",
                                        txnConfiguration.familyName, tx.getConfiguration().getFamilyName()));
                            }
                        }

                        throw new TransactionNotAllowedException(
                            format("No transaction is allowed for TxnExecutor '%s' with propagation level 'Never'"+
                                ", but transaction '%s' was found",
                                    txnConfiguration.familyName, tx.getConfiguration().getFamilyName())
                            );
                    }

                    if (TRACING_ENABLED) {
                        if (txnConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                            logger.info(
                                format("[%s] Has 'Never' propagation level and no transaction is found",
                                    txnConfiguration.familyName));
                        }
                    }
                    return closure.execute(null);
                case RequiresNew:
                    if (tx == null) {
                        if(TRACING_ENABLED){
                            if (txnConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(
                                    format("[%s] Has 'RequiresNew' propagation level and no transaction is found, starting new transaction",
                                        txnConfiguration.familyName));
                            }
                        }

                        tx = txnFactory.newTransaction(pool);
                        transactionContainer.tx = tx;
                        return atomic(tx, transactionContainer, pool, closure);
                    } else {
                        if(TRACING_ENABLED){
                            if (txnConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(
                                    format("[%s] Has 'RequiresNew' propagationLevel and existing transaction [%s] was found",
                                        txnConfiguration.familyName, tx.getConfiguration().getFamilyName()));
                            }
                        }

                        GammaTransaction suspendedTransaction = tx;
                        tx = txnFactory.newTransaction(pool);
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
                            if (txnConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(
                                    format("[%s] Has 'RequiresNew' propagationLevel and existing transaction [%s] was found",
                                        txnConfiguration.familyName, tx.getConfiguration().getFamilyName()));
                            }
                        }else{
                            if (txnConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(
                                    format("[%s] Has 'RequiresNew' propagationLevel and existing transaction [%s] was found",
                                        txnConfiguration.familyName, tx.getConfiguration().getFamilyName()));
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
                            if (txnConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(
                                    format("[%s] Has 'Requires' propagation level and no transaction found, starting a new transaction",
                                    txnConfiguration.familyName));
                            }
                        }

                        tx = txnFactory.newTransaction(pool);
                        transactionContainer.tx = tx;
                        return atomic(tx, transactionContainer, pool, closure);
                    } else {
                        if (TRACING_ENABLED) {
                            if (txnConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(
                                    format("[%s] Has 'Requires' propagation level, and existing transaction [%s] found",
                                        txnConfiguration.familyName, tx.getConfiguration().getFamilyName()));
                                }
                            }

                        return closure.execute(tx);
                    }
                case Mandatory:
                    if (tx == null) {
                        if (TRACING_ENABLED) {
                            if (txnConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(
                                    format("[%s] Has 'Mandatory' propagation level, and no transaction is found",
                                        txnConfiguration.familyName));
                                }
                            }
                            throw new TransactionMandatoryException(
                                format("No transaction is found for TxnExecutor '%s' with 'Mandatory' propagation level",
                                    txnConfiguration.familyName));
                        }

                    if (TRACING_ENABLED) {
                        if (txnConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                            logger.info(
                                format("[%s] Has 'Mandatory' propagation level and transaction [%s] found",
                                    txnConfiguration.familyName, tx.getConfiguration().getFamilyName()));
                        }
                    }
                    return closure.execute(tx);
                case Never:
                    if (tx != null) {
                        if (TRACING_ENABLED) {
                            if (txnConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(
                                    format("[%s] Has 'Never' propagation level, but transaction [%s] is found",
                                        txnConfiguration.familyName, tx.getConfiguration().getFamilyName()));
                            }
                        }

                        throw new TransactionNotAllowedException(
                            format("No transaction is allowed for TxnExecutor '%s' with propagation level 'Never'"+
                                ", but transaction '%s' was found",
                                    txnConfiguration.familyName, tx.getConfiguration().getFamilyName())
                            );
                    }

                    if (TRACING_ENABLED) {
                        if (txnConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                            logger.info(
                                format("[%s] Has 'Never' propagation level and no transaction is found",
                                    txnConfiguration.familyName));
                        }
                    }
                    return closure.execute(null);
                case RequiresNew:
                    if (tx == null) {
                        if(TRACING_ENABLED){
                            if (txnConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(
                                    format("[%s] Has 'RequiresNew' propagation level and no transaction is found, starting new transaction",
                                        txnConfiguration.familyName));
                            }
                        }

                        tx = txnFactory.newTransaction(pool);
                        transactionContainer.tx = tx;
                        return atomic(tx, transactionContainer, pool, closure);
                    } else {
                        if(TRACING_ENABLED){
                            if (txnConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(
                                    format("[%s] Has 'RequiresNew' propagationLevel and existing transaction [%s] was found",
                                        txnConfiguration.familyName, tx.getConfiguration().getFamilyName()));
                            }
                        }

                        GammaTransaction suspendedTransaction = tx;
                        tx = txnFactory.newTransaction(pool);
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
                            if (txnConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(
                                    format("[%s] Has 'RequiresNew' propagationLevel and existing transaction [%s] was found",
                                        txnConfiguration.familyName, tx.getConfiguration().getFamilyName()));
                            }
                        }else{
                            if (txnConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(
                                    format("[%s] Has 'RequiresNew' propagationLevel and existing transaction [%s] was found",
                                        txnConfiguration.familyName, tx.getConfiguration().getFamilyName()));
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
                            if (txnConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(
                                    format("[%s] Has 'Requires' propagation level and no transaction found, starting a new transaction",
                                    txnConfiguration.familyName));
                            }
                        }

                        tx = txnFactory.newTransaction(pool);
                        transactionContainer.tx = tx;
                        atomic(tx, transactionContainer,pool, closure);
                        return;
                    } else {
                        if (TRACING_ENABLED) {
                            if (txnConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(
                                    format("[%s] Has 'Requires' propagation level, and existing transaction [%s] found",
                                        txnConfiguration.familyName, tx.getConfiguration().getFamilyName()));
                                }
                            }

                        closure.execute(tx);
                        return;
                    }
                case Mandatory:
                    if (tx == null) {
                        if (TRACING_ENABLED) {
                            if (txnConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(
                                    format("[%s] Has 'Mandatory' propagation level, and no transaction is found",
                                        txnConfiguration.familyName));
                                }
                            }
                            throw new TransactionMandatoryException(
                                format("No transaction is found for TxnExecutor '%s' with 'Mandatory' propagation level",
                                    txnConfiguration.familyName));
                        }

                    if (TRACING_ENABLED) {
                        if (txnConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                            logger.info(
                                format("[%s] Has 'Mandatory' propagation level and transaction [%s] found",
                                    txnConfiguration.familyName, tx.getConfiguration().getFamilyName()));
                        }
                    }
                    closure.execute(tx);
                    return;
                case Never:
                    if (tx != null) {
                        if (TRACING_ENABLED) {
                            if (txnConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(
                                    format("[%s] Has 'Never' propagation level, but transaction [%s] is found",
                                        txnConfiguration.familyName, tx.getConfiguration().getFamilyName()));
                            }
                        }

                        throw new TransactionNotAllowedException(
                            format("No transaction is allowed for TxnExecutor '%s' with propagation level 'Never'"+
                                ", but transaction '%s' was found",
                                    txnConfiguration.familyName, tx.getConfiguration().getFamilyName())
                            );
                    }

                    if (TRACING_ENABLED) {
                        if (txnConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                            logger.info(
                                format("[%s] Has 'Never' propagation level and no transaction is found",
                                    txnConfiguration.familyName));
                        }
                    }
                    closure.execute(null);
                    return;
                case RequiresNew:
                    if (tx == null) {
                        if(TRACING_ENABLED){
                            if (txnConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(
                                    format("[%s] Has 'RequiresNew' propagation level and no transaction is found, starting new transaction",
                                        txnConfiguration.familyName));
                            }
                        }

                        tx = txnFactory.newTransaction(pool);
                        transactionContainer.tx = tx;
                        atomic(tx, transactionContainer, pool, closure);
                        return;
                    } else {
                        if(TRACING_ENABLED){
                            if (txnConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(
                                    format("[%s] Has 'RequiresNew' propagationLevel and existing transaction [%s] was found",
                                        txnConfiguration.familyName, tx.getConfiguration().getFamilyName()));
                            }
                        }

                        GammaTransaction suspendedTransaction = tx;
                        tx = txnFactory.newTransaction(pool);
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
                            if (txnConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(
                                    format("[%s] Has 'RequiresNew' propagationLevel and existing transaction [%s] was found",
                                        txnConfiguration.familyName, tx.getConfiguration().getFamilyName()));
                            }
                        }else{
                            if (txnConfiguration.getTraceLevel().isLoggableFrom(TraceLevel.Coarse)) {
                                logger.info(
                                    format("[%s] Has 'RequiresNew' propagationLevel and existing transaction [%s] was found",
                                        txnConfiguration.familyName, tx.getConfiguration().getFamilyName()));
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
