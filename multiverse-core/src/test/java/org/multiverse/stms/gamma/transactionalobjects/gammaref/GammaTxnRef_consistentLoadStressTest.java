package org.multiverse.stms.gamma.transactionalobjects.gammaref;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.TestThread;
import org.multiverse.stms.gamma.GammaConstants;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactionalobjects.GammaTxnRef;
import org.multiverse.stms.gamma.transactionalobjects.Tranlocal;
import org.multiverse.stms.gamma.transactions.GammaTxn;

import java.util.concurrent.atomic.AtomicLong;

import static org.junit.Assert.assertEquals;
import static org.multiverse.TestUtils.*;

public class GammaTxnRef_consistentLoadStressTest implements GammaConstants {

    private GammaStm stm;
    private volatile boolean stop;
    private GammaTxnRef ref;
    private final AtomicLong inconsistencyCount = new AtomicLong();

    @Before
    public void setUp() {
        stm = new GammaStm();
        stop = false;
        ref = new GammaTxnRef(stm, new Long(VERSION_UNCOMMITTED + 1));
    }

    @Test
    public void test() {
        int readThreadCount = 10;
        ReadThread[] readThreads = new ReadThread[readThreadCount];
        for (int k = 0; k < readThreads.length; k++) {
            readThreads[k] = new ReadThread(k);
        }

        int writerCount = 2;
        UpdateThread[] updateThreads = new UpdateThread[writerCount];
        for (int k = 0; k < updateThreads.length; k++) {
            updateThreads[k] = new UpdateThread(k);
        }

        startAll(readThreads);
        startAll(updateThreads);
        sleepMs(60 * 1000);
        stop = true;
        joinAll(readThreads);
        joinAll(writerCount);
        assertEquals(0, inconsistencyCount.get());
    }

    class ReadThread extends TestThread {
        public ReadThread(int id) {
            super("ReadThread-" + id);
        }

        @Override
        public void doRun() throws Exception {
            int k = 0;
            GammaTxn tx = stm.newDefaultTxn();
            Tranlocal tranlocal = new Tranlocal();
            while (!stop) {
                boolean success = ref.load(tx, tranlocal, LOCKMODE_NONE, 100, true);
                if (success) {
                    Long v = (Long) tranlocal.ref_value;
                    if (tranlocal.version != v.longValue()) {
                        inconsistencyCount.incrementAndGet();
                        System.out.println("Inconsistency detected");
                    }
                    if (tranlocal.hasDepartObligation) {
                        ref.departAfterReading();
                    }
                }

                k++;
                if (k % 100000 == 0) {
                    System.out.printf("%s is at %s\n", getName(), k);
                }
            }
        }
    }

    class UpdateThread extends TestThread {

        public UpdateThread(int id) {
            super("UpdateThread-" + id);
        }

        @Override
        public void doRun() throws Exception {
            int k = 0;
            while (!stop) {
                int arriveStatus = ref.arriveAndLock(1, LOCKMODE_EXCLUSIVE);
                if (arriveStatus == FAILURE) {
                    continue;
                }

                Long value = ref.version + 1;
                ref.ref_value = value;
                ref.version = value;
                ref.departAfterUpdateAndUnlock();

                k++;
                if (k % 100000 == 0) {
                    System.out.printf("%s is at %s\n", getName(), k);
                }
            }
        }
    }
}
