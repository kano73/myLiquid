package org.example.model;

import java.util.Objects;

public class BaseMigCha {
    private String author;
    private String filename;
    private String description;



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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        BaseMigCha that = (BaseMigCha) o;
        return Objects.equals(author, that.author) && Objects.equals(filename, that.filename) && Objects.equals(description, that.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(author, filename, description);
    }

    @Override
    public String toString() {
        return "BaseMigCha{" +
                "author='" + author + '\'' +
                ", filename='" + filename + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
