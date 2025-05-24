package io.github.Opsord.architecture_evaluator_backend.modules.parser.project_scanner.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

@Service
public class ScanningService {

    private static final Logger logger = LoggerFactory.getLogger(ScanningService.class);

    /**
     * Finds the root directory of a project by looking for a `pom.xml` file and a `src` folder.
     *
     * @param file The starting file or directory.
     * @return The project root directory, or null if not found.
     */
    public File findProjectRoot(File file) {
        File current = file;
        while (current != null) {
            if (isProjectRoot(current)) {
                logger.info("Project root found: {}", current.getAbsolutePath());
                return current;
            }
            current = current.getParentFile();
        }
        assert file != null;
        logger.warn("No project root found starting from: {}", file.getAbsolutePath());
        return null;
    }

    /**
     * Scans the `src` folder for all `.java` files.
     *
     * @param srcFolder The `src` folder to scan.
     * @return A list of `.java` files found in the folder.
     * @throws IOException If an error occurs while scanning.
     */
    public List<File> scanSrcFolder(File srcFolder) throws IOException {
        if (!srcFolder.exists() || !srcFolder.isDirectory()) {
            logger.warn("Invalid `src` folder: {}", srcFolder.getAbsolutePath());
            return List.of();
        }

        List<File> javaFiles = new ArrayList<>();
        try (var paths = Files.walk(srcFolder.toPath())) {
            paths.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".java"))
                    .forEach(path -> javaFiles.add(path.toFile()));
        }
        logger.info("Found {} Java files in `src` folder: {}", javaFiles.size(), srcFolder.getAbsolutePath());
        return javaFiles;
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