package com.backstreetbrogrammer.cas;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ConcurrentStackUsingCASTest {

    private final ConcurrentStackUsingCAS<String> myStack = new ConcurrentStackUsingCAS<>();

    @Test
    @DisplayName("Test ConcurrentStack CAS implementation using single thread")
    void testConcurrentStackUsingCAS_SingleThreaded() {
        myStack.push("A");
        myStack.push("B");
        myStack.push("C");
        myStack.push("D");

        assertEquals("D", myStack.peek());
        assertEquals("D", myStack.pop());
        assertEquals("C", myStack.pop());
        assertEquals("B", myStack.peek());

        myStack.push("E");

        assertEquals("E", myStack.pop());
        assertEquals("B", myStack.peek());
    }

    @Test
    @DisplayName("Test ConcurrentStack CAS implementation using multiple threads")
    void testConcurrentStackUsingCAS_MultiThreaded() throws InterruptedException {
        final var latch = new CountDownLatch(2);
        final Thread thread1 = new Thread(() -> {
            myStack.push("A");
            myStack.push("B");
            myStack.push("C");
            myStack.push("D");
            myStack.push("E");
            myStack.push("F");
            latch.countDown();
        });
        thread1.start();
        TimeUnit.SECONDS.sleep(1L);

        final Thread thread2 = new Thread(() -> {
            myStack.pop(); // F
            latch.countDown();
        });
        thread2.start();

        latch.await();

        assertEquals("E", myStack.peek());
        assertEquals("E", myStack.pop());

        myStack.push("G");
        assertEquals("G", myStack.pop());
        assertEquals("D", myStack.peek());
    }
}
