package io.github.opsord.architecture_evaluator_backend.modules.parser.processor.services.parts;

import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.instances.file_instance.FileInstance;
import io.github.opsord.architecture_evaluator_backend.modules.parser.project_scanner.pom.PomDependencyInstance;
import io.github.opsord.architecture_evaluator_backend.modules.parser.processor.dto.parts.ImportCategory;
import io.github.opsord.architecture_evaluator_backend.modules.parser.project_scanner.pom.ParentSectionDTO;
import io.github.opsord.architecture_evaluator_backend.modules.parser.project_scanner.pom.PomFileInstance;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ImportClassifierService {

    /**
     * Classifies the imported packages of a file into categories based on the
     * project's POM and internal base package.
     *
     * @param pomFileInstance     the POM file data transfer object containing
     *                            dependencies and parent section
     * @param fileInstance        the file instance containing imported packages
     * @param internalBasePackage the base package of the internal project
     * @return a map where the key is the import category and the value is a list of
     *         import names in that category
     */
    public Map<ImportCategory, List<String>> classifyDependencies(Optional<PomFileInstance> pomFileInstance,
                                                                  FileInstance fileInstance, String internalBasePackage) {
        Map<ImportCategory, List<String>> classifiedDependencies = new HashMap<>();

        List<String> importedPackages = fileInstance.getImportedPackages();
        ParentSectionDTO parentSection = pomFileInstance.map(PomFileInstance::getParentSection).orElse(null);

        if (importedPackages != null) {
            List<PomDependencyInstance> dependencies = pomFileInstance.map(PomFileInstance::getDependencies).orElse(Collections.emptyList());
            for (String importName : importedPackages) {
                ImportCategory category = classify(importName, dependencies, parentSection, internalBasePackage);
                classifiedDependencies.computeIfAbsent(category, k -> new ArrayList<>()).add(importName);
            }
        }

        return classifiedDependencies;
    }

    /**
     * Classifies a single import name into an import category.
     *
     * @param importName          the import statement to classify
     * @param knownDependencies   the list of known dependencies from the POM
     * @param parentSection       the parent section of the POM
     * @param internalBasePackage the base package of the internal project
     * @return the determined import category
     */
    public ImportCategory classify(String importName, List<PomDependencyInstance> knownDependencies,
                                   ParentSectionDTO parentSection, String internalBasePackage) {
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

    /**
     * Checks if the import matches the parent section's groupId or artifactId.
     *
     * @param importName    the import statement
     * @param parentSection the parent section of the POM
     * @return true if the import matches the parent section, false otherwise
     */
    private boolean matchesParentSection(String importName, ParentSectionDTO parentSection) {
        if (parentSection == null) {
            return false;
        }
        String parentGroupId = parentSection.getGroupId();
        String parentArtifactId = parentSection.getArtifactId();
        return (parentGroupId != null && importName.startsWith(parentGroupId)) ||
                (parentArtifactId != null && importName.startsWith(parentArtifactId));
    }

    /**
     * Checks if the import matches any of the known dependencies by groupId or
     * artifactId.
     *
     * @param importName   the import statement
     * @param dependencies the list of dependencies from the POM
     * @return true if the import matches any dependency, false otherwise
     */
    private boolean matchesAnyDependency(String importName, List<PomDependencyInstance> dependencies) {
        for (PomDependencyInstance dep : dependencies) {
            String basePackage = dep.getBasePackage().replace(".", "/");
            String artifactId = dep.getArtifactId();
            if (importName.startsWith(basePackage) || (artifactId != null && importName.contains(artifactId))) {
                return true;
            }
        }
        return false;
    }
}