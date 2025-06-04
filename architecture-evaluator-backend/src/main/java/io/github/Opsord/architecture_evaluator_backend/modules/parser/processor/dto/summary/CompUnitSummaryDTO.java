package io.github.Opsord.architecture_evaluator_backend.modules.parser.processor.dto.summary;

import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.dto.parts.AnnotationDTO;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.processor.dto.summary.parts.MethodSummaryDTO;
import lombok.Data;

import java.util.List;

@Data
public class CompUnitSummaryDTO {
    private String className;
    private List<MethodSummaryDTO> methods;
    private Integer linesOfCode;
    private List<AnnotationDTO> annotationDTOS;
    private List<String> dependentClasses;
}
