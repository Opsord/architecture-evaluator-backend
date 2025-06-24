package io.github.opsord.architecture_evaluator_backend.modules.compilation_unit.class_instance.parts.exception_handler;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.instances.parts.ExceptionHandlingInstance;
import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.parts.exception_handler.ExceptionHandlerVisitor;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ExceptionHandlerVisitorTest {

    @Test
    void testDetectsTryCatchFinally() {
        String source = """
            class Demo {
                void foo() {
                    try {
                        int x = 1 / 0;
                    } catch (ArithmeticException e) {
                        System.out.println("Error");
                    } finally {
                        System.out.println("Done");
                    }
                }
            }
        """;
        CompilationUnit cu = StaticJavaParser.parse(source);
        ClassOrInterfaceDeclaration clazz = cu.getClassByName("Demo").orElseThrow();

        List<ExceptionHandlingInstance> handlers = new ArrayList<>();
        ExceptionHandlerVisitor visitor = new ExceptionHandlerVisitor();
        clazz.accept(visitor, handlers);

        assertEquals(1, handlers.size());
        ExceptionHandlingInstance instance = handlers.get(0);
        assertTrue(instance.getTryBlock().contains("int x = 1 / 0;"));
        assertEquals(1, instance.getCatchBlocks().size());
        assertTrue(instance.getCatchBlocks().get(0).contains("catch (ArithmeticException e)"));
        assertNotNull(instance.getFinallyBlock());
        assertTrue(instance.getFinallyBlock().contains("System.out.println(\"Done\");"));
    }

    @Test
    void testDetectsMultipleTryStatements() {
        String source = """
            class Demo {
                void foo() {
                    try { int a = 0; } catch (Exception e) {}
                    try { int b = 1; } finally { System.out.println("F"); }
                }
            }
        """;
        CompilationUnit cu = StaticJavaParser.parse(source);
        ClassOrInterfaceDeclaration clazz = cu.getClassByName("Demo").orElseThrow();

        List<ExceptionHandlingInstance> handlers = new ArrayList<>();
        ExceptionHandlerVisitor visitor = new ExceptionHandlerVisitor();
        clazz.accept(visitor, handlers);

        assertEquals(2, handlers.size());
        assertTrue(handlers.get(0).getTryBlock().contains("int a = 0;"));
        assertEquals(1, handlers.get(0).getCatchBlocks().size());
        assertNull(handlers.get(0).getFinallyBlock());

        assertTrue(handlers.get(1).getTryBlock().contains("int b = 1;"));
        assertEquals(0, handlers.get(1).getCatchBlocks().size());
        assertNotNull(handlers.get(1).getFinallyBlock());
    }

    @Test
    void testNoTryCatchReturnsEmptyList() {
        String source = """
            class Demo {
                void noTry() {
                    int x = 5;
                }
            }
        """;
        CompilationUnit cu = StaticJavaParser.parse(source);
        ClassOrInterfaceDeclaration clazz = cu.getClassByName("Demo").orElseThrow();

        List<ExceptionHandlingInstance> handlers = new ArrayList<>();
        ExceptionHandlerVisitor visitor = new ExceptionHandlerVisitor();
        clazz.accept(visitor, handlers);

        assertTrue(handlers.isEmpty());
    }
}