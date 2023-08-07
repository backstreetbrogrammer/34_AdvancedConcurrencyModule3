# Advanced Concurrency - Module 3

> This is a tutorials course covering advanced concurrency in Java.

Tools used:

- JDK 11
- Maven
- JUnit 5, Mockito
- IntelliJ IDE

## Table of contents

1. [CAS operation and Atomic classes](https://github.com/backstreetbrogrammer/34_AdvancedConcurrencyModule3#chapter-01-cas-operation-and-atomic-classes)
2. [Concurrent Collections](https://github.com/backstreetbrogrammer/34_AdvancedConcurrencyModule3#chapter-02-concurrent-collections)

---

### Chapter 01. CAS operation and Atomic classes

**Compare and Swap (CAS)** is a technique used when designing concurrent algorithms and are very low level
functionalities given by the CPU that are exposed at the API level (Package: `java.util.concurrent.atomic`) so that we
can use them in our applications.

The approach is to compare the **actual** value of the variable to the **expected** value of the variable and if the
**actual** value matches the **expected** value, then **swap** the **actual** value of the variable for the **new**
value passed in.

**Working of the Algorithm**

It is like we know that this variable should be `1`, and we want to change it to `2`.

Since this is a multithreaded environment, we know that others might be working on the same variable.

So we should first check if the value of the variable is `1` as we thought and if yes, then we change it to `2`.

If we see that the variable is `3` now, then that means someone else is working on it and so let us not touch it at this
time.

#### Interview Problem 1 (UBS): Explain cost of synchronization in Java and best practices for lock acquisition

The problem in concurrent programming is the concurrent access to **shared** memory. We used **synchronization** to
handle that. But in certain cases, we have **more** tools.

Synchronization can cause us a lot of trouble when they are not used thoughtfully.

As a general rule, we should synchronize only on objects that we are sure no **outside** code will lock.

In other words, it's a bad practice to use **pooled** or **reusable** objects for synchronization.

The reason is that a pooled or reusable object is accessible to other processes in the JVM, and any **modification** to
such objects by outside or untrusted code can result in a **deadlock** and **non-deterministic** behavior.

**Examples**:

- **String literals**

String literals are pooled and often reused in Java. Therefore, it's not advised to use the `String` literal type with
the `synchronized` keyword for synchronization:

```
private final String stringLock = "MY_LOCK";
public void stringBadPractice() {
    synchronized (stringLock) {
        // ...
    }
}
```

The `String` object is the most used class in the Java language.

Thus, Java has a **String Pool** — the special memory region where Strings are stored by the JVM.

Because Strings are **immutable**, the JVM can optimize the amount of memory allocated for them by storing only one copy
of each literal String in the pool. This process is called **interning**.

When we create a String variable and assign a value to it, the JVM searches the pool for a String of equal value. If
found, the Java compiler will simply return a reference to its memory address, without allocating additional memory.

Note: All String literals and string-valued constant expressions are automatically interned.

Before Java 7, the JVM placed the **Java String Pool** in the **PermGen** space, which has a fixed size — it can't be
expanded at runtime and is **not** eligible for garbage collection.

The risk of interning Strings in the **PermGen** (instead of the **Heap**) is that we can get an `OutOfMemory` error
from the JVM if we intern too many Strings.

From Java 7 onwards, the **Java String Pool** is stored in the **Heap** space, which is garbage collected by the JVM.
The advantage of this approach is the reduced risk of `OutOfMemory` error because unreferenced Strings will be removed
from the pool, thereby releasing memory.

The **solution** is to use String **object** rather than String literal:

```
private final String stringLock = new String("MY_LOCK");
public void stringSolution() {
    synchronized (stringLock) {
        // ...
    }
}
```

- **Wrapper classes** or **Boxed Primitive**

Similar to the `String` literals, boxed types may reuse the instance for some values. JVM caches and shares the value
that can be represented as a `byte`.

```
private int count = 0;
private final Integer intLock = count; 
public void boxedPrimitiveBadPractice() { 
    synchronized (intLock) {
        count++;
        // ... 
    } 
}
```

The **solution** is to create a new instance **object**:

```
private int count = 0;
private final Integer intLock = new Integer(count);
public void boxedPrimitiveSolution() {
    synchronized (intLock) {
        count++;
        // ...
    }
}
```

- **Boolean literals**

The `Boolean` type with its two values, `true` and `false`, is unsuitable for locking purposes. Similar to `String`
literals in the JVM, `boolean` literal values also share the unique instances of the `Boolean` class.

```
private final Boolean booleanLock = Boolean.FALSE;
public void booleanBadPractice() {
    synchronized (booleanLock) {
        // ...
    }
}
```

System can become unresponsive or result in a deadlock situation if any outside code also synchronizes on a `Boolean`
literal with the same value.

- **Using Class instance**

JVM uses the object itself as a monitor (its intrinsic lock) when a class implements method synchronization or block
synchronization with the `this` keyword.

Untrusted code can obtain and indefinitely hold the intrinsic lock of an accessible class. Consequently, this can result
in a deadlock situation.

It's recommended to synchronize on a **private final** instance of the `Object` class. Such an object will be
inaccessible to outside untrusted code that may otherwise interact with our public classes, thus reducing the
possibility that such interactions could result in deadlock.

```
private final Object objLock = new Object();
public void finalObjectLockSolution() {
    synchronized (objLock) {
        // ...
    }
}
```

Additionally, if a method that implements the synchronized block modifies a `static` variable, we must synchronize by
locking on the `static` object:

```
private static int staticCount = 0;
private static final Object staticObjLock = new Object();
public void staticVariableSolution() {
    synchronized (staticObjLock) {
        count++;
        // ...
    }
}
```

**Understanding CAS**

Then a thread enters the critical section blocked by synchronization - at that runtime, there is NO real
**concurrency**. Because only 1 thread is running at that time.

This is where **CAS** can be used.

**Compare and Swap** works with three parameters:

- a location in memory
- an **existing** value at that location
- a **new** value to **replace** this **existing** value

If the **current value** at that address is the **expected value**, then it is replaced by the **new value** and returns
`true`.

If not, it returns `false`

This is all done in a **single, atomic** assembly instruction.

**Example with `AtomicInteger`**:

```
        // Create an atomic integer
        final AtomicInteger counter = new AtomicInteger(10);
        // Safely increment the value
        final int newValue = counter.incrementAndGet();
```

This is a safe incrementation of a counter without synchronization.

Under the hood, the Java API **tries** to **apply** the incrementation.

The CASing tells the calling code if the incrementation **failed**.

If it did, the API **tries again**.

#### Interview Problem 2 (Barclays): Demonstrate CAS operation without using Java Atomic API

Demonstrate the Compare-And-Swap (CAS) operation using simple locks.

**Solution**:

First, lets implement a simulated CAS using locks:

```java
public class CASUsingLocks {

    private int value;

    public synchronized int get() {
        return value;
    }

    public synchronized int compareAndSwap(final int expectedValue, final int newValue) {
        final int oldValue = value;
        if (oldValue == expectedValue) {
            value = newValue;
        }
        return oldValue;
    }

    public synchronized boolean compareAndSet(final int expectedValue, final int newValue) {
        return expectedValue == compareAndSwap(expectedValue, newValue);
    }
}
```

Now, let's use it for implementation of a thread-safe counter.

```java
public class CounterUsingCAS {

    private final CASUsingLocks value = new CASUsingLocks();

    public int get() {
        return value.get();
    }

    public void increment() {
        int v;

        do {
            v = value.get();
        } while (v != value.compareAndSwap(v, v + 1));

    }
}
```

Unit Test class:

```java
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
```

Output will always be correct:

```
Counter Value = 1000000
```

#### Java Atomic API

**AtomicBoolean**

- get(), set()
- getAndSet(value)
- compareAndSet(expected, value)

**AtomicInteger**, **AtomicLong**

- get(), set()
- getAndSet(value)
- compareAndSet(expected, value)
- getAndUpdate(unaryOp), updateAndGet(unaryOp)
- getAndIncrement(), getAndDecrement()
- getAndAdd(value), addAndGet(value)
- getAndAccumulate(value, binOp), accumulateAndGet(value, binOp)

**AtomicReference<V>**

- get(), set()
- getAndSet(value)
- getAndUpdate(unaryOp), updateAndGet(unaryOp)
- getAndAccumulate(value, binOp), accumulateAndGet(value, binOp)
- compareAndSet(expected, value)

**Summary**

- CASing works well when concurrency is not "too" high
- CASing: many tries until it is accepted…
- CASing is different from synchronization and can lead to better performances
- Synchronization: waiting threads until one can enter the synchronized block
- CASing may create load on the memory and / or CPU

#### Interview Problem 3 (Goldman Sachs): Implement thread-safe concurrent Stack using CAS

A stack is a linear data structure that follows the LIFO - **Last-In, First-Out** principle. That means the objects can
be inserted or removed only at one end of it, also called a **top**. Last object inserted will be the first object to
get.

![Stack](Stack.PNG)

Implement a thread-safe concurrent Stack using CAS.

Following 3 methods should be implemented from the `StackI` interface.

```java
public interface StackI<T> {

    T pop();

    void push(T item);

    T peek();

}
```

**SOLUTION**

Let's create a `StackNode` first to be used in the concurrent stack.

```java
public class StackNode<T> {

    private T data;
    private StackNode<T> next;

    public StackNode(final T data) {
        this.data = data;
    }

    public T getData() {
        return data;
    }

    public void setData(final T data) {
        this.data = data;
    }

    public StackNode<T> getNext() {
        return next;
    }

    public void setNext(final StackNode<T> next) {
        this.next = next;
    }
}
```

Final `ConcurrentStackUsingCAS` implementation:

```java
import java.util.concurrent.atomic.AtomicReference;

public class ConcurrentStackUsingCAS<T> implements StackI<T> {
    private final AtomicReference<StackNode<T>> top = new AtomicReference<>();

    @Override
    public T pop() {
        StackNode<T> oldHead;
        StackNode<T> newHead;
        do {
            oldHead = top.get();
            if (oldHead == null) {
                return null;
            }
            newHead = oldHead.getNext();
        } while (!top.compareAndSet(oldHead, newHead));
        return oldHead.getData();
    }

    @Override
    public void push(final T item) {
        final StackNode<T> newHead = new StackNode<>(item);
        StackNode<T> oldHead;
        do {
            oldHead = top.get();
            newHead.setNext(oldHead);
        } while (!top.compareAndSet(oldHead, newHead));
    }

    @Override
    public T peek() {
        if (top.get() == null) {
            return null;
        }
        return top.get().getData();
    }
}
```

**Unit Test**

```java
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
```

#### Adders and Accumulators added in Java 8

All the methods are built on the "modify and get" or "get and modify". Sometimes we do not need the "get" part at each
modification.

**Adders** and **Accumulators** are created to be very efficient in the multithreaded environment and both leverage very
clever tactics to be lock-free and still remain thread-safe.

They do not return the updated value - thus, it can distribute the update on different calls and merge the results on
a **get** call.

These are tailored for high concurrency. If there are less number of threads - it may not be that useful.

**Dynamic Striping**

All adder and accumulator implementations in Java are inheriting from base-class called `Striped64`.

Instead of using just one value to maintain the current state, this class uses an array of states to distribute the
contention to different memory locations.

![Dynamic Striping](DynamicStriping.PNG)

Different threads are updating different memory locations. Since we're using an array (that is, stripes) of states, this
idea is called dynamic striping.

We expect dynamic striping to improve the overall performance. However, the way the JVM allocates these states may have
a counterproductive effect.

To be more specific, the JVM may allocate those states near each other in the heap. This means that a few states can
reside in the same CPU cache line. Therefore, updating one memory location may cause a cache miss to its nearby states.
This phenomenon, known as false sharing, will hurt the performance.

To prevent false sharing. the Striped64 implementation adds enough padding around each state to make sure that each
state resides in its own cache line:

![Striping Padding](StripingPadded.PNG)

The `@Contended` annotation is responsible for adding this padding. The padding improves performance at the expense of
more memory consumption.

*LongAdder**

API

- increment(), decrement()
- add(long)
- sum(), longValue(), intValue()
- sumThenReset()

Let's consider some logic that's incrementing some values very often, where using an `AtomicLong` can be a bottleneck.
This uses a CAS operation, which – under heavy contention – can lead to a lot of wasted CPU cycles.

`LongAdder` uses a very clever trick to reduce contention between threads, when these are incrementing it.

When we want to increment an instance of the `LongAdder`, we need to call the `increment()` method. That implementation
keeps an array of counters that can grow on demand.

When more threads are calling `increment()`, the array will be longer. Each record in the array can be updated
separately – thus, reducing the contention.

Due to that fact, the `LongAdder` is a very efficient way to increment a counter from multiple threads.

```java
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
```

**LongAccumulator** - built on a binary operator

API

- accumulate(long)
- get()
- intValue(), longValue(), floatValue(), doubleValue()
- getThenReset()

`LongAccumulator` can be used to accumulate results according to the supplied `LongBinaryOperator` – this works
similarly to the `reduce()` operation from Stream API.

The instance of the `LongAccumulator` can be created by supplying the `LongBinaryOperator` and the initial value to its
constructor. The important thing to remember that LongAccumulator will work correctly if we supply it with a commutative
function where the **order of accumulation** does not matter.

```
final LongAccumulator accumulator = new LongAccumulator(Long::sum, 0L);
```

We're creating a `LongAccumulator` which will add a new value to the value that was already in the accumulator.

We are setting the initial value of the `LongAccumulator` to **zero**, so in the first call of the `accumulate()`
method, the `previousValue` will have a zero value.

Firstly, it executes an action defined as a `LongBinaryOperator`, and then it checks if the `previousValue` changed. If
it was changed, the action is executed again with the new value. If not, it succeeds in changing the value that is
stored in the accumulator.

```java
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
```

---

### Chapter 02. Concurrent Collections

---
