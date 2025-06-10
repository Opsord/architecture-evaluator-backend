package io.github.Opsord.architecture_evaluator_backend.modules.parser.processor.services.analysis;

import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.instances.file_instance.FileInstance;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.instances.class_instance.ClassInstance;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.processor.dto.analysis.AnalysedClassInstance;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.processor.dto.analysis.parts.*;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.processor.services.analysis.parts.*;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.project_scanner.dto.pom.PomFileDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class FileAnalysisService {

    private final ProgramMetricsService programMetricsService;
    private final ComplexityMetricsService complexityMetricsService;
    private final CouplingMetricsService couplingMetricsService;
    private final CohesionMetricsService cohesionMetricsService;
    private final ImportClassifierService importClassifierService;

    /**
     * Analyzes a file instance and generates a detailed analysis.
     */
    public AnalysedClassInstance analyseCompUnit(FileInstance fileInstance,
                                                 List<FileInstance> projectCompUnitsWithoutTests,
                                                 String internalBasePackage,
                                                 PomFileDTO pomFileDTO,
                                                 boolean includeNonInternalDependencies) {
        AnalysedClassInstance detailedCompUnit = new AnalysedClassInstance();

        // Classify dependencies
        Map<ImportCategory, List<String>> classifiedDependencies = importClassifierService.classifyDependencies(
                pomFileDTO, fileInstance, internalBasePackage);
        detailedCompUnit.setClassifiedDependencies(classifiedDependencies);

        // Set basic metrics
        setBasicMetrics(detailedCompUnit, fileInstance);

        // For metrics, analyze the first class in the file (or adapt as needed)
        ClassInstance mainClass = fileInstance.getClasses() != null && !fileInstance.getClasses().isEmpty()
                ? fileInstance.getClasses().get(0)
                : null;

        if (mainClass != null) {
            // Generate and set program metrics
            ProgramMetricsDTO programMetrics = programMetricsService.generateProgramMetrics(mainClass);
            detailedCompUnit.setProgramMetrics(programMetrics);

            // Generate and set complexity metrics
            ComplexityMetricsDTO complexityMetrics = complexityMetricsService.calculateComplexityMetrics(mainClass);
            detailedCompUnit.setComplexityMetrics(complexityMetrics);

            // Generate and set coupling metrics
            // Adapt projectCompUnitsWithoutTests to a list of ClassInstance
            List<ClassInstance> allClasses = projectCompUnitsWithoutTests.stream()
                    .flatMap(f -> f.getClasses().stream())
                    .toList();
            CouplingMetricsDTO couplingMetrics = couplingMetricsService.calculateCouplingMetrics(
                    mainClass, allClasses, classifiedDependencies, includeNonInternalDependencies);
            detailedCompUnit.setCouplingMetrics(couplingMetrics);

            // Generate and set cohesion metrics
            CohesionMetricsDTO cohesionMetrics = cohesionMetricsService.calculateCohesionMetrics(mainClass);
            detailedCompUnit.setCohesionMetrics(cohesionMetrics);
        }

        return detailedCompUnit;
    }

    // -------------------------------------------------------------------------
    // Helper Methods
    // -------------------------------------------------------------------------

    private void setBasicMetrics(AnalysedClassInstance detailedCompUnit, FileInstance fileInstance) {
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
        detailedCompUnit.setClassCount(classCount);
        detailedCompUnit.setInterfaceCount(interfaceCount);
        detailedCompUnit.setStatementCount(statementCount);
    }
}