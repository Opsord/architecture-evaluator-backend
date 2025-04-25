// DetailingService.java
package io.github.Opsord.architecture_evaluator_backend.modules.parser.detailer.services.parts;

import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.dto.CustomCompilationUnitDTO;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.dto.parts.MethodDTO;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.detailer.dto.method.DetailedMethodDTO;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.detailer.dto.method.parts.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MethodDetailingService {

    public List<DetailedMethodDTO> generateDetailedMethods(CustomCompilationUnitDTO customCompilationUnit) {
        return customCompilationUnit.getMethods().stream().map(method -> {
            DetailedMethodDTO detailedMethod = new DetailedMethodDTO();

            // Basic information
            BasicInfo basicInfo = new BasicInfo();
            basicInfo.setName(method.getName());
            basicInfo.setAccessModifier(method.getAccessModifier());
            basicInfo.setReturnType(method.getReturnType());
            detailedMethod.setBasicInfo(basicInfo);

            // Statement information
            StatementInfo statementInfo = new StatementInfo();
            statementInfo.setNumberOfStatements(method.getStatements() != null ? method.getStatements().size() : 0);
            statementInfo.setNumberOfExecutableStatements(method.getExecutableStatements() != null ? method.getExecutableStatements().size() : 0);
            statementInfo.setNumberOfControlStatements(method.getControlStatements() != null ? method.getControlStatements().size() : 0);
            detailedMethod.setStatementInfo(statementInfo);

            // Detailed parameters
            DetailedParameters detailedParameters = new DetailedParameters();
            detailedParameters.setNumberOfInputs(method.getInputs() != null ? method.getInputs().size() : 0);
            detailedParameters.setNumberOfOutputs(method.getOutputs() != null ? method.getOutputs().size() : 0);
            detailedParameters.setLinesOfCode(method.getLinesOfCode());
            detailedMethod.setDetailedParameters(detailedParameters);

            // Metrics
            MethodMetrics methodMetrics = new MethodMetrics();
            methodMetrics.setApproxMcCabeCC(calculateApproxMcCabeCC(method));
            // Improved CC can be calculated here if needed
            detailedMethod.setMethodMetrics(methodMetrics);

            return detailedMethod;
        }).collect(Collectors.toList());
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
    public int calculateApproxMcCabeCC(MethodDTO methodDTO) {
        int nodes = methodDTO.getStatements().size() + methodDTO.getControlStatements().size();
        int edges = methodDTO.getStatements().size() + 2 * methodDTO.getControlStatements().size();
        int connectedComponents = 1; // Assuming a single connected component for simplicity
        return edges - nodes + (2 * connectedComponents);
    }

}

