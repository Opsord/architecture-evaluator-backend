package io.github.Opsord.architecture_evaluator_backend.modules.parser.processor.services.analysis.parts;

import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.instances.CustomCompilationUnit;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.project_scanner.dto.pom.DependencyDTO;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.processor.dto.analysis.parts.ImportCategory;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.project_scanner.dto.pom.ParentSectionDTO;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.project_scanner.dto.pom.PomFileDTO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ImportClassifierService {

    public Map<ImportCategory, List<String>> classifyDependencies(PomFileDTO pomFileDTO, CustomCompilationUnit compilationUnitDTO, String internalBasePackage) {
        Map<ImportCategory, List<String>> classifiedDependencies = new HashMap<>();

        List<String> importedPackages = compilationUnitDTO.getImportedPackages();
        ParentSectionDTO parentSection = pomFileDTO.getParentSection();

        for (String importName : importedPackages) {
            ImportCategory category = classify(importName, pomFileDTO.getDependencies(), parentSection, internalBasePackage);
            classifiedDependencies.computeIfAbsent(category, k -> new ArrayList<>()).add(importName);
        }

        return classifiedDependencies;
    }

    public ImportCategory classify(String importName, List<DependencyDTO> knownDependencies, ParentSectionDTO parentSection, String internalBasePackage) {
        if (importName.startsWith("java.") || importName.startsWith("javax.")) {
            return ImportCategory.JAVA_STANDARD;
        } else if (importName.startsWith("spring") || importName.startsWith("org.springframework")) {
            return ImportCategory.SPRING;
        } else if (importName.startsWith(internalBasePackage)) {
            return ImportCategory.INTERNAL;
        } else if (matchesParentSection(importName, parentSection)) {
            return ImportCategory.PARENT_DEPENDENCY;
        } else if (matchesAnyDependency(importName, knownDependencies)) {
            return ImportCategory.EXTERNAL_KNOWN;
        } else {
            return ImportCategory.EXTERNAL_UNKNOWN;
        }
    }

    private boolean matchesParentSection(String importName, ParentSectionDTO parentSection) {
        if (parentSection == null) {
            return false;
        }
        String parentGroupId = parentSection.getGroupId();
        String parentArtifactId = parentSection.getArtifactId();
        return (parentGroupId != null && importName.startsWith(parentGroupId)) ||
                (parentArtifactId != null && importName.startsWith(parentArtifactId));
    }

    private boolean matchesAnyDependency(String importName, List<DependencyDTO> dependencies) {
        for (DependencyDTO dep : dependencies) {
            String basePackage = dep.getBasePackage().replace(".", "/");
            String artifactId = dep.getArtifactId();
            // Check if the import name starts with the base package or contains the artifactId
            if (importName.startsWith(basePackage) || artifactId != null && importName.contains(artifactId)) {
                return true;
            }
        }
        return false;
    }
}
