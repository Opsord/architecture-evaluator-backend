package io.github.Opsord.architecture_evaluator_backend.modules.parser.processor.services.summary;

import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.dto.CustomCompilationUnitDTO;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.dto.parts.VariableDTO;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.dto.parts.method.MethodDTO;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.processor.dto.summary.CompUnitSummaryDTO;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.processor.dto.summary.parts.MethodSummaryDTO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CompUnitSummaryService {

    /**
     * Creates a summary of a compilation unit
     *
     * @param compilationUnit The compilation unit to summarize
     * @return A CompUnitSummaryDTO containing the summary information
     */
    public CompUnitSummaryDTO createSummary(CustomCompilationUnitDTO compilationUnit) {
        CompUnitSummaryDTO summary = new CompUnitSummaryDTO();

        // Set the class name (using the first one if there is multiple)
        summary.setClassName(compilationUnit.getClassName().isEmpty() ?
                "Unknown" : compilationUnit.getClassName().get(0));

        // Set lines of code
        summary.setLinesOfCode(compilationUnit.getLinesOfCode());

        // Set the annotations
        summary.setAnnotationDTOS(compilationUnit.getAnnotations());

        // Create method summaries
        List<MethodSummaryDTO> methodSummaries = compilationUnit.getMethods().stream()
                .map(this::createMethodSummary)
                .collect(Collectors.toList());

        summary.setMethods(methodSummaries);

        // Set the dependent classes
        summary.setDependentClasses(compilationUnit.getDependentClasses());

        return summary;
    }

    /**
     * Creates a summary of a method
     *
     * @param method The method to summarize
     * @return A MethodSummaryDTO containing the summary information
     */
    private MethodSummaryDTO createMethodSummary(MethodDTO method) {
        MethodSummaryDTO methodSummary = new MethodSummaryDTO();

        // Set method name
        methodSummary.setMethodName(method.getName());

        // Set the return type
        String returnType = method.getBasicInfo() != null ?
                method.getBasicInfo().getReturnType() : "void";
        methodSummary.setReturnType(returnType);

        // Set lines of code
        Integer linesOfCode = method.getMethodMetrics() != null ?
                method.getMethodMetrics().getLinesOfCode() : 0;
        methodSummary.setLinesOfCode(linesOfCode);

        // Set McCabe complexity
        Integer mcCabeComplexity = method.getMethodMetrics() != null && method.getMethodMetrics().getMcCabeComplexity() != null
                ? method.getMethodMetrics().getMcCabeComplexity()
                : 0;
        methodSummary.setMcCabeComplexity(mcCabeComplexity);

        // Extract used variables
        List<String> usedVariables = method.getMethodVariables().stream()
                .map(VariableDTO::getName)
                .distinct()
                .collect(Collectors.toList());
        methodSummary.setUsedVariables(usedVariables);

        return methodSummary;
    }
}