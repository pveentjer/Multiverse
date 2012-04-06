package org.multiverse.collections;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.Stm;
import org.multiverse.api.StmUtils;
import org.multiverse.api.Txn;
import org.multiverse.api.closures.TxnVoidClosure;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.multiverse.api.GlobalStmInstance.getGlobalStmInstance;
import static org.multiverse.api.TxnThreadLocal.clearThreadLocalTxn;

public class NaiveTxnHashMap_getTest {

    private Stm stm;
    private NaiveTxnHashMap<String, String> map;

    @Before
    public void setUp() {
        stm = getGlobalStmInstance();
        clearThreadLocalTxn();
        map = new NaiveTxnHashMap<String, String>(stm);
    }

    @Test
    public void whenNotFound() {
        StmUtils.atomic(new TxnVoidClosure() {
            @Override
            public void execute(Txn tx) throws Exception {
                map.put("1", "a");
                map.put("2", "b");
                map.put("3", "c");
                map.put("4", "d");

                String result = map.get("banana");

                assertNull(result);
                //assertEquals(4)
                //assertEquals("[]",map.toString());
            }
        });
    }

    @Test
    public void whenFound() {
        StmUtils.atomic(new TxnVoidClosure() {
            @Override
            public void execute(Txn tx) throws Exception {
                map.put("1", "a");
                map.put("2", "b");
                map.put("3", "c");
                map.put("4", "d");

                String result = map.get("3");

                assertEquals("c", result);
                //assertEquals(4)
                //assertEquals("[]",map.toString());
            }
        });
    }

    @Test
    public void whenNullKey_thenReturnNull(){
           StmUtils.atomic(new TxnVoidClosure() {
               @Override
               public void execute(Txn tx) throws Exception {
                   map.put("1", "a");
                   map.put("2", "b");
                   map.put("3", "c");
                   map.put("4", "d");

                   String result = map.get(null);

                   assertNull(result);
                   //assertEquals(4)
                   //assertEquals("[]",map.toString());
               }
           });
    }

    @Test
    public void whenEmpty() {
        StmUtils.atomic(new TxnVoidClosure() {
            @Override
            public void execute(Txn tx) throws Exception {
                String result = map.get("1");

                assertNull(result);
                assertEquals("[]", map.toString());
            }
        });
    }
}
