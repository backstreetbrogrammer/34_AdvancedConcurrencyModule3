package com.backstreetbrogrammer.cas;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class CounterUsingCASTest {

    @Test
    @DisplayName("Demonstrate thread safe counter using CAS")
    void demonstrateThreadSafeCounterUsingCAS() throws InterruptedException {
        final CounterUsingCAS counter = new CounterUsingCAS();
        final Runnable r = () -> {
            for (int i = 0; i < 1_000; i++) {
                counter.increment();
            }
        };

        final Thread[] threads = new Thread[1_000];
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(r);
            threads[i].start();
        }

        for (int i = 0; i < threads.length; i++) {
            threads[i].join();
        }

        System.out.printf("Counter Value = %d%n", counter.get());
    }

}
