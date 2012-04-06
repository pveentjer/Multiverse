package org.multiverse.stms.gamma;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.Txn;
import org.multiverse.api.TxnExecutor;
import org.multiverse.api.closures.AtomicVoidClosure;
import org.multiverse.stms.gamma.transactions.GammaTxn;
import org.multiverse.stms.gamma.transactions.GammaTxnFactory;

import static org.junit.Assert.*;
import static org.multiverse.TestUtils.assertIsActive;
import static org.multiverse.api.TxnThreadLocal.clearThreadLocalTxn;
import static org.multiverse.api.TxnThreadLocal.getThreadLocalTxn;

public class LeanGammaTxnExecutor_integrationTest {
    private GammaStm stm;

    @Before
    public void setUp() {
        clearThreadLocalTxn();
        stm = new GammaStm();
    }

    @Test
    public void whenExecutedThenTxnThreadLocalSet() {
        GammaTxnFactory transactionFactory = stm.newTransactionFactoryBuilder().newTransactionFactory();
        TxnExecutor block = new LeanGammaTxnExecutor(transactionFactory);
        block.atomic(new AtomicVoidClosure() {
            @Override
            public void execute(Txn tx) throws Exception {
                assertNotNull(tx);
                assertIsActive((GammaTxn) tx);
                assertSame(tx, getThreadLocalTxn());
            }
        });

        assertNull(getThreadLocalTxn());
    }
}
