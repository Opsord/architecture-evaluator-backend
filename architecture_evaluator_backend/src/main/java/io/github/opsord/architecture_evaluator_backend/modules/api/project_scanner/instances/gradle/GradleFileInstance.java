package io.github.opsord.architecture_evaluator_backend.modules.api.project_scanner.instances.gradle;

import lombok.Data;
import java.util.List;

@Data
public class GradleFileInstance {
    private String group;
    private String name;
    private String version;
    private String description;
    private String javaVersion;
    private List<GradleDependencyInstance> dependencies;
}