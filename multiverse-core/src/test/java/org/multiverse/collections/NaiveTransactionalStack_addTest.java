package org.multiverse.collections;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.Stm;
import org.multiverse.api.StmUtils;
import org.multiverse.api.Transaction;
import org.multiverse.api.closures.AtomicVoidClosure;

import static org.junit.Assert.*;
import static org.multiverse.api.GlobalStmInstance.getGlobalStmInstance;
import static org.multiverse.api.ThreadLocalTransaction.clearThreadLocalTransaction;

public class NaiveTransactionalStack_addTest {

    private Stm stm;

    @Before
    public void setUp() {
        stm = getGlobalStmInstance();
        clearThreadLocalTransaction();
    }

    @Test
    public void whenNullItem_thenNullPointerException() {
        final NaiveTransactionalStack<String> stack = new NaiveTransactionalStack<String>(stm);

        StmUtils.atomic(new AtomicVoidClosure() {
            @Override
            public void execute(Transaction tx) throws Exception {
                try {
                    stack.add(null);
                    fail();
                } catch (NullPointerException expected) {

                }

                assertEquals("[]", stack.toString());
                assertEquals(0, stack.size());
            }
        });
    }

    @Test
    public void whenEmpty() {
        final NaiveTransactionalStack<String> stack = new NaiveTransactionalStack<String>(stm);

        StmUtils.atomic(new AtomicVoidClosure() {
            @Override
            public void execute(Transaction tx) throws Exception {
                boolean result = stack.add("1");

                assertTrue(result);
                assertEquals("[1]", stack.toString());
                assertEquals(1, stack.size());
            }
        });
    }

    @Test
    public void whenNotEmpty() {
        final NaiveTransactionalStack<String> stack = new NaiveTransactionalStack<String>(stm);

        StmUtils.atomic(new AtomicVoidClosure() {
            @Override
            public void execute(Transaction tx) throws Exception {
                stack.add("1");
                boolean result = stack.add("2");

                assertTrue(result);
                assertEquals("[2, 1]", stack.toString());
                assertEquals(2, stack.size());
            }
        });
    }

    @Test
    public void whenFull() {
        final NaiveTransactionalStack<String> stack = new NaiveTransactionalStack<String>(stm, 2);

        StmUtils.atomic(new AtomicVoidClosure() {
            @Override
            public void execute(Transaction tx) throws Exception {
                stack.add("1");
                stack.add("2");

                try {
                    stack.add("3");
                    fail();
                } catch (IllegalStateException expected) {

                }

                assertEquals("[2, 1]", stack.toString());
                assertEquals(2, stack.size());
            }
        });
    }
}
