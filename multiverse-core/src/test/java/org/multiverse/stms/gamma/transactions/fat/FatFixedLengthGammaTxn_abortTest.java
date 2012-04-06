package org.multiverse.stms.gamma.transactions.fat;

import org.multiverse.stms.gamma.transactionalobjects.Tranlocal;
import org.multiverse.stms.gamma.transactions.GammaTxnConfig;

import static org.junit.Assert.assertNull;

public class FatFixedLengthGammaTxn_abortTest extends FatGammaTxn_abortTest<FatFixedLengthGammaTxn> {

    @Override
    protected void assertCleaned(FatFixedLengthGammaTxn tx) {
        Tranlocal node = tx.head;
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
    protected FatFixedLengthGammaTxn newTransaction(GammaTxnConfig config) {
        return new FatFixedLengthGammaTxn(config);
    }
}
