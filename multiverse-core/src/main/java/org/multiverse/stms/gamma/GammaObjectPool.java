package org.multiverse.stms.gamma;

import org.multiverse.stms.gamma.transactionalobjects.BaseGammaRef;
import org.multiverse.stms.gamma.transactionalobjects.CallableNode;
import org.multiverse.stms.gamma.transactionalobjects.GammaRefTranlocal;

import java.util.ArrayList;

/**
 * A pool for tranlocals. The pool is not threadsafe and should be connected to a thread (can
 * be stored in a threadlocal). Eventually the performance of the stm will be limited to the rate
 * of cleanup, and using a pool seriously improves scalability.
 * <p/>
 * Improvement: atm there is only one single type of tranlocal. If there are more types of tranlocals,
 * each class needs to have an index. This index can be used to determine the type of ref. If the pool
 * contains an array of arrays, where the first array is index based on the type of the ref, finding the
 * second array (that contains pooled tranlocals) can be found easily.
 * <p/>
 * ObjectPool is not thread safe and should not be shared between threads.
 * <p/>
 * This class is generated.
 *
 * @author Peter Veentjer
 */
@SuppressWarnings({"ClassWithTooManyFields"})
public final class GammaObjectPool {

    private final static boolean ENABLED = Boolean.parseBoolean(
            System.getProperty("org.multiverse.stm,gamma.GammaObjectPool.enabled", "true"));

    private final static boolean TRANLOCAL_POOLING_ENABLED = Boolean.parseBoolean(
            System.getProperty("org.multiverse.stm.gamma.GammaObjectPool.tranlocalPooling", String.valueOf(ENABLED)));

    private final static boolean TRANLOCALARRAY_POOLING_ENABLED = Boolean.parseBoolean(
            System.getProperty("org.multiverse.stm.gamma.GammaObjectPool.tranlocalArrayPooling", String.valueOf(ENABLED)));

    private final static boolean LISTENER_POOLING_ENABLED = Boolean.parseBoolean(
            System.getProperty("org.multiverse.stm.gamma.GammaObjectPool.listenersPooling", String.valueOf(ENABLED)));

    private final static boolean LISTENERSARRAY_POOLING_ENABLED = Boolean.parseBoolean(
            System.getProperty("org.multiverse.stm.gamma.GammaObjectPool.listenersArrayPooling", String.valueOf(ENABLED)));

    private final static boolean ARRAYLIST_POOLING_ENABLED = Boolean.parseBoolean(
            System.getProperty("org.multiverse.stm.gamma.GammaObjectPool.arrayListPooling", String.valueOf(ENABLED)));

    private final static boolean CALLABLENODE_POOLING_ENABLED = Boolean.parseBoolean(
            System.getProperty("org.multiverse.stm.gamma.GammaObjectPool.callableNodePooling", String.valueOf(ENABLED)));

    private final boolean tranlocalPoolingEnabled;
    private final boolean tranlocalArrayPoolingEnabled;
    private final boolean listenersPoolingEnabled;
    private final boolean listenersArrayPoolingEnabled;
    private final boolean arrayListPoolingEnabled;
    private final boolean callableNodePoolingEnabled;

    private final GammaRefTranlocal[] tranlocalsGammaRef = new GammaRefTranlocal[100];
    private int lastUsedGammaRef = -1;

    private final Listeners[] listenersPool = new Listeners[100];
    private int listenersPoolIndex = -1;

    private final ArrayList[] arrayListPool = new ArrayList[10];
    private int arrayListPoolIndex = -1;

    private final CallableNode[] callableNodePool = new CallableNode[100];
    private int callableNodePoolIndex = -1;

    public GammaObjectPool() {
        arrayListPoolingEnabled = ARRAYLIST_POOLING_ENABLED;
        tranlocalArrayPoolingEnabled = TRANLOCALARRAY_POOLING_ENABLED;
        tranlocalPoolingEnabled = TRANLOCAL_POOLING_ENABLED;
        listenersPoolingEnabled = LISTENER_POOLING_ENABLED;
        listenersArrayPoolingEnabled = LISTENERSARRAY_POOLING_ENABLED;
        callableNodePoolingEnabled = CALLABLENODE_POOLING_ENABLED;
    }

