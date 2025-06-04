package io.github.Opsord.architecture_evaluator_backend.modules.parser.processor.services.analysis.parts;

import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.dto.CustomCompilationUnitDTO;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.dto.parts.method.MethodDTO;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.processor.dto.analysis.parts.ComplexityMetricsDTO;
import org.springframework.stereotype.Service;

@Service
public class ComplexityMetricsService {

    public ComplexityMetricsDTO calculateComplexityMetrics(CustomCompilationUnitDTO customCompilationUnit) {
        ComplexityMetricsDTO complexityMetrics = new ComplexityMetricsDTO();

        int compUnitApproxMcCabeCC = 0;
        int numberOfMethods = customCompilationUnit.getMethods().size();
        int sumOfExecutableStatements = customCompilationUnit.getStatements().size();
        int maxInputParameters = 0;
        int maxOutputParameters = 0;
        int totalLinesOfCode = customCompilationUnit.getLinesOfCode();

        for (MethodDTO method : customCompilationUnit.getMethods()) {
            int inputParameters = method.getParameters().getInputs() != null ? method.getParameters().getInputs().size() : 0;
            int outputParameters = method.getParameters().getOutputs() != null ? method.getParameters().getOutputs().size() : 0;

            maxInputParameters = Math.max(maxInputParameters, inputParameters);
            maxOutputParameters = Math.max(maxOutputParameters, outputParameters);

            int methodMcCabeCC = calculateApproxMcCabeCC(method);
            System.out.println("Method: " + method.getName() + ", McCabe CC: " + methodMcCabeCC);
            method.getMethodMetrics().setMcCabeComplexity(methodMcCabeCC);
            compUnitApproxMcCabeCC += methodMcCabeCC;
        }

        double improvedCC = calculateImprovedCC(numberOfMethods, sumOfExecutableStatements, maxInputParameters, maxOutputParameters, totalLinesOfCode);

        complexityMetrics.setApproxMcCabeCC(compUnitApproxMcCabeCC);
        complexityMetrics.setImprovedCC(improvedCC);

        return complexityMetrics;
    }

    /**
     * Advertencia:
     * Este modelo es una aproximación. En realidad, no todas las sentencias de control producen exactamente dos salidas
     * y las sentencias simples (aunque forman parte del flujo) a veces se conectan de manera secuencial
     * sin “rama” adicional.
     * Por ello, esta fórmula te dará una cifra aproximada, pero podría diferir de lo que
     * se obtendría si se construye el CFG real del método.
     * La fórmula es:
     * CC = E - N + 2P
     * donde:
     * E = número de aristas (edges) en el grafo de control de flujo
     * N = número de nodos (nodes) en el grafo de control de flujo
     * P = número de componentes conectados (connected components) en el grafo de control de flujo
     * En la mayoría de los casos, P = 1, ya que el método es un único bloque de código.
     */
    private int calculateApproxMcCabeCC(MethodDTO methodDTO) {
        int nodes = methodDTO.getStatementsInfo().getStatements().size() + methodDTO.getStatementsInfo().getNumberOfControlStatements();
        int edges = methodDTO.getStatementsInfo().getStatements().size() + 2 * methodDTO.getStatementsInfo().getNumberOfControlStatements();
        int connectedComponents = 1; // Assuming a single connected component for simplicity
        return edges - nodes + (2 * connectedComponents);
    }

    /**
     * Improved Cyclomatic Complexity (ICC_p) = (N + S + I + O) / LOC
     * where:
     * N = number of methods
     * S = sum of executable statements
     * I = maximum number of input parameters
     * O = maximum number of output parameters
     * LOC = total lines of code
     */
    private double calculateImprovedCC(int numberOfMethods, int sumOfExecutableStatements, int maxInputParameters, int maxOutputParameters, int totalLinesOfCode) {
        return totalLinesOfCode > 0
                ? (double) (numberOfMethods + sumOfExecutableStatements + maxInputParameters + maxOutputParameters) / totalLinesOfCode
                : 0;
    }
}