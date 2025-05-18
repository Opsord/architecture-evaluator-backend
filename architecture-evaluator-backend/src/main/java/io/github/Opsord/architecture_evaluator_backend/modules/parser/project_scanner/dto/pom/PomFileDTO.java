package io.github.Opsord.architecture_evaluator_backend.modules.parser.project_scanner.dto.pom;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PomFileDTO {
    private String groupId;
    private String artifactId;
    private String version;
    private List<DependencyDTO> dependencies;
}
