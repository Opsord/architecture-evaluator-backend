package io.github.opsord.architecture_evaluator_backend.modules.compilation_unit.parts.annotation;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.class_instance.parts.annotation.AnnotationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AnnotationServiceTest {

    private AnnotationService annotationService;

    @BeforeEach
    void setUp() {
        annotationService = new AnnotationService();
    }

    @Test
    void testGetAnnotationsFromClass_singleAnnotation() {
        String source = """
            @Deprecated
            class Demo {}
        """;
        CompilationUnit cu = StaticJavaParser.parse(source);
        ClassOrInterfaceDeclaration clazz = cu.getClassByName("Demo").orElseThrow();

        List<String> annotations = annotationService.getAnnotationsFromClass(clazz);

        assertEquals(1, annotations.size());
        assertTrue(annotations.contains("Deprecated"));
    }

    @Test
    void testGetAnnotationsFromClass_multipleAnnotations() {
        String source = """
            @Entity
            @Table(name = "demo")
            class Demo {}
        """;
        CompilationUnit cu = StaticJavaParser.parse(source);
        ClassOrInterfaceDeclaration clazz = cu.getClassByName("Demo").orElseThrow();

        List<String> annotations = annotationService.getAnnotationsFromClass(clazz);

        assertEquals(2, annotations.size());
        assertTrue(annotations.contains("Entity"));
        assertTrue(annotations.contains("Table"));
    }

    @Test
    void testGetAnnotationsFromClass_noAnnotations() {
        String source = "class Demo {}";
        CompilationUnit cu = StaticJavaParser.parse(source);
        ClassOrInterfaceDeclaration clazz = cu.getClassByName("Demo").orElseThrow();

        List<String> annotations = annotationService.getAnnotationsFromClass(clazz);

        assertTrue(annotations.isEmpty());
    }

    @Test
    void testGetAnnotationsFromFile_multipleClasses() {
        String source = """
            @Entity
            class A {}

            @Repository
            class B {}

            class C {}
        """;
        CompilationUnit cu = StaticJavaParser.parse(source);

        List<String> annotations = annotationService.getAnnotationsFromFile(cu);

        assertEquals(2, annotations.size());
        assertTrue(annotations.contains("Entity"));
        assertTrue(annotations.contains("Repository"));
    }
}