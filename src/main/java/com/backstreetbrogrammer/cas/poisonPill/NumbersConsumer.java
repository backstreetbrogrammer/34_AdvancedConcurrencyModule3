package com.backstreetbrogrammer.cas.poisonPill;

import java.util.concurrent.BlockingQueue;

public class NumbersConsumer implements Runnable {
    private final BlockingQueue<Integer> queue;
    private final int poisonPill;

    public NumbersConsumer(final BlockingQueue<Integer> queue, final int poisonPill) {
        this.queue = queue;
        this.poisonPill = poisonPill;
    }

    @Override
    public void run() {
        try {
            while (true) {
                final Integer number = queue.take();
                if (number.equals(poisonPill)) {
                    return;
                }
                System.out.printf("%s result: %d%n", Thread.currentThread().getName(), number);
            }
        } catch (final InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
    }
}
