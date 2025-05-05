package io.github.Opsord.architecture_evaluator_backend.modules.parser.project_manager.services;

import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.dto.CustomCompilationUnitDTO;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.project_manager.dto.LayerAnnotation;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.project_manager.dto.ProjectDTO;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.CompilationUnitService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectScannerService {

    private static final Logger logger = LoggerFactory.getLogger(ProjectScannerService.class);
    private final CompilationUnitService compilationUnitService;

    public ProjectDTO scanAndOrganizeProject(String projectPath) throws IOException {
        List<File> javaFiles = new ArrayList<>();

        try (var paths = java.nio.file.Files.walk(java.nio.file.Paths.get(projectPath))) {
            paths.filter(java.nio.file.Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".java"))
                    .forEach(path -> javaFiles.add(path.toFile()));
        }

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

        // Organize it by layers
        ProjectDTO projectDTO = new ProjectDTO();
        projectDTO.setEntities(filterByLayer(compilationUnits, "entity"));
        projectDTO.setDocuments(filterByLayer(compilationUnits, "document"));
        projectDTO.setRepositories(filterByLayer(compilationUnits, "repository"));
        projectDTO.setServices(filterByLayer(compilationUnits, "service"));
        projectDTO.setControllers(filterByLayer(compilationUnits, "controller"));

        return projectDTO;
    }

    private List<CustomCompilationUnitDTO> filterByLayer(List<CustomCompilationUnitDTO> units, String layer) {
        return units.stream()
                .filter(dto -> dto.getAnnotations().stream()
                        .anyMatch(annotation -> {
                            try {
                                // Compare the layer with the annotation name
                                return LayerAnnotation.valueOf(layer.toUpperCase()).getAnnotation()
                                        .equalsIgnoreCase(annotation.getName());
                            } catch (IllegalArgumentException e) {
                                return false; // Handle the case where the layer does not match any enum value
                            }
                        }))
                .collect(Collectors.toList());
    }
}