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
    public void clear(Txn tnx) {
        if (size.get(tnx) == 0) {
            return;
        }

        TxnRef<NaiveEntry>[] tab = table.get();
        for (int i = 0; i < tab.length; i++) {
            tab[i].set(null);
        }
        size.set(0);
    }

    @Override
    public int size(Txn tnx) {
        return size.get(tnx);
    }

    @Override
    public V get(Txn tnx, Object key) {
        NaiveEntry<K, V> entry = getEntry(tnx, key);
        return entry == null ? null : entry.value.get(tnx);
    }

    private NaiveEntry<K, V> getEntry(Txn tnx, Object key) {
        if (key == null) {
            return null;
        }

        if (size.get(tnx) == 0) {
            return null;
        }

        int hash = key.hashCode();

        for (NaiveEntry<K, V> entry = table.get(tnx)[indexFor(hash, table.get(tnx).length)].get(tnx); entry != null; entry = entry.next.get(tnx)) {
            Object k;
            if (entry.hash == hash && ((k = entry.key) == key || key.equals(k))) {
                return entry;
            }
        }
        return null;
    }

    @Override
    public V put(Txn tnx, K key, V value) {
        if (key == null) {
            throw new NullPointerException();
        }

        int hash = key.hashCode();

        int i = indexFor(hash, table.get(tnx).length);
        for (NaiveEntry<K, V> entry = table.get(tnx)[i].get(tnx); entry != null; entry = entry.next.get()) {
            Object foundKey;
            if (entry.hash == hash && ((foundKey = entry.key) == key || key.equals(foundKey))) {
                V oldValue = entry.value.get(tnx);
                entry.value.set(tnx, value);
                //entry.recordAccess(this);
                return oldValue;
            }
        }

        addEntry(tnx, hash, key, value, i);
        return null;
    }

    void addEntry(Txn tnx, int hash, K key, V value, int bucketIndex) {
        NaiveEntry<K, V> e = table.get(tnx)[bucketIndex].get(tnx);
        table.get(tnx)[bucketIndex].set(new NaiveEntry<K, V>(hash, key, value, e));
        size.increment(tnx);
        if (size.get(tnx) >= threshold.get(tnx)) {
            resize(tnx, 2 * table.get(tnx).length);
        }
    }

    void resize(Txn tnx, int newCapacity) {
        TxnRef<NaiveEntry>[] oldTable = table.get(tnx);
        int oldCapacity = oldTable.length;
        if (oldCapacity == MAXIMUM_CAPACITY) {
            threshold.set(Integer.MAX_VALUE);
            return;
        }

        TxnRef<NaiveEntry>[] newTable = new TxnRef[newCapacity];
        for (int k = 0; k < newTable.length; k++) {
            newTable[k] = defaultRefFactory.newTxnRef(null);
        }

        transfer(tnx, newTable);
        table.set(tnx, newTable);
        threshold.set(tnx, (int) (newCapacity * loadFactor));
    }

    void transfer(Txn tnx, TxnRef<NaiveEntry>[] newTable) {
        TxnRef<NaiveEntry>[] src = table.get(tnx);
        int newCapacity = newTable.length;
        for (int j = 0; j < src.length; j++) {
            NaiveEntry<K, V> e = src[j].get(tnx);
            if (e != null) {
                src[j] = null;
                do {
                    NaiveEntry<K, V> next = e.next.get(tnx);
                    int i = indexFor(e.hash, newCapacity);
                    e.next.set(tnx, newTable[i].get(tnx));
                    newTable[i].set(tnx, e);
                    e = next;
                } while (e != null);
            }
        }
    }

    static int indexFor(int h, int length) {
        return h & (length - 1);
    }

    @Override
    public V remove(Txn tnx, Object key) {
        throw new TodoException();
    }

    @Override
    public String toString(Txn tnx) {
        int s = size.get(tnx);
        if (s == 0) {
            return "[]";
        }

        throw new TodoException();
    }

    @Override
    public TxnSet<Entry<K, V>> entrySet(Txn tnx) {
        throw new TodoException();
    }

    @Override
    public TxnSet<K> keySet(Txn tnx) {
        throw new TodoException();
    }

    @Override
    public boolean containsKey(Txn tnx, Object key) {
        return getEntry(tnx, key) != null;
    }

    @Override
    public boolean containsValue(Txn tnx, Object value) {
        throw new TodoException();
    }

    @Override
    public TxnCollection<V> values(Txn tnx) {
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
