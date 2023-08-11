package com.backstreetbrogrammer.cas.poisonPill;

import java.util.concurrent.BlockingQueue;

public class Consumer implements Runnable {
    private final BlockingQueue<String> queue;
    private final String poisonPill;

    public Consumer(final BlockingQueue<String> queue, final String poisonPill) {
        this.queue = queue;
        this.poisonPill = poisonPill;
    }

    @Override
    public void run() {
        try {
            while (true) {
                final String item = queue.take();
                if (item.equals(poisonPill)) {
                    System.out.printf("[Consumer-%s] Got %s - Terminating Gracefully :) %n",
                                      Thread.currentThread().getName(), poisonPill);
                    return;
                }
                System.out.printf("[Consumer-%s] consumed: %s%n", Thread.currentThread().getName(), item);
            }
        } catch (final InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
    }
}
