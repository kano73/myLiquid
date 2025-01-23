package org.example.interfaces;

import org.example.model.Migration;
import org.example.model.ChangeSet;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public interface DatabaseService {

    ArrayList<Migration> getAllChanges();

    boolean executeAllMigrations(List<ChangeSet> changeSets) throws SQLException;
}
