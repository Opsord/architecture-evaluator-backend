package io.github.opsord.architecture_evaluator_backend.modules.parser.processor.services.parts;

import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.instances.class_instance.ClassInstance;
import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.instances.class_instance.parts.VariableInstance;
import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.instances.class_instance.parts.method.MethodInstance;
import io.github.opsord.architecture_evaluator_backend.modules.parser.processor.dto.parts.CohesionMetricsDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CohesionMetricsService {

    private static final String INSTANCE_SCOPE = "instance";

    public CohesionMetricsDTO calculateCohesionMetrics(ClassInstance classInstance) {
        CohesionMetricsDTO metrics = new CohesionMetricsDTO();

        // Cache core data
        List<MethodInstance> methods = classInstance.getMethods();
        List<VariableInstance> vars = classInstance.getClassVariables().stream()
                .filter(v -> INSTANCE_SCOPE.equals(v.getScope()))
                .toList();

        // Precompute field-sharing adjacency
        boolean[][] fieldGraph = buildFieldAdjacency(methods, vars);

        // Section: LCOM1
        metrics.setLackOfCohesion1(calculateLCOM1(fieldGraph));
        // Section: LCOM2
        metrics.setLackOfCohesion2(calculateLCOM2(fieldGraph));
        // Section: LCOM3
        metrics.setLackOfCohesion3(calculateLCOM3(fieldGraph));
        // Section: LCOM4
        metrics.setLackOfCohesion4(calculateLCOM3(mergeCallGraph(fieldGraph, methods)));
        // Section: LCOM5
        metrics.setLackOfCohesion5(calculateLCOM5(methods, vars));

        return metrics;
    }

    // -------------------------------------------------------------------------
    // LCOM1
    // -------------------------------------------------------------------------

    /**
     * Calculates LCOM1: number of method pairs that do NOT share instance variables.
     *
     * @param graph Adjacency matrix where graph[i][j] = true if methods i and j share at least one instance field.
     * @return The count of method pairs without shared attributes.
     */
    private int calculateLCOM1(boolean[][] graph) {
        int disconnectedPairs = 0;
        int n = graph.length;

        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                if (!graph[i][j]) {
                    disconnectedPairs++;
                }
            }
        }

        return disconnectedPairs;
    }

    // -------------------------------------------------------------------------
    // LCOM2
    // -------------------------------------------------------------------------

    /**
     * Calculates LCOM2 as defined by Chidamber & Kemerer (1994):
     * LCOM2 = |P| - |Q| if |P| > |Q|, otherwise 0.
     * Where:
     *   - P = number of method pairs with no shared attributes
     *   - Q = number of method pairs with shared attributes
     *
     * @param graph Adjacency matrix where graph[i][j] = true if methods i and j share at least one instance field.
     * @return LCOM2 value as an integer.
     */
    private int calculateLCOM2(boolean[][] graph) {
        int disconnected = 0; // P
        int connected = 0;    // Q
        int n = graph.length;

        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                if (graph[i][j]) {
                    connected++;
                } else {
                    disconnected++;
                }
            }
        }

        return Math.max(disconnected - connected, 0);
    }

    // -------------------------------------------------------------------------
    // LCOM3
    // -------------------------------------------------------------------------

    /**
     * Calculates LCOM3 as the number of connected components in the
     * method-variable interaction graph.
     * Each method is a node, and there is an undirected edge between two
     * methods if they share at least one instance variable.
     *
     * @param graph adjacency matrix where graph[i][j] = true if method i and j share a variable
     * @return number of connected components
     */
    private int calculateLCOM3(boolean[][] graph) {
        return countConnectedComponents(graph);
    }


    // -------------------------------------------------------------------------
    // LCOM4: extend with call-graph
    // -------------------------------------------------------------------------

    /**
     * Combines method-variable adjacency with method-call relations to build
     * the graph used in LCOM4.
     */
    private boolean[][] mergeCallGraph(boolean[][] baseGraph, List<MethodInstance> methods) {
        int n = methods.size();
        boolean[][] graph = new boolean[n][n];

        // Copy field-sharing adjacency
        for (int i = 0; i < n; i++) {
            graph[i] = Arrays.copyOf(baseGraph[i], n);
        }

        // Add method-call edges (undirected)
        for (int i = 0; i < n; i++) {
            Set<String> calls = extractCalledNames(methods.get(i));
            for (int j = 0; j < n; j++) {
                if (i != j && calls.contains(methods.get(j).getName())) {
                    graph[i][j] = graph[j][i] = true;
                }
            }
        }

        return graph;
    }

    private Set<String> extractCalledNames(MethodInstance methodInstance) {
        if (methodInstance.getStatementsInfo() == null)
            return Collections.emptySet();
        return methodInstance.getStatementsInfo().getStatements().stream()
                .filter(statementInstance -> statementInstance.getType().name().equals("EXPRESSION"))
                .map(statementInstance -> {
                    String structure = statementInstance.getStructure();
                    if (structure.endsWith("();")) {
                        structure = structure.substring(0, structure.length() - 3).trim();
                    }
                    return structure;
                })
                .collect(Collectors.toSet());
    }

    // -------------------------------------------------------------------------
    // LCOM5: normalized cohesion index [0..1]
    // -------------------------------------------------------------------------

    /**
     * Calculates LCOM5: a normalized cohesion index in [0, 1].
     * LCOM5 = 1 - (sum of distinct attributes accessed by each method) / (m × a)
     * <p>
     * A lower LCOM5 indicates higher cohesion.
     *
     * @param methods list of methods in the class
     * @param vars    list of instance attributes in the class
     * @return normalized LCOM5 value
     */
    private double calculateLCOM5(List<MethodInstance> methods, List<VariableInstance> vars) {
        int m = methods.size();
        int a = (int) vars.stream()
                .filter(v -> INSTANCE_SCOPE.equals(v.getScope()))
                .count();

        if (m == 0 || a == 0)
            return 0.0;

        Set<String> attributeNames = vars.stream()
                .filter(v -> INSTANCE_SCOPE.equals(v.getScope()))
                .map(VariableInstance::getName)
                .collect(Collectors.toSet());

        long sumDistinctAccesses = methods.stream()
                .mapToLong(method -> method.getMethodVariables().stream()
                        .filter(v -> INSTANCE_SCOPE.equals(v.getScope()))
                        .map(VariableInstance::getName)
                        .filter(attributeNames::contains)
                        .distinct()
                        .count())
                .sum();

        double lcom5 = 1.0 - (sumDistinctAccesses / (double) (m * a));
        return Math.max(0.0, lcom5); // prevent rounding underflow
    }

    // -------------------------------------------------------------------------
    // Graph & utility methods
    // -------------------------------------------------------------------------

    /**
     * Builds an undirected adjacency matrix where
     * graph[i][j] == true iff methods i and j share at least one instance field.
     */
    private boolean[][] buildFieldAdjacency(List<MethodInstance> methods, List<VariableInstance> vars) {
        int numberOfMethods = methods.size();
        boolean[][] adj = new boolean[numberOfMethods][numberOfMethods];
        Set<String> varNames = vars.stream()
                .map(VariableInstance::getName)
                .collect(Collectors.toSet());

        // Precompute the set of fields used by each method
        List<Set<String>> usedFields = methods.stream()
                .map(m -> m.getMethodVariables().stream()
                        .map(VariableInstance::getName)
                        .filter(varNames::contains)
                        .collect(Collectors.toSet()))
                .toList();

        // Build adjacency by set intersection
        for (int i = 0; i < numberOfMethods; i++) {
            for (int j = i + 1; j < numberOfMethods; j++) {
                Set<String> intersection = new HashSet<>(usedFields.get(i));
                intersection.retainAll(usedFields.get(j));
                if (!intersection.isEmpty()) {
                    adj[i][j] = adj[j][i] = true;
                }
            }
        }
        return adj;
    }

    private int countConnectedComponents(boolean[][] adj) {
        int n = adj.length;
        boolean[] visited = new boolean[n];
        int count = 0;
        Deque<Integer> stack = new ArrayDeque<>();

        for (int i = 0; i < n; i++) {
            if (!visited[i]) {
                count++;
                stack.push(i);
                while (!stack.isEmpty()) {
                    int u = stack.pop();
                    if (!visited[u]) {
                        visited[u] = true;
                        for (int v = 0; v < n; v++) {
                            if (adj[u][v] && !visited[v]) {
                                stack.push(v);
                            }
                        }
                    }
                }
            }
        }
        return count;
    }
}
