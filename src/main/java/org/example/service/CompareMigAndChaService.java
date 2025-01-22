package org.example.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.exeptions.Md5SumNotMatch;
import org.example.model.Change;
import org.example.model.MasterChangeLog;
import org.example.model.Migration;
import org.example.repository.MigrationRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class CompareMigAndChaService {
    private final DatabaseServiceImplementation dbService;

    private static final Logger logger = LogManager.getLogger(CompareMigAndChaService.class);

    public CompareMigAndChaService(DatabaseServiceImplementation dbService) {
        this.dbService = dbService;
    }

    public List<Migration> findNotExecutedMigrations() {
        List<Change> executedChanges = dbService.getAllChanges();


        List<Migration> migrationsWithOutOrder = MigrationRepository.readAllChanges();
        MasterChangeLog masterChangeLog = MigrationRepository.readMasterChanges();
        List<String> filesInOrder = masterChangeLog.getChanges();

//        sort
        List<Migration> migrations =  filesInOrder.stream()
                .map(name -> migrationsWithOutOrder.stream()
                        .filter(migration -> Objects.equals(migration.getFilename(), name))
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("Change not found for: " + name)))
                .toList();

        List<Change> allChangesFromMig = migrations.stream()
                .map(Migration::toChange)
                .collect(Collectors.toList());

        List<Change> executedChangesToCheck = new ArrayList<>();
        List<Migration> notExecutedMigrations = new ArrayList<>();

        for (Migration migration : migrations) {
            if (executedChanges.stream()
                    .anyMatch(change -> change.getFilename().equals(migration.getFilename()))) {
                Change changeFromMigration = migration.toChange();
                executedChangesToCheck.add(changeFromMigration);
            } else {
                notExecutedMigrations.add(migration);
            }
        }

        try {
            compareOrderAndMD5Sum(executedChangesToCheck, executedChanges);
        } catch (Md5SumNotMatch md5SumNotMatch) {
            logger.error(md5SumNotMatch.getMessage(), md5SumNotMatch);
            throw md5SumNotMatch;
        } catch (RuntimeException e) {
            logger.warn(e.getMessage(), e);
            throw e;
        }

        return notExecutedMigrations;
    }

    private boolean compareOrderAndMD5Sum(List<Change> executedMigrations, List<Change> changes) {
        if (executedMigrations.size() != changes.size()) {
            throw new RuntimeException("The number of changes in master_migrations does not match the number of changes in database.");
        }

        for (int i = 0; i < executedMigrations.size(); i++) {
            Change fromMigration = executedMigrations.get(i);
            Change fromExecuted = changes.get(i);

            if(!fromMigration.getFilename().equals(fromExecuted.getFilename())) {
                throw new RuntimeException("The filenames in master_migrations "+fromMigration.getFilename()+
                        " do not match the filenames in database "+fromExecuted.getFilename()+" \n" +
                        " check the order");
            }
            if (!fromMigration.getMd5sum().equals(fromExecuted.getMd5sum())) {
                throw new Md5SumNotMatch("MD5 mismatch at index " + i +
                        ". Migration: " + fromMigration +
                        ", Executed: " + fromExecuted+ "\n you better do git pull to get proper files");
            }

        }
        return true;
    }
}
