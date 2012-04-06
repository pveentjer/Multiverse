package org.multiverse.collections;

import org.multiverse.api.Stm;
import org.multiverse.api.Txn;
import org.multiverse.api.collections.TxnCollection;
import org.multiverse.api.collections.TxnSet;
import org.multiverse.api.exceptions.TodoException;
import org.multiverse.api.references.TxnInteger;
import org.multiverse.api.references.TxnRef;

import java.util.Map;

public final class NaiveTxnHashMap<K, V> extends AbstractTxnMap<K, V> {

    static final int DEFAULT_INITIAL_CAPACITY = 16;
    /**
     * The load factor used when none specified in constructor.
     */
    static final float DEFAULT_LOAD_FACTOR = 0.75f;
    /**
     * The maximum capacity, used if a higher value is implicitly specified
     * by either of the constructors with arguments.
     * MUST be a power of two <= 1<<30.
     */
    static final int MAXIMUM_CAPACITY = 1 << 30;

    private final TxnInteger size;
    private final TxnRef<TxnRef<NaiveEntry>[]> table;
    private final TxnInteger threshold;

    /**
     * The load factor for the hash table.
     *
     * @serial
     */
    final float loadFactor;

    public NaiveTxnHashMap(Stm stm) {
        super(stm);
        this.size = defaultRefFactory.newTxnInteger(0);
        this.threshold = defaultRefFactory.newTxnInteger((int) (DEFAULT_INITIAL_CAPACITY * DEFAULT_LOAD_FACTOR));
        this.loadFactor = DEFAULT_LOAD_FACTOR;

        TxnRef<NaiveEntry>[] entries = new TxnRef[DEFAULT_INITIAL_CAPACITY];
        for (int k = 0; k < entries.length; k++) {
            entries[k] = defaultRefFactory.newTxnRef(null);
        }

        table = defaultRefFactory.newTxnRef(entries);
    }

    public float getLoadFactor() {
        return loadFactor;
    }

    @Override
    public void clear(Txn tx) {
        if (size.get(tx) == 0) {
            return;
        }

        TxnRef<NaiveEntry>[] tab = table.get();
        for (int i = 0; i < tab.length; i++) {
            tab[i].set(null);
        }
        size.set(0);
    }

    @Override
    public int size(Txn tx) {
        return size.get(tx);
    }

    @Override
    public V get(Txn tx, Object key) {
        NaiveEntry<K, V> entry = getEntry(tx, key);
        return entry == null ? null : entry.value.get(tx);
    }

    private NaiveEntry<K, V> getEntry(Txn tx, Object key) {
        if (key == null) {
            return null;
        }

        if (size.get(tx) == 0) {
            return null;
        }

        int hash = key.hashCode();

        for (NaiveEntry<K, V> entry = table.get(tx)[indexFor(hash, table.get(tx).length)].get(tx); entry != null; entry = entry.next.get(tx)) {
            Object k;
            if (entry.hash == hash && ((k = entry.key) == key || key.equals(k))) {
                return entry;
            }
        }
        return null;
    }

    @Override
    public V put(Txn tx, K key, V value) {
        if (key == null) {
            throw new NullPointerException();
        }

        int hash = key.hashCode();

        int i = indexFor(hash, table.get(tx).length);
        for (NaiveEntry<K, V> entry = table.get(tx)[i].get(tx); entry != null; entry = entry.next.get()) {
            Object foundKey;
            if (entry.hash == hash && ((foundKey = entry.key) == key || key.equals(foundKey))) {
                V oldValue = entry.value.get(tx);
                entry.value.set(tx, value);
                //entry.recordAccess(this);
                return oldValue;
            }
        }

        addEntry(tx, hash, key, value, i);
        return null;
    }

