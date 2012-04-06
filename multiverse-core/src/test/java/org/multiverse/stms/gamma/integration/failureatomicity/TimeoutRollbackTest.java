package org.multiverse.stms.gamma.integration.failureatomicity;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.Txn;
import org.multiverse.api.TxnExecutor;
import org.multiverse.api.closures.AtomicVoidClosure;
import org.multiverse.api.exceptions.RetryTimeoutException;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactionalobjects.GammaLongRef;
import org.multiverse.stms.gamma.transactions.GammaTxn;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.multiverse.api.GlobalStmInstance.getGlobalStmInstance;
import static org.multiverse.api.StmUtils.retry;
import static org.multiverse.api.TxnThreadLocal.clearThreadLocalTxn;

public class TimeoutRollbackTest {
    private GammaLongRef modifyRef;
    private GammaLongRef awaitRef;
    private GammaStm stm;

    @Before
    public void setUp() {
        clearThreadLocalTxn();
        stm = (GammaStm) getGlobalStmInstance();
        modifyRef = new GammaLongRef(stm);
        awaitRef = new GammaLongRef(stm);
    }

    @Test
    public void test() {
        try {
            setAndTimeout();
            fail();
        } catch (RetryTimeoutException expected) {
        }

        assertEquals(0, modifyRef.atomicGet());
    }

    public void setAndTimeout() {
        TxnExecutor block = stm.newTransactionFactoryBuilder()
                .setTimeoutNs(TimeUnit.SECONDS.toNanos(1))
                .newTxnExecutor();

        block.atomic(new AtomicVoidClosure() {
            @Override
            public void execute(Txn tx) throws Exception {
                GammaTxn btx = (GammaTxn) tx;
                modifyRef.getAndSet(btx, 1);

                if (awaitRef.get(btx) != 1000) {
                    retry();
                }
            }
        });
    }
}
