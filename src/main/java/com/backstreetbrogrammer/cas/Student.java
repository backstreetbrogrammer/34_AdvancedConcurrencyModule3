package com.backstreetbrogrammer.cas;

public class Student {

    private String name;
    private String course;

    // getters and constructors

    public synchronized void setName(final String name) {
        this.name = name;
    }

    public void setCourse(final String course) {
        synchronized (this) {
            this.course = course;
        }
    }
}
