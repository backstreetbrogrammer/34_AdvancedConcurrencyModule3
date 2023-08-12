package com.backstreetbrogrammer.cas.concurrentMap;

public class CustomStripedMap<K, V> {

    private static final int N_LOCKS = 16;
    private final LinkedListNode<K, V>[] buckets;
    private final Object[] locks;

    // DoublyLinkedList node
    private static class LinkedListNode<K, V> {
        public LinkedListNode<K, V> next;
        public LinkedListNode<K, V> prev;
        public K key;
        public V value;

        public LinkedListNode(final K key, final V value) {
            this.key = key;
            this.value = value;
        }
    }

    public CustomStripedMap(final int capacity) {
        if (capacity < 1) {
            throw new IllegalArgumentException("capacity can not be less than 1");
        }
        buckets = new LinkedListNode[capacity];
        locks = new Object[N_LOCKS];
        for (int i = 0; i < N_LOCKS; i++) {
            locks[i] = new Object();
        }
    }

    private int hash(final K key) {
        return Math.abs(key.hashCode() % buckets.length);
    }

    public V get(final K key) {
        if (key == null) {
            throw new IllegalArgumentException("Key must not be null");
        }
        final int hash = hash(key);
        synchronized (locks[hash % N_LOCKS]) {
            for (final LinkedListNode<K, V> node : buckets) {
                if (node.key.equals(key)) {
                    return node.value;
                }
            }
        }
        return null;
    }

    public void clear() {
        for (int i = 0; i < buckets.length; i++) {
            synchronized (locks[i % N_LOCKS]) {
                buckets[i] = null;
            }
        }
    }
}
