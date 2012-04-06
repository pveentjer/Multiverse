package org.multiverse.stms.gamma;

import org.multiverse.api.*;
import org.multiverse.api.closures.*;
import org.multiverse.api.exceptions.*;
import static org.multiverse.api.TxnThreadLocal.*;

public class GammaOrElseBlock implements OrElseBlock{


    @Override
    public <E> E execute(TxnClosure<E> either, TxnClosure<E> orelse){
        try{
            return executeChecked(either,orelse);
        }catch(RuntimeException e){
            throw e;
        }catch(Exception e){
            throw new InvisibleCheckedException(e);
        }
    }

    @Override
    public <E> E executeChecked(TxnClosure<E> either, TxnClosure<E> orelse)throws Exception{
        if(either == null){
            throw new NullPointerException("either closure can't be null");
        }

        if(orelse == null){
            throw new NullPointerException("orelse closure can't be null");
        }

        Txn txn = getThreadLocalTxn();
        if(txn == null){
            throw new TxnMandatoryException("No txn is found, but one is required for the orelse");
        }

        try{
            return either.execute(txn);
        }catch(RetryError retry){
            return orelse.execute(txn);
        }
    }

    @Override
    public  int execute(TxnIntClosure either, TxnIntClosure orelse){
        try{
            return executeChecked(either,orelse);
        }catch(RuntimeException e){
            throw e;
        }catch(Exception e){
            throw new InvisibleCheckedException(e);
        }
    }

    @Override
    public  int executeChecked(TxnIntClosure either, TxnIntClosure orelse)throws Exception{
        if(either == null){
            throw new NullPointerException("either closure can't be null");
        }

        if(orelse == null){
            throw new NullPointerException("orelse closure can't be null");
        }

        Txn txn = getThreadLocalTxn();
        if(txn == null){
            throw new TxnMandatoryException("No txn is found, but one is required for the orelse");
        }

        try{
            return either.execute(txn);
        }catch(RetryError retry){
            return orelse.execute(txn);
        }
    }

    @Override
    public  long execute(TxnLongClosure either, TxnLongClosure orelse){
        try{
            return executeChecked(either,orelse);
        }catch(RuntimeException e){
            throw e;
        }catch(Exception e){
            throw new InvisibleCheckedException(e);
        }
    }

    @Override
    public  long executeChecked(TxnLongClosure either, TxnLongClosure orelse)throws Exception{
        if(either == null){
            throw new NullPointerException("either closure can't be null");
        }

        if(orelse == null){
            throw new NullPointerException("orelse closure can't be null");
        }

        Txn txn = getThreadLocalTxn();
        if(txn == null){
            throw new TxnMandatoryException("No txn is found, but one is required for the orelse");
        }

        try{
            return either.execute(txn);
        }catch(RetryError retry){
            return orelse.execute(txn);
        }
    }

    @Override
    public  double execute(TxnDoubleClosure either, TxnDoubleClosure orelse){
        try{
            return executeChecked(either,orelse);
        }catch(RuntimeException e){
            throw e;
        }catch(Exception e){
            throw new InvisibleCheckedException(e);
        }
    }

    @Override
    public  double executeChecked(TxnDoubleClosure either, TxnDoubleClosure orelse)throws Exception{
        if(either == null){
            throw new NullPointerException("either closure can't be null");
        }

        if(orelse == null){
            throw new NullPointerException("orelse closure can't be null");
        }

        Txn txn = getThreadLocalTxn();
        if(txn == null){
            throw new TxnMandatoryException("No txn is found, but one is required for the orelse");
        }

        try{
            return either.execute(txn);
        }catch(RetryError retry){
            return orelse.execute(txn);
        }
    }

    @Override
    public  boolean execute(TxnBooleanClosure either, TxnBooleanClosure orelse){
        try{
            return executeChecked(either,orelse);
        }catch(RuntimeException e){
            throw e;
        }catch(Exception e){
            throw new InvisibleCheckedException(e);
        }
    }

    @Override
    public  boolean executeChecked(TxnBooleanClosure either, TxnBooleanClosure orelse)throws Exception{
        if(either == null){
            throw new NullPointerException("either closure can't be null");
        }

        if(orelse == null){
            throw new NullPointerException("orelse closure can't be null");
        }

        Txn txn = getThreadLocalTxn();
        if(txn == null){
            throw new TxnMandatoryException("No txn is found, but one is required for the orelse");
        }

        try{
            return either.execute(txn);
        }catch(RetryError retry){
            return orelse.execute(txn);
        }
    }

    @Override
    public  void execute(TxnVoidClosure either, TxnVoidClosure orelse){
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
    public  void executeChecked(TxnVoidClosure either, TxnVoidClosure orelse)throws Exception{
        if(either == null){
            throw new NullPointerException("either closure can't be null");
        }

        if(orelse == null){
            throw new NullPointerException("orelse closure can't be null");
        }

        Txn txn = getThreadLocalTxn();
        if(txn == null){
            throw new TxnMandatoryException("No txn is found, but one is required for the orelse");
        }

        try{
            either.execute(txn);
            return;
        }catch(RetryError retry){
            orelse.execute(txn);
            return;
        }
    }
}