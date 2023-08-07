package com.backstreetbrogrammer.cas;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAccumulator;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LongAccumulatorTest {

    @Test
    @DisplayName("Test LongAccumulator demo")
    void testLongAccumulator() throws InterruptedException {
        final LongAccumulator accumulator = new LongAccumulator(Long::sum, 0L);
        final ExecutorService executorService = Executors.newFixedThreadPool(8);

        final int numberOfThreads = 4;
        final int numberOfIncrements = 100;

        final Runnable accumulateAction = () -> IntStream
                .rangeClosed(0, numberOfIncrements)
                .forEach(accumulator::accumulate);

        for (int i = 0; i < numberOfThreads; i++) {
            executorService.execute(accumulateAction);
        }

        TimeUnit.SECONDS.sleep(1L);

        assertEquals(accumulator.get(), 20200);
        assertEquals(accumulator.longValue(), 20200);
    }
}
