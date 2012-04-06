package org.multiverse.stms.gamma;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.TxnExecutor;
import org.multiverse.api.Transaction;
import org.multiverse.api.closures.AtomicVoidClosure;
import org.multiverse.stms.gamma.transactions.GammaTransaction;
import org.multiverse.stms.gamma.transactions.GammaTxnFactory;

import static org.junit.Assert.*;
import static org.multiverse.TestUtils.assertIsActive;
import static org.multiverse.api.ThreadLocalTransaction.clearThreadLocalTransaction;
import static org.multiverse.api.ThreadLocalTransaction.getThreadLocalTransaction;

public class LeanGammaTxnExecutor_integrationTest {
    private GammaStm stm;

    @Before
    public void setUp() {
        clearThreadLocalTransaction();
        stm = new GammaStm();
    }

    @Test
    public void whenExecutedThenThreadLocalTransactionSet() {
        GammaTxnFactory transactionFactory = stm.newTransactionFactoryBuilder().newTransactionFactory();
        TxnExecutor block = new LeanGammaTxnExecutor(transactionFactory);
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
