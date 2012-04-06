package org.multiverse.collections;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.Stm;
import org.multiverse.api.StmUtils;
import org.multiverse.api.Txn;
import org.multiverse.api.closures.AtomicVoidClosure;

import static org.junit.Assert.assertEquals;
import static org.multiverse.api.GlobalStmInstance.getGlobalStmInstance;
import static org.multiverse.api.TxnThreadLocal.clearThreadLocalTxn;

public class NaiveTransactionalLinkedList_clearTest {

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
                list.clear();

                assertEquals(0, list.size());
                assertEquals("[]", list.toString());
            }
        });
    }

    @Test
    public void whenSingleItem() {
        StmUtils.atomic(new AtomicVoidClosure() {
            @Override
            public void execute(Txn tx) throws Exception {
                list.put("foo");
                list.clear();

                assertEquals(0, list.size());
                assertEquals("[]", list.toString());
            }
        });
    }

    @Test
    public void whenMultipleItems() {
        StmUtils.atomic(new AtomicVoidClosure() {
            @Override
            public void execute(Txn tx) throws Exception {
                list.put("foo");
                list.put("bar");
                list.clear();

                assertEquals(0, list.size());
                assertEquals("[]", list.toString());
            }
        });
    }
}
