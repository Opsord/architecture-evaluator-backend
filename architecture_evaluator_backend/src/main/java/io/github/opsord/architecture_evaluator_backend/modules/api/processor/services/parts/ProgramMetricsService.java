package io.github.opsord.architecture_evaluator_backend.modules.api.processor.services.parts;

import io.github.opsord.architecture_evaluator_backend.modules.api.parser.instances.class_instance.ClassInstance;
import io.github.opsord.architecture_evaluator_backend.modules.api.parser.instances.class_instance.parts.method.MethodInstance;
import io.github.opsord.architecture_evaluator_backend.modules.api.parser.services.class_instance.parts.statement.StatementService;
import io.github.opsord.architecture_evaluator_backend.modules.api.processor.dto.parts.ProgramMetricsDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProgramMetricsService {

    private final StatementService statementService;

    /**
     * Generates program-level metrics for a given class.
     *
     * @param classInstance The class to analyze.
     * @return The program metrics for the given class.
     */
    public ProgramMetricsDTO generateProgramMetrics(ClassInstance classInstance) {
        ProgramMetricsDTO programMetrics = new ProgramMetricsDTO();

        int numberOfMethods = classInstance.getMethods().size();
        int sumOfExecutableStatements = statementService.countExecutableStatements(classInstance.getStatements());
        int maxInputParameters = 0;
        int maxOutputParameters = 0;
        int totalLinesOfCode = classInstance.getLinesOfCode();

        for (MethodInstance method : classInstance.getMethods()) {
            int inputParameters = method.getInputParameters() != null ? method.getInputParameters().size() : 0;
            int outputParameters = method.getOutputParameters() != null ? method.getOutputParameters().size() : 0;
            if (inputParameters > maxInputParameters) {
                maxInputParameters = inputParameters;
            }
            if (outputParameters > maxOutputParameters) {
                maxOutputParameters = outputParameters;
            }
        }

        programMetrics.setNumberOfMethods(numberOfMethods);
        programMetrics.setSumOfExecutableStatements(sumOfExecutableStatements);
        programMetrics.setMaxInputParameters(maxInputParameters);
        programMetrics.setMaxOutputParameters(maxOutputParameters);
        programMetrics.setLinesOfCode(totalLinesOfCode);

        return programMetrics;
    }
}