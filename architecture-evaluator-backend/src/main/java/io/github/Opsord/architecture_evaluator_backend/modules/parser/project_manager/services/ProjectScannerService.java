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

    /**
     * Scans and organizes the project by delegating to separate methods.
     *
     * @param projectPath The path of the project to scan.
     * @return A ProjectDTO containing the organized data.
     * @throws IOException If an error occurs while reading files.
     */
    public ProjectDTO scanAndOrganizeProject(String projectPath) throws IOException {
        // Scan the project for Java files
        List<CustomCompilationUnitDTO> compilationUnits = scanProject(projectPath);
        // Organize the scanned files into layers
        return organizeProject(compilationUnits);
    }

    /**
     * Scans the project directory and parses Java files into CustomCompilationUnitDTOs.
     *
     * @param projectPath The path of the project to scan.
     * @return A list of CustomCompilationUnitDTOs.
     * @throws IOException If an error occurs while reading files.
     */
    private List<CustomCompilationUnitDTO> scanProject(String projectPath) throws IOException {
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

    /**
     * Organizes the parsed compilation units into layers and returns a ProjectDTO.
     *
     * @param compilationUnits The list of CustomCompilationUnitDTOs to organize.
     * @return A ProjectDTO with the units organized into layers.
     */
    private ProjectDTO organizeProject(List<CustomCompilationUnitDTO> compilationUnits) {
        ProjectDTO projectDTO = new ProjectDTO();
        projectDTO.setEntities(filterByLayer(compilationUnits, "entity"));
        projectDTO.setDocuments(filterByLayer(compilationUnits, "document"));
        projectDTO.setRepositories(filterByLayer(compilationUnits, "repository"));
        projectDTO.setServices(filterByLayer(compilationUnits, "service"));
        projectDTO.setControllers(filterByLayer(compilationUnits, "controller"));
        return projectDTO;
    }

    /**
     * Filters the list of CustomCompilationUnitDTOs by the specified layer.
     *
     * @param units The list of CustomCompilationUnitDTOs to filter.
     * @param layer The layer to filter by (e.g., "entity", "document", etc.).
     * @return A filtered list of CustomCompilationUnitDTOs that belong to the specified layer.
     */
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