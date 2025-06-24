// MethodDTO.java
package io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.instances.parts.method;

import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.instances.parts.ParameterInstance;
import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.instances.parts.VariableInstance;
import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.instances.parts.method.parts.BasicInfo;
import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.instances.parts.method.parts.MethodMetrics;
import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.instances.parts.method.parts.StatementsInfo;
import lombok.Data;

import java.util.List;

@Data
public class MethodInstance {
    private String name;
    private BasicInfo basicInfo;
    private StatementsInfo statementsInfo;
    private List<ParameterInstance> inputParameters;
    private List<ParameterInstance> outputParameters;
    private MethodMetrics methodMetrics;
    private List<VariableInstance> methodVariables;
}