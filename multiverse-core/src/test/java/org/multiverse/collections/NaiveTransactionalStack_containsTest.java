package org.multiverse.collections;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.Stm;
import org.multiverse.api.Transaction;
import org.multiverse.api.closures.AtomicVoidClosure;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.multiverse.api.GlobalStmInstance.getGlobalStmInstance;
import static org.multiverse.api.StmUtils.execute;
import static org.multiverse.api.ThreadLocalTransaction.clearThreadLocalTransaction;

public class NaiveTransactionalStack_containsTest {

    private Stm stm;
    private NaiveTransactionalStack<String> stack;

    @Before
    public void setUp() {
        stm = getGlobalStmInstance();
        clearThreadLocalTransaction();
        stack = new NaiveTransactionalStack<String>(stm);
    }

    @Test
    public void whenNullItem() {
        execute(new AtomicVoidClosure() {
            @Override
            public void execute(Transaction tx) throws Exception {
                stack.push("1");
                stack.push("2");
                boolean result = stack.contains(null);
                assertFalse(result);
                assertEquals("[2, 1]", stack.toString());
            }
        });
    }

    @Test
    public void whenEmptyStack() {
        execute(new AtomicVoidClosure() {
            @Override
            public void execute(Transaction tx) throws Exception {
                boolean result = stack.contains("foo");

                assertFalse(result);
                assertEquals("[]", stack.toString());
            }
        });
    }

    @Test
    public void whenStackDoesntContainItem() {
        execute(new AtomicVoidClosure() {
            @Override
            public void execute(Transaction tx) throws Exception {
                stack.push("1");
                stack.push("2");
                stack.push("3");
                stack.push("4");

                boolean result = stack.contains("b");

                assertFalse(result);
                assertEquals("[4, 3, 2, 1]", stack.toString());
            }
        });
    }

    @Test
    public void whenContainsItem() {
         execute(new AtomicVoidClosure() {
            @Override
            public void execute(Transaction tx) throws Exception {
                stack.push("1");
                stack.push("2");
                stack.push("3");
                stack.push("4");

                boolean result = stack.contains("3");

                assertTrue(result);
                assertEquals("[4, 3, 2, 1]", stack.toString());
            }
        });
    }
}
