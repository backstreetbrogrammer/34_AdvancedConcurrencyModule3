package com.backstreetbrogrammer.cas.poisonPill;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class PoisonPillDemo {

    public static void main(final String[] args) {
        final int BOUND = 10;
        final int N_PRODUCERS = 4;
        final int N_CONSUMERS = Runtime.getRuntime().availableProcessors();
        final String poisonPill = "POISON_PILL";
        final int poisonPillPerProducer = N_CONSUMERS / N_PRODUCERS;
        final int mod = N_CONSUMERS % N_PRODUCERS;

        final BlockingQueue<String> queue = new LinkedBlockingQueue<>(BOUND);

        System.out.printf(
                "No of producers:[%d], No of consumers:[%d], poison pill:[%s], poison pills per producer:[%d]%n",
                N_PRODUCERS, N_CONSUMERS, poisonPill, poisonPillPerProducer);
        System.out.println("-----------------------------");

        for (int i = 1; i < N_PRODUCERS; i++) {
            new Thread(new Producer(queue, poisonPill, poisonPillPerProducer)).start();
        }

        for (int j = 0; j < N_CONSUMERS; j++) {
            new Thread(new Consumer(queue, poisonPill)).start();
        }

        new Thread(new Producer(queue, poisonPill, poisonPillPerProducer + mod)).start();
    }

}
