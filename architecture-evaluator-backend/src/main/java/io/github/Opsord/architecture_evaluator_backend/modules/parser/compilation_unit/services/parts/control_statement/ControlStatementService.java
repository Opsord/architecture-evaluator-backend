// ControlStatementService.java
package io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.parts.control_statement;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.dto.parts.ControlStatementDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ControlStatementService {

    private static final Logger logger = LoggerFactory.getLogger(ControlStatementService.class);

    public List<ControlStatementDTO> getControlStatements(CompilationUnit compilationUnit) {
        logger.info("Extracting control statements from compilation unit");
        return extractControlStatements(compilationUnit);
    }

    public List<ControlStatementDTO> getControlStatementsFromMethod(MethodDeclaration method) {
        logger.info("Extracting control statements from method: {}", method.getNameAsString());
        return extractControlStatements(method);
    }

    private List<ControlStatementDTO> extractControlStatements(Node node) {
        List<ControlStatementDTO> controlStatements = new ArrayList<>();
        ControlStatementVisitor visitor = new ControlStatementVisitor();
        node.accept(visitor, controlStatements);
        return controlStatements;
    }
}