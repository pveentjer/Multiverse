package org.multiverse.collections;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.Stm;
import org.multiverse.api.Transaction;
import org.multiverse.api.closures.AtomicVoidClosure;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.multiverse.api.GlobalStmInstance.getGlobalStmInstance;
import static org.multiverse.api.StmUtils.execute;
import static org.multiverse.api.ThreadLocalTransaction.clearThreadLocalTransaction;

public class NaiveTransactionalHashMap_putAllTest {

    private Stm stm;
    private NaiveTransactionalHashMap<String, String> map;

    @Before
    public void setUp() {
        stm = getGlobalStmInstance();
        clearThreadLocalTransaction();
        map = new NaiveTransactionalHashMap<String, String>(stm);
    }

    @Test
    public void whenNullMap_thenNullPointerException() {
        execute(new AtomicVoidClosure() {
            @Override
            public void execute(Transaction tx) throws Exception {
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
        execute(new AtomicVoidClosure() {
            @Override
            public void execute(Transaction tx) throws Exception {
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
        execute(new AtomicVoidClosure() {
            @Override
            public void execute(Transaction tx) throws Exception {
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
        execute(new AtomicVoidClosure() {
            @Override
            public void execute(Transaction tx) throws Exception {
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
