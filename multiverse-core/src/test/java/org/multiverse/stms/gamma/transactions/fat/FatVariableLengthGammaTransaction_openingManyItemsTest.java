package org.multiverse.stms.gamma.transactions.fat;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.stms.gamma.GammaConstants;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactionalobjects.GammaLongRef;
import org.multiverse.stms.gamma.transactionalobjects.GammaRefTranlocal;
import org.multiverse.stms.gamma.transactions.GammaTransactionConfiguration;

import static junit.framework.Assert.assertSame;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.multiverse.TestUtils.assertIsActive;

public class FatVariableLengthGammaTransaction_openingManyItemsTest implements GammaConstants {
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

        GammaTransactionConfiguration config = new GammaTransactionConfiguration(stm)
                .setMaximumPoorMansConflictScanLength(refCount);
        FatVariableLengthGammaTransaction tx = new FatVariableLengthGammaTransaction(config);

        GammaLongRef[] refs = new GammaLongRef[refCount];
        GammaRefTranlocal[] tranlocals = new GammaRefTranlocal[refCount];
        for (int k = 0; k < refCount; k++) {
            GammaLongRef ref = new GammaLongRef(stm);
            refs[k] = ref;
            tranlocals[k] = reading ? ref.openForRead(tx, LOCKMODE_NONE) : ref.openForWrite(tx, LOCKMODE_NONE);
        }

        assertEquals(refCount, tx.size());

        System.out.println("everything inserted");
        System.out.println("usage percentage: " + (100 * tx.getUsage()));

        for (int k = 0; k < refCount; k++) {
            GammaLongRef ref = refs[k];
            GammaRefTranlocal found = reading ? ref.openForRead(tx, LOCKMODE_NONE) : ref.openForWrite(tx, LOCKMODE_NONE);
            assertNotNull(found);
            assertSame(ref, found.owner);
            assertSame("tranlocal is incorrect at " + k, tranlocals[k], found);
        }

        assertIsActive(tx);
    }
}
