package io.github.Opsord.architecture_evaluator_backend.modules.parser.project_scanner.services;

import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.dto.CustomCompilationUnitDTO;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.CompilationUnitService;
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
    private final CompilationUnitService compilationUnitService;

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

    public List<CustomCompilationUnitDTO> parseJavaFiles(List<File> javaFiles) {
        List<CustomCompilationUnitDTO> compilationUnits = new ArrayList<>();
        for (File javaFile : javaFiles) {
            try {
                CustomCompilationUnitDTO unit = compilationUnitService.parseJavaFile(javaFile);
                compilationUnits.add(unit);
            } catch (Exception e) {
                logger.error("Failed to parse file: {}", javaFile.getAbsolutePath(), e);
            }
        }
        return compilationUnits;
    }
}