package org.example.model;

import java.time.LocalDateTime;
import java.util.Objects;

public class Migration extends BaseMigCha{
    private Integer id;

    private String md5sum;
    private LocalDateTime executed_at;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }


    public String getMd5sum() {
        return md5sum;
    }

    public void setMd5sum(String md5sum) {
        this.md5sum = md5sum;
    }


    public LocalDateTime getExecuted_at() {
        return executed_at;
    }

    public void setExecuted_at(LocalDateTime executed_at) {
        this.executed_at = executed_at;
    }

    @Override
    public String toString() {
        return "Migration{" +
                "id=" + id +
                super.toString() +
                ", md5sum='" + md5sum + '\'' +
                ", executed_at=" + executed_at +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Migration migration = (Migration) o;
        return Objects.equals(id, migration.id) &&
                Objects.equals(md5sum, migration.md5sum) &&
                Objects.equals(executed_at, migration.executed_at) &&
                super.equals(o);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, md5sum, executed_at, super.hashCode());
    }
}
