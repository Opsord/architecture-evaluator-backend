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
        boolean[][] fieldGraph = buildFieldGraph(methods, vars);

        // Section: LCOM1
        metrics.setLackOfCohesion1(calculateLCOM1(methods.size(), fieldGraph));

        // Section: LCOM2
        metrics.setLackOfCohesion2(calculateLCOM2(methods, vars));

        // Section: LCOM3
        metrics.setLackOfCohesion3(calculateLCOM3(fieldGraph));

        // Section: LCOM4
        boolean[][] fullGraph = mergeCallGraph(fieldGraph, methods);
        metrics.setLackOfCohesion4(Math.max(0, calculateLCOM3(fullGraph)));

        // Section: LCOM5
        metrics.setLackOfCohesion5(calculateLCOM5(methods, vars));

        return metrics;
    }

    // -------------------------------------------------------------------------
    // LCOM1 & LCOM3 shared logic
    // -------------------------------------------------------------------------

    private int calculateLCOM1(int nMethods, boolean[][] graph) {
        int connected = 0;
        int disconnected = 0;
        for (int i = 0; i < nMethods; i++) {
            for (int j = i + 1; j < nMethods; j++) {
                if (graph[i][j])
                    connected++;
                else
                    disconnected++;
            }
        }
        return Math.max(disconnected - connected, 0);
    }

    private int calculateLCOM3(boolean[][] graph) {
        return Math.max(0, countConnectedComponents(graph) - 1);
    }

    // -------------------------------------------------------------------------
    // LCOM2: normalized field-access metric
    // -------------------------------------------------------------------------

    private double calculateLCOM2(List<MethodInstance> methods, List<VariableInstance> vars) {
        int numberOfMethods = methods.size();
        int numberOfVariables = vars.size();
        if (numberOfMethods <= 1 || numberOfVariables == 0)
            return 0.0;

        // Count accesses per variable
        Map<String, Long> accessCount = methods.stream()
                .flatMap(m -> m.getMethodVariables().stream())
                .filter(v -> INSTANCE_SCOPE.equals(v.getScope()))
                .map(VariableInstance::getName)
                .collect(Collectors.groupingBy(name -> name, Collectors.counting()));

        long sumMAj = vars.stream()
                .mapToLong(v -> accessCount.getOrDefault(v.getName(), 0L))
                .sum();

        double lcom2 = 1.0 - (sumMAj / (double) (numberOfMethods * numberOfVariables));
        return Math.max(0.0, lcom2);
    }

    // -------------------------------------------------------------------------
    // LCOM4: extend with call-graph
    // -------------------------------------------------------------------------

    private boolean[][] mergeCallGraph(boolean[][] baseGraph, List<MethodInstance> methods) {
        int n = methods.size();
        boolean[][] graph = new boolean[n][n];
        // copy base
        for (int i = 0; i < n; i++)
            graph[i] = Arrays.copyOf(baseGraph[i], n);

        // add method-call edges (undirected)
        for (int i = 0; i < n; i++) {
            Set<String> calls = extractCalledNames(methods.get(i));
            for (int j = 0; j < n; j++) {
                if (calls.contains(methods.get(j).getName())) {
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
                .filter(s -> s.getType().name().equals("EXPRESSION"))
                .map(s -> s.getStructure().replaceAll("\\s*\\(\\)\\s*;", ""))
                .collect(Collectors.toSet());
    }

    // -------------------------------------------------------------------------
    // LCOM5: normalized cohesion index [0..1]
    // -------------------------------------------------------------------------

    private double calculateLCOM5(List<MethodInstance> methods, List<VariableInstance> vars) {
        int numberOfMethods = methods.size();
        int numberOfVariables = vars.size();
        if (numberOfMethods == 0 || numberOfVariables == 0)
            return 0.0;

        long totalDistinct = methods.stream()
                .mapToLong(m -> m.getMethodVariables().stream()
                        .filter(v -> INSTANCE_SCOPE.equals(v.getScope()))
                        .map(VariableInstance::getName)
                        .distinct().count())
                .sum();

        double lcom5 = 1.0 - (totalDistinct / (double) (numberOfMethods * numberOfVariables));
        return Math.max(0.0, lcom5);
    }

    // -------------------------------------------------------------------------
    // Graph & utility methods
    // -------------------------------------------------------------------------

    private boolean[][] buildFieldGraph(List<MethodInstance> methods, List<VariableInstance> vars) {
        int numberOfMethods = methods.size();
        boolean[][] graph = new boolean[numberOfMethods][numberOfMethods];
        Set<String> varNames = vars.stream().map(VariableInstance::getName).collect(Collectors.toSet());

        for (int i = 0; i < numberOfMethods; i++) {
            for (int j = i + 1; j < numberOfMethods; j++) {
                Set<String> f1 = methods.get(i).getMethodVariables().stream()
                        .map(VariableInstance::getName).filter(varNames::contains).collect(Collectors.toSet());
                Set<String> f2 = methods.get(j).getMethodVariables().stream()
                        .map(VariableInstance::getName).filter(varNames::contains).collect(Collectors.toSet());
                f1.retainAll(f2);
                if (!f1.isEmpty())
                    graph[i][j] = graph[j][i] = true;
            }
        }
        return graph;
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
