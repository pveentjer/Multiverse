package org.multiverse.api;

public class SpinningBackoffPolicy implements BackoffPolicy{

    @Override
    public void delay(int attempt) throws InterruptedException {

    }

    @Override
    public void delayUninterruptible(int attempt) {

    }
}
