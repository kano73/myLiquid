package org.example.interfaces;

import org.example.model.Change;
import org.example.model.Migration;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public interface DatabaseService {

    ArrayList<Change> getAllChanges();

    boolean executeAllMigrations(List<Migration> migrations) throws SQLException;
}
