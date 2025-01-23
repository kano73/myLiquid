package org.example.repository;

import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.config.GetProperties;
import org.example.model.ChangeLog;
import org.example.model.ChangeSet;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class ChangesRepository {
    private static final Logger logger = LogManager.getLogger(ChangesRepository.class);

    private static String pathToMigrationsFile;
    private static String pathTomasterFile;

    static {
        pathToMigrationsFile = "./myLiquid_changes/changesets/";
        pathTomasterFile = "./myLiquid_changes/master_changelog.json";

        Properties properties = GetProperties.get();
        if(properties.getProperty("myliquid.changes.path") != null) {
            pathToMigrationsFile = properties.getProperty("myliquid.changes.path")+"/changesets/";
            pathTomasterFile = properties.getProperty("myliquid.changes.path")+"/master_changelog.json";
        }
    }

    public static ArrayList<ChangeSet> readChangesFromList(List<String> filesInOrder) {
        ArrayList<ChangeSet> changeSets = new ArrayList<>();
        Gson gson = new Gson();

        for (String filename : filesInOrder) {
            Path path = Paths.get(pathToMigrationsFile+filename);
            try {
                String content = Files.readString(path);

                changeSets.add(gson.fromJson(content, ChangeSet.class));
            }catch (Exception e) {
                logger.error("error while reading :{}", path, e);
                throw new RuntimeException(e);
            }
        }

        System.out.println("changesets read");
        changeSets.forEach(System.out::println);

        return changeSets;
    }

    public static ArrayList<ChangeSet> readAllChanges() {
        ArrayList<ChangeSet> changeSets = new ArrayList<>();

        Gson gson = new Gson();
        try (var paths = Files.walk(Paths.get(pathToMigrationsFile))) {
            paths.filter(Files::isRegularFile)
                    .forEach(path -> {
                        try {
                            String content = Files.readString(path);

                            ChangeSet changeSet = gson.fromJson(content, ChangeSet.class);
                            changeSet.setFilename(path.getFileName().toString());

                            changeSets.add(changeSet);

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
        }catch (Exception e) {
            logger.error("error while reading changes from: "+pathToMigrationsFile, e);
            throw new RuntimeException(e);
        }

        return changeSets;
    }

    public static ChangeLog readMasterChanges(){
        ChangeLog master = new ChangeLog();

        Path path = Paths.get(pathTomasterFile);

        Gson gson = new Gson();
        try {
            String content = Files.readString(path);

            master = gson.fromJson(content, ChangeLog.class);
        }catch (Exception e) {
            logger.error("error while reading master_changelog.json", e);
            throw new RuntimeException(e);
        }

        System.out.println(master);

        return master;
    }
}
