package org.example.repository;

import com.zaxxer.hikari.HikariDataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.config.HikariCpConfig;
import org.example.model.Change;
import org.example.model.Migration;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseRepository {

    private static final HikariDataSource dataSource;
    private static Connection connection;

    static {
        dataSource = HikariCpConfig.getHikariDataSource();
    }

    private static final Logger logger = LogManager.getLogger(DatabaseRepository.class);

    public DatabaseRepository() {
        initTables();
    }

    public boolean acquireLock() throws SQLException {
        String sql = "UPDATE LOCK SET LOCKED = TRUE WHERE ID = 1 AND LOCKED = FALSE";
        return setLockedStatus(sql, "Lock acquired successfully.", "Lock is already acquired by another process.");
    }

    public boolean releaseLock() throws SQLException {
        String sql = "UPDATE LOCK SET LOCKED = FALSE WHERE ID = 1";
        return setLockedStatus(sql, "Lock released successfully.", "Failed to release lock");
    }

    private boolean setLockedStatus(String sql, String successMessage, String errorMessage) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);

            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                int rowsUpdated = pstmt.executeUpdate();
                if (rowsUpdated > 0) {
                    connection.commit();
                    logger.info(successMessage);
                    return true;
                } else {
                    connection.rollback();
                    logger.warn(errorMessage);
                    throw new RuntimeException(errorMessage);
                }
            } catch (SQLException e) {
                connection.rollback();
                throw new RuntimeException("Database operation failed", e);
            }
        }
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

    public DatabaseRepository commit() {
        try {
            if (connection == null && connection.isClosed()) {
                throw new RuntimeException("Database connection is closed");
            }
            connection.commit();
            return this;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to commit ", e);
        }
    }

    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                connection = null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to close database connection", e);
        }
    }

    public void rollbackAndClose() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.rollback();
                connection.close();
                connection = null;
            }
            logger.info("Rollback and close database connection");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to rollback and close database connection", e);
        }
    }

    public ArrayList<Change> getAllChanges() {
        if(connection==null) {
            throw new RuntimeException("Database connection not open");
        }

        String sql = """
         SELECT ID, AUTHOR, FILENAME, MD5SUM, DESCRIPTION, EXECUTED_AT
         FROM CHANGELOG ORDER BY ID DESC ;""";

        ArrayList<Change> changes = new ArrayList<>();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            changes = rowMapper(rs);
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch changes", e);
        }
        return changes;
    }

    public boolean addChange(Change change) {
        if(connection==null) {
            throw new RuntimeException("Database connection not open");
        }

        String sql = """ 
             INSERT INTO CHANGELOG 
             (AUTHOR, FILENAME, MD5SUM, DESCRIPTION)
             VALUES (?, ?, ?, ?)
             """;
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, change.getAuthor());
            pstmt.setString(2, change.getFilename());
            pstmt.setString(3, change.getMd5sum());
            pstmt.setString(4, change.getDescription());

            return pstmt.executeUpdate() > 0;
        } catch (Exception e) {
            rollbackAndClose();
            logger.error("Failed to add change", e);
            throw new RuntimeException("Failed to add change", e);
        }
    }

    public void executeMigration(Migration migration) throws SQLException {
        if (connection == null || connection.isClosed()) {
            throw new RuntimeException("Database connection is not open");
        }

        List<String> sqls = migration.getStatements();

        try {
            connection.setAutoCommit(false);

            for (String sql : sqls) {
                try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                    pstmt.executeUpdate();
                    logger.info("Executed SQL: " + sql);
                } catch (SQLException e) {
                    connection.rollback();
                    logger.error("Failed to execute SQL: " + sql + migration.getFilename(), e);
                    throw new RuntimeException("Failed to execute migration", e);
                }
            }
            logger.info("Migration executed successfully: " + migration.getFilename());
        } catch (SQLException e) {
            connection.rollback();
            throw new RuntimeException("Migration execution failed. Rolled back transaction.", e);
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
