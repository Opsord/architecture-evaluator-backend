package io.github.opsord.architecture_evaluator_backend.modules.compilation_unit.parts.variable;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.instances.class_instance.parts.VariableInstance;
import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.class_instance.parts.variable.VariableVisitor;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class VariableVisitorTest {

    @Test
    void testDetectsInstanceVariable() {
        String source = """
            class Demo {
                private int number;
            }
        """;
        CompilationUnit cu = StaticJavaParser.parse(source);
        ClassOrInterfaceDeclaration clazz = cu.getClassByName("Demo").orElseThrow();

        List<VariableInstance> variables = new ArrayList<>();
        VariableVisitor visitor = new VariableVisitor();
        clazz.accept(visitor, variables);

        assertEquals(1, variables.size());
        VariableInstance var = variables.get(0);
        assertEquals("number", var.getName());
        assertEquals("int", var.getType());
        assertEquals("instance", var.getScope());
    }

    @Test
    void testDetectsLocalVariable() {
        String source = """
            class Demo {
                void foo() {
                    String text = "hello";
                }
            }
        """;
        CompilationUnit cu = StaticJavaParser.parse(source);
        ClassOrInterfaceDeclaration clazz = cu.getClassByName("Demo").orElseThrow();

        List<VariableInstance> variables = new ArrayList<>();
        VariableVisitor visitor = new VariableVisitor();
        clazz.accept(visitor, variables);

        assertEquals(1, variables.size());
        VariableInstance var = variables.get(0);
        assertEquals("text", var.getName());
        assertEquals("String", var.getType());
        assertEquals("local", var.getScope());
    }

    @Test
    void testDetectsMultipleVariables() {
        String source = """
            class Demo {
                int a, b;
                void foo() {
                    double x = 1.0, y = 2.0;
                }
            }
        """;
        CompilationUnit cu = StaticJavaParser.parse(source);
        ClassOrInterfaceDeclaration clazz = cu.getClassByName("Demo").orElseThrow();

        List<VariableInstance> variables = new ArrayList<>();
        VariableVisitor visitor = new VariableVisitor();
        clazz.accept(visitor, variables);

        // Should detect 2 instances and 2 local variables
        assertEquals(4, variables.size());
        assertTrue(variables.stream().anyMatch(v -> v.getName().equals("a") && v.getScope().equals("instance")));
        assertTrue(variables.stream().anyMatch(v -> v.getName().equals("b") && v.getScope().equals("instance")));
        assertTrue(variables.stream().anyMatch(v -> v.getName().equals("x") && v.getScope().equals("local")));
        assertTrue(variables.stream().anyMatch(v -> v.getName().equals("y") && v.getScope().equals("local")));
    }
}