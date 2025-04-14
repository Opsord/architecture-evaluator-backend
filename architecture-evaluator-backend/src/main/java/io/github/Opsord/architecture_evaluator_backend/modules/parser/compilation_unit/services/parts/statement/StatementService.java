// ControlStatementService.java
package io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.parts.statement;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.dto.parts.StatementDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class StatementService {

    private static final Logger logger = LoggerFactory.getLogger(StatementService.class);

    public List<StatementDTO> getControlStatements(CompilationUnit compilationUnit) {
        logger.info("Extracting control statements from compilation unit");
        return extractControlStatements(compilationUnit);
    }

    public List<StatementDTO> getControlStatementsFromMethod(MethodDeclaration method) {
        logger.info("Extracting control statements from method: {}", method.getNameAsString());
        return extractControlStatements(method);
    }

    private List<StatementDTO> extractControlStatements(Node node) {
        List<StatementDTO> controlStatements = new ArrayList<>();
        StatementVisitor visitor = new StatementVisitor();
        node.accept(visitor, controlStatements);
        return controlStatements;
    }
}