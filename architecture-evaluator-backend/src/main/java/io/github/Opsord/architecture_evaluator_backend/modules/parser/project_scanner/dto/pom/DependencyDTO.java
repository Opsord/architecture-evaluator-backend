package io.github.Opsord.architecture_evaluator_backend.modules.parser.project_scanner.dto.pom;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DependencyDTO {
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