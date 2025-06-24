package io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.instances.parts;

import lombok.Data;

import java.util.List;

@Data
public class ParameterInstance {
    private String name; // e.g., "repository"
    private String type; // e.g., "UserRepository"
    private List<String> annotations; // e.g., @Qualifier("myRepo")
}
