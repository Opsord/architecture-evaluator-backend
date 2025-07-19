package io.github.opsord.architecture_evaluator_backend.modules.parser.parts.exception_handler;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import io.github.opsord.architecture_evaluator_backend.modules.api.parser.instances.class_instance.parts.ExceptionHandlingInstance;
import io.github.opsord.architecture_evaluator_backend.modules.api.parser.services.class_instance.parts.exception_handler.ExceptionHandlerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ExceptionHandlerServiceTest {

    private ExceptionHandlerService exceptionHandlerService;

    @BeforeEach
    void setUp() {
        exceptionHandlerService = new ExceptionHandlerService();
    }

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

        List<ExceptionHandlingInstance> handlers = exceptionHandlerService.getExceptionHandling(clazz);

        assertEquals(1, handlers.size());
        ExceptionHandlingInstance instance = handlers.get(0);
        assertTrue(instance.getTryBlock().contains("int x = 1 / 0;"));
        assertEquals(1, instance.getCatchBlocks().size());
        assertTrue(instance.getCatchBlocks().get(0).contains("catch (ArithmeticException e)"));
        assertNotNull(instance.getFinallyBlock());
        assertTrue(instance.getFinallyBlock().contains("System.out.println(\"Done\");"));
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

        List<ExceptionHandlingInstance> handlers = exceptionHandlerService.getExceptionHandling(clazz);

        assertTrue(handlers.isEmpty());
    }
}