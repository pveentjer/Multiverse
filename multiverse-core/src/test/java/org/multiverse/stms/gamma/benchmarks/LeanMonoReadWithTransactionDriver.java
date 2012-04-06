package org.multiverse.stms.gamma.benchmarks;

import org.benchy.BenchyUtils;
import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.Txn;
import org.multiverse.api.closures.TxnVoidClosure;
import org.multiverse.stms.gamma.GammaConstants;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.LeanGammaTxnExecutor;
import org.multiverse.stms.gamma.transactionalobjects.GammaTxnRef;
import org.multiverse.stms.gamma.transactions.lean.LeanMonoGammaTxn;
import org.multiverse.stms.gamma.transactions.lean.LeanMonoGammaTxnFactory;

import static org.junit.Assert.assertEquals;

public class LeanMonoReadWithTransactionDriver implements GammaConstants {

    private GammaStm stm;

    @Before
    public void setUp() {
        stm = new GammaStm();
    }

    public static void main(String[] srgs) throws Throwable {
        LeanMonoReadWithTransactionDriver driver = new LeanMonoReadWithTransactionDriver();
        driver.setUp();
        try {
            driver.test();
        } catch (Throwable e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void test() {
        final long txCount = 1000 * 1000 * 1000;

        final GammaTxnRef<String> ref = new GammaTxnRef<String>(stm, null);
        long initialVersion = ref.getVersion();

        final LeanGammaTxnExecutor block = new LeanGammaTxnExecutor(new LeanMonoGammaTxnFactory(stm));

        final TxnVoidClosure closure = new TxnVoidClosure() {
            @Override
            public void execute(Txn tx) throws Exception {
                Object x = ref.openForRead((LeanMonoGammaTxn) tx, LOCKMODE_NONE).ref_value;
            }
        };

        System.out.println("Starting");
        long startMs = System.currentTimeMillis();

        for (long k = 0; k < txCount; k++) {
            block.atomic(closure);
        }

        long durationMs = System.currentTimeMillis() - startMs;
        System.out.println("finished");

        String s = BenchyUtils.operationsPerSecondPerThreadAsString(txCount, durationMs, 1);

        System.out.printf("Duration %s ms\n", durationMs);
        System.out.printf("Performance is %s transactions/second/thread\n", s);

        //assertEquals(txCount, ref.long_value);
        assertEquals(initialVersion, ref.version);
    }
}
