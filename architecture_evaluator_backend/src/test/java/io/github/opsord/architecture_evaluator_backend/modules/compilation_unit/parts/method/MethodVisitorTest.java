package io.github.opsord.architecture_evaluator_backend.modules.compilation_unit.parts.method;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.instances.class_instance.parts.method.MethodInstance;
import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.class_instance.parts.method.MethodVisitor;
import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.class_instance.parts.statement.StatementService;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MethodVisitorTest {

    @Test
    void testSimpleMethodExtraction() {
        String source = """
            class TestClass {
                public int sum(int a, int b) {
                    return a + b;
                }
            }
        """;
        CompilationUnit cu = StaticJavaParser.parse(source);
        ClassOrInterfaceDeclaration clazz = cu.getClassByName("TestClass").orElseThrow();

        List<MethodInstance> methods = new ArrayList<>();
        MethodVisitor visitor = new MethodVisitor(new StatementService());
        clazz.accept(visitor, methods);

        assertEquals(1, methods.size());
        MethodInstance method = methods.get(0);
        assertEquals("sum", method.getName());
        assertEquals("int", method.getBasicInfo().getReturnType());
        assertEquals(2, method.getInputParameters().size());
        assertEquals("a", method.getInputParameters().get(0).getName());
        assertEquals("b", method.getInputParameters().get(1).getName());
        assertEquals("int", method.getInputParameters().get(0).getType());
        assertEquals("int", method.getInputParameters().get(1).getType());
        assertEquals(1, method.getOutputParameters().size());
        assertEquals("int", method.getOutputParameters().get(0).getType());
        assertTrue(method.getMethodMetrics().getLinesOfCode() > 0);
    }

    @Test
    void testVoidMethodExtraction() {
        String source = """
            class TestClass {
                public void doNothing() {}
            }
        """;
        CompilationUnit cu = StaticJavaParser.parse(source);
        ClassOrInterfaceDeclaration clazz = cu.getClassByName("TestClass").orElseThrow();

        List<MethodInstance> methods = new ArrayList<>();
        MethodVisitor visitor = new MethodVisitor(new StatementService());
        clazz.accept(visitor, methods);

        assertEquals(1, methods.size());
        MethodInstance method = methods.get(0);
        assertEquals("doNothing", method.getName());
        assertEquals("void", method.getBasicInfo().getReturnType());
        assertEquals(0, method.getInputParameters().size());
        assertEquals(0, method.getOutputParameters().size());
    }

    @Test
    void testMethodWithAnnotations() {
        String source = """
            class TestClass {
                @Deprecated
                public String greet(String name) {
                    return "Hello " + name;
                }
            }
        """;
        CompilationUnit cu = StaticJavaParser.parse(source);
        ClassOrInterfaceDeclaration clazz = cu.getClassByName("TestClass").orElseThrow();

        List<MethodInstance> methods = new ArrayList<>();
        MethodVisitor visitor = new MethodVisitor(new StatementService());
        clazz.accept(visitor, methods);

        assertEquals(1, methods.size());
        MethodInstance method = methods.get(0);
        assertEquals("greet", method.getName());
        assertEquals("String", method.getBasicInfo().getReturnType());
        assertEquals(1, method.getInputParameters().size());
        assertEquals("name", method.getInputParameters().get(0).getName());
        assertEquals("String", method.getInputParameters().get(0).getType());
    }
}