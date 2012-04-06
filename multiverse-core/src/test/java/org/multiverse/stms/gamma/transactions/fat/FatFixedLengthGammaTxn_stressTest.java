package org.multiverse.stms.gamma.transactions.fat;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.stms.gamma.GammaConstants;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactionalobjects.GammaLongRef;
import org.multiverse.stms.gamma.transactions.GammaTxnConfig;

import java.util.Random;

import static org.junit.Assert.assertEquals;

public class FatFixedLengthGammaTxn_stressTest implements GammaConstants {

    private GammaStm stm;

    @Before
    public void setUp() {
        stm = new GammaStm();
    }

    @Test
    public void newTransaction_whenMultipleUpdatesAndDirtyCheckEnabled() {
        integrationTest_whenMultipleUpdatesAndDirtyCheck(true, false);
    }

    @Test
    public void newTransaction_whenMultipleUpdatesAndDirtyCheckDisabled() {
        integrationTest_whenMultipleUpdatesAndDirtyCheck(false, false);
    }

    @Test
    public void reuseTransaction_whenMultipleUpdatesAndDirtyCheckEnabled() {
        integrationTest_whenMultipleUpdatesAndDirtyCheck(true, true);
    }

    @Test
    public void reuseTransaction_whenMultipleUpdatesAndDirtyCheckDisabled() {
        integrationTest_whenMultipleUpdatesAndDirtyCheck(false, true);
    }

    public void integrationTest_whenMultipleUpdatesAndDirtyCheck(final boolean dirtyCheck, final boolean transactionReuse) {
        GammaLongRef[] refs = new GammaLongRef[30];
        long created = 0;

        //create the references
        for (int k = 0; k < refs.length; k++) {
            refs[k] = new GammaLongRef(stm, 0);
        }

        //atomicChecked all transactions
        Random random = new Random();
        int transactionCount = 100000;

        GammaTxnConfig config = new GammaTxnConfig(stm, refs.length)
                .setMaximumPoorMansConflictScanLength(refs.length);
        config.dirtyCheck = dirtyCheck;

        FatFixedLengthGammaTxn tx = null;
        for (int transaction = 0; transaction < transactionCount; transaction++) {

            if (transactionReuse) {
                if (tx == null) {
                    tx = new FatFixedLengthGammaTxn(config);
                }
            } else {
                tx = new FatFixedLengthGammaTxn(config);
            }

            for (int k = 0; k < refs.length; k++) {
                if (random.nextInt(3) == 1) {
                    refs[k].openForWrite(tx, LOCKMODE_NONE).long_value++;
                    created++;
                } else {
                    refs[k].openForWrite(tx, LOCKMODE_NONE);
                }
            }
            tx.commit();
            tx.hardReset();

            if (transaction % 1000 == 0) {
                System.out.println("at " + transaction);
            }
        }

        long sum = 0;
        for (int k = 0; k < refs.length; k++) {
            sum += refs[k].atomicGet();
        }

        assertEquals(created, sum);
    }
}
