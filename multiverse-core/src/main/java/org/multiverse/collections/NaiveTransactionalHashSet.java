package org.multiverse.collections;

import org.multiverse.api.Stm;
import org.multiverse.api.Txn;
import org.multiverse.api.collections.TransactionalIterator;
import org.multiverse.api.collections.TransactionalSet;
import org.multiverse.api.exceptions.TodoException;

import java.util.Map;

import static org.multiverse.api.TxnThreadLocal.getThreadLocalTxn;

public final class NaiveTransactionalHashSet<E>
        extends AbstractTransactionalCollection<E>
        implements TransactionalSet<E>{

    private final NaiveTransactionalHashMap<E, Object> map;

    public NaiveTransactionalHashSet(Stm stm) {
        super(stm);
        this.map = new NaiveTransactionalHashMap<E, Object>(stm);
    }

    @Override
    public boolean add(Txn tx, E e) {
        return map.put(tx, e, this) == null;
    }

    @Override
    public boolean contains(Object item) {
        return contains(getThreadLocalTxn(), item);
    }

    @Override
    public boolean contains(Txn tx, Object o) {
        return map.get(tx, o) != null;
    }

    @Override
    public boolean remove(Object item) {
        return remove(getThreadLocalTxn(), item);
    }

    @Override
    public boolean remove(Txn tx, Object item) {
        return map.remove(tx, item) != null;
    }

    @Override
    public int size(Txn tx) {
        return map.size(tx);
    }

    @Override
    public void clear(Txn tx) {
        map.clear(tx);
    }

    @Override
    public TransactionalIterator<E> iterator(Txn tx) {
        return map.keySet(tx).iterator(tx);
    }

    static class It<E> extends AbstractTransactionalIterator<E> {

        private final TransactionalIterator<Map.Entry<E, Object>> iterator;

        It(TransactionalIterator<Map.Entry<E, Object>> iterator) {
            this.iterator = iterator;
        }

        @Override
        public boolean hasNext(Txn tx) {
            return iterator.hasNext(tx);
        }

        @Override
        public E next(Txn tx) {
            return iterator.next(tx).getKey();
        }

        @Override
        public void remove(Txn tx) {
            iterator.remove(tx);
        }
    }

    @Override
    public String toString(Txn tx) {
        throw new TodoException();
    }
}
