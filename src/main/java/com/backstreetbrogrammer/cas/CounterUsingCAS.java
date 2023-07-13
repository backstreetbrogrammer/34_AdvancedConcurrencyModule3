package com.backstreetbrogrammer.cas;

public class CounterUsingCAS {

    private final CASUsingLocks value = new CASUsingLocks();

    public int get() {
        return value.get();
    }

    public void increment() {
        int v;

        do {
            v = value.get();
        } while (v != value.compareAndSwap(v, v + 1));

    }
}
