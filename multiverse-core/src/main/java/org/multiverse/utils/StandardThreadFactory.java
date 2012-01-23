package org.multiverse.utils;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A customizable implementation of the {@link java.util.concurrent.ThreadFactory}. The new
 * java.util.concurrency library provides a {@link java.util.concurrent.ThreadFactory} interface, which is a great
 * thing, but strangely enough it doesn't provide an customizable implementation.
 * <p/>
 * If the maximum priority of the ThreadGroup is changed after this StandardThreadFactory is
 * constructed, then this will be ignored by the StandardThreadFactory. So it could be that a
 * StandardThreadFactory has a higher priority than the ThreadGroup allowed. What will happen at
 * construction?
 *
 * @author Peter Veentjer.
 */
@SuppressWarnings({"ClassWithTooManyConstructors"})
public final class StandardThreadFactory implements ThreadFactory {

    private static final AtomicInteger poolNumber = new AtomicInteger(1);

    private static String createThreadGroupName() {
        return Integer.toString(poolNumber.getAndIncrement());
    }

    private final ThreadGroup threadGroup;
    private final AtomicInteger threadNumber = new AtomicInteger(1);
    private final String namePrefix;
    private final boolean daemon;
    private volatile int priority;

    /**
     * Constructs a new StandardThreadFactory with a Thread.NORM_PRIORITY as priority and a newly
     * created ThreadGroup. The created Threads are not daemons.
     */
    public StandardThreadFactory() {
        this(Thread.NORM_PRIORITY, createThreadGroupName());
    }

    /**
     * Constructs a new StandardThreadFactory with a Thread.NORM_PRIORITY as priority and with a
     * newly created ThreadGroup with the given groupName. The created threads are not daemons.
     *
     * @param groupName the name of the ThreadGroup (is allowed to be null).
     */
    public StandardThreadFactory(String groupName) {
        this(Thread.NORM_PRIORITY, groupName);
    }

    /**
     * Constructs a new StandardThreadFactory with the given priority. The created threads are
     * not daemons.
     *
     * @param priority the priority of th threads.
     * @throws IllegalArgumentException if the priority is not valid.
     */
    public StandardThreadFactory(int priority) {
        this(priority, createThreadGroupName());
    }

    /**
     * Constructs a new StandardThreadFactory with the given priority and with a newly created
     * ThreadGroup with the given groupName.  The created threads are not daemons.
     *
     * @param priority  the priority of the threads this StandardThreadFactory is going to createReference.
     * @param groupName the name of the ThreadGroup (is allowed to be null).
     * @throws IllegalArgumentException if priority is not a valid value.
     */
    public StandardThreadFactory(int priority, String groupName) {
        this(priority, new ThreadGroup(Thread.currentThread().getThreadGroup(), groupName), false);
    }

    /**
     * Constructs a new StandardThreadFactory with the given priority and are part of the give
     * ThreadGroup. The created threads are not daemons.
     *
     * @param priority    the priority of the created threads.
     * @param threadGroup the ThreadGroup the created Threads are part of.
     * @throws NullPointerException     if threadGroup is null
     * @throws IllegalArgumentException if the priority is not valid value.
     */
    public StandardThreadFactory(int priority, ThreadGroup threadGroup) {
        this(priority, threadGroup, false);
    }

    /**
     * Creates a new StandardThreadFactory with the given priority and if the threads are daemons
     *
     * @param priority the priority of the thread.
     * @param daemon   if the thread is a daemon.
     */
    public StandardThreadFactory(int priority, boolean daemon) {
        this(priority, new ThreadGroup(
                Thread.currentThread().getThreadGroup(),
                createThreadGroupName()),
                daemon);
    }

    /**
     * Constructs a new StandardThreadFactory with the given priority and ThreadGroup.
     *
     * @param priority    the priority of the threads this StandardThreadFactory is going to createReference.
     * @param threadGroup the ThreadGroup the thread is part of
     * @param daemon      if the thread should be a daemon.
     * @throws IllegalArgumentException if the priority is not valid.
     * @throws NullPointerException     if threadGroup is null.
     */
    public StandardThreadFactory(int priority, ThreadGroup threadGroup, boolean daemon) {
        if (threadGroup == null) {
            throw new NullPointerException();
        }

        this.threadGroup = threadGroup;
        ensureValidPriority(priority);
        this.priority = priority;

        this.daemon = daemon;
        this.namePrefix = threadGroup.getName() + "-thread#";
    }

    private void ensureValidPriority(int priority) {
        if (priority < Thread.MIN_PRIORITY) {
            throw new IllegalArgumentException("priority can`t be smaller than: " +
                    Thread.MIN_PRIORITY + ", priority was: " + priority);
        }

        if (priority > Thread.MAX_PRIORITY) {
            throw new IllegalArgumentException("priority can`t be greater than: " +
                    Thread.MAX_PRIORITY + ", priority was: " + priority);
        }

        if (priority > threadGroup.getMaxPriority()) {
            throw new IllegalArgumentException(
                    "priority can`t be greater than threadGroup.maxPriority: " +
                            threadGroup.getMaxPriority() + ", priority was: " + priority);
        }
    }

    /**
     * Returns true if this StandardThreadFactory is producing daemon threads, false
     * otherwise.
     *
     * @return true if this StandardThreadFactory is producing daemon threads, false
     *         otherwise.
     */
    public boolean isProducingDaemons() {
        return daemon;
    }

    /**
     * Returns the ThreadGroup of the created Threads.
     *
     * @return the ThreadGroup of the created Threads.
     */
    public ThreadGroup getThreadGroup() {
        return threadGroup;
    }

    /**
     * Returns the priority of created Threads. This is a value ranging from
     * Thread.MIN_PRIORITY to Thread.MAX_PRIORITY.
     *
     * @return the priority of created Threads.
     */
    public int getPriority() {
        return priority;
    }

    /**
     * Sets the priority of the threads. This will only effect newly created Threads. A value must
     * be getAndSet ranging from Thread.MIN_PRIORITY and Thread.MAX_PRIORITY.
     * <p/>
     * This call is not completely threadsafe, the following scenario could happen:
     * <ol>
     * <li>thread1 call setPriority and newTransaction the checking part of this method and the check passes</li>
     * <li>thread2 calls the ThreadGroup directly and lowers the priority</li>
     * <li>thread1 sets the priority on this StandardThreadFactory</li>
     * </ol>
     * The consequence is that the priority of this StandardThreadFactory is higher than the maximum
     * priority of the ThreadGroup and this means that thread creation could fail because threads are
     * created with a too high priority. This race problem is very hard to prevent because the check/getAndSet
     * can't be done atomic because the ThreadGroup is exposed.
     *
     * @param priority the new priority.
     * @throws IllegalArgumentException if priority is smaller than {@link Thread#MIN_PRIORITY} or
     *                                  larger than {@link Thread#MAX_PRIORITY} or larger than the
     *                                  maximum priority of the ThreadGroup.
     */
    public void setPriority(int priority) {
        ensureValidPriority(priority);
        this.priority = priority;
    }

    @Override
    public Thread newThread(Runnable runnable) {
        if (runnable == null) throw new NullPointerException();

        String threadName = namePrefix + threadNumber.getAndIncrement();
        Thread thread = new Thread(threadGroup, runnable, threadName);
        thread.setDaemon(daemon);
        thread.setPriority(priority);
        return thread;
    }
}
