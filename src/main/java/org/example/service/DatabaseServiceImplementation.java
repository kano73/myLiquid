package org.example.service;

import org.example.interfaces.DatabaseService;
import org.example.model.Change;
import org.example.model.Migration;
import org.example.repository.DatabaseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DatabaseServiceImplementation implements DatabaseService {

    private static final Logger log = LoggerFactory.getLogger(DatabaseServiceImplementation.class);
    private final DatabaseRepository databaseRepository;

    public DatabaseServiceImplementation(DatabaseRepository databaseRepository) {
        this.databaseRepository = databaseRepository;
    }

    @Override
    public ArrayList<Change> getAllChanges(){
        try{
            databaseRepository.openConnection();
            return databaseRepository.getAllChanges();
        }catch(Exception e){
            databaseRepository.rollbackAndClose();
            throw new RuntimeException(e);
        }
        finally{
            databaseRepository.closeConnection();
        }
    }

    @Override
    public boolean executeAllMigrations(List<Migration> migrations) throws SQLException {
        try {
            if (!databaseRepository.acquireLock()) {
                log.warn("Unable to acquire lock, another user is changing it");
                throw new RuntimeException("Unable to acquire lock, another user is changing it");
            }

            databaseRepository.openConnection();
            try {
                List<Change> changesFromMig = migrations.stream()
                        .map(Migration::toChange)
                        .toList();

                for (Migration migration : migrations) {
                    try {
                        databaseRepository.executeMigration(migration);
                        changesFromMig.stream()
                                .filter(change -> Objects.equals(migration.getFilename(), change.getFilename()))
                                .forEach(databaseRepository::addChange);
                    } catch (SQLException e) {
                        log.error("Migration failed: " + migration.getFilename(), e);
                        databaseRepository.rollbackAndClose();
                        throw new RuntimeException("Failed to execute migration: " + migration.getFilename(), e);
                    }
                }

                databaseRepository.commit();
                return true;
            } catch (Exception e) {
                log.error("Error executing migrations", e);
                databaseRepository.rollbackAndClose();
                throw new RuntimeException(e);
            } finally {
                databaseRepository.closeConnection();
            }
        } finally {
            databaseRepository.releaseLock();
        }
    }

    private boolean addChange(Change change) {
        try {
            databaseRepository.addChange(change);
            return true;
        } catch (Exception e) {
            throw new RuntimeException("Error during addChange operation", e);
        }
    }

}
