package io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.instances.class_instance;

import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.instances.class_instance.parts.*;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.instances.class_instance.parts.method.MethodInstance;
import lombok.Data;

import java.util.List;

@Data
public class ClassInstance {
    // --- Identity ---
    private String name;                  // class name
    private JavaFileType javaFileType;    // CLASS, INTERFACE, ENUM...
    private LayerType layerType;          // CONTROLLER, SERVICE...

    // --- Inheritance & Interfaces ---
    private List<String> superClasses;    // extends
    private List<String> implementedInterfaces; // implements

    // --- Annotations on the type ---
    private List<String> annotations;     // @Entity, @RestController...

    // --- Members ---
    private List<ConstructorInstance> constructors;
    private List<MethodInstance> methods;
    private List<VariableInstance> fields;
    private List<ClassInstance> innerClasses;  // nested types

    // --- Dependencies within the class ---
    private List<String> usedClasses;     // Only names
    private List<GenericUsageInstance> genericUsages;

    // --- Exception handling blocks (try/catch) ---
    private List<ExceptionHandlingInstance> exceptionHandling;

    // --- Metrics ---
    private int linesOfCode;
    private int methodCount;
    private int fieldCount;
    private int innerClassCount;
}
