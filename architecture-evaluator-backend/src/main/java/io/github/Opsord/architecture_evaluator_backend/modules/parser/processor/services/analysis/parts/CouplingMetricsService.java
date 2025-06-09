package io.github.Opsord.architecture_evaluator_backend.modules.parser.processor.services.analysis.parts;

import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.instances.class_instance.ClassInstance;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.instances.file_instance.FileInstance;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.class_instance.ClassService;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.file_instance.FileInstanceService;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.processor.dto.analysis.parts.CouplingMetricsDTO;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.processor.dto.analysis.parts.ImportCategory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CouplingMetricsService {

    private final FileInstanceService fileInstanceService;
    private final ClassService classService;

    /**
     * Calculate the coupling metrics for a given compilation unit.
     *
     * @param classInstance The compilation unit to analyze.
     * @param projectFileInstances The list of all compilation units in the project.
     * @return The coupling metrics for the given compilation unit.
     */
    public CouplingMetricsDTO calculateCouplingMetrics(ClassInstance classInstance,
                                                       List<FileInstance> projectFileInstances,
                                                       Map<ImportCategory, List<String>> classifiedDependencies,
                                                       boolean includeNonInternalDependencies) {
        CouplingMetricsDTO metrics = new CouplingMetricsDTO();
        metrics.setEfferentCoupling(calculateEfferentCoupling(classInstance, projectFileInstances, classifiedDependencies, includeNonInternalDependencies));
        metrics.setAfferentCoupling(calculateAfferentCoupling(classInstance, projectFileInstances, classifiedDependencies, includeNonInternalDependencies));
        metrics.setInstability(calculateInstability(metrics.getAfferentCoupling(), metrics.getEfferentCoupling()));
        return metrics;
    }

    /**
     * Calculate the efferent coupling for a given compilation unit.
     * (i.e., the number of classes that this class depends on)
     *
     * @param classInstance The compilation unit to analyze.
     * @param projectFileInstances The list of all compilation units in the project.
     * @return The number of classes that this class depends on.
     */
    public int calculateEfferentCoupling(ClassInstance classInstance,
                                         List<FileInstance> projectFileInstances,
                                         Map<ImportCategory, List<String>> classifiedDependencies,
                                         boolean includeNonInternal) {
        List<String> importedClasses = fileInstanceService.getImportedClasses(classInstance, projectFileInstances);

        // Filter dependencies according to the category INTERNAL
        List<String> internalDependencies = classifiedDependencies.getOrDefault(ImportCategory.INTERNAL, List.of());
        if (!includeNonInternal) {
            importedClasses = importedClasses.stream()
                    .filter(internalDependencies::contains)
                    .toList();
        }
        return importedClasses.size();
    }

    /**
     * Calculate the afferent coupling for a given compilation unit.
     * (i.e., the number of classes that depend on this class)
     */
    public int calculateAfferentCoupling(ClassInstance classInstance,
                                         List<FileInstance> projectFileInstances,
                                         Map<ImportCategory, List<String>> classifiedDependencies,
                                         boolean includeNonInternal) {
        if (classInstance.getName() == null || classInstance.getName().isEmpty()) {
            return 0; // No class name available
        }
        List<String> dependentClasses = fileInstanceService.getDependentClassesFromFile(classInstance.getName(), projectFileInstances);
        List<String> internalDependencies = classifiedDependencies.getOrDefault(ImportCategory.INTERNAL, List.of());
        if (!includeNonInternal) {
            dependentClasses = dependentClasses.stream()
                    .filter(internalDependencies::contains)
                    .toList();
        }
        return dependentClasses.size();
    }

    public double calculateInstability(int afferentCoupling, int efferentCoupling) {
        if (afferentCoupling + efferentCoupling == 0) {
            return 0; // Avoid division by zero
        }
        return (double) efferentCoupling / (afferentCoupling + efferentCoupling);
    }
}