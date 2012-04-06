package org.multiverse.collections;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.Stm;
import org.multiverse.api.StmUtils;
import org.multiverse.api.Txn;
import org.multiverse.api.callables.TxnVoidCallable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.multiverse.api.GlobalStmInstance.getGlobalStmInstance;
import static org.multiverse.api.TxnThreadLocal.clearThreadLocalTxn;

public class NaiveTxnLinkedList_pushTest {
   private Stm stm;

    @Before
    public void setUp() {
        stm = getGlobalStmInstance();
        clearThreadLocalTxn();
    }

    @Test
    public void whenNullItem_thenNullPointerException() {
        final NaiveTxnLinkedList<String> list = new NaiveTxnLinkedList<String>(stm);

        StmUtils.atomic(new TxnVoidCallable() {
            @Override
            public void call(Txn tx) throws Exception {
                try {
                    list.push(null);
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
        final NaiveTxnLinkedList<String> list = new NaiveTxnLinkedList<String>(stm);

        StmUtils.atomic(new TxnVoidCallable() {
            @Override
            public void call(Txn tx) throws Exception {
                list.push("1");

                assertEquals("[1]", list.toString());
                assertEquals(1, list.size());
            }
        });
    }

    @Test
    public void whenNotEmpty() {
        final NaiveTxnLinkedList<String> list = new NaiveTxnLinkedList<String>(stm);

        StmUtils.atomic(new TxnVoidCallable() {
            @Override
            public void call(Txn tx) throws Exception {
                list.add("1");
                list.push("2");

                assertEquals("[2, 1]", list.toString());
                assertEquals(2, list.size());
            }
        });
    }

    @Test
    public void whenFull() {
        final NaiveTxnLinkedList<String> list = new NaiveTxnLinkedList<String>(stm, 2);

        StmUtils.atomic(new TxnVoidCallable() {
            @Override
            public void call(Txn tx) throws Exception {
                list.add("1");
                list.add("2");

                try {
                    list.push("3");
                    fail();
                } catch (IllegalStateException expected) {
                }

                assertEquals("[1, 2]", list.toString());
                assertEquals(2, list.size());
            }
        });
    }
}
