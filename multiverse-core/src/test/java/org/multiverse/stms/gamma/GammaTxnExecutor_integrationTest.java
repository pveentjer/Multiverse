package org.multiverse.stms.gamma;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.TxnExecutor;
import org.multiverse.api.Txn;
import org.multiverse.api.closures.TxnLongClosure;
import org.multiverse.api.closures.TxnVoidClosure;
import org.multiverse.api.exceptions.TooManyRetriesException;
import org.multiverse.stms.gamma.transactionalobjects.GammaLongRef;
import org.multiverse.stms.gamma.transactions.fat.FatMonoGammaTxn;

import static org.junit.Assert.*;
import static org.multiverse.api.TxnThreadLocal.clearThreadLocalTxn;
import static org.multiverse.api.TxnThreadLocal.getThreadLocalTxn;

public class GammaTxnExecutor_integrationTest implements GammaConstants {

    private GammaStm stm;

    @Before
    public void setUp() {
        stm = new GammaStm();
        clearThreadLocalTxn();
    }


    @Test
    public void whenRead() {
        final GammaLongRef ref = new GammaLongRef(stm, 10);

        TxnExecutor block = stm.newTxnFactoryBuilder().newTxnExecutor();
        long result = block.atomic(new TxnLongClosure() {
            @Override
            public long execute(Txn tx) throws Exception {
                assertSame(tx, getThreadLocalTxn());
                return ref.get(tx);
            }
        });

        assertNull(getThreadLocalTxn());
        assertEquals(10, result);
    }

    @Test
    public void whenUpdate() {
        final GammaLongRef ref = new GammaLongRef(stm, 0);

        TxnExecutor block = stm.newTxnFactoryBuilder().newTxnExecutor();
        block.atomic(new TxnVoidClosure() {
            @Override
            public void execute(Txn tx) throws Exception {
                ref.incrementAndGet(tx, 1);
            }
        });

        assertEquals(1, ref.atomicGet());
    }

    @Test
    public void whenTooManyRetries() {
        final GammaLongRef ref = new GammaLongRef(stm);

        FatMonoGammaTxn otherTx = new FatMonoGammaTxn(stm);
        ref.openForWrite(otherTx, LOCKMODE_EXCLUSIVE);

        try {
            TxnExecutor block = stm.newTxnFactoryBuilder()
                    .setMaxRetries(100)
                    .newTxnExecutor();

            block.atomic(new TxnVoidClosure() {
                @Override
                public void execute(Txn tx) throws Exception {
                    ref.get(tx);
                }
            });

            fail();
        } catch (TooManyRetriesException expected) {
            expected.printStackTrace();
        }
    }

    @Test
    public void whenMultipleUpdatesDoneInSingleTransaction() {
        final GammaLongRef ref = new GammaLongRef(stm);

        TxnExecutor block = stm.newTxnFactoryBuilder()
                .setDirtyCheckEnabled(false)
                .newTxnExecutor();
        block.atomic(new TxnVoidClosure() {
            @Override
            public void execute(Txn tx) throws Exception {
                for (int k = 0; k < 10; k++) {
                    long l = ref.get();
                    ref.set(l + 1);
                }
            }
        });

        assertEquals(10, ref.atomicGet());
    }
}
