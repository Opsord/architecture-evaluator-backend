package io.github.opsord.architecture_evaluator_backend.modules.compilation_unit.class_instance;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.instances.file_types.ClassInstance;
import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.instances.JavaFileType;
import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.file_types.class_instance.ClassVisitor;
import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.file_types.annotation_instance.AnnotationService;
import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.parts.constructor.ConstructorService;
import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.parts.exception_handler.ExceptionHandlerService;
import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.file_types.interface_instance.InterfaceService;
import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.parts.method.MethodService;
import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.parts.statement.StatementService;
import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.parts.variable.VariableService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ClassVisitorTest {

    private ClassVisitor classVisitor;

    @BeforeEach
    void setUp() {
        classVisitor = new ClassVisitor(
                new MethodService(new StatementService()),
                new StatementService(),
                new VariableService(),
                new ConstructorService(),
                new AnnotationService(),
                new InterfaceService(
                        new AnnotationService(),
                        new MethodService(new StatementService())
                ),
                new ExceptionHandlerService()
        );
    }

    @Test
    void testDetectsInterface() {
        String source = "interface MyInterface {}";
        CompilationUnit cu = StaticJavaParser.parse(source);
        List<ClassInstance> result = new ArrayList<>();
        cu.accept(classVisitor, result);

        assertEquals(JavaFileType.INTERFACE, result.get(0).getJavaFileType());
    }

    @Test
    void testDetectsEnum() {
        String source = "enum MyEnum { A, B }";
        CompilationUnit cu = StaticJavaParser.parse(source);
        List<ClassInstance> result = new ArrayList<>();
        cu.accept(classVisitor, result);

        assertEquals(JavaFileType.ENUM, result.get(0).getJavaFileType());
    }

    @Test
    void testDetectsAnnotationType() {
        String source = "@interface MyAnno {}";
        CompilationUnit cu = StaticJavaParser.parse(source);
        List<ClassInstance> result = new ArrayList<>();
        cu.accept(classVisitor, result);

        assertEquals(JavaFileType.ANNOTATION, result.get(0).getJavaFileType());
    }

    @Test
    void testDetectsRecord() {
        String source = "record MyRecord(int x, String y) {}";
        CompilationUnit cu = StaticJavaParser.parse(source);
        List<ClassInstance> result = new ArrayList<>();
        cu.accept(classVisitor, result);

        assertEquals(JavaFileType.RECORD, result.get(0).getJavaFileType());
    }

    @Test
    void testDetectsAbstractClass() {
        String source = "abstract class MyAbstract {}";
        CompilationUnit cu = StaticJavaParser.parse(source);
        List<ClassInstance> result = new ArrayList<>();
        cu.accept(classVisitor, result);

        assertEquals(JavaFileType.ABSTRACT_CLASS, result.get(0).getJavaFileType());
    }

    @Test
    void testDetectsRegularClass() {
        String source = "class MyClass {}";
        CompilationUnit cu = StaticJavaParser.parse(source);
        List<ClassInstance> result = new ArrayList<>();
        cu.accept(classVisitor, result);

        assertEquals(JavaFileType.CLASS, result.get(0).getJavaFileType());
    }

    @Test
    void testLayerAnnotationDetection() {
        String source = """
        @Service
        class ServiceClass {}
        @Repository
        class RepoClass {}
        @Controller
        class ControllerClass {}
        @Entity
        class EntityClass {}
        @Document
        class DocumentClass {}
        @SpringBootTest
        class TestClass {}
        class OtherClass {}
    """;
        CompilationUnit cu = StaticJavaParser.parse(source);
        List<ClassInstance> result = new ArrayList<>();
        cu.accept(classVisitor, result);

        assertEquals("SERVICE", result.get(0).getLayerAnnotation().name());
        assertEquals("REPOSITORY", result.get(1).getLayerAnnotation().name());
        assertEquals("CONTROLLER", result.get(2).getLayerAnnotation().name());
        assertEquals("ENTITY", result.get(3).getLayerAnnotation().name());
        assertEquals("DOCUMENT", result.get(4).getLayerAnnotation().name());
        assertEquals("TESTING", result.get(5).getLayerAnnotation().name());
        assertEquals("OTHER", result.get(6).getLayerAnnotation().name());
    }

    @Test
    void testInheritanceAndInterfaces() {
        String source = """
        class Parent {}
        interface MyIntf {}
        class Child extends Parent implements MyIntf {}
    """;
        CompilationUnit cu = StaticJavaParser.parse(source);
        List<ClassInstance> result = new ArrayList<>();
        cu.accept(classVisitor, result);

        ClassInstance child = result.stream().filter(c -> "Child".equals(c.getName())).findFirst().orElseThrow();
        assertTrue(child.getSuperClasses().contains("Parent"));
        assertTrue(child.getImplementedInterfaces().contains("MyIntf"));
    }

    @Test
    void testExceptionHandlingExtraction() {
        String source = """
        class Demo {
            void foo() {
                try { int x = 1/0; } catch(Exception e) {}
            }
        }
    """;
        CompilationUnit cu = StaticJavaParser.parse(source);
        List<ClassInstance> result = new ArrayList<>();
        cu.accept(classVisitor, result);

        ClassInstance demo = result.get(0);
        assertFalse(demo.getExceptionHandling().isEmpty());
    }

    @Test
    void testUsedClassesExtraction() {
        String source = """
        import java.util.List;
        class Demo extends Base implements Runnable {
            List<String> items;
            public void run() {}
        }
    """;
        CompilationUnit cu = StaticJavaParser.parse(source);
        List<ClassInstance> result = new ArrayList<>();
        cu.accept(classVisitor, result);

        ClassInstance demo = result.get(0);
        assertTrue(demo.getUsedClasses().contains("Base"));
        assertTrue(demo.getUsedClasses().contains("Runnable"));
        assertTrue(demo.getUsedClasses().contains("List"));
        assertTrue(demo.getUsedClasses().contains("String"));
    }

    @Test
    void testLinesOfCodeCalculation() {
        String source = """
        class Demo {
            void foo() {
                int x = 1;
            }
        }
    """;
        CompilationUnit cu = StaticJavaParser.parse(source);
        List<ClassInstance> result = new ArrayList<>();
        cu.accept(classVisitor, result);

        ClassInstance demo = result.get(0);
        // Should be 6 lines (class line + 2 method lines + 3 braces/empty lines)
        assertTrue(demo.getLinesOfCode() >= 4);
    }
}