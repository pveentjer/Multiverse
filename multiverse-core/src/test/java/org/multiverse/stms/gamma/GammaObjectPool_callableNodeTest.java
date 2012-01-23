package org.multiverse.stms.gamma;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.functions.Function;
import org.multiverse.stms.gamma.transactionalobjects.CallableNode;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

public class GammaObjectPool_callableNodeTest {
    private GammaObjectPool pool;

    @Before
    public void setUp() {
        pool = new GammaObjectPool();
    }

    @Test(expected = NullPointerException.class)
    public void whenPutNullNode_thenNullPointerException() {
        pool.putCallableNode(null);
    }

    @Test
    public void whenPutInPool_thenPreparedForPooling() {
        Function function = mock(Function.class);
        CallableNode next = new CallableNode(null, null);
        CallableNode node = new CallableNode(function, next);

        pool.putCallableNode(node);

        assertNull(node.function);
        assertNull(node.next);
    }

    @Test
    public void whenSuccess() {
        Function function = mock(Function.class);
        CallableNode node1 = new CallableNode(function, null);
        CallableNode node2 = new CallableNode(function, null);
        CallableNode node3 = new CallableNode(function, null);

        pool.putCallableNode(node1);
        pool.putCallableNode(node2);
        pool.putCallableNode(node3);

        assertSame(node3, pool.takeCallableNode());
        assertSame(node2, pool.takeCallableNode());
        assertSame(node1, pool.takeCallableNode());
        assertNotNull(pool.takeCallableNode());
    }
}
