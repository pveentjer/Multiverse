package org.multiverse.collections;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.Stm;
import org.multiverse.api.StmUtils;
import org.multiverse.api.Txn;
import org.multiverse.api.closures.TxnVoidClosure;

import static org.junit.Assert.assertEquals;
import static org.multiverse.api.GlobalStmInstance.getGlobalStmInstance;

import static org.multiverse.api.StmUtils.atomic;
import static org.multiverse.api.TxnThreadLocal.clearThreadLocalTxn;

public class NaiveTxnLinkedList_lastIndexOfTest {

    private Stm stm;
    private NaiveTxnLinkedList<String> list;

    @Before
    public void setUp() {
        stm = getGlobalStmInstance();
        clearThreadLocalTxn();
        list = new NaiveTxnLinkedList<String>(stm);
    }

    @Test
    public void whenNullItem_thenMinusOne() {
        atomic(new TxnVoidClosure() {
            @Override
            public void call(Txn tx) throws Exception {
                int result = list.lastIndexOf(null);
                assertEquals(result, -1);
                assertEquals("[]", list.toString());
            }
        });
    }

    @Test
    public void whenEmptyList() {
        atomic(new TxnVoidClosure() {
            @Override
            public void call(Txn tx) throws Exception {
                int result = list.lastIndexOf("a");
                assertEquals(result, -1);
                assertEquals("[]", list.toString());
            }
        });
    }

    @Test
    public void whenNotFound_thenMinusOne() {
        atomic(new TxnVoidClosure() {
            @Override
            public void call(Txn tx) throws Exception {
                list.add("1");
                list.add("2");
                list.add("3");
                list.add("4");

                int result = list.lastIndexOf("a");
                assertEquals(result, -1);
                assertEquals("[1, 2, 3, 4]", list.toString());
            }
        });
    }

    @Test
    public void whenOnlyOnceInCollection() {
        StmUtils.atomic(new TxnVoidClosure() {
            @Override
            public void call(Txn tx) throws Exception {
                list.add("1");
                list.add("2");
                list.add("3");
                list.add("4");

                int result = list.lastIndexOf("2");
                assertEquals(1, result);
                assertEquals("[1, 2, 3, 4]", list.toString());
            }
        });
    }

    @Test
    public void whenMultipleTimesInCollection() {
          StmUtils.atomic(new TxnVoidClosure() {
              @Override
              public void call(Txn tx) throws Exception {
                  list.add("1");
                  list.add("2");
                  list.add("3");
                  list.add("4");
                  list.add("2");
                  list.add("5");


                  int result = list.lastIndexOf("2");
                  assertEquals(4, result);
                  assertEquals("[1, 2, 3, 4, 2, 5]", list.toString());
              }
          });
    }
}
