package org.multiverse.stms.gamma;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.TransactionExecutor;
import org.multiverse.api.PropagationLevel;

import static org.junit.Assert.assertTrue;

public class GammaStm_transactionExecutorTest {

    private GammaStm stm;

    @Before
    public void setUp() {
        stm = new GammaStm();
    }

    @Test
    public void whenCreateTransactionExecutor() {
        TransactionExecutor block = stm.getDefaultTransactionExecutor();
        assertTrue(block instanceof LeanGammaTransactionExecutor);
    }

    @Test
    public void testDefault() {
        TransactionExecutor block = stm.newTransactionFactoryBuilder()
                .newTransactionExecutor();
        assertTrue(block instanceof LeanGammaTransactionExecutor);
    }

    @Test
    public void whenMandatory() {
        TransactionExecutor block = stm.newTransactionFactoryBuilder()
                .setPropagationLevel(PropagationLevel.Mandatory)
                .newTransactionExecutor();
        assertTrue(block instanceof FatGammaTransactionExecutor);
    }

    @Test
    public void whenRequires() {
        TransactionExecutor block = stm.newTransactionFactoryBuilder()
                .setPropagationLevel(PropagationLevel.Requires)
                .newTransactionExecutor();
        assertTrue(block instanceof LeanGammaTransactionExecutor);
    }

    @Test
    public void whenRequiresNew() {
        TransactionExecutor block = stm.newTransactionFactoryBuilder()
                .setPropagationLevel(PropagationLevel.RequiresNew)
                .newTransactionExecutor();
        assertTrue(block instanceof FatGammaTransactionExecutor);
    }

    @Test
    public void whenNever() {
        TransactionExecutor block = stm.newTransactionFactoryBuilder()
                .setPropagationLevel(PropagationLevel.Never)
                .newTransactionExecutor();
        assertTrue(block instanceof FatGammaTransactionExecutor);
    }

    @Test
    public void whenSupports() {
        TransactionExecutor block = stm.newTransactionFactoryBuilder()
                .setPropagationLevel(PropagationLevel.Supports)
                .newTransactionExecutor();
        assertTrue(block instanceof FatGammaTransactionExecutor);
    }
}
