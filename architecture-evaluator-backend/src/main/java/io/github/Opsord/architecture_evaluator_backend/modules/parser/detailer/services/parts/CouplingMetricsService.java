package io.github.Opsord.architecture_evaluator_backend.modules.parser.detailer.services.parts;

import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.dto.CustomCompilationUnitDTO;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.CompilationUnitService;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.detailer.dto.parts.CouplingMetricsDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

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
    public CouplingMetricsDTO calculateCouplingMetrics(CustomCompilationUnitDTO compilationUnit, List<CustomCompilationUnitDTO> allUnits) {
        CouplingMetricsDTO metrics = new CouplingMetricsDTO();
        metrics.setAfferentCoupling(calculateAfferentCoupling(compilationUnit, allUnits));
        metrics.setEfferentCoupling(calculateEfferentCoupling(compilationUnit, allUnits));
        metrics.setInstability(calculateInstability(metrics.getAfferentCoupling(), metrics.getEfferentCoupling()));
        return metrics;
    }

    public int calculateAfferentCoupling(CustomCompilationUnitDTO compilationUnit, List<CustomCompilationUnitDTO> allUnits) {
        List<String> importedClasses = compilationUnitService.getImportedClasses(compilationUnit, allUnits);
//        System.out.println("Original class: " + compilationUnit.getClassNames());
//        System.out.println("Imported classes: " + importedClasses);
        return importedClasses.size();
    }

    public int calculateEfferentCoupling(CustomCompilationUnitDTO compilationUnit, List<CustomCompilationUnitDTO> allUnits) {
        if (compilationUnit.getClassNames().isEmpty()) {
            return 0; // No classes to calculate efferent coupling for
        }
        List<String> dependentClasses = compilationUnitService.getDependentClasses(compilationUnit.getClassNames().get(0), allUnits);
//        System.out.println("Original class: " + compilationUnit.getClassNames());
//        System.out.println("Dependent classes: " + dependentClasses);
        return dependentClasses.size();
    }

    public double calculateInstability(int afferentCoupling, int efferentCoupling) {
        if (afferentCoupling + efferentCoupling == 0) {
            return 0; // Avoid division by zero
        }
        return (double) efferentCoupling / (afferentCoupling + efferentCoupling);
    }
}