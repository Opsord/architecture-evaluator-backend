package io.github.opsord.architecture_evaluator_backend.modules.parser.parts.statement;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import io.github.opsord.architecture_evaluator_backend.modules.api.parser.instances.class_instance.parts.method.parts.StatementsInfo;
import io.github.opsord.architecture_evaluator_backend.modules.api.parser.instances.class_instance.parts.method.parts.statement.StatementInstance;
import io.github.opsord.architecture_evaluator_backend.modules.api.parser.instances.class_instance.parts.method.parts.statement.StatementType;
import io.github.opsord.architecture_evaluator_backend.modules.api.parser.services.class_instance.parts.statement.StatementService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StatementServiceTest {

    private final StatementService statementService = new StatementService();

    @Test
    void countsEachLogicalAndTernaryOperatorOnce() {
        String source = """
            class Demo {
                int foo(boolean a, boolean b) {
                    return a && b || (a ? 1 : 0) > 0 ? 2 : 3;
                }
            }
            """;
        CompilationUnit cu = StaticJavaParser.parse(source);
        MethodDeclaration method = cu.getClassByName("Demo").orElseThrow()
                .getMethodsByName("foo").get(0);

        StatementsInfo info = statementService.getStatementsInfoFromMethod(method);

        assertEquals(2, info.getNumberOfLogicalOperators());
        assertEquals(2, info.getNumberOfTernaryOperators());
        assertNull(info.getStatements().get(0).getStructure());
    }

    @Test
    void storesMethodCallNameInsteadOfFullSource() {
        String source = """
            class Demo {
                void caller() {
                    callee();
                    int x = 1;
                }
                void callee() {}
            }
            """;
        CompilationUnit cu = StaticJavaParser.parse(source);
        MethodDeclaration method = cu.getClassByName("Demo").orElseThrow()
                .getMethodsByName("caller").get(0);

        StatementsInfo info = statementService.getStatementsInfoFromMethod(method);
        List<String> expressionStructures = info.getStatements().stream()
                .filter(s -> s.getType() == StatementType.EXPRESSION)
                .map(StatementInstance::getStructure)
                .toList();
        assertTrue(expressionStructures.contains("callee"));
        assertTrue(expressionStructures.contains(null));
    }
}
