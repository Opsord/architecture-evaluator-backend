package io.github.Opsord.architecture_evaluator_backend.modules.parser.detailer.services.parts;

import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.dto.CustomCompilationUnitDTO;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.dto.parts.VariableDTO;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.dto.parts.method.MethodDTO;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.dto.parts.method.parts.StatementsInfo;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.dto.parts.statement.StatementDTO;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.dto.parts.statement.StatementType;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.detailer.dto.parts.CohesionMetricsDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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
        metrics.setLackOfCohesion3(calculateLCOM3(compilationUnit));
        metrics.setLackOfCohesion4(calculateLCOM4(compilationUnit));
        metrics.setLackOfCohesion5(calculateLCOM5(compilationUnit));

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

    private List<String> getCalledMethodNames(MethodDTO method) {
        List<String> calledMethods = new ArrayList<>();
        StatementsInfo statementsInfo = method.getStatementsInfo();
        if (statementsInfo != null && statementsInfo.getStatements() != null) {
            for (StatementDTO statement : statementsInfo.getStatements()) {
                // Solo procesar ExpressionStmt (ejemplo: "m3();")
                if (statement.getType() == StatementType.EXPRESSION) {
                    String methodName = extractMethodName(statement.getStructure());
                    if (methodName != null) {
                        calledMethods.add(methodName);
                    }
                }
            }
        }
        return calledMethods;
    }

    private String extractMethodName(String structure) {
        // Example: "m3();" or "m3();"
        // Use regex to extract the method name from the structure
        if (structure.matches(".*\\w+\\(\\)\\s*;")) { // Match with "methods();"
            return structure.replaceAll("\\s*\\(\\)\\s*;", "");
        }
        return null;
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
    private double calculateLCOM2(CustomCompilationUnitDTO compilationUnit) {
        List<MethodDTO> methods = getMethods(compilationUnit);
        List<VariableDTO> variables = getInstanceVariables(compilationUnit);

        int M = methods.size();
        int A = variables.size();

        if (M == 0 || A == 0) return 0.0;

        int[] MAj = new int[A]; // Número de métodos que acceden a cada atributo
        for (MethodDTO method : methods) {
            List<String> methodVars = method.getMethodVariables().stream()
                    .filter(v -> "instance".equals(v.getScope()))
                    .map(VariableDTO::getName)
                    .toList();
            for (int j = 0; j < A; j++) {
                if (methodVars.contains(variables.get(j).getName())) {
                    MAj[j]++;
                }
            }
        }

        int sumMAj = Arrays.stream(MAj).sum();
        double lcom2 = 1.0 - (sumMAj / (double) (M * A));
        return Math.max(lcom2, 0.0); // Asegurar valor no negativo
    }

    // -------------------------------------------------------------------------
    // LCOM3 Calculation
    // -------------------------------------------------------------------------

    /**
     * Calculate LCOM3 (Lack of Cohesion Metric 3).
     *
     * @param compilationUnit The compilation unit.
     * @return LCOM3 value.
     */
    private int calculateLCOM3(CustomCompilationUnitDTO compilationUnit) {
        List<MethodDTO> methods = getMethods(compilationUnit);
        List<VariableDTO> variables = getInstanceVariables(compilationUnit);
        boolean[][] adj = buildRelationMatrix(methods, variables);
        int components = countConnectedComponents(adj);
        return components - 1; // LCOM3 = Componentes - 1
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
        boolean[][] adj = buildRelationMatrixForLCOM4(methods, compilationUnit);
        int components = countConnectedComponents(adj);
        return components - 1; // LCOM4 = Componentes - 1
    }

    private boolean[][] buildRelationMatrixForLCOM4(List<MethodDTO> methods, CustomCompilationUnitDTO compilationUnit) {
        int n = methods.size();
        boolean[][] adj = new boolean[n][n];
        List<VariableDTO> variables = getInstanceVariables(compilationUnit);

        // Conexiones por variables compartidas
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                if (shareInstanceVariable(methods.get(i), methods.get(j), variables)) {
                    adj[i][j] = adj[j][i] = true;
                }
            }
        }

        // Conexiones por invocaciones de métodos
        for (int i = 0; i < n; i++) {
            List<String> calledMethods = getCalledMethodNames(methods.get(i));
            for (String callee : calledMethods) {
                for (int j = 0; j < n; j++) {
                    if (methods.get(j).getName().equals(callee)) {
                        adj[i][j] = adj[j][i] = true; // Grafo no dirigido
                    }
                }
            }
        }

        return adj;
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
    // LCOM5 Calculation
    // -------------------------------------------------------------------------

    /**
     * Calculate LCOM5 (Lack of Cohesion Metric 5).
     *
     * @param compilationUnit The compilation unit.
     * @return LCOM5 value.
     */
    private double calculateLCOM5(CustomCompilationUnitDTO compilationUnit) {
        List<MethodDTO> methods = getMethods(compilationUnit);
        List<VariableDTO> variables = getInstanceVariables(compilationUnit);

        int M = methods.size();
        int A = variables.size();

        if (M == 0 || A == 0) return 0.0;

        int[] MAj = new int[A];
        for (MethodDTO method : methods) {
            List<String> methodVars = method.getMethodVariables().stream()
                    .filter(v -> "instance".equals(v.getScope()))
                    .map(VariableDTO::getName)
                    .toList();
            for (int j = 0; j < A; j++) {
                if (methodVars.contains(variables.get(j).getName())) {
                    MAj[j]++;
                }
            }
        }

        int sumMAj = Arrays.stream(MAj).sum();
        double lcom5 = (1.0 - (sumMAj / (double) (M * A))) * M;
        return Math.max(lcom5, 0.0);
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
        List<String> instanceVarNames = variables.stream()
                .map(VariableDTO::getName)
                .toList();

        List<String> method1Vars = method1.getMethodVariables().stream()
                .map(VariableDTO::getName)
                .filter(instanceVarNames::contains) // Filtro clave
                .toList();

        List<String> method2Vars = method2.getMethodVariables().stream()
                .map(VariableDTO::getName)
                .filter(instanceVarNames::contains) // Filtro clave
                .toList();

        for (String var : method1Vars) {
            if (method2Vars.contains(var)) {
                return true;
            }
        }
        return false;
    }
}