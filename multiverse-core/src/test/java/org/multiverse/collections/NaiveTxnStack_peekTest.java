package org.multiverse.collections;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.Stm;
import org.multiverse.api.StmUtils;
import org.multiverse.api.Txn;
import org.multiverse.api.callables.TxnVoidCallable;

import static org.junit.Assert.*;
import static org.multiverse.api.GlobalStmInstance.getGlobalStmInstance;
import static org.multiverse.api.TxnThreadLocal.clearThreadLocalTxn;

public class NaiveTxnStack_peekTest {

    private Stm stm;
    private NaiveTxnStack<String> stack;

    @Before
    public void setUp() {
        stm = getGlobalStmInstance();
        clearThreadLocalTxn();
        stack = new NaiveTxnStack<String>(stm);
    }

    @Test
    public void whenEmpty(){
        StmUtils.atomic(new TxnVoidCallable() {
            @Override
            public void call(Txn tx) throws Exception {
                String s = stack.peek();
                assertNull(s);
                assertEquals("[]", stack.toString());
            }
        });
    }

    @Test
    public void whenNotEmpty(){
        StmUtils.atomic(new TxnVoidCallable() {
            @Override
            public void call(Txn tx) throws Exception {
                stack.push("1");
                stack.push("2");
                String s = stack.peek();
                assertSame("2", s);
                assertEquals("[2, 1]", stack.toString());
            }
        });
    }
}
