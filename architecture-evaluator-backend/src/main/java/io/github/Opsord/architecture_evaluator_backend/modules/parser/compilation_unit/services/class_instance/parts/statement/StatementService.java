// ControlStatementService.java
package io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.class_instance.parts.statement;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.instances.class_instance.parts.method.parts.StatementsInfo;
import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.instances.class_instance.parts.method.parts.statement.StatementInstance;
import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.instances.class_instance.parts.method.parts.statement.StatementType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class StatementService {

    private static final Logger logger = LoggerFactory.getLogger(StatementService.class);

    public List<StatementInstance> getStatements(ClassOrInterfaceDeclaration classOrInterfaceDeclaration) {
        // logger.info("Extracting statements from compilation unit");
        return extractStatements(classOrInterfaceDeclaration);
    }

    public List<StatementInstance> getStatementsFromMethod(MethodDeclaration method) {
        // logger.info("Extracting statements from method: {}",
        // method.getNameAsString());
        return extractStatements(method);
    }

    private List<StatementInstance> extractStatements(Node node) {
        List<StatementInstance> statements = new ArrayList<>();
        StatementVisitor visitor = new StatementVisitor();
        node.accept(visitor, statements);
        return statements;
    }

    public int countControlStatements(List<StatementInstance> statements) {
        return (int) statements.stream()
                .filter(statement -> statement.getType() == StatementType.IF ||
                        statement.getType() == StatementType.FOR ||
                        statement.getType() == StatementType.WHILE ||
                        statement.getType() == StatementType.SWITCH)
                .count();
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
        info.setNumberOfControlStatements(countControlStatements(statements));
        info.setNumberOfExecutableStatements(countExecutableStatements(statements));
        return info;
    }
}