package io.github.opsord.architecture_evaluator_backend.modules.compilation_unit.class_instance;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.instances.class_instance.ClassInstance;
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

class ClassServiceTest {

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
    void testGetClassFromValidJavaClass() {
        String source = """
            package com.example;

            public class ExampleClass {
                private int number;
                public ExampleClass() {}
                public void doSomething() {}
            }
        """;

        CompilationUnit cu = StaticJavaParser.parse(source);
        List<ClassInstance> result = classService.getClassesFromCompUnit(cu);

        assertEquals(1, result.size(), "Debe haber una clase extraída");

        ClassInstance instance = result.get(0);
        assertEquals("ExampleClass", instance.getName(), "El nombre de la clase debe coincidir");
        assertEquals(1, instance.getConstructors().size(), "Debe haber un constructor detectado");
        assertEquals(1, instance.getMethods().size(), "Debe haber un método detectado");
        assertEquals(1, instance.getClassVariables().size(), "Debe haber una variable de clase detectada");
    }

    @Test
    void testEmptyCompilationUnitReturnsEmptyList() {
        String source = "";
        CompilationUnit cu = StaticJavaParser.parse(source);
        List<ClassInstance> result = classService.getClassesFromCompUnit(cu);

        assertTrue(result.isEmpty(), "No debería haber clases detectadas");
    }

    @Test
    void testInnerClassIsDetected() {
        String source = """
        public class Outer {
            class Inner {}
        }
    """;

        CompilationUnit cu = StaticJavaParser.parse(source);
        List<ClassInstance> result = classService.getClassesFromCompUnit(cu);

        assertEquals(2, result.size(), "Debe haber dos clases extraídas");

        // Find the outer class by name
        ClassInstance outer = result.stream()
                .filter(c -> "Outer".equals(c.getName()))
                .findFirst()
                .orElseThrow();

        assertEquals("Outer", outer.getName());
        assertEquals(1, outer.getInnerClasses().size(), "Debe detectar una clase interna");
        assertEquals("Inner", outer.getInnerClasses().get(0).getName(), "El nombre de la clase interna debe coincidir");
    }

    @Test
    void testMultipleTopLevelClasses() {
        String source = """
            class A {}
            class B {}
        """;

        CompilationUnit cu = StaticJavaParser.parse(source);
        List<ClassInstance> result = classService.getClassesFromCompUnit(cu);

        assertEquals(2, result.size(), "Debe detectar dos clases en el archivo");
        assertTrue(result.stream().anyMatch(c -> c.getName().equals("A")));
        assertTrue(result.stream().anyMatch(c -> c.getName().equals("B")));
    }
}
