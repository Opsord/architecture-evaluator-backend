package io.github.Opsord.architecture_evaluator_backend.modules.parser.project_scanner.services;

import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.dto.CustomCompilationUnitDTO;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.project_scanner.dto.LayerAnnotation;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.CompilationUnitService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectScannerService {

    private static final Logger logger = LoggerFactory.getLogger(ProjectScannerService.class);
    private final CompilationUnitService compilationUnitService;

    /**
     * Scans the project directory and parses Java files into CustomCompilationUnitDTOs.
     *
     * @param projectPath The path of the project to scan.
     * @return A list of CustomCompilationUnitDTOs.
     * @throws IOException If an error occurs while reading files.
     */
    public List<CustomCompilationUnitDTO> scanProject(String projectPath) throws IOException {
        List<File> javaFiles = new ArrayList<>();

        // Walk through the project directory and collect all Java files
        try (var paths = java.nio.file.Files.walk(java.nio.file.Paths.get(projectPath))) {
            paths.filter(java.nio.file.Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".java"))
                    .forEach(path -> javaFiles.add(path.toFile()));
        }

        // Parse each Java file and create a CustomCompilationUnitDTO
        List<CustomCompilationUnitDTO> compilationUnits = new ArrayList<>();
        for (File javaFile : javaFiles) {
            try {
                logger.info("Parsing file: {}", javaFile.getAbsolutePath());
                CustomCompilationUnitDTO dto = compilationUnitService.parseJavaFile(javaFile);
                compilationUnits.add(dto);
            } catch (Exception e) {
                logger.error("Failed to parse file: {}", javaFile.getAbsolutePath(), e);
            }
        }
        return compilationUnits;
    }

}