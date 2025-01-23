package org.example.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.config.GetProperties;

import java.time.LocalDateTime;
import java.util.Properties;

public class MyLiquid {
    private static final Logger logger = LogManager.getLogger(MyLiquid.class);
    private final MigrationExecutionService migrationExecutionService;
    private final GitService gitService;

    private static String migrationLevel;

    static{
        Properties prop = GetProperties.get();
        migrationLevel = prop.getProperty("myliquid.migration.level");
        if(migrationLevel == null || migrationLevel.isEmpty()){
            migrationLevel="mig";
        }
    }

    public MyLiquid(MigrationExecutionService migrationExecutionService, GitService gitService) {
        this.migrationExecutionService = migrationExecutionService;
        this.gitService = gitService;
    }

    public void commitPushToGit(String message){
        gitService.addAllFiles().commit(message).push();
    }

    public void pullFromGit(){
        gitService.pull();
    }

    public void migrate(){
        if(migrationLevel.equals("push_mig")){
            LocalDateTime now = LocalDateTime.now();
            commitPushToGit("mig"+now);
        }else if(migrationLevel.equals("pull_mig")){
            pullFromGit();
        }
        migrationExecutionService.migrate();
    }
}
