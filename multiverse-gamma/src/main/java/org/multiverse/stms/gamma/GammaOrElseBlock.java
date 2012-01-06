package org.multiverse.stms.gamma;

import org.multiverse.api.*;
import org.multiverse.api.closures.*;
import org.multiverse.api.exceptions.*;
import static org.multiverse.api.ThreadLocalTransaction.*;

public class GammaOrElseBlock implements OrElseBlock{


    @Override
    public <E> E execute(AtomicClosure<E> either, AtomicClosure<E> orelse){
        try{
            return executeChecked(either,orelse);
        }catch(RuntimeException e){
            throw e;
        }catch(Exception e){
            throw new InvisibleCheckedException(e);
        }
    }

    @Override
    public <E> E executeChecked(AtomicClosure<E> either, AtomicClosure<E> orelse)throws Exception{
        if(either == null){
            throw new NullPointerException("either closure can't be null");
        }

        if(orelse == null){
            throw new NullPointerException("orelse closure can't be null");
        }

        Transaction tx = getThreadLocalTransaction();
        if(tx == null){
            throw new TransactionMandatoryException("No transaction is found, but one is required for the orelse");
        }

        try{
            return either.execute(tx);
        }catch(RetryError retry){
            return orelse.execute(tx);
        }
    }

    @Override
    public  int execute(AtomicIntClosure either, AtomicIntClosure orelse){
        try{
            return executeChecked(either,orelse);
        }catch(RuntimeException e){
            throw e;
        }catch(Exception e){
            throw new InvisibleCheckedException(e);
        }
    }

    @Override
    public  int executeChecked(AtomicIntClosure either, AtomicIntClosure orelse)throws Exception{
        if(either == null){
            throw new NullPointerException("either closure can't be null");
        }

        if(orelse == null){
            throw new NullPointerException("orelse closure can't be null");
        }

        Transaction tx = getThreadLocalTransaction();
        if(tx == null){
            throw new TransactionMandatoryException("No transaction is found, but one is required for the orelse");
        }

        try{
            return either.execute(tx);
        }catch(RetryError retry){
            return orelse.execute(tx);
        }
    }

    @Override
    public  long execute(AtomicLongClosure either, AtomicLongClosure orelse){
        try{
            return executeChecked(either,orelse);
        }catch(RuntimeException e){
            throw e;
        }catch(Exception e){
            throw new InvisibleCheckedException(e);
        }
    }

    @Override
    public  long executeChecked(AtomicLongClosure either, AtomicLongClosure orelse)throws Exception{
        if(either == null){
            throw new NullPointerException("either closure can't be null");
        }

        if(orelse == null){
            throw new NullPointerException("orelse closure can't be null");
        }

        Transaction tx = getThreadLocalTransaction();
        if(tx == null){
            throw new TransactionMandatoryException("No transaction is found, but one is required for the orelse");
        }

        try{
            return either.execute(tx);
        }catch(RetryError retry){
            return orelse.execute(tx);
        }
    }

    @Override
    public  double execute(AtomicDoubleClosure either, AtomicDoubleClosure orelse){
        try{
            return executeChecked(either,orelse);
        }catch(RuntimeException e){
            throw e;
        }catch(Exception e){
            throw new InvisibleCheckedException(e);
        }
    }

    @Override
    public  double executeChecked(AtomicDoubleClosure either, AtomicDoubleClosure orelse)throws Exception{
        if(either == null){
            throw new NullPointerException("either closure can't be null");
        }

        if(orelse == null){
            throw new NullPointerException("orelse closure can't be null");
        }

        Transaction tx = getThreadLocalTransaction();
        if(tx == null){
            throw new TransactionMandatoryException("No transaction is found, but one is required for the orelse");
        }

        try{
            return either.execute(tx);
        }catch(RetryError retry){
            return orelse.execute(tx);
        }
    }

    @Override
    public  boolean execute(AtomicBooleanClosure either, AtomicBooleanClosure orelse){
        try{
            return executeChecked(either,orelse);
        }catch(RuntimeException e){
            throw e;
        }catch(Exception e){
            throw new InvisibleCheckedException(e);
        }
    }

    @Override
    public  boolean executeChecked(AtomicBooleanClosure either, AtomicBooleanClosure orelse)throws Exception{
        if(either == null){
            throw new NullPointerException("either closure can't be null");
        }

        if(orelse == null){
            throw new NullPointerException("orelse closure can't be null");
        }

        Transaction tx = getThreadLocalTransaction();
        if(tx == null){
            throw new TransactionMandatoryException("No transaction is found, but one is required for the orelse");
        }

        try{
            return either.execute(tx);
        }catch(RetryError retry){
            return orelse.execute(tx);
        }
    }

    @Override
    public  void execute(AtomicVoidClosure either, AtomicVoidClosure orelse){
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
    public  void executeChecked(AtomicVoidClosure either, AtomicVoidClosure orelse)throws Exception{
        if(either == null){
            throw new NullPointerException("either closure can't be null");
        }

        if(orelse == null){
            throw new NullPointerException("orelse closure can't be null");
        }

        Transaction tx = getThreadLocalTransaction();
        if(tx == null){
            throw new TransactionMandatoryException("No transaction is found, but one is required for the orelse");
        }

        try{
            either.execute(tx);
            return;
        }catch(RetryError retry){
            orelse.execute(tx);
            return;
        }
    }
}