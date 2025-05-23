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
        metrics.setLackOfCohesion2(calculateLCOM2(compilationUnit));

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

    private List<VariableDTO> getInstanceVariables(CustomCompilationUnitDTO compilationUnit) {
        return compilationUnit.getVariables().stream()
                .filter(variable -> "instance".equals(variable.getScope())) // Filtrar por scope "instance"
                .toList();
    }

    private int calculateLCOM1(CustomCompilationUnitDTO compilationUnit) {
        List<MethodDTO> methods = getMethods(compilationUnit);
        List<VariableDTO> variables = getInstanceVariables(compilationUnit); // Filtrar variables de tipo instance

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

        return Math.max(disconnectedPairs - connectedPairs, 0);
    }

    private int calculateLCOM2(CustomCompilationUnitDTO compilationUnit) {
        List<MethodDTO> methods = getMethods(compilationUnit);
        List<VariableDTO> variables = getInstanceVariables(compilationUnit); // Filtrar variables de tipo instance

        int[][] accessMatrix = buildAccessMatrix(methods, variables);

        int sumMA = 0;
        for (int[] matrix : accessMatrix) {
            for (int i : matrix) {
                if (i > 0) {
                    sumMA++;
                }
            }
        }

        int M = methods.size();
        int A = variables.size();

        if (M == 0 || A == 0) {
            return 0;
        }

        return (int) Math.round(1.0 - ((double) sumMA / (M * A)));
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
//        System.out.println("Relation Matrix:");
//        for (boolean[] row : relationMatrix) {
//            for (boolean value : row) {
//                System.out.print(value ? "1 " : "0 ");
//            }
//            System.out.println();
//        }
        return relationMatrix;
    }

    private int[][] buildAccessMatrix(List<MethodDTO> methods, List<VariableDTO> variables) {
        int[][] accessMatrix = new int[methods.size()][variables.size()];

        for (int i = 0; i < methods.size(); i++) {
            MethodDTO method = methods.get(i);
            List<String> methodVariables = method.getMethodVariables().stream()
                    .map(VariableDTO::getName)
                    .toList();

            for (int j = 0; j < variables.size(); j++) {
                if (methodVariables.contains(variables.get(j).getName())) {
                    accessMatrix[i][j] = 1; // Mark access
                }
            }
        }

        return accessMatrix;
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