package org.multiverse.collections;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.Stm;
import org.multiverse.api.StmUtils;
import org.multiverse.api.Transaction;
import org.multiverse.api.closures.AtomicVoidClosure;

import static org.junit.Assert.*;
import static org.multiverse.api.GlobalStmInstance.getGlobalStmInstance;
import static org.multiverse.api.ThreadLocalTransaction.clearThreadLocalTransaction;

public class NaiveTransactionalHashMap_putTest {

    private Stm stm;
    private NaiveTransactionalHashMap<String, String> map;

    @Before
    public void setUp() {
        stm = getGlobalStmInstance();
        clearThreadLocalTransaction();
        map = new NaiveTransactionalHashMap<String, String>(stm);
    }

    @Test
    public void whenEmpty() {
        StmUtils.atomic(new AtomicVoidClosure() {
            @Override
            public void execute(Transaction tx) throws Exception {
                String result = map.put("key", "value");

                assertNull(result);
                assertEquals(1, map.size());
                //todo: tostring
            }
        });
    }

    @Test
    public void whenReplacingExistingKey() {
        StmUtils.atomic(new AtomicVoidClosure() {
            @Override
            public void execute(Transaction tx) throws Exception {
                map.put("1", "a");
                map.put("2", "b");
                map.put("3", "c");

                String result = map.put("2", "B");

                assertEquals("b", result);
                assertEquals("B", map.get("2"));
                assertEquals(3, map.size());
                //todo: tostring
            }
        });
    }

    @Test
    public void whenNullKey_thenNullPointerException() {
        StmUtils.atomic(new AtomicVoidClosure() {
            @Override
            public void execute(Transaction tx) throws Exception {
                try {
                    map.put(null, "foo");
                    fail();
                } catch (NullPointerException expected) {
                }

                assertEquals(0, map.size());
                assertEquals("[]", map.toString());
                //todo: tostring
            }
        });
    }

    @Test
    public void whenManyItems() {
        final int itemCount = 100 * 1000;

        for (int k = 0; k < itemCount; k++) {
            final int key = k;
            StmUtils.atomic(new AtomicVoidClosure() {
                @Override
                public void execute(Transaction tx) throws Exception {
                    map.put("" + key, "" + key);
                }
            });
        }

        System.out.println("Finished inserting");

        StmUtils.atomic(new AtomicVoidClosure() {
            @Override
            public void execute(Transaction tx) throws Exception {
                assertEquals(itemCount, map.size());
            }
        });

        System.out.println("Doing content check");

        for (int k = 0; k < itemCount; k++) {
            final int key = k;
            StmUtils.atomic(new AtomicVoidClosure() {
                @Override
                public void execute(Transaction tx) throws Exception {
                    assertEquals("" + key, map.get("" + key));
                }
            });
        }
    }
}
