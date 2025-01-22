package org.example.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.example.config.GetProperties;

import java.io.File;
import java.io.IOException;
import java.util.Properties;


public class GitService {

    private static final Logger logger = LogManager.getLogger(GitService.class);
    private static String token;
    private static String username;
    private final Git git;

    public GitService() {
        Properties prop = GetProperties.get();

        String repoUrl = prop.getProperty("myliquid.git.link");
        String localPath = prop.getProperty("myliquid.migrations.path");
        String token = prop.getProperty("myliquid.git.token");
        String username = prop.getProperty("myliquid.git.username");

        localPath = localPath == null ? "./migrations" : localPath;
        if (repoUrl == null || repoUrl.isEmpty()) {
            throw new IllegalArgumentException("git.link (Link for git repository) property not set");
        }
        if(token == null || token.isEmpty()) {
            throw new IllegalArgumentException("git.token (Token for git repository) property not set");
        }
        if(username == null || username.isEmpty()) {
            throw new IllegalArgumentException("git.username property not set");
        }
        GitService.token = token;
        GitService.username = username;

        this.git = initializeGit(localPath, repoUrl, token);
    }

    public GitService pull() {
        executeGitOperation("pulling changes", git ->{
            PullResult result = git.pull()
                    .setCredentialsProvider(new UsernamePasswordCredentialsProvider(username, token))
                    .call();
            if (result.isSuccessful()) {
                logger.info("Pull Successful");
            } else {
                logger.error("Pull Failed");
                throw new RuntimeException("git pull Failed");
            }
        });
        return this;
    }

    private Git initializeGit(String localPath, String repoUrl, String token) {
        try {
            File localRepoDir =  new File(localPath);

            String tokenUrl = repoUrl.replace("https://", "https://" + token + "@");

            if (!localRepoDir.exists()) {
                logger.info("Cloning repository...");
                return Git.cloneRepository()
                        .setURI(tokenUrl)
                        .setDirectory(localRepoDir)
                        .call();
            } else {
                logger.info("Opening existing repository...");
                return Git.open(localRepoDir);
            }
        } catch (GitAPIException | IOException e) {
            logger.error("Failed to initialize Git repository", e);
            throw new RuntimeException("Failed to initialize Git repository", e);
        }
    }

    public GitService addAllFiles() {
        executeGitOperation("Adding all files", git -> {
            git.add().addFilepattern(".").call();
        });
        return this;
    }

    public GitService commit(String message) {
        executeGitOperation("Committing changes", git -> {
            Status status = git.status().call();
            if (!status.hasUncommittedChanges()) {
                logger.info("Nothing to commit");
                return;
            }
            git.commit().setMessage(message).call();
        });
        return this;
    }

    public GitService push() {
        executeGitOperation("Pushing changes", git -> {
            git.push()
                    .setCredentialsProvider(new UsernamePasswordCredentialsProvider(username, token))
                    .call();
        });
        return this;
    }

    private void executeGitOperation(String operationName, GitOperation operation) {
        try {
            logger.info("{} started...", operationName);
            operation.execute(git);
            logger.info("{} completed successfully.", operationName);
        } catch (Exception e) {
            logger.error("{} failed: {}", operationName, e.getMessage());
            throw new RuntimeException(operationName + " failed", e);
        }
    }

    @FunctionalInterface
    private interface GitOperation {
        void execute(Git git) throws Exception;
    }
}
