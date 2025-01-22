package org.example.model;

import java.util.List;
import java.util.Objects;

public class MasterChangeLog {
    private List<String> changes;

    public List<String> getChanges() {
        return changes;
    }

    public void setChanges(List<String> changes) {
        this.changes = changes;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        MasterChangeLog that = (MasterChangeLog) o;
        return Objects.equals(changes, that.changes);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(changes);
    }

    @Override
    public String toString() {
        return "MasterChangeLog{" +
                "changes=" + changes.toString() +
                '}';
    }
}
