package io.github.Opsord.architecture_evaluator_backend.modules.parser.processor.dto.summary.parts;

import lombok.Data;

import java.util.List;

@Data
public class ClassInstanceSummary {
    private String name;
    private List<String> userClasses;
    private List<String> dependentClasses;
    private String javaFileType;
    private String layerType;
    private List<String> annotations;
    private int linesOfCode;
    private List<MethodInstanceSummary> methodSummaries;
    private int fieldCount;
}