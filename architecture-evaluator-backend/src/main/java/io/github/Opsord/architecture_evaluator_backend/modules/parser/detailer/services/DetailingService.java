// DetailingService.java
package io.github.Opsord.architecture_evaluator_backend.modules.parser.detailer.services;

import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.dto.CustomCompilationUnitDTO;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.dto.parts.MethodDTO;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.detailer.dto.DetailedMethodDTO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DetailingService {

    public List<DetailedMethodDTO> generateDetailedMethods(CustomCompilationUnitDTO customCompilationUnit) {
        return customCompilationUnit.getMethods().stream().map(method -> {
            DetailedMethodDTO detailedMethod = new DetailedMethodDTO();
            detailedMethod.setName(method.getName());
            detailedMethod.setAccessModifier(method.getAccessModifier());
            detailedMethod.setReturnType(method.getReturnType());
            detailedMethod.setStatementCount(method.getStatements() != null ? method.getStatements().size() : 0);
            detailedMethod.setControlStatementCount(method.getControlStatements() != null ? method.getControlStatements().size() : 0);
            detailedMethod.setApproximateCC(calculateCyclomaticComplexity(method));
            return detailedMethod;
        }).collect(Collectors.toList());
    }

    /**
     * Advertencia:
     * Este modelo es una aproximación. En realidad, no todas las sentencias de control producen exactamente dos salidas
     * y las sentencias simples (aunque forman parte del flujo) a veces se conectan de manera secuencial
     * sin “rama” adicional.
     * Por ello, esta fórmula te dará una cifra aproximada, pero podría diferir de la que
     * obtendrías si construyes el CFG real del método.
     */
    public int calculateCyclomaticComplexity(MethodDTO methodDTO) {
        int nodes = methodDTO.getStatements().size() + methodDTO.getControlStatements().size();
        int edges = methodDTO.getStatements().size()+ 2 * methodDTO.getControlStatements().size();
        return edges - nodes + 2;
    }

}

