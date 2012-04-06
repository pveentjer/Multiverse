package org.multiverse.collections;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.Stm;
import org.multiverse.api.StmUtils;
import org.multiverse.api.Txn;
import org.multiverse.api.closures.AtomicVoidClosure;

import static org.junit.Assert.*;
import static org.multiverse.api.GlobalStmInstance.getGlobalStmInstance;
import static org.multiverse.api.TxnThreadLocal.clearThreadLocalTxn;

public class NaiveTransactionalHashMap_containsKeyTest {

    private Stm stm;
    private NaiveTransactionalHashMap<String, String> map;

    @Before
    public void setUp() {
        stm = getGlobalStmInstance();
        clearThreadLocalTxn();
        map = new NaiveTransactionalHashMap<String, String>(stm);
    }

    @Test
    public void whenNotFound() {
        StmUtils.atomic(new AtomicVoidClosure() {
            @Override
            public void execute(Txn tx) throws Exception {
                map.put("1", "a");
                map.put("2", "b");
                map.put("3", "c");
                map.put("4", "d");

                boolean result = map.containsKey("banana");

                assertFalse(result);
                //assertEquals(4)
                //assertEquals("[]",map.toString());
            }
        });
    }

    @Test
    public void whenFound() {
        StmUtils.atomic(new AtomicVoidClosure() {
            @Override
            public void execute(Txn tx) throws Exception {
                map.put("1", "a");
                map.put("2", "b");
                map.put("3", "c");
                map.put("4", "d");

                boolean result = map.containsKey("3");

                assertTrue(result);
                //assertEquals(4)
                //assertEquals("[]",map.toString());
            }
        });
    }

    @Test
    public void whenNullKey_thenReturnFalse() {
        StmUtils.atomic(new AtomicVoidClosure() {
            @Override
            public void execute(Txn tx) throws Exception {
                map.put("1", "a");
                map.put("2", "b");
                map.put("3", "c");
                map.put("4", "d");

                boolean result = map.containsKey(null);

                assertFalse(result);
                //assertEquals(4)
                //assertEquals("[]",map.toString());
            }
        });
    }

    @Test
    public void whenEmpty() {
        StmUtils.atomic(new AtomicVoidClosure() {
            @Override
            public void execute(Txn tx) throws Exception {
                boolean result = map.containsKey("1");

                assertFalse(result);
                assertEquals("[]", map.toString());
            }
        });
    }
}
