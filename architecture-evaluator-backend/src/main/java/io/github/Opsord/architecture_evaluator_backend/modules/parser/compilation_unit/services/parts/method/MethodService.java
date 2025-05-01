// MethodService.java
package io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.parts.method;

import com.github.javaparser.ast.CompilationUnit;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.dto.parts.method.MethodDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class MethodService {

    private static final Logger logger = LoggerFactory.getLogger(MethodService.class);

    public List<MethodDTO> getMethods(CompilationUnit compilationUnit) {
        logger.info("Extracting methods from compilation unit");
        List<MethodDTO> methods = new ArrayList<>();
        MethodVisitor visitor = new MethodVisitor();
        compilationUnit.accept(visitor, methods);
        // Calculate metrics before returning
        for (MethodDTO method : methods) {
            // Calculate McCabe's Cyclomatic Complexity
            calculateApproxMcCabeCC(method);
        }
        return methods;
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
    public void calculateApproxMcCabeCC(MethodDTO methodDTO) {
        int nodes = methodDTO.getStatementsInfo().getStatements().size() + methodDTO.getStatementsInfo().getNumberOfControlStatements();
        int edges = methodDTO.getStatementsInfo().getStatements().size() + 2 * methodDTO.getStatementsInfo().getNumberOfControlStatements();
        int connectedComponents = 1; // Assuming a single connected component for simplicity
        int approxMcCabeCC = edges - nodes + (2 * connectedComponents);
        methodDTO.getMethodMetrics().setApproxMcCabeCC(approxMcCabeCC);
    }
}