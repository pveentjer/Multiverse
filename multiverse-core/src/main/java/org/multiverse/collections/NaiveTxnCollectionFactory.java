package org.multiverse.collections;

import org.multiverse.api.Stm;
import org.multiverse.api.collections.*;

public final class NaiveTxnCollectionFactory implements TxnCollectionsFactory {

    private final Stm stm;

    public NaiveTxnCollectionFactory(Stm stm) {
        if (stm == null) {
            throw new NullPointerException();
        }
        this.stm = stm;
    }

    @Override
    public Stm getStm() {
        return stm;
    }

    @Override
    public <E> NaiveTxnStack<E> newStack() {
        return new NaiveTxnStack<E>(stm);
    }

    @Override
    public <E> TxnStack<E> newStack(int capacity) {
        return new NaiveTxnStack<E>(stm, capacity);
    }

    @Override
    public <E> NaiveTxnLinkedList<E> newQueue() {
        return new NaiveTxnLinkedList<E>(stm);
    }

    @Override
    public <E> NaiveTxnLinkedList<E> newQueue(int capacity) {
        return new NaiveTxnLinkedList<E>(stm, capacity);
    }

    @Override
    public <E> NaiveTxnLinkedList<E> newDeque() {
        return new NaiveTxnLinkedList<E>(stm);
    }

    @Override
    public <E> NaiveTxnLinkedList<E> newDeque(int capacity) {
        return new NaiveTxnLinkedList<E>(stm, capacity);
    }

    @Override
    public <E> NaiveTxnHashSet<E> newHashSet() {
        return new NaiveTxnHashSet<E>(stm);
    }

    @Override
    public <K, V> NaiveTxnHashMap<K, V> newHashMap() {
        return new NaiveTxnHashMap<K, V>(stm);
    }

    @Override
    public <E> NaiveTxnLinkedList<E> newLinkedList() {
        return new NaiveTxnLinkedList<E>(stm);
    }
}
