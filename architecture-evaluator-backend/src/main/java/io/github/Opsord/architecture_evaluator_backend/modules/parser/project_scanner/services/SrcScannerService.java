package io.github.Opsord.architecture_evaluator_backend.modules.parser.project_scanner.services;

import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.instances.class_instance.parts.LayerAnnotation;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.instances.file_instance.FileInstance;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.file_instance.FileInstanceService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

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
     * Parses Java files into FileInstance objects.
     */
    public List<FileInstance> parseJavaFiles(List<File> javaFiles, File projectRoot) {
        List<FileInstance> fileInstances = new ArrayList<>();
        for (File javaFile : javaFiles) {
            try {
                FileInstance unit = fileInstanceService.parseJavaFile(javaFile, projectRoot);
                fileInstances.add(unit);
            } catch (Exception e) {
                logger.error("Failed to parse file: {}", javaFile.getAbsolutePath(), e);
            }
        }
        return fileInstances;
    }

    /**
     * Filters FileInstances by LayerType.
     */
    public List<FileInstance> filterByLayerType(List<FileInstance> fileInstances, LayerAnnotation layerAnnotation) {
        List<FileInstance> filtered = new ArrayList<>();
        for (FileInstance unit : fileInstances) {
            if (unit.getClasses() != null && !unit.getClasses().isEmpty()) {
                if (unit.getClasses().get(0).getLayerAnnotation() == layerAnnotation) {
                    filtered.add(unit);
                }
            }
        }
        return filtered;
    }
}