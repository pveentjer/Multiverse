package org.multiverse.collections;

import org.multiverse.api.Stm;
import org.multiverse.api.Txn;
import org.multiverse.api.collections.TxnIterator;
import org.multiverse.api.collections.TxnSet;

import java.util.Map;

import static org.multiverse.api.TxnThreadLocal.getThreadLocalTxn;

public final class NaiveTxnHashSet<E>
        extends AbstractTxnCollection<E>
        implements TxnSet<E> {

    private final NaiveTxnHashMap<E, Object> map;

    public NaiveTxnHashSet(Stm stm) {
        super(stm);
        this.map = new NaiveTxnHashMap<E, Object>(stm);
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
    public TxnIterator<E> iterator(Txn tx) {
        return map.keySet(tx).iterator(tx);
    }

    static class It<E> extends AbstractTxnIterator<E> {

        private final TxnIterator<Map.Entry<E, Object>> iterator;

        It(TxnIterator<Map.Entry<E, Object>> iterator) {
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
        TxnIterator<E> it = iterator(tx);
        if(! it.hasNext(tx)) {
            return "[]";
        }
        StringBuilder result = new StringBuilder();
        result.append('[');
        while(it.hasNext(tx)) {
            E item = it.next(tx);
            result.append(item);
            if(it.hasNext(tx)) {
              result.append(",");
            }
        }
        result.append(']');
        return result.toString();
    }
}
