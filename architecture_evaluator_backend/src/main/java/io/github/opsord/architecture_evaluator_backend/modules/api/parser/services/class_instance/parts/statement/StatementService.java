// ControlStatementService.java
package io.github.opsord.architecture_evaluator_backend.modules.api.parser.services.class_instance.parts.statement;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.Expression;
import io.github.opsord.architecture_evaluator_backend.modules.api.parser.instances.class_instance.parts.method.parts.StatementsInfo;
import io.github.opsord.architecture_evaluator_backend.modules.api.parser.instances.class_instance.parts.method.parts.statement.StatementInstance;
import io.github.opsord.architecture_evaluator_backend.modules.api.parser.instances.class_instance.parts.method.parts.statement.StatementType;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class StatementService {

    public List<StatementInstance> getStatements(ClassOrInterfaceDeclaration classOrInterfaceDeclaration) {
        return extractStatements(classOrInterfaceDeclaration);
    }

    public List<StatementInstance> getStatementsFromMethod(MethodDeclaration method) {
        return extractStatements(method);
    }

    private List<StatementInstance> extractStatements(Node node) {
        List<StatementInstance> statements = new ArrayList<>();
        StatementVisitor visitor = new StatementVisitor();
        node.accept(visitor, statements);
        return statements;
    }

    public int countExecutableStatements(List<StatementInstance> statements) {
        return (int) statements.stream()
                .filter(statement -> statement.getType() == StatementType.IF ||
                        statement.getType() == StatementType.FOR ||
                        statement.getType() == StatementType.WHILE ||
                        statement.getType() == StatementType.SWITCH ||
                        statement.getType() == StatementType.EXPRESSION ||
                        statement.getType() == StatementType.RETURN ||
                        statement.getType() == StatementType.THROW ||
                        statement.getType() == StatementType.TRY)
                .count();
    }

    public StatementsInfo getStatementsInfoFromMethod(MethodDeclaration method) {
        List<StatementInstance> statements = getStatementsFromMethod(method);
        StatementsInfo info = new StatementsInfo();
        info.setStatements(statements);
        info.setNumberOfStatements(statements.size());
        info.setNumberOfControlStatements((int) statements.stream().filter(s ->
                s.getType() == StatementType.IF ||
                        s.getType() == StatementType.FOR ||
                        s.getType() == StatementType.WHILE ||
                        s.getType() == StatementType.SWITCH ||
                        s.getType() == StatementType.TRY ||
                        s.getType() == StatementType.CATCH).count());
        info.setNumberOfExecutableStatements((int) statements.stream().filter(s ->
                s.getType() == StatementType.IF ||
                        s.getType() == StatementType.FOR ||
                        s.getType() == StatementType.WHILE ||
                        s.getType() == StatementType.SWITCH ||
                        s.getType() == StatementType.EXPRESSION ||
                        s.getType() == StatementType.RETURN ||
                        s.getType() == StatementType.THROW ||
                        s.getType() == StatementType.TRY).count());
        info.setNumberOfReturnStatements((int) statements.stream()
                .filter(statementInstance -> statementInstance.getType() == StatementType.RETURN).count());
        info.setNumberOfThrowStatements((int) statements.stream()
                .filter(statementInstance -> statementInstance.getType() == StatementType.THROW).count());
        info.setNumberOfCatchClauses((int) statements.stream()
                .filter(statementType -> statementType.getType() == StatementType.CATCH).count());

        // Count logical and ternary operators
        int logicalOps = 0;
        int ternaryOps = 0;
        if (method.getBody().isPresent()) {
            for (Expression expr : method.getBody().get().findAll(Expression.class)) {
                logicalOps += StatementVisitor.countLogicalOperators(expr);
                ternaryOps += StatementVisitor.countTernaryOperators(expr);
            }
        }
        info.setNumberOfLogicalOperators(logicalOps);
        info.setNumberOfTernaryOperators(ternaryOps);
        return info;
    }
}