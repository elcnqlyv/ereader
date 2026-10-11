package com.ereader.model;

public class Bookmark {
    private String bookPath;
    private String title;
    private int page;
    private long createdAt;

    public Bookmark() {
    }

    public Bookmark(String bookPath, String title, int page) {
        this.bookPath = bookPath;
        this.title = title;
        this.page = page;
        this.createdAt = System.currentTimeMillis();
    }

    public String getBookPath() {
        return bookPath;
    }

    public void setBookPath(String bookPath) {
        this.bookPath = bookPath;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}
