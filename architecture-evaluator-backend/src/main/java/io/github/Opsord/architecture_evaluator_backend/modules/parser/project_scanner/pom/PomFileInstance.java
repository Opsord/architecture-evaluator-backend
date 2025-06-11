package io.github.Opsord.architecture_evaluator_backend.modules.parser.project_scanner.pom;

import lombok.Data;

import java.util.List;

@Data
public class PomFileInstance {
    private ParentSectionDTO parentSection;
    private String groupId;
    private String artifactId;
    private String version;
    private String description;
    private String url;
    private String license;
    private List<String> developers;
    private String javaVersion;
    private List<PomDependencyInstance> dependencies;
}