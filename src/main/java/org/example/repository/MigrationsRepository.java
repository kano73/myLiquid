package org.example.repository;

import com.google.gson.Gson;
import org.example.config.GetProperties;
import org.example.model.Migration;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class MigrationsRepository {

    private static String pathToMigrationsFile;

    public MigrationsRepository() {
        pathToMigrationsFile = ".migrations/changes";

        Properties properties = GetProperties.get();
        if(properties.getProperty("myliquid.migrations.path") != null) {
            pathToMigrationsFile = properties.getProperty("myliquid.migrations.path");
        }
    }

    public static ArrayList<Migration> readAllFiles(String directoryPath) throws IOException {
        ArrayList<Migration> migrations = new ArrayList<>();

        Gson gson = new Gson();
        try (var paths = Files.walk(Paths.get(directoryPath))) {
            paths.filter(Files::isRegularFile)
                    .forEach(path -> {
                        try {
                            String content = Files.readString(path);

                            migrations.add(gson.fromJson(content, Migration.class));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
        }

        return null;
    }
}
