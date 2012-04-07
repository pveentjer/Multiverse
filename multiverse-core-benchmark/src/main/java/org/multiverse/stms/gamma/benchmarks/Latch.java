package org.multiverse.stms.gamma.benchmarks;

/**
 * A once reusable blocking structure. Other threads can wait for it while it is still closed, and once it
 * opens all waiting threads can continue.
 *
 * @author Peter Veentjer
 */
public final class Latch {

    private volatile boolean isOpen = false;

    public Latch() {
        this(false);
    }

    public Latch(boolean open) {
        isOpen = open;
    }

    public boolean isOpen() {
        return isOpen;
    }

    public void open() {
        if (isOpen) {
            return;
        }

        synchronized (this) {
            isOpen = true;
            notifyAll();
        }
    }

    public void await() {
        if (isOpen) {
            return;
        }

        synchronized (this) {
            while (!isOpen) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    @Override
    public String toString() {
        return "Latch{" +
                "isOpen=" + isOpen +
                '}';
    }
}
