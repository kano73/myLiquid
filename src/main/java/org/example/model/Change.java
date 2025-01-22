package org.example.model;

import java.time.LocalDateTime;

public class Change {
    private Integer id;
    private String author;
    private String filename;
    private String md5sum;
    private String description;
    private LocalDateTime executed_at;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getMd5sum() {
        return md5sum;
    }

    public void setMd5sum(String md5sum) {
        this.md5sum = md5sum;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getExecuted_at() {
        return executed_at;
    }

    public void setExecuted_at(LocalDateTime executed_at) {
        this.executed_at = executed_at;
    }
}
