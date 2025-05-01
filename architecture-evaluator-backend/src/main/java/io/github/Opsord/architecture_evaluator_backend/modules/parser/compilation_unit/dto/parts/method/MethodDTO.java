// MethodDTO.java
package io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.dto.parts.method;

import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.dto.parts.method.parts.BasicInfo;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.dto.parts.method.parts.MethodMetrics;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.dto.parts.method.parts.Parameters;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.dto.parts.method.parts.StatementsInfo;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MethodDTO {
    private String name;
    private BasicInfo basicInfo;
    private StatementsInfo statementsInfo;
    private Parameters parameters;
    private MethodMetrics methodMetrics;
}