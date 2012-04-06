package org.multiverse.commitbarriers;

import org.multiverse.TestThread;
import org.multiverse.api.Txn;
import org.multiverse.api.closures.AtomicVoidClosure;
import org.multiverse.stms.gamma.GammaStm;

import static org.junit.Assert.assertNotNull;

/**
 * @author Peter Veentjer
 */
public class JoinCommitThread extends TestThread {
    private final CountDownCommitBarrier barrier;
    private final GammaStm stm;

    public JoinCommitThread(GammaStm stm, CountDownCommitBarrier barrier) {
        this.barrier = barrier;
        this.stm = stm;
    }

    @Override
    public void doRun() throws Exception {
        stm.getDefaultTxnExecutor().atomic(new AtomicVoidClosure() {
            @Override
            public void execute(Txn tx) throws Exception {
                assertNotNull(tx);
                barrier.joinCommit(tx);
            }
        });
    }
}
