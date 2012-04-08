package org.multiverse.collections;

import org.multiverse.api.Stm;
import org.multiverse.api.Txn;
import org.multiverse.api.collections.TxnCollection;
import org.multiverse.api.collections.TxnIterator;
import org.multiverse.api.references.TxnRefFactory;

import java.util.Collection;

import static org.multiverse.api.TxnThreadLocal.getThreadLocalTxn;

public abstract class AbstractTxnCollection<E> implements TxnCollection<E> {

    protected final Stm stm;
    protected final TxnRefFactory defaultRefFactory;

    protected AbstractTxnCollection(Stm stm) {
        if (stm == null) {
            throw new NullPointerException();
        }
        this.stm = stm;
        this.defaultRefFactory = stm.getDefaultRefFactory();
    }

    @Override
    public Stm getStm() {
        return stm;
    }

    @Override
    public boolean isEmpty() {
        return isEmpty(getThreadLocalTxn());
    }

    @Override
    public boolean isEmpty(final Txn txn) {
        return size(txn) == 0;
    }

    @Override
    public int size() {
        return size(getThreadLocalTxn());
    }

    @Override
    public void clear() {
        clear(getThreadLocalTxn());
    }

    @Override
    public boolean contains(final Object item) {
        return contains(getThreadLocalTxn(), item);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return containsAll(getThreadLocalTxn(), c);
    }

    @Override
    public boolean containsAll(Txn txn, Collection<?> c) {
        if (c == null) {
            throw new NullPointerException();
        }

        if (c.isEmpty()) {
            return true;
        }

        if (isEmpty(txn)) {
            return false;
        }

        for (Object item : c) {
            if (!contains(txn, item)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean remove(Object o) {
        return remove(getThreadLocalTxn(), o);
    }

    @Override
    public boolean add(final E item) {
        return add(getThreadLocalTxn(), item);
    }

    @Override
    public boolean addAll(final Collection<? extends E> c) {
        return addAll(getThreadLocalTxn(), c);
    }

    @Override
    public boolean addAll(final Txn txn, final Collection<? extends E> c) {
        if (c == null) {
            throw new NullPointerException();
        }

        if (c.isEmpty()) {
            return false;
        }

        boolean change = false;
        for (E item : c) {
            if (add(txn, item)) {
                change = true;
            }
        }

        return change;
    }

    @Override
    public boolean addAll(final TxnCollection<? extends E> c) {
        return addAll(getThreadLocalTxn(), c);
    }

    @Override
    public boolean addAll(final Txn txn, final TxnCollection<? extends E> c) {
        if (c == null) {
            throw new NullPointerException();
        }

        if (c.isEmpty(txn)) {
            return false;
        }

        boolean change = false;
        for (TxnIterator<? extends E> it = c.iterator(txn); it.hasNext(txn);) {

            if (add(txn, it.next(txn))) {
                change = true;
            }
        }

        return change;
    }

    @Override
    public TxnIterator<E> iterator() {
        return iterator(getThreadLocalTxn());
    }

    @Override
    public String toString() {
        return toString(getThreadLocalTxn());
    }

    @Override
    public Object[] toArray() {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }
}
