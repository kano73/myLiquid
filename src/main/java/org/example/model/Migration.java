package org.example.model;


import com.google.gson.Gson;

import java.util.List;

public class Migration {
    private String filename;
    private String author;
    private String description;
    private List<String> statements;

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getStatements() {
        return statements;
    }

    public void setStatements(List<String> statements) {
        this.statements = statements;
    }

    public String toJsonText() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }
}
