package com.backstreetbrogrammer.cas.poisonPill;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadLocalRandom;

public class NumbersProducer implements Runnable {
    private final BlockingQueue<Integer> numbersQueue;
    private final int poisonPill;
    private final int poisonPillPerProducer;

    public NumbersProducer(final BlockingQueue<Integer> numbersQueue, final int poisonPill, final int poisonPillPerProducer) {
        this.numbersQueue = numbersQueue;
        this.poisonPill = poisonPill;
        this.poisonPillPerProducer = poisonPillPerProducer;
    }

    @Override
    public void run() {
        try {
            for (int i = 0; i < 100; i++) {
                numbersQueue.put(ThreadLocalRandom.current().nextInt(100));
            }
            for (int j = 0; j < poisonPillPerProducer; j++) {
                numbersQueue.put(poisonPill);
            }
        } catch (final InterruptedException ie) {
            ie.printStackTrace();
            Thread.currentThread().interrupt();
        }
    }
}
