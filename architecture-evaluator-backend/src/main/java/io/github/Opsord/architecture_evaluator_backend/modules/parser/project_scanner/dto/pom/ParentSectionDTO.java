package io.github.Opsord.architecture_evaluator_backend.modules.parser.project_scanner.dto.pom;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ParentSectionDTO {
    private String groupId;
    private String artifactId;
    private String version;
}
