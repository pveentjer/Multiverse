package org.multiverse.stms.gamma.transactions;

import org.junit.Test;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.GammaStmConfig;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Peter Veentjer
 */
public class GammaTxnConfigTest {

    @Test
    public void testIsRichMansConflictScanRequired() {
        GammaStmConfig stmConfig = new GammaStmConfig();
        stmConfig.maximumPoorMansConflictScanLength = 0;
        stmConfig.speculativeConfigEnabled = true;
        GammaStm stm = new GammaStm(stmConfig);
        GammaTxnConfig txConfig = new GammaTxnConfig(stm, stmConfig);
        txConfig.init();

        assertTrue(txConfig.speculativeConfiguration.get().richMansConflictScanRequired);
    }

    @Test
    public void testIsRichMansConflictScanRequiredIfMaximumPoorMansConflictScanLengthIsZero() {
        GammaStmConfig stmConfig = new GammaStmConfig();
        stmConfig.maximumPoorMansConflictScanLength = 10;
        stmConfig.speculativeConfigEnabled = true;
        stmConfig.dirtyCheck = false;
        GammaStm stm = new GammaStm(stmConfig);
        GammaTxnConfig txConfig = new GammaTxnConfig(stm, stmConfig);
        txConfig.init();

        assertFalse(txConfig.speculativeConfiguration.get().richMansConflictScanRequired);
    }
}
