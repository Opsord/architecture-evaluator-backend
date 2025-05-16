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

    public List<CustomCompilationUnitDTO> scanProject(String filePath) throws IOException {
        File projectRoot = scanningService.findProjectRoot(new File(filePath));
        if (projectRoot == null) {
            logger.warn("No valid project root found for path: {}", filePath);
            return List.of();
        }

        File srcFolder = new File(projectRoot, "src");
        if (!srcFolder.exists() || !srcFolder.isDirectory()) {
            logger.warn("No 'src' folder found in project root: {}", projectRoot.getAbsolutePath());
            return List.of();
        }

        List<File> javaFiles = scanningService.scanSrcFolder(srcFolder);
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