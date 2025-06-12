package io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.instances.class_instance.parts;

import lombok.Data;

import java.util.List;

@Data
public class ConstructorInstance {
    private String name; // Usually same as class name
    private List<ParameterInstance> parameters; // Constructor parameters
    private List<String> annotations; // e.g., @Autowired
    private List<String> modifiers; // e.g., public, protected, private
    private List<String> thrownExceptions; // e.g., throws IOException
    private String body; // Optional: full body as string
    private int lineCount; // LOC for constructor body
    private List<String> comments; // Javadoc or inline comments
}
