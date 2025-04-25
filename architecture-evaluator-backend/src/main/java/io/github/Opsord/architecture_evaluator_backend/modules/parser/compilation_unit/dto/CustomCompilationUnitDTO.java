// CompilationUnitDTO.java
package io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.dto;

import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.dto.parts.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CustomCompilationUnitDTO {
    private String packageName;
    private List<String> classNames;
    private List<String> interfaceNames;
    private List<MethodDTO> methods;
    private List<VariableDTO> variables;
    private List<String> importedPackages;
    private List<String> comments;
    private List<ExceptionHandlingDTO> exceptionHandling;
    private List<String> superClasses;
    private List<String> implementedInterfaces;
    private List<AnnotationDTO> annotations;
    private List<GenericUsageDTO> genericUsages;

    private int LinesOfCode;
}