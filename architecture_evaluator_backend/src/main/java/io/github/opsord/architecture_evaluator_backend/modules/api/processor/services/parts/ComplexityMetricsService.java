package io.github.opsord.architecture_evaluator_backend.modules.api.processor.services.parts;

import io.github.opsord.architecture_evaluator_backend.modules.api.parser.instances.class_instance.ClassInstance;
import io.github.opsord.architecture_evaluator_backend.modules.api.parser.instances.class_instance.parts.method.MethodInstance;
import io.github.opsord.architecture_evaluator_backend.modules.api.parser.instances.class_instance.parts.method.parts.StatementsInfo;
import io.github.opsord.architecture_evaluator_backend.modules.api.processor.dto.parts.ComplexityMetricsDTO;
import org.springframework.stereotype.Service;

@Service
public class ComplexityMetricsService {

    /**
     * Calculates complexity metrics for a given class instance.
     *
     * @param classInstance the class instance to analyze
     * @return a ComplexityMetricsDTO containing the calculated metrics
     */
    public ComplexityMetricsDTO calculateComplexityMetrics(ClassInstance classInstance) {
        ComplexityMetricsDTO complexityMetrics = new ComplexityMetricsDTO();

        int compUnitApproxMcCabeCC = 0;
        int numberOfMethods = classInstance.getMethods().size();
        int sumOfExecutableStatements = classInstance.getStatements().size();
        int maxInputParameters = 0;
        int maxOutputParameters = 0;
        int totalLinesOfCode = classInstance.getLinesOfCode();

        for (MethodInstance method : classInstance.getMethods()) {
            int inputParameters = method.getInputParameters() != null ? method.getInputParameters().size() : 0;
            int outputParameters = method.getOutputParameters() != null ? method.getOutputParameters().size() : 0;

            maxInputParameters = Math.max(maxInputParameters, inputParameters);
            maxOutputParameters = Math.max(maxOutputParameters, outputParameters);

            int methodMcCabeCC = calculateApproxMcCabeCC(method);
            if (method.getMethodMetrics() != null) {
                method.getMethodMetrics().setMcCabeComplexity(methodMcCabeCC);
            }
            complexityMetrics.setMaxMethodMcCabeCC(Math.max(complexityMetrics.getMaxMethodMcCabeCC(), methodMcCabeCC));
            compUnitApproxMcCabeCC += methodMcCabeCC;
        }

        double improvedCC = calculateImprovedCC(numberOfMethods, sumOfExecutableStatements, maxInputParameters,
                maxOutputParameters, totalLinesOfCode);

        complexityMetrics.setApproxMcCabeCC(compUnitApproxMcCabeCC);
        complexityMetrics.setImprovedCC(improvedCC);

        return complexityMetrics;
    }

    /**
     * Calculates an improved approximation of McCabe Cyclomatic Complexity
     * by inspecting various control structures in the AST.
     *
     * @param methodInstance the method instance to analyze
     * @return approximate McCabe cyclomatic complexity
     */
    private int calculateApproxMcCabeCC(MethodInstance methodInstance) {
        StatementsInfo info = methodInstance.getStatementsInfo();

        // Base node count: all statements and explicit control nodes
        int nodeCount = info.getStatements().size()
                + info.getNumberOfControlStatements()        // if, for, while, switch
                + info.getNumberOfReturnStatements()         // return
                + info.getNumberOfThrowStatements()          // throw
                + info.getNumberOfCatchClauses();            // catch

        // Edge count: each control adds two edges, plus sequential edges
        int sequentialEdges = info.getStatements().size() - 1;
        int controlEdges = 2 * info.getNumberOfControlStatements()
                + info.getNumberOfLogicalOperators()        // "&&" or "||"
                + info.getNumberOfTernaryOperators();       // ?:

        int edgeCount = sequentialEdges + controlEdges + info.getNumberOfReturnStatements();

        // Usually a single connected component
        int connectedComponents = 1;

        // CC = E - N + 2P
        return edgeCount - nodeCount + (2 * connectedComponents);
    }


    /**
     * Calculates the Improved Cyclomatic Complexity (ICC_p).
     * Formula: ICC_p = (N + S + I + O) / LOC
     * where:
     * N = number of methods
     * S = sum of executable statements
     * I = maximum number of input parameters
     * O = maximum number of output parameters
     * LOC = total lines of code
     *
     * @param numberOfMethods number of methods in the class
     * @param sumOfExecutableStatements total number of executable statements
     * @param maxInputParameters maximum number of input parameters in any method
     * @param maxOutputParameters maximum number of output parameters in any method
     * @param totalLinesOfCode total lines of code in the class
     * @return the improved cyclomatic complexity value
     */
    private double calculateImprovedCC(int numberOfMethods, int sumOfExecutableStatements, int maxInputParameters,
                                       int maxOutputParameters, int totalLinesOfCode) {
        return totalLinesOfCode > 0
                ? (double) (numberOfMethods + sumOfExecutableStatements + maxInputParameters + maxOutputParameters)
                / totalLinesOfCode
                : 0;
    }

}