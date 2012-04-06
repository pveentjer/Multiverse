package org.multiverse;

import org.multiverse.api.Transaction;
import org.multiverse.api.TxnStatus;
import org.multiverse.api.blocking.RetryLatch;
import org.multiverse.stms.gamma.GammaConstants;
import org.multiverse.stms.gamma.transactionalobjects.AbstractGammaObject;
import org.multiverse.utils.Bugshaker;
import org.multiverse.utils.ThreadLocalRandom;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static java.lang.String.format;
import static org.junit.Assert.*;

/**
 * @author Peter Veentjer
 */
public class TestUtils implements MultiverseConstants {

    public static void assertOrecValue(AbstractGammaObject object, long expected) {
        assertEquals(expected, object.orec);
    }

    public static void assertFailure(int value) {
        assertEquals(GammaConstants.FAILURE, value);
    }

    public static void assertHasMasks(int value, int... masks) {
        for (int mask : masks) {
            assertTrue((value & mask) > 0);
        }
    }

    public static void assertNotHasMasks(int value, int... masks) {
        for (int mask : masks) {
            assertTrue((value & mask) == 0);
        }
    }

    public static void clearCurrentThreadInterruptedStatus() {
        Thread.interrupted();
    }

    public static void assertEqualsDouble(String msg, double expected, double found) {
        assertEquals(msg, Double.doubleToLongBits(expected), Double.doubleToLongBits(found));
    }

    public static void assertEqualsDouble(double expected, double found) {
        assertEqualsDouble(format("expected %s found %s", expected, found), expected, found);
    }

    public static int processorCount() {
        return Runtime.getRuntime().availableProcessors();
    }

    public static void assertEra(RetryLatch latch, long era) {
        assertEquals(era, latch.getEra());
    }

    public static void assertOpen(RetryLatch latch) {
        assertTrue(latch.isOpen());
    }

    public static void assertClosed(RetryLatch latch) {
        assertFalse(latch.isOpen());
    }

    public static void assertEqualByteArray(byte[] array1, byte[] array2) {
        if (array1 == array2) {
            return;
        }

        if (array1 == null) {
            fail();
        }

        int length = array1.length;
        assertEquals(length, array2.length);
        for (int k = 0; k < array1.length; k++) {
            assertEquals(array1[k], array2[k]);
        }
    }

