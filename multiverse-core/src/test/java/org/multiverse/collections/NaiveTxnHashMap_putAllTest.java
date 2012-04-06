package org.multiverse.collections;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.Stm;
import org.multiverse.api.StmUtils;
import org.multiverse.api.Txn;
import org.multiverse.api.callables.TxnVoidCallable;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.multiverse.api.GlobalStmInstance.getGlobalStmInstance;
import static org.multiverse.api.TxnThreadLocal.clearThreadLocalTxn;

public class NaiveTxnHashMap_putAllTest {

    private Stm stm;
    private NaiveTxnHashMap<String, String> map;

    @Before
    public void setUp() {
        stm = getGlobalStmInstance();
        clearThreadLocalTxn();
        map = new NaiveTxnHashMap<String, String>(stm);
    }

    @Test
    public void whenNullMap_thenNullPointerException() {
        StmUtils.atomic(new TxnVoidCallable() {
            @Override
            public void call(Txn tx) throws Exception {
                try {
                    map.putAll(null);
                    fail();
                } catch (NullPointerException expected) {
                }

                assertEquals(0, map.size());
            }
        });
    }

    @Test
    public void whenEmptyMapAdded() {
        StmUtils.atomic(new TxnVoidCallable() {
            @Override
            public void call(Txn tx) throws Exception {
                map.putAll(new HashMap<String, String>());

                assertEquals(0, map.size());
            }
        });
    }

    @Test
    public void whenOneOfTheItemsIsNull() {

    }

    @Test
    public void whenAllDifferentItems() {
        StmUtils.atomic(new TxnVoidCallable() {
            @Override
            public void call(Txn tx) throws Exception {
                map.put("1", "a");
                map.put("2", "b");
                map.put("3", "c");

                Map<String, String> other = new HashMap<String, String>();
                other.put("4", "d");
                other.put("5", "e");
                other.put("6", "f");

                map.putAll(other);

                assertEquals(6, map.size());
                assertEquals("a", map.get("1"));
                assertEquals("b", map.get("2"));
                assertEquals("c", map.get("3"));
                assertEquals("d", map.get("4"));
                assertEquals("e", map.get("5"));
                assertEquals("f", map.get("6"));
            }
        });
    }

    @Test
    public void whenSomeItemsReplaced() {
        StmUtils.atomic(new TxnVoidCallable() {
            @Override
            public void call(Txn tx) throws Exception {
                map.put("1", "a");
                map.put("2", "b");
                map.put("3", "c");

                Map<String, String> other = new HashMap<String, String>();
                other.put("4", "d");
                other.put("2", "B");
                other.put("3", "C");

                map.putAll(other);

                assertEquals(4, map.size());
                assertEquals("a", map.get("1"));
                assertEquals("B", map.get("2"));
                assertEquals("C", map.get("3"));
                assertEquals("d", map.get("4"));
            }
        });
    }
}
