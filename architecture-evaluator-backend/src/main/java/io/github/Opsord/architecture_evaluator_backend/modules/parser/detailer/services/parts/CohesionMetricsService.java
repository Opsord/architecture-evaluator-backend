package io.github.Opsord.architecture_evaluator_backend.modules.parser.detailer.services.parts;

import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.dto.CustomCompilationUnitDTO;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.dto.parts.VariableDTO;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.dto.parts.method.MethodDTO;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.detailer.dto.parts.CohesionMetricsDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CohesionMetricsService {

    /**
     * Calculate the cohesion metrics for a given compilation unit.
     *
     * @param compilationUnit The compilation unit to analyze.
     * @return The cohesion metrics for the given compilation unit.
     */
    public CohesionMetricsDTO calculateCohesionMetrics(CustomCompilationUnitDTO compilationUnit) {
        CohesionMetricsDTO metrics = new CohesionMetricsDTO();

        // Calculate CBO (Coupling Between Objects)
        metrics.setCouplingBetweenObjects(calculateCBO(compilationUnit));

        // Calculate LCOM (Lack of Cohesion)
        metrics.setLackOfCohesion1(calculateLCOM1(compilationUnit));
        metrics.setLackOfCohesion2(calculateLCOM4(compilationUnit));

        // Calculate CAM (Cohesion Among Methods)
        metrics.setCohesionAmongMethods(calculateCAM(compilationUnit));

        return metrics;
    }

    private int calculateCBO(CustomCompilationUnitDTO compilationUnit) {
        // Count the number of unique external classes referenced by this class
        return new HashSet<>(compilationUnit.getImportedPackages()).size();
    }

    private double calculateLCOM1(CustomCompilationUnitDTO compilationUnit) {
        List<MethodDTO> methods = compilationUnit.getMethods();
        List<VariableDTO> variables = compilationUnit.getVariables();

        int methodPairsWithoutSharedAttributes = 0;
        int totalMethodPairs = 0;

        for (int i = 0; i < methods.size(); i++) {
            for (int j = i + 1; j < methods.size(); j++) {
                totalMethodPairs++;
                if (!methodsShareAttributes(methods.get(i), methods.get(j), variables)) {
                    methodPairsWithoutSharedAttributes++;
                }
            }
        }

        if (totalMethodPairs == 0) {
            return 0.0;
        }

        return (double) methodPairsWithoutSharedAttributes / totalMethodPairs;
    }

    private double calculateLCOM4(CustomCompilationUnitDTO compilationUnit) {
        // Placeholder for LCOM4 calculation (requires graph-based analysis of method connectivity)
        return 0.0;
    }

    private double calculateCAM(CustomCompilationUnitDTO compilationUnit) {
        List<MethodDTO> methods = compilationUnit.getMethods();
        List<VariableDTO> variables = compilationUnit.getVariables();

        int relatedMethodPairs = 0;
        int totalMethodPairs = 0;

        for (int i = 0; i < methods.size(); i++) {
            for (int j = i + 1; j < methods.size(); j++) {
                totalMethodPairs++;
                if (methodsShareAttributes(methods.get(i), methods.get(j), variables)) {
                    relatedMethodPairs++;
                }
            }
        }

        if (totalMethodPairs == 0) {
            return 0.0;
        }

        return (double) relatedMethodPairs / totalMethodPairs;
    }

    private boolean methodsShareAttributes(MethodDTO method1, MethodDTO method2, List<VariableDTO> variables) {
        Set<String> method1Attributes = extractAttributesUsedByMethod(method1, variables);
        Set<String> method2Attributes = extractAttributesUsedByMethod(method2, variables);

        // Check if there is any overlap between the attributes used by the two methods
        method1Attributes.retainAll(method2Attributes);
        return !method1Attributes.isEmpty();
    }

    private Set<String> extractAttributesUsedByMethod(MethodDTO method, List<VariableDTO> variables) {
        // Placeholder: Extract attributes used by the method (requires detailed analysis of method body)
        return new HashSet<>();
    }
}