package io.github.opsord.architecture_evaluator_backend.modules.parser.file_instance;

import io.github.opsord.architecture_evaluator_backend.modules.api.parser.instances.class_instance.ClassInstance;
import io.github.opsord.architecture_evaluator_backend.modules.api.parser.instances.file_instance.FileInstance;
import io.github.opsord.architecture_evaluator_backend.modules.api.parser.services.class_instance.ClassService;
import io.github.opsord.architecture_evaluator_backend.modules.api.parser.services.class_instance.parts.annotation.AnnotationService;
import io.github.opsord.architecture_evaluator_backend.modules.api.parser.services.class_instance.parts.constructor.ConstructorService;
import io.github.opsord.architecture_evaluator_backend.modules.api.parser.services.class_instance.parts.exception_handler.ExceptionHandlerService;
import io.github.opsord.architecture_evaluator_backend.modules.api.parser.services.class_instance.parts.interface_instance.InterfaceService;
import io.github.opsord.architecture_evaluator_backend.modules.api.parser.services.class_instance.parts.method.MethodService;
import io.github.opsord.architecture_evaluator_backend.modules.api.parser.services.class_instance.parts.statement.StatementService;
import io.github.opsord.architecture_evaluator_backend.modules.api.parser.services.class_instance.parts.variable.VariableService;
import io.github.opsord.architecture_evaluator_backend.modules.api.parser.services.file_instance.FileInstanceService;
import io.github.opsord.architecture_evaluator_backend.modules.api.parser.services.file_instance.package_part.PackageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileWriter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FileServiceTest {

    private FileInstanceService fileInstanceService;

    @BeforeEach
    void setUp() {
        PackageService packageService = new PackageService();
        StatementService statementService = new StatementService();
        MethodService methodService = new MethodService(statementService);
        VariableService variableService = new VariableService();
        ConstructorService constructorService = new ConstructorService();
        AnnotationService annotationService = new AnnotationService();
        InterfaceService interfaceService = new InterfaceService();
        ExceptionHandlerService exceptionHandlerService = new ExceptionHandlerService();

        ClassService classService = new ClassService(
                methodService,
                statementService,
                variableService,
                constructorService,
                annotationService,
                interfaceService,
                exceptionHandlerService
        );
        fileInstanceService = new FileInstanceService(classService, packageService, annotationService);
    }

    @Test
    void testParseJavaFileReturnsFileInstance() throws Exception {
        File tempFile = File.createTempFile("TestClass", ".java");
        tempFile.deleteOnExit();
        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.write("package test.pkg;\npublic class TestClass {}");
        }
        File projectRoot = tempFile.getParentFile();

        FileInstance fileInstance = fileInstanceService.parseJavaFile(tempFile, projectRoot);

        assertTrue(fileInstance.getFileName().endsWith(".java"));
        assertTrue(fileInstance.getFileName().startsWith("TestClass"));
        assertEquals("test.pkg", fileInstance.getPackageName());
        assertEquals(1, fileInstance.getClasses().size());
        assertEquals("TestClass", fileInstance.getClasses().get(0).getName());
        assertNotNull(fileInstance.getFilePath());
    }

    @Test
    void testGetDependentClassNamesFromClass() {
        ClassInstance classA = new ClassInstance();
        classA.setName("A");
        classA.setUsedClasses(List.of("B", "C", "D"));

        ClassInstance classB = new ClassInstance();
        classB.setName("B");
        ClassInstance classC = new ClassInstance();
        classC.setName("C");

        FileInstance fileB = new FileInstance();
        fileB.setClasses(List.of(classB));
        FileInstance fileC = new FileInstance();
        fileC.setClasses(List.of(classC));
        FileInstance fileD = new FileInstance();
        fileD.setClasses(List.of());

        List<FileInstance> allFiles = List.of(fileB, fileC, fileD);

        List<String> deps = fileInstanceService.getDependentClassNamesFromClass(classA, allFiles);

        assertTrue(deps.contains("B"));
        assertTrue(deps.contains("C"));
        assertFalse(deps.contains("D"));
    }
}