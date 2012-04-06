package org.multiverse.collections;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.Stm;
import org.multiverse.api.StmUtils;
import org.multiverse.api.Txn;
import org.multiverse.api.closures.AtomicVoidClosure;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.multiverse.api.GlobalStmInstance.getGlobalStmInstance;
import static org.multiverse.api.TxnThreadLocal.clearThreadLocalTxn;

public class NaiveTransactionalLinkedList_addLastTest {

     private Stm stm;

    @Before
    public void setUp() {
        stm = getGlobalStmInstance();
        clearThreadLocalTxn();
    }

    @Test
    public void whenNullItem_thenNullPointerException() {
        final NaiveTransactionalLinkedList<String> list = new NaiveTransactionalLinkedList<String>(stm);

        StmUtils.atomic(new AtomicVoidClosure() {
            @Override
            public void execute(Txn tx) throws Exception {
                try {
                    list.addLast(null);
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

        StmUtils.atomic(new AtomicVoidClosure() {
            @Override
            public void execute(Txn tx) throws Exception {
                list.addLast("1");

                assertEquals("[1]", list.toString());
                assertEquals(1, list.size());
            }
        });
    }

    @Test
    public void whenNotEmpty() {
        final NaiveTransactionalLinkedList<String> list = new NaiveTransactionalLinkedList<String>(stm);

        StmUtils.atomic(new AtomicVoidClosure() {
            @Override
            public void execute(Txn tx) throws Exception {
                list.add("1");
                list.addLast("2");

                assertEquals("[1, 2]", list.toString());
                assertEquals(2, list.size());
            }
        });
    }

    @Test
    public void whenFull() {
        final NaiveTransactionalLinkedList<String> list = new NaiveTransactionalLinkedList<String>(stm, 2);

        StmUtils.atomic(new AtomicVoidClosure() {
            @Override
            public void execute(Txn tx) throws Exception {
                list.add("1");
                list.add("2");

                try {
                    list.addLast("3");
                    fail();
                } catch (IllegalStateException expected) {
                }

                assertEquals("[1, 2]", list.toString());
                assertEquals(2, list.size());
            }
        });
    }
}
