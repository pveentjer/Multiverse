package org.multiverse.collections;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.Stm;
import org.multiverse.api.StmUtils;
import org.multiverse.api.Txn;
import org.multiverse.api.callables.TxnVoidCallable;

import static org.junit.Assert.assertEquals;
import static org.multiverse.api.GlobalStmInstance.getGlobalStmInstance;
import static org.multiverse.api.TxnThreadLocal.clearThreadLocalTxn;

public class NaiveTxHashMap_clearTest {

    private Stm stm;
    private NaiveTxnHashMap<String, String> map;

    @Before
    public void setUp() {
        stm = getGlobalStmInstance();
        clearThreadLocalTxn();
        map = new NaiveTxnHashMap<String, String>(stm);
    }

    @Test
    public void whenNotEmpty() {
        StmUtils.atomic(new TxnVoidCallable() {
            @Override
            public void call(Txn tx) throws Exception {
                map.put("1", "a");
                map.put("2", "b");
                map.put("3", "c");
                map.put("4", "d");

                map.clear();
                assertEquals(0, map.size());
                assertEquals("[]", map.toString());
            }
        });
    }

    @Test
    public void whenManyItems() {
        StmUtils.atomic(new TxnVoidCallable() {
            @Override
            public void call(Txn tx) throws Exception {
                for (int k = 0; k < 1000; k++) {
                    map.put("" + k, "" + k);
                }

                map.clear();
                assertEquals(0, map.size());
                assertEquals("[]", map.toString());
            }
        });
    }

    @Test
    public void whenEmpty() {
        StmUtils.atomic(new TxnVoidCallable() {
            @Override
            public void call(Txn tx) throws Exception {
                map.clear();
                assertEquals(0, map.size());
                assertEquals("[]", map.toString());
            }
        });
    }
}
