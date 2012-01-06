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

public class NaiveTransactionalLinkedList_lastIndexOfTest {

    private Stm stm;
    private NaiveTransactionalLinkedList<String> list;

    @Before
    public void setUp() {
        stm = getGlobalStmInstance();
        clearThreadLocalTransaction();
        list = new NaiveTransactionalLinkedList<String>(stm);
    }

    @Test
    public void whenNullItem_thenMinusOne() {
        execute(new AtomicVoidClosure() {
            @Override
            public void execute(Transaction tx) throws Exception {
                int result = list.lastIndexOf(null);
                assertEquals(result, -1);
                assertEquals("[]", list.toString());
            }
        });
    }

    @Test
    public void whenEmptyList() {
        execute(new AtomicVoidClosure() {
            @Override
            public void execute(Transaction tx) throws Exception {
                int result = list.lastIndexOf("a");
                assertEquals(result, -1);
                assertEquals("[]", list.toString());
            }
        });
    }

    @Test
    public void whenNotFound_thenMinusOne() {
        execute(new AtomicVoidClosure() {
            @Override
            public void execute(Transaction tx) throws Exception {
                list.add("1");
                list.add("2");
                list.add("3");
                list.add("4");

                int result = list.lastIndexOf("a");
                assertEquals(result, -1);
                assertEquals("[1, 2, 3, 4]", list.toString());
            }
        });
    }

    @Test
    public void whenOnlyOnceInCollection() {
        execute(new AtomicVoidClosure() {
            @Override
            public void execute(Transaction tx) throws Exception {
                list.add("1");
                list.add("2");
                list.add("3");
                list.add("4");

                int result = list.lastIndexOf("2");
                assertEquals(1, result);
                assertEquals("[1, 2, 3, 4]", list.toString());
            }
        });
    }

    @Test
    public void whenMultipleTimesInCollection() {
          execute(new AtomicVoidClosure() {
            @Override
            public void execute(Transaction tx) throws Exception {
                list.add("1");
                list.add("2");
                list.add("3");
                list.add("4");
                list.add("2");
                list.add("5");


                int result = list.lastIndexOf("2");
                assertEquals(4, result);
                assertEquals("[1, 2, 3, 4, 2, 5]", list.toString());
            }
        });
    }
}
