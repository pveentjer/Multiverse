package org.multiverse.stms.gamma.transactions.fat;

import org.multiverse.stms.gamma.transactionalobjects.GammaRefTranlocal;
import org.multiverse.stms.gamma.transactions.GammaTxnConfiguration;

import static org.junit.Assert.assertNull;

public class FatFixedLengthGammaTxn_abortTest extends FatGammaTxn_abortTest<FatFixedLengthGammaTxn> {

    @Override
    protected void assertCleaned(FatFixedLengthGammaTxn tx) {
        GammaRefTranlocal node = tx.head;
        while (node != null) {
            assertNull(node.owner);
            node = node.next;
        }
    }

    @Override
    protected FatFixedLengthGammaTxn newTransaction() {
        return new FatFixedLengthGammaTxn(stm);
    }

    @Override
    protected FatFixedLengthGammaTxn newTransaction(GammaTxnConfiguration config) {
        return new FatFixedLengthGammaTxn(config);
    }
}
