package org.multiverse.api.collections;

import org.multiverse.api.Stm;

/**
 * A factory responsible for creating Transactional collections.
 *
 * @author Peter Veentjer.
 */
public interface TxnCollectionsFactory {

    Stm getStm();

    <E> TxnStack<E> newStack();

    <E> TxnStack<E> newStack(int capacity);

    <E> TxnQueue<E> newQueue();

    <E> TxnQueue<E> newQueue(int capacity);

    <E> TxnDeque<E> newDeque();

    <E> TxnDeque<E> newDeque(int capacity);

    <E> TxnSet<E> newHashSet();

    <K, V> TxnMap<K, V> newHashMap();

    <E> TxnList<E> newLinkedList();
}
