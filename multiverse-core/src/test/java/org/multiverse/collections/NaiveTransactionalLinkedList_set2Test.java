package org.multiverse.collections;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.Stm;
import org.multiverse.api.StmUtils;
import org.multiverse.api.Transaction;
import org.multiverse.api.closures.AtomicVoidClosure;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.multiverse.api.GlobalStmInstance.getGlobalStmInstance;
import static org.multiverse.api.ThreadLocalTransaction.clearThreadLocalTransaction;

public class NaiveTransactionalLinkedList_set2Test {

    private Stm stm;
    private NaiveTransactionalLinkedList<String> list;

    @Before
    public void setUp() {
        stm = getGlobalStmInstance();
        clearThreadLocalTransaction();
        list = new NaiveTransactionalLinkedList<String>(stm);
    }

    @Test
    public void whenIndexTooSmall() {
        StmUtils.atomic(new AtomicVoidClosure() {
            @Override
            public void execute(Transaction tx) throws Exception {
                try {
                    list.set(-1, "foo");
                    fail();
                } catch (IndexOutOfBoundsException expected) {
                }

                assertEquals(0, list.size());
                assertEquals("[]", list.toString());
            }
        });
    }

    @Test
    public void whenIndexTooBig() {
        StmUtils.atomic(new AtomicVoidClosure() {
            @Override
            public void execute(Transaction tx) throws Exception {
                list.add("1");
                list.add("2");
                try {
                    list.set(2, "foo");
                    fail();
                } catch (IndexOutOfBoundsException expected) {
                }

                assertEquals(2, list.size());
                assertEquals("[1, 2]", list.toString());
            }
        });
    }

    @Test
    public void whenSuccess() {
        StmUtils.atomic(new AtomicVoidClosure() {
            @Override
            public void execute(Transaction tx) throws Exception {
                list.add("1");
                list.add("2");
                list.add("3");
                list.add("4");

                assertEquals("1", list.set(0, "a"));
                assertEquals("2", list.set(1, "b"));
                assertEquals("3", list.set(2, "c"));
                assertEquals("4", list.set(3, "d"));

                assertEquals(4, list.size());
                assertEquals("[a, b, c, d]", list.toString());
            }
        });
    }
}
