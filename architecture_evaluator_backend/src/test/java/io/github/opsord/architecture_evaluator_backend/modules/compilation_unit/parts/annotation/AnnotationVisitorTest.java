package io.github.opsord.architecture_evaluator_backend.modules.compilation_unit.parts.annotation;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.class_instance.parts.annotation.AnnotationVisitor;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AnnotationVisitorTest {

    @Test
    void testMarkerAnnotation() {
        String source = """
            @Deprecated
            class Demo {}
        """;
        CompilationUnit cu = StaticJavaParser.parse(source);
        ClassOrInterfaceDeclaration clazz = cu.getClassByName("Demo").orElseThrow();

        List<String> annotations = new ArrayList<>();
        AnnotationVisitor visitor = new AnnotationVisitor();
        clazz.accept(visitor, annotations);

        assertEquals(1, annotations.size());
        assertTrue(annotations.contains("Deprecated"));
    }

    @Test
    void testMultipleAnnotations() {
        String source = """
            @Entity
            @Table(name = "demo")
            class Demo {}
        """;
        CompilationUnit cu = StaticJavaParser.parse(source);
        ClassOrInterfaceDeclaration clazz = cu.getClassByName("Demo").orElseThrow();

        List<String> annotations = new ArrayList<>();
        AnnotationVisitor visitor = new AnnotationVisitor();
        clazz.accept(visitor, annotations);

        assertEquals(2, annotations.size());
        assertTrue(annotations.contains("Entity"));
        assertTrue(annotations.contains("Table"));
    }

    @Test
    void testNoAnnotations() {
        String source = "class Demo {}";
        CompilationUnit cu = StaticJavaParser.parse(source);
        ClassOrInterfaceDeclaration clazz = cu.getClassByName("Demo").orElseThrow();

        List<String> annotations = new ArrayList<>();
        AnnotationVisitor visitor = new AnnotationVisitor();
        clazz.accept(visitor, annotations);

        assertTrue(annotations.isEmpty());
    }
}