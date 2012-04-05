package org.multiverse.stms.gamma;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.TransactionExecutor;
import org.multiverse.api.Transaction;
import org.multiverse.api.closures.AtomicVoidClosure;
import org.multiverse.stms.gamma.transactions.GammaTransaction;
import org.multiverse.stms.gamma.transactions.GammaTransactionFactory;

import static org.junit.Assert.*;
import static org.multiverse.TestUtils.assertIsActive;
import static org.multiverse.api.ThreadLocalTransaction.clearThreadLocalTransaction;
import static org.multiverse.api.ThreadLocalTransaction.getThreadLocalTransaction;

public class LeanGammaTransactionExecutor_integrationTest {
    private GammaStm stm;

    @Before
    public void setUp() {
        clearThreadLocalTransaction();
        stm = new GammaStm();
    }

    @Test
    public void whenExecutedThenThreadLocalTransactionSet() {
        GammaTransactionFactory transactionFactory = stm.newTransactionFactoryBuilder().newTransactionFactory();
        TransactionExecutor block = new LeanGammaTransactionExecutor(transactionFactory);
        block.atomic(new AtomicVoidClosure() {
            @Override
            public void execute(Transaction tx) throws Exception {
                assertNotNull(tx);
                assertIsActive((GammaTransaction) tx);
                assertSame(tx, getThreadLocalTransaction());
            }
        });

        assertNull(getThreadLocalTransaction());
    }
}
