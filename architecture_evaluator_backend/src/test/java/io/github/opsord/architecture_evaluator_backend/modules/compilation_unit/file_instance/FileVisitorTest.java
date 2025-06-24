package io.github.opsord.architecture_evaluator_backend.modules.compilation_unit.file_instance;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.instances.file_instance.FileInstance;
import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.class_instance.ClassService;
import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.class_instance.parts.annotation.AnnotationService;
import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.class_instance.parts.constructor.ConstructorService;
import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.class_instance.parts.exception_handler.ExceptionHandlerService;
import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.class_instance.parts.interface_instance.InterfaceService;
import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.class_instance.parts.method.MethodService;
import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.class_instance.parts.statement.StatementService;
import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.class_instance.parts.variable.VariableService;
import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.file_instance.FileInstanceVisitor;
import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.file_instance.package_part.PackageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FileVisitorTest {

    private FileInstanceVisitor visitor;

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
        visitor = new FileInstanceVisitor(packageService, annotationService, classService);
    }

    @Test
    void testFileInstanceVisitorParsesFileInfo() {
        String source = """
            package com.example.demo;
            import java.util.List;
            @Deprecated
            public class TestClass {}
        """;
        CompilationUnit cu = StaticJavaParser.parse(source);

        FileInstance fileInstance = new FileInstance();
        cu.accept(visitor, fileInstance);

        assertEquals("com.example.demo", fileInstance.getPackageName());
        assertTrue(fileInstance.getImportedPackages().contains("java.util.List"));
        assertTrue(fileInstance.getFileAnnotations().contains("Deprecated"));
        assertEquals(1, fileInstance.getClasses().size());
        assertEquals("TestClass", fileInstance.getClasses().get(0).getName());
        assertTrue(fileInstance.getLinesOfCode() > 0);
        assertEquals(1, fileInstance.getImportCount());
    }

    @Test
    void testFileInstanceVisitorHandlesDefaultPackage() {
        String source = "class A {}";
        CompilationUnit cu = StaticJavaParser.parse(source);

        FileInstance fileInstance = new FileInstance();
        cu.accept(visitor, fileInstance);

        assertEquals("default", fileInstance.getPackageName());
        assertEquals(1, fileInstance.getClasses().size());
        assertEquals("A", fileInstance.getClasses().get(0).getName());
    }
}