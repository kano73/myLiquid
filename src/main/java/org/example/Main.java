package org.example;

import org.example.repository.DatabaseRepository;
import org.example.service.MigrationExecutionService;
import org.example.service.DatabaseServiceImplementation;
import org.example.service.GitService;
import org.example.service.MyLiquid;

public class Main {
    public static void main(String[] args) {
        DatabaseServiceImplementation dbService = new DatabaseServiceImplementation(new DatabaseRepository());
        MyLiquid ml = new MyLiquid(new MigrationExecutionService(dbService), new GitService());
        ml.migrate();
    }
}