package io.github.Opsord.architecture_evaluator_backend.modules.parser.detailer.services.parts;

import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.dto.CustomCompilationUnitDTO;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.dto.parts.method.MethodDTO;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.parts.statement.StatementService;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.detailer.dto.ProgramMetricsDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProgramMetricsService {

    private final StatementService statementService;

    public ProgramMetricsDTO generateProgramMetrics(CustomCompilationUnitDTO customCompilationUnit) {
        ProgramMetricsDTO programMetrics = new ProgramMetricsDTO();

        int numberOfMethods = customCompilationUnit.getMethods().size();
        int sumOfExecutableStatements = 0;
        int maxInputParameters = 0;
        int maxOutputParameters = 0;
        int totalLinesOfCode = customCompilationUnit.getLinesOfCode();

        // Get the sum of the executable statements in the program
        sumOfExecutableStatements = statementService.countExecutableStatements(customCompilationUnit.getStatements());
        // Get the maximum number of input parameters and output parameters
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

        // Calculate ICC_p
        double iccP = totalLinesOfCode > 0
                ? (double) (numberOfMethods + sumOfExecutableStatements + maxInputParameters + maxOutputParameters) / totalLinesOfCode
                : 0;

        // Set metrics in DTO
        programMetrics.setNumberOfMethods(numberOfMethods);
        programMetrics.setSumOfExecutableStatements(sumOfExecutableStatements);
        programMetrics.setMaxInputParameters(maxInputParameters);
        programMetrics.setMaxOutputParameters(maxOutputParameters);
        programMetrics.setLinesOfCode(totalLinesOfCode);
        programMetrics.setImprovedCyclomaticComplexity(iccP);

        return programMetrics;
    }
}