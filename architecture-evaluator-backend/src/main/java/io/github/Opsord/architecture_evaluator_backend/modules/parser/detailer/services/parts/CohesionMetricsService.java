package io.github.Opsord.architecture_evaluator_backend.modules.parser.detailer.services.parts;

import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.dto.CustomCompilationUnitDTO;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.dto.parts.VariableDTO;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.dto.parts.method.MethodDTO;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.detailer.dto.parts.CohesionMetricsDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CohesionMetricsService {

    /**
     * Main method to calculate cohesion metrics for a given compilation unit.
     *
     * @param compilationUnit The compilation unit to analyze.
     * @return The cohesion metrics for the given compilation unit.
     */
    public CohesionMetricsDTO calculateCohesionMetrics(CustomCompilationUnitDTO compilationUnit) {
        CohesionMetricsDTO metrics = new CohesionMetricsDTO();

        // Calculate LCOM (Lack of Cohesion)
        metrics.setLackOfCohesion1(calculateLCOM1(compilationUnit));
        metrics.setLackOfCohesion2(calculateLCOM2(compilationUnit));
        metrics.setLackOfCohesion4(calculateLCOM4(compilationUnit));

        return metrics;
    }

    // -------------------------------------------------------------------------
    // Helper Methods to Extract Data
    // -------------------------------------------------------------------------

    /**
     * Get all methods from the compilation unit.
     *
     * @param compilationUnit The compilation unit.
     * @return List of methods.
     */
    private List<MethodDTO> getMethods(CustomCompilationUnitDTO compilationUnit) {
        return compilationUnit.getMethods();
    }

    /**
     * Get all instance variables from the compilation unit.
     *
     * @param compilationUnit The compilation unit.
     * @return List of instance variables.
     */
    private List<VariableDTO> getInstanceVariables(CustomCompilationUnitDTO compilationUnit) {
        return compilationUnit.getVariables().stream()
                .filter(variable -> "instance".equals(variable.getScope())) // Filter by scope "instance"
                .toList();
    }

    // -------------------------------------------------------------------------
    // LCOM1 Calculation
    // -------------------------------------------------------------------------

    /**
     * Calculate LCOM1 (Lack of Cohesion Metric 1).
     *
     * @param compilationUnit The compilation unit.
     * @return LCOM1 value.
     */
    private int calculateLCOM1(CustomCompilationUnitDTO compilationUnit) {
        List<MethodDTO> methods = getMethods(compilationUnit);
        List<VariableDTO> variables = getInstanceVariables(compilationUnit);

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

    // -------------------------------------------------------------------------
    // LCOM2 Calculation
    // -------------------------------------------------------------------------

    /**
     * Calculate LCOM2 (Lack of Cohesion Metric 2).
     *
     * @param compilationUnit The compilation unit.
     * @return LCOM2 value.
     */
    private int calculateLCOM2(CustomCompilationUnitDTO compilationUnit) {
        List<MethodDTO> methods = getMethods(compilationUnit);
        List<VariableDTO> variables = getInstanceVariables(compilationUnit);

        int M = methods.size();
        int A = variables.size();

        if (M == 0 || A <= 1) { // Special cases
            return 0;
        }

        int[] MAj = new int[A]; // MAj[j] = Number of methods accessing variable j
        for (MethodDTO method : methods) {
            List<String> methodVars = method.getMethodVariables().stream()
                    .filter(v -> "instance".equals(v.getScope()))
                    .map(VariableDTO::getName)
                    .toList();

            for (int j = 0; j < variables.size(); j++) {
                if (methodVars.contains(variables.get(j).getName())) {
                    MAj[j]++;
                }
            }
        }

        int sumMAj = Arrays.stream(MAj).sum();
        double numerator = sumMAj - M;
        double denominator = M * (A - 1);
        double lcom2 = 1.0 - (numerator / denominator);

        lcom2 = Math.max(0, Math.min(lcom2, 1)); // Ensure range [0, 1]
        return (int) Math.round(lcom2 * 100); // Example: 0.75 → 75%
    }

    // -------------------------------------------------------------------------
    // LCOM4 Calculation
    // -------------------------------------------------------------------------

    /**
     * Calculate LCOM4 (Lack of Cohesion Metric 4).
     *
     * @param compilationUnit The compilation unit.
     * @return LCOM4 value.
     */
    private int calculateLCOM4(CustomCompilationUnitDTO compilationUnit) {
        List<MethodDTO> methods = getMethods(compilationUnit);
        List<VariableDTO> variables = getInstanceVariables(compilationUnit);
        boolean[][] adj = buildRelationMatrix(methods, variables);
        return countConnectedComponents(adj);
    }

    /**
     * Count the number of connected components in the relation matrix.
     *
     * @param adj The adjacency matrix.
     * @return Number of connected components.
     */
    private int countConnectedComponents(boolean[][] adj) {
        int n = adj.length;
        boolean[] visited = new boolean[n];
        int components = 0;
        for (int i = 0; i < n; i++) {
            if (!visited[i]) {
                dfs(i, adj, visited);
                components++;
            }
        }
        return components;
    }

    /**
     * Perform a depth-first search (DFS) on the relation matrix.
     *
     * @param u       Current node.
     * @param adj     Adjacency matrix.
     * @param visited Visited nodes array.
     */
    private void dfs(int u, boolean[][] adj, boolean[] visited) {
        visited[u] = true;
        for (int v = 0; v < adj.length; v++) {
            if (adj[u][v] && !visited[v]) {
                dfs(v, adj, visited);
            }
        }
    }

    // -------------------------------------------------------------------------
    // Relation Matrix Construction
    // -------------------------------------------------------------------------

    /**
     * Build the relation matrix for methods and instance variables.
     *
     * @param methods   List of methods.
     * @param variables List of instance variables.
     * @return Relation matrix.
     */
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

    /**
     * Check if two methods share at least one instance variable.
     *
     * @param method1   First method.
     * @param method2   Second method.
     * @param variables List of instance variables.
     * @return True if they share an instance variable, false otherwise.
     */
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