package io.github.opsord.architecture_evaluator_backend.modules.parser.processor.dto.parts;

import lombok.Data;

@Data
public class ComplexityMetricsDTO {
    /**
     * Cyclomatic Complexity (CC) = E - N + 2P
     * where:
     * E = number of edges in the control flow graph
     * N = number of nodes in the control flow graph
     * P = number of connected components (usually 1 for a single method)
     */
    private int approxMcCabeCC;

    /**
     * Improved Cyclomatic Complexity (ICC_p) = (N + S + I + O) / LOC
     * where:
     * N = number of methods
     * S = sum of executable statements
     * I = maximum number of input parameters
     * O = maximum number of output parameters
     * LOC = total lines of code
     */
    private Number improvedCC;
}
