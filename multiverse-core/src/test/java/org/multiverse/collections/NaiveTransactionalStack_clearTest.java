package org.multiverse.collections;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.Stm;
import org.multiverse.api.StmUtils;
import org.multiverse.api.Txn;
import org.multiverse.api.closures.AtomicVoidClosure;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.multiverse.api.GlobalStmInstance.getGlobalStmInstance;
import static org.multiverse.api.TxnThreadLocal.clearThreadLocalTxn;

public class NaiveTransactionalStack_clearTest {

    private Stm stm;
    private NaiveTransactionalStack<String> stack;

    @Before
    public void setUp() {
        stm = getGlobalStmInstance();
        clearThreadLocalTxn();
        stack = new NaiveTransactionalStack<String>(stm);
    }

    @Test
    public void whenEmpty() {
        StmUtils.atomic(new AtomicVoidClosure() {
            @Override
            public void execute(Txn tx) throws Exception {
                stack.clear();
                assertTrue(stack.isEmpty());
                assertEquals("[]", stack.toString());
            }
        });
    }

    @Test
    public void whenNotEmpty(){
         StmUtils.atomic(new AtomicVoidClosure() {
             @Override
             public void execute(Txn tx) throws Exception {
                 stack.push("1");
                 stack.push("2");
                 stack.clear();
                 assertTrue(stack.isEmpty());
                 assertEquals("[]", stack.toString());
             }
         });
    }
}
