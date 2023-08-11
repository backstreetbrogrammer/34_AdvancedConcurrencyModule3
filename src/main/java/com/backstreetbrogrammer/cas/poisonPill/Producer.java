package com.backstreetbrogrammer.cas.poisonPill;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadLocalRandom;

public class Producer implements Runnable {
    private final BlockingQueue<String> queue;
    private final String poisonPill;
    private final int poisonPillPerProducer;

    public Producer(final BlockingQueue<String> queue, final String poisonPill,
                    final int poisonPillPerProducer) {
        this.queue = queue;
        this.poisonPill = poisonPill;
        this.poisonPillPerProducer = poisonPillPerProducer;
    }

    @Override
    public void run() {
        try {
            for (int i = 0; i < 10; i++) {
                queue.put(randomString());
            }
            for (int j = 0; j < poisonPillPerProducer; j++) {
                queue.put(poisonPill);
                System.out.printf("[Producer-%s] Putting %s in the queue%n",
                                  Thread.currentThread().getName(), poisonPill);
            }
        } catch (final InterruptedException ie) {
            ie.printStackTrace();
            Thread.currentThread().interrupt();
        }
    }

    private String randomString() {
        final int leftLimit = 48; // numeral '0'
        final int rightLimit = 122; // letter 'z'
        final int targetStringLength = 10;

        return ThreadLocalRandom.current().ints(leftLimit, rightLimit + 1)
                                .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
                                .limit(targetStringLength)
                                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                                .toString();
    }
}
