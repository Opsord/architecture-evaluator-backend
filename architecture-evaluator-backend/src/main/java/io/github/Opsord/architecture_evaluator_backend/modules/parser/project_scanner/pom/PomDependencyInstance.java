package io.github.opsord.architecture_evaluator_backend.modules.parser.project_scanner.pom;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PomDependencyInstance {
    private String groupId;
    private String artifactId;
    private String version;

    public String getBasePackage() {
        // This method is used to get the base package of the dependency
        return groupId != null ? groupId : "";
    }

    public String toKey() {
        return groupId + ":" + artifactId;
    }
}