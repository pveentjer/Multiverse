package org.multiverse.collections;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.Stm;
import org.multiverse.api.Transaction;
import org.multiverse.api.closures.AtomicVoidClosure;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.multiverse.api.GlobalStmInstance.getGlobalStmInstance;
import static org.multiverse.api.StmUtils.execute;
import static org.multiverse.api.ThreadLocalTransaction.clearThreadLocalTransaction;

public class NaiveTransactionalHashMap_getTest {

    private Stm stm;
    private NaiveTransactionalHashMap<String, String> map;

    @Before
    public void setUp() {
        stm = getGlobalStmInstance();
        clearThreadLocalTransaction();
        map = new NaiveTransactionalHashMap<String, String>(stm);
    }

    @Test
    public void whenNotFound() {
        execute(new AtomicVoidClosure() {
            @Override
            public void execute(Transaction tx) throws Exception {
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
        execute(new AtomicVoidClosure() {
            @Override
            public void execute(Transaction tx) throws Exception {
                map.put("1", "a");
                map.put("2", "b");
                map.put("3", "c");
                map.put("4", "d");

                String result = map.get("3");

                assertEquals("c",result);
                //assertEquals(4)
                //assertEquals("[]",map.toString());
            }
        });
    }

    @Test
    public void whenNullKey_thenReturnNull(){
           execute(new AtomicVoidClosure() {
            @Override
            public void execute(Transaction tx) throws Exception {
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
        execute(new AtomicVoidClosure() {
            @Override
            public void execute(Transaction tx) throws Exception {
                String result = map.get("1");

                assertNull(result);
                assertEquals("[]", map.toString());
            }
        });
    }
}
