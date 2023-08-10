package com.backstreetbrogrammer.cas;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class BlockingQueue<E> {
    private final List<E> queue = new LinkedList<>();
    private final int limit;

    private static final Lock lock = new ReentrantLock();
    private static final Condition notFull = lock.newCondition();
    private static final Condition notEmpty = lock.newCondition();

    public BlockingQueue(final int limit) {
        if (limit < 1) {
            throw new IllegalArgumentException("capacity can not be less than 1");
        }
        this.limit = limit;
    }

    public void enqueue(final E item) {
        try {
            lock.lock();
            while (isFull()) {
                if (!notFull.await(10, TimeUnit.MILLISECONDS)) {
                    throw new TimeoutException("Producer time out!");
                }
            }
            this.queue.add(item);
            notEmpty.signalAll();
        } catch (final InterruptedException | TimeoutException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }

    public E dequeue() {
        try {
            lock.lock();
            while (isEmpty()) {
                if (!notEmpty.await(10, TimeUnit.MILLISECONDS)) {
                    throw new TimeoutException("Consumer time out!");
                }
            }
            final E item = this.queue.remove(0);
            notFull.signalAll();
            return item;
        } catch (final InterruptedException | TimeoutException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
        return null;
    }

    private boolean isEmpty() {
        return this.queue.size() == 0;
    }

    private boolean isFull() {
        return this.queue.size() == this.limit;
    }

}
