package org.example.service;

import org.example.model.Change;
import org.example.model.Migration;
import org.example.repository.MigrationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class MyLiquid {
    private static final Logger log = LoggerFactory.getLogger(MyLiquid.class);

    private final DatabaseServiceImplementation dbService;
    private final MigrationRepository migRepository;
    private final CompareMigAndChaService compareMigAndChaService;
    private final GitService gitService;

    public MyLiquid(DatabaseServiceImplementation dbService, MigrationRepository migRepository, CompareMigAndChaService compareMigAndChaService, GitService gitService) {
        this.dbService = dbService;
        this.migRepository = migRepository;
        this.compareMigAndChaService = compareMigAndChaService;
        this.gitService = gitService;
    }

    public void commitAndPushToGit(String message){
        gitService.addAllFiles().commit(message).push();
    }

    public void migrate() throws SQLException {
        List<Migration> migrations = compareMigAndChaService.findNotExecutedMigrations();
        if (migrations.isEmpty()) {
            log.info("No migrations found");
            return;
        }
        dbService.executeAllMigrations(migrations);
    }
}
