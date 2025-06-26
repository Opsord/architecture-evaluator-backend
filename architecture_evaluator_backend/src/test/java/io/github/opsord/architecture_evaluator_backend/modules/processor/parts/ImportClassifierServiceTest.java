package io.github.opsord.architecture_evaluator_backend.modules.processor.parts;

import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.instances.file_instance.FileInstance;
import io.github.opsord.architecture_evaluator_backend.modules.parser.processor.dto.parts.ImportCategory;
import io.github.opsord.architecture_evaluator_backend.modules.parser.processor.services.parts.ImportClassifierService;
import io.github.opsord.architecture_evaluator_backend.modules.parser.project_scanner.instances.pom.PomDependencyInstance;
import io.github.opsord.architecture_evaluator_backend.modules.parser.project_scanner.instances.pom.ParentSectionDTO;
import io.github.opsord.architecture_evaluator_backend.modules.parser.project_scanner.instances.pom.PomFileInstance;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class ImportClassifierServiceTest {

    private final ImportClassifierService service = new ImportClassifierService();

    private FileInstance fileWithImports(List<String> imports) {
        FileInstance file = new FileInstance();
        file.setImportedPackages(imports);
        return file;
    }

    private PomDependencyInstance dep(String groupId, String artifactId, String basePackage) {
        PomDependencyInstance d = new PomDependencyInstance();
        d.setGroupId(groupId);
        d.setArtifactId(artifactId);
        // getBasePackage() uses groupId, so set groupId accordingly
        return d;
    }

    private ParentSectionDTO parent(String groupId, String artifactId) {
        ParentSectionDTO p = new ParentSectionDTO();
        p.setGroupId(groupId);
        p.setArtifactId(artifactId);
        return p;
    }

    @Test
    void classifyDependencies_allCategories() {
        List<String> imports = Arrays.asList(
                "java.util.List",
                "javax.annotation.PostConstruct",
                "org.springframework.beans.factory.annotation.Autowired",
                "com.mycompany.project.module.ServiceClass",
                "parent.groupid.something.SomeClass",
                "external.known.lib.SomeClass",
                "com.unknown.lib.SomeClass"
        );
        FileInstance file = fileWithImports(imports);

        PomDependencyInstance knownDep = dep("external.known.lib", "lib", "external.known.lib");
        ParentSectionDTO parentSection = parent("parent.groupid", "parent-artifact");
        PomFileInstance pom = new PomFileInstance();
        pom.setDependencies(List.of(knownDep));
        pom.setParentSection(parentSection);

        Map<ImportCategory, List<String>> result = service.classifyDependencies(
                Optional.of(pom), file, "com.mycompany.project"
        );

        assertEquals(List.of("java.util.List", "javax.annotation.PostConstruct"), result.get(ImportCategory.JAVA_STANDARD));
        assertEquals(List.of("org.springframework.beans.factory.annotation.Autowired"), result.get(ImportCategory.SPRING));
        assertEquals(List.of("com.mycompany.project.module.ServiceClass"), result.get(ImportCategory.INTERNAL));
        assertEquals(List.of("parent.groupid.something.SomeClass"), result.get(ImportCategory.PARENT_DEPENDENCY));
        assertEquals(List.of("external.known.lib.SomeClass"), result.get(ImportCategory.EXTERNAL_KNOWN));
        assertEquals(List.of("com.unknown.lib.SomeClass"), result.get(ImportCategory.EXTERNAL_UNKNOWN));
    }

    @Test
    void classify_singleImport() {
        PomDependencyInstance knownDep = dep("external.known.lib", "lib", "external.known.lib");
        ParentSectionDTO parentSection = parent("parent.groupid", "parent-artifact");
        List<PomDependencyInstance> deps = List.of(knownDep);

        assertEquals(ImportCategory.JAVA_STANDARD,
                service.classify("java.util.Map", deps, parentSection, "com.mycompany.project"));
        assertEquals(ImportCategory.SPRING,
                service.classify("org.springframework.context.ApplicationContext", deps, parentSection, "com.mycompany.project"));
        assertEquals(ImportCategory.INTERNAL,
                service.classify("com.mycompany.project.service.MyService", deps, parentSection, "com.mycompany.project"));
        assertEquals(ImportCategory.PARENT_DEPENDENCY,
                service.classify("parent.groupid.foo.Bar", deps, parentSection, "com.mycompany.project"));
        assertEquals(ImportCategory.EXTERNAL_KNOWN,
                service.classify("external.known.lib.SomeClass", deps, parentSection, "com.mycompany.project"));
        assertEquals(ImportCategory.EXTERNAL_UNKNOWN,
                service.classify("com.unknown.lib.SomeClass", deps, parentSection, "com.mycompany.project"));
    }

    @Test
    void classifyDependencies_nullPomOrImports() {
        FileInstance file = new FileInstance();
        file.setImportedPackages(null);
        Map<ImportCategory, List<String>> result = service.classifyDependencies(Optional.empty(), file, "base");
        assertTrue(result.isEmpty());
    }
}