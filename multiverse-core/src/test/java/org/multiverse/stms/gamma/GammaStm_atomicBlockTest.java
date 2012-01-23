package org.multiverse.stms.gamma;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.AtomicBlock;
import org.multiverse.api.PropagationLevel;

import static org.junit.Assert.assertTrue;

public class GammaStm_atomicBlockTest {

    private GammaStm stm;

    @Before
    public void setUp() {
        stm = new GammaStm();
    }

    @Test
    public void whenCreateAtomicBlock() {
        AtomicBlock block = stm.getDefaultAtomicBlock();
        assertTrue(block instanceof LeanGammaAtomicBlock);
    }

    @Test
    public void testDefault() {
        AtomicBlock block = stm.newTransactionFactoryBuilder()
                .newAtomicBlock();
        assertTrue(block instanceof LeanGammaAtomicBlock);
    }

    @Test
    public void whenMandatory() {
        AtomicBlock block = stm.newTransactionFactoryBuilder()
                .setPropagationLevel(PropagationLevel.Mandatory)
                .newAtomicBlock();
        assertTrue(block instanceof FatGammaAtomicBlock);
    }

    @Test
    public void whenRequires() {
        AtomicBlock block = stm.newTransactionFactoryBuilder()
                .setPropagationLevel(PropagationLevel.Requires)
                .newAtomicBlock();
        assertTrue(block instanceof LeanGammaAtomicBlock);
    }

    @Test
    public void whenRequiresNew() {
        AtomicBlock block = stm.newTransactionFactoryBuilder()
                .setPropagationLevel(PropagationLevel.RequiresNew)
                .newAtomicBlock();
        assertTrue(block instanceof FatGammaAtomicBlock);
    }

    @Test
    public void whenNever() {
        AtomicBlock block = stm.newTransactionFactoryBuilder()
                .setPropagationLevel(PropagationLevel.Never)
                .newAtomicBlock();
        assertTrue(block instanceof FatGammaAtomicBlock);
    }

    @Test
    public void whenSupports() {
        AtomicBlock block = stm.newTransactionFactoryBuilder()
                .setPropagationLevel(PropagationLevel.Supports)
                .newAtomicBlock();
        assertTrue(block instanceof FatGammaAtomicBlock);
    }
}
