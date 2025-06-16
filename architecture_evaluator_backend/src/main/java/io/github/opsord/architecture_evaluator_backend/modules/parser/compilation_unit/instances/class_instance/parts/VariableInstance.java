package io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.instances.class_instance.parts;

import lombok.Data;

@Data
public class VariableInstance {
    private String name;
    private String type;
    private String scope; // "instance" or "local"
}