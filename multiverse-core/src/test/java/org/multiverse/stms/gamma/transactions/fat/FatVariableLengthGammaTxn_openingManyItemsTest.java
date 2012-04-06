package org.multiverse.stms.gamma.transactions.fat;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.stms.gamma.GammaConstants;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactionalobjects.GammaTxnLong;
import org.multiverse.stms.gamma.transactionalobjects.Tranlocal;
import org.multiverse.stms.gamma.transactions.GammaTxnConfig;

import static junit.framework.Assert.assertSame;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.multiverse.TestUtils.assertIsActive;

public class FatVariableLengthGammaTxn_openingManyItemsTest implements GammaConstants {
    private GammaStm stm;

    @Before
    public void setUp() {
        stm = new GammaStm();
    }

    @Test
    public void whenReadonly() {
        whenManyItems(true);
    }

    @Test
    public void whenUpdate() {
        whenManyItems(false);
    }

    public void whenManyItems(boolean reading) {
        int refCount = 10000;

        GammaTxnConfig config = new GammaTxnConfig(stm)
                .setMaximumPoorMansConflictScanLength(refCount);
        FatVariableLengthGammaTxn tx = new FatVariableLengthGammaTxn(config);

        GammaTxnLong[] refs = new GammaTxnLong[refCount];
        Tranlocal[] tranlocals = new Tranlocal[refCount];
        for (int k = 0; k < refCount; k++) {
            GammaTxnLong ref = new GammaTxnLong(stm);
            refs[k] = ref;
            tranlocals[k] = reading ? ref.openForRead(tx, LOCKMODE_NONE) : ref.openForWrite(tx, LOCKMODE_NONE);
        }

        assertEquals(refCount, tx.size());

        System.out.println("everything inserted");
        System.out.println("usage percentage: " + (100 * tx.getUsage()));

        for (int k = 0; k < refCount; k++) {
            GammaTxnLong ref = refs[k];
            Tranlocal found = reading ? ref.openForRead(tx, LOCKMODE_NONE) : ref.openForWrite(tx, LOCKMODE_NONE);
            assertNotNull(found);
            assertSame(ref, found.owner);
            assertSame("tranlocal is incorrect at " + k, tranlocals[k], found);
        }

        assertIsActive(tx);
    }
}
