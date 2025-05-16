package io.github.Opsord.architecture_evaluator_backend.modules.parser.project_scanner.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class ScanningService {

    private static final Logger logger = LoggerFactory.getLogger(ScanningService.class);

    public File findProjectRoot(File file) {
        File current = file;
        while (current != null) {
            if (new File(current, "pom.xml").exists() && new File(current, "src").exists()) {
                logger.info("Project root found: {}", current.getAbsolutePath());
                return current;
            }
            current = current.getParentFile();
        }
        return null;
    }

    public List<File> scanSrcFolder(File srcFolder) throws IOException {
        List<File> javaFiles = new ArrayList<>();
        try (var paths = java.nio.file.Files.walk(srcFolder.toPath())) {
            paths.filter(java.nio.file.Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".java"))
                    .forEach(path -> javaFiles.add(path.toFile()));
        }
        return javaFiles;
    }

    public File findPomFile(String projectPath) {
        File pomFile = new File(projectPath, "pom.xml");
        if (pomFile.exists()) {
            return pomFile;
        }
        logger.warn("No pom.xml file found in project path: {}", projectPath);
        return null;
    }
}