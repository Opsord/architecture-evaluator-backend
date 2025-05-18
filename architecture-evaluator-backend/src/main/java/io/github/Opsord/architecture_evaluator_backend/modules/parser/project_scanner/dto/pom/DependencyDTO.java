package io.github.Opsord.architecture_evaluator_backend.modules.parser.project_scanner.dto.pom;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DependencyDTO {
    private String groupId;
    private String artifactId;

    public String getBasePackage() {
        // This method is used to get the base package of the dependency
        return groupId != null ? groupId : "";
    }

    public String toKey() {
        return groupId + ":" + artifactId;
    }
}