package io.github.opsord.architecture_evaluator_backend.modules.api.project_scanner.instances.pom;

import lombok.Data;

@Data
public class ParentSectionDTO {
    private String groupId;
    private String artifactId;
    private String version;
}
