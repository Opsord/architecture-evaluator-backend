package io.github.opsord.architecture_evaluator_backend.modules.compilation_unit.class_instance.parts.method;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.instances.parts.method.MethodInstance;
import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.parts.method.MethodService;
import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.parts.statement.StatementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MethodServiceTest {

    private MethodService methodService;

    @BeforeEach
    void setUp() {
        methodService = new MethodService(new StatementService());
    }

    @Test
    void testExtractsSingleMethod() {
        String source = """
            class Demo {
                public int add(int x, int y) { return x + y; }
            }
        """;
        CompilationUnit cu = StaticJavaParser.parse(source);
        ClassOrInterfaceDeclaration clazz = cu.getClassByName("Demo").orElseThrow();

        List<MethodInstance> methods = methodService.getMethods(clazz);

        assertEquals(1, methods.size());
        MethodInstance method = methods.get(0);
        assertEquals("add", method.getName());
        assertEquals("int", method.getBasicInfo().getReturnType());
        assertEquals(2, method.getInputParameters().size());
    }

    @Test
    void testExtractsMultipleMethods() {
        String source = """
            class Multi {
                void a() {}
                String b(int x) { return "" + x; }
            }
        """;
        CompilationUnit cu = StaticJavaParser.parse(source);
        ClassOrInterfaceDeclaration clazz = cu.getClassByName("Multi").orElseThrow();

        List<MethodInstance> methods = methodService.getMethods(clazz);

        assertEquals(2, methods.size());
        assertTrue(methods.stream().anyMatch(m -> m.getName().equals("a")));
        assertTrue(methods.stream().anyMatch(m -> m.getName().equals("b")));
    }

    @Test
    void testNoMethodsReturnsEmptyList() {
        String source = "class Empty {}";
        CompilationUnit cu = StaticJavaParser.parse(source);
        ClassOrInterfaceDeclaration clazz = cu.getClassByName("Empty").orElseThrow();

        List<MethodInstance> methods = methodService.getMethods(clazz);

        assertTrue(methods.isEmpty());
    }
}