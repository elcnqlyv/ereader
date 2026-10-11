package com.ereader.model;

public class Book {
    private String title;
    private String path;
    private int lastPosition;
    private long lastOpened;

    public Book() {
    }

    public Book(String title, String path) {
        this.title = title;
        this.path = path;
        this.lastPosition = 0;
        this.lastOpened = System.currentTimeMillis();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getLastPosition() {
        return lastPosition;
    }

    public void setLastPosition(int lastPosition) {
        this.lastPosition = lastPosition;
    }

    public long getLastOpened() {
        return lastOpened;
    }

    public void setLastOpened(long lastOpened) {
        this.lastOpened = lastOpened;
    }
}
