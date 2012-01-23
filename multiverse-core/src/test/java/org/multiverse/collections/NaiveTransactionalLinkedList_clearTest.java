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

public class NaiveTransactionalLinkedList_clearTest {

    private Stm stm;
    private NaiveTransactionalLinkedList<String> list;

    @Before
    public void setUp() {
        stm = getGlobalStmInstance();
        clearThreadLocalTransaction();
        list = new NaiveTransactionalLinkedList<String>(stm);
    }

    @Test
    public void whenEmpty() {
        execute(new AtomicVoidClosure() {
            @Override
            public void execute(Transaction tx) throws Exception {
                list.clear();

                assertEquals(0, list.size());
                assertEquals("[]", list.toString());
            }
        });
    }

    @Test
    public void whenSingleItem() {
        execute(new AtomicVoidClosure() {
            @Override
            public void execute(Transaction tx) throws Exception {
                list.put("foo");
                list.clear();

                assertEquals(0, list.size());
                assertEquals("[]", list.toString());
            }
        });
    }

    @Test
    public void whenMultipleItems() {
        execute(new AtomicVoidClosure() {
            @Override
            public void execute(Transaction tx) throws Exception {
                list.put("foo");
                list.put("bar");
                list.clear();

                assertEquals(0, list.size());
                assertEquals("[]", list.toString());
            }
        });
    }
}
