package io.github.opsord.architecture_evaluator_backend.modules.compilation_unit.parts.variable;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.instances.class_instance.parts.VariableInstance;
import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.class_instance.parts.variable.VariableService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class VariableServiceTest {

    private VariableService variableService;

    @BeforeEach
    void setUp() {
        variableService = new VariableService();
    }

    @Test
    void testGetVariablesReturnsInstanceVariables() {
        String source = """
            class Demo {
                private int number;
                String name;
            }
        """;
        CompilationUnit cu = StaticJavaParser.parse(source);
        ClassOrInterfaceDeclaration clazz = cu.getClassByName("Demo").orElseThrow();

        List<VariableInstance> variables = variableService.getVariables(clazz);

        assertEquals(2, variables.size());
        assertTrue(variables.stream().allMatch(v -> v.getScope().equals("instance")));
        assertTrue(variables.stream().anyMatch(v -> v.getName().equals("number") && v.getType().equals("int")));
        assertTrue(variables.stream().anyMatch(v -> v.getName().equals("name") && v.getType().equals("String")));
    }

    @Test
    void testGetVariablesReturnsLocalVariables() {
        String source = """
            class Demo {
                void foo() {
                    int x = 1;
                    String y = "test";
                }
            }
        """;
        CompilationUnit cu = StaticJavaParser.parse(source);
        ClassOrInterfaceDeclaration clazz = cu.getClassByName("Demo").orElseThrow();

        List<VariableInstance> variables = variableService.getVariables(clazz);

        assertEquals(2, variables.size());
        assertTrue(variables.stream().allMatch(v -> v.getScope().equals("local")));
        assertTrue(variables.stream().anyMatch(v -> v.getName().equals("x") && v.getType().equals("int")));
        assertTrue(variables.stream().anyMatch(v -> v.getName().equals("y") && v.getType().equals("String")));
    }

    @Test
    void testGetVariablesReturnsEmptyListForNoVariables() {
        String source = """
            class Demo {
                void foo() {}
            }
        """;
        CompilationUnit cu = StaticJavaParser.parse(source);
        ClassOrInterfaceDeclaration clazz = cu.getClassByName("Demo").orElseThrow();

        List<VariableInstance> variables = variableService.getVariables(clazz);

        assertTrue(variables.isEmpty());
    }
}