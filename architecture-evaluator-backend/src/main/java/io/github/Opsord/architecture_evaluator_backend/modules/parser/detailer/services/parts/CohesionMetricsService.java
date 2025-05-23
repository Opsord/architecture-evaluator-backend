package io.github.Opsord.architecture_evaluator_backend.modules.parser.detailer.services.parts;

import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.dto.CustomCompilationUnitDTO;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.dto.parts.VariableDTO;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.dto.parts.method.MethodDTO;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.detailer.dto.parts.CohesionMetricsDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;

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
//        metrics.setLackOfCohesion2(calculateLCOM2(compilationUnit));

        // Calculate CAM (Cohesion Among Methods)
//        metrics.setCohesionAmongMethods(calculateCAM(compilationUnit));

        return metrics;
    }

    private int calculateCBO(CustomCompilationUnitDTO compilationUnit) {
        // Count the number of unique external classes referenced by this class
        return new HashSet<>(compilationUnit.getImportedPackages()).size();
    }

    private List<MethodDTO> getMethods(CustomCompilationUnitDTO compilationUnit) {
        return compilationUnit.getMethods();
    }

    private List<VariableDTO> getVariables(CustomCompilationUnitDTO compilationUnit) {
        return compilationUnit.getVariables();
    }

    private double calculateLCOM1(CustomCompilationUnitDTO compilationUnit) {
        List<MethodDTO> methods = getMethods(compilationUnit);
        List<VariableDTO> variables = getVariables(compilationUnit);

        boolean[][] relationMatrix = buildRelationMatrix(methods, variables);

        int connectedPairs = 0;
        int disconnectedPairs = 0;

        int methodCount = relationMatrix.length;
        for (int i = 0; i < methodCount; i++) {
            for (int j = i + 1; j < methodCount; j++) {
                if (relationMatrix[i][j]) {
                    connectedPairs++;
                } else {
                    disconnectedPairs++;
                }
            }
        }

        // Calcular LCOM1
        return disconnectedPairs > connectedPairs
                ? (double) (disconnectedPairs - connectedPairs) / methodCount
                : 0.0;
    }

    private boolean[][] buildRelationMatrix(List<MethodDTO> methods, List<VariableDTO> variables) {
        int methodCount = methods.size();
        boolean[][] relationMatrix = new boolean[methodCount][methodCount];

        for (int i = 0; i < methodCount; i++) {
            for (int j = i + 1; j < methodCount; j++) {
                if (shareInstanceVariable(methods.get(i), methods.get(j), variables)) {
                    relationMatrix[i][j] = true;
                    relationMatrix[j][i] = true;
                }
            }
        }
        return relationMatrix;
    }

    private boolean shareInstanceVariable(MethodDTO method1, MethodDTO method2, List<VariableDTO> variables) {
        List<String> method1Variables = method1.getMethodVariables().stream()
                .map(VariableDTO::getName)
                .toList();

        List<String> method2Variables = method2.getMethodVariables().stream()
                .map(VariableDTO::getName)
                .toList();

        for (String var : method1Variables) {
            if (method2Variables.contains(var)) {
                return true;
            }
        }
        return false;
    }
}