package org.multiverse.stms.gamma.transactions;

import org.multiverse.stms.gamma.GammaConstants;
import org.multiverse.stms.gamma.transactions.fat.FatFixedLengthGammaTransaction;
import org.multiverse.stms.gamma.transactions.fat.FatMonoGammaTransaction;
import org.multiverse.stms.gamma.transactions.fat.FatVariableLengthGammaTransaction;
import org.multiverse.stms.gamma.transactions.lean.LeanFixedLengthGammaTransaction;
import org.multiverse.stms.gamma.transactions.lean.LeanMonoGammaTransaction;

/**
 * A pool for pooling GammaTransactions.
 *
 * @author Peter Veentjer.
 */
@SuppressWarnings({"ClassWithTooManyFields"})
public final class GammaTransactionPool implements GammaConstants {

    private final static boolean ENABLED = Boolean.parseBoolean(
            System.getProperty("org.multiverse.stm.gamma.transactions.GammaTransactionPool.enabled", "true"));

    private final boolean enabled;

    private final FatMonoGammaTransaction[] poolFatMono = new FatMonoGammaTransaction[10];
    private int poolFatMonoIndex = -1;
    private final FatFixedLengthGammaTransaction[] poolFatFixedLength = new FatFixedLengthGammaTransaction[10];
    private int poolFatFixedLengthIndex = -1;
    private final LeanMonoGammaTransaction[] poolLeanMono = new LeanMonoGammaTransaction[10];
    private int poolLeanMonoIndex = -1;
    private final LeanFixedLengthGammaTransaction[] poolLeanFixedLength = new LeanFixedLengthGammaTransaction[10];
    private int poolLeanFixedLengthIndex = -1;
    private final FatVariableLengthGammaTransaction[] poolFatVariableLength = new FatVariableLengthGammaTransaction[10];
    private int poolFatVariableLengthIndex = -1;

    public GammaTransactionPool() {
        enabled = ENABLED;
    }

    /**
     * Takes a FatMonoGammaTransaction from the pool.
     *
     * @return the taken FatMonoGammaTransaction or null of none available.
     */
    public FatMonoGammaTransaction takeFatMono() {
        if (!enabled || poolFatMonoIndex == -1) {
            return null;
        }

        FatMonoGammaTransaction tx = poolFatMono[poolFatMonoIndex];
        poolFatMono[poolFatMonoIndex] = null;
        poolFatMonoIndex--;
        return tx;
    }


    /**
     * Takes a FatArrayGammaTransaction from the pool.
     *
     * @return the taken FatArrayGammaTransaction or null of none available.
     */
    public FatFixedLengthGammaTransaction takeFatFixedLength() {
        if (!enabled || poolFatFixedLengthIndex == -1) {
            return null;
        }

        FatFixedLengthGammaTransaction tx = poolFatFixedLength[poolFatFixedLengthIndex];
        poolFatFixedLength[poolFatFixedLengthIndex] = null;
        poolFatFixedLengthIndex--;
        return tx;
    }

    /**
     * Takes a FatMonoGammaTransaction from the pool.
     *
     * @return the taken FatMonoGammaTransaction or null of none available.
     */
    public LeanMonoGammaTransaction takeLeanMono() {
        if (!enabled || poolLeanMonoIndex == -1) {
            return null;
        }

        LeanMonoGammaTransaction tx = poolLeanMono[poolLeanMonoIndex];
        poolLeanMono[poolLeanMonoIndex] = null;
        poolLeanMonoIndex--;
        return tx;
    }


    /**
     * Takes a FatArrayGammaTransaction from the pool.
     *
     * @return the taken FatArrayGammaTransaction or null of none available.
     */
    public LeanFixedLengthGammaTransaction takeLeanFixedLength() {
        if (!enabled || poolLeanFixedLengthIndex == -1) {
            return null;
        }

        LeanFixedLengthGammaTransaction tx = poolLeanFixedLength[poolLeanFixedLengthIndex];
        poolLeanFixedLength[poolLeanFixedLengthIndex] = null;
        poolLeanFixedLengthIndex--;
        return tx;
    }


    /**
     * Takes a FatArrayTreeGammaTransaction from the pool.
     *
     * @return the taken FatArrayTreeGammaTransaction or null of none available.
     */
    public FatVariableLengthGammaTransaction takeMap() {
        if (!enabled || poolFatVariableLengthIndex == -1) {
            return null;
        }

        FatVariableLengthGammaTransaction tx = poolFatVariableLength[poolFatVariableLengthIndex];
        poolFatVariableLength[poolFatVariableLengthIndex] = null;
        poolFatVariableLengthIndex--;
        return tx;
    }

    /**
     * Puts a GammaTransaction in the pool.
     *
     * @param tx the GammaTransaction to put in the pool.
     * @throws NullPointerException if tx is null.
     */
    public void put(GammaTransaction tx) {
        if (!enabled) {
            return;
        }

        final int type = tx.transactionType;

        if (type == TRANSACTIONTYPE_FAT_MONO) {
            if (poolFatMonoIndex == poolFatMono.length - 1) {
                return;
            }

            poolFatMonoIndex++;
            poolFatMono[poolFatMonoIndex] = (FatMonoGammaTransaction) tx;
            return;
        }

        if (type == TRANSACTIONTYPE_FAT_FIXED_LENGTH) {
            if (poolFatFixedLengthIndex == poolFatFixedLength.length - 1) {
                return;
            }

            poolFatFixedLengthIndex++;
            poolFatFixedLength[poolFatFixedLengthIndex] = (FatFixedLengthGammaTransaction) tx;
            return;
        }

        if (type == TRANSACTIONTYPE_LEAN_MONO) {
            if (poolLeanMonoIndex == poolLeanMono.length - 1) {
                return;
            }

            poolLeanMonoIndex++;
            poolLeanMono[poolLeanMonoIndex] = (LeanMonoGammaTransaction) tx;
            return;
        }

        if (type == TRANSACTIONTYPE_LEAN_FIXED_LENGTH) {
            if (poolLeanFixedLengthIndex == poolLeanFixedLength.length - 1) {
                return;
            }

            poolLeanFixedLengthIndex++;
            poolLeanFixedLength[poolLeanFixedLengthIndex] = (LeanFixedLengthGammaTransaction) tx;
            return;
        }


        if (type == TRANSACTIONTYPE_FAT_VARIABLE_LENGTH) {
            if (poolFatVariableLengthIndex == poolFatVariableLength.length - 1) {
                return;
            }

            poolFatVariableLengthIndex++;
            poolFatVariableLength[poolFatVariableLengthIndex] = (FatVariableLengthGammaTransaction) tx;
            return;
        }

        throw new IllegalArgumentException();
    }
}
