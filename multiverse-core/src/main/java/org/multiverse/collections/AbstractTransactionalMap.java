package org.multiverse.collections;

import org.multiverse.api.Stm;
import org.multiverse.api.Transaction;
import org.multiverse.api.collections.TransactionalCollection;
import org.multiverse.api.collections.TransactionalMap;
import org.multiverse.api.collections.TransactionalSet;
import org.multiverse.api.references.RefFactory;

import java.util.Map;

import static org.multiverse.api.ThreadLocalTransaction.getThreadLocalTransaction;

public abstract class AbstractTransactionalMap<K, V> implements TransactionalMap<K, V> {

    protected final Stm stm;
    protected final RefFactory defaultRefFactory;

    public AbstractTransactionalMap(Stm stm) {
        if (stm == null) {
            throw new NullPointerException();
        }
        this.stm = stm;
        this.defaultRefFactory = stm.getDefaultRefFactory();
    }

    @Override
    public final Stm getStm() {
        return stm;
    }

    @Override
    public int size() {
        return size(getThreadLocalTransaction());
    }

    @Override
    public boolean isEmpty() {
        return isEmpty(getThreadLocalTransaction());
    }

    @Override
    public boolean isEmpty(Transaction tx) {
        return size(tx) == 0;
    }

    @Override
    public void clear() {
        clear(getThreadLocalTransaction());
    }

    @Override
    public V get(Object key) {
        return get(getThreadLocalTransaction(), key);
    }

    @Override
    public V put(K key, V value) {
        return put(getThreadLocalTransaction(), key, value);
    }

    @Override
    public V remove(Object key) {
        return remove(getThreadLocalTransaction(), key);
    }

    @Override
    public TransactionalSet<K> keySet() {
        return keySet(getThreadLocalTransaction());
    }

    @Override
    public boolean containsKey(Object key) {
        return containsKey(getThreadLocalTransaction(), key);
    }

    /*
    public boolean containsKey(Transaction tx, Object key) {
        TransactionalIterator<Entry<K, V>> i = entrySet(tx).iterator();
        if (key == null) {
            while (i.hasNext()) {
                Entry<K, V> e = i.next();
                if (e.getKey() == null)
                    return true;
            }
        } else {
            while (i.hasNext()) {
                Entry<K, V> e = i.next();
                if (key.equals(e.getKey()))
                    return true;
            }
        }
        return false;
    } */

    @Override
    public boolean containsValue(Object value) {
        return containsValue(getThreadLocalTransaction(), value);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        putAll(getThreadLocalTransaction(), m);
    }

    @Override
    public void putAll(Transaction tx, Map<? extends K, ? extends V> m) {
        for (Map.Entry<? extends K, ? extends V> e : m.entrySet()){
            put(tx, e.getKey(), e.getValue());
        }
    }

    @Override
    public TransactionalCollection<V> values() {
        return values(getThreadLocalTransaction());
    }

    @Override
    public TransactionalSet<Entry<K, V>> entrySet() {
        return entrySet(getThreadLocalTransaction());
    }

    @Override
    public String toString() {
        return toString(getThreadLocalTransaction());
    }
}
