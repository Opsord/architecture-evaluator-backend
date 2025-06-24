package io.github.opsord.architecture_evaluator_backend.modules.parser.processor.services.parts;

import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.instances.JavaTypeInstance;
import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.instances.file_types.ClassInstance;
import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.instances.parts.method.MethodInstance;
import io.github.opsord.architecture_evaluator_backend.modules.parser.processor.dto.parts.ComplexityMetricsDTO;
import org.springframework.stereotype.Service;

@Service
public class ComplexityMetricsService {

    /**
     * Calculates complexity metrics for a given Java type instance.
     *
     * @param javaTypeInstance the Java type instance to analyze
     * @return a ComplexityMetricsDTO containing the calculated metrics
     */
    public ComplexityMetricsDTO calculateComplexityMetrics(JavaTypeInstance javaTypeInstance) {
        if (javaTypeInstance == null || javaTypeInstance.getType() == null || javaTypeInstance.getContent() == null) {
            return new ComplexityMetricsDTO();
        }
        if (javaTypeInstance.getType().isClassType()) {
            ClassInstance classInstance = (ClassInstance) javaTypeInstance.getContent();
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
                compUnitApproxMcCabeCC += methodMcCabeCC;
            }

            double improvedCC = calculateImprovedCC(numberOfMethods, sumOfExecutableStatements, maxInputParameters,
                    maxOutputParameters, totalLinesOfCode);

            complexityMetrics.setApproxMcCabeCC(compUnitApproxMcCabeCC);
            complexityMetrics.setImprovedCC(improvedCC);

            return complexityMetrics;
        }
        return new ComplexityMetricsDTO();
    }

    /**
     * Warning:
     * This model is an approximation. In reality, not all control statements produce exactly two outputs,
     * and simple statements (although part of the flow) are sometimes connected sequentially
     * without an additional "branch".
     * Therefore, this formula will give you an approximate figure, but it may differ from what
     * would be obtained if the real CFG (Control Flow Graph) of the method is built.
     * The formula is:
     * CC = E - N + 2P
     * where:
     * E = number of edges in the control flow graph
     * N = number of nodes in the control flow graph
     * P = number of connected components in the control flow graph
     * In most cases, P = 1, since the method is a single block of code.
     *
     * @param methodInstance the method instance to analyze
     * @return the approximate McCabe cyclomatic complexity
     */
    private int calculateApproxMcCabeCC(MethodInstance methodInstance) {
        int nodes = methodInstance.getStatementsInfo().getStatements().size()
                + methodInstance.getStatementsInfo().getNumberOfControlStatements();
        int edges = methodInstance.getStatementsInfo().getStatements().size()
                + 2 * methodInstance.getStatementsInfo().getNumberOfControlStatements();
        int connectedComponents = 1; // Assuming a single connected component for simplicity
        return edges - nodes + (2 * connectedComponents);
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