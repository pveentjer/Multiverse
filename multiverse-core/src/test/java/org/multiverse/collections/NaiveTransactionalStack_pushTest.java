package org.multiverse.collections;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.multiverse.api.Stm;
import org.multiverse.api.StmUtils;
import org.multiverse.api.Txn;
import org.multiverse.api.closures.AtomicVoidClosure;

import static org.junit.Assert.*;
import static org.multiverse.api.GlobalStmInstance.getGlobalStmInstance;

import static org.multiverse.api.TxnThreadLocal.clearThreadLocalTxn;

public class NaiveTransactionalStack_pushTest {

    private Stm stm;

    @Before
    public void setUp() {
        stm = getGlobalStmInstance();
        clearThreadLocalTxn();
    }

    @Test
    public void whenNullItem_thenNullPointerException() {
        final NaiveTransactionalStack<String> stack = new NaiveTransactionalStack<String>(stm);

        StmUtils.atomic(new AtomicVoidClosure() {
            @Override
            public void execute(Txn tx) throws Exception {
                try {
                    stack.push(null);
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
            public void execute(Txn tx) throws Exception {
                stack.push("1");

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
            public void execute(Txn tx) throws Exception {
                stack.push("1");
                stack.push("2");

                assertEquals("[2, 1]", stack.toString());
                assertEquals(2, stack.size());
            }
        });
    }

    @Test
    @Ignore
    public void whenFull() {
        final NaiveTransactionalStack<String> stack = new NaiveTransactionalStack<String>(stm, 2);

        StmUtils.atomic(new AtomicVoidClosure() {
            @Override
            public void execute(Txn tx) throws Exception {
                stack.push("1");
                stack.push("2");
                stack.push("3");

                assertEquals("[2, 1]", stack.toString());
                assertEquals(2, stack.size());
            }
        });
    }
}
