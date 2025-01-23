package org.example.service;

import org.example.interfaces.DatabaseService;
import org.example.model.Migration;
import org.example.model.ChangeSet;
import org.example.repository.DatabaseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class DatabaseServiceImplementation implements DatabaseService {

    private static final Logger log = LoggerFactory.getLogger(DatabaseServiceImplementation.class);
    private final DatabaseRepository databaseRepository;

    public DatabaseServiceImplementation(DatabaseRepository databaseRepository) {
        this.databaseRepository = databaseRepository;
    }

    @Override
    public ArrayList<Migration> getAllChanges(){
        try{
            databaseRepository.openConnection();
            return databaseRepository.getAllMigrations();
        }catch(Exception e){
            databaseRepository.rollbackAndClose();
            throw new RuntimeException(e);
        }
        finally{
            databaseRepository.closeConnection();
        }
    }

    @Override
    public boolean executeAllChangeSets(List<ChangeSet> changeSets) throws SQLException {
        return executeChangeSets(changeSets, false);
    }

    public boolean rollBackChangeSets(List<ChangeSet> executedTillVersion) throws SQLException {
        return executeChangeSets(executedTillVersion, true);
    }

    private boolean executeChangeSets(List<ChangeSet> changeSets, boolean isRollback) throws SQLException {
        try {
            if (!databaseRepository.acquireLock()) {
                log.warn("Unable to acquire lock, another user is changing it");
                throw new RuntimeException("Unable to acquire lock, another user is changing it");
            }

            databaseRepository.openConnection();

            List<Migration> changesFromMig = changeSets.stream()
                    .map(ChangeSet::toMigration)
                    .toList();

            changeSets.forEach(changeSet -> {
                try {
                    databaseRepository.executeChangeSet(changeSet, isRollback);

                    changesFromMig.stream()
                            .filter(change -> Objects.equals(changeSet.getFilename(), change.getFilename()))
                            .forEach(migration -> {
                                if (isRollback) {
                                    databaseRepository.deleteMigration(migration);
                                } else {
                                    databaseRepository.addMigration(migration);
                                }
                            });

                } catch (SQLException e) {
                    log.error("Migration failed: " + changeSet.getFilename(), e);
                    databaseRepository.rollbackAndClose();
                    throw new RuntimeException("Failed to execute migration: " + changeSet.getFilename(), e);
                }
            });

            databaseRepository.commit();
            return true;

        } catch (Exception e) {
            log.error("Error executing migrations", e);
            databaseRepository.rollbackAndClose();
            throw new RuntimeException("Error executing migrations", e);
        } finally {
            databaseRepository.releaseLock();
        }
    }

    private boolean addChange(Migration migration) {
        try {
            databaseRepository.addMigration(migration);
            return true;
        } catch (Exception e) {
            throw new RuntimeException("Error during addChange operation", e);
        }
    }
}
