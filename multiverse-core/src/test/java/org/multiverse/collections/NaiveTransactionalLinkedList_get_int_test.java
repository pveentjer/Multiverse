package org.multiverse.collections;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.Stm;
import org.multiverse.api.Transaction;
import org.multiverse.api.closures.AtomicVoidClosure;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.multiverse.api.GlobalStmInstance.getGlobalStmInstance;
import static org.multiverse.api.StmUtils.execute;
import static org.multiverse.api.ThreadLocalTransaction.clearThreadLocalTransaction;

public class NaiveTransactionalLinkedList_get_int_test {

    private Stm stm;
    private NaiveTransactionalLinkedList<String> list;

    @Before
    public void setUp() {
        stm = getGlobalStmInstance();
        clearThreadLocalTransaction();
        list = new NaiveTransactionalLinkedList<String>(stm);
    }

    @Test
    public void whenIndexTooSmall() {
        execute(new AtomicVoidClosure() {
            @Override
            public void execute(Transaction tx) throws Exception {
                list.add("1");
                list.add("2");
                try {
                    list.get(-1);
                    fail();
                } catch (IndexOutOfBoundsException expected) {

                }

                assertEquals("[1, 2]", list.toString());
                assertEquals(2, list.size());
            }
        });
    }

    @Test
    public void whenIndexTooBig() {
        execute(new AtomicVoidClosure() {
            @Override
            public void execute(Transaction tx) throws Exception {
                list.add("1");
                list.add("2");
                try {
                    list.get(2);
                    fail();
                } catch (IndexOutOfBoundsException expected) {

                }

                assertEquals("[1, 2]", list.toString());
                assertEquals(2, list.size());
            }
        });
    }

    @Test
    public void whenListContainsSingleItem() {
        execute(new AtomicVoidClosure() {
            @Override
            public void execute(Transaction tx) throws Exception {
                list.add("1");
                String item = list.get(0);

                assertEquals("1", item);
                assertEquals("[1]", list.toString());
                assertEquals(1, list.size());
            }
        });
    }

    @Test
    public void whenMultipleItems() {
        execute(new AtomicVoidClosure() {
            @Override
            public void execute(Transaction tx) throws Exception {
                list.add("1");
                list.add("2");
                list.add("3");
                list.add("4");
                list.add("5");
                list.add("6");

                assertEquals("1", list.get(0));
                assertEquals("2", list.get(1));
                assertEquals("3", list.get(2));
                assertEquals("4", list.get(3));
                assertEquals("5", list.get(4));
                assertEquals("6", list.get(5));


                assertEquals("[1, 2, 3, 4, 5, 6]", list.toString());
                assertEquals(6, list.size());
            }
        });
    }
}
