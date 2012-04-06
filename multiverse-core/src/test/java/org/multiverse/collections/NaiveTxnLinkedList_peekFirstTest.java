package org.multiverse.collections;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.Stm;
import org.multiverse.api.Txn;
import org.multiverse.api.closures.TxnVoidClosure;

import static org.junit.Assert.*;
import static org.multiverse.api.GlobalStmInstance.getGlobalStmInstance;
import static org.multiverse.api.StmUtils.atomic;
import static org.multiverse.api.TxnThreadLocal.clearThreadLocalTxn;

public class NaiveTxnLinkedList_peekFirstTest {

     private Stm stm;
    private NaiveTxnLinkedList<String> list;

    @Before
    public void setUp() {
        stm = getGlobalStmInstance();
        clearThreadLocalTxn();
        list = new NaiveTxnLinkedList<String>(stm);
    }

    @Test
    public void whenEmpty() {
       atomic(new TxnVoidClosure() {
           @Override
           public void execute(Txn tx) throws Exception {
               String result = list.peekFirst();

               assertNull(result);
               assertEquals("[]", list.toString());
               assertEquals(0, list.size());
           }
       });
    }

    @Test
    public void whenMultipleItems() {
        atomic(new TxnVoidClosure() {
            @Override
            public void execute(Txn tx) throws Exception {
                list.offerLast("1");
                list.offerLast("2");
                list.offerLast("3");

                String found = list.peekFirst();

                assertEquals("1", found);
                assertEquals("[1, 2, 3]", list.toString());
                assertEquals(3, list.size());
            }
        });
    }

    @Test
    public void whenSingleItem() {
        atomic(new TxnVoidClosure() {
            @Override
            public void execute(Txn tx) throws Exception {
                String item = "1";
                list.put(item);

                String found = list.peekFirst();

                assertSame(item, found);
                assertEquals("[1]", list.toString());
                assertEquals(1, list.size());
            }
        });
    }
}
