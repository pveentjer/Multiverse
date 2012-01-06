package org.multiverse.stms.gamma.benchmarks;

import org.benchy.BenchyUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.multiverse.MultiverseConstants;
import org.multiverse.stms.gamma.GammaStm;
import org.multiverse.stms.gamma.transactionalobjects.GammaLongRef;
import org.multiverse.utils.ToolUnsafe;
import sun.misc.Unsafe;

@Ignore
public class BasicPerformanceDriver {

    public static void main(String[] args) {
        BasicPerformanceDriver driver = new BasicPerformanceDriver();
        driver.casPerformance();
    }

    @Test
    public void test() {
        GammaStm stm = new GammaStm();
        GammaLongRef ref = new GammaLongRef(stm);

        final long transactionCount = 1000 * 1000 * 1000;

        final long startMs = System.currentTimeMillis();


        for (long k = 0; k < transactionCount; k++) {
            ref.arriveAndLock(1, MultiverseConstants.LOCKMODE_EXCLUSIVE);
            //ref.orec = 0;
            ref.departAfterUpdateAndUnlock();
        }

        long durationMs = System.currentTimeMillis() - startMs;

        String s = BenchyUtils.operationsPerSecondPerThreadAsString(transactionCount, durationMs, 1);

        System.out.printf("Performance is %s transactions/second/thread\n", s);
    }

    @Test
    public void casPerformance() {
        final long transactionCount = 1000 * 1000 * 1000;

        final long startMs = System.currentTimeMillis();

        Cas cas = new Cas();
        final long t = transactionCount / 10;
        for (long k = 0; k < t; k++) {
            cas.atomicInc();
            cas.atomicInc();
            cas.atomicInc();
            cas.atomicInc();
            cas.atomicInc();
            cas.atomicInc();
            cas.atomicInc();
            cas.atomicInc();
            cas.atomicInc();
            cas.atomicInc();
        }

        long durationMs = System.currentTimeMillis() - startMs;

        String s = BenchyUtils.operationsPerSecondPerThreadAsString(transactionCount, durationMs, 1);

        System.out.printf("Performance is %s transactions/second/thread\n", s);
    }

    @Test
    public void volatileWritePerformance() {
        final long transactionCount = 1000 * 1000 * 1000;

        final long startMs = System.currentTimeMillis();

        final Cas cas = new Cas();
        final long t = transactionCount / 10;
        for (long k = 0; k < t; k++) {
            cas.volatile_value++;
            cas.volatile_value++;
            cas.volatile_value++;
            cas.volatile_value++;
            cas.volatile_value++;
            cas.volatile_value++;
            cas.volatile_value++;
            cas.volatile_value++;
            cas.volatile_value++;
            cas.volatile_value++;
        }

        long durationMs = System.currentTimeMillis() - startMs;

        String s = BenchyUtils.operationsPerSecondPerThreadAsString(transactionCount, durationMs, 1);

        System.out.printf("Performance is %s transactions/second/thread\n", s);
    }

    @Test
    public void basicWritePerformance() {
        final long transactionCount = 1000 * 1000 * 1000;

        final long startMs = System.currentTimeMillis();

        final Cas cas = new Cas();
        for (long k = 0; k < transactionCount; k++) {
            cas.volatile_value++;//lock
            cas.volatile_value++;//version
            cas.volatile_value++;//value
            cas.volatile_value++;//unlock
        }

        long durationMs = System.currentTimeMillis() - startMs;

        String s = BenchyUtils.operationsPerSecondPerThreadAsString(transactionCount, durationMs, 1);

        System.out.printf("Performance is %s transactions/second/thread\n", s);
    }

    @Test
    public void basicWritePerformanceWithNonVolatileVersionAndValue() {
        final long transactionCount = 1000 * 1000 * 1000;

        final long startMs = System.currentTimeMillis();

        final Cas cas = new Cas();
        for (long k = 0; k < transactionCount; k++) {
            cas.volatile_value++;//lock
            cas.value++;//version
            cas.value++;//value
            cas.volatile_value++;//unlock
        }

        long durationMs = System.currentTimeMillis() - startMs;

        String s = BenchyUtils.operationsPerSecondPerThreadAsString(transactionCount, durationMs, 1);

        System.out.printf("Performance is %s transactions/second/thread\n", s);
    }


    class Cas {
        protected final Unsafe ___unsafe = ToolUnsafe.getUnsafe();
        protected final long valueOffset;

        {
            try {
                valueOffset = ___unsafe.objectFieldOffset(
                        Cas.class.getDeclaredField("volatile_value"));
            } catch (Exception ex) {
                throw new Error(ex);
            }
        }


        volatile long volatile_value;
        long value;

        void atomicInc() {
            final long oldValue = volatile_value;
            final long newValue = oldValue + 1;
            ___unsafe.compareAndSwapLong(this, valueOffset, oldValue, newValue);
        }
    }

}
