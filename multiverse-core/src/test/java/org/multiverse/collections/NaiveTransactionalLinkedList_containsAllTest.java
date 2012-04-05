package org.multiverse.collections;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.Stm;
import org.multiverse.api.StmUtils;
import org.multiverse.api.Transaction;
import org.multiverse.api.closures.AtomicVoidClosure;

import java.util.Arrays;
import java.util.LinkedList;

import static org.junit.Assert.*;
import static org.multiverse.api.GlobalStmInstance.getGlobalStmInstance;
import static org.multiverse.api.ThreadLocalTransaction.clearThreadLocalTransaction;

public class NaiveTransactionalLinkedList_containsAllTest {
  private Stm stm;

    @Before
    public void setUp() {
        stm = getGlobalStmInstance();
        clearThreadLocalTransaction();
    }

    @Test
    public void whenNullCollection_thenNullPointerException() {
        final NaiveTransactionalLinkedList<String> list = new NaiveTransactionalLinkedList<String>(stm);

        StmUtils.atomic(new AtomicVoidClosure() {
            @Override
            public void execute(Transaction tx) throws Exception {
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
        final NaiveTransactionalLinkedList<String> list = new NaiveTransactionalLinkedList<String>(stm);

        StmUtils.atomic(new AtomicVoidClosure() {
            @Override
            public void execute(Transaction tx) throws Exception {
                boolean result = list.containsAll(new LinkedList());

                assertTrue(result);
                assertEquals("[]", list.toString());
                assertEquals(0, list.size());
            }
        });
    }

    @Test
    public void whenlistEmpty_andCollectionNonEmpty() {
       final NaiveTransactionalLinkedList<String> list = new NaiveTransactionalLinkedList<String>(stm);

        StmUtils.atomic(new AtomicVoidClosure() {
            @Override
            public void execute(Transaction tx) throws Exception {
                boolean result = list.containsAll(Arrays.asList("1", "2"));

                assertFalse(result);
                assertEquals("[]", list.toString());
                assertEquals(0, list.size());
            }
        });
    }

    @Test
    public void whenlistNonEmpty_andCollectionEmpty() {
       final NaiveTransactionalLinkedList<String> list = new NaiveTransactionalLinkedList<String>(stm);

        StmUtils.atomic(new AtomicVoidClosure() {
            @Override
            public void execute(Transaction tx) throws Exception {
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
         final NaiveTransactionalLinkedList<String> list = new NaiveTransactionalLinkedList<String>(stm);

        StmUtils.atomic(new AtomicVoidClosure() {
            @Override
            public void execute(Transaction tx) throws Exception {
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
          final NaiveTransactionalLinkedList<String> list = new NaiveTransactionalLinkedList<String>(stm);

        StmUtils.atomic(new AtomicVoidClosure() {
            @Override
            public void execute(Transaction tx) throws Exception {
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
         final NaiveTransactionalLinkedList<String> list = new NaiveTransactionalLinkedList<String>(stm);

        StmUtils.atomic(new AtomicVoidClosure() {
            @Override
            public void execute(Transaction tx) throws Exception {
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
             final NaiveTransactionalLinkedList<String> list = new NaiveTransactionalLinkedList<String>(stm);

        StmUtils.atomic(new AtomicVoidClosure() {
            @Override
            public void execute(Transaction tx) throws Exception {
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
        final NaiveTransactionalLinkedList<String> list = new NaiveTransactionalLinkedList<String>(stm);

          StmUtils.atomic(new AtomicVoidClosure() {
              @Override
              public void execute(Transaction tx) throws Exception {
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
