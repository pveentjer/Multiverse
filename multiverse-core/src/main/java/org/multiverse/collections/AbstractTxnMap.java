package org.multiverse.collections;

import org.multiverse.api.Stm;
import org.multiverse.api.Txn;
import org.multiverse.api.collections.TxnCollection;
import org.multiverse.api.collections.TxnMap;
import org.multiverse.api.collections.TxnSet;
import org.multiverse.api.references.TxnRefFactory;

import java.util.Map;

import static org.multiverse.api.TxnThreadLocal.getThreadLocalTxn;

public abstract class AbstractTxnMap<K, V> implements TxnMap<K, V> {

    protected final Stm stm;
    protected final TxnRefFactory defaultRefFactory;

    public AbstractTxnMap(Stm stm) {
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
    public boolean isEmpty(Txn txn) {
        return size(txn) == 0;
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
    public TxnSet<K> keySet() {
        return keySet(getThreadLocalTxn());
    }

    @Override
    public boolean containsKey(Object key) {
        return containsKey(getThreadLocalTxn(), key);
    }

    /*
    public boolean containsKey(Txn tx, Object key) {
        TxnIterator<Entry<K, V>> i = entrySet(tx).iterator();
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
    public void putAll(Txn txn, Map<? extends K, ? extends V> m) {
        for (Map.Entry<? extends K, ? extends V> e : m.entrySet()){
            put(txn, e.getKey(), e.getValue());
        }
    }

    @Override
    public TxnCollection<V> values() {
        return values(getThreadLocalTxn());
    }

    @Override
    public TxnSet<Entry<K, V>> entrySet() {
        return entrySet(getThreadLocalTxn());
    }

    @Override
    public String toString() {
        return toString(getThreadLocalTxn());
    }
}
