package com.backstreetbrogrammer.cas;

public class StudentBetter {

    private String name;
    private String course;

    // getters and constructors

    private final Object objLock1 = new Object();
    private final Object objLock2 = new Object();

    public synchronized void setName(final String name) {
        synchronized (objLock1) {
            this.name = name;
        }
    }

    public void setCourse(final String course) {
        synchronized (objLock2) {
            this.course = course;
        }
    }
}
