package io.github.opsord.architecture_evaluator_backend.modules.api.project_scanner.services;

import io.github.opsord.architecture_evaluator_backend.modules.api.parser.instances.file_instance.FileInstance;
import io.github.opsord.architecture_evaluator_backend.modules.api.parser.services.file_instance.FileInstanceService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class SrcScannerService {

    private static final Logger logger = LoggerFactory.getLogger(SrcScannerService.class);
    private final FileInstanceService fileInstanceService;

    /**
     * Scans the `src` folder for all `.java` files.
     */
    public List<File> scanSrcFolder(File srcFolder) throws IOException {
        if (!srcFolder.exists() || !srcFolder.isDirectory()) {
            logger.warn("Invalid `src` folder: {}", srcFolder.getAbsolutePath());
            return List.of();
        }

        File scanRoot = resolveJavaSourceRoot(srcFolder);
        List<File> javaFiles = new ArrayList<>();
        try (var paths = Files.walk(scanRoot.toPath())) {
            paths.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".java"))
                    .forEach(path -> javaFiles.add(path.toFile()));
        }
        logger.info("Found {} Java files under: {}", javaFiles.size(), scanRoot.getAbsolutePath());
        return javaFiles;
    }

    private File resolveJavaSourceRoot(File srcFolder) {
        File mainJava = new File(srcFolder, "main/java");
        if (mainJava.isDirectory()) {
            return mainJava;
        }
        return srcFolder;
    }

    /**
     * Parses Java files into FileInstance objects.
     */
    public List<FileInstance> parseJavaFiles(List<File> javaFiles, File projectRoot) {
        return javaFiles.parallelStream()
                .map(javaFile -> {
                    try {
                        return fileInstanceService.parseJavaFile(javaFile, projectRoot);
                    } catch (Exception e) {
                        logger.error("Failed to parse file: {}", javaFile.getAbsolutePath(), e);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .toList();
    }
}