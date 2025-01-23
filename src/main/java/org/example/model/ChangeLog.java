package org.example.model;

import java.util.List;
import java.util.Objects;

public class ChangeLog {
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
        ChangeLog that = (ChangeLog) o;
        return Objects.equals(changes, that.changes);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(changes);
    }

    @Override
    public String toString() {
        return "ChangeLog{" +
                "changes=" + changes.toString() +
                '}';
    }
}
