package io.github.Opsord.architecture_evaluator_backend.modules.parser.processor.services;

import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.instances.file_instance.FileInstance;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.instances.class_instance.ClassInstance;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.processor.dto.ClassAnalysis;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.processor.dto.parts.ImportCategory;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.processor.services.parts.ImportClassifierService;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.project_scanner.dto.pom.PomFileDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Service for analyzing a Java file (compilation unit) and producing a {@link ClassAnalysis}
 * with metrics and dependency classification.
 */
@Service
@RequiredArgsConstructor
public class FileAnalysisService {

    private final ClassAnalysisService classAnalysisService;
    private final ImportClassifierService importClassifierService;

    /**
     * Analyzes a {@link FileInstance} (Java file) and returns a {@link ClassAnalysis} with metrics and dependencies.
     *
     * @param fileInstance The file to analyze.
     * @param projectCompUnitsWithoutTests List of all project files (excluding tests).
     * @param internalBasePackage The base package of the internal project.
     * @param pomFileDTO The POM file data transfer object.
     * @param includeNonInternalDependencies Whether to include non-internal dependencies in coupling metrics.
     * @return The analysis result for the main class in the file, or an empty analysis if no class is present.
     */
    public ClassAnalysis analyseFileInstance(
            FileInstance fileInstance,
            List<FileInstance> projectCompUnitsWithoutTests,
            String internalBasePackage,
            PomFileDTO pomFileDTO,
            boolean includeNonInternalDependencies
    ) {
        int[] metrics = computeBasicMetrics(fileInstance);
        ClassInstance mainClass = extractMainClass(fileInstance);

        if (mainClass == null) {
            return buildEmptyAnalysis(metrics);
        }

        Map<ImportCategory, List<String>> classifiedDependencies = classifyFileDependencies(
                pomFileDTO, fileInstance, internalBasePackage
        );
        List<ClassInstance> allClasses = extractAllProjectClasses(projectCompUnitsWithoutTests);

        ClassAnalysis analysis = classAnalysisService.analyseClassInstance(
                mainClass,
                allClasses,
                classifiedDependencies,
                includeNonInternalDependencies
        );
        analysis.setClassCount(metrics[0]);
        analysis.setInterfaceCount(metrics[1]);
        analysis.setStatementCount(metrics[2]);
        return analysis;
    }

    // Returns int[]{classCount, interfaceCount, statementCount}
    private int[] computeBasicMetrics(FileInstance fileInstance) {
        int classCount = 0;
        int interfaceCount = 0;
        int statementCount = 0;
        if (fileInstance.getClasses() != null) {
            classCount = (int) fileInstance.getClasses().stream()
                    .filter(c -> c.getJavaFileType() != null && c.getJavaFileType().name().equals("CLASS"))
                    .count();
            interfaceCount = (int) fileInstance.getClasses().stream()
                    .filter(c -> c.getJavaFileType() != null && c.getJavaFileType().name().equals("INTERFACE"))
                    .count();
            statementCount = fileInstance.getClasses().stream()
                    .mapToInt(c -> c.getStatements() != null ? c.getStatements().size() : 0)
                    .sum();
        }
        return new int[]{classCount, interfaceCount, statementCount};
    }

    private ClassInstance extractMainClass(FileInstance fileInstance) {
        return (fileInstance.getClasses() != null && !fileInstance.getClasses().isEmpty())
                ? fileInstance.getClasses().get(0)
                : null;
    }

    private Map<ImportCategory, List<String>> classifyFileDependencies(
            PomFileDTO pomFileDTO,
            FileInstance fileInstance,
            String internalBasePackage
    ) {
        return importClassifierService.classifyDependencies(pomFileDTO, fileInstance, internalBasePackage);
    }

    private List<ClassInstance> extractAllProjectClasses(List<FileInstance> projectCompUnitsWithoutTests) {
        return projectCompUnitsWithoutTests.stream()
                .flatMap(f -> f.getClasses().stream())
                .toList();
    }

    private ClassAnalysis buildEmptyAnalysis(int[] metrics) {
        ClassAnalysis empty = new ClassAnalysis();
        empty.setClassCount(metrics[0]);
        empty.setInterfaceCount(metrics[1]);
        empty.setStatementCount(metrics[2]);
        return empty;
    }
}