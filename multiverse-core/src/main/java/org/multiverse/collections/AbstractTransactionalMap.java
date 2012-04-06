package org.multiverse.collections;

import org.multiverse.api.Stm;
import org.multiverse.api.Txn;
import org.multiverse.api.collections.TransactionalCollection;
import org.multiverse.api.collections.TransactionalMap;
import org.multiverse.api.collections.TransactionalSet;
import org.multiverse.api.references.RefFactory;

import java.util.Map;

import static org.multiverse.api.TxnThreadLocal.getThreadLocalTxn;

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
        return size(getThreadLocalTxn());
    }

    @Override
    public boolean isEmpty() {
        return isEmpty(getThreadLocalTxn());
    }

    @Override
    public boolean isEmpty(Txn tx) {
        return size(tx) == 0;
    }

    @Override
    public void clear() {
        clear(getThreadLocalTxn());
    }

    @Override
    public V get(Object key) {
        return get(getThreadLocalTxn(), key);
    }

    @Override
    public V put(K key, V value) {
        return put(getThreadLocalTxn(), key, value);
    }

    @Override
    public V remove(Object key) {
        return remove(getThreadLocalTxn(), key);
    }

    @Override
    public TransactionalSet<K> keySet() {
        return keySet(getThreadLocalTxn());
    }

    @Override
    public boolean containsKey(Object key) {
        return containsKey(getThreadLocalTxn(), key);
    }

    /*
    public boolean containsKey(Txn tx, Object key) {
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
        return containsValue(getThreadLocalTxn(), value);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        putAll(getThreadLocalTxn(), m);
    }

    @Override
    public void putAll(Txn tx, Map<? extends K, ? extends V> m) {
        for (Map.Entry<? extends K, ? extends V> e : m.entrySet()){
            put(tx, e.getKey(), e.getValue());
        }
    }

    @Override
    public TransactionalCollection<V> values() {
        return values(getThreadLocalTxn());
    }

    @Override
    public TransactionalSet<Entry<K, V>> entrySet() {
        return entrySet(getThreadLocalTxn());
    }

    @Override
    public String toString() {
        return toString(getThreadLocalTxn());
    }
}