    /**
     * Takes a GammaRefTranlocal from the pool for the specified GammaRef.
     *
     * @param owner the GammaRef to get the GammaRefTranlocal for.
     * @return the pooled tranlocal, or null if none is found.
     * @throws NullPointerException if owner is null.
     */
    public GammaRefTranlocal take(final BaseGammaRef owner) {
        if (owner == null) {
            throw new NullPointerException();
        }

        if (lastUsedGammaRef == -1) {
            GammaRefTranlocal tranlocal = new GammaRefTranlocal();
            tranlocal.owner = owner;
            return tranlocal;
        }

        GammaRefTranlocal tranlocal = tranlocalsGammaRef[lastUsedGammaRef];
        tranlocal.owner = owner;
        tranlocalsGammaRef[lastUsedGammaRef] = null;
        lastUsedGammaRef--;
        return tranlocal;
    }

    /**
     * Puts an old GammaRefTranlocal in this pool. If the tranlocal is allowed to be null,
     * the call is ignored. The same goes for when the tranlocal is permanent, since you
     * can't now how many transactions are still using it.
     *
     * @param tranlocal the GammaRefTranlocal to pool.
     */
    public void put(final GammaRefTranlocal tranlocal) {
        if (!tranlocalPoolingEnabled) {
            return;
        }

        if (lastUsedGammaRef == tranlocalsGammaRef.length - 1) {
            return;
        }

        lastUsedGammaRef++;
        tranlocalsGammaRef[lastUsedGammaRef] = tranlocal;
    }

    private GammaRefTranlocal[][] tranlocalArrayPool = new GammaRefTranlocal[8193][];

    /**
     * Puts a GammaTranlocal array in the pool.
     *
     * @param array the GammaTranlocal array to put in the pool.
     * @throws NullPointerException is array is null.
     */
    public void putTranlocalArray(final GammaRefTranlocal[] array) {
        if (array == null) {
            throw new NullPointerException();
        }

        if (!tranlocalArrayPoolingEnabled) {
            return;
        }

        if (array.length - 1 > tranlocalArrayPool.length) {
            return;
        }

        int index = array.length;

        if (tranlocalArrayPool[index] != null) {
            return;
        }

        //lets clean the array
        for (int k = 0; k < array.length; k++) {
            array[k] = null;
        }

        tranlocalArrayPool[index] = array;
    }

    /**
     * Takes a tranlocal array from the pool with the given size.
     *
     * @param size the size of the array to take
     * @return the GammaTranlocal array taken from the pool, or null if none available.
     * @throws IllegalArgumentException if size smaller than 0.
     */
    public GammaRefTranlocal[] takeTranlocalArray(final int size) {
        if (size < 0) {
            throw new IllegalArgumentException();
        }

        if (!tranlocalArrayPoolingEnabled) {
            return new GammaRefTranlocal[size];
        }

        if (size >= tranlocalArrayPool.length) {
            return new GammaRefTranlocal[size];
        }

        if (tranlocalArrayPool[size] == null) {
            return new GammaRefTranlocal[size];
        }

        GammaRefTranlocal[] array = tranlocalArrayPool[size];
        tranlocalArrayPool[size] = null;
        return array;
    }

    /**
     * Takes a CallableNode from the pool, or null if none is available.
     *
     * @return the CallableNode from the pool, or null if none available.
     */
    public CallableNode takeCallableNode() {
        if (!callableNodePoolingEnabled || callableNodePoolIndex == -1) {
            return new CallableNode();
        }

        CallableNode node = callableNodePool[callableNodePoolIndex];
        callableNodePool[callableNodePoolIndex] = null;
        callableNodePoolIndex--;
        return node;
    }

