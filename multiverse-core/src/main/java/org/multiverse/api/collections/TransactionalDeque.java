package org.multiverse.api.collections;

import org.multiverse.api.Transaction;

import java.util.Deque;

public interface TransactionalDeque<E> extends TransactionalQueue<E>, Deque<E> {

    boolean offerFirst(Transaction txn, E e);

    E pollLast(Transaction txn);

    E pollFirst(Transaction txn);

    E peekFirst(Transaction txn);

    void putFirst(E item);

    void putFirst(Transaction txn, E item);

    E takeFirst();

    E takeFirst(Transaction txn);

    boolean offerLast(Transaction txn, E e);

    E peekLast(Transaction txn);

    void putLast(E item);

    void putLast(Transaction txn, E item);

    E takeLast();

    E takeLast(Transaction txn);

    void addFirst(Transaction txn, E e);

    void addLast(Transaction txn, E e);

    E removeFirst(Transaction txn);

    E removeLast(Transaction txn);

    E getFirst(Transaction txn);

    E getLast(Transaction txn);

    boolean removeFirstOccurrence(Transaction txn, Object o);

    boolean removeLastOccurrence(Transaction txn, Object o);

    void push(Transaction txn, E e);

    E pop(Transaction txn);

    TransactionalIterator<E> descendingIterator(Transaction txn);
}