    public static Object getField(Object o, String fieldname) {
        if (o == null || fieldname == null) {
            throw new NullPointerException();
        }

        try {
            Field field = findField(o.getClass(), fieldname);
            if (field == null) {
                fail(format("field '%s' is not found on class '%s' or on one of its super classes", fieldname, o.getClass().getName()));
            }
            field.setAccessible(true);
            return field.get(o);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static Field findField(Class clazz, String fieldname) {
        try {
            return clazz.getDeclaredField(fieldname);
        } catch (NoSuchFieldException e) {
            if (clazz.equals(Object.class)) {
                return null;
            }

            return findField(clazz.getSuperclass(), fieldname);
        }
    }

    public static void assertNotEquals(long l1, long l2) {
        assertFalse(format("both values are %s, but should not be equal", l2), l1 == l2);
    }

    public static void assertIsPrepared(Transaction... transactions) {
        for (Transaction tx : transactions) {
            assertEquals(TxnStatus.Prepared, tx.getStatus());
        }
    }

    public static void assertIsAborted(Transaction... transactions) {
        for (Transaction tx : transactions) {
            assertEquals(TxnStatus.Aborted, tx.getStatus());
        }
    }

    public static void assertIsCommitted(Transaction... transactions) {
        for (Transaction tx : transactions) {
            assertEquals(TxnStatus.Committed, tx.getStatus());
        }
    }

    public static void assertIsActive(Transaction... transactions) {
        for (Transaction tx : transactions) {
            assertEquals(TxnStatus.Active, tx.getStatus());
        }
    }

    public static int randomInt(int max) {
        if (max <= 0) {
            return 0;
        }

        return ThreadLocalRandom.current().nextInt(max);
    }

    public static void sleepRandomMs(int maxMs) {
        Bugshaker.sleepUs((long) randomInt((int) TimeUnit.MILLISECONDS.toMicros(maxMs)));
    }

    public static void sleepMs(long ms) {
        long us = TimeUnit.MILLISECONDS.toMicros(ms);
        Bugshaker.sleepUs(us);
    }

    public static boolean randomBoolean() {
        return randomInt(10) % 2 == 0;
    }

    public static boolean randomOneOf(int chance) {
        return randomInt(Integer.MAX_VALUE) % chance == 0;
    }

    public static long getStressTestDurationMs(long defaultDuration) {
        String value = System.getProperty("org.multiverse.integrationtest.durationMs", String.valueOf(defaultDuration));
        return Long.parseLong(value);
    }

    public static void assertIsInterrupted(Thread t) {
        assertTrue(t.isInterrupted());
    }

    public static void assertAlive(Thread... threads) {
        for (Thread thread : threads) {
            assertTrue(thread.getName(), thread.isAlive());
        }
    }

    public static boolean isAlive(Thread... threads) {
        for (Thread thread : threads) {
            if (!thread.isAlive()) {
                return false;
            }
        }

        return true;
    }

    public static void assertNothingThrown(TestThread... threads){
        for(TestThread t: threads){
            Throwable throwable = t.getThrowable();
            if(throwable != null){
                fail(String.format("TestThread [%s] failed with the following exception\n%s",
                        t.getName(),getStackTrace(throwable)));

            }
        }
    }
    
    public static String getStackTrace(Throwable aThrowable) {
        final Writer result = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(result);
        aThrowable.printStackTrace(printWriter);
        return result.toString();
      }
    
    public static void assertNotAlive(Thread... threads) {
        for (Thread thread : threads) {
            assertFalse(thread.isAlive());
        }
    }

    public static void assertEventuallyNotAlive(Thread... threads){
       assertEventuallyNotAlive(60 * 1000, threads);
    }
    
    public static void assertEventuallyNotAlive(long timeoutMs, Thread... threads) {
        for (Thread thread : threads) {
            if(timeoutMs <=0){
                fail("There is no remaining timeout");
            }

            long startMs = System.currentTimeMillis();
            try {
                thread.join(timeoutMs);
            } catch (InterruptedException e) {
                fail("Failed to join thread: " + thread.getName());
            }
            long elapsed = System.currentTimeMillis() - startMs;

            if (thread.isAlive()) {
                fail(format("Thread [%s] is still alive after a timeout of [%s] ms", thread, timeoutMs));
            }

            assertFalse(thread.isAlive());
            timeoutMs -= elapsed;
        }
    }

    public static void startAll(TestThread... threads) {
        for (Thread thread : threads) {
            thread.start();
        }
    }

    public static void assertEventuallyFalse(Callable<Boolean> f) {
        assertEventually(f, false);
    }

    public static void assertEventuallyFalse(Callable<Boolean> f, long timeoutMs) {
        assertEventually(f, false, timeoutMs);
    }

    public static void assertEventually(Callable<Boolean> f, boolean value) {
        assertEventually(f, value, 60 * 1000);
    }

    public static void assertEventuallyTrue(Callable<Boolean> f) {
        assertEventually(f, true);
    }

    public static void assertEventuallyTrue(Callable<Boolean> f, long timeoutMs) {
        assertEventually(f, true, timeoutMs);
    }

    public static void assertEventually(Callable<Boolean> f, boolean value, long timeoutMs) {
        long endTime = System.currentTimeMillis() + timeoutMs;
        for (; ; ) {
            try {
                if (f.call() == value) {
                    return;
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            sleepMs(100);

            if (endTime > System.currentTimeMillis()) {
                fail("Failed to become true in the given timeout");
            }
        }
    }


    public static void sleepRandomUs(int maxUs) {
        Bugshaker.sleepUs((long) randomInt(maxUs));
    }

    public static void assertInstanceof(Class expected, Object o) {
        assertTrue(o.getClass().getName(), expected.isAssignableFrom(o.getClass()));
    }

    /**
     * Joins all threads. If this can't be done within 5 minutes, an assertion failure is thrown.
     *
     * @param threads the threads to join.
     * @return the total duration of all threads (so the sum of the time each thread has been running.
     * @see #joinAll(long, TestThread...) for more specifics.
     */
    public static long joinAll(TestThread... threads) {
        return joinAll(5 * 60, threads);
    }

    /**
     * Joins all threads. If one of the thread throws a throwable, the join will fail as well.
     *
     * @param timeoutSec the timeout in seconds. If the join doesn't complete within that time, the
     *                   join fails.
     * @param threads    the threads to join.
     * @return the total duration of all threads (so the sum of the time each thread has been running.
     */
    @SuppressWarnings({"ThrowableResultOfMethodCallIgnored"})
    public static long joinAll(long timeoutSec, TestThread... threads) {
        if (timeoutSec < 0) {
            throw new IllegalArgumentException();
        }

        List<TestThread> uncompleted = new LinkedList<TestThread>(Arrays.asList(threads));

        long maxTimeMs = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(timeoutSec);

        long durationMs = 0;

        while (!uncompleted.isEmpty()) {
            for (Iterator<TestThread> it = uncompleted.iterator(); it.hasNext(); ) {
                TestThread thread = it.next();
                try {
                    if (System.currentTimeMillis() > maxTimeMs) {
                        fail(String.format(
                                "Failed to join all threads in %s seconds, remaining threads %s.\n%s",
                                timeoutSec, uncompleted, getStacks(uncompleted)));
                    }
                    thread.join(100);

                    if (!thread.isAlive()) {
                        it.remove();
                        durationMs += thread.getDurationMs();

                        if (thread.getThrowable() == null) {
                            System.out.printf("Multiverse > %s completed successfully\n", thread.getName());
                        } else {
                            System.out.printf("Multiverse > %s encountered the following error\n", thread.getName());
                            thread.getThrowable().printStackTrace();
                            fail(String.format("Multiverse > %s completed with failure", thread.getName()));
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(String.format("Joining %s was interrupted", thread), e);
                }
            }
        }

        return durationMs;
    }

    private static String getStacks(List<TestThread> uncompleted) {
        StringBuffer sb = new StringBuffer();
        sb.append("Uncompleted threads:\n");
        for (TestThread thread : uncompleted) {
            sb.append("-------------------------------------------------------------------\n");
            sb.append(thread.getName() + "\n");
            for (StackTraceElement element : thread.getStackTrace()) {
                sb.append("\tat " + element + "\n");
            }
        }
        sb.append("-------------------------------------------------------------------\n");
        return sb.toString();
    }
}
