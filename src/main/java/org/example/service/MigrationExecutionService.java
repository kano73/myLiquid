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
import java.util.*;

public class MigrationExecutionService {
    private static String migVersion;

    private List<Migration> executedMigrationsToCheck = new ArrayList<>();

    private List<Migration> executedMigrationsFromDB = new ArrayList<>();
    private List<Migration> notExecutedMigrations= new ArrayList<>();

    private List<ChangeSet> executedChangeSets= new ArrayList<>();
    private List<ChangeSet> notExecutedChangeSets= new ArrayList<>();

    private final DatabaseServiceImplementation dbService;
    private static final Logger logger = LogManager.getLogger(MigrationExecutionService.class);

    static {
        Properties properties = GetProperties.get();
        migVersion = properties.getProperty("myliquid.migration.version");
        if (Objects.equals(migVersion, "")) {
            migVersion=null;
        }
    }

    public MigrationExecutionService(DatabaseServiceImplementation dbService) {
        this.dbService = dbService;
    }

    public void migrate(){
        try {
            groupSetsAndMigs();
            compareOrderAndMD5Sum();
            executeTillVersion();
            logger.info("Your db version: "+migVersion);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    public void groupSetsAndMigs() throws SQLException {
        executedMigrationsFromDB = dbService.getAllChanges();

        ChangeLog changeLog = ChangesRepository.readMasterChanges();
        List<String> filesInOrder = changeLog.getChanges();

        List<ChangeSet> changeSets = ChangesRepository.readChangesFromList(filesInOrder);

        for (ChangeSet changeSet : changeSets) {
            if (executedMigrationsFromDB.stream()
                    .anyMatch(change -> change.getFilename().equals(changeSet.getFilename())))
            {
                executedChangeSets.add(changeSet);
                executedMigrationsToCheck.add(changeSet.toMigration());
            } else {
                notExecutedChangeSets.add(changeSet);
                notExecutedMigrations.add(changeSet.toMigration());
            }
        }
        Collections.reverse(notExecutedChangeSets);
    }

    public void executeTillVersion() throws SQLException {
        if(migVersion == null || Objects.equals(migVersion, "")){

            if(notExecutedChangeSets.isEmpty()){
                logger.info("Your db is up to date");
                try{
                    migVersion = executedMigrationsFromDB.getFirst().getFilename();
                }catch(Exception e){
                    migVersion="-none-";
                }
                return;
            }
            migVersion= notExecutedMigrations.getLast().getFilename();
            dbService.executeAllChangeSets(notExecutedChangeSets);
            return;
        }

        List<String> namesOfNotExecuted = notExecutedChangeSets.stream()
                .map(BaseMigCha::getFilename)
                .toList();

        List<String> namesOfExecuted = executedMigrationsFromDB.stream()
                .map(BaseMigCha::getFilename)
                .toList();

        if(namesOfNotExecuted.contains(migVersion)){
            List<ChangeSet> notExecutedTillVersion = new ArrayList<>();

            for(ChangeSet changeSet : notExecutedChangeSets){
                notExecutedTillVersion.add(changeSet);
                if(changeSet.getFilename().equals(migVersion)){
                    break;
                }
            }

            if(notExecutedTillVersion.isEmpty()){
                logger.info("Your db is up to date");
                return;
            }
            dbService.executeAllChangeSets(notExecutedTillVersion);
        }
        else if (namesOfExecuted.contains(migVersion)) {
            List<ChangeSet> executedTillVersion = new ArrayList<>();

            for(ChangeSet changeSet : executedChangeSets){
                if(changeSet.getFilename().equals(migVersion)){
                    break;
                }
                executedTillVersion.add(changeSet);
            }

            if(executedTillVersion.isEmpty()){
                logger.info("Your db is up to date");
                return;
            }
            logger.info("rolling back process started");
            dbService.rollBackChangeSets(executedTillVersion);
        }
        else{
            throw new RuntimeException("Migrated version: "+migVersion+" do not match any file");
        }
    }

    private boolean compareOrderAndMD5Sum() {
        if (executedMigrationsFromDB.size() != executedMigrationsToCheck.size()) {
            throw new RuntimeException("The number of migrations in master_changelog " +
                    "does not match the number of migrations in database."+"\n"+
                    "!!!this is unexpected behaviour!!!");
        }

        for (int i = 0; i < executedMigrationsFromDB.size(); i++) {
            Migration fromMigration = executedMigrationsToCheck.get(i);
            Migration fromExecuted = executedMigrationsFromDB.get(i);

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
