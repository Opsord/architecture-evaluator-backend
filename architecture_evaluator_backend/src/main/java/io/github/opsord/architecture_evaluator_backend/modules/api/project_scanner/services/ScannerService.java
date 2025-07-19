package io.github.opsord.architecture_evaluator_backend.modules.api.project_scanner.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Set;
import java.util.List;

@Service
public class ScannerService {

    private static final Logger logger = LoggerFactory.getLogger(ScannerService.class);

    private static final Set<String> DEFAULT_IGNORED_FOLDERS = Set.of(
            "target", "build", ".git", "node_modules",
            ".idea", ".vscode", "out", "dist");

    private static final List<String> GRADLE_FILES = List.of("build.gradle", "build.gradle.kts");

    public File findProjectRoot(File file) {
        if (!isValidDirectory(file)) {
            return null;
        }

        logger.info("Scanning directory: {}", file.getAbsolutePath());
        if (isProjectRoot(file)) {
            logger.info("Project root found: {}", file.getAbsolutePath());
            return file;
        }

        return scanSubdirectoriesForProjectRoot(file);
    }

    public File findPomFile(File projectDirectory) {
        if (projectDirectory == null || !projectDirectory.isDirectory()) {
            logger.warn("Invalid project directory: {}",
                    projectDirectory != null ? projectDirectory.getAbsolutePath() : "null");
            return null;
        }

        File pomFile = new File(projectDirectory, "pom.xml");
        if (pomFile.exists()) {
            logger.info("Found `pom.xml` file at: {}", pomFile.getAbsolutePath());
            return pomFile;
        }

        logger.warn("No `pom.xml` file found in directory: {}", projectDirectory.getAbsolutePath());
        return null;
    }

    private boolean isValidDirectory(File file) {
        if (file == null || !file.isDirectory()) {
            logger.warn("Invalid starting directory");
            return false;
        }

        if (DEFAULT_IGNORED_FOLDERS.contains(file.getName())) {
            logger.info("Skipping ignored folder");
            return false;
        }

        return true;
    }

    /**
     * Checks if a directory is a project root by verifying the presence of
     * `pom.xml` or Gradle build file and `src`.
     */
    private boolean isProjectRoot(File directory) {
        boolean hasSrc = new File(directory, "src").exists();
        boolean hasPom = new File(directory, "pom.xml").exists();
        boolean hasGradle = GRADLE_FILES.stream().anyMatch(f -> new File(directory, f).exists());
        return hasSrc && (hasPom || hasGradle);
    }

    private File scanSubdirectoriesForProjectRoot(File directory) {
        File[] subDirectories = directory.listFiles(File::isDirectory);
        if (subDirectories == null) {
            return null;
        }

        for (File subDir : subDirectories) {
            if (DEFAULT_IGNORED_FOLDERS.contains(subDir.getName())) {
                continue;
            }

            File projectRoot = findProjectRoot(subDir);
            if (projectRoot != null) {
                return projectRoot;
            }
        }

        logger.warn("No project root found in directory: {}", directory.getAbsolutePath());
        return null;
    }
}