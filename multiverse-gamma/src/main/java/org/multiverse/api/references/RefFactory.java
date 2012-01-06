package org.multiverse.api.references;

/**
* A Factory for creating references.
*
* @author Peter Veentjer.
*/
public interface RefFactory {
    
    /**
    * Creates a committed Ref.
    *
    * @param value the initial value.
    * @return the created Ref.
    */
        <E> Ref<E> newRef(E value);
        
    /**
    * Creates a committed IntRef.
    *
    * @param value the initial value.
    * @return the created IntRef.
    */
         IntRef newIntRef(int value);
        
    /**
    * Creates a committed BooleanRef.
    *
    * @param value the initial value.
    * @return the created BooleanRef.
    */
         BooleanRef newBooleanRef(boolean value);
        
    /**
    * Creates a committed DoubleRef.
    *
    * @param value the initial value.
    * @return the created DoubleRef.
    */
         DoubleRef newDoubleRef(double value);
        
    /**
    * Creates a committed LongRef.
    *
    * @param value the initial value.
    * @return the created LongRef.
    */
         LongRef newLongRef(long value);
        }
