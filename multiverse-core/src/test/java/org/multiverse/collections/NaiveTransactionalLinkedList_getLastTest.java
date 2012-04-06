package org.multiverse.collections;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.Stm;
import org.multiverse.api.Txn;
import org.multiverse.api.closures.AtomicVoidClosure;

import java.util.NoSuchElementException;

import static org.junit.Assert.*;
import static org.multiverse.api.GlobalStmInstance.getGlobalStmInstance;
import static org.multiverse.api.StmUtils.atomic;
import static org.multiverse.api.TxnThreadLocal.clearThreadLocalTxn;

public class NaiveTransactionalLinkedList_getLastTest {

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
        atomic(new AtomicVoidClosure() {
            @Override
            public void execute(Txn tx) throws Exception {
                try {
                    list.getLast();
                    fail();
                } catch (NoSuchElementException expected) {

                }

                assertEquals("[]", list.toString());
                assertEquals(0, list.size());
            }
        });
    }

    @Test
    public void whenMultipleItems() {
        atomic(new AtomicVoidClosure() {
            @Override
            public void execute(Txn tx) throws Exception {
                list.offerLast("1");
                list.offerLast("2");
                list.offerLast("3");

                String found = list.getLast();

                assertEquals("3", found);
                assertEquals("[1, 2]", list.toString());
                assertEquals(2, list.size());
            }
        });
    }

    @Test
    public void whenSingleItem() {
        atomic(new AtomicVoidClosure() {
            @Override
            public void execute(Txn tx) throws Exception {
                String item = "1";
                list.put(item);

                String found = list.getLast();

                assertSame(item, found);
                assertEquals("[]", list.toString());
                assertEquals(0, list.size());
            }
        });
    }
}
