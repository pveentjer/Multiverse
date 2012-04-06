package org.multiverse.collections;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.Stm;
import org.multiverse.api.StmUtils;
import org.multiverse.api.Txn;
import org.multiverse.api.closures.AtomicVoidClosure;

import static org.junit.Assert.*;
import static org.multiverse.api.GlobalStmInstance.getGlobalStmInstance;
import static org.multiverse.api.TxnThreadLocal.clearThreadLocalTxn;

public class NaiveTransactionalLinkedList_peekLastTest {

      private Stm stm;
    private NaiveTransactionalLinkedList<String> list;

    @Before
    public void setUp() {
        stm = getGlobalStmInstance();
        clearThreadLocalTxn();
        list = new NaiveTransactionalLinkedList<String>(stm);
    }

    @Test
    public void whenEmpty() {
        StmUtils.atomic(new AtomicVoidClosure() {
            @Override
            public void execute(Txn tx) throws Exception {
                String result = list.peekLast();

                assertNull(result);
                assertEquals("[]", list.toString());
                assertEquals(0, list.size());
            }
        });
    }

    @Test
    public void whenMultipleItems() {
        StmUtils.atomic(new AtomicVoidClosure() {
            @Override
            public void execute(Txn tx) throws Exception {
                list.offerLast("1");
                list.offerLast("2");
                list.offerLast("3");

                String found = list.peekLast();

                assertEquals("3", found);
                assertEquals("[1, 2, 3]", list.toString());
                assertEquals(3, list.size());
            }
        });
    }

    @Test
    public void whenSingleItem() {
        StmUtils.atomic(new AtomicVoidClosure() {
            @Override
            public void execute(Txn tx) throws Exception {
                String item = "1";
                list.put(item);

                String found = list.peekLast();

                assertSame(item, found);
                assertEquals("[1]", list.toString());
                assertEquals(1, list.size());
            }
        });
    }
}
