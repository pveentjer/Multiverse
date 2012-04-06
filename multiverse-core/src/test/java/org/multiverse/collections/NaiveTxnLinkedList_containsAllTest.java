package org.multiverse.collections;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.Stm;
import org.multiverse.api.StmUtils;
import org.multiverse.api.Txn;
import org.multiverse.api.callables.TxnVoidCallable;

import java.util.Arrays;
import java.util.LinkedList;

import static org.junit.Assert.*;
import static org.multiverse.api.GlobalStmInstance.getGlobalStmInstance;
import static org.multiverse.api.TxnThreadLocal.clearThreadLocalTxn;

public class NaiveTxnLinkedList_containsAllTest {
  private Stm stm;

    @Before
    public void setUp() {
        stm = getGlobalStmInstance();
        clearThreadLocalTxn();
    }

    @Test
    public void whenNullCollection_thenNullPointerException() {
        final NaiveTxnLinkedList<String> list = new NaiveTxnLinkedList<String>(stm);

        StmUtils.atomic(new TxnVoidCallable() {
            @Override
            public void call(Txn tx) throws Exception {
                try {
                    list.containsAll(null);
                    fail();
                } catch (NullPointerException expected) {

                }

                assertEquals("[]", list.toString());
                assertEquals(0, list.size());
            }
        });
    }

    @Test
    public void whenBothEmpty() {
        final NaiveTxnLinkedList<String> list = new NaiveTxnLinkedList<String>(stm);

        StmUtils.atomic(new TxnVoidCallable() {
            @Override
            public void call(Txn tx) throws Exception {
                boolean result = list.containsAll(new LinkedList());

                assertTrue(result);
                assertEquals("[]", list.toString());
                assertEquals(0, list.size());
            }
        });
    }

    @Test
    public void whenlistEmpty_andCollectionNonEmpty() {
       final NaiveTxnLinkedList<String> list = new NaiveTxnLinkedList<String>(stm);

        StmUtils.atomic(new TxnVoidCallable() {
            @Override
            public void call(Txn tx) throws Exception {
                boolean result = list.containsAll(Arrays.asList("1", "2"));

                assertFalse(result);
                assertEquals("[]", list.toString());
                assertEquals(0, list.size());
            }
        });
    }

    @Test
    public void whenlistNonEmpty_andCollectionEmpty() {
       final NaiveTxnLinkedList<String> list = new NaiveTxnLinkedList<String>(stm);

        StmUtils.atomic(new TxnVoidCallable() {
            @Override
            public void call(Txn tx) throws Exception {
                list.add("1");
                list.add("2");

                boolean result = list.containsAll(new LinkedList<String>());

                assertTrue(result);
                assertEquals("[1, 2]", list.toString());
                assertEquals(2, list.size());
            }
        });
    }

    @Test
    public void whenExactMatch() {
         final NaiveTxnLinkedList<String> list = new NaiveTxnLinkedList<String>(stm);

        StmUtils.atomic(new TxnVoidCallable() {
            @Override
            public void call(Txn tx) throws Exception {
                list.add("1");
                list.add("2");
                boolean result = list.containsAll(Arrays.asList("1", "2"));

                assertTrue(result);
                assertEquals("[1, 2]", list.toString());
                assertEquals(2, list.size());
            }
        });
    }

    @Test
    public void whenOrderDifferentThanStillMatch() {
          final NaiveTxnLinkedList<String> list = new NaiveTxnLinkedList<String>(stm);

        StmUtils.atomic(new TxnVoidCallable() {
            @Override
            public void call(Txn tx) throws Exception {
                list.add("1");
                list.add("2");
                list.add("1");
                boolean result = list.containsAll(Arrays.asList("1", "2"));

                assertTrue(result);
                assertEquals("[1, 2, 1]", list.toString());
                assertEquals(3, list.size());
            }
        });
    }

    @Test
    public void whenNoneMatch() {
         final NaiveTxnLinkedList<String> list = new NaiveTxnLinkedList<String>(stm);

        StmUtils.atomic(new TxnVoidCallable() {
            @Override
            public void call(Txn tx) throws Exception {
                list.add("1");
                list.add("2");
                list.add("3");
                boolean result = list.containsAll(Arrays.asList("a", "b"));

                assertFalse(result);
                assertEquals("[1, 2, 3]", list.toString());
                assertEquals(3, list.size());
            }
        });
    }

    @Test
    public void whenSomeMatch() {
             final NaiveTxnLinkedList<String> list = new NaiveTxnLinkedList<String>(stm);

        StmUtils.atomic(new TxnVoidCallable() {
            @Override
            public void call(Txn tx) throws Exception {
                list.add("1");
                list.add("2");
                list.add("3");
                boolean result = list.containsAll(Arrays.asList("1", "b"));

                assertFalse(result);
                assertEquals("[1, 2, 3]", list.toString());
                assertEquals(3, list.size());
            }
        });
    }

    @Test
    public void whenSomeElementsNull() {
        final NaiveTxnLinkedList<String> list = new NaiveTxnLinkedList<String>(stm);

          StmUtils.atomic(new TxnVoidCallable() {
              @Override
              public void call(Txn tx) throws Exception {
                  list.add("1");
                  list.add("2");
                  list.add("3");
                  boolean result = list.containsAll(Arrays.asList("1", null));

                  assertFalse(result);
                  assertEquals("[1, 2, 3]", list.toString());
                  assertEquals(3, list.size());
              }
          });
    }
}
