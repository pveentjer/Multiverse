package org.multiverse.collections;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.Stm;
import org.multiverse.api.StmUtils;
import org.multiverse.api.Txn;
import org.multiverse.api.closures.TxnVoidClosure;

import static org.junit.Assert.*;
import static org.multiverse.api.GlobalStmInstance.getGlobalStmInstance;
import static org.multiverse.api.TxnThreadLocal.clearThreadLocalTxn;

public class NaiveTxnStack_pollTest {

    private Stm stm;
    private NaiveTxnStack<String> stack;

    @Before
    public void setUp() {
        stm = getGlobalStmInstance();
        clearThreadLocalTxn();
        stack = new NaiveTxnStack<String>(stm);
    }

    @Test
    public void whenEmpty() {
        StmUtils.atomic(new TxnVoidClosure() {
            @Override
            public void execute(Txn tx) throws Exception {
                String item = stack.poll();
                assertNull(item);
                assertEquals("[]", stack.toString());
            }
        });
    }

    @Test
    public void whenSingleItem() {
        StmUtils.atomic(new TxnVoidClosure() {
            @Override
            public void execute(Txn tx) throws Exception {
                stack.push("1");

                String found = stack.poll();
                assertEquals("1", found);
                assertTrue(stack.isEmpty());
                assertEquals("[]", stack.toString());
            }
        });
    }

    @Test
    public void whenMultipleItem() {
        StmUtils.atomic(new TxnVoidClosure() {
            @Override
            public void execute(Txn tx) throws Exception {
                stack.push("1");
                stack.push("2");

                String found = stack.poll();
                assertEquals("2", found);
                assertEquals(1, stack.size());
                assertEquals("[1]", stack.toString());
            }
        });
    }
}
