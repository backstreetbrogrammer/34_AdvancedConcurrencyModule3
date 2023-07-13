package com.backstreetbrogrammer.cas;

public class CASUsingLocks {

    private int value;

    public synchronized int get() {
        return value;
    }

    public synchronized int compareAndSwap(final int expectedValue, final int newValue) {
        final int oldValue = value;
        if (oldValue == expectedValue) {
            value = newValue;
        }
        return oldValue;
    }

    public synchronized boolean compareAndSet(final int expectedValue, final int newValue) {
        return expectedValue == compareAndSwap(expectedValue, newValue);
    }
}
