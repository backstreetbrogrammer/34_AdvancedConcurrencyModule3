package com.backstreetbrogrammer.cas;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LongAdderTest {

    @Test
    @DisplayName("Test LongAdder demo")
    void testLongAdder() throws InterruptedException {
        final LongAdder counter = new LongAdder();
        final ExecutorService executorService = Executors.newFixedThreadPool(8);

        final int numberOfThreads = 4;
        final int numberOfIncrements = 100;

        final Runnable incrementAction = () -> IntStream
                .range(0, numberOfIncrements)
                .forEach(i -> counter.increment());

        for (int i = 0; i < numberOfThreads; i++) {
            executorService.execute(incrementAction);
        }

        TimeUnit.SECONDS.sleep(1L);

        assertEquals(counter.sum(), numberOfIncrements * numberOfThreads);
        assertEquals(counter.longValue(), numberOfIncrements * numberOfThreads);

        assertEquals(counter.sumThenReset(), numberOfIncrements * numberOfThreads);
        assertEquals(counter.sum(), 0);
    }
}
