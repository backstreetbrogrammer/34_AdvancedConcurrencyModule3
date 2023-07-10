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

#### Interview Problem 1 (UBS): Explain cost of synchronization in Java and if we have better alternatives

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

This is all done in a **single, atomic** assembly instruction



---

### Chapter 02. Concurrent Collections

---
