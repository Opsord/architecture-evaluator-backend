package io.github.Opsord.architecture_evaluator_backend.modules.parser.project_scanner.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Set;

@Service
public class ScannerService {

    private static final Logger logger = LoggerFactory.getLogger(ScannerService.class);

    // -------------------------------------------------------------------------
    // Constants
    // -------------------------------------------------------------------------
    private static final Set<String> DEFAULT_IGNORED_FOLDERS = Set.of(
            "target", "build", ".git", "node_modules",
            ".idea", ".vscode", "out", "dist"
    );

    // -------------------------------------------------------------------------
    // Public Methods
    // -------------------------------------------------------------------------

    /**
     * Finds the root directory of a project by looking for a `pom.xml` file and a `src` folder.
     *
     * @param file The starting file or directory.
     * @return The project root directory, or null if not found.
     */
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
            logger.warn("Invalid project directory: {}", projectDirectory != null ? projectDirectory.getAbsolutePath() : "null");
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

    // -------------------------------------------------------------------------
    // Private Methods
    // -------------------------------------------------------------------------

    /**
     * Validates if the given file is a directory and not ignored.
     *
     * @param file The file to validate.
     * @return True if the file is a valid directory, false otherwise.
     */
    private boolean isValidDirectory(File file) {
        if (file == null || !file.isDirectory()) {
            logger.warn("Invalid starting directory: {}", file != null ? file.getAbsolutePath() : "null");
            return false;
        }

        if (DEFAULT_IGNORED_FOLDERS.contains(file.getName())) {
            logger.info("Skipping ignored folder: {}", file.getAbsolutePath());
            return false;
        }

        return true;
    }

    /**
     * Checks if a directory is a project root by verifying the presence of `pom.xml` and `src`.
     *
     * @param directory The directory to check.
     * @return True if the directory is a project root, false otherwise.
     */
    private boolean isProjectRoot(File directory) {
        return new File(directory, "pom.xml").exists() && new File(directory, "src").exists();
    }

    /**
     * Recursively scans subdirectories to find the project root.
     *
     * @param directory The directory to scan.
     * @return The project root directory, or null if not found.
     */
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