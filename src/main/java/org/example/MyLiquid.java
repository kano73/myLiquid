package org.example;

import org.example.repository.DatabaseRepository;
import org.example.service.MigrationExecutionService;
import org.example.service.DatabaseServiceImplementation;
import org.example.service.GitService;
import org.example.service.MyLiquidStarter;

public class MyLiquid {

    public static void main(String[] args) {
        startMigration();
    }

    public static void startMigration() {
        DatabaseServiceImplementation dbService = new DatabaseServiceImplementation(new DatabaseRepository());
        MyLiquidStarter ml = new MyLiquidStarter(new MigrationExecutionService(dbService), new GitService());
        ml.migrate();
    }
}