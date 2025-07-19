package io.github.opsord.architecture_evaluator_backend.modules.api.project_scanner.instances.gradle;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GradleDependencyInstance {
    private String group;
    private String name;
    private String version;

    public String toKey() {
        return group + ":" + name;
    }
}