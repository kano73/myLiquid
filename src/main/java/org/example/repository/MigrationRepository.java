package org.example.repository;

import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.config.GetProperties;
import org.example.model.MasterChangeLog;
import org.example.model.Migration;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Properties;

public class MigrationRepository {
    private static final Logger logger = LogManager.getLogger(MigrationRepository.class);

    private static String pathToMigrationsFile;
    private static String pathTomasterFile;

    static {
        pathToMigrationsFile = "./myLiquid_migrations/migrations";
        pathTomasterFile = "./myLiquid_migrations/master_migrationlog.json";

        Properties properties = GetProperties.get();
        if(properties.getProperty("myliquid.migrations.path") != null) {
            pathToMigrationsFile = properties.getProperty("myliquid.migrations.path")+"/changes";
            pathTomasterFile = properties.getProperty("myliquid.migrations.path")+"/master_migrationlog.json";
        }
    }

    public static ArrayList<Migration> readAllChanges() {
        ArrayList<Migration> migrations = new ArrayList<>();

        Gson gson = new Gson();
        try (var paths = Files.walk(Paths.get(pathToMigrationsFile))) {
            paths.filter(Files::isRegularFile)
                    .forEach(path -> {
                        try {
                            String content = Files.readString(path);

                            Migration migration = gson.fromJson(content, Migration.class);
                            migration.setFilename(path.getFileName().toString());

                            migrations.add(migration);

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
        }catch (Exception e) {
            logger.error("error while reading changes from: "+pathToMigrationsFile, e);
            throw new RuntimeException(e);
        }

        return migrations;
    }

    public static MasterChangeLog readMasterChanges(){
        MasterChangeLog master = new MasterChangeLog();

        Path path = Paths.get(pathTomasterFile);

        Gson gson = new Gson();
        try {
            try {
                String content = Files.readString(path);

                master = gson.fromJson(content, MasterChangeLog.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }catch (Exception e) {
            logger.error("error while reading master_changelog.json", e);
            throw new RuntimeException(e);
        }

        return master;
    }
}
