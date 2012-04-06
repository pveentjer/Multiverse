package org.multiverse.collections;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.multiverse.api.Stm;
import org.multiverse.api.Txn;
import org.multiverse.api.closures.TxnVoidClosure;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.multiverse.api.GlobalStmInstance.getGlobalStmInstance;
import static org.multiverse.api.StmUtils.atomic;
import static org.multiverse.api.TxnThreadLocal.clearThreadLocalTxn;

public class NaiveTxnLinkedList_offerLastTest {

    private Stm stm;

    @Before
    public void setUp() {
        stm = getGlobalStmInstance();
        clearThreadLocalTxn();
    }

    @Test
    public void whenNullItem_thenNullPointerException() {
        final NaiveTxnLinkedList<String> list = new NaiveTxnLinkedList<String>(stm);

        atomic(new TxnVoidClosure() {
            @Override
            public void execute(Txn tx) throws Exception {
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
        final NaiveTxnLinkedList<String> list =
                new NaiveTxnLinkedList<String>(stm);

        atomic(new TxnVoidClosure() {
            @Override
            public void execute(Txn tx) throws Exception {
                String item = "1";
                list.offerLast(item);

                assertEquals(1, list.size());
                assertEquals("[1]", list.toString());
            }
        });
    }

    @Test
    public void whenNotEmpty() {
        final NaiveTxnLinkedList<String> list =
                new NaiveTxnLinkedList<String>(stm);

        atomic(new TxnVoidClosure() {
            @Override
            public void execute(Txn tx) throws Exception {
                list.offerLast("1");
                list.offerLast("2");
                list.offerLast("3");

                assertEquals(3, list.size());
                assertEquals("[1, 2, 3]", list.toString());
            }
        });
    }

    @Test
    @Ignore
    public void whenFull() {

    }
}
