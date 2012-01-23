package org.multiverse.collections;

import org.multiverse.api.Stm;
import org.multiverse.api.Transaction;
import org.multiverse.api.collections.TransactionalCollection;
import org.multiverse.api.collections.TransactionalIterator;
import org.multiverse.api.collections.TransactionalSet;
import org.multiverse.api.exceptions.TodoException;

import java.util.Map;

import static org.multiverse.api.ThreadLocalTransaction.getThreadLocalTransaction;

public final class NaiveTransactionalHashSet<E>
        extends AbstractTransactionalCollection<E>
        implements TransactionalSet<E>{

    private final NaiveTransactionalHashMap<E, Object> map;

    public NaiveTransactionalHashSet(Stm stm) {
        super(stm);
        this.map = new NaiveTransactionalHashMap<E, Object>(stm);
    }

    @Override
    public boolean add(Transaction tx, E e) {
        return map.put(tx, e, this) == null;
    }

    @Override
    public boolean contains(Object item) {
        return contains(getThreadLocalTransaction(), item);
    }

    @Override
    public boolean contains(Transaction tx, Object o) {
        return map.get(tx, o) != null;
    }

    @Override
    public boolean remove(Object item) {
        return remove(getThreadLocalTransaction(), item);
    }

    @Override
    public boolean remove(Transaction tx, Object item) {
        return map.remove(tx, item) != null;
    }

    @Override
    public int size(Transaction tx) {
        return map.size(tx);
    }

    @Override
    public void clear(Transaction tx) {
        map.clear(tx);
    }

    @Override
    public TransactionalIterator<E> iterator(Transaction tx) {
        return map.keySet(tx).iterator(tx);
    }

    static class It<E> extends AbstractTransactionalIterator<E> {

        private final TransactionalIterator<Map.Entry<E, Object>> iterator;

        It(TransactionalIterator<Map.Entry<E, Object>> iterator) {
            this.iterator = iterator;
        }

        @Override
        public boolean hasNext(Transaction tx) {
            return iterator.hasNext(tx);
        }

        @Override
        public E next(Transaction tx) {
            return iterator.next(tx).getKey();
        }

        @Override
        public void remove(Transaction tx) {
            iterator.remove(tx);
        }
    }

    @Override
    public String toString(Transaction tx) {
        throw new TodoException();
    }

    @Override
    public TransactionalCollection<E> buildNew() {
        return new NaiveTransactionalHashSet(stm);
    }
}
