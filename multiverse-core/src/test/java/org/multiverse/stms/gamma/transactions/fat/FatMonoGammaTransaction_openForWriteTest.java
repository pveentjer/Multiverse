package org.multiverse.stms.gamma.transactions.fat;

import org.junit.Test;
import org.multiverse.api.exceptions.ReadWriteConflict;
import org.multiverse.stms.gamma.transactionalobjects.GammaLongRef;
import org.multiverse.stms.gamma.transactionalobjects.GammaRefTranlocal;
import org.multiverse.stms.gamma.transactions.GammaTransaction;
import org.multiverse.stms.gamma.transactions.GammaTransactionConfiguration;

import static org.junit.Assert.*;
import static org.multiverse.TestUtils.assertIsAborted;
import static org.multiverse.TestUtils.assertIsActive;
import static org.multiverse.stms.gamma.GammaTestUtils.*;

public class FatMonoGammaTransaction_openForWriteTest extends FatGammaTransaction_openForWriteTest {

    @Override
    protected GammaTransaction newTransaction(GammaTransactionConfiguration config) {
        return new FatMonoGammaTransaction(config);
    }

    protected GammaTransaction newTransaction() {
        return new FatMonoGammaTransaction(new GammaTransactionConfiguration(stm));
    }

    @Override
    protected int getMaxCapacity() {
        return 1;
    }


}
