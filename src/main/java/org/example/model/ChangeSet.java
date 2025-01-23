package org.example.model;

import com.google.gson.Gson;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import java.util.Objects;

public class ChangeSet extends BaseMigCha{

    private List<String> statements;
    private List<String> rollBack;

    public List<String> getStatements() {
        return statements;
    }

    public void setStatements(List<String> statements) {
        this.statements = statements;
    }

    public List<String> getRollBack() {
        return rollBack;
    }

    public void setRollBack(List<String> rollBack) {
        this.rollBack = rollBack;
    }

    public String toJsonText() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    public Migration toMigration() {
        Migration migration = new Migration();

        migration.setFilename(getFilename());
        migration.setDescription(getDescription());
        migration.setAuthor(getAuthor());
        migration.setMd5sum(calculateMD5());

        return migration;
    }

    public String calculateMD5(){
        String jsonString = this.toJsonText();

        MessageDigest md;
        try{
            md = MessageDigest.getInstance("MD5");
        }catch (Exception e){
            throw new RuntimeException(e);
        }
        byte[] hashBytes = md.digest(jsonString.getBytes(StandardCharsets.UTF_8));

        StringBuilder hexString = new StringBuilder();
        for (byte b : hashBytes) {
            hexString.append(String.format("%02x", b));
        }

        return hexString.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ChangeSet changeSet = (ChangeSet) o;
        return Objects.equals(statements, changeSet.statements) && super.equals(o);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), statements);
    }

    @Override
    public String toString() {
        return "ChangeSet{" +
                super.toString() +
                " , statements=" + statements +
                '}';
    }
}
