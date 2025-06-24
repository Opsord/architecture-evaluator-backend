package io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.instances.file_types;

import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.instances.JavaFileType;
import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.instances.JavaTypeContent;
import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.instances.parts.LayerAnnotation;
import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.instances.parts.method.MethodInstance;
import lombok.Data;

import java.util.List;

/**
 * Represents a Java Interface.
 */
@Data
public class InterfaceInstance implements JavaTypeContent {

    private String name;
    private LayerAnnotation layerAnnotation;          // CONTROLLER, SERVICE...
    private List<String> extendedInterfaces;
    private List<String> annotations;
    private List<MethodInstance> methods;
    private List<String> usedClasses;

    private Integer linesOfCode;

    @Override
    public JavaFileType getJavaFileType() {
        return JavaFileType.INTERFACE;
    }
}
