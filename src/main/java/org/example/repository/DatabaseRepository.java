package org.example.repository;

import com.zaxxer.hikari.HikariDataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.config.HikariCpConfig;
import org.example.model.Change;

import java.sql.*;
import java.util.ArrayList;

public class DatabaseRepository {

    private static final HikariDataSource dataSource;
    private static Connection connection;

    static {
        dataSource = HikariCpConfig.get();
    }

    private static final Logger logger = LogManager.getLogger(DatabaseRepository.class);

    public DatabaseRepository() {
        initTables();
    }

    public void initTables() {

        String createLogTable = """
            CREATE TABLE IF NOT EXISTS CHANGELOG (
                ID SERIAL PRIMARY KEY,
                AUTHOR VARCHAR(255),
                FILENAME VARCHAR(255) UNIQUE NOT NULL,
                MD5SUM VARCHAR(35) NOT NULL,
                DESCRIPTION VARCHAR(255) NOT NULL,
                EXECUTED_AT TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        """;

        String createStateTable = """
            CREATE TABLE IF NOT EXISTS LOCK (
                ID SERIAL PRIMARY KEY,
                LOCKED BOOLEAN DEFAULT FALSE
            )
        """;

        String insertOrIgnoreIntoLock = """
            INSERT INTO LOCK (ID, LOCKED)
            VALUES (1, FALSE)
            ON CONFLICT (ID) DO NOTHING
        """;

        try (Connection conn = dataSource.getConnection();
             Statement statement = conn.createStatement()) {
            conn.setAutoCommit(false);

            statement.execute(createLogTable);
            statement.execute(createStateTable);
            statement.execute(insertOrIgnoreIntoLock);

            conn.commit();
        } catch (SQLException e) {
            logger.error(e);
            throw new RuntimeException(e);
        }
    }

    public void openConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = dataSource.getConnection();
                connection.setAutoCommit(false);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to open database connection", e);
        }
    }

    public void commitAndClose() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.commit();
                connection.close();
                connection = null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to commit and close database connection", e);
        }
    }

    public void rollbackAndClose() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.rollback();
                connection.close();
                connection = null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to rollback and close database connection", e);
        }
    }

    public ArrayList<Change> getAllChanges() {
        String sql = """
         SELECT ID, AUTHOR, FILENAME, MD5SUM, DESCRIPTION, EXECUTED_AT
         FROM CHANGELOG;""";

        ArrayList<Change> changes = new ArrayList<>();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            changes = rowMapper(rs);
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch changes", e);
        }
        return changes;
    }

    public boolean isDataBaseIsAvailableForChanges() {
        String sql = "SELECT LOCKED FROM LOCK LIMIT 1;";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return !rs.getBoolean("LOCKED");
            } else {
                throw new RuntimeException("Unexpected behaviour: No LOCKED found in table LOCK");
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to check database availability", e);
        }
    }

    public boolean setLockedStatus(boolean locked) {
        String sql = "UPDATE LOCK SET LOCKED = ? WHERE ID = 1";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setBoolean(1, locked);
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) {
            throw new RuntimeException("Failed to set lock status", e);
        }
    }

    public boolean addChange(Change change) {
        String sql = """ 
             INSERT INTO CHANGELOG 
             (AUTHOR, FILENAME, MD5SUM, DESCRIPTION, EXECUTED_AT)
             VALUES (?, ?, ?, ?, ?)
             """;
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, change.getAuthor());
            pstmt.setString(2, change.getFilename());
            pstmt.setString(3, change.getMd5sum());
            pstmt.setString(4, change.getDescription());
            pstmt.setTimestamp(5, Timestamp.valueOf(change.getExecuted_at()));
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) {
            throw new RuntimeException("Failed to add change", e);
        }
    }

    private ArrayList<Change> rowMapper(ResultSet rs) throws SQLException {
        ArrayList<Change> changes = new ArrayList<>();
        while (rs.next()) {
            Change change = new Change();
            change.setId(rs.getInt("ID"));
            change.setAuthor(rs.getString("AUTHOR"));
            change.setFilename(rs.getString("FILENAME"));
            change.setMd5sum(rs.getString("MD5SUM"));
            change.setDescription(rs.getString("DESCRIPTION"));
            change.setExecuted_at(rs.getTimestamp("EXECUTED_AT").toLocalDateTime());
            changes.add(change);
        }
        return changes;
    }
}
