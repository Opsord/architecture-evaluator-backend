package io.github.Opsord.architecture_evaluator_backend.modules.parser.detailer.services.parts;

import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.dto.CustomCompilationUnitDTO;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.CompilationUnitService;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.detailer.dto.parts.CouplingMetricsDTO;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.detailer.dto.parts.ImportCategory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CouplingMetricsService {

    private final CompilationUnitService compilationUnitService;

    /**
     * Calculate the coupling metrics for a given compilation unit.
     *
     * @param compilationUnit The compilation unit to analyze.
     * @param allUnits The list of all compilation units in the project.
     * @return The coupling metrics for the given compilation unit.
     */
    public CouplingMetricsDTO calculateCouplingMetrics(CustomCompilationUnitDTO compilationUnit,
                                                       List<CustomCompilationUnitDTO> allUnits,
                                                       Map<ImportCategory, List<String>> classifiedDependencies,
                                                       boolean includeNonInternalDependencies) {
        CouplingMetricsDTO metrics = new CouplingMetricsDTO();
        metrics.setEfferentCoupling(calculateEfferentCoupling(compilationUnit, allUnits, classifiedDependencies, includeNonInternalDependencies));
        metrics.setAfferentCoupling(calculateAfferentCoupling(compilationUnit, allUnits, classifiedDependencies, includeNonInternalDependencies));
        metrics.setInstability(calculateInstability(metrics.getAfferentCoupling(), metrics.getEfferentCoupling()));
        return metrics;
    }

    /**
     * Calculate the efferent coupling for a given compilation unit.
     * (i.e., the number of classes that this class depends on)
     *
     * @param compilationUnit The compilation unit to analyze.
     * @param projectCompUnits The list of all compilation units in the project.
     * @return The number of classes that this class depends on.
     */
    public int calculateEfferentCoupling(CustomCompilationUnitDTO compilationUnit,
                                         List<CustomCompilationUnitDTO> projectCompUnits,
                                         Map<ImportCategory, List<String>> classifiedDependencies,
                                         boolean includeNonInternal) {
        List<String> importedClasses = compilationUnitService.getImportedClasses(compilationUnit, projectCompUnits);

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
     *
     * @param compilationUnit The compilation unit to analyze.
     * @param allUnits The list of all compilation units in the project.
     * @return The number of classes that depend on this class.
     */
    public int calculateAfferentCoupling(CustomCompilationUnitDTO compilationUnit,
                                         List<CustomCompilationUnitDTO> allUnits,
                                         Map<ImportCategory, List<String>> classifiedDependencies,
                                         boolean includeNonInternal) {
        if (compilationUnit.getClassName().isEmpty()) {
            return 0; // No class name available
        }
//        System.out.println("Class name: " + compilationUnit.getClassName().get(0));
        List<String> dependentClasses = compilationUnitService.getDependentClasses(compilationUnit.getClassName().get(0), allUnits);
//        System.out.println("Dependent classes: " + dependentClasses);
        List<String> internalDependencies = classifiedDependencies.getOrDefault(ImportCategory.INTERNAL, List.of());
//        System.out.printf("Internal dependencies: %s%n", internalDependencies);
        return dependentClasses.size();
    }

    public double calculateInstability(int afferentCoupling, int efferentCoupling) {
        if (afferentCoupling + efferentCoupling == 0) {
            return 0; // Avoid division by zero
        }
        return (double) efferentCoupling / (afferentCoupling + efferentCoupling);
    }
}