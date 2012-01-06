package org.multiverse.api.collections;

import org.multiverse.api.Transaction;

import java.util.Deque;

public interface TransactionalDeque<E> extends TransactionalQueue<E>, Deque<E> {

    boolean offerFirst(Transaction tx, E e);

    E pollLast(Transaction tx);

    E pollFirst(Transaction tx);

    E peekFirst(Transaction tx);

    void putFirst(E item);

    void putFirst(Transaction tx, E item);

    E takeFirst();

    E takeFirst(Transaction tx);

    boolean offerLast(Transaction tx, E e);

    E peekLast(Transaction tx);

    void putLast(E item);

    void putLast(Transaction tx, E item);

    E takeLast();

    E takeLast(Transaction tx);

    void addFirst(Transaction tx, E e);

    void addLast(Transaction tx, E e);

    E removeFirst(Transaction tx);

    E removeLast(Transaction tx);

    E getFirst(Transaction tx);

    E getLast(Transaction tx);

    boolean removeFirstOccurrence(Transaction tx, Object o);

    boolean removeLastOccurrence(Transaction tx, Object o);

    void push(Transaction tx, E e);

    E pop(Transaction tx);

    TransactionalIterator<E> descendingIterator(Transaction tx);
}
