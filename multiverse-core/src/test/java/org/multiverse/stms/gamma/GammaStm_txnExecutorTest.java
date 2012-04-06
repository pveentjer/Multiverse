package org.multiverse.stms.gamma;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.TxnExecutor;
import org.multiverse.api.PropagationLevel;

import static org.junit.Assert.assertTrue;

public class GammaStm_txnExecutorTest {

    private GammaStm stm;

    @Before
    public void setUp() {
        stm = new GammaStm();
    }

    @Test
    public void whenCreateTxnExecutor() {
        TxnExecutor executor = stm.getDefaultTxnExecutor();
        assertTrue(executor instanceof LeanGammaTxnExecutor);
    }

    @Test
    public void testDefault() {
        TxnExecutor executor = stm.newTxnFactoryBuilder()
                .newTxnExecutor();
        assertTrue(executor instanceof LeanGammaTxnExecutor);
    }

    @Test
    public void whenMandatory() {
        TxnExecutor executor = stm.newTxnFactoryBuilder()
                .setPropagationLevel(PropagationLevel.Mandatory)
                .newTxnExecutor();
        assertTrue(executor instanceof FatGammaTxnExecutor);
    }

    @Test
    public void whenRequires() {
        TxnExecutor executor = stm.newTxnFactoryBuilder()
                .setPropagationLevel(PropagationLevel.Requires)
                .newTxnExecutor();
        assertTrue(executor instanceof LeanGammaTxnExecutor);
    }

    @Test
    public void whenRequiresNew() {
        TxnExecutor executor = stm.newTxnFactoryBuilder()
                .setPropagationLevel(PropagationLevel.RequiresNew)
                .newTxnExecutor();
        assertTrue(executor instanceof FatGammaTxnExecutor);
    }

    @Test
    public void whenNever() {
        TxnExecutor executor = stm.newTxnFactoryBuilder()
                .setPropagationLevel(PropagationLevel.Never)
                .newTxnExecutor();
        assertTrue(executor instanceof FatGammaTxnExecutor);
    }

    @Test
    public void whenSupports() {
        TxnExecutor executor = stm.newTxnFactoryBuilder()
                .setPropagationLevel(PropagationLevel.Supports)
                .newTxnExecutor();
        assertTrue(executor instanceof FatGammaTxnExecutor);
    }
}
