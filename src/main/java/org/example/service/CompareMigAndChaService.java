package org.example.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.exeptions.Md5SumNotMatch;
import org.example.model.ChangeLog;
import org.example.model.Migration;
import org.example.model.ChangeSet;
import org.example.repository.ChangesRepository;

import java.util.ArrayList;
import java.util.List;

public class CompareMigAndChaService {
    private final DatabaseServiceImplementation dbService;

    private static final Logger logger = LogManager.getLogger(CompareMigAndChaService.class);

    public CompareMigAndChaService(DatabaseServiceImplementation dbService) {
        this.dbService = dbService;
    }

    public List<ChangeSet> findNotExecutedChangesAndCheckExecuted() {
        List<Migration> executedMigrations = dbService.getAllChanges();

        ChangeLog changeLog = ChangesRepository.readMasterChanges();
        List<String> filesInOrder = changeLog.getChanges();

        List<ChangeSet> changeSets = ChangesRepository.readChangesFromList(filesInOrder);

        List<Migration> executedChangesToCheck = new ArrayList<>();
        List<ChangeSet> notExecutedChangeSets = new ArrayList<>();

        for (ChangeSet changeSet : changeSets) {
            if (executedMigrations.stream()
                    .anyMatch(change -> change.getFilename().equals(changeSet.getFilename()))) {
                Migration migrationFromMigration = changeSet.toMigration();
                executedChangesToCheck.add(migrationFromMigration);
            } else {
                notExecutedChangeSets.add(changeSet);
            }
        }

        compareOrderAndMD5Sum(executedChangesToCheck, executedMigrations);

        return notExecutedChangeSets;
    }

    private boolean compareOrderAndMD5Sum(List<Migration> executedMigrations, List<Migration> migrations) {
        if (executedMigrations.size() != migrations.size()) {
            throw new RuntimeException("The number of migrations in master_changelog " +
                    "does not match the number of migrations in database."+"\n"+
                    "!!!this is unexpected behaviour!!!");
        }

        for (int i = 0; i < executedMigrations.size(); i++) {
            Migration fromMigration = executedMigrations.get(i);
            Migration fromExecuted = migrations.get(i);

            if(!fromMigration.getFilename().equals(fromExecuted.getFilename())) {
                throw new RuntimeException("The filename in master_changelog "+fromMigration.getFilename()+
                        " do not match the filename in database "+fromExecuted.getFilename()+" \n" +
                        " check the order OR run myLiquid with git pull to get proper files");
            }
            if (!fromMigration.getMd5sum().equals(fromExecuted.getMd5sum())) {
                throw new Md5SumNotMatch("MD5 mismatch at file: " + fromMigration.getFilename() +".\n"+
                        "thy yo run myLiquid with pull request" +
                        "More details: "+
                        " From file: " + fromMigration + "\n" +
                        ", Executed: " + fromExecuted+ "\n you better run myLiquid with git pull to get proper files");
            }

        }
        return true;
    }
}
