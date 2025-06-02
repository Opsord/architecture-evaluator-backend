package io.github.Opsord.architecture_evaluator_backend.modules.parser.processor.services.analysis.parts;

import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.dto.CustomCompilationUnitDTO;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.dto.parts.method.MethodDTO;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.parts.statement.StatementService;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.processor.dto.analysis.parts.ProgramMetricsDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProgramMetricsService {

    private final StatementService statementService;

    public ProgramMetricsDTO generateProgramMetrics(CustomCompilationUnitDTO customCompilationUnit) {
        ProgramMetricsDTO programMetrics = new ProgramMetricsDTO();

        int numberOfMethods = customCompilationUnit.getMethods().size();
        int sumOfExecutableStatements;
        int maxInputParameters = 0;
        int maxOutputParameters = 0;
        int totalLinesOfCode = customCompilationUnit.getLinesOfCode();

        // Get the sum of the executable statements in the program
        sumOfExecutableStatements = statementService.countExecutableStatements(customCompilationUnit.getStatements());

        // Get the maximum number of input parameters and output parameters
        // Also, calculate the sum of approxMcCabeCC
        for (MethodDTO method : customCompilationUnit.getMethods()) {
            int inputParameters = method.getParameters().getInputs() != null ? method.getParameters().getInputs().size() : 0;
            int outputParameters = method.getParameters().getOutputs() != null ? method.getParameters().getOutputs().size() : 0;
            if (inputParameters > maxInputParameters) {
                maxInputParameters = inputParameters;
            }
            if (outputParameters > maxOutputParameters) {
                maxOutputParameters = outputParameters;
            }
        }

        // Set metrics in DTO
        programMetrics.setNumberOfMethods(numberOfMethods);
        programMetrics.setSumOfExecutableStatements(sumOfExecutableStatements);
        programMetrics.setMaxInputParameters(maxInputParameters);
        programMetrics.setMaxOutputParameters(maxOutputParameters);
        programMetrics.setLinesOfCode(totalLinesOfCode);

        return programMetrics;
    }
}