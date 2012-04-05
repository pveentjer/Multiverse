package org.multiverse.collections;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.multiverse.api.Stm;
import org.multiverse.api.StmUtils;
import org.multiverse.api.Transaction;
import org.multiverse.api.closures.AtomicVoidClosure;
import org.multiverse.api.exceptions.RetryError;

import static org.junit.Assert.*;
import static org.multiverse.api.GlobalStmInstance.getGlobalStmInstance;
import static org.multiverse.api.ThreadLocalTransaction.clearThreadLocalTransaction;

public class NaiveTransactionalStack_popTest {

    private Stm stm;
    private NaiveTransactionalStack<String> stack;

    @Before
    public void setUp() {
        stm = getGlobalStmInstance();
        clearThreadLocalTransaction();
        stack = new NaiveTransactionalStack<String>(stm);
    }

    @Test
    public void whenSingleItem() {
        StmUtils.atomic(new AtomicVoidClosure() {
            @Override
            public void execute(Transaction tx) throws Exception {
                String item = "1";
                stack.push(item);

                String found = stack.pop();
                assertSame(found, item);
                assertEquals(0, stack.size());
                assertEquals("[]", stack.toString());
            }
        });
    }

    @Test
    public void whenMultipleItems() {
        StmUtils.atomic(new AtomicVoidClosure() {
            @Override
            public void execute(Transaction tx) throws Exception {
                String item1 = "1";
                String item2 = "2";
                stack.push(item1);
                stack.push(item2);

                String found = stack.pop();
                assertSame(found, item2);
                assertEquals(1, stack.size());
                assertEquals("[1]", stack.toString());
            }
        });
    }

    @Test
    @Ignore
    public void whenEmpty_thenRetryError() {
        StmUtils.atomic(new AtomicVoidClosure() {
            @Override
            public void execute(Transaction tx) throws Exception {
                try {
                    stack.pop();
                    fail();
                } catch (RetryError retry) {
                }
            }
        });
    }
}
