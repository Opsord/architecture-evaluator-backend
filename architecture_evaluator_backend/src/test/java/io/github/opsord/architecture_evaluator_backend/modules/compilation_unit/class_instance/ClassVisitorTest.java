package io.github.opsord.architecture_evaluator_backend.modules.compilation_unit.class_instance;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.instances.class_instance.ClassInstance;
import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.instances.class_instance.parts.JavaFileType;
import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.instances.class_instance.parts.LayerAnnotation;
import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.class_instance.ClassService;
import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.class_instance.parts.annotation.AnnotationService;
import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.class_instance.parts.constructor.ConstructorService;
import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.class_instance.parts.exception_handler.ExceptionHandlerService;
import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.class_instance.parts.interface_instance.InterfaceService;
import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.class_instance.parts.method.MethodService;
import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.class_instance.parts.statement.StatementService;
import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.class_instance.parts.variable.VariableService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ClassVisitorTest {

    private ClassService classService;

    @BeforeEach
    void setUp() {
        StatementService statementService = new StatementService();
        MethodService methodService = new MethodService(statementService);
        VariableService variableService = new VariableService();
        ConstructorService constructorService = new ConstructorService();
        AnnotationService annotationService = new AnnotationService();
        InterfaceService interfaceService = new InterfaceService();
        ExceptionHandlerService exceptionHandlerService = new ExceptionHandlerService();

        classService = new ClassService(
                methodService,
                statementService,
                variableService,
                constructorService,
                annotationService,
                interfaceService,
                exceptionHandlerService
        );
    }

    @Test
    void testComplexClassParsing() {
        String source = """
        package com.example;

        import org.springframework.stereotype.Service;
        import java.util.List;
        import java.io.IOException;

        @Service
        public class MyService extends BaseClass implements Runnable {
        
            private String name;

            public MyService() {}

            public List<User> getUsers() {
                try {
                    return List.of(new User());
                } catch (Exception e) {
                    e.printStackTrace();
                    return List.of();
                }
            }

            @Override
            public void run() {}

            class InnerHelper {}
        }
        """;

        CompilationUnit cu = StaticJavaParser.parse(source);
        List<ClassInstance> result = classService.getClassesFromCompUnit(cu);

        // Only test the main class
        ClassInstance instance = result.stream()
                .filter(ci -> "MyService".equals(ci.getName()))
                .findFirst()
                .orElseThrow();

        // --- Identity ---
        assertEquals("MyService", instance.getName());
        assertEquals(JavaFileType.CLASS, instance.getJavaFileType());
        assertEquals(LayerAnnotation.SERVICE, instance.getLayerAnnotation());

        // --- Inheritance & Interfaces ---
        assertTrue(instance.getSuperClasses().contains("BaseClass"));
        assertTrue(instance.getImplementedInterfaces().contains("Runnable"));

        // --- Annotations ---
        assertTrue(instance.getAnnotations().contains("Service"));

        // --- Variables, methods, constructor ---
        assertEquals(1, instance.getClassVariables().size());
        assertEquals(1, instance.getConstructors().size());
        assertEquals(2, instance.getMethods().size()); // run() y getUsers()

        // --- Used classes ---
        List<String> used = instance.getUsedClasses();
        assertTrue(used.contains("List"));
        assertTrue(used.contains("User"));
        assertTrue(used.contains("Exception"));

        // --- Inner classes ---
        assertEquals(1, instance.getInnerClasses().size());
        assertEquals("InnerHelper", instance.getInnerClasses().get(0).getName());

        // --- Lines of code ---
        assertTrue(instance.getLinesOfCode() > 0);
    }

    @Test
    void testLayerAnnotationsDetection() {
        String source = """
            @Entity
            class EntityClass {}

            @Repository
            class RepoClass {}

            @Controller
            class ControllerClass {}

            @RestController
            class RestControllerClass {}

            @Document
            class DocumentClass {}

            @Custom
            class CustomClass {}
        """;

        CompilationUnit cu = StaticJavaParser.parse(source);
        List<ClassInstance> result = classService.getClassesFromCompUnit(cu);

        assertEquals(6, result.size());

        assertEquals(LayerAnnotation.ENTITY, result.get(0).getLayerAnnotation());
        assertEquals(LayerAnnotation.REPOSITORY, result.get(1).getLayerAnnotation());
        assertEquals(LayerAnnotation.CONTROLLER, result.get(2).getLayerAnnotation());
        assertEquals(LayerAnnotation.CONTROLLER, result.get(3).getLayerAnnotation());
        assertEquals(LayerAnnotation.DOCUMENT, result.get(4).getLayerAnnotation());
    }

}