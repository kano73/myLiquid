package org.example.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.config.GetProperties;
import org.example.exeptions.Md5SumNotMatch;
import org.example.model.BaseMigCha;
import org.example.model.ChangeLog;
import org.example.model.Migration;
import org.example.model.ChangeSet;
import org.example.repository.ChangesRepository;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

public class MigrationExecutionService {
    private static String migVersion;

    private List<Migration> executedMigrations;
    private List<ChangeSet> executedChangeSets;
    private List<ChangeSet> notExecutedChangeSets;

    private final DatabaseServiceImplementation dbService;
    private static final Logger logger = LogManager.getLogger(MigrationExecutionService.class);

    static {
        Properties properties = GetProperties.get();
        migVersion = properties.getProperty("myliquid.migration.version");
    }

    public MigrationExecutionService(DatabaseServiceImplementation dbService) {
        this.dbService = dbService;
    }

    public void migrate(){

    }

    public void groupSetsAndMig() throws SQLException {
        List<Migration> executedMigrations = dbService.getAllChanges();

        ChangeLog changeLog = ChangesRepository.readMasterChanges();
        List<String> filesInOrder = changeLog.getChanges();

        List<ChangeSet> changeSets = ChangesRepository.readChangesFromList(filesInOrder);

        for (ChangeSet changeSet : changeSets) {
            if (executedMigrations.stream()
                    .anyMatch(change -> change.getFilename().equals(changeSet.getFilename())))
            {
                this.executedChangeSets.add(changeSet);
                this.executedMigrations.add(changeSet.toMigration());
            } else {
                notExecutedChangeSets.add(changeSet);
            }
        }
        Collections.reverse(notExecutedChangeSets);
    }

    public void executeTillVersion() throws SQLException {
        if(migVersion==null){
            dbService.executeAllChangeSets(notExecutedChangeSets);
        }

        List<String> namesOfNotExecuted = notExecutedChangeSets.stream()
                .map(BaseMigCha::getFilename)
                .toList();

        List<String> namesOfExecuted = executedMigrations.stream()
                .map(BaseMigCha::getFilename)
                .toList();

        if(!namesOfNotExecuted.contains(migVersion)){
            List<ChangeSet> notExecutedTillVersion = getTillVersionChangeSets(notExecutedChangeSets);
            dbService.executeAllChangeSets(notExecutedTillVersion);
        } else if (!namesOfExecuted.contains(migVersion)) {
            List<ChangeSet> executedTillVersion = getTillVersionChangeSets(executedChangeSets);
            dbService.rollBackChangeSets(executedTillVersion);
        }
    }

    private List<ChangeSet> getTillVersionChangeSets(List<ChangeSet> sets) {
        List<ChangeSet> result = new ArrayList<>();

        for(ChangeSet changeSet : sets){
            result.add(changeSet);
            if(changeSet.getFilename().equals(migVersion)){
                break;
            }
        }
        return result;
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
