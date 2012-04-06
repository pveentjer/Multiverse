package org.multiverse.stms.gamma.transactionalobjects;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.functions.Functions;
import org.multiverse.api.functions.LongFunction;
import org.multiverse.stms.gamma.GammaConstants;
import org.multiverse.stms.gamma.GammaObjectPool;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactionalobjects.Tranlocal;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.multiverse.stms.gamma.GammaTestUtils.assertHasCommutingFunctions;

public class AddCommutingFunctionTest implements GammaConstants {

    private GammaObjectPool pool;
    private GammaStm stm;

    @Before
    public void setUp() {
        stm = new GammaStm();
        pool = new GammaObjectPool();
    }

    @Test
    public void whenFirstAddition() {
        Tranlocal tranlocal = new Tranlocal();
        tranlocal.mode = TRANLOCAL_COMMUTING;
        tranlocal.addCommutingFunction(pool, Functions.incLongFunction(1));

        assertFalse(tranlocal.isRead());
        assertTrue(tranlocal.isCommuting());
        assertEquals(0, tranlocal.long_value);
        assertHasCommutingFunctions(tranlocal, Functions.incLongFunction(1));
    }

    @Test
    public void whenMultipleAdditions() {
        Tranlocal tranlocal = new Tranlocal();
        tranlocal.mode = TRANLOCAL_COMMUTING;

        LongFunction function1 = mock(LongFunction.class);
        LongFunction function2 = mock(LongFunction.class);
        LongFunction function3 = mock(LongFunction.class);

        tranlocal.addCommutingFunction(pool, function1);
        tranlocal.addCommutingFunction(pool, function2);
        tranlocal.addCommutingFunction(pool, function3);

        assertFalse(tranlocal.isRead());
        assertTrue(tranlocal.isCommuting());
        assertEquals(0, tranlocal.long_value);
        assertHasCommutingFunctions(tranlocal, function3, function2, function1);
    }
}
