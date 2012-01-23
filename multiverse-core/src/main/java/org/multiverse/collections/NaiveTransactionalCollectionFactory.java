package org.multiverse.collections;

import org.multiverse.api.Stm;
import org.multiverse.api.collections.*;

public final class NaiveTransactionalCollectionFactory implements TransactionalCollectionsFactory {

    private final Stm stm;

    public NaiveTransactionalCollectionFactory(Stm stm) {
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
    public <E> NaiveTransactionalStack<E> newStack() {
        return new NaiveTransactionalStack<E>(stm);
    }

    @Override
    public <E> TransactionalStack<E> newStack(int capacity) {
        return new NaiveTransactionalStack<E>(stm, capacity);
    }

    @Override
    public <E> NaiveTransactionalLinkedList<E> newQueue() {
        return new NaiveTransactionalLinkedList<E>(stm);
    }

    @Override
    public <E> NaiveTransactionalLinkedList<E> newQueue(int capacity) {
        return new NaiveTransactionalLinkedList<E>(stm, capacity);
    }

    @Override
    public <E> NaiveTransactionalLinkedList<E> newDeque() {
        return new NaiveTransactionalLinkedList<E>(stm);
    }

    @Override
    public <E> NaiveTransactionalLinkedList<E> newDeque(int capacity) {
        return new NaiveTransactionalLinkedList<E>(stm, capacity);
    }

    @Override
    public <E> NaiveTransactionalHashSet<E> newHashSet() {
        return new NaiveTransactionalHashSet<E>(stm);
    }

    @Override
    public <K, V> NaiveTransactionalHashMap<K, V> newHashMap() {
        return new NaiveTransactionalHashMap<K, V>(stm);
    }

    @Override
    public <E> NaiveTransactionalLinkedList<E> newLinkedList() {
        return new NaiveTransactionalLinkedList<E>(stm);
    }
}
