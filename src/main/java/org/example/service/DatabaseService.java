package org.example.service;

import org.example.model.Change;
import org.example.repository.DatabaseRepository;

import java.util.ArrayList;
import java.util.function.Function;

public class DatabaseService {

    private final DatabaseRepository databaseRepository;

    public DatabaseService(DatabaseRepository databaseRepository) {
        this.databaseRepository = databaseRepository;
    }

    public ArrayList<Change> getAllChanges() {
        return executeWithLock(DatabaseRepository::getAllChanges);
    }

    public boolean addChange(Change change) {
        return executeWithLock(repo -> repo.addChange(change));
    }

    private <T> T executeWithLock(Function<DatabaseRepository, T> action) {
        databaseRepository.openConnection();
        try {
            if (databaseRepository.isDataBaseIsAvailableForChanges()) {
                databaseRepository.setLockedStatus(false);

                //method to execute
                return action.apply(databaseRepository);
            } else {
                databaseRepository.rollbackAndClose();
                throw new RuntimeException("Database is not available for changes, due to it is changing currently");
            }
        } finally {
            databaseRepository.setLockedStatus(true);
            databaseRepository.commitAndClose();
        }
    }

}
