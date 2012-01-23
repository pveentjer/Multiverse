package org.multiverse.stms.gamma.transactions.fat;

import org.multiverse.stms.gamma.transactionalobjects.GammaRefTranlocal;
import org.multiverse.stms.gamma.transactions.GammaTransactionConfiguration;

import static org.junit.Assert.assertNull;

public class FatFixedLengthGammaTransaction_abortTest extends FatGammaTransaction_abortTest<FatFixedLengthGammaTransaction> {

    @Override
    protected void assertCleaned(FatFixedLengthGammaTransaction tx) {
        GammaRefTranlocal node = tx.head;
        while (node != null) {
            assertNull(node.owner);
            node = node.next;
        }
    }

    @Override
    protected FatFixedLengthGammaTransaction newTransaction() {
        return new FatFixedLengthGammaTransaction(stm);
    }

    @Override
    protected FatFixedLengthGammaTransaction newTransaction(GammaTransactionConfiguration config) {
        return new FatFixedLengthGammaTransaction(config);
    }
}
