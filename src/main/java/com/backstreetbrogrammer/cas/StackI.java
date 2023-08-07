package com.backstreetbrogrammer.cas;

public interface StackI<T> {

    T pop();

    void push(T item);

    T peek();

}
