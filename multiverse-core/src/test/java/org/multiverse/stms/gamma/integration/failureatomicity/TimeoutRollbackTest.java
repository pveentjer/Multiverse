package org.multiverse.stms.gamma.integration.failureatomicity;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.Txn;
import org.multiverse.api.TxnExecutor;
import org.multiverse.api.callables.TxnVoidCallable;
import org.multiverse.api.exceptions.RetryTimeoutException;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactionalobjects.GammaTxnLong;
import org.multiverse.stms.gamma.transactions.GammaTxn;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.multiverse.api.GlobalStmInstance.getGlobalStmInstance;
import static org.multiverse.api.StmUtils.retry;
import static org.multiverse.api.TxnThreadLocal.clearThreadLocalTxn;

public class TimeoutRollbackTest {
    private GammaTxnLong modifyRef;
    private GammaTxnLong awaitRef;
    private GammaStm stm;

    @Before
    public void setUp() {
        clearThreadLocalTxn();
        stm = (GammaStm) getGlobalStmInstance();
        modifyRef = new GammaTxnLong(stm);
        awaitRef = new GammaTxnLong(stm);
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
        TxnExecutor executor = stm.newTxnFactoryBuilder()
                .setTimeoutNs(TimeUnit.SECONDS.toNanos(1))
                .newTxnExecutor();

        executor.execute(new TxnVoidCallable() {
            @Override
            public void call(Txn tx) throws Exception {
                GammaTxn btx = (GammaTxn) tx;
                modifyRef.getAndSet(btx, 1);

                if (awaitRef.get(btx) != 1000) {
                    retry();
                }
            }
        });
    }
}