    void addEntry(Txn tx, int hash, K key, V value, int bucketIndex) {
        NaiveEntry<K, V> e = table.get(tx)[bucketIndex].get(tx);
        table.get(tx)[bucketIndex].set(new NaiveEntry<K, V>(hash, key, value, e));
        size.increment(tx);
        if (size.get(tx) >= threshold.get(tx)) {
            resize(tx, 2 * table.get(tx).length);
        }
    }

    void resize(Txn tx, int newCapacity) {
        TxnRef<NaiveEntry>[] oldTable = table.get(tx);
        int oldCapacity = oldTable.length;
        if (oldCapacity == MAXIMUM_CAPACITY) {
            threshold.set(Integer.MAX_VALUE);
            return;
        }

        TxnRef<NaiveEntry>[] newTable = new TxnRef[newCapacity];
        for (int k = 0; k < newTable.length; k++) {
            newTable[k] = defaultRefFactory.newTxnRef(null);
        }

        transfer(tx, newTable);
        table.set(tx, newTable);
        threshold.set(tx, (int) (newCapacity * loadFactor));
    }

    void transfer(Txn tx, TxnRef<NaiveEntry>[] newTable) {
        TxnRef<NaiveEntry>[] src = table.get(tx);
        int newCapacity = newTable.length;
        for (int j = 0; j < src.length; j++) {
            NaiveEntry<K, V> e = src[j].get(tx);
            if (e != null) {
                src[j] = null;
                do {
                    NaiveEntry<K, V> next = e.next.get(tx);
                    int i = indexFor(e.hash, newCapacity);
                    e.next.set(tx, newTable[i].get(tx));
                    newTable[i].set(tx, e);
                    e = next;
                } while (e != null);
            }
        }
    }

    static int indexFor(int h, int length) {
        return h & (length - 1);
    }

    @Override
    public V remove(Txn tx, Object key) {
        throw new TodoException();
    }

    @Override
    public String toString(Txn tx) {
        int s = size.get(tx);
        if (s == 0) {
            return "[]";
        }

        throw new TodoException();
    }

    @Override
    public TxnSet<Entry<K, V>> entrySet(Txn tx) {
        throw new TodoException();
    }

    @Override
    public TxnSet<K> keySet(Txn tx) {
        throw new TodoException();
    }

    @Override
    public boolean containsKey(Txn tx, Object key) {
        return getEntry(tx, key) != null;
    }

    @Override
    public boolean containsValue(Txn tx, Object value) {
        throw new TodoException();
    }

    @Override
    public TxnCollection<V> values(Txn tx) {
        throw new TodoException();
    }

    private class NaiveEntry<K, V> implements Map.Entry<K, V> {
        final K key;
        final int hash;
        final TxnRef<V> value;
        final TxnRef<NaiveEntry<K, V>> next;

        NaiveEntry(int hash, K key, V value, NaiveEntry<K, V> next) {
            this.value = defaultRefFactory.newTxnRef(value);
            this.next = defaultRefFactory.newTxnRef(next);
            this.key = key;
            this.hash = hash;
        }

        public final K getKey() {
            return key;
        }

        public final V getValue() {
            return value.get();
        }

        public final V setValue(V newValue) {
            V oldValue = value.get();
            value.set(newValue);
            return oldValue;
        }

        public final boolean equals(Object o) {
            if (!(o instanceof Map.Entry)) {
                return false;
            }

            Map.Entry e = (Map.Entry) o;
            Object k1 = getKey();
            Object k2 = e.getKey();
            if (k1 == k2 || (k1 != null && k1.equals(k2))) {
                Object v1 = getValue();
                Object v2 = e.getValue();
                if (v1 == v2 || (v1 != null && v1.equals(v2))) {
                    return true;
                }
            }
            return false;
        }

        public final int hashCode() {
            V v = value.get();

            return (key == null ? 0 : key.hashCode()) ^ (v == null ? 0 : v.hashCode());
        }

        public final String toString() {
            return getKey() + "=" + getValue();
        }
    }
}
