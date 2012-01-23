package org.multiverse.collections;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.multiverse.api.Stm;
import org.multiverse.api.Transaction;
import org.multiverse.api.closures.AtomicVoidClosure;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.multiverse.api.GlobalStmInstance.getGlobalStmInstance;
import static org.multiverse.api.StmUtils.execute;
import static org.multiverse.api.ThreadLocalTransaction.clearThreadLocalTransaction;

public class NaiveTransactionalLinkedList_offerTest {

     private Stm stm;

    @Before
    public void setUp() {
        stm = getGlobalStmInstance();
        clearThreadLocalTransaction();
    }

    @Test
    public void whenNullItem_thenNullPointerException() {
        final NaiveTransactionalLinkedList<String> list = new NaiveTransactionalLinkedList<String>(stm);

        execute(new AtomicVoidClosure() {
            @Override
            public void execute(Transaction tx) throws Exception {
                try {
                    list.offer(null);
                    fail();
                } catch (NullPointerException expected) {
                }

                assertEquals("[]", list.toString());
                assertEquals(0, list.size());
            }
        });
    }

    @Test
    public void whenEmpty() {
        final NaiveTransactionalLinkedList<String> list = new NaiveTransactionalLinkedList<String>(stm);

        execute(new AtomicVoidClosure() {
            @Override
            public void execute(Transaction tx) throws Exception {
                boolean result = list.offer("1");

                assertTrue(result);
                assertEquals("[1]", list.toString());
                assertEquals(1, list.size());
            }
        });
    }

    @Test
    public void whenNotEmpty() {
        final NaiveTransactionalLinkedList<String> list = new NaiveTransactionalLinkedList<String>(stm);

        execute(new AtomicVoidClosure() {
            @Override
            public void execute(Transaction tx) throws Exception {
                list.offer("1");
                boolean result = list.offer("2");

                assertTrue(result);
                assertEquals("[1, 2]", list.toString());
                assertEquals(2, list.size());
            }
        });
    }

    @Test
    @Ignore
    public void whenFull() {

    }
}
