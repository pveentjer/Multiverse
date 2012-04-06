package org.multiverse.api.collections;

import org.multiverse.api.Txn;

import java.util.Deque;

public interface TransactionalDeque<E> extends TransactionalQueue<E>, Deque<E> {

    boolean offerFirst(Txn txn, E e);

    E pollLast(Txn txn);

    E pollFirst(Txn txn);

    E peekFirst(Txn txn);

    void putFirst(E item);

    void putFirst(Txn txn, E item);

    E takeFirst();

    E takeFirst(Txn txn);

    boolean offerLast(Txn txn, E e);

    E peekLast(Txn txn);

    void putLast(E item);

    void putLast(Txn txn, E item);

    E takeLast();

    E takeLast(Txn txn);

    void addFirst(Txn txn, E e);

    void addLast(Txn txn, E e);

    E removeFirst(Txn txn);

    E removeLast(Txn txn);

    E getFirst(Txn txn);

    E getLast(Txn txn);

    boolean removeFirstOccurrence(Txn txn, Object o);

    boolean removeLastOccurrence(Txn txn, Object o);

    void push(Txn txn, E e);

    E pop(Txn txn);

    TransactionalIterator<E> descendingIterator(Txn txn);
}
