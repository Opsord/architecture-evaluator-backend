package io.github.Opsord.architecture_evaluator_backend.modules.parser.processor.services.parts;

import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.instances.class_instance.ClassInstance;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.class_instance.ClassService;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.processor.dto.parts.CouplingMetricsDTO;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.processor.dto.parts.ImportCategory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Service for calculating coupling metrics (afferent, efferent, instability) for a class.
 */
@Service
@RequiredArgsConstructor
public class CouplingMetricsService {

    private final ClassService classService;

    /**
     * Calculates the coupling metrics for a given class.
     *
     * @param classInstance The class to analyze.
     * @param projectClassInstances The list of all classes in the project.
     * @param classifiedDependencies Map of import categories to dependency names.
     * @param includeNonInternalDependencies Whether to include non-internal dependencies in the calculation.
     * @return The coupling metrics for the given class.
     */
    public CouplingMetricsDTO calculateCouplingMetrics(ClassInstance classInstance,
                                                       List<ClassInstance> projectClassInstances,
                                                       Map<ImportCategory, List<String>> classifiedDependencies,
                                                       boolean includeNonInternalDependencies) {
        CouplingMetricsDTO metrics = new CouplingMetricsDTO();
        metrics.setEfferentCoupling(calculateEfferentCoupling(classInstance, projectClassInstances, classifiedDependencies, includeNonInternalDependencies));
        metrics.setAfferentCoupling(calculateAfferentCoupling(classInstance, projectClassInstances, classifiedDependencies, includeNonInternalDependencies));
        metrics.setInstability(calculateInstability(metrics.getAfferentCoupling(), metrics.getEfferentCoupling()));
        return metrics;
    }

    /**
     * Calculates the efferent coupling (Ce) for a given class.
     * Efferent coupling is the number of classes that this class depends on.
     *
     * @param classInstance The class to analyze.
     * @param projectClassInstances The list of all classes in the project.
     * @param classifiedDependencies Map of import categories to dependency names.
     * @param includeNonInternal Whether to include non-internal dependencies in the calculation.
     * @return The number of classes that this class depends on.
     */
    public int calculateEfferentCoupling(ClassInstance classInstance,
                                         List<ClassInstance> projectClassInstances,
                                         Map<ImportCategory, List<String>> classifiedDependencies,
                                         boolean includeNonInternal) {
        List<String> importedClasses = classService.getImportedClasses(classInstance, projectClassInstances);

        List<String> internalDependencies = classifiedDependencies.getOrDefault(ImportCategory.INTERNAL, List.of());
        if (!includeNonInternal) {
            importedClasses = importedClasses.stream()
                    .filter(internalDependencies::contains)
                    .toList();
        }
        return importedClasses.size();
    }

    /**
     * Calculates the afferent coupling (Ca) for a given class.
     * Afferent coupling is the number of classes that depend on this class.
     *
     * @param classInstance The class to analyze.
     * @param projectClassInstances The list of all classes in the project.
     * @param classifiedDependencies Map of import categories to dependency names.
     * @param includeNonInternal Whether to include non-internal dependencies in the calculation.
     * @return The number of classes that depend on this class.
     */
    public int calculateAfferentCoupling(ClassInstance classInstance,
                                         List<ClassInstance> projectClassInstances,
                                         Map<ImportCategory, List<String>> classifiedDependencies,
                                         boolean includeNonInternal) {
        if (classInstance.getName() == null || classInstance.getName().isEmpty()) {
            return 0;
        }
        List<String> dependentClasses = classService.getDependentClasses(classInstance.getName(), projectClassInstances);
        List<String> internalDependencies = classifiedDependencies.getOrDefault(ImportCategory.INTERNAL, List.of());
        if (!includeNonInternal) {
            dependentClasses = dependentClasses.stream()
                    .filter(internalDependencies::contains)
                    .toList();
        }
        return dependentClasses.size();
    }

    /**
     * Calculates the instability metric for a class.
     * Instability = Ce / (Ca + Ce)
     *
     * @param afferentCoupling The afferent coupling (Ca).
     * @param efferentCoupling The efferent coupling (Ce).
     * @return The instability metric, or 0 if both couplings are zero.
     */
    public double calculateInstability(int afferentCoupling, int efferentCoupling) {
        if (afferentCoupling + efferentCoupling == 0) {
            return 0;
        }
        return (double) efferentCoupling / (afferentCoupling + efferentCoupling);
    }
}