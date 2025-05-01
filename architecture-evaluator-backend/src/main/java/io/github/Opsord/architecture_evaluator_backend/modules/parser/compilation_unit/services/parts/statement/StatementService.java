// ControlStatementService.java
package io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.parts.statement;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.dto.statement.StatementDTO;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.dto.statement.StatementType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class StatementService {

    private static final Logger logger = LoggerFactory.getLogger(StatementService.class);

    public List<StatementDTO> getStatements(CompilationUnit compilationUnit) {
        logger.info("Extracting statements from compilation unit");
        return extractStatements(compilationUnit);
    }

    public List<StatementDTO> getStatementsFromMethod(MethodDeclaration method) {
        logger.info("Extracting statements from method: {}", method.getNameAsString());
        return extractStatements(method);
    }

    private List<StatementDTO> extractStatements(Node node) {
        List<StatementDTO> statements = new ArrayList<>();
        StatementVisitor visitor = new StatementVisitor();
        node.accept(visitor, statements);
        return statements;
    }

    public int countControlStatements(List<StatementDTO> statements) {
        return (int) statements.stream()
                .filter(statement -> statement.getType() == StatementType.IF ||
                        statement.getType() == StatementType.FOR ||
                        statement.getType() == StatementType.WHILE ||
                        statement.getType() == StatementType.SWITCH)
                .count();
    }

    public int countExecutableStatements(List<StatementDTO> statements) {
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
}