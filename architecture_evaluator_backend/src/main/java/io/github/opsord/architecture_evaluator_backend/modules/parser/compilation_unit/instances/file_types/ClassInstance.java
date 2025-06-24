package io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.instances.file_types;

import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.instances.JavaFileType;
import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.instances.JavaTypeContent;
import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.instances.parts.*;
import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.instances.parts.method.MethodInstance;
import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.instances.parts.method.parts.statement.StatementInstance;
import lombok.Data;

import java.util.List;

@Data
public class ClassInstance implements JavaTypeContent {
    // --- Identity ---
    private String name;                  // class name
    private LayerAnnotation layerAnnotation;          // CONTROLLER, SERVICE...

    // --- Inheritance & Interfaces ---
    private List<String> superClasses;    // extends
    private List<String> implementedInterfaces; // implements

    // --- Annotations on the type ---
    private List<String> annotations;     // @Entity, @RestController...

    // --- Members ---
    private List<ConstructorInstance> constructors;
    private List<MethodInstance> methods;
    private List<StatementInstance> statements;
    private List<VariableInstance> classVariables;
    private List<ClassInstance> innerClasses;  // nested types

    // --- Dependencies within the class ---
    private List<String> usedClasses;     // Every class used in the class
    private List<String> classDependencies; // Other program classes used in the class
    private List<String> dependentClasses; // Classes that depend on this class
    private List<GenericUsageInstance> genericUsages;

    // --- Exception handling blocks (try/catch) ---
    private List<ExceptionHandlingInstance> exceptionHandling;

    // --- Metrics ---
    private Integer linesOfCode;

    @Override
    public JavaFileType getJavaFileType() {
        return JavaFileType.CLASS;
    }

    @Override
    public Integer getLinesOfCode() {
        return linesOfCode;
    }

}
