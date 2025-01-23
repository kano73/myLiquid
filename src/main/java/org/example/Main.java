package org.example;



import org.example.repository.DatabaseRepository;
import org.example.repository.ChangesRepository;
import org.example.service.CompareMigAndChaService;
import org.example.service.DatabaseServiceImplementation;
import org.example.service.GitService;
import org.example.service.MyLiquid;

import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {
        DatabaseServiceImplementation dbService = new DatabaseServiceImplementation(new DatabaseRepository());
        MyLiquid ml = new MyLiquid(dbService,new ChangesRepository(), new CompareMigAndChaService(dbService), new GitService());

//        ml.commitAndPushToGit("ml test");
        try {
            ml.migrate();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}