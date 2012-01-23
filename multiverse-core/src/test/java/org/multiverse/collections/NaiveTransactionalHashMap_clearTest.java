package org.multiverse.collections;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.Stm;
import org.multiverse.api.Transaction;
import org.multiverse.api.closures.AtomicVoidClosure;

import static org.junit.Assert.assertEquals;
import static org.multiverse.api.GlobalStmInstance.getGlobalStmInstance;
import static org.multiverse.api.StmUtils.execute;
import static org.multiverse.api.ThreadLocalTransaction.clearThreadLocalTransaction;

public class NaiveTransactionalHashMap_clearTest {

    private Stm stm;
    private NaiveTransactionalHashMap<String, String> map;

    @Before
    public void setUp() {
        stm = getGlobalStmInstance();
        clearThreadLocalTransaction();
        map = new NaiveTransactionalHashMap<String, String>(stm);
    }

    @Test
    public void whenNotEmpty() {
        execute(new AtomicVoidClosure() {
            @Override
            public void execute(Transaction tx) throws Exception {
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
        execute(new AtomicVoidClosure() {
            @Override
            public void execute(Transaction tx) throws Exception {
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
        execute(new AtomicVoidClosure() {
            @Override
            public void execute(Transaction tx) throws Exception {
                map.clear();
                assertEquals(0, map.size());
                assertEquals("[]", map.toString());
            }
        });
    }
}
