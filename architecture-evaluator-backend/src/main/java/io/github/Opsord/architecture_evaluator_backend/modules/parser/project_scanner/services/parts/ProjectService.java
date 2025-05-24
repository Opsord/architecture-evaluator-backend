package io.github.Opsord.architecture_evaluator_backend.modules.parser.project_scanner.services.parts;

import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.dto.CustomCompilationUnitDTO;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.CompilationUnitService;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.project_scanner.services.ScanningService;
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
public class ProjectService {

    private static final Logger logger = LoggerFactory.getLogger(ProjectService.class);
    private final CompilationUnitService compilationUnitService;
    private final ScanningService scanningService;

    /**
     * Scans the project directory for Java files and parses them into compilation units.
     *
     * @param filePath The path to the project directory.
     * @return A list of `CustomCompilationUnitDTO` objects representing the parsed Java files.
     * @throws IOException If an error occurs during scanning.
     */
    public List<CustomCompilationUnitDTO> scanProject(String filePath) throws IOException {
        File projectRoot = findProjectRoot(filePath);
        if (projectRoot == null) {
            return List.of();
        }

        File srcFolder = new File(projectRoot, "src");
        if (!isValidSrcFolder(srcFolder)) {
            return List.of();
        }

        List<File> javaFiles = scanningService.scanSrcFolder(srcFolder);
        return parseJavaFiles(javaFiles);
    }

    // -------------------------------------------------------------------------
    // Helper Methods
    // -------------------------------------------------------------------------

    private File findProjectRoot(String filePath) {
        File projectRoot = scanningService.findProjectRoot(new File(filePath));
        if (projectRoot == null) {
            logger.warn("No valid project root found for path: {}", filePath);
        }
        return projectRoot;
    }

    private boolean isValidSrcFolder(File srcFolder) {
        if (!srcFolder.exists() || !srcFolder.isDirectory()) {
            logger.warn("No 'src' folder found in project root: {}", srcFolder.getAbsolutePath());
            return false;
        }
        return true;
    }

    private List<CustomCompilationUnitDTO> parseJavaFiles(List<File> javaFiles) {
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