package org.multiverse.api.references;

/**
* A Factory for creating references.
*
* @author Peter Veentjer.
*/
public interface TxnRefFactory {
    
    /**
    * Creates a committed TxnRef.
    *
    * @param value the initial value.
    * @return the created TxnRef.
    */
        <E> TxnRef<E> newTxnRef(E value);
        
    /**
    * Creates a committed TxnInteger.
    *
    * @param value the initial value.
    * @return the created TxnInteger.
    */
         TxnInteger newTxnInteger(int value);
        
    /**
    * Creates a committed TxnBoolean.
    *
    * @param value the initial value.
    * @return the created TxnBoolean.
    */
         TxnBoolean newTxnBoolean(boolean value);
        
    /**
    * Creates a committed TxnDouble.
    *
    * @param value the initial value.
    * @return the created TxnDouble.
    */
         TxnDouble newTxnDouble(double value);
        
    /**
    * Creates a committed TxnLong.
    *
    * @param value the initial value.
    * @return the created TxnLong.
    */
         TxnLong newTxnLong(long value);
        }
