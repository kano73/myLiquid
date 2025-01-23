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
    private final MigrationExecutionService migrationExecutionService;
    private final GitService gitService;

    public MyLiquid(DatabaseServiceImplementation dbService, ChangesRepository migRepository, MigrationExecutionService migrationExecutionService, GitService gitService) {
        this.dbService = dbService;
        this.migRepository = migRepository;
        this.migrationExecutionService = migrationExecutionService;
        this.gitService = gitService;
    }

    public void commitPushToGit(String message){
        gitService.addAllFiles().commit(message).push();
    }

    public void migrate() throws SQLException {

    }
}
