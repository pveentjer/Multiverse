package org.multiverse.stms.gamma;

import org.multiverse.api.*;
import org.multiverse.api.callables.*;
import org.multiverse.api.exceptions.*;
import static org.multiverse.api.TxnThreadLocal.*;

public class GammaOrElseBlock implements OrElseBlock{


    @Override
    public <E> E execute(TxnCallable<E> either, TxnCallable<E> orelse){
        try{
            return executeChecked(either,orelse);
        }catch(RuntimeException e){
            throw e;
        }catch(Exception e){
            throw new InvisibleCheckedException(e);
        }
    }

    @Override
    public <E> E executeChecked(TxnCallable<E> either, TxnCallable<E> orelse)throws Exception{
        if(either == null){
            throw new NullPointerException("either callable can't be null");
        }

        if(orelse == null){
            throw new NullPointerException("orelse callable can't be null");
        }

        Txn txn = getThreadLocalTxn();
        if(txn == null){
            throw new TxnMandatoryException("No txn is found, but one is required for the orelse");
        }

        try{
            return either.call(txn);
        }catch(RetryError retry){
            return orelse.call(txn);
        }
    }

    @Override
    public  int execute(TxnIntCallable either, TxnIntCallable orelse){
        try{
            return executeChecked(either,orelse);
        }catch(RuntimeException e){
            throw e;
        }catch(Exception e){
            throw new InvisibleCheckedException(e);
        }
    }

    @Override
    public  int executeChecked(TxnIntCallable either, TxnIntCallable orelse)throws Exception{
        if(either == null){
            throw new NullPointerException("either callable can't be null");
        }

        if(orelse == null){
            throw new NullPointerException("orelse callable can't be null");
        }

        Txn txn = getThreadLocalTxn();
        if(txn == null){
            throw new TxnMandatoryException("No txn is found, but one is required for the orelse");
        }

        try{
            return either.call(txn);
        }catch(RetryError retry){
            return orelse.call(txn);
        }
    }

    @Override
    public  long execute(TxnLongCallable either, TxnLongCallable orelse){
        try{
            return executeChecked(either,orelse);
        }catch(RuntimeException e){
            throw e;
        }catch(Exception e){
            throw new InvisibleCheckedException(e);
        }
    }

    @Override
    public  long executeChecked(TxnLongCallable either, TxnLongCallable orelse)throws Exception{
        if(either == null){
            throw new NullPointerException("either callable can't be null");
        }

        if(orelse == null){
            throw new NullPointerException("orelse callable can't be null");
        }

        Txn txn = getThreadLocalTxn();
        if(txn == null){
            throw new TxnMandatoryException("No txn is found, but one is required for the orelse");
        }

        try{
            return either.call(txn);
        }catch(RetryError retry){
            return orelse.call(txn);
        }
    }

    @Override
    public  double execute(TxnDoubleCallable either, TxnDoubleCallable orelse){
        try{
            return executeChecked(either,orelse);
        }catch(RuntimeException e){
            throw e;
        }catch(Exception e){
            throw new InvisibleCheckedException(e);
        }
    }

    @Override
    public  double executeChecked(TxnDoubleCallable either, TxnDoubleCallable orelse)throws Exception{
        if(either == null){
            throw new NullPointerException("either callable can't be null");
        }

        if(orelse == null){
            throw new NullPointerException("orelse callable can't be null");
        }

        Txn txn = getThreadLocalTxn();
        if(txn == null){
            throw new TxnMandatoryException("No txn is found, but one is required for the orelse");
        }

        try{
            return either.call(txn);
        }catch(RetryError retry){
            return orelse.call(txn);
        }
    }

    @Override
    public  boolean execute(TxnBooleanCallable either, TxnBooleanCallable orelse){
        try{
            return executeChecked(either,orelse);
        }catch(RuntimeException e){
            throw e;
        }catch(Exception e){
            throw new InvisibleCheckedException(e);
        }
    }

    @Override
    public  boolean executeChecked(TxnBooleanCallable either, TxnBooleanCallable orelse)throws Exception{
        if(either == null){
            throw new NullPointerException("either callable can't be null");
        }

        if(orelse == null){
            throw new NullPointerException("orelse callable can't be null");
        }

        Txn txn = getThreadLocalTxn();
        if(txn == null){
            throw new TxnMandatoryException("No txn is found, but one is required for the orelse");
        }

        try{
            return either.call(txn);
        }catch(RetryError retry){
            return orelse.call(txn);
        }
    }

    @Override
    public  void execute(TxnVoidCallable either, TxnVoidCallable orelse){
        try{
            executeChecked(either,orelse);
            return;
        }catch(RuntimeException e){
            throw e;
        }catch(Exception e){
            throw new InvisibleCheckedException(e);
        }
    }

    @Override
    public  void executeChecked(TxnVoidCallable either, TxnVoidCallable orelse)throws Exception{
        if(either == null){
            throw new NullPointerException("either callable can't be null");
        }

        if(orelse == null){
            throw new NullPointerException("orelse callable can't be null");
        }

        Txn txn = getThreadLocalTxn();
        if(txn == null){
            throw new TxnMandatoryException("No txn is found, but one is required for the orelse");
        }

        try{
            either.call(txn);
            return;
        }catch(RetryError retry){
            orelse.call(txn);
            return;
        }
    }
}