package io.github.Opsord.architecture_evaluator_backend.modules.parser.project_scanner.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class ScannerService {

    private static final Logger logger = LoggerFactory.getLogger(ScannerService.class);

    private static final Set<String> DEFAULT_IGNORED_FOLDERS = Set.of(
            "target", "build", ".git", "node_modules",
            ".idea", ".vscode", "out", "dist");

    /**
     * Finds the root directory of a project by looking for a `pom.xml` file and a `src` folder.
     *
     * @param file The starting file or directory.
     * @return The project root directory, or null if not found.
     */

    public File findProjectRoot(File file) {
        if (file == null || !file.isDirectory()) {
            logger.warn("Invalid starting directory: {}", file != null ? file.getAbsolutePath() : "null");
            return null;
        }

        if (DEFAULT_IGNORED_FOLDERS.contains(file.getName())) {
            logger.info("Skipping ignored folder: {}", file.getAbsolutePath());
            return null;
        }

        logger.info("Scanning directory: {}", file.getAbsolutePath());
        if (isProjectRoot(file)) {
            logger.info("Project root found: {}", file.getAbsolutePath());
            return file; // Stop scanning and return the root
        }

        File[] subDirectories = file.listFiles(File::isDirectory);
        if (subDirectories != null) {
            for (File subDir : subDirectories) {
                if (!DEFAULT_IGNORED_FOLDERS.contains(subDir.getName())) {
                    File projectRoot = findProjectRoot(subDir);
                    if (projectRoot != null) {
                        return projectRoot; // Stop scanning once a valid root is found
                    }
                }
            }
        }

        logger.warn("No project root found in directory: {}", file.getAbsolutePath());
        return null;
    }

    /**
     * Finds the `pom.xml` file in the given project path.
     *
     * @param projectPath The path to the project.
     * @return The `pom.xml` file, or null if not found.
     */
    public File findPomFile(String projectPath) {
        File pomFile = new File(projectPath, "pom.xml");
        if (pomFile.exists()) {
            logger.info("Found `pom.xml` file at: {}", pomFile.getAbsolutePath());
            return pomFile;
        }
        logger.warn("No `pom.xml` file found in project path: {}", projectPath);
        return null;
    }

    // -------------------------------------------------------------------------
    // Helper Methods
    // -------------------------------------------------------------------------

    /**
     * Checks if a directory is a project root by verifying the presence of `pom.xml` and `src`.
     *
     * @param directory The directory to check.
     * @return True if the directory is a project root, false otherwise.
     */
    private boolean isProjectRoot(File directory) {
        return new File(directory, "pom.xml").exists() && new File(directory, "src").exists();
    }
}