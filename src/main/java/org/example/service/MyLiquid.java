package org.example.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.model.ChangeSet;
import org.example.repository.ChangesRepository;

import java.sql.SQLException;
import java.util.List;

public class MyLiquid {
    private static final Logger logger = LogManager.getLogger(MyLiquid.class);

    private final DatabaseServiceImplementation dbService;
    private final ChangesRepository migRepository;
    private final CompareMigAndChaService compareMigAndChaService;
    private final GitService gitService;

    public MyLiquid(DatabaseServiceImplementation dbService, ChangesRepository migRepository, CompareMigAndChaService compareMigAndChaService, GitService gitService) {
        this.dbService = dbService;
        this.migRepository = migRepository;
        this.compareMigAndChaService = compareMigAndChaService;
        this.gitService = gitService;
    }

    public void commitAndPushToGit(String message){
        gitService.addAllFiles().commit(message).push();
    }

    public void migrate() throws SQLException {
        List<ChangeSet> changeSets = compareMigAndChaService.findNotExecutedChangesAndCheckExecuted();

        if (changeSets.isEmpty()) {
            logger.info("No migrations found, your db is up to date");
            return;
        }

        dbService.executeAllMigrations(changeSets);
    }
}