    /**
     * Puts a CallableNode in the pool.
     *
     * @param node the CallableNode to pool.
     * @throws NullPointerException if node is null.
     */
    public void putCallableNode(CallableNode node) {
        if (node == null) {
            throw new NullPointerException();
        }

        if (!callableNodePoolingEnabled || callableNodePoolIndex == callableNodePool.length - 1) {
            return;
        }

        node.prepareForPooling();
        callableNodePoolIndex++;
        callableNodePool[callableNodePoolIndex] = node;
    }

    // ====================== array list ===================================

    /**
     * Takes an ArrayList from the pool, The returned ArrayList is cleared.
     *
     * @return the ArrayList from the pool, or null of none is found.
     */
    public ArrayList takeArrayList() {
        if (!arrayListPoolingEnabled || arrayListPoolIndex == -1) {
            return new ArrayList(10);
        }

        ArrayList list = arrayListPool[arrayListPoolIndex];
        arrayListPool[arrayListPoolIndex] = null;
        arrayListPoolIndex--;
        return list;
    }

    /**
     * Puts an ArrayList in this pool. The ArrayList will be cleared before being placed
     * in the pool.
     *
     * @param list the ArrayList to place in the pool.
     * @throws NullPointerException if list is null.
     */
    public void putArrayList(ArrayList list) {
        if (list == null) {
            throw new NullPointerException();
        }

        if (!arrayListPoolingEnabled || arrayListPoolIndex == arrayListPool.length - 1) {
            return;
        }

        list.clear();
        arrayListPoolIndex++;
        arrayListPool[arrayListPoolIndex] = list;
    }


    // ============================ listeners ==================================

    /**
     * Takes a Listeners object from the pool.
     *
     * @return the Listeners object taken from the pool. or null if none is taken.
     */
    public Listeners takeListeners() {
        if (!listenersPoolingEnabled || listenersPoolIndex == -1) {
            return new Listeners();
        }

        Listeners listeners = listenersPool[listenersPoolIndex];
        listenersPool[listenersPoolIndex] = null;
        listenersPoolIndex--;
        return listeners;
    }

    /**
     * Puts a Listeners object in the pool. The Listeners object is preparedForPooling before
     * it is put in the pool. The next Listeners object is ignored (the next field itself is ignored).
     *
     * @param listeners the Listeners object to pool.
     * @throws NullPointerException is listeners is null.
     */
    public void putListeners(Listeners listeners) {
        if (listeners == null) {
            throw new NullPointerException();
        }

        if (!listenersPoolingEnabled || listenersPoolIndex == listenersPool.length - 1) {
            return;
        }

        listeners.prepareForPooling();
        listenersPoolIndex++;
        listenersPool[listenersPoolIndex] = listeners;
    }

    // ============================= listeners array =============================

    private Listeners[] listenersArray = new Listeners[100000];

    /**
     * Takes a Listeners array from the pool. If an array is returned, it is completely nulled.
     *
     * @param minimalSize the minimalSize of the Listeners array.
     * @return the found Listeners array, or null if none is taken from the pool.
     * @throws IllegalArgumentException if minimalSize is smaller than 0.
     */
    public Listeners[] takeListenersArray(int minimalSize) {
        if (minimalSize < 0) {
            throw new IllegalArgumentException();
        }

        if (!listenersArrayPoolingEnabled) {
            return new Listeners[minimalSize];
        }

        if (listenersArray == null || listenersArray.length < minimalSize) {
            return new Listeners[minimalSize];
        }

        Listeners[] result = listenersArray;
        listenersArray = null;
        return result;
    }

    /**
     * Puts a Listeners array in the pool.
     * <p/>
     * Listeners array should be nulled before being put in the pool. It is not going to be done by this
     * GammaObjectPool but should be done when the listeners on the listeners array are notified.
     *
     * @param listenersArray the array to pool.
     * @throws NullPointerException if listenersArray is null.
     */
    public void putListenersArray(Listeners[] listenersArray) {
        if (listenersArray == null) {
            throw new NullPointerException();
        }

        if (!listenersArrayPoolingEnabled) {
            return;
        }

        if (this.listenersArray != listenersArray) {
            return;
        }

        this.listenersArray = listenersArray;
    }
}
