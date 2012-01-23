package org.multiverse.stms.gamma;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.*;

public class GammaObjectPool_arrayListTest {
    private GammaObjectPool pool;

    @Before
    public void setUp() {
        pool = new GammaObjectPool();
    }

    @Test(expected = NullPointerException.class)
    public void whenNullArrayList_thenNullPointerException() {
        pool.putArrayList(null);
    }

    @Test
    public void whenPlacedInPoolThenCleared() {
        ArrayList list = new ArrayList();
        list.add("foo");
        list.add("bar");

        pool.putArrayList(list);

        assertTrue(list.isEmpty());
    }

    @Test
    public void whenSuccess() {
        ArrayList list1 = new ArrayList();
        ArrayList list2 = new ArrayList();
        ArrayList list3 = new ArrayList();

        pool.putArrayList(list1);
        pool.putArrayList(list2);
        pool.putArrayList(list3);

        assertSame(list3, pool.takeArrayList());
        assertSame(list2, pool.takeArrayList());
        assertSame(list1, pool.takeArrayList());
        assertNotNull(pool.takeArrayList());
    }
}
