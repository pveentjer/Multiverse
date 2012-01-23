package org.multiverse.collections;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.multiverse.api.Stm;
import org.multiverse.api.Transaction;
import org.multiverse.api.closures.AtomicVoidClosure;

import static org.junit.Assert.*;
import static org.multiverse.api.GlobalStmInstance.getGlobalStmInstance;
import static org.multiverse.api.StmUtils.execute;
import static org.multiverse.api.ThreadLocalTransaction.clearThreadLocalTransaction;

public class NaiveTransactionalLinkedList_takeFirstTest {

    private Stm stm;
    private NaiveTransactionalLinkedList<String> list;

    @Before
    public void setUp() {
        stm = getGlobalStmInstance();
        clearThreadLocalTransaction();
        list = new NaiveTransactionalLinkedList<String>(stm);
    }

    @Test
    @Ignore
    public void whenEmpty() {
        execute(new AtomicVoidClosure() {
            @Override
            public void execute(Transaction tx) throws Exception {
                String item = list.takeFirst();

                assertNull(item);
                assertEquals("[]", list.toString());
                assertEquals(0, list.size());
            }
        });
    }

    @Test
    public void whenMultipleItems() {
        execute(new AtomicVoidClosure() {
            @Override
            public void execute(Transaction tx) throws Exception {
                list.offerLast("1");
                list.offerLast("2");
                list.offerLast("3");

                String found = list.takeFirst();

                assertEquals("1", found);
                assertEquals("[2, 3]", list.toString());
                assertEquals(2, list.size());
            }
        });
    }

    @Test
    public void whenSingleItem() {
        execute(new AtomicVoidClosure() {
            @Override
            public void execute(Transaction tx) throws Exception {
                String item = "1";
                list.put(item);

                String found = list.takeFirst();

                assertSame(item, found);
                assertEquals("[]", list.toString());
                assertEquals(0, list.size());
            }
        });
    }
}
