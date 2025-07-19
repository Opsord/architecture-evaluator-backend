package io.github.opsord.architecture_evaluator_backend.modules.processor;

import io.github.opsord.architecture_evaluator_backend.modules.api.parser.instances.class_instance.ClassInstance;
import io.github.opsord.architecture_evaluator_backend.modules.api.parser.instances.class_instance.parts.JavaFileType;
import io.github.opsord.architecture_evaluator_backend.modules.api.parser.instances.file_instance.FileInstance;
import io.github.opsord.architecture_evaluator_backend.modules.api.processor.dto.ClassAnalysisInstance;
import io.github.opsord.architecture_evaluator_backend.modules.api.processor.dto.ProcessedClassInstance;
import io.github.opsord.architecture_evaluator_backend.modules.api.processor.dto.parts.ImportCategory;
import io.github.opsord.architecture_evaluator_backend.modules.api.processor.services.ClassAnalysisService;
import io.github.opsord.architecture_evaluator_backend.modules.api.processor.services.FileAnalysisService;
import io.github.opsord.architecture_evaluator_backend.modules.api.processor.services.parts.ImportClassifierService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FileAnalysisServiceTest {

    private ClassAnalysisService classAnalysisService;
    private ImportClassifierService importClassifierService;
    private FileAnalysisService fileAnalysisService;

    @BeforeEach
    void setUp() {
        classAnalysisService = mock(ClassAnalysisService.class);
        importClassifierService = mock(ImportClassifierService.class);
        fileAnalysisService = new FileAnalysisService(classAnalysisService, importClassifierService);
    }

    @Test
    void testAnalyseFileInstance_withClasses() {
        // Prepare a file with two classes
        ClassInstance class1 = new ClassInstance();
        class1.setJavaFileType(JavaFileType.CLASS);
        class1.setStatements(List.of());
        ClassInstance class2 = new ClassInstance();
        class2.setJavaFileType(JavaFileType.INTERFACE);

        FileInstance fileInstance = new FileInstance();
        fileInstance.setClasses(List.of(class1, class2));

        Map<ImportCategory, List<String>> classifiedDeps = Map.of(ImportCategory.JAVA_STANDARD, List.of("java.util.List"));
        when(importClassifierService.classifyDependencies(any(), eq(fileInstance), anyString())).thenReturn(classifiedDeps);

        // Mock ClassAnalysisInstance
        when(classAnalysisService.analyseClassInstance(any(), eq(classifiedDeps))).thenAnswer(invocation -> {
            ClassAnalysisInstance analysis = new ClassAnalysisInstance();
            analysis.setProgramMetrics(null);
            analysis.setClassifiedDependencies(classifiedDeps);
            return analysis;
        });

        List<ProcessedClassInstance> result = fileAnalysisService.analyseFileInstance(
                fileInstance, "com.example", Optional.empty());

        assertEquals(2, result.size());
        for (ProcessedClassInstance pci : result) {
            assertNotNull(pci.getClassInstance());
            assertNotNull(pci.getClassAnalysisInstance());
            assertEquals(classifiedDeps, pci.getClassAnalysisInstance().getClassifiedDependencies());
            // All metrics are per-file, so both should be 1, 1, 0
            assertEquals(1, pci.getClassAnalysisInstance().getClassCount());
            assertEquals(1, pci.getClassAnalysisInstance().getInterfaceCount());
            assertEquals(0, pci.getClassAnalysisInstance().getStatementCount());
        }
    }

    @Test
    void testAnalyseFileInstance_noClasses() {
        FileInstance fileInstance = new FileInstance();
        fileInstance.setClasses(Collections.emptyList());

        List<ProcessedClassInstance> result = fileAnalysisService.analyseFileInstance(
                fileInstance, "com.example", Optional.empty());

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testAnalyseFileInstance_nullClasses() {
        FileInstance fileInstance = new FileInstance();
        fileInstance.setClasses(null);

        List<ProcessedClassInstance> result = fileAnalysisService.analyseFileInstance(
                fileInstance, "com.example", Optional.empty());

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}