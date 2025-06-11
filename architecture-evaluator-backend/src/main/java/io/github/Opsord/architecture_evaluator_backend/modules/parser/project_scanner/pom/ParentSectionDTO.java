package io.github.Opsord.architecture_evaluator_backend.modules.parser.project_scanner.pom;

import lombok.Data;

@Data
public class ParentSectionDTO {
    private String groupId;
    private String artifactId;
    private String version;
}
