package org.multiverse.stms.gamma.transactions;

import org.multiverse.stms.gamma.GammaConstants;
import org.multiverse.stms.gamma.transactions.fat.FatFixedLengthGammaTxn;
import org.multiverse.stms.gamma.transactions.fat.FatMonoGammaTxn;
import org.multiverse.stms.gamma.transactions.fat.FatVariableLengthGammaTxn;
import org.multiverse.stms.gamma.transactions.lean.LeanFixedLengthGammaTxn;
import org.multiverse.stms.gamma.transactions.lean.LeanMonoGammaTxn;

/**
 * A pool for pooling GammaTxns.
 *
 * @author Peter Veentjer.
 */
@SuppressWarnings({"ClassWithTooManyFields"})
public final class GammaTxnPool implements GammaConstants {

    private final static boolean ENABLED = Boolean.parseBoolean(
            System.getProperty("org.multiverse.stm.gamma.transactions.GammaTxnPool.enabled", "true"));

    private final boolean enabled;

    private final FatMonoGammaTxn[] poolFatMono = new FatMonoGammaTxn[10];
    private int poolFatMonoIndex = -1;
    private final FatFixedLengthGammaTxn[] poolFatFixedLength = new FatFixedLengthGammaTxn[10];
    private int poolFatFixedLengthIndex = -1;
    private final LeanMonoGammaTxn[] poolLeanMono = new LeanMonoGammaTxn[10];
    private int poolLeanMonoIndex = -1;
    private final LeanFixedLengthGammaTxn[] poolLeanFixedLength = new LeanFixedLengthGammaTxn[10];
    private int poolLeanFixedLengthIndex = -1;
    private final FatVariableLengthGammaTxn[] poolFatVariableLength = new FatVariableLengthGammaTxn[10];
    private int poolFatVariableLengthIndex = -1;

    public GammaTxnPool() {
        enabled = ENABLED;
    }

    /**
     * Takes a FatMonoGammaTxn from the pool.
     *
     * @return the taken FatMonoGammaTxn or null of none available.
     */
    public FatMonoGammaTxn takeFatMono() {
        if (!enabled || poolFatMonoIndex == -1) {
            return null;
        }

        FatMonoGammaTxn tx = poolFatMono[poolFatMonoIndex];
        poolFatMono[poolFatMonoIndex] = null;
        poolFatMonoIndex--;
        return tx;
    }


    /**
     * Takes a FatArrayGammaTxn from the pool.
     *
     * @return the taken FatArrayGammaTxn or null of none available.
     */
    public FatFixedLengthGammaTxn takeFatFixedLength() {
        if (!enabled || poolFatFixedLengthIndex == -1) {
            return null;
        }

        FatFixedLengthGammaTxn tx = poolFatFixedLength[poolFatFixedLengthIndex];
        poolFatFixedLength[poolFatFixedLengthIndex] = null;
        poolFatFixedLengthIndex--;
        return tx;
    }

    /**
     * Takes a FatMonoGammaTxn from the pool.
     *
     * @return the taken FatMonoGammaTxn or null of none available.
     */
    public LeanMonoGammaTxn takeLeanMono() {
        if (!enabled || poolLeanMonoIndex == -1) {
            return null;
        }

        LeanMonoGammaTxn tx = poolLeanMono[poolLeanMonoIndex];
        poolLeanMono[poolLeanMonoIndex] = null;
        poolLeanMonoIndex--;
        return tx;
    }


    /**
     * Takes a FatArrayGammaTxn from the pool.
     *
     * @return the taken FatArrayGammaTxn or null of none available.
     */
    public LeanFixedLengthGammaTxn takeLeanFixedLength() {
        if (!enabled || poolLeanFixedLengthIndex == -1) {
            return null;
        }

        LeanFixedLengthGammaTxn tx = poolLeanFixedLength[poolLeanFixedLengthIndex];
        poolLeanFixedLength[poolLeanFixedLengthIndex] = null;
        poolLeanFixedLengthIndex--;
        return tx;
    }


    /**
     * Takes a FatArrayTreeGammaTxn from the pool.
     *
     * @return the taken FatArrayTreeGammaTxn or null of none available.
     */
    public FatVariableLengthGammaTxn takeMap() {
        if (!enabled || poolFatVariableLengthIndex == -1) {
            return null;
        }

        FatVariableLengthGammaTxn tx = poolFatVariableLength[poolFatVariableLengthIndex];
        poolFatVariableLength[poolFatVariableLengthIndex] = null;
        poolFatVariableLengthIndex--;
        return tx;
    }

    /**
     * Puts a GammaTxn in the pool.
     *
     * @param tx the GammaTxn to put in the pool.
     * @throws NullPointerException if tx is null.
     */
    public void put(GammaTxn tx) {
        if (!enabled) {
            return;
        }

        final int type = tx.transactionType;

        if (type == TRANSACTIONTYPE_FAT_MONO) {
            if (poolFatMonoIndex == poolFatMono.length - 1) {
                return;
            }

            poolFatMonoIndex++;
            poolFatMono[poolFatMonoIndex] = (FatMonoGammaTxn) tx;
            return;
        }

        if (type == TRANSACTIONTYPE_FAT_FIXED_LENGTH) {
            if (poolFatFixedLengthIndex == poolFatFixedLength.length - 1) {
                return;
            }

            poolFatFixedLengthIndex++;
            poolFatFixedLength[poolFatFixedLengthIndex] = (FatFixedLengthGammaTxn) tx;
            return;
        }

        if (type == TRANSACTIONTYPE_LEAN_MONO) {
            if (poolLeanMonoIndex == poolLeanMono.length - 1) {
                return;
            }

            poolLeanMonoIndex++;
            poolLeanMono[poolLeanMonoIndex] = (LeanMonoGammaTxn) tx;
            return;
        }

        if (type == TRANSACTIONTYPE_LEAN_FIXED_LENGTH) {
            if (poolLeanFixedLengthIndex == poolLeanFixedLength.length - 1) {
                return;
            }

            poolLeanFixedLengthIndex++;
            poolLeanFixedLength[poolLeanFixedLengthIndex] = (LeanFixedLengthGammaTxn) tx;
            return;
        }


        if (type == TRANSACTIONTYPE_FAT_VARIABLE_LENGTH) {
            if (poolFatVariableLengthIndex == poolFatVariableLength.length - 1) {
                return;
            }

            poolFatVariableLengthIndex++;
            poolFatVariableLength[poolFatVariableLengthIndex] = (FatVariableLengthGammaTxn) tx;
            return;
        }

        throw new IllegalArgumentException();
    }
}
