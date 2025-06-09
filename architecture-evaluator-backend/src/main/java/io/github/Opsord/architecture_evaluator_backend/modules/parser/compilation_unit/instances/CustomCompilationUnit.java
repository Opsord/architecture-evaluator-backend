// CustomCompilationUnit.java
package io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.instances;

import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.instances.class_instance.ClassInstance;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.instances.class_instance.parts.*;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.instances.class_instance.parts.method.MethodInstance;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.instances.class_instance.parts.method.parts.statement.StatementInstance;
import lombok.Data;

import java.util.List;

@Data
public class CustomCompilationUnit {

    // --- File Information ---
    private String fileName;
    private String filePath;
    private String packageName;

    // --- Class Metadata ---
    private List<ClassInstance> classes;
    private List<ClassInstance> superClasses;
    private List<String> interfaceNames;
    private List<String> implementedInterfaces;
    private List<String> annotations;
    private JavaFileType javaFileType;
    private LayerType layerType;

    // --- Relationships / Dependencies ---
    private List<String> importedPackages;
    private List<ClassInstance> dependentClasses;
    private List<String> usedClasses;
    private List<GenericUsageInstance> genericUsages;

    // --- Code Details ---
    private List<StatementInstance> statements;
    private List<MethodInstance> methods;
    private List<VariableInstance> variables;
    private List<String> comments;
    private List<ExceptionHandlingInstance> exceptionHandling;

    // --- Metrics ---
    private int LinesOfCode;
}