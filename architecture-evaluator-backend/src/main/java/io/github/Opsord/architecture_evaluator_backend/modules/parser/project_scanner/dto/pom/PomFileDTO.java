package io.github.Opsord.architecture_evaluator_backend.modules.parser.project_scanner.dto.pom;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PomFileDTO {
    private ParentSectionDTO parentSection;
    private String groupId;
    private String artifactId;
    private String version;
    private String description;
    private String url;
    private String license;
    private List<String> developers;
    private String javaVersion;
    private List<DependencyDTO> dependencies;
}