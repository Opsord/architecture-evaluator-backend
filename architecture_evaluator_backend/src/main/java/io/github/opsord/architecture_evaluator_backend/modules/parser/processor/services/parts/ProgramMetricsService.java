package io.github.opsord.architecture_evaluator_backend.modules.parser.processor.services.parts;

import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.instances.JavaTypeInstance;
import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.instances.file_types.ClassInstance;
import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.instances.parts.method.MethodInstance;
import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.parts.statement.StatementService;
import io.github.opsord.architecture_evaluator_backend.modules.parser.processor.dto.parts.ProgramMetricsDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProgramMetricsService {

    private final StatementService statementService;

    /**
     * Generates program-level metrics for a given Java type instance.
     *
     * @param javaTypeInstance The Java type to analyze.
     * @return The program metrics for the given type (only for classes).
     */
    public ProgramMetricsDTO generateProgramMetrics(JavaTypeInstance javaTypeInstance) {
        if (javaTypeInstance == null || javaTypeInstance.getType() == null || javaTypeInstance.getContent() == null) {
            return new ProgramMetricsDTO();
        }
        if (javaTypeInstance.getType().isClassType()) { // You may need to implement isClassType() in JavaFileType
            ClassInstance classInstance = (ClassInstance) javaTypeInstance.getContent();
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
        // If not a class, return empty metrics or handle other types as needed
        return new ProgramMetricsDTO();
    }
}